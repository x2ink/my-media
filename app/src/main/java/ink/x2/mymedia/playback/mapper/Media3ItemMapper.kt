package ink.x2.mymedia.playback.mapper

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import java.io.File

fun LocalMediaItem.toMediaItem(): MediaItem{
    val mediaMetadata= MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setDurationMs(duration)
        .setExtras(Bundle().apply {
            putString("uri",uriString)
        })
        .setMediaType(when(mediaType){
            MediaType.VIDEO->MediaMetadata.MEDIA_TYPE_VIDEO
            MediaType.AUDIO->MediaMetadata.MEDIA_TYPE_MUSIC
        })
        .build()
    return MediaItem.Builder().setMediaId(id.toString()).setUri(uriString.toUri()).setMediaMetadata(mediaMetadata).build()
}
fun List<LocalMediaItem>.toMediaItemList(): List<MediaItem> {
    return this.map { it.toMediaItem() }
}

fun LocalMedia.toMediaItem(): MediaItem {
    val mediaMetadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setDurationMs(durationMs)
        .build()
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(File(localRelativePath).toUri())
        .setMediaMetadata(mediaMetadata)
        .build()
}

@JvmName("toMediaItemListLocalMedia")
fun List<LocalMedia>.toMediaItemList(): List<MediaItem> {
    return this.map { it.toMediaItem() }
}