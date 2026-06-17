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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityScanBinding
import ink.x2.mymedia.databinding.DialogEditScanResultBinding
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
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
    }

    fun initListeners() {
        binding.btnScan.setOnClickListener {
            viewModel.queryScanMediaResult()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaList.collect { list ->
                    list.all {
                        it.isSelected
                    }.apply {
                        binding.cbSelectAll.isChecked=this
                    }
                    scanAdapter.submitList(list)
                }
            }
        }
    }
}