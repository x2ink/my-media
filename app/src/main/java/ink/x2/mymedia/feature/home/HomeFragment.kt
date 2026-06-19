package ink.x2.mymedia.feature.home

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.R
import ink.x2.mymedia.core.base.BaseFragment
import ink.x2.mymedia.core.ext.toSizeString
import ink.x2.mymedia.domain.model.torrent.TorrentMetadata
import ink.x2.mymedia.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    companion object {
        const val TITLE: String = "下载"
    }

    private val viewModel: HomeViewModel by activityViewModels()
    private val torrentFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            binding.layoutMagnetInput.error = null
            viewModel.fetchTorrentFileMetadata(uri.toString())
                .onSuccess { metadata ->
                    showVideoSelectionDialog(metadata)
                }
                .onFailure { error ->
                    binding.layoutMagnetInput.error = error.message ?: "种子文件解析失败"
                }
        }
    }

    override fun bindView(view: View): FragmentHomeBinding {
        return FragmentHomeBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
    }

    private fun initListeners() {
        // Paste clipboard button
        binding.btnPaste.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val pasteText = clipData.getItemAt(0).text?.toString().orEmpty()
                binding.etMagnetLink.setText(pasteText)
            } else {
                Toast.makeText(requireContext(), "剪贴板为空", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStartDownload.setOnClickListener {
            val link = binding.etMagnetLink.text?.toString().orEmpty()
            viewLifecycleOwner.lifecycleScope.launch {
                binding.layoutMagnetInput.error = null
                viewModel.fetchTorrentMetadata(link)
                    .onSuccess { metadata ->
                        showVideoSelectionDialog(metadata)
                    }
                    .onFailure { error ->
                        binding.layoutMagnetInput.error = error.message
                            ?: getString(R.string.invalid_magnet_link)
                    }
            }
        }

        binding.btnSelectTorrent.setOnClickListener {
            torrentFilePicker.launch(
                arrayOf(
                    "application/x-bittorrent",
                    "application/octet-stream",
                    "*/*"
                )
            )
        }
    }

    private fun showVideoSelectionDialog(metadata: TorrentMetadata) {
        val videoFiles = metadata.videoFiles
        val checkedItems = BooleanArray(videoFiles.size) { true }
        val labels = videoFiles.map { file ->
            "${file.name} (${file.sizeBytes.toSizeString()})"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(metadata.name)
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.start_download) { _, _ ->
                val selectedIndexes = videoFiles
                    .filterIndexed { index, _ -> checkedItems[index] }
                    .map { it.index }
                if (selectedIndexes.isEmpty()) {
                    Toast.makeText(requireContext(), "请选择至少一个视频文件", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.startTorrentDownload(metadata, selectedIndexes)
                binding.etMagnetLink.text?.clear()
                Toast.makeText(requireContext(), "已添加视频下载任务", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Video total count stat
                launch {
                    viewModel.totalVideosCount.collect { count ->
                        binding.tvStatVideoCount.text = "$count 个"
                    }
                }

                // Space used stat
                launch {
                    viewModel.occupiedSpaceStr.collect { space ->
                        binding.tvStatSpaceUsed.text = space
                    }
                }

                launch {
                    viewModel.isFetchingMetadata.collect { loading ->
                        binding.btnStartDownload.isEnabled = !loading
                        binding.btnSelectTorrent.isEnabled = !loading
                        binding.btnStartDownload.text = if (loading) {
                            getString(R.string.parsing_magnet)
                        } else {
                            getString(R.string.parse_magnet)
                        }
                    }
                }
            }
        }
    }
}
