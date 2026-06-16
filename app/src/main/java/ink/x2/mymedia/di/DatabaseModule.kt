package ink.x2.mymedia.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ink.x2.mymedia.data.local.db.AppDatabase
import ink.x2.mymedia.data.local.db.dao.MediaDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(
            context, AppDatabase::class.java,
            "mymedia_db"
        ).build()
    }
    @Provides
    @Singleton
    fun provideMediaDao(database: AppDatabase): MediaDao {
        return database.mediaDao()
    }
}