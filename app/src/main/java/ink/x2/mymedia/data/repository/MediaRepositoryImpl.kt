package ink.x2.mymedia.data.repository

import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.mapper.toLocalMedia
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
    override fun getMediaList(type: MediaType): Flow<List<LocalMedia>> {
        return mediaDao.getMediaByTypeFlow(type.name).map { entities ->
            entities.map { it.toLocalMedia() }
        }
    }
}
