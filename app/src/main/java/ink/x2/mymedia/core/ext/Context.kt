package ink.x2.mymedia.core.ext

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toastText(
    @StringRes stringId: Int,
    duration: Int = Toast.LENGTH_SHORT
) {
    Toast.makeText(this, stringId, duration).show()
}