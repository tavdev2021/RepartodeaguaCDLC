package com.example.repartodeaguacdlc.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.common.viewmodel.AuthViewModel
import com.example.repartodeaguacdlc.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Configuración de Lottie
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.delivery_car))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // La animación se repite mientras carga
    )

    // Lógica de navegación (El "Guardián de Roles")
    LaunchedEffect(Unit) {

        val currentUser = authViewModel.currentUser.value
        val startTime = System.currentTimeMillis()

        if (currentUser == null) {
            delay(1500)
            onNavigateToLogin()
        } else {
            authViewModel.fetchUserData()
            authViewModel.userProfile.collect { profile ->
                if (profile != null) {

                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingTime = 2500 - elapsedTime

                    if (remainingTime > 0) delay(remainingTime)

                    if (profile.role == "Repartidor") {
                        onNavigateToHome()
                    } else {
                        authViewModel.logout()
                        onNavigateToLogin()
                    }
                    return@collect
                }
            }
        }
    }

    // DISEÑO DE LA PANTALLA
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Fondo con un gradiente sutil para que se vea moderno
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // La Animación Lottie
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(180.dp) // Tamaño llamativo
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre de la App con estilo
            Text(
                text = "REPARTO DE AGUA",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "CDLC",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 6.sp
            )
        }

        // Indicador de versión o eslogan en la parte inferior
        Text(
            text = "v1.0.0",
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}