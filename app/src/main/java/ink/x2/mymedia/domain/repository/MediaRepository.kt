package ink.x2.mymedia.domain.repository

import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getMediaList(type: MediaType): Flow<List<LocalMedia>>
    suspend fun updateMediaItemTitleById(title: String,id: Long):AppResult<Unit>
    suspend fun deleteMediaItemById(id: Long):AppResult<Unit>
    suspend fun getMediaItemById(id: Long) : AppResult<LocalMedia>
}
