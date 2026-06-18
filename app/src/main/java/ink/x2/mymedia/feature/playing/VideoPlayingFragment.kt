package ink.x2.mymedia.feature.playing


import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.databinding.FragmentVideoPlayingBinding
import ink.x2.mymedia.playback.controller.PlaybackController
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayingFragment : BaseFragment<FragmentVideoPlayingBinding>(R.layout.fragment_video_playing){
    override fun bindView(view: View): FragmentVideoPlayingBinding {
        return FragmentVideoPlayingBinding.bind(view)
    }
    @Inject
    lateinit var playbackController: PlaybackController
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playerView.player = playbackController.getPlayer()
    }
    override fun onDestroyView() {
        binding.playerView.player = null
        super.onDestroyView()
    }
}