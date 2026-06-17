package ink.x2.mymedia.core.ext

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun Long.toSizeString(): String {
    if (this <= 0L) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        this >= gb -> String.format("%.2f GB", this / gb)
        this >= mb -> String.format("%.2f MB", this / mb)
        this >= kb -> String.format("%.2f KB", this / kb)
        else -> "$this B"
    }
}

fun Long.toDurationString(): String {
    if (this <= 0L) return "00:00"

    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
