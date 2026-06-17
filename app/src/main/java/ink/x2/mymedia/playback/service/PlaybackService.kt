package ink.x2.mymedia.playback.service

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService: MediaSessionService() {
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this@PlaybackService).build()
    }
    private var mediaSession: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        exoPlayer.let { player ->
            mediaSession = MediaSession.Builder(this, player).build()
        }
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.apply {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}