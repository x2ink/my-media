package ink.x2.mymedia.feature.scan

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ink.x2.mymedia.databinding.ItemMediaLinearBinding
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import androidx.core.net.toUri
import com.orhanobut.logger.Logger

class ScanResultListAdapter(private val dataList: MutableList<MediaItemUiState> = mutableListOf()):
    RecyclerView.Adapter<ScanResultListAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
       val binding = ItemMediaLinearBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<MediaItemUiState>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
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
        val item = dataList[position]
        holder.binding.apply {
            Glide.with(ivMediaIcon.context)
                .load(item.media.albumBytes ?: getIcon(item.media))
                .into(ivMediaIcon)
            tvMediaTitle.text = item.media.title
            tvMediaSubtitle.text = when(item.media.mediaType){
                MediaType.AUDIO->{
                    "${item.media.artist}/${item.media.duration.toDurationString()}/${item.media.size.toSizeString()}"
                }

                MediaType.VIDEO->{
                    ""
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(
        val binding: ItemMediaLinearBinding
    ) : RecyclerView.ViewHolder(binding.root)
}