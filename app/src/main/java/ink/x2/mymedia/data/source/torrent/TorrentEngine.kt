package ink.x2.mymedia.data.source.torrent

import ink.x2.mymedia.domain.model.torrent.TorrentMetadata
import ink.x2.mymedia.domain.model.torrent.TorrentVideoFile
import org.json.JSONArray
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorrentEngine @Inject constructor(
    private val bridge: NativeLibtorrentBridge,
    private val trackerListDataSource: TorrentTrackerListDataSource
) {
    @Volatile
    private var publicTrackers: List<String> = TorrentEngineDefaultTrackers.values

    fun refreshPublicTrackers(): Int {
        val trackers = trackerListDataSource.fetchBestTrackers()
        publicTrackers = trackers
        bridge.nativeUpdatePublicTrackers(trackers.toTypedArray())
        return trackers.size
    }

    fun fetchMetadata(magnetUri: String): Pair<TorrentMetadata, ByteArray> {
        val enhancedMagnetUri = magnetUri.withDefaultTrackers(publicTrackers)
        val result = bridge.fetchMetadata(
            magnetUri = enhancedMagnetUri,
            timeoutSeconds = METADATA_TIMEOUT_SECONDS,
            maxMetadataBytes = MAX_METADATA_BYTES
        )
        return result.metadataJson.toMetadata(enhancedMagnetUri) to result.torrentData
    }

    fun parseTorrentData(torrentData: ByteArray): Pair<TorrentMetadata, ByteArray> {
        val metadataJson = bridge.nativeParseTorrentMetadata(torrentData)
        val metadata = metadataJson.toMetadata(TORRENT_FILE_SOURCE_PREFIX)
        return metadata.copy(magnetUri = "$TORRENT_FILE_SOURCE_PREFIX${metadata.infoHash}") to torrentData
    }

    @Synchronized
    fun startDownload(
        magnetUri: String,
        torrentData: ByteArray,
        selectedFileIndexes: List<Int>,
        saveDir: File
    ): String {
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        return bridge.nativeStartDownload(
            magnetUri = magnetUri.withDefaultTrackers(publicTrackers),
            torrentData = torrentData,
            selectedFileIndexes = selectedFileIndexes.toIntArray(),
            saveDir = saveDir.absolutePath
        )
    }

    @Synchronized
    fun pause(infoHash: String) {
        bridge.nativePause(infoHash)
    }

    @Synchronized
    fun resume(infoHash: String) {
        bridge.nativeResume(infoHash)
    }

    @Synchronized
    fun remove(infoHash: String) {
        bridge.nativeRemove(infoHash)
    }

    @Synchronized
    fun snapshot(infoHash: String): TorrentRuntimeSnapshot? {
        val json = bridge.nativeSnapshot(infoHash) ?: return null
        val status = json.toJsonObject()
        return TorrentRuntimeSnapshot(
            progress = status.optInt("progress", 0).coerceIn(0, 100),
            downloadedBytes = status.optLong("downloadedBytes", 0L),
            totalBytes = status.optLong("totalBytes", 0L),
            downloadSpeedBytes = status.optLong("downloadSpeedBytes", 0L),
            peersCount = status.optInt("peersCount", 0),
            isFinished = status.optBoolean("isFinished", false)
        )
    }

    @Synchronized
    fun downloadedFiles(infoHash: String, selectedFileIndexes: List<Int>, saveDir: File): List<File> {
        val json = bridge.nativeDownloadedFiles(
            infoHash = infoHash,
            selectedFileIndexes = selectedFileIndexes.toIntArray(),
            saveDir = saveDir.absolutePath
        )
        val files = JSONArray(json)
        return buildList {
            for (index in 0 until files.length()) {
                val file = File(files.getString(index))
                if (file.exists() && file.isFile) {
                    add(file)
                }
            }
        }
    }

    private fun String.toMetadata(magnetUri: String): TorrentMetadata {
        val json = toJsonObject()
        val videoFilesJson = json.getJSONArray("videoFiles")
        val videoFiles = buildList {
            for (index in 0 until videoFilesJson.length()) {
                val item = videoFilesJson.getJSONObject(index)
                add(
                    TorrentVideoFile(
                        index = item.getInt("index"),
                        path = item.getString("path"),
                        name = item.getString("name"),
                        sizeBytes = item.getLong("sizeBytes"),
                        extension = item.getString("extension")
                    )
                )
            }
        }
        return TorrentMetadata(
            magnetUri = magnetUri,
            infoHash = json.getString("infoHash"),
            name = json.getString("name"),
            totalSizeBytes = json.getLong("totalSizeBytes"),
            videoFiles = videoFiles
        )
    }

    companion object {
        private const val METADATA_TIMEOUT_SECONDS = 120
        private const val MAX_METADATA_BYTES = 8 * 1024 * 1024
        const val TORRENT_FILE_SOURCE_PREFIX = "torrent:"
    }
}

private fun String.withDefaultTrackers(trackers: List<String>): String {
    val clean = trim()
    val lower = clean.lowercase()
    if (!lower.startsWith("magnet:?xt=urn:btih:")) {
        return clean
    }
    val existing = clean.substringAfter("?", "")
        .split("&")
        .filter { it.startsWith("tr=", ignoreCase = true) }
        .map { it.substringAfter("tr=") }
        .toSet()
    val trackerParams = trackers
        .map { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }
        .filterNot { it in existing }
        .joinToString(separator = "") { "&tr=$it" }
    return clean + trackerParams
}

private object TorrentEngineDefaultTrackers {
    val values = listOf(
        "udp://tracker.opentrackr.org:1337/announce",
        "udp://open.stealth.si:80/announce",
        "udp://tracker.torrent.eu.org:451/announce",
        "udp://tracker.bittor.pw:1337/announce",
        "udp://exodus.desync.com:6969/announce",
        "udp://open.demonii.com:1337/announce",
        "udp://tracker.moeking.me:6969/announce",
        "udp://explodie.org:6969/announce",
        "https://tracker.tamersunion.org:443/announce",
        "https://tracker.gbitt.info:443/announce"
    )
}

data class TorrentRuntimeSnapshot(
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val downloadSpeedBytes: Long,
    val peersCount: Int,
    val isFinished: Boolean
)
