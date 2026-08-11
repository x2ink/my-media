package ink.x2.grpctest.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import ink.x2.aidl.IMediaControlService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MediaAidlClient @Inject constructor(
    @ApplicationContext private val context: Context
){
    private var mediaService: IMediaControlService?=null
    private var isBound = false
    private var _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private val connection= object : ServiceConnection{
        override fun onServiceConnected(
            p0: ComponentName?,
            p1: IBinder?
        ) {
           mediaService = IMediaControlService.Stub.asInterface(p1)
            _isConnected.value=true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
           mediaService = null
            _isConnected.value=false
        }

    }
    fun connect(){
        if(isBound) return
        val intent= Intent().apply {
            component = ComponentName("ink.x2.mymedia","ink.x2.mymedia.aidl.MediaControlAidlService")
        }
        isBound = context.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE,
        )

    }
    fun disconnect() {
        if (!isBound) {
            return
        }
        context.unbindService(connection)
        isBound = false
        mediaService = null
        _isConnected.value = false
    }

    fun play(): Boolean {
        val service = mediaService ?: return false

        return try {
            service.play()
            true
        } catch (exception: RemoteException) {
            false
        }
    }

    fun pause(): Boolean {
        val service = mediaService ?: return false

        return try {
            service.pause()
            true
        } catch (exception: RemoteException) {
            false
        }
    }

}