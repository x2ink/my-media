package ink.x2.mymedia.domain.usecase

import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.domain.repository.MediaRepository
import javax.inject.Inject

class DeleteMediaItemUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(id: Long) : AppResult<Unit>{
       return mediaRepository.deleteMediaItemById(id)
    }
}