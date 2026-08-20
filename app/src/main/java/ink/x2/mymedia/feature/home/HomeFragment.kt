package ink.x2.mymedia.feature.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaMetadata
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.databinding.FragmentHomeBinding
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.usecase.ScanMediaUseCase
import ink.x2.mymedia.feature.playing.AudioPlayingActivity
import ink.x2.mymedia.feature.playing.VideoPlayingActivity
import ink.x2.mymedia.feature.scan.ScanActivity
import ink.x2.mymedia.playback.controller.PlaybackController
import ink.x2.mymedia.playback.controller.PlaybackUiBinder
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    companion object{
        const val TITLE : String = "首页"
    }
    @Inject
    lateinit var playbackController: PlaybackController
    @Inject
    lateinit var scanMediaUseCase: ScanMediaUseCase
    private val viewModel: HomeViewModel by viewModels()
    override fun bindView(view: View): FragmentHomeBinding {
        return FragmentHomeBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        observeViewModel()
    }
    fun observeViewModel(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    playbackController.currentMediaItem.collect { mediaItem->
                        mediaItem?.let{
                            if(mediaItem.mediaMetadata.mediaType == MediaMetadata.MEDIA_TYPE_VIDEO){
                                binding.includeAudioCard.root.isVisible=false
                                binding.includeVideoCard.root.isVisible=true
                                PlaybackUiBinder(viewLifecycleOwner, playbackController)
                                    .bind(
                                        cardView = binding.includeVideoCard.cardVideoPlay,
                                        titleView = binding.includeVideoCard.tvVideoTitle,
                                        coverView = binding.includeVideoCard.ivVideoCover
                                    )
                                binding.includeVideoCard.cardVideoPlay.setOnClickListener {
                                    VideoPlayingActivity.startFrom(requireActivity())
                                }
                            }else if(mediaItem.mediaMetadata.mediaType == MediaMetadata.MEDIA_TYPE_MUSIC){
                                binding.includeVideoCard.root.isVisible=false
                                binding.includeAudioCard.root.isVisible=true
                                PlaybackUiBinder(viewLifecycleOwner, playbackController)
                                    .bind(
                                        cardView = binding.includeAudioCard.cardAudioPlay,
                                        titleView = binding.includeAudioCard.tvAudioTitle,
                                        artistView = binding.includeAudioCard.tvAudioArtist,
                                        playPauseBtn = binding.includeAudioCard.btnAudioPlayPause,
                                        seekBar = binding.includeAudioCard.seekBarAudio,
                                        currentTimeTv = binding.includeAudioCard.tvAudioTimeCurrent,
                                        totalTimeTv = binding.includeAudioCard.tvAudioTimeTotal
                                    )
                            }else{
                                binding.includeVideoCard.root.isVisible=false
                                binding.includeAudioCard.root.isVisible=false
                            }
                        }
                    }
                }
            }
        }
    }
    fun initListener(){
        binding.scanAudio.setOnClickListener {
            ScanActivity.startFrom(requireContext(), MediaType.AUDIO)
        }
        binding.scanVideo.setOnClickListener {
            ScanActivity.startFrom(requireContext(), MediaType.VIDEO)
        }
        binding.includeAudioCard.root.setOnClickListener {
            AudioPlayingActivity.startFrom(requireContext())
        }
    }
}