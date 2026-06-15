package ink.x2.mymedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityMainBinding
import ink.x2.mymedia.feature.home.HomeFragment
import ink.x2.mymedia.feature.music.MusicFragment
import ink.x2.mymedia.feature.setting.SettingFragment
import ink.x2.mymedia.feature.video.VideoFragment

class MainActivity : BaseActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val homeFragment by lazy { HomeFragment() }
    private val musicFragment by lazy { MusicFragment() }
    private val videoFragment by lazy { VideoFragment() }
    private val settingFragment by lazy { SettingFragment() }

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets()
        setupFragments(savedInstanceState)
        setupBottomNavigation()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        val fm = supportFragmentManager
        if (savedInstanceState == null) {
            fm.beginTransaction().apply {
                add(R.id.fragmentContainer, homeFragment, "home")
                add(R.id.fragmentContainer, musicFragment, "music")
                add(R.id.fragmentContainer, videoFragment, "video")
                add(R.id.fragmentContainer, settingFragment, "setting")
                show(homeFragment)
                hide(musicFragment)
                hide(videoFragment)
                hide(settingFragment)
                commit()
            }
            activeFragment = homeFragment
        } else {
            val fmHome = fm.findFragmentByTag("home") as? HomeFragment ?: homeFragment
            val fmMusic = fm.findFragmentByTag("music") as? MusicFragment ?: musicFragment
            val fmVideo = fm.findFragmentByTag("video") as? VideoFragment ?: videoFragment
            val fmSetting = fm.findFragmentByTag("setting") as? SettingFragment ?: settingFragment
            val selectedItemId = binding.bottomNav.selectedItemId
            activeFragment = when (selectedItemId) {
                R.id.tab_home -> fmHome
                R.id.tab_music -> fmMusic
                R.id.tab_video -> fmVideo
                R.id.tab_setting -> fmSetting
                else -> fmHome
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val targetFragment = when (item.itemId) {
                R.id.tab_home -> supportFragmentManager.findFragmentByTag("home") ?: homeFragment
                R.id.tab_music -> supportFragmentManager.findFragmentByTag("music") ?: musicFragment
                R.id.tab_video -> supportFragmentManager.findFragmentByTag("video") ?: videoFragment
                R.id.tab_setting -> supportFragmentManager.findFragmentByTag("setting") ?: settingFragment
                else -> null
            }

            if (targetFragment != null) {
                binding.toolbar.title = when (targetFragment) {
                    is HomeFragment -> HomeFragment.TITLE
                    is MusicFragment -> MusicFragment.TITLE
                    is VideoFragment -> VideoFragment.TITLE
                    is SettingFragment -> SettingFragment.TITLE
                    else -> ""
                }
                if (targetFragment != activeFragment) {
                    supportFragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(targetFragment)
                        .commit()

                    activeFragment = targetFragment
                }

                true
            } else {
                false
            }
        }
    }
}