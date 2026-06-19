package ink.x2.mymedia.data.local.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import ink.x2.mymedia.core.common.AppError
import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivateMediaStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val baseMediaDir: File by lazy {
        File(context.getExternalFilesDir(null), "media").apply {
            if (!exists()) {
                mkdirs()
            }
            val nomedia = File(this, ".nomedia")
            if (!nomedia.exists()) {
                nomedia.createNewFile()
            }
        }
    }
    private val audioDir: File by lazy {
        File(baseMediaDir, "audio").apply { if (!exists()) mkdirs() }
    }
    private val videoDir: File by lazy {
        File(baseMediaDir, "video").apply { if (!exists()) mkdirs() }
    }
    private val torrentVideoDownloadDir: File by lazy {
        File(videoDir, "torrent").apply { if (!exists()) mkdirs() }
    }

    fun getTorrentVideoDir(): File = torrentVideoDownloadDir

    fun calculateFileHash(file: File): String? {
        return try {
            file.inputStream().use { inputStream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
    fun calculateUriHash(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                val hashBytes = digest.digest()
                hashBytes.joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
    suspend fun copyMediaToPrivateStorage(
        sourceUri: Uri,
        mediaType: MediaType,
        displayName: String?,
        mimeType: String?,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): AppResult<File> = withContext(ioDispatcher) {
        try {
            val targetDir = when (mediaType) {
                MediaType.AUDIO -> audioDir
                MediaType.VIDEO -> videoDir
            }
            val fileName = buildSafeFileName(
                displayName = displayName ?: queryDisplayName(sourceUri),
                mimeType = mimeType
            )
            val targetFile = File(targetDir, fileName)
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext AppResult.Error(
                    AppError.Unknown(
                        IllegalStateException("Cannot open input stream: $sourceUri")
                    )
                )
            inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(
                        out = output,
                        bufferSize = DEFAULT_BUFFER_SIZE
                    )
                }
            }

            AppResult.Success(targetFile)
        } catch (e: SecurityException) {
            AppResult.Error(AppError.SecurityException)
        } catch (e: Exception) {
            AppResult.Error(AppError.Unknown(e))
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1 && cursor.moveToFirst()) {
                cursor.getString(index)
            } else {
                null
            }
        }
    }

    private fun buildSafeFileName(
        displayName: String?,
        mimeType: String?
    ): String {
        val extension = getExtensionFromMimeType(mimeType)
        val rawName = displayName
            ?.substringBeforeLast(".", missingDelimiterValue = displayName)
            ?.takeIf { it.isNotBlank() }
            ?: "media"
        val safeName = rawName
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .take(80)
        return "${System.currentTimeMillis()}_${safeName}$extension"
    }
    private fun getExtensionFromMimeType(mimeType: String?): String {
        return when (mimeType) {
            "audio/mpeg" -> ".mp3"
            "audio/mp4" -> ".m4a"
            "audio/aac" -> ".aac"
            "audio/flac" -> ".flac"
            "audio/ogg" -> ".ogg"
            "audio/wav", "audio/x-wav" -> ".wav"

            "video/mp4" -> ".mp4"
            "video/3gpp" -> ".3gp"
            "video/x-matroska" -> ".mkv"
            "video/webm" -> ".webm"

            else -> ""
        }
    }
}
