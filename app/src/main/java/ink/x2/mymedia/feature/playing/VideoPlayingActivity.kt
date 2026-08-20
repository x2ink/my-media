package ink.x2.mymedia.feature.playing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.databinding.ActivityVideoPlayingBinding
import ink.x2.mymedia.databinding.ControllerVideoBinding
import ink.x2.mymedia.playback.controller.PlaybackController
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayingActivity : AppCompatActivity() {
    private val binding: ActivityVideoPlayingBinding by lazy {
        ActivityVideoPlayingBinding.inflate(layoutInflater)
    }
    @Inject
    lateinit var playbackController: PlaybackController
    private var listPlayerView: PlayerView? = null
    @OptIn(UnstableApi::class)
    private fun getOrCreatePlayerView(): PlayerView {
        var pv = listPlayerView
        if (pv == null) {
            val playerBinding =
                ControllerVideoBinding.inflate(layoutInflater)
            pv = playerBinding.playerView.apply {
                setFullscreenButtonClickListener { isFullscreen ->
                    if (isFullscreen) {
                        // 进入全屏
                    } else {
                       finish()
                    }
                }
                player = playbackController.getPlayer()
                setFullscreenButtonState(true)
            }
            listPlayerView = pv
        }
        return pv
    }
    companion object {
        fun startFrom(activity: Context) {
            val intent = Intent(activity, VideoPlayingActivity::class.java)
            activity.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val playerView = getOrCreatePlayerView()
        val parent = playerView.parent as? ViewGroup
        if (parent != binding.playerContainer) {
            parent?.removeView(playerView)
            binding.playerContainer.removeAllViews()
            binding.playerContainer.addView(
                playerView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        playbackController.play()
    }
    override fun onDestroy() {
        listPlayerView?.player = null
        listPlayerView = null
        super.onDestroy()
    }
}