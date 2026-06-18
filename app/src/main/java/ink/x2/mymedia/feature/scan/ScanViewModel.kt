package ink.x2.mymedia.feature.scan

import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.R
import ink.x2.mymedia.core.common.AppError
import ink.x2.mymedia.core.common.onError
import ink.x2.mymedia.core.common.onSuccess
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.usecase.ScanMediaUseCase
import ink.x2.mymedia.feature.playing.PlayingActivity
import ink.x2.mymedia.playback.controller.PlaybackController
import ink.x2.mymedia.playback.mapper.toMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MediaItemUiState(
    val media: LocalMediaItem,
    val isSelected: Boolean = false
)
sealed interface ScanUiEvent{
    data class OpenPlaying(
        val media: MediaItemUiState
    ) : ScanUiEvent

    data class ShowMessage(
        val stringId: Int
    ) : ScanUiEvent

    data object RequestMediaPermission : ScanUiEvent
}
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanMediaUseCase: ScanMediaUseCase,
    private val playbackController: PlaybackController,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _mediaList = MutableStateFlow<List<MediaItemUiState>>(emptyList())
    val mediaList: StateFlow<List<MediaItemUiState>> = _mediaList.asStateFlow()
    private val _uiEvent = MutableSharedFlow<ScanUiEvent>(
        extraBufferCapacity = 1
    )
    val uiEvent = _uiEvent.asSharedFlow()

    private val mediaType: MediaType =
        savedStateHandle.get<MediaType>("media_type") ?: MediaType.AUDIO
    fun getMediaType(): MediaType{
        return mediaType
    }
    fun queryScanMediaResult() {
        viewModelScope.launch(Dispatchers.IO) {
           scanMediaUseCase.queryScanMediaResult(mediaType).onSuccess { scanMediaList->
                _mediaList.value = scanMediaList.map {
                    MediaItemUiState(
                        isSelected = false,
                        media = it
                    )
                }
            }.onError { error ->
                when(error){
                    AppError.SecurityException->{
                        _uiEvent.emit(ScanUiEvent.RequestMediaPermission)
                    }

                    is AppError.Unknown ->{
                        _uiEvent.emit(ScanUiEvent.ShowMessage(R.string.unknow))
                    }
                }
           }
        }
    }

    fun updateMediaList(mediaItem: MediaItemUiState) {
        _mediaList.update { currentList ->
            currentList.map { item ->
                if (item.media.id == mediaItem.media.id) {
                    mediaItem
                } else {
                    item
                }
            }
        }
    }
    fun updateListSelect(mediaItem: MediaItemUiState,isChecked: Boolean){
        val newMediaItem=mediaItem.copy(isSelected = isChecked)
        updateMediaList(newMediaItem)
    }
    fun updateListSelectAll(isAllSelectL: Boolean){
        _mediaList.value.forEach {
            updateListSelect(it,isAllSelectL)
        }
    }
    fun openPlayingActivity(media: MediaItemUiState){
        viewModelScope.launch {
            _uiEvent.emit(ScanUiEvent.OpenPlaying(media))
            playbackController.playMediaList(listOf(media.media.toMediaItem()),0)
            playbackController.play()
        }
    }
}

