package ink.x2.mymedia.feature.setting

import android.view.View
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.databinding.FragmentSettingBinding

class SettingFragment : BaseFragment<FragmentSettingBinding>(R.layout.fragment_setting) {
    companion object{
        const val TITLE : String =  "设置"
    }
    override fun bindView(view: View): FragmentSettingBinding {
        return FragmentSettingBinding.bind(view)
    }
}
