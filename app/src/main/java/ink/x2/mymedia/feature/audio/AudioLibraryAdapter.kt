package ink.x2.mymedia.feature.audio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ink.x2.mymedia.core.ext.toDurationString
import ink.x2.mymedia.core.ext.toSizeString
import ink.x2.mymedia.databinding.ItemMediaLinearBinding
import ink.x2.mymedia.domain.model.LocalMedia

class AudioLibraryAdapter(
    private val onItemClick: (LocalMedia) -> Unit
) : ListAdapter<LocalMedia, AudioLibraryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemMediaLinearBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaLinearBinding.inflate(
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
            tvMediaTitle.text = item.title
            tvMediaSubtitle.text = "${item.artist ?: "未知艺术家"} / ${item.durationMs.toDurationString()} / ${item.sizeBytes.toSizeString()}"
            
            root.setOnClickListener {
                onItemClick(item)
            }
            llMediaContent.setOnClickListener {
                onItemClick(item)
            }
            ivMediaIcon.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LocalMedia>() {
            override fun areItemsTheSame(oldItem: LocalMedia, newItem: LocalMedia): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LocalMedia, newItem: LocalMedia): Boolean {
                return oldItem == newItem
            }
        }
    }
}
