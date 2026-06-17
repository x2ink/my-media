package ink.x2.mymedia.playback.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService: MediaSessionService() {
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this@PlaybackService).build().apply {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA).build()
            setAudioAttributes(audioAttributes,true)
        }
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