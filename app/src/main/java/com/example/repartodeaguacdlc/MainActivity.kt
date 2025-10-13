package com.example.repartodeaguacdlc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.repartodeaguacdlc.navigation.AppNavHost
import com.example.repartodeaguacdlc.ui.theme.RepartoDeAguaCDLCTheme
import com.example.repartodeaguacdlc.view.HomeScreenNew

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {

            RepartoDeAguaCDLCTheme {

                AppNavHost()
                /*Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondary
                ){
                    AppNavHost()
                }*/
            }
        }
    }
}