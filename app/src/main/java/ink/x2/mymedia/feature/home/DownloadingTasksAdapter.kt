package ink.x2.mymedia.feature.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ink.x2.mymedia.R
import ink.x2.mymedia.core.ext.toSizeString
import ink.x2.mymedia.databinding.ItemDownloadTaskBinding

class DownloadingTasksAdapter(
    private val onPauseResumeClick: (DownloadTask) -> Unit,
    private val onDeleteClick: (DownloadTask) -> Unit
) : ListAdapter<DownloadTask, DownloadingTasksAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemDownloadTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        holder.binding.apply {
            tvTaskTitle.text = task.title
            pbDownload.progress = task.progress

            val speedText = if (task.isPaused) "已暂停" else task.downloadSpeed
            val peersText = if (task.isPaused || task.progress == 100) "" else " • ${task.peersCount} Peers"
            tvTaskProgress.text = "${task.progress}% • $speedText$peersText"

            val progressBytes = task.downloadedSize
            tvTaskSize.text = "${progressBytes.toSizeString()} / ${task.totalSize.toSizeString()}"

            // Pause/Resume Icon
            val iconRes = if (task.isPaused) R.drawable.ic_play else R.drawable.ic_pause
            btnPauseResume.setIconResource(iconRes)

            btnPauseResume.setOnClickListener {
                onPauseResumeClick(task)
            }
            btnDelete.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DownloadTask>() {
            override fun areItemsTheSame(oldItem: DownloadTask, newItem: DownloadTask): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DownloadTask, newItem: DownloadTask): Boolean {
                return oldItem == newItem
            }
        }
    }
}
