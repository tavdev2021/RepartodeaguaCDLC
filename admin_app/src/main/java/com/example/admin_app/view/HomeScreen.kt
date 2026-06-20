package com.example.admin_app.view

import android.icu.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.common.viewmodel.AuthViewModel
import com.example.admin_app.R
import com.example.admin_app.model.AccionRapida
import com.example.common.view.LoadingOverlay
import java.text.DateFormat

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.navigationEvent.collect { event ->
            if (event is AuthViewModel.AuthEvent.NavigateToLogin) {
                onLogout()
            }
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance().time
    val dateFormat = DateFormat.getDateInstance(DateFormat.FULL).format(calendar)

    val acciones = listOf(
        AccionRapida(R.drawable.nuevo_cliente, "Nuevo Cliente"),
        AccionRapida(R.drawable.lista_clientes, "Clientes"),
        AccionRapida(R.drawable.corte_caja, "Corte de caja"),
        AccionRapida(R.drawable.inventario, "Inventario"),
        AccionRapida(R.drawable.ruta, "Ruta")
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()) {
        user?.let { firebaseUser ->
            Column(modifier = Modifier.fillMaxSize()) {

                // --- HEADER (Tu diseño original refinado) ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.15f),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Foto de perfil
                        ProfileImage(url = firebaseUser.photoUrl.toString())

                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Hola, ${firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "Admin"}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = userProfile?.role ?: "Administrador",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { authViewModel.logout() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.logout_icon),
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // --- CUERPO DEL DASHBOARD ---
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Text(dateFormat, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    }

                    // 1. GRID DE INDICADORES (KPIs)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Ventas Hoy",
                                value = "42",
                                trend = "+15% vs ayer",
                                isPositive = true,
                                icon = R.drawable.water_drop
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Ingresos",
                                value = "$3,450",
                                trend = "-5% vs ayer",
                                isPositive = false,
                                icon = R.drawable.money_range
                            )
                        }
                    }

                    // 2. SECCIÓN: AVANCE DE RUTAS
                    item {
                        Column {
                            Text("Avance de Rutas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            RouteProgressItem("Arroyo Grande - Centro", 0.75f, "75/100 Entregas")
                            Spacer(Modifier.height(8.dp))
                            RouteProgressItem("Arroyo Grande - Sur", 0.30f, "6/20 Entregas")
                            Spacer(Modifier.height(8.dp))
                            RouteProgressItem("La Laja - El Timbinal", 0.80f, "64/80 Entregas")
                            Spacer(Modifier.height(8.dp))
                            RouteProgressItem("La Cienega", 0.25f, "10/40 Entregas")
                            Spacer(Modifier.height(8.dp))
                            RouteProgressItem("La Cañada - El pinzan", 0.50f, "10/20 Entregas")
                        }
                    }

                    // 3. ACCIONES RÁPIDAS (LazyRow original)
                    item {
                        Column {
                            Text("Gestión Rápida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(acciones) { accion ->
                                    QuickActionCard(accion)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- EL LOADING OVERLAY (Aquí está de vuelta) ---
        LoadingOverlay(
            visible = isLoading,
            message = stringResource(R.string.loading_cerrar_sesion)
        )
    }
}

// --- SUB-COMPONENTES PARA ORDENAR EL CÓDIGO ---

@Composable
fun ProfileImage(url: String?) {
    Box(modifier = Modifier.size(60.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape)) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(R.drawable.ic_error)
        )
    }
}

@Composable
fun MetricCard(modifier: Modifier, title: String, value: String, trend: String, isPositive: Boolean, icon: Int) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painterResource(icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Text(trend, style = MaterialTheme.typography.labelSmall, color = if(isPositive) Color(0xFF4CAF50) else Color.Red)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}

@Composable
fun RouteProgressItem(name: String, progress: Float, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(accion: AccionRapida) {
    Card(
        modifier = Modifier.size(110.dp).clickable { /* Acción */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Image(painterResource(accion.icon), null, Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(accion.text, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}