package ink.x2.mymedia.feature.audio

import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.databinding.FragmentAudioBinding
@AndroidEntryPoint
class AudioFragment : BaseFragment<FragmentAudioBinding>(R.layout.fragment_audio) {
    companion object{
        const val TITLE : String =  "媒体"
    }
    override fun bindView(view: View): FragmentAudioBinding {
        return FragmentAudioBinding.bind(view)
    }
}
