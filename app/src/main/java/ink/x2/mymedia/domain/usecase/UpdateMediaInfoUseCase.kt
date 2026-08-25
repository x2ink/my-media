package ink.x2.mymedia.domain.usecase

import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.domain.repository.MediaRepository
import javax.inject.Inject

class UpdateMediaInfoUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(media: MediaEntity): AppResult<Unit>{
        return mediaRepository.updateMediaItemInfo(media)
    }
}