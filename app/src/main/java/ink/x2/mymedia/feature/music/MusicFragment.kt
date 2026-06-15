package ink.x2.mymedia.feature.music

import android.view.View
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.databinding.FragmentMusicBinding

class MusicFragment : BaseFragment<FragmentMusicBinding>(R.layout.fragment_music) {
    companion object{
        const val TITLE : String =  "音乐"
    }
    override fun bindView(view: View): FragmentMusicBinding {
        return FragmentMusicBinding.bind(view)
    }
}
