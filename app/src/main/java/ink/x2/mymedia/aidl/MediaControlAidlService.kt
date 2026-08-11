package ink.x2.mymedia.aidl

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.aidl.IMediaControlService
import ink.x2.mymedia.playback.controller.PlaybackController
import javax.inject.Inject

@AndroidEntryPoint
class MediaControlAidlService: Service() {
    @Inject
    lateinit var playbackController: PlaybackController
    private var mainHandler = Handler(Looper.getMainLooper())
    private val binder =object :IMediaControlService.Stub(){
        override fun play() {
            mainHandler.post {
                playbackController.play()
            }
        }

        override fun pause() {
         mainHandler.post {
             playbackController.pause()
         }
        }

    }
    override fun onBind(p0: Intent?): IBinder {
        return binder
    }
}