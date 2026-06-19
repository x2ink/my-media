package ink.x2.mymedia.domain.usecase.torrent

import ink.x2.mymedia.domain.model.torrent.TorrentMetadata
import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import javax.inject.Inject

class StartTorrentDownloadUseCase @Inject constructor(
    private val torrentRepository: TorrentRepository
) {
    suspend operator fun invoke(
        metadata: TorrentMetadata,
        selectedFileIndexes: List<Int>
    ): Long = torrentRepository.startDownload(metadata, selectedFileIndexes)
}
