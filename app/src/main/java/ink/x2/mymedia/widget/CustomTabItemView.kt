package ink.x2.mymedia.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import ink.x2.mymedia.databinding.ViewCustomTabItemBinding

class CustomTabItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewCustomTabItemBinding.inflate(LayoutInflater.from(context), this, true)

    lateinit var data: TabItemData
        private set

    fun bind(data: TabItemData) {
        this.data = data
        binding.tvTitle.text = data.title
        if(data.count<=0){
            binding.tvBadge.isVisible = false
            binding.tvBadge.text = data.count.toString()
        }else{
            binding.tvBadge.isVisible = true
        }

    }
    fun setSelectedState(selected: Boolean) {
        isSelected = selected
        binding.root.isSelected = selected
    }
}