package ink.x2.mymedia.feature.scan

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
import ink.x2.mymedia.domain.model.ImportProgress
import ink.x2.mymedia.domain.usecase.ScanMediaUseCase
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
import javax.inject.Inject


sealed interface ScanStatus{
    data object Idel: ScanStatus
    data object Start: ScanStatus
    data object End: ScanStatus
}
data class MediaItemUiState(
    val media: LocalMediaItem,
    val isSelected: Boolean = false,
    val isPlaying: Boolean = false
)
sealed interface ScanUiEvent{
    data class OpenAudioPlaying(
        val media: MediaItemUiState
    ) : ScanUiEvent
    data class OpenVideoPlaying(
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
    private val _scanStatue = MutableStateFlow<ScanStatus>(ScanStatus.Idel)
    val scanStatue: StateFlow<ScanStatus> = _scanStatue.asStateFlow()
    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ScanUiEvent>(
        extraBufferCapacity = 1
    )
    val uiEvent = _uiEvent.asSharedFlow()
    
    fun updateScanStatus(status: ScanStatus){
        _scanStatue.value=status
    }
    
    fun resetImportProgress() {
        _importProgress.value = null
    }

    private val mediaType: MediaType =
        savedStateHandle.get<MediaType>("media_type") ?: MediaType.AUDIO
    fun getMediaType(): MediaType{
        return mediaType
    }
    
    fun queryScanMediaResult() {
        updateScanStatus(ScanStatus.Start)
        viewModelScope.launch(Dispatchers.IO) {
           scanMediaUseCase.queryScanMediaResult(mediaType).onSuccess { scanMediaList->
                _mediaList.value = scanMediaList.map {
                    MediaItemUiState(
                        isSelected = false,
                        media = it
                    )
                }
                updateScanStatus(ScanStatus.End)
            }.onError { error ->
                updateScanStatus(ScanStatus.End)
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

    fun importSelectedMedia() {
        val selectedItems = _mediaList.value
            .filter { it.isSelected }
            .map { it.media }

        if (selectedItems.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(ScanUiEvent.ShowMessage(R.string.please_input_new_name))
            }
            return
        }

        viewModelScope.launch {
            scanMediaUseCase.importSelectdMedia(selectedItems)
                .collect { result ->
                    result.onSuccess { progress ->
                        _importProgress.value = progress
                    }.onError {
                        _importProgress.value = ImportProgress.Failure
                    }
                }
        }
    }
    fun clearList(){
        _mediaList.value=mutableListOf()
        playbackController.pause()
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
    fun openAudioPlayingActivity(media: MediaItemUiState){
        viewModelScope.launch {
            _uiEvent.emit(ScanUiEvent.OpenAudioPlaying(media))
            playbackController.playMediaList(listOf(media.media.toMediaItem()),0)
            playbackController.play()
        }
    }
    fun openVideoPlayingFragment(media: MediaItemUiState){
        viewModelScope.launch {
            val isCurrentlyPlaying = _mediaList.value.find { it.media.id == media.media.id }?.isPlaying == true
            if (isCurrentlyPlaying) {
                if (playbackController.isPlaying.value) {
                    playbackController.pause()
                } else {
                    playbackController.play()
                }
                return@launch
            }

            _mediaList.update { currentList ->
                currentList.map { item ->
                    item.copy(isPlaying = item.media.id == media.media.id)
                }
            }
            _uiEvent.emit(ScanUiEvent.OpenVideoPlaying(media))
            playbackController.playMediaList(listOf(media.media.toMediaItem()),0)
            playbackController.play()
        }
    }
}

