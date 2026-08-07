package ink.x2.mymedia.grpc

import io.grpc.InsecureServerCredentials
import io.grpc.Server
import io.grpc.okhttp.OkHttpServerBuilder
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrpcServerManager @Inject constructor(
    private val mediaGrpcService: MediaGrpcService
) {
    private var server: Server? = null
    fun start(){
        if(server!=null) return
        server= OkHttpServerBuilder.forPort(
            InetSocketAddress("127.0.0.1",50051),
            InsecureServerCredentials.create()
        ).addService(mediaGrpcService).build().start()
    }
    fun stop(){
        server?.shutdownNow()
        server=null
    }
}