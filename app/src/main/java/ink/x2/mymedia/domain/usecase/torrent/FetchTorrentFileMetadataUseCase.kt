package ink.x2.mymedia.domain.usecase.torrent

import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import javax.inject.Inject

class FetchTorrentFileMetadataUseCase @Inject constructor(
    private val torrentRepository: TorrentRepository
) {
    suspend operator fun invoke(uriString: String) = torrentRepository.fetchTorrentFileMetadata(uriString)
}
