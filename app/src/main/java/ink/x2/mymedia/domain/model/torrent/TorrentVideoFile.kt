package ink.x2.mymedia.domain.model.torrent

data class TorrentVideoFile(
    val index: Int,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val extension: String
)
