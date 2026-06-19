package ink.x2.mymedia.domain.usecase.torrent

import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import javax.inject.Inject

class ResumeTorrentDownloadUseCase @Inject constructor(
    private val torrentRepository: TorrentRepository
) {
    suspend operator fun invoke(taskId: Long) = torrentRepository.resumeDownload(taskId)
}
