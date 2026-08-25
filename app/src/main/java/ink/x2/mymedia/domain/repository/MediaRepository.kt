package ink.x2.mymedia.domain.repository

import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getMediaList(type: MediaType): Flow<List<MediaEntity>>
    suspend fun updateMediaItemInfo(mediaItem: MediaEntity):AppResult<Unit>
    suspend fun deleteMediaItem(mediaItem: MediaEntity):AppResult<Unit>
}
