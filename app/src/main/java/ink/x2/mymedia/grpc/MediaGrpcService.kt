package ink.x2.mymedia.grpc

import android.os.Handler
import android.os.Looper
import ink.x2.mymedia.playback.controller.PlaybackController
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaGrpcService @Inject constructor(
    private val playbackController: PlaybackController
) : MediaControlServiceGrpc.MediaControlServiceImplBase(){
    private val mainHandler = Handler(Looper.getMainLooper())
    override fun play(request: ControlRequest,responseObserver: StreamObserver<ControlResponse>){
        mainHandler.post {
            playbackController.play()
            val response = ControlResponse.newBuilder().setSuccess(true).setMessage("success").build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }

    override fun pause(request: ControlRequest,responseObserver: StreamObserver<ControlResponse>) {
       mainHandler.post {
           playbackController.pause()
           val response = ControlResponse.newBuilder().setSuccess(true).setMessage("success").build()
           responseObserver.onNext(response)
           responseObserver.onCompleted()
       }
    }
}