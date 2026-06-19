package ink.x2.mymedia.domain.model.torrent

data class TorrentMetadata(
    val magnetUri: String,
    val infoHash: String,
    val name: String,
    val totalSizeBytes: Long,
    val videoFiles: List<TorrentVideoFile>
)
