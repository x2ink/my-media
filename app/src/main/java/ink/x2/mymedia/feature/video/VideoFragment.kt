package ink.x2.mymedia.feature.video

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.core.ui.VerticalGapDecoration
import ink.x2.mymedia.databinding.FragmentVideoBinding
import ink.x2.mymedia.playback.controller.PlaybackController
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VideoFragment : BaseFragment<FragmentVideoBinding>(R.layout.fragment_video) {
    companion object {
        const val TITLE: String = "视频"
    }

    @Inject
    lateinit var playbackController: PlaybackController

    private val viewModel: VideoViewModel by viewModels()
    private lateinit var videoAdapter: VideoLibraryAdapter
    private var listPlayerView: PlayerView? = null

    private fun getOrCreatePlayerView(): PlayerView {
        var pv = listPlayerView
        if (pv == null) {
            pv = PlayerView(requireContext()).apply {
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
            listPlayerView = pv
        }
        pv.player = playbackController.getPlayer()
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

    override fun onDestroyView() {
        listPlayerView?.player = null
        listPlayerView = null
        super.onDestroyView()
    }
}
