package ink.x2.mymedia.data.repository

import ink.x2.mymedia.core.common.AppError
import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.mapper.toLocalMedia
import ink.x2.mymedia.di.IoDispatcher
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.repository.MediaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MediaRepository {
    override fun getMediaList(type: MediaType): Flow<List<LocalMedia>> {
        return mediaDao.getMediaByTypeFlow(type.name).map {
            it.map { mediaItem->
                mediaItem.toLocalMedia()
            }
        }
    }

    override suspend fun updateMediaItemTitleById(title: String,id: Long): AppResult<Unit> {
        try {
            mediaDao.updateMediaItemTitleById(title,id, System.currentTimeMillis())
            return AppResult.Success(Unit)
        }catch (e: Exception){
            return AppResult.Error(AppError.Unknown(e))
        }
    }

    override suspend fun deleteMediaItemById(id: Long): AppResult<Unit> = withContext(ioDispatcher){
        try {
            val mediaItem=mediaDao.getMediaItemById(id).toLocalMedia()
            val file= File(mediaItem.localRelativePath)
            if(file.exists()&&!file.delete()){
                return@withContext AppResult.Error(AppError.Unknown(IOException("文件删除失败")))
            }
            mediaDao.deleteMediaItemById(id)
            return@withContext  AppResult.Success(Unit)
        }catch (e: Exception){
            return@withContext AppResult.Error(AppError.Unknown(e))
        }
    }

    override suspend fun getMediaItemById(id: Long): AppResult<LocalMedia> {
        return try {
            AppResult.Success(mediaDao.getMediaItemById(id).toLocalMedia())
        }catch (e: Exception){
            AppResult.Error(AppError.Unknown(e))
        }
    }
}
