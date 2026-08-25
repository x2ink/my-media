package ink.x2.mymedia.feature.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ink.x2.mymedia.core.ext.toDurationString
import ink.x2.mymedia.core.ext.toSizeString
import ink.x2.mymedia.databinding.ItemMediaGridBinding
import java.io.File

class VideoLibraryAdapter(
    private val onItemClick: (VideoItemUiState) -> Unit,
    private val onItemLongClick:(VideoItemUiState, View) -> Unit,
    private val onVideoPlayBind: ((ItemMediaGridBinding, VideoItemUiState) -> Unit)? = null,
    private val onVideoPlayRecycle: ((ItemMediaGridBinding) -> Unit)? = null
) : ListAdapter<VideoItemUiState, VideoLibraryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemMediaGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            cbSelect.visibility = View.GONE
            btnEdit.visibility = View.GONE
            
            Glide.with(ivMediaIcon.context)
                .load(File(item.media.localRelativePath))
                .into(ivMediaIcon)
                
            tvMediaTitle.text = item.media.title
            tvMediaSubtitle.text = "${item.media.durationMs.toDurationString()} / ${item.media.sizeBytes.toSizeString()}"
            
            if (item.isPlaying) {
                ivPlayIcon.visibility = View.GONE
                onVideoPlayBind?.invoke(this, item)
            } else {
                ivPlayIcon.visibility = View.VISIBLE
                flVideoContainer.removeAllViews()
            }
            
            root.setOnClickListener {
                onItemClick(item)
            }
            root.setOnLongClickListener {view->
                onItemLongClick(item,view)
                true
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        onVideoPlayRecycle?.invoke(holder.binding)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<VideoItemUiState>() {
            override fun areItemsTheSame(oldItem: VideoItemUiState, newItem: VideoItemUiState): Boolean {
                return oldItem.media.id == newItem.media.id
            }

            override fun areContentsTheSame(oldItem: VideoItemUiState, newItem: VideoItemUiState): Boolean {
                return oldItem == newItem
            }
        }
    }
}
