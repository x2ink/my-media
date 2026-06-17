package ink.x2.mymedia.data.source.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.orhanobut.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import ink.x2.mymedia.di.IoDispatcher
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private fun getEmbeddedPicture(uri: Uri): ByteArray? {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bytes = retriever.embeddedPicture
            retriever.release()
            bytes
        } catch (e: Exception) {
            null
        }
    }
    suspend fun scanMedia(type: MediaType): List<LocalMediaItem> = withContext(ioDispatcher) {
        val mediaList = mutableListOf<LocalMediaItem>()

        val collection = when (type) {
            MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = when (type) {
            MediaType.AUDIO -> arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ID
            )

            MediaType.VIDEO -> arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_ADDED
            )
        }

        val selection = when (type) {
            MediaType.AUDIO -> """
                ${MediaStore.Audio.Media.IS_MUSIC} != 0
                AND ${MediaStore.Audio.Media.DURATION} > ?
                AND ${MediaStore.Audio.Media.SIZE} > ?
            """.trimIndent()

            MediaType.VIDEO -> """
                ${MediaStore.Video.Media.DURATION} > ?
                AND ${MediaStore.Video.Media.SIZE} > ?
            """.trimIndent()
        }

        val selectionArgs = arrayOf(
            "30000",
            "102400"
        )

        val sortOrder = when (type) {
            MediaType.AUDIO -> "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            MediaType.VIDEO -> "${MediaStore.Video.Media.DATE_ADDED} DESC"
        }

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            val artistColumn = if (type == MediaType.AUDIO) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            } else {
                -1
            }

            val albumIdColumn = if (type == MediaType.AUDIO) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                val uri = ContentUris.withAppendedId(
                    collection,
                    id
                )

                val item = LocalMediaItem(
                    id = id,
                    uriString = uri.toString(),
                    title = cursor.getString(titleColumn) ?: "未知媒体",
                    artist = if (artistColumn != -1) {
                        cursor.getString(artistColumn)
                    } else {
                        null
                    },
                    duration = cursor.getLong(durationColumn),
                    size = cursor.getLong(sizeColumn),
                    mimeType = cursor.getString(mimeTypeColumn),
                    dateAdded = cursor.getLong(dateAddedColumn),
                    albumId = cursor.getLong(albumIdColumn),
                    albumBytes = if (type == MediaType.AUDIO) {
                        getEmbeddedPicture(uri)
                    } else {
                        null
                    },
                    mediaType = type
                )

                mediaList.add(item)
            }
        }

        Logger.d(mediaList)
        mediaList
    }
}