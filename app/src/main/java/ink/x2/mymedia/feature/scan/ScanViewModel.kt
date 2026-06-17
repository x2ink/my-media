package ink.x2.mymedia.feature.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanMediaUseCase: ScanMediaUseCase,
    private val playbackController: PlaybackController,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _mediaList = MutableStateFlow<List<MediaItemUiState>>(emptyList())
    val mediaList: StateFlow<List<MediaItemUiState>> = _mediaList.asStateFlow()
    private val _openPlayingEvent = MutableSharedFlow<MediaItemUiState>()
    val openPlayingEvent = _openPlayingEvent.asSharedFlow()

    private val mediaType: MediaType =
        savedStateHandle.get<MediaType>("media_type") ?: MediaType.AUDIO
    fun getMediaType(): MediaType{
        return mediaType
    }
    fun queryScanMediaResult() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = scanMediaUseCase.queryScanMediaResult(mediaType)
            _mediaList.value = result.map {
                MediaItemUiState(
                    isSelected = false,
                    media = it
                )
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
            _openPlayingEvent.emit(media)
            playbackController.playMediaList(listOf(media.media.toMediaItem()),0)
            playbackController.play()
        }
    }
}

