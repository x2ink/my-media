package ink.x2.mymedia.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ink.x2.mymedia.data.repository.ScanRepositoryImpl
import ink.x2.mymedia.data.repository.MediaRepositoryImpl
import ink.x2.mymedia.data.repository.torrent.TorrentRepositoryImpl
import ink.x2.mymedia.domain.repository.ScanRepository
import ink.x2.mymedia.domain.repository.MediaRepository
import ink.x2.mymedia.domain.repository.torrent.TorrentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindTorrentRepository(impl: TorrentRepositoryImpl): TorrentRepository
}
