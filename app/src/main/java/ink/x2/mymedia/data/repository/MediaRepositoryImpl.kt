package ink.x2.mymedia.data.repository

import ink.x2.mymedia.core.common.AppError
import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.mapper.toLocalMedia
import ink.x2.mymedia.domain.model.ImportProgress
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao
) : MediaRepository {
    override fun getMediaList(type: MediaType): Flow<List<MediaEntity>> {
        return mediaDao.getMediaByTypeFlow(type.name)
    }

    override suspend fun updateMediaItemInfo(mediaItem: MediaEntity): AppResult<Unit> {
        try {
            mediaDao.updateMediaItemInfo(mediaItem)
            return AppResult.Success(Unit)
        }catch (e: Exception){
            return AppResult.Error(AppError.Unknown(e))
        }
    }

    override suspend fun deleteMediaItem(mediaItem: MediaEntity): AppResult<Unit> {
        try {
            mediaDao.deleteMediaItem(mediaItem)
            return AppResult.Success(Unit)
        }catch (e: Exception){
            return AppResult.Error(AppError.Unknown(e))
        }
    }
}
