package ink.x2.mymedia.domain.usecase

import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.repository.ScanRepository
import javax.inject.Inject
class ScanMediaUseCase @Inject constructor(private val scanRepository: ScanRepository) {
    suspend fun queryScanMediaResult(type: MediaType):List<LocalMediaItem>{
        return scanRepository.queryScanMediaResult(type)
    }
}