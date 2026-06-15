package ink.x2.mymedia.feature.video

import android.view.View
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.databinding.FragmentVideoBinding

class VideoFragment : BaseFragment<FragmentVideoBinding>(R.layout.fragment_video) {
    companion object{
        const val TITLE : String =  "视频"
    }
    override fun bindView(view: View): FragmentVideoBinding {
        return FragmentVideoBinding.bind(view)
    }
}
