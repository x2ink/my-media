package ink.x2.mymedia.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ink.x2.mymedia.data.local.db.entity.MediaEntity

@Dao
interface MediaDao {
    @Insert
    fun insertAll(medias: List<MediaEntity>)
    @Query("SELECT EXISTS(SELECT 1 FROM media WHERE hash = :hash LIMIT 1)")
    fun exitsByHash(hash: String) : Boolean
}