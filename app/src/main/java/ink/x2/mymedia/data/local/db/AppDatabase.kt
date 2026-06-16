package ink.x2.mymedia.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ink.x2.mymedia.data.local.db.dao.MediaDao
import ink.x2.mymedia.data.local.db.entity.MediaEntity

@Database(entities = [MediaEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}