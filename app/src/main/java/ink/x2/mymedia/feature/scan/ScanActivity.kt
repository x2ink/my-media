package ink.x2.mymedia.feature.scan

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityScanBinding
import ink.x2.mymedia.databinding.DialogEditScanResultBinding
import ink.x2.mymedia.databinding.DialogImportProgressBinding
import ink.x2.mymedia.domain.model.ImportProgress
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
    private var scanRotationAnimator: android.animation.ObjectAnimator? = null
    private var progressDialog: AlertDialog? = null
    private var progressDialogBinding: DialogImportProgressBinding? = null

    companion object {
        fun startFrom(activity: Context, mediaType: MediaType) {
            val intent = Intent(activity, ScanActivity::class.java)
            intent.putExtra("media_type", mediaType)
            activity.startActivity(intent)
        }
    }

    override fun getInsetAppBar(): View = binding.appBar
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
            viewModel.clearList()
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
        binding.btnImport.setOnClickListener {
            viewModel.importSelectedMedia()
        }
    }
    fun startScanAni(){
        scanRotationAnimator = android.animation.ObjectAnimator.ofFloat(binding.ivScanIcon, "rotation", 0f, 360f).apply {
            duration = 1000
            repeatCount = android.animation.ValueAnimator.INFINITE
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }

        binding.btnScan.animate().translationY(resources.getDimension(R.dimen.dimens_64)).setDuration(300).start()
        binding.tvResultsTitle.animate().alpha(0f).setDuration(300).start()
        binding.tvEmpty.animate().alpha(0f).setDuration(300).start()
        binding.rvScanResults.animate().alpha(0f).setDuration(300).start()
        binding.bottomBar.animate().alpha(0f).setDuration(300).start()
    }
    fun endScanAni(){
        scanRotationAnimator?.cancel()
        scanRotationAnimator = null
        binding.ivScanIcon.rotation = 0f

        binding.btnScan.animate().translationY(0f).setDuration(300).start()
        binding.tvResultsTitle.animate().alpha(1f).setDuration(300).start()
        
        val listIsEmpty = viewModel.mediaList.value.isEmpty()
        binding.tvEmpty.visibility = if (listIsEmpty) View.VISIBLE else View.GONE
        binding.tvEmpty.animate().alpha(if (listIsEmpty) 1f else 0f).setDuration(300).start()
        
        binding.rvScanResults.animate().alpha(1f).setDuration(300).start()
        binding.bottomBar.animate().alpha(1f).setDuration(300).start()
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                    viewModel.scanStatue.collect {
                        when(it){
                            ScanStatus.Idel->{

                            }

                            ScanStatus.Start->{
                                startScanAni()
                            }

                            ScanStatus.End->{
                                endScanAni()
                            }
                        }
                    }
                }
                launch {
                    viewModel.importProgress.collect { progress ->
                        updateImportProgress(progress)
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event->
                        when(event){
                            is ScanUiEvent.OpenPlaying->{
                                PlayingActivity.startFrom(this@ScanActivity)
                            }
                            is ScanUiEvent.ShowMessage->{
                                Toast.makeText(this@ScanActivity, getString(event.stringId), Toast.LENGTH_SHORT).show()
                            }
                            is ScanUiEvent.RequestMediaPermission->{
                                PermissionX.init(this@ScanActivity)
                                    .permissions(
                                        Manifest.permission.READ_MEDIA_AUDIO,
                                        Manifest.permission.READ_MEDIA_VIDEO,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                    .request { allGranted, grantedList, deniedList ->
                                        if (allGranted) {
                                            Toast.makeText(this@ScanActivity, getString(R.string.permission_all_granted), Toast.LENGTH_LONG).show()
                                            viewModel.queryScanMediaResult()
                                        } else {
                                            Toast.makeText(this@ScanActivity, getString(R.string.permission_denied_list,deniedList), Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateImportProgress(progress: ImportProgress?) {
        if (progress == null) {
            dismissProgressDialog()
            return
        }

        when (progress) {
            is ImportProgress.Loading -> {
                if (progressDialog == null) {
                    val dialogBinding = DialogImportProgressBinding.inflate(layoutInflater)
                    progressDialogBinding = dialogBinding
                    progressDialog = MaterialAlertDialogBuilder(this)
                        .setTitle("正在导入媒体")
                        .setView(dialogBinding.root)
                        .setCancelable(false)
                        .create()
                    progressDialog?.show()
                }

                progressDialogBinding?.apply {
                    progressIndicator.max = progress.total
                    progressIndicator.progress = progress.current
                    tvProgressText.text = "正在导入: ${progress.current}/${progress.total}\n${progress.currentItem.title}"
                }
            }
            is ImportProgress.Success -> {
                dismissProgressDialog()
                val msg = "导入完成！成功: ${progress.successCount} 首，失败: ${progress.failedItems.size} 首"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.resetImportProgress()
                if (progress.successCount > 0) {
                    finish()
                }
            }
            is ImportProgress.Failure -> {
                dismissProgressDialog()
                Toast.makeText(this, "导入失败，请重试", Toast.LENGTH_SHORT).show()
                viewModel.resetImportProgress()
            }
        }
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
        progressDialogBinding = null
    }

    override fun onDestroy() {
        dismissProgressDialog()
        scanRotationAnimator?.cancel()
        super.onDestroy()
    }
}