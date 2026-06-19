package ink.x2.mymedia.domain.usecase.torrent

import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import javax.inject.Inject

class GetTorrentDownloadTasksUseCase @Inject constructor(
    private val torrentRepository: TorrentRepository
) {
    operator fun invoke() = torrentRepository.downloadTasks
}
