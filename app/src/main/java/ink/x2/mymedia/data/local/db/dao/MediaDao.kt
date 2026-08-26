package ink.x2.mymedia.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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

    @Query("SELECT * FROM media WHERE id=:id")
    suspend fun getMediaItemById(id: Long): MediaEntity

    @Query("UPDATE media SET title = :title,updated_at = :updatedAt WHERE id = :id")
    suspend fun updateMediaItemTitleById(title: String,id: Long, updatedAt: Long)

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteMediaItemById(id: Long)

}