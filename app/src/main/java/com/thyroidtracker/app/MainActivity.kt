package com.thyroidtracker.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.thyroidtracker.app.ui.ThyroidTrackerApp
import com.thyroidtracker.app.ui.theme.ThyroidTrackerTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThyroidTrackerTheme {
                ThyroidTrackerApp()
            }
        }
        requestNotificationPermissionOnFirstLaunch()
    }

    private fun requestNotificationPermissionOnFirstLaunch() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) return

        val systemPrefs = getSharedPreferences("thyroid_echo_system", MODE_PRIVATE)
        val alreadyPrompted = systemPrefs.getBoolean("notification_permission_prompted", false)
        if (alreadyPrompted) return

        systemPrefs.edit().putBoolean("notification_permission_prompted", true).apply()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
