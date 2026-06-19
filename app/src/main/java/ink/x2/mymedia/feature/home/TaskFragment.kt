package ink.x2.mymedia.feature.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.core.ui.VerticalGapDecoration
import ink.x2.mymedia.databinding.FragmentTaskBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : BaseFragment<FragmentTaskBinding>(R.layout.fragment_task) {

    companion object {
        const val TITLE: String = "任务"
    }

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var downloadingAdapter: DownloadingTasksAdapter

    override fun bindView(view: View): FragmentTaskBinding {
        return FragmentTaskBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerViews()
        observeViewModel()
    }

    private fun initRecyclerViews() {
        downloadingAdapter = DownloadingTasksAdapter(
            onPauseResumeClick = { task ->
                viewModel.togglePauseResume(task.id)
            },
            onDeleteClick = { task ->
                viewModel.deleteDownloadTask(task.id)
                Toast.makeText(requireContext(), "已删除任务", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvDownloadingTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadingAdapter
            addItemDecoration(
                VerticalGapDecoration(
                    resources.getDimensionPixelSize(R.dimen.dimens_8)
                )
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Downloading tasks list
                launch {
                    viewModel.downloadingTasks.collect { list ->
                        downloadingAdapter.submitList(list)
                        binding.tvEmptyTasks.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvDownloadingTasks.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }
}
