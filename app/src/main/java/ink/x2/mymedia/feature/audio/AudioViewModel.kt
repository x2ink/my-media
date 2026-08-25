package ink.x2.mymedia.feature.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.mapper.toLocalMedia
import ink.x2.mymedia.domain.model.LocalMedia
import ink.x2.mymedia.domain.usecase.GetAudioLibraryUseCase
import ink.x2.mymedia.playback.controller.PlaybackController
import ink.x2.mymedia.playback.mapper.toMediaItemList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val getAudioLibraryUseCase: GetAudioLibraryUseCase,
    private val playbackController: PlaybackController
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val audioList: StateFlow<List<MediaEntity>> = combine(
        getAudioLibraryUseCase(),
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playAudio(media: LocalMedia) {
        val currentList = audioList.value
        val index = currentList.indexOfFirst { it.id == media.id }
        if (index != -1) {
            playbackController.playMediaList(currentList.map {
                it.toLocalMedia()
            }.toMediaItemList(), index)
            playbackController.play()
        }
    }
}
