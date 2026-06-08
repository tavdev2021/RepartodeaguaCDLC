package com.example.repartodeaguacdlc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.repartodeaguacdlc.navigation.AppNavHost
import com.example.common.ui.theme.RepartoDeAguaCDLCTheme
import com.example.common.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.isLoading.value
        }

        setContent {

            RepartoDeAguaCDLCTheme {
                AppNavHost()
            }
        }
    }
}