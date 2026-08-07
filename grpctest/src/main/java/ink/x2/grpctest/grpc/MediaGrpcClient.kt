package ink.x2.grpctest.grpc

import android.util.Log
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaGrpcClient @Inject constructor() {
    private val channel: ManagedChannel = OkHttpChannelBuilder.forAddress("127.0.0.1",50051).usePlaintext().build()
    private val stub = MediaControlServiceGrpc.newStub(channel)
    fun play(){
        val request = ControlRequest.getDefaultInstance()
        stub.play(request,object : StreamObserver<ControlResponse> {
            override fun onNext(response: ControlResponse?) {
                Log.d(
                    "GrpcClient",
                    "success=${response?.success}, message=${response?.message}"
                )
            }

            override fun onError(t: Throwable?) {
                Log.e("GrpcClient", "play error", t)
            }

            override fun onCompleted() {
                Log.d("GrpcClient", "play completed")
            }
        })
    }
    fun stop(){
        val request = ControlRequest.getDefaultInstance()
        stub.pause(request,object : StreamObserver<ControlResponse> {
            override fun onNext(response: ControlResponse?) {
                Log.d(
                    "GrpcClient",
                    "success=${response?.success}, message=${response?.message}"
                )
            }

            override fun onError(t: Throwable?) {
                Log.e("GrpcClient", "play error", t)
            }

            override fun onCompleted() {
                Log.d("GrpcClient", "play completed")
            }
        })
    }
}