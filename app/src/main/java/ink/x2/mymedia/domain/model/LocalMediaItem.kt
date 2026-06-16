package ink.x2.mymedia.domain.model

import android.net.Uri

data class LocalMediaItem(
    val id: Long,
    val title: String,
    val uri: Uri,
    val artist: String?,
    val duration: Long,
    val size: Long,
    val mimeType: String?,
    val dateAdded: Long,
    val albumId: Int
)