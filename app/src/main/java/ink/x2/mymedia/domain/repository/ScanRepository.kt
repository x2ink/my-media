package ink.x2.mymedia.domain.repository

import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType

interface ScanRepository {
    suspend fun queryScanMediaResult(type: MediaType): List<LocalMediaItem>
}