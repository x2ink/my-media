package ink.x2.mymedia.domain.model.torrent

data class TorrentDownloadTask(
    val id: Long,
    val magnetUri: String,
    val infoHash: String,
    val title: String,
    val selectedFileIndexes: List<Int>,
    val selectedFileNames: List<String>,
    val status: TorrentDownloadStatus,
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val downloadSpeedBytes: Long,
    val peersCount: Int,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long
)
