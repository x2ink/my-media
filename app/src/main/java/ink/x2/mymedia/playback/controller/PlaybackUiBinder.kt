package ink.x2.mymedia.playback.controller

import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import ink.x2.mymedia.R
import ink.x2.mymedia.core.ext.toDurationString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PlaybackUiBinder @Inject constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val playbackController: PlaybackController
) {
    private var isUserTracking = false

    fun bind(
        cardView: View? = null,
        titleView: TextView? = null,
        artistView: TextView? = null,
        playPauseBtn: MaterialButton? = null,
        seekBar: SeekBar? = null,
        currentTimeTv: TextView? = null,
        totalTimeTv: TextView? = null,
        coverView: ImageView?= null,
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    playbackController.currentMediaItem.collect { mediaItem ->
                        titleView?.text = mediaItem?.mediaMetadata?.title ?: "暂无播放"
                        artistView?.text = mediaItem?.mediaMetadata?.artist ?: ""
                        coverView?.let {
                            Glide.with(coverView.context)
                                .load(mediaItem?.mediaMetadata?.extras?.getString("uri")?.toUri())
                                .into(coverView)
                        }
                    }
                }
                launch {
                    playbackController.isPlaying.collect { isPlaying ->
                        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        playPauseBtn?.setIconResource(iconRes)
                    }
                }
            }
        }
        playPauseBtn?.setOnClickListener {
            if (playbackController.isPlaying.value) {
                playbackController.pause()
            } else {
                playbackController.play()
            }
        }
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentTimeTv?.text = progress.toLong().toDurationString()
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                isUserTracking = true
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let {
                    playbackController.seekTo(it.progress.toLong())
                }
                isUserTracking = false
            }
        })

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    if (!isUserTracking) {
                        val duration = playbackController.getDuration()
                        val position = playbackController.getCurrentPosition()

                        seekBar?.max = duration.toInt()
                        seekBar?.progress = position.toInt()

                        currentTimeTv?.text = position.toDurationString()
                        totalTimeTv?.text = duration.toDurationString()
                    }
                    delay(500.milliseconds)
                }
            }
        }
    }
}