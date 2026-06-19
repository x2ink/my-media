package ink.x2.mymedia.data.source.torrent

import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeLibtorrentBridge @Inject constructor() {
    init {
        System.loadLibrary("mymedia_libtorrent")
    }

    external fun nativeVersion(): String

    fun fetchMetadata(
        magnetUri: String,
        timeoutSeconds: Int,
        maxMetadataBytes: Int
    ): NativeTorrentMetadataResult {
        val result = nativeFetchMetadata(magnetUri, timeoutSeconds, maxMetadataBytes)
        require(result.size == 2) { "libtorrent 返回的元数据结果无效" }
        val metadataJson = result[0] as? String
            ?: error("libtorrent 返回的元数据 JSON 无效")
        val torrentData = result[1] as? ByteArray
            ?: error("libtorrent 返回的 torrent 数据无效")
        return NativeTorrentMetadataResult(metadataJson, torrentData)
    }

    external fun nativeFetchMetadata(
        magnetUri: String,
        timeoutSeconds: Int,
        maxMetadataBytes: Int
    ): Array<Any>

    external fun nativeParseTorrentMetadata(torrentData: ByteArray): String

    external fun nativeStartDownload(
        magnetUri: String,
        torrentData: ByteArray,
        selectedFileIndexes: IntArray,
        saveDir: String
    ): String

    external fun nativePause(infoHash: String)

    external fun nativeResume(infoHash: String)

    external fun nativeRemove(infoHash: String)

    external fun nativeSnapshot(infoHash: String): String?

    external fun nativeDownloadedFiles(
        infoHash: String,
        selectedFileIndexes: IntArray,
        saveDir: String
    ): String

    external fun nativeUpdatePublicTrackers(trackers: Array<String>)
}

data class NativeTorrentMetadataResult(
    val metadataJson: String,
    val torrentData: ByteArray
)

internal fun String.toJsonObject(): JSONObject = JSONObject(this)
