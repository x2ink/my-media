package ink.x2.mymedia.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert
    suspend fun insertAll(medias: List<MediaEntity>)
    @Query("SELECT EXISTS(SELECT 1 FROM media WHERE hash = :hash LIMIT 1)")
    suspend fun exitsByHash(hash: String) : Boolean

    @Query("SELECT * FROM media WHERE type = :type ORDER BY imported_at DESC")
    fun getMediaByTypeFlow(type: String): Flow<List<MediaEntity>>

    @Update
    suspend fun updateMediaItemInfo(media: MediaEntity)

    @Delete
    suspend fun deleteMediaItem(media: MediaEntity)
}