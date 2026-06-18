package ink.x2.mymedia.domain.repository

import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType

interface ScanRepository {
    suspend fun queryScanMediaResult(type: MediaType): AppResult<List<LocalMediaItem>>
    suspend fun importSelectdMedia(mediaList:List<LocalMediaItem>) : AppResult<Unit>
}