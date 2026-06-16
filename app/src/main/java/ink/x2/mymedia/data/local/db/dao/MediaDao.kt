package ink.x2.mymedia.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import ink.x2.mymedia.data.local.db.entity.MediaEntity

@Dao
interface MediaDao {
    @Insert
    fun insertAll(medias: List<MediaEntity>)
}