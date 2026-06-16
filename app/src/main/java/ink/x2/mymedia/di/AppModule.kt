package ink.x2.mymedia.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ink.x2.mymedia.data.repository.ScanRepositoryImpl
import ink.x2.mymedia.domain.repository.ScanRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository
}