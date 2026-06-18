package ink.x2.mymedia.domain.repository

import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getMediaList(type: MediaType): Flow<List<LocalMedia>>
}
