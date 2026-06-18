package ink.x2.mymedia.feature.playing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityPlayingBinding
import ink.x2.mymedia.playback.controller.PlaybackController
import ink.x2.mymedia.playback.controller.PlaybackUiBinder
import javax.inject.Inject
import kotlin.getValue
@AndroidEntryPoint
class AudioPlayingActivity  : BaseActivity<ActivityPlayingBinding>(){
    private val binding: ActivityPlayingBinding by lazy {
        ActivityPlayingBinding.inflate(layoutInflater)
    }
    private val viewModel: AudioPlayingViewModel by viewModels()
    override fun getInsetAppBar(): View = binding.appBar
    @Inject
    lateinit var playbackController: PlaybackController
    companion object {
        fun startFrom(activity: Context) {
            val intent = Intent(activity, AudioPlayingActivity::class.java)
            activity.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initListeners()
        PlaybackUiBinder(this, playbackController)
            .bind(
                titleView = binding.tvTitle,
                artistView = binding.tvArtist,
                playPauseBtn = binding.btnPlay,
                seekBar = binding.seekBar,
                currentTimeTv = binding.tvCurrentTime,
                totalTimeTv = binding.tvTotalTime
            )
    }
    fun initListeners(){
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
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}