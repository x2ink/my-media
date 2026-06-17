package ink.x2.mymedia.feature.playing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.core.ext.toDurationString
import ink.x2.mymedia.databinding.ActivityPlayingBinding
import ink.x2.mymedia.playback.controller.PlaybackController
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue
@AndroidEntryPoint
class PlayingActivity  : BaseActivity<ActivityPlayingBinding>(){
    private val binding: ActivityPlayingBinding by lazy {
        ActivityPlayingBinding.inflate(layoutInflater)
    }
    private val viewModel: PlayingViewModel by viewModels()
    override fun getInsetAppBar(): View = binding.appBar
    @Inject
    lateinit var playbackController: PlaybackController
    companion object {
        fun startFrom(activity: Context) {
            val intent = Intent(activity, PlayingActivity::class.java)
            activity.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observeViewModel()
        initListeners()
    }
    fun initListeners(){
        binding.btnPlay.setOnClickListener {
            if (playbackController.isPlaying.value) {
                playbackController.pause()
            } else {
                playbackController.play()
            }
        }
        binding.btnNext.apply {
            isEnabled =playbackController.hasNext()
            alpha = if (playbackController.hasNext()) 1.0f else 0.3f
            setOnClickListener {
                playbackController.skipToNext()
            }
        }
        binding.btnPrev.apply {
            isEnabled =playbackController.hasPrevious()
            alpha = if (playbackController.hasPrevious()) 1.0f else 0.3f
            setOnClickListener {
                playbackController.skipToPrevious()
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {
                if (p2) {
                    binding.tvCurrentTime.text = p1.toLong().toDurationString()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                viewModel.setDragProgressFlag(true)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.let { seekBar ->
                    playbackController.seekTo(seekBar.progress.toLong())
                }
                viewModel.setDragProgressFlag(false)
            }

        })
    }
    fun observeViewModel(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    playbackController.currentMediaItem.collect { mediaItem ->
                        Logger.d(mediaItem?.mediaMetadata.toString())
                        binding.tvTitle.text= mediaItem?.mediaMetadata?.title?:"未知音频"
                        binding.tvArtist.text= mediaItem?.mediaMetadata?.artist?:"未知作者"
                        Logger.i(mediaItem?.mediaMetadata?.durationMs.toString())
                        binding.tvTotalTime.text= mediaItem?.mediaMetadata?.durationMs?.toDurationString()
                    }
                }
                launch {
                    playbackController.isPlaying.collect { isPlaying ->
                        binding.btnPlay.setIconResource(
                            if (isPlaying) {
                                R.drawable.ic_pause
                            } else {
                                R.drawable.ic_play
                            }
                        )
                    }
                }
                launch {
                    viewModel.currentPosition.collect { position->
                        binding.seekBar.progress=position.toInt()
                        binding.tvCurrentTime.text=position.toDurationString()
                    }
                }
                launch {
                    viewModel.duration.collect { duration->
                        binding.seekBar.max=duration.toInt()
                    }
                }
            }
        }
    }
}