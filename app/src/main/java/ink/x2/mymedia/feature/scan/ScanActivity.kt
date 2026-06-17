package ink.x2.mymedia.feature.scan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityScanBinding
import ink.x2.mymedia.databinding.DialogEditScanResultBinding
import ink.x2.mymedia.domain.model.MediaType
import ink.x2.mymedia.feature.playing.PlayingActivity
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScanActivity : BaseActivity<ActivityScanBinding>() {
    private val binding: ActivityScanBinding by lazy {
        ActivityScanBinding.inflate(layoutInflater)
    }
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var scanAdapter: ScanResultListAdapter

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
        initRecyclerView()
        observeViewModel()
    }

    @SuppressLint("InflateParams")
    private fun showInputDialog(mediaItem: MediaItemUiState) {
        val dialogBinding = DialogEditScanResultBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(this@ScanActivity)
            .setTitle("重命名")
            .setView(dialogBinding.root)
            .setNegativeButton("取消", null)
            .setPositiveButton("确认", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val text = dialogBinding.inputEditText.text?.toString()?.trim().orEmpty()
                        if (text.isEmpty()) {
                            dialogBinding.inputLayout.error = "内容不能为空"
                            return@setOnClickListener
                        }
                        dialogBinding.inputLayout.error = null
                        val updatedMedia = mediaItem.media.copy(title = text)
                        val updatedUiState = mediaItem.copy(media = updatedMedia)
                        viewModel.updateMediaList(updatedUiState)
                        dismiss()
                    }
                }
            }
            .show()
    }

    private fun initRecyclerView() {
        scanAdapter = ScanResultListAdapter(onEditClick = { data ->
            showInputDialog(data)
        }, onSelectClick = { media, isSelected ->
            viewModel.updateListSelect(media, isSelected)
        }, onClickItem = {media->
            viewModel.openPlayingActivity(media)
        })
        binding.rvScanResults.layoutManager = LinearLayoutManager(this)
        binding.rvScanResults.addItemDecoration(
            VerticalGapDecoration(
                resources.getDimensionPixelSize(
                    R.dimen.dimens_16
                )
            )
        )
        binding.rvScanResults.adapter = scanAdapter
        (binding.rvScanResults.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    fun initListeners() {
        binding.btnScan.setOnClickListener {
            viewModel.queryScanMediaResult()
        }
        when(viewModel.getMediaType()){
            MediaType.AUDIO->binding.toolbar.title=getString(R.string.scan_audio)
            MediaType.VIDEO->binding.toolbar.title=getString(R.string.scan_video)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.cbSelectAll.setOnCheckedChangeListener { button, bool ->
            viewModel.updateListSelectAll(bool)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.mediaList.collect { list ->
                        binding.cbSelectAll.isChecked =
                            list.isNotEmpty() && list.all { it.isSelected }

                        scanAdapter.submitList(list)
                    }
                }

                launch {
                    viewModel.openPlayingEvent.collect { media ->
                        PlayingActivity.startFrom(this@ScanActivity)
                    }
                }
            }
        }
    }
}