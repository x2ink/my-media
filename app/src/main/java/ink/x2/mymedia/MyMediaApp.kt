package ink.x2.mymedia

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import ink.x2.mymedia.playback.controller.PlaybackController
import javax.inject.Inject

@HiltAndroidApp
class MyMediaApp : Application(){
    @Inject
    lateinit var playbackController: PlaybackController
    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}