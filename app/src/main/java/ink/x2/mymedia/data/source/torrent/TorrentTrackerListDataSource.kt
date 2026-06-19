package ink.x2.mymedia.data.source.torrent

import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorrentTrackerListDataSource @Inject constructor() {

    fun fetchBestTrackers(): List<String> {
        val errors = mutableListOf<Throwable>()
        for (url in TRACKER_LIST_URLS) {
            runCatching {
                val trackers = fetch(url)
                if (trackers.isNotEmpty()) {
                    return trackers
                }
            }.onFailure { error ->
                errors += error
            }
        }
        throw errors.firstOrNull() ?: IllegalStateException("Tracker 列表为空")
    }

    private fun fetch(url: String): List<String> {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
        }
        return try {
            if (connection.responseCode !in 200..299) {
                error("Tracker 列表请求失败：${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .distinct()
                    .toList()
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val TIMEOUT_MS = 10_000
        private val TRACKER_LIST_URLS = listOf(
            "https://raw.githubusercontent.com/XIU2/TrackersListCollection/master/best.txt",
            "https://raw.githubusercontent.com/ngosang/trackerslist/master/trackers_best.txt"
        )
    }
}
