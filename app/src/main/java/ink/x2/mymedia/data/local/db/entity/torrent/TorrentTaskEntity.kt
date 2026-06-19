package ink.x2.mymedia.data.local.db.entity.torrent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "torrent_task",
    indices = [
        Index(value = ["info_hash"])
    ]
)
data class TorrentTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "magnet_uri")
    val magnetUri: String,
    @ColumnInfo(name = "info_hash")
    val infoHash: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "selected_file_indexes")
    val selectedFileIndexes: String,
    @ColumnInfo(name = "selected_file_names")
    val selectedFileNames: String,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "progress")
    val progress: Int = 0,
    @ColumnInfo(name = "downloaded_bytes")
    val downloadedBytes: Long = 0,
    @ColumnInfo(name = "total_bytes")
    val totalBytes: Long = 0,
    @ColumnInfo(name = "download_speed_bytes")
    val downloadSpeedBytes: Long = 0,
    @ColumnInfo(name = "peers_count")
    val peersCount: Int = 0,
    @ColumnInfo(name = "save_dir")
    val saveDir: String,
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
