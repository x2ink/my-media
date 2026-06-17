package ink.x2.mymedia.feature.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.usecase.ScanMediaUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaItemUiState(
    val media: LocalMediaItem,
    val isSelected: Boolean = false
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanMediaUseCase: ScanMediaUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _mediaList = MutableStateFlow<List<MediaItemUiState>>(emptyList())
    val mediaList: StateFlow<List<MediaItemUiState>> = _mediaList.asStateFlow()

    private val mediaType: MediaType =
        savedStateHandle.get<MediaType>("media_type") ?: MediaType.AUDIO

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
}

