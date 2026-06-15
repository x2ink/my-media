package ink.x2.mymedia.core.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment<VB : ViewBinding>(@LayoutRes layoutId: Int) : Fragment(layoutId) {
    private var _binding :VB?=null
    protected val binding:VB get() = _binding?:error("无效的viewbinding")
    protected abstract fun bindView(view: View):VB
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = bindView(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        super.onDestroyView()
    }
}