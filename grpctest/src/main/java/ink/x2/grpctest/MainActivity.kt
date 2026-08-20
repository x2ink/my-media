package ink.x2.grpctest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.grpctest.aidl.MediaAidlClient
import ink.x2.grpctest.grpc.MediaGrpcClient
import ink.x2.grpctest.ui.theme.MyMediaTheme
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mediaAidlClient: MediaAidlClient

    @Inject
    lateinit var mediaGrpcClient: MediaGrpcClient
    var bottomTabs = listOf("首页", "发现", "我的")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMediaTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        var selectedIndex by remember { mutableIntStateOf(0) }
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {},
                    title = {
                        Text("这是标题")
                    }

                )
            },
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar() {
                    bottomTabs.forEachIndexed { index, string ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            label = { Text(string) },
                            icon = {}
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
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
                Button(onClick = {
                    mediaAidlClient.play()
                }) {
                    Text("播放")
                }
                Button(onClick = {
                    mediaAidlClient.pause()
                }) {
                    Text("暂停")
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        mediaAidlClient.connect()
    }
}
