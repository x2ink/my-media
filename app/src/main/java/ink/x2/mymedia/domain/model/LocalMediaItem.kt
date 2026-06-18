package ink.x2.mymedia.domain.model

data class LocalMediaItem(
    val id: Long,
    val title: String,
    val uriString: String,
    val artist: String?,
    val duration: Long,
    val size: Long,
    val mimeType: String?,
    val dateAdded: Long,
    val mediaType: MediaType,
)