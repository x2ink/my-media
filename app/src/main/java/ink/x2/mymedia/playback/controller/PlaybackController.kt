package ink.x2.mymedia.playback.controller
import android.content.ComponentName
import androidx.media3.session.SessionToken
import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import ink.x2.mymedia.playback.service.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class PlaybackController @Inject constructor(
    @param:ApplicationContext private val context: Context
){
    private val sessionToken by lazy {
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
    }
    private val controllerFuture by lazy {
        MediaController.Builder(context,sessionToken).setApplicationLooper(Looper.getMainLooper()).buildAsync()
    }
    private var mediaController: MediaController? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying : StateFlow<Boolean> = _isPlaying.asStateFlow()
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()
    private fun connectToService(){
        controllerFuture.addListener({
            val controller = controllerFuture.get() ?: return@addListener
            mediaController = controller
            setupPlayerListener(controller)
        }, MoreExecutors.directExecutor())
    }
    private fun setupPlayerListener(player: Player){
        _isPlaying.value = player.isPlaying
        _currentMediaItem.value = player.currentMediaItem
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaItem.value = mediaItem
            }
        })
    }
    fun play() {
        mediaController?.play()
    }
    fun hasNext(): Boolean {
        return mediaController?.hasNextMediaItem() ?: false
    }
    fun hasPrevious(): Boolean {
        return mediaController?.hasPreviousMediaItem() ?: false
    }
    fun pause() {
        mediaController?.pause()
    }
    fun skipToNext() {
        mediaController?.seekToNext()
    }
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }
    fun getCurrentPosition(): Long{
        return mediaController?.currentPosition?:0L
    }
    fun getDuration(): Long{
        val duration = mediaController?.duration ?: 0L
        return if (duration < 0) 0L else duration
    }
    fun release() {
        controllerFuture.let {
            MediaController.releaseFuture(it)
        }
    }
    fun getPlayer(): Player? {
        return mediaController
    }
    fun playMediaList(mediaItems: List<MediaItem>, startIndex: Int) {
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItems)
            controller.seekTo(startIndex, 0)
            controller.prepare()
            controller.play()
        }
    }
    init {
        connectToService()
    }
}