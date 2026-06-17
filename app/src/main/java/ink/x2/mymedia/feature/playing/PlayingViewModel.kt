package ink.x2.mymedia.feature.playing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.playback.controller.PlaybackController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PlayingViewModel @Inject constructor(
    private val playbackController: PlaybackController
): ViewModel(

) {
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private var dragProgressFlag = false
    init {
        viewModelScope.launch {
            while (isActive){
                if(!dragProgressFlag){
                    _currentPosition.value=playbackController.getCurrentPosition()
                    _duration.value=playbackController.getDuration()
                }
                delay(500.milliseconds)
            }
        }
    }
    fun setDragProgressFlag(flag: Boolean){
        dragProgressFlag=flag
    }
}