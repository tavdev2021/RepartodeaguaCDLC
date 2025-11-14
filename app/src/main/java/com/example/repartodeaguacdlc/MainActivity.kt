package com.example.repartodeaguacdlc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.repartodeaguacdlc.navigation.AppNavHost
import com.example.repartodeaguacdlc.ui.theme.RepartoDeAguaCDLCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {

            RepartoDeAguaCDLCTheme {

                AppNavHost()
            }
        }
    }
}