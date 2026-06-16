package ink.x2.mymedia.core.base

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding

open class BaseActivity<VB : ViewBinding>: AppCompatActivity() {
    open fun getInsetAppBar(): View? = null
    open fun getInsetBottomNav(): View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupInsets()
    }
    private fun setupInsets() {
        getInsetAppBar()?.let { appBar ->
            ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(view.paddingLeft, systemBars.top, view.paddingRight, view.paddingBottom)
                insets
            }
        }
        getInsetBottomNav()?.let { bottomNav ->
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, systemBars.bottom)
                insets
            }
        }
    }
}