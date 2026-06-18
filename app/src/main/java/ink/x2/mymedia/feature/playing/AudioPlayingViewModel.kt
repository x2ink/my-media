package ink.x2.mymedia.feature.playing

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ink.x2.mymedia.playback.controller.PlaybackController
import javax.inject.Inject

@HiltViewModel
class AudioPlayingViewModel @Inject constructor(
    private val playbackController: PlaybackController
): ViewModel(

) {

}