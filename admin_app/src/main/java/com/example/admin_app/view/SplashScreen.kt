package com.example.admin_app.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.common.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // 👇 Efecto para navegar apenas se sepa el estado
        LaunchedEffect(isAuthenticated) {
            delay(2500)
            if (isAuthenticated) {
                onNavigateToHome()
            } else {
                onNavigateToLogin()
            }
        }


        // UI del splash

        // Overlay con blur elegante
        LoadingOverlay(
            visible = true,
            message = "Cargando..."
        )
    }
}