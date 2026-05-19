package com.example.statussaver.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.statussaver.R
import com.example.statussaver.databinding.ActivityMainBinding
import com.example.statussaver.ui.FragmentSaveStatus
import com.example.statussaver.ui.FragmentSettings
import com.example.statussaver.ui.FragmentStatus
import com.example.statussaver.utils.Constants
import com.example.statussaver.utils.SharedPrefUtils
import com.example.statussaver.utils.replaceFragment
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    private val activity = this

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var currentSelectedItem = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        Thread.sleep(1000)

        setContentView(binding.root)

        SharedPrefUtils.init(activity)

        checkStoragePermissions()

        setupBottomNavigation()

        loadDefaultFragment()
    }

    private fun loadDefaultFragment() {

        val fragmentWhatsAppStatus = FragmentStatus()

        val bundle = Bundle()

        bundle.putString(
            Constants.FRAGMENT_TYPE_KEY,
            Constants.TYPE_WHATSAPP_MAIN
        )

        replaceFragment(fragmentWhatsAppStatus, bundle)

        currentSelectedItem = R.id.menu_status

        binding.bottomNavigationView.selectedItemId = R.id.menu_status
    }

    private fun setupBottomNavigation() {

        binding.bottomNavigationView.setOnItemSelectedListener {

            if (currentSelectedItem == it.itemId) {
                return@setOnItemSelectedListener true
            }

            currentSelectedItem = it.itemId

            when (it.itemId) {

                R.id.menu_status -> {

                    val fragmentWhatsAppStatus = FragmentStatus()

                    val bundle = Bundle()

                    bundle.putString(
                        Constants.FRAGMENT_TYPE_KEY,
                        Constants.TYPE_WHATSAPP_MAIN
                    )

                    replaceFragment(fragmentWhatsAppStatus, bundle)
                }

                R.id.menu_business_status -> {

                    val fragmentWhatsAppStatus = FragmentStatus()

                    val bundle = Bundle()

                    bundle.putString(
                        Constants.FRAGMENT_TYPE_KEY,
                        Constants.TYPE_WHATSAPP_BUSINESS
                    )

                    replaceFragment(fragmentWhatsAppStatus, bundle)
                }

                R.id.nav_saved_files -> {

                    replaceFragment(FragmentSaveStatus())
                }

                R.id.menu_settings -> {

                    replaceFragment(FragmentSettings())
                }
            }

            true
        }
    }

    private fun checkStoragePermissions() {

        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

        } else {

            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        if (!EasyPermissions.hasPermissions(this, *perms)) {

            EasyPermissions.requestPermissions(
                this,
                "This app needs storage permissions to save and display statuses.",
                123,
                *perms
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    private fun hideSystemBars() {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(
            window,
            window.decorView
        )

        controller.hide(
            WindowInsetsCompat.Type.systemBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        val fragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        fragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }
}