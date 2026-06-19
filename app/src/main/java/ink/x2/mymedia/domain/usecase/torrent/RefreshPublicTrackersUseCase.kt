package ink.x2.mymedia.domain.usecase.torrent

import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import javax.inject.Inject

class RefreshPublicTrackersUseCase @Inject constructor(
    private val torrentRepository: TorrentRepository
) {
    suspend operator fun invoke(): Int = torrentRepository.refreshPublicTrackers()
}
