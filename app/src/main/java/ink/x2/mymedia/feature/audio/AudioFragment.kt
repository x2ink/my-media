package ink.x2.mymedia.feature.audio

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.core.ui.VerticalGapDecoration
import ink.x2.mymedia.data.mapper.toLocalMedia
import ink.x2.mymedia.databinding.FragmentAudioBinding
import ink.x2.mymedia.feature.playing.AudioPlayingActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AudioFragment : BaseFragment<FragmentAudioBinding>(R.layout.fragment_audio) {
    companion object {
        const val TITLE: String = "媒体"
    }

    private val viewModel: AudioViewModel by viewModels()
    private lateinit var audioAdapter: AudioLibraryAdapter

    override fun bindView(view: View): FragmentAudioBinding {
        return FragmentAudioBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSearchListener()
        observeViewModel()
    }

    private fun initRecyclerView() {
        audioAdapter = AudioLibraryAdapter { media ->
            viewModel.playAudio(media)
            AudioPlayingActivity.startFrom(requireContext())
        }
        binding.rvAudioList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
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
                viewModel.audioList.collect { list ->
                    audioAdapter.submitList(list.map{
                        it.toLocalMedia()
                    })
                }
            }
        }
    }
}
