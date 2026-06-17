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
    private val onSelectClick:(MediaItemUiState, Boolean)-> Unit,
    private val onEditClick: (MediaItemUiState) -> Unit
) : ListAdapter<MediaItemUiState, ScanResultListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemMediaLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("DefaultLocale")
    private fun Long.toSizeString(): String {
        if (this <= 0L) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            this >= gb -> String.format("%.2f GB", this / gb)
            this >= mb -> String.format("%.2f MB", this / mb)
            this >= kb -> String.format("%.2f KB", this / kb)
            else -> "$this B"
        }
    }

    private fun Long.toDurationString(): String {
        if (this <= 0L) return "00:00"

        val totalSeconds = this / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    fun getIcon(media: LocalMediaItem): Uri {
        return when (media.mediaType) {
            MediaType.AUDIO -> {
                ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    media.albumId
                )
            }

            MediaType.VIDEO -> {
                ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    media.albumId
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            Logger.d(item.media)
            Glide.with(ivMediaIcon.context)
                .load(item.media.albumBytes ?: getIcon(item.media))
                .error(R.drawable.ic_music)
                .into(ivMediaIcon)
            cbSelect.isChecked = item.isSelected
            tvMediaTitle.text = item.media.title
            tvMediaSubtitle.text = when (item.media.mediaType) {
                MediaType.AUDIO -> {
                    "${item.media.artist} / ${item.media.duration.toDurationString()} / ${item.media.size.toSizeString()}"
                }

                MediaType.VIDEO -> {
                    ""
                }
            }
            btnEdit.setOnClickListener {
                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onEditClick(getItem(adapterPosition))
            }
            cbSelect.setOnClickListener {
                onSelectClick(item, cbSelect.isChecked)
            }
        }
    }

    inner class ViewHolder(
        val binding: ItemMediaLinearBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
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