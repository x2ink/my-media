package ink.x2.mymedia.domain.usecase

import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioLibraryUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(): Flow<List<LocalMedia>> {
        return mediaRepository.getMediaList(MediaType.AUDIO)
    }
}
