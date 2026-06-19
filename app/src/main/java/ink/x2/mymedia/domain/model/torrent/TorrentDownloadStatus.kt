package ink.x2.mymedia.domain.model.torrent

enum class TorrentDownloadStatus {
    FETCHING_METADATA,
    READY,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}
