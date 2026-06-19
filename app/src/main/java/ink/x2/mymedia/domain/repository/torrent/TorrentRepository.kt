package ink.x2.mymedia.domain.repository.torrent

import ink.x2.mymedia.domain.model.torrent.TorrentDownloadTask
import ink.x2.mymedia.domain.model.torrent.TorrentMetadata
import kotlinx.coroutines.flow.Flow

interface TorrentRepository {
    val downloadTasks: Flow<List<TorrentDownloadTask>>

    suspend fun fetchMetadata(magnetUri: String): TorrentMetadata

    suspend fun fetchTorrentFileMetadata(uriString: String): TorrentMetadata

    suspend fun refreshPublicTrackers(): Int

    suspend fun startDownload(
        metadata: TorrentMetadata,
        selectedFileIndexes: List<Int>
    ): Long

    suspend fun pauseDownload(taskId: Long)

    suspend fun resumeDownload(taskId: Long)

    suspend fun deleteDownload(taskId: Long)
}
