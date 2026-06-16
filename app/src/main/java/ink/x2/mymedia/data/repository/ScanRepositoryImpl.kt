package ink.x2.mymedia.data.repository

import ink.x2.mymedia.data.source.mediastore.MediaStoreScanner
import ink.x2.mymedia.domain.repository.ScanRepository
import javax.inject.Inject
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType

class ScanRepositoryImpl @Inject constructor(
private val mediaStoreScanner: MediaStoreScanner
) : ScanRepository{

    override suspend fun queryScanMediaResult(type: MediaType): List<LocalMediaItem> {
        return mediaStoreScanner.scanMedia(type)
    }
}