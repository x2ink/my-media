package ink.x2.mymedia.feature.scan

import android.annotation.SuppressLint
import android.content.ContentUris
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ink.x2.mymedia.databinding.ItemMediaLinearBinding
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import androidx.core.net.toUri
import com.orhanobut.logger.Logger
import ink.x2.mymedia.R
import ink.x2.mymedia.core.ext.toDurationString
import ink.x2.mymedia.core.ext.toSizeString
import ink.x2.mymedia.databinding.ItemMediaGridBinding

class VerticalGapDecoration(
    private val gap: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        if (position != 0) {
            outRect.top = gap
        }
    }
}

class ScanResultListAdapter(
    private val onClickItem:(MediaItemUiState)-> Unit,
    private val onSelectClick:(MediaItemUiState, Boolean)-> Unit,
    private val onEditClick: (MediaItemUiState) -> Unit,
    private val onVideoPlayBind: ((ItemMediaGridBinding, MediaItemUiState) -> Unit)? = null,
    private val onVideoPlayRecycle: ((ItemMediaGridBinding) -> Unit)? = null
) : ListAdapter<MediaItemUiState, ScanResultListAdapter.ScanViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            VIEW_TYPE_AUDIO -> {
                val binding = ItemMediaLinearBinding.inflate(inflater, parent, false)
                ScanViewHolder.Audio(binding)
            }
            VIEW_TYPE_VIDEO -> {
                val binding = ItemMediaGridBinding.inflate(inflater, parent, false)
                ScanViewHolder.Video(binding)
            }

            else -> throw IllegalArgumentException("未知的 ViewType: $viewType")
        }
    }
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).media.mediaType) {
            MediaType.AUDIO -> VIEW_TYPE_AUDIO
            MediaType.VIDEO -> VIEW_TYPE_VIDEO
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ScanViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        when(holder){
            is ScanViewHolder.Audio->{
                holder.binding.apply {
                    cbSelect.isChecked = item.isSelected
                    tvMediaTitle.text = item.media.title
                    tvMediaSubtitle.text = "${item.media.artist} / ${item.media.duration.toDurationString()} / ${item.media.size.toSizeString()}"
                    btnEdit.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onEditClick(getItem(adapterPosition))
                    }
                    cbSelect.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onSelectClick(getItem(adapterPosition), cbSelect.isChecked)
                    }
                    ivMediaIcon.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onClickItem(getItem(adapterPosition))
                    }
                    llMediaContent.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onClickItem(getItem(adapterPosition))
                    }
                }
            }
            is ScanViewHolder.Video->{
                holder.binding.apply {
                    cbSelect.isChecked = item.isSelected
                    tvMediaTitle.text = item.media.title
                    tvMediaSubtitle.text = "${item.media.duration.toDurationString()} / ${item.media.size.toSizeString()}"
                    Glide.with(ivMediaIcon.context).load(item.media.uriString.toUri()).into(ivMediaIcon)
                    
                    if (item.isPlaying) {
                        ivPlayIcon.visibility = View.GONE
                        onVideoPlayBind?.invoke(this, item)
                    } else {
                        ivPlayIcon.visibility = View.VISIBLE
                        flVideoContainer.removeAllViews()
                    }

                    btnEdit.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onEditClick(getItem(adapterPosition))
                    }
                    cbSelect.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onSelectClick(getItem(adapterPosition), cbSelect.isChecked)
                    }
                    ivMediaIcon.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onClickItem(getItem(adapterPosition))
                    }
                    llMediaContent.setOnClickListener {
                        val adapterPosition = holder.bindingAdapterPosition
                        if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                        onClickItem(getItem(adapterPosition))
                    }
                }
            }
        }



    }

    override fun onViewRecycled(holder: ScanViewHolder) {
        super.onViewRecycled(holder)
        if (holder is ScanViewHolder.Video) {
            onVideoPlayRecycle?.invoke(holder.binding)
        }
    }

    sealed  class ScanViewHolder(
        val root:View
    ) : RecyclerView.ViewHolder(root){
        class Audio(val binding: ItemMediaLinearBinding) : ScanViewHolder(binding.root)
        class Video(val binding: ItemMediaGridBinding) : ScanViewHolder(binding.root)
    }

    companion object {
        private const val VIEW_TYPE_AUDIO = 1
        private const val VIEW_TYPE_VIDEO = 2
        private val DiffCallback = object : DiffUtil.ItemCallback<MediaItemUiState>() {
            override fun areItemsTheSame(oldItem: MediaItemUiState, newItem: MediaItemUiState): Boolean {
                return oldItem.media.id == newItem.media.id
            }

            override fun areContentsTheSame(oldItem: MediaItemUiState, newItem: MediaItemUiState): Boolean {
                return oldItem == newItem
            }
        }
    }
}