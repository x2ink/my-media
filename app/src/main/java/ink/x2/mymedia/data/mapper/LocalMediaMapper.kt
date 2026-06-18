package ink.x2.mymedia.data.mapper

import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType

fun MediaEntity.toLocalMedia(): LocalMedia {
    return LocalMedia(
        id = id,
        type = MediaType.valueOf(type),
        title = title,
        artist = artist,
        durationMs = durationMs,
        sizeBytes = sizeBytes,
        mimeType = mimeType,
        localRelativePath = localRelativePath,
        sourceUri = sourceUri,
        importedAt = importedAt,
        lastPlayedAt = lastPlayedAt,
        lastPositionMs = lastPositionMs,
        playCount = playCount
    )
}
