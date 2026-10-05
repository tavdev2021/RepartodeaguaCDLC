package com.example.repartodeaguacdlc.view

import android.icu.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.common.model.VentaConDatos
import com.example.common.view.LoadingOverlay
import com.example.repartodeaguacdlc.R
import com.example.common.viewmodel.AuthViewModel
import com.example.repartodeaguacdlc.data.AppDatabase
import com.example.repartodeaguacdlc.viewmodel.VentasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Locale



@Composable
fun HomeScreen(viewModel: AuthViewModel,
               ventasViewModel: VentasViewModel,
               onAddNewClient:() -> Unit,
               onPedidos:() -> Unit,
               onClientes:() -> Unit,
               onSettings:() -> Unit,
               onLogout: () -> Unit) {

    //val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    // Obtén la referencia al productosViewModel
    val ventasHoy by ventasViewModel.ventasHoyCount.collectAsStateWithLifecycle()
    val ingresosHoy by ventasViewModel.ingresosHoy.collectAsStateWithLifecycle()
    val ultimaVenta by ventasViewModel.ultimaVenta.collectAsStateWithLifecycle()
    val rutasDisponibles by viewModel.availableRoutes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is AuthViewModel.AuthEvent.NavigateToLogin) {
                onLogout() // Esta es la función que viene del NavHost
            }
        }
    }

    val context = LocalContext.current


    //Obtener el dia y fecha del sistema
    val calendar = Calendar.getInstance().time
    val dateFormat = DateFormat.getDateInstance(DateFormat.FULL).format(calendar)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        user?.let { firebaseUser ->


            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.15f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, start = 10.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //Foto de perfil
                        val imageUrl = firebaseUser.photoUrl
                        if (imageUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .placeholder(R.drawable.ic_downloading)
                                    .error(R.drawable.ic_error)
                                    .build(),
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.secondary,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop,

                                )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .size(50.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }

                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center

                        ) {

                            Text(
                                text = (stringResource(R.string.name_hello) + " " +
                                        (firebaseUser.displayName?.split(" ")?.firstOrNull()
                                            ?: "Usuario")),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(start = 8.dp)
                            )

                            val nombreRuta = remember(userProfile, rutasDisponibles) {
                                val id = userProfile?.routeId
                                if (id != null) {
                                    // Buscamos la ruta cuyo ID coincida con el ID del usuario
                                    rutasDisponibles.find { it.id == id }?.nombre
                                        ?: "Sin ruta asignada"
                                } else {
                                    "Cargando ruta..."
                                }
                            }

                            Text(
                                "Ruta: $nombreRuta",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.padding(start = 8.dp, top = 0.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))


                        Icon(
                            painter = painterResource(id = R.drawable.logout_icon),
                            contentDescription = "Logout Icon",
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 16.dp)
                                .clickable(onClick = {
                                    viewModel.logout(
                                        onClearLocalStorage = {
                                            withContext(Dispatchers.IO) {
                                                AppDatabase.getDatabase(context).clearAllTables()
                                            }
                                        }
                                    )
                                }),
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {

                    Text(
                        "$dateFormat",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    //Implementacion de Cards para datos rapidos

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(120.dp)
                                .clickable(onClick = { }),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.water_drop),
                                    contentDescription = "Water Drop Icon",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 8.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    "Ventas de hoy",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall

                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    ventasHoy.toString(),
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold

                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(120.dp)
                                .clickable(onClick = { }),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.money_range),
                                    contentDescription = "Money Icon",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 8.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    "Ingresos de hoy",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall

                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${
                                        String.format(
                                            Locale.getDefault(),
                                            "%.2f",
                                            ingresosHoy
                                        )
                                    }",
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold

                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        Column {
                            Text(
                                text = "Acciones Rápidas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(80.dp)
                                    .clickable { onClientes() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.lista_clientes),
                                        contentDescription = "Clientes",
                                        modifier = Modifier.size(40.dp)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Clientes en Ruta",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Lista de clientes y ventas directas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    }

                                    Icon(
                                        painter = painterResource(id = R.drawable.chevron_forward),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Fila 1: Clientes y Nuevo Cliente (Las 2 más importantes)
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AccionCardGrid(
                                    titulo = "Nuevo Cliente",
                                    subtitulo = "Registrar casa",
                                    icono = R.drawable.nuevo_cliente,
                                    onClick = onAddNewClient,
                                    modifier = Modifier.weight(1f)
                                )

                                AccionCardGrid(
                                    titulo = "Inventario",
                                    subtitulo = "Garrafones camión",
                                    icono = R.drawable.inventario,
                                    onClick = onSettings,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Fila 2: Corte de Caja e Inventario
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AccionCardGrid(
                                    titulo = "Corte de Caja",
                                    subtitulo = "Efectivo del día",
                                    icono = R.drawable.corte_caja,
                                    onClick = onPedidos,
                                    modifier = Modifier.weight(1f)
                                )
                                AccionCardGrid(
                                    titulo = "Ruta y Ajustes",
                                    subtitulo = "Configuración",
                                    icono = R.drawable.ruta,
                                    onClick = onSettings,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Última Venta Realizada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val ultimaVenta = ultimaVenta.firstOrNull()

                            if (ultimaVenta == null) {
                                Text(
                                    text = "No has registrado ventas aun",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                VentaCard(venta = ultimaVenta)
                            }
                        }
                    }
                }
            }
        }
    }

    // Overlay con blur elegante
    LoadingOverlay(
        visible = isLoading,
        message = stringResource(R.string.loading_cerrar_sesion)
    )
}

@Composable
private fun AccionCardGrid(
    titulo: String,
    subtitulo: String,
    icono: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icono),
                contentDescription = titulo,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}



@Composable
fun VentaCard(venta: VentaConDatos, modifier: Modifier = Modifier) {

    //Formatear la hora de la venta
    val horaFormateada = remember(venta.fecha) {
        java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(java.util.Date(venta.fecha))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = venta.nombreCliente, // 👈 Ahora mostramos el nombre
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metodo de pago
                    Surface(
                        shape = CircleShape,
                        color = when (venta.metodoPago) {
                            "Efectivo" -> MaterialTheme.colorScheme.primaryContainer
                            "Tarjeta" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ) {
                        Text(
                            text = venta.metodoPago,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (venta.metodoPago) {
                                "Efectivo" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Tarjeta" -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        painter = painterResource(
                            id = if (venta.isSynced) R.drawable.outline_cloud_done
                            else R.drawable.outline_cloud_off
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (venta.isSynced) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            //Fila inferior: Detalles de producto, hora de venta y total $

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                    Text(
                        text = "${venta.totalProductos} productos • $horaFormateada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )


                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", venta.total)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}