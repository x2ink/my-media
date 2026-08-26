package ink.x2.mymedia.feature.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.R
import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.core.ext.toastText
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.mapper.toLocalMedia
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.repository.MediaRepository
import ink.x2.mymedia.domain.usecase.DeleteMediaItemUseCase
import ink.x2.mymedia.domain.usecase.GetVideoLibraryUseCase
import ink.x2.mymedia.feature.scan.MediaItemUiState
import ink.x2.mymedia.feature.scan.ScanUiEvent
import ink.x2.mymedia.playback.controller.PlaybackController
import ink.x2.mymedia.playback.mapper.toMediaItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoItemUiState(
    val media: LocalMedia,
    val isPlaying: Boolean = false
)
sealed interface VideoFragmentUiEvent{
    data class ShowMessage(
        val stringId: Int
    ) : VideoFragmentUiEvent
}
@HiltViewModel
class VideoViewModel @Inject constructor(
    getVideoLibraryUseCase: GetVideoLibraryUseCase,
    private val playbackController: PlaybackController,
    private val mediaRepository: MediaRepository,
    private val deleteMediaItemUseCase: DeleteMediaItemUseCase
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _playingMediaId = MutableStateFlow<Long?>(null)
    val playingMediaId: StateFlow<Long?> = _playingMediaId

    private val _uiEvent = MutableSharedFlow<VideoFragmentUiEvent>(
        extraBufferCapacity = 1
    )
    val uiEvent = _uiEvent.asSharedFlow()

    val videoList: StateFlow<List<VideoItemUiState>> = combine(
        getVideoLibraryUseCase(),
        _searchQuery,
        _playingMediaId
    ) { list, query, playingId ->
        val filtered = if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }
        filtered.map { VideoItemUiState(media = it, isPlaying = it.id == playingId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
    fun updateMediaItemTitleById(text:String,id: Long){
        viewModelScope.launch {
            when(mediaRepository.updateMediaItemTitleById(text,id)){
                is AppResult.Success ->{
                    _uiEvent.emit(VideoFragmentUiEvent.ShowMessage(R.string.edit_success))
                }
                is AppResult.Error->{
                    _uiEvent.emit(VideoFragmentUiEvent.ShowMessage(R.string.edit_error))
                }
            }
        }
    }

    fun deleteMediaItem(id: Long){
        viewModelScope.launch {
            when(deleteMediaItemUseCase(id)){
                is AppResult.Success ->{
                    _uiEvent.emit(VideoFragmentUiEvent.ShowMessage(R.string.del_success))
                }
                is AppResult.Error->{
                    _uiEvent.emit(VideoFragmentUiEvent.ShowMessage(R.string.del_error))
                }
            }
        }
    }
}
