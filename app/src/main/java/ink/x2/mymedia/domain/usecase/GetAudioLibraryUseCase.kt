package ink.x2.mymedia.domain.usecase

import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAudioLibraryUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaEntity>> {
        return mediaRepository.getMediaList(MediaType.AUDIO)
    }
}
