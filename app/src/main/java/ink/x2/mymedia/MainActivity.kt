package ink.x2.mymedia

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import ink.x2.mymedia.core.base.BaseActivity
import ink.x2.mymedia.databinding.ActivityMainBinding
import ink.x2.mymedia.feature.home.HomeFragment
import ink.x2.mymedia.feature.audio.AudioFragment
import ink.x2.mymedia.feature.setting.SettingFragment
import ink.x2.mymedia.feature.video.VideoFragment
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val homeFragment by lazy { HomeFragment() }
    private val audioFragment by lazy { AudioFragment() }
    private val videoFragment by lazy { VideoFragment() }
    private val settingFragment by lazy { SettingFragment() }

    private var activeFragment: Fragment = homeFragment

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupFragments(savedInstanceState)
        setupBottomNavigation()
        PermissionX.init(this@MainActivity)
            .permissions(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, getString(R.string.permission_all_granted), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied_list,deniedList), Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun setupFragments(savedInstanceState: Bundle?) {
        val fm = supportFragmentManager
        if (savedInstanceState == null) {
            fm.beginTransaction().apply {
                add(R.id.fragmentContainer, homeFragment, "home")
                add(R.id.fragmentContainer, audioFragment, "audio")
                add(R.id.fragmentContainer, videoFragment, "video")
                add(R.id.fragmentContainer, settingFragment, "setting")
                show(homeFragment)
                hide(audioFragment)
                hide(videoFragment)
                hide(settingFragment)
                commit()
            }
            activeFragment = homeFragment
        } else {
            val fmHome = fm.findFragmentByTag("home") as? HomeFragment ?: homeFragment
            val fmAudio = fm.findFragmentByTag("audio") as? AudioFragment ?: audioFragment
            val fmVideo = fm.findFragmentByTag("video") as? VideoFragment ?: videoFragment
            val fmSetting = fm.findFragmentByTag("setting") as? SettingFragment ?: settingFragment
            val selectedItemId = binding.bottomNav.selectedItemId
            activeFragment = when (selectedItemId) {
                R.id.tab_home -> fmHome
                R.id.tab_audio -> fmAudio
                R.id.tab_video -> fmVideo
                R.id.tab_setting -> fmSetting
                else -> fmHome
            }
        }
    }
    override fun getInsetAppBar(): View = binding.appBar
    override fun getInsetBottomNav(): View =binding.bottomNav
    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val targetFragment = when (item.itemId) {
                R.id.tab_home -> supportFragmentManager.findFragmentByTag("home") ?: homeFragment
                R.id.tab_audio -> supportFragmentManager.findFragmentByTag("audio") ?: audioFragment
                R.id.tab_video -> supportFragmentManager.findFragmentByTag("video") ?: videoFragment
                R.id.tab_setting -> supportFragmentManager.findFragmentByTag("setting")
                    ?: settingFragment

                else -> null
            }

            if (targetFragment != null) {
                binding.toolbar.title = when (targetFragment) {
                    is HomeFragment -> HomeFragment.TITLE
                    is AudioFragment -> AudioFragment.TITLE
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