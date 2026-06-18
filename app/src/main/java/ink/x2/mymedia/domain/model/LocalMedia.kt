package ink.x2.mymedia.domain.model

data class LocalMedia(
    val id: Long,
    val type: MediaType,
    val title: String,
    val artist: String?,
    val durationMs: Long,
    val sizeBytes: Long,
    val mimeType: String?,
    val localRelativePath: String,
    val sourceUri: String,
    val importedAt: Long,
    val lastPlayedAt: Long?,
    val lastPositionMs: Long,
    val playCount: Int
)
