package ink.x2.mymedia.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.local.db.dao.torrent.TorrentTaskDao
import ink.x2.mymedia.data.local.db.entity.MediaEntity
import ink.x2.mymedia.data.local.db.entity.torrent.TorrentTaskEntity

@Database(entities = [MediaEntity::class, TorrentTaskEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun torrentTaskDao(): TorrentTaskDao
}
