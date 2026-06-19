package ink.x2.mymedia.data.repository.torrent

import android.content.Context
import android.net.Uri
import com.orhanobut.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.local.db.dao.torrent.TorrentTaskDao
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.local.db.entity.torrent.TorrentTaskEntity
import ink.x2.mymedia.data.local.storage.PrivateMediaStorage
import ink.x2.mymedia.data.source.torrent.TorrentEngine
import ink.x2.mymedia.di.IoDispatcher
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.model.torrent.TorrentDownloadStatus
import ink.x2.mymedia.domain.model.torrent.TorrentDownloadTask
import ink.x2.mymedia.domain.model.torrent.TorrentMetadata
import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorrentRepositoryImpl @Inject constructor(
    private val torrentEngine: TorrentEngine,
    private val torrentTaskDao: TorrentTaskDao,
    private val mediaDao: MediaDao,
    private val privateMediaStorage: PrivateMediaStorage,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TorrentRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val metadataCache = mutableMapOf<String, ByteArray>()

    init {
        scope.launch {
            restoreActiveTasks()
        }
    }

    override val downloadTasks: Flow<List<TorrentDownloadTask>> =
        torrentTaskDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun fetchMetadata(magnetUri: String): TorrentMetadata = withContext(ioDispatcher) {
        require(magnetUri.trim().startsWith("magnet:?xt=urn:btih:", ignoreCase = true)) {
            "请输入有效的 magnet 链接"
        }
        val (metadata, data) = torrentEngine.fetchMetadata(magnetUri.trim())
        require(metadata.videoFiles.isNotEmpty()) {
            "该磁力未解析到可下载的视频文件"
        }
        metadataCache[metadata.infoHash] = data
        metadata
    }

    override suspend fun fetchTorrentFileMetadata(uriString: String): TorrentMetadata = withContext(ioDispatcher) {
        val torrentData = readTorrentFileBytes(uriString)
        val (metadata, data) = torrentEngine.parseTorrentData(torrentData)
        require(metadata.videoFiles.isNotEmpty()) {
            "该种子未解析到可下载的视频文件"
        }
        metadataCache[metadata.infoHash] = data
        metadata
    }

    override suspend fun refreshPublicTrackers(): Int = withContext(ioDispatcher) {
        torrentEngine.refreshPublicTrackers()
    }

    override suspend fun startDownload(
        metadata: TorrentMetadata,
        selectedFileIndexes: List<Int>
    ): Long = withContext(ioDispatcher) {
        require(selectedFileIndexes.isNotEmpty()) { "请选择至少一个视频文件" }
        val selectedFiles = metadata.videoFiles.filter { it.index in selectedFileIndexes.toSet() }
        require(selectedFiles.isNotEmpty()) { "选择的文件不在视频列表中" }

        val saveDir = File(privateMediaStorage.getTorrentVideoDir(), metadata.infoHash).apply {
            if (!exists()) mkdirs()
        }
        val totalBytes = selectedFiles.sumOf { it.sizeBytes }
        val taskId = torrentTaskDao.insert(
            TorrentTaskEntity(
                magnetUri = metadata.magnetUri,
                infoHash = metadata.infoHash,
                title = selectedFiles.singleOrNull()?.name ?: metadata.name,
                selectedFileIndexes = selectedFileIndexes.toPersistedIntList(),
                selectedFileNames = selectedFiles.map { it.name }.toPersistedStringList(),
                status = TorrentDownloadStatus.DOWNLOADING.name,
                totalBytes = totalBytes,
                saveDir = saveDir.absolutePath
            )
        )

        val torrentData = resolveTorrentData(metadata, saveDir).also {
            persistTorrentData(saveDir, it)
        }
        runCatching {
            torrentEngine.startDownload(
                magnetUri = metadata.magnetUri,
                torrentData = torrentData,
                selectedFileIndexes = selectedFileIndexes,
                saveDir = saveDir
            )
            monitorTask(taskId)
        }.onFailure { error ->
            updateStatus(taskId, TorrentDownloadStatus.FAILED, error.message)
        }
        taskId
    }

    private fun resolveTorrentData(metadata: TorrentMetadata, saveDir: File): ByteArray {
        return metadataCache[metadata.infoHash]
            ?: readPersistedTorrentData(saveDir)?.also {
                metadataCache[metadata.infoHash] = it
            }
            ?: torrentEngine.fetchMetadata(metadata.magnetUri).second.also {
                metadataCache[metadata.infoHash] = it
            }
    }

    override suspend fun pauseDownload(taskId: Long) = withContext(ioDispatcher) {
        val entity = torrentTaskDao.getById(taskId) ?: return@withContext
        torrentEngine.pause(entity.infoHash)
        torrentTaskDao.update(
            entity.copy(
                status = TorrentDownloadStatus.PAUSED.name,
                downloadSpeedBytes = 0,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun resumeDownload(taskId: Long) = withContext(ioDispatcher) {
        val entity = torrentTaskDao.getById(taskId) ?: return@withContext
        torrentEngine.resume(entity.infoHash)
        torrentTaskDao.update(
            entity.copy(
                status = TorrentDownloadStatus.DOWNLOADING.name,
                updatedAt = System.currentTimeMillis()
            )
        )
        monitorTask(taskId)
    }

    override suspend fun deleteDownload(taskId: Long) = withContext(ioDispatcher) {
        val entity = torrentTaskDao.getById(taskId) ?: return@withContext
        torrentEngine.remove(entity.infoHash)
        torrentTaskDao.deleteById(taskId)
    }

    private fun monitorTask(taskId: Long) {
        scope.launch {
            while (true) {
                val entity = torrentTaskDao.getById(taskId) ?: return@launch
                if (entity.status == TorrentDownloadStatus.PAUSED.name ||
                    entity.status == TorrentDownloadStatus.COMPLETED.name ||
                    entity.status == TorrentDownloadStatus.FAILED.name
                ) {
                    return@launch
                }

                val snapshot = torrentEngine.snapshot(entity.infoHash)
                if (snapshot == null) {
                    delay(PROGRESS_INTERVAL_MS)
                    continue
                }

                val nextStatus = if (snapshot.isFinished) {
                    TorrentDownloadStatus.COMPLETED
                } else {
                    TorrentDownloadStatus.DOWNLOADING
                }
                torrentTaskDao.update(
                    entity.copy(
                        status = nextStatus.name,
                        progress = snapshot.progress,
                        downloadedBytes = snapshot.downloadedBytes,
                        totalBytes = snapshot.totalBytes.takeIf { it > 0L } ?: entity.totalBytes,
                        downloadSpeedBytes = if (snapshot.isFinished) 0L else snapshot.downloadSpeedBytes,
                        peersCount = snapshot.peersCount,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                if (snapshot.isFinished) {
                    importFinishedVideos(entity)
                    return@launch
                }

                delay(PROGRESS_INTERVAL_MS)
            }
        }
    }

    private suspend fun restoreActiveTasks() {
        torrentTaskDao.getByStatuses(
            listOf(
                TorrentDownloadStatus.DOWNLOADING.name,
                TorrentDownloadStatus.READY.name,
                TorrentDownloadStatus.FETCHING_METADATA.name
            )
        ).forEach { entity ->
            runCatching {
                val torrentData = metadataCache[entity.infoHash]
                    ?: readPersistedTorrentData(File(entity.saveDir))?.also {
                        metadataCache[entity.infoHash] = it
                    }
                    ?: torrentEngine.fetchMetadata(entity.magnetUri).second.also {
                        metadataCache[entity.infoHash] = it
                    }
                torrentEngine.startDownload(
                    magnetUri = entity.magnetUri,
                    torrentData = torrentData,
                    selectedFileIndexes = entity.selectedFileIndexes.toIntList(),
                    saveDir = File(entity.saveDir)
                )
                torrentTaskDao.update(
                    entity.copy(
                        status = TorrentDownloadStatus.DOWNLOADING.name,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                monitorTask(entity.id)
            }.onFailure { error ->
                updateStatus(entity.id, TorrentDownloadStatus.FAILED, error.message)
            }
        }
    }

    private fun readTorrentFileBytes(uriString: String): ByteArray {
        val uri = Uri.parse(uriString)
        return context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("无法读取种子文件")
    }

    private fun persistTorrentData(saveDir: File, torrentData: ByteArray) {
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        File(saveDir, TORRENT_METADATA_FILE_NAME).writeBytes(torrentData)
    }

    private fun readPersistedTorrentData(saveDir: File): ByteArray? {
        val file = File(saveDir, TORRENT_METADATA_FILE_NAME)
        return file.takeIf { it.exists() && it.isFile }?.readBytes()
    }

    private suspend fun importFinishedVideos(entity: TorrentTaskEntity) {
        runCatching {
            val files = torrentEngine.downloadedFiles(
                infoHash = entity.infoHash,
                selectedFileIndexes = entity.selectedFileIndexes.toIntList(),
                saveDir = File(entity.saveDir)
            )
            val mediaEntities = files.mapNotNull { file ->
                val hash = privateMediaStorage.calculateFileHash(file) ?: return@mapNotNull null
                if (mediaDao.exitsByHash(hash)) {
                    return@mapNotNull null
                }
                MediaEntity(
                    type = MediaType.VIDEO.name,
                    title = file.nameWithoutExtension,
                    artist = null,
                    durationMs = 0L,
                    sizeBytes = file.length(),
                    mimeType = file.toVideoMimeType(),
                    localRelativePath = file.absolutePath,
                    sourceUri = entity.magnetUri,
                    hash = hash
                )
            }
            if (mediaEntities.isNotEmpty()) {
                mediaDao.insertAll(mediaEntities)
            }
        }.onFailure { error ->
            Logger.e(error, "Import torrent videos failed")
            updateStatus(entity.id, TorrentDownloadStatus.FAILED, error.message)
        }
    }

    private suspend fun updateStatus(
        taskId: Long,
        status: TorrentDownloadStatus,
        errorMessage: String?
    ) {
        val entity = torrentTaskDao.getById(taskId) ?: return
        torrentTaskDao.update(
            entity.copy(
                status = status.name,
                errorMessage = errorMessage,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun File.toVideoMimeType(): String {
        return when (extension.lowercase()) {
            "mp4", "m4v" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "ts" -> "video/mp2t"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            else -> "video/*"
        }
    }

    companion object {
        private const val PROGRESS_INTERVAL_MS = 1000L
        private const val TORRENT_METADATA_FILE_NAME = "metadata.torrent"
    }
}
