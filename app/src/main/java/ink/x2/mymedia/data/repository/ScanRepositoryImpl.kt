package ink.x2.mymedia.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.orhanobut.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import ink.x2.mymedia.core.common.AppResult
import ink.x2.mymedia.core.common.onError
import ink.x2.mymedia.core.common.onSuccess
import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.local.storage.PrivateMediaStorage
import ink.x2.mymedia.data.source.mediastore.MediaStoreScanner
import ink.x2.mymedia.di.IoDispatcher
import ink.x2.mymedia.domain.model.ImportProgress
import ink.x2.mymedia.domain.model.ImportStatus
import ink.x2.mymedia.domain.repository.ScanRepository
import javax.inject.Inject
import ink.x2.mymedia.domain.model.LocalMediaItem
import ink.x2.mymedia.domain.model.MediaType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ScanRepositoryImpl @Inject constructor(
    private val mediaStoreScanner: MediaStoreScanner,
    private val privateMediaStorage: PrivateMediaStorage,
    private val mediaDao: MediaDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : ScanRepository {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun queryScanMediaResult(type: MediaType): AppResult<List<LocalMediaItem>> {
        return mediaStoreScanner.scanMedia(type)
    }

    override suspend fun importSelectdMedia(mediaList: List<LocalMediaItem>): Flow<AppResult<ImportProgress>> =
        flow {
            val total = mediaList.size
            val failedItems = mutableListOf<LocalMediaItem>()
            val successEntities = mutableListOf<MediaEntity>()
            mediaList.forEachIndexed { index, item ->
                val currentStep = index + 1
                emit(
                    AppResult.Success(
                        ImportProgress.Loading(
                            current = currentStep,
                            total = total,
                            currentItem = item,
                            status = ImportStatus.IN_PROGRESS
                        )
                    )
                )
                try {
                    val fileHash=privateMediaStorage.calculateUriHash(context,item.uriString.toUri())?:""
                    Logger.i(fileHash)
                    if(!mediaDao.exitsByHash(fileHash)){
                        privateMediaStorage.copyMediaToPrivateStorage(
                            sourceUri = item.uriString.toUri(),
                            mediaType = item.mediaType,
                            displayName = item.title,
                            mimeType = item.mimeType,
                        ).onSuccess { file ->
                            successEntities.add(
                                MediaEntity(
                                    type = item.mediaType.name,
                                    title = item.title,
                                    artist = item.artist,
                                    durationMs = item.duration,
                                    sizeBytes = item.size,
                                    mimeType = item.mimeType,
                                    localRelativePath = file.absolutePath,
                                    sourceUri = item.uriString,
                                    hash = fileHash
                                )
                            )
                        }.onError {
                            failedItems.add(item)
                            emit(
                                AppResult.Success(
                                    ImportProgress.Loading(
                                        current = currentStep,
                                        total = total,
                                        currentItem = item,
                                        status = ImportStatus.FAILURE
                                    )
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Logger.d(e)
                    failedItems.add(item)
                    emit(
                        AppResult.Success(
                            ImportProgress.Loading(
                                current = currentStep,
                                total = total,
                                currentItem = item,
                                status = ImportStatus.FAILURE
                            )
                        )
                    )
                }
            }
            if (successEntities.isNotEmpty()) {
                try {
                    mediaDao.insertAll(successEntities)
                    emit(AppResult.Success(ImportProgress.Success(
                        successCount = successEntities.size,
                        failedItems = failedItems.toList()
                    )
                    ))
                } catch (e: Exception) {
                    emit(AppResult.Success(ImportProgress.Failure))
                }
            }else{
                emit(AppResult.Success(ImportProgress.Success(
                    successCount = successEntities.size,
                    failedItems = failedItems.toList()
                )
                ))
            }
        }.flowOn(ioDispatcher)
}