package ink.x2.mymedia

import android.os.Bundle
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityMainBinding

class MainActivity : BaseActivity(){
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}