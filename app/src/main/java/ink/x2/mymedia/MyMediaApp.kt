package ink.x2.mymedia

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import ink.x2.mymedia.grpc.GrpcServerManager
import ink.x2.mymedia.playback.controller.PlaybackController
import javax.inject.Inject

@HiltAndroidApp
class MyMediaApp : Application(){
    @Inject
    lateinit var playbackController: PlaybackController
    @Inject
    lateinit var grpcServerManager: GrpcServerManager
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        grpcServerManager.start()
    }
}