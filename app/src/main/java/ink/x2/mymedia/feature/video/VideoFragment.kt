package ink.x2.mymedia.feature.video

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.core.ext.toastText
import ink.x2.mymedia.core.ui.VerticalGapDecoration
import ink.x2.mymedia.databinding.ControllerVideoBinding
import ink.x2.mymedia.databinding.FragmentVideoBinding
import ink.x2.mymedia.domain.usecase.DeleteMediaItemUseCase
import ink.x2.mymedia.domain.usecase.UpdateMediaInfoUseCase
import ink.x2.mymedia.feature.playing.VideoPlayingActivity
import ink.x2.mymedia.playback.controller.PlaybackController
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VideoFragment : BaseFragment<FragmentVideoBinding>(R.layout.fragment_video) {
    companion object {
        const val TITLE: String = "视频"
    }

    @Inject
    lateinit var updateMediaInfoUseCase: UpdateMediaInfoUseCase

    @Inject
    lateinit var deleteMediaItemUseCase: DeleteMediaItemUseCase

    @Inject
    lateinit var playbackController: PlaybackController

    private val viewModel: VideoViewModel by viewModels()
    private lateinit var videoAdapter: VideoLibraryAdapter
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
                        listPlayerView?.player = null
                        VideoPlayingActivity.startFrom(requireActivity())
                    } else {
                        // 退出全屏
                    }
                }
                player = playbackController.getPlayer()
            }
            listPlayerView = pv
        }
        return pv
    }

    override fun bindView(view: View): FragmentVideoBinding {
        return FragmentVideoBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSearchListener()
        observeViewModel()
    }

    private fun initRecyclerView() {
        videoAdapter = VideoLibraryAdapter(
            onItemClick = { item ->
                viewModel.playVideo(item)
            },
            onVideoPlayBind = { itemBinding, itemState ->
                val playerView = getOrCreatePlayerView()
                val parent = playerView.parent as? ViewGroup
                if (parent != itemBinding.flVideoContainer) {
                    parent?.removeView(playerView)
                    itemBinding.flVideoContainer.removeAllViews()
                    itemBinding.flVideoContainer.addView(
                        playerView,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            onVideoPlayRecycle = { itemBinding ->
                val playerView = listPlayerView ?: return@VideoLibraryAdapter
                if (itemBinding.flVideoContainer.indexOfChild(playerView) != -1) {
                    itemBinding.flVideoContainer.removeView(playerView)
                }
            },
            onItemLongClick = { mediaItem, view ->
                PopupMenu(requireContext(), view).apply {
                    menuInflater.inflate(R.menu.menu_video_item_actions, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.actions_edit -> {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    when(updateMediaInfoUseCase(mediaItem.media)){
                                        is AppResult.Success ->{
                                            requireContext().toastText(R.string.edit_success)
                                        }
                                        is AppResult.Error->{
                                            requireContext().toastText(R.string.edit_error)
                                        }
                                    }
                                }
                                true
                            }

                            R.id.actions_delete -> {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    when(val result =deleteMediaItemUseCase(mediaItem.media)){
                                        is AppResult.Success ->{
                                            requireContext().toastText(R.string.del_success)
                                        }
                                        is AppResult.Error->{
                                            Logger.d(result.error)
                                            requireContext().toastText(R.string.del_error)
                                        }
                                    }
                                }
                                true
                            }

                            else -> false
                        }
                    }

                    show()
                }
            }
        )
        binding.rvVideoList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videoAdapter
            addItemDecoration(
                VerticalGapDecoration(
                    resources.getDimensionPixelSize(
                        R.dimen.dimens_16
                    )
                )
            )
        }
    }

    private fun initSearchListener() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videoList.collect { list ->
                    videoAdapter.submitList(list)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayback()
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        listPlayerView?.apply {
            player = playbackController.getPlayer()
            setFullscreenButtonState(false)
        }
    }

    override fun onDestroyView() {
        listPlayerView?.player = null
        listPlayerView = null
        super.onDestroyView()
    }
}
