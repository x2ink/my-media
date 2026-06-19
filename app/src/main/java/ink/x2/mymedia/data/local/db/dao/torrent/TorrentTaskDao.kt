package ink.x2.mymedia.data.local.db.dao.torrent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ink.x2.mymedia.data.local.db.entity.torrent.TorrentTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TorrentTaskDao {
    @Insert
    suspend fun insert(entity: TorrentTaskEntity): Long

    @Update
    suspend fun update(entity: TorrentTaskEntity)

    @Query("SELECT * FROM torrent_task ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TorrentTaskEntity>>

    @Query("SELECT * FROM torrent_task WHERE id = :taskId LIMIT 1")
    suspend fun getById(taskId: Long): TorrentTaskEntity?

    @Query("SELECT * FROM torrent_task WHERE status IN (:statuses) ORDER BY created_at ASC")
    suspend fun getByStatuses(statuses: List<String>): List<TorrentTaskEntity>

    @Query("DELETE FROM torrent_task WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)
}
