package ink.x2.mymedia.feature.home

import android.view.View
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.databinding.FragmentHomeBinding

class HomeFragment: BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    companion object{
        const val TITLE : String = "首页"
    }
    override fun bindView(view: View): FragmentHomeBinding {
        return FragmentHomeBinding.bind(view)
    }
}