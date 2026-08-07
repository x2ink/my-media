package ink.x2.grpctest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.grpctest.grpc.MediaGrpcClient
import ink.x2.grpctest.ui.theme.MyMediaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mediaGrpcClient: MediaGrpcClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMediaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),) {
                        Button(onClick = {
                            mediaGrpcClient.play()
                        }) {
                          Text("播放")
                        }
                        Button(onClick = {
                            mediaGrpcClient.stop()
                        }) {
                            Text("暂停")
                        }
                    }
                }
            }
        }
    }
    fun startPlay(){

    }
    fun stopPlay(){

    }
}
