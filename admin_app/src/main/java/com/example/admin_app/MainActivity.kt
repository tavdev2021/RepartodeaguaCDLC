package com.example.admin_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.admin_app.ui.theme.RepartoDeAguaCDLCTheme
import com.example.admin_app.view.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepartoDeAguaCDLCTheme {
                HomeScreen()
            }
        }
    }
}