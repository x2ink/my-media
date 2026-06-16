package ink.x2.mymedia.feature.scan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityScanBinding
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanActivity : BaseActivity<ActivityScanBinding>() {
    private val binding: ActivityScanBinding by lazy {
        ActivityScanBinding.inflate(layoutInflater)
    }
    private val viewModel: ScanViewModel by viewModels()
    companion object {
        fun startFrom(activity: Context, mediaType: MediaType) {
            val intent = Intent(activity, ScanActivity::class.java)
            intent.putExtra("media_type", mediaType)
            activity.startActivity(intent)
        }
    }
    override fun getInsetAppBar(): View = binding.appBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initListeners()
        observeViewModel()

    }
    fun initListeners(){
        binding.btnScan.setOnClickListener {
            viewModel.queryScanMediaResult()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaList.collect { list ->
                    Logger.d("扫描到媒体数量: ${list.size}")
                    Logger.d(list)
                }
            }
        }
    }
}