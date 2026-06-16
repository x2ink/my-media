package ink.x2.mymedia.di
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}