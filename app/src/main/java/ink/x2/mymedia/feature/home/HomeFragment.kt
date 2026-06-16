package ink.x2.mymedia.feature.home

import android.os.Bundle
import android.view.View
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.databinding.FragmentHomeBinding
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.domain.usecase.ScanMediaUseCase
import ink.x2.mymedia.feature.scan.ScanActivity
import javax.inject.Inject
@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    companion object{
        const val TITLE : String = "首页"
    }
    @Inject
    lateinit var scanMediaUseCase: ScanMediaUseCase
    private val viewModel: HomeViewModel by viewModels()
    override fun bindView(view: View): FragmentHomeBinding {
        return FragmentHomeBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }
    fun initListener(){
        binding.scanAudio.setOnClickListener {
            ScanActivity.startFrom(requireContext(), MediaType.AUDIO)
        }
        binding.scanVideo.setOnClickListener {
            ScanActivity.startFrom(requireContext(), MediaType.VIDEO)
        }
    }
}