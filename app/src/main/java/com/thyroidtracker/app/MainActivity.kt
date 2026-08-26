package com.thyroidtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.thyroidtracker.app.ui.ThyroidTrackerApp
import com.thyroidtracker.app.ui.theme.ThyroidTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThyroidTrackerTheme {
                ThyroidTrackerApp()
            }
        }
    }
}
