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
    val albumId: Long,
    val mediaType: MediaType,
    val albumBytes: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalMediaItem

        if (id != other.id) return false
        if (duration != other.duration) return false
        if (size != other.size) return false
        if (dateAdded != other.dateAdded) return false
        if (albumId != other.albumId) return false
        if (title != other.title) return false
        if (uriString != other.uriString) return false
        if (artist != other.artist) return false
        if (mimeType != other.mimeType) return false
        if (mediaType != other.mediaType) return false
        if (!albumBytes.contentEquals(other.albumBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + dateAdded.hashCode()
        result = 31 * result + albumId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + uriString.hashCode()
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + (albumBytes?.contentHashCode() ?: 0)
        return result
    }
}