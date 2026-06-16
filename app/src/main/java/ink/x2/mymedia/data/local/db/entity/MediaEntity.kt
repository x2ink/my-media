package ink.x2.mymedia.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    val artist: String?,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
    @ColumnInfo(name = "mime_type")
    val mimeType: String?,
    @ColumnInfo(name = "local_relative_path")
    val localRelativePath: String,// 导入后的相对存储路径（如 media/audio/xxx.mp3）
    @ColumnInfo(name = "source_uri")
    val sourceUri: String, // 原始源 URI 字符串
    @ColumnInfo(name = "cover_relative_path")
    val coverRelativePath: String?, // 专辑封面相对路径，可空
    @ColumnInfo(name = "imported_at")
    val importedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_played_at")
    val lastPlayedAt: Long? = null, // 上次播放时间戳，可空
    @ColumnInfo(name = "last_position_ms")
    val lastPositionMs: Long = 0, // 上次播放进度（毫秒）
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0, // 播放次数
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(), // 创建时间戳
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis() // 更新时间戳
)