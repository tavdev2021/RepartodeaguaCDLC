package com.example.admin_app.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.common.viewmodel.AuthViewModel
import com.example.admin_app.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.animation.core.animateFloatAsState // Para animar la opacidad
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer // Para aplicar la opacidad de forma eficiente

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Configuración de Lottie
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.admin_panel))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // La animación se repite mientras carga
    )

    var startAnimation by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), // Aparece en 1 segundo
        label = "FadeIn"
    )

    val animatedBottomColor by animateColorAsState(
        targetValue = if (startAnimation) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 1200),
        label = "GradientAnim"
    )

    // Lógica de navegación (El "Guardián de Roles")
    LaunchedEffect(Unit) {

        startAnimation = true

        val currentUser = authViewModel.currentUser.value
        val startTime = System.currentTimeMillis()

        if (currentUser == null) {
            delay(2000.milliseconds)
            onNavigateToLogin()
        } else {
            authViewModel.fetchUserData()
            authViewModel.userProfile.collect { profile ->
                if (profile != null) {

                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingTime = 3000 - elapsedTime

                    if (remainingTime > 0) delay(remainingTime.milliseconds)

                    if (profile.role == "Administrador") {
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
                        animatedBottomColor
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer(alpha = contentAlpha),
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
                text = "ADMINISTRADOR",
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