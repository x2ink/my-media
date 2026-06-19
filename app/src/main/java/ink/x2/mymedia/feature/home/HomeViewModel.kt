package ink.x2.mymedia.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.core.ext.toSizeString
import ink.x2.mymedia.domain.model.torrent.TorrentDownloadStatus
import ink.x2.mymedia.domain.model.torrent.TorrentMetadata
import ink.x2.mymedia.domain.usecase.GetVideoLibraryUseCase
import ink.x2.mymedia.domain.usecase.torrent.DeleteTorrentDownloadUseCase
import ink.x2.mymedia.domain.usecase.torrent.FetchTorrentFileMetadataUseCase
import ink.x2.mymedia.domain.usecase.torrent.FetchTorrentMetadataUseCase
import ink.x2.mymedia.domain.usecase.torrent.GetTorrentDownloadTasksUseCase
import ink.x2.mymedia.domain.usecase.torrent.PauseTorrentDownloadUseCase
import ink.x2.mymedia.domain.usecase.torrent.ResumeTorrentDownloadUseCase
import ink.x2.mymedia.domain.usecase.torrent.StartTorrentDownloadUseCase
import ink.x2.mymedia.feature.video.VideoItemUiState
import ink.x2.mymedia.playback.controller.PlaybackController
import ink.x2.mymedia.playback.mapper.toMediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadTask(
    val id: Long,
    val title: String,
    val progress: Int,
    val downloadSpeed: String,
    val peersCount: Int,
    val totalSize: Long,
    val downloadedSize: Long,
    val status: TorrentDownloadStatus,
    val isPaused: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVideoLibraryUseCase: GetVideoLibraryUseCase,
    private val fetchTorrentMetadataUseCase: FetchTorrentMetadataUseCase,
    private val fetchTorrentFileMetadataUseCase: FetchTorrentFileMetadataUseCase,
    private val startTorrentDownloadUseCase: StartTorrentDownloadUseCase,
    private val getTorrentDownloadTasksUseCase: GetTorrentDownloadTasksUseCase,
    private val pauseTorrentDownloadUseCase: PauseTorrentDownloadUseCase,
    private val resumeTorrentDownloadUseCase: ResumeTorrentDownloadUseCase,
    private val deleteTorrentDownloadUseCase: DeleteTorrentDownloadUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _playingMediaId = MutableStateFlow<Long?>(null)
    val playingMediaId: StateFlow<Long?> = _playingMediaId

    // Completed local videos mapped to UI state
    val completedVideos: StateFlow<List<VideoItemUiState>> = combine(
        getVideoLibraryUseCase(),
        _playingMediaId
    ) { list, playingId ->
        list.map { VideoItemUiState(media = it, isPlaying = it.id == playingId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Total downloaded videos count
    val totalVideosCount: StateFlow<Int> = completedVideos
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Total occupied space string
    val occupiedSpaceStr: StateFlow<String> = completedVideos
        .map { list ->
            list.sumOf { it.media.sizeBytes }.toSizeString()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "0.00 B"
        )

    val downloadingTasks: StateFlow<List<DownloadTask>> = getTorrentDownloadTasksUseCase()
        .map { tasks ->
            tasks.map { task ->
                DownloadTask(
                    id = task.id,
                    title = task.title,
                    progress = task.progress,
                    downloadSpeed = task.downloadSpeedBytes.toSpeedString(),
                    peersCount = task.peersCount,
                    totalSize = task.totalBytes,
                    downloadedSize = task.downloadedBytes,
                    status = task.status,
                    isPaused = task.status == TorrentDownloadStatus.PAUSED
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isFetchingMetadata = MutableStateFlow(false)
    val isFetchingMetadata: StateFlow<Boolean> = _isFetchingMetadata

    suspend fun fetchTorrentMetadata(link: String): Result<TorrentMetadata> {
        val cleanLink = link.trim()
        if (!cleanLink.startsWith("magnet:?xt=urn:btih:", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("链接格式错误，请检查"))
        }
        _isFetchingMetadata.value = true
        return runCatching {
            fetchTorrentMetadataUseCase(cleanLink)
        }.also {
            _isFetchingMetadata.value = false
        }
    }

    suspend fun fetchTorrentFileMetadata(uriString: String): Result<TorrentMetadata> {
        _isFetchingMetadata.value = true
        return runCatching {
            fetchTorrentFileMetadataUseCase(uriString)
        }.also {
            _isFetchingMetadata.value = false
        }
    }

    fun startTorrentDownload(metadata: TorrentMetadata, selectedFileIndexes: List<Int>) {
        viewModelScope.launch {
            startTorrentDownloadUseCase(metadata, selectedFileIndexes)
        }
    }

    fun togglePauseResume(taskId: Long) {
        viewModelScope.launch {
            val task = downloadingTasks.value.firstOrNull { it.id == taskId } ?: return@launch
            if (task.isPaused) {
                resumeTorrentDownloadUseCase(taskId)
            } else {
                pauseTorrentDownloadUseCase(taskId)
            }
        }
    }

    fun deleteDownloadTask(taskId: Long) {
        viewModelScope.launch {
            deleteTorrentDownloadUseCase(taskId)
        }
    }

    fun playVideo(item: VideoItemUiState) {
        viewModelScope.launch {
            val isCurrentlyPlaying = item.isPlaying
            if (isCurrentlyPlaying) {
                if (playbackController.isPlaying.value) {
                    playbackController.pause()
                } else {
                    playbackController.play()
                }
                return@launch
            }

            _playingMediaId.value = item.media.id
            playbackController.playMediaList(listOf(item.media.toMediaItem()), 0)
            playbackController.play()
        }
    }

    fun pausePlayback() {
        playbackController.pause()
    }

    private fun Long.toSpeedString(): String {
        return if (this <= 0L) "0 B/s" else "${toSizeString()}/s"
    }
}
