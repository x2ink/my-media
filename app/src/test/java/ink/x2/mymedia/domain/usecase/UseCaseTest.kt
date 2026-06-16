package ink.x2.mymedia.domain.usecase

import android.content.Context
import android.util.Log
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import ink.x2.mymedia.data.repository.ScanRepositoryImpl
import ink.x2.mymedia.data.source.mediastore.MediaStoreScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UseCaseTest {
    @Before
    fun setup(){
        mockkStatic(Log::class)
        every { Log.println(any(), any(), any()) } answers {
            val priority = arg<Int>(0)
            val tag = arg<String?>(1) ?: "Logger"
            val msg = arg<String>(2)
            println("[$tag] $msg")
            0
        }
        every { Log.isLoggable(any(), any()) } returns true
        Logger.clearLogAdapters()
        Logger.addLogAdapter(AndroidLogAdapter())
    }
    @Test
    fun `Test media scan results`() = runTest {
        val mockContext = mockk<Context>()
        val mediaStoreScanner = MediaStoreScanner(mockContext, Dispatchers.Unconfined)
        val realRepository = ScanRepositoryImpl(mediaStoreScanner)
        val realScanMediaUseCase = ScanMediaUseCase(realRepository)
        val result = realScanAudioUseCase()
    }
}