package com.spiritual.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.spiritual.hub.ui.HomeScreen
import com.spiritual.hub.ui.theme.myAppTheme
import com.spiritual.hub.viewmodel.ChalisaViewModel
import dagger.hilt.android.AndroidEntryPoint

// Hanuman Chalisa player app main activity
// Built using Jetpack Compose + Hilt + Media3 by @engineer-aman-sharma

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Show splash while initializing app
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            myAppTheme {
                // Trigger ChalisaViewModel to initialize audio playback logic
                hiltViewModel<ChalisaViewModel>()

                // Composable that displays UI and interacts with ViewModel
                HomeScreen()
            }
        }
    }
}