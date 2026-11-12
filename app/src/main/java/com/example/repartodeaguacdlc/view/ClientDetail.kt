package com.example.repartodeaguacdlc.view

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.common.model.VentaConDatos
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.example.repartodeaguacdlc.viewmodel.VentasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ClientDetail(
    animatedVisibilityScope: AnimatedVisibilityScope,
    clientesViewModel: ClientesViewModel,
    ventasViewModel: VentasViewModel,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToVenta: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cliente by clientesViewModel.selectedClient.collectAsStateWithLifecycle()
    val isLoading by clientesViewModel.isLoading.collectAsStateWithLifecycle()

    // Intents
    // 🟢 MAP INTENT CORREGIDO: Prioriza siempre las Coordenadas GPS exactas
    val mapIntent = remember(cliente?.ubicacion, cliente?.direccion, cliente?.nombre) {
        // 1. Prioridad: Coordenadas GPS exactas del sensor (ubicacion)
        val gpsCoords = cliente?.ubicacion?.trim()
        val addressText = cliente?.direccion?.trim()
        val clientName = java.net.URLEncoder.encode(cliente?.nombre ?: "Cliente", "UTF-8")

        when {
            !gpsCoords.isNullOrBlank() -> {
                // Google Maps abre las coordenadas GPS exactas con la etiqueta del nombre del cliente
                Intent(Intent.ACTION_VIEW, "geo:0,0?q=$gpsCoords($clientName)&z=17".toUri())
            }

            !addressText.isNullOrBlank() -> {
                // Respaldo: Si no hay coordenadas GPS, busca por dirección en texto
                Intent(Intent.ACTION_VIEW, "geo:0,0?q=$addressText&z=17".toUri())
            }

            else -> null
        }
    }

    val callIntent = remember(cliente?.telefono) {
        if (!cliente?.telefono.isNullOrBlank()) Intent(
            Intent.ACTION_DIAL,
            "tel:${cliente?.telefono}".toUri()
        ) else null
    }

    val messageIntent = remember(cliente?.telefono) {
        if (!cliente?.telefono.isNullOrBlank()) Intent(
            Intent.ACTION_SENDTO,
            "smsto:${cliente?.telefono}".toUri()
        ) else null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detalle del Cliente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    cliente?.let {
                        IconButton(onClick = { onNavigateToEdit(it.id) }) {
                            Icon(
                                Icons.TwoTone.EditNote,
                                "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (cliente != null) {
                val currentCliente = cliente!!

                // Observamos las últimas 3 ventas del cliente en tiempo real
                val ultimasVentas by ventasViewModel.getUltimasVentasPorCliente(currentCliente.id)
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    // --- 1. HEADER (Foto, Nombre, Dirección) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Surface para darle elevación/sombra real a la foto
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 6.dp, // 👈 Eleva la foto del fondo
                            color = MaterialTheme.colorScheme.surface
                        ) {

                            AsyncImage(
                                model = currentCliente.imagenUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "image-${currentCliente.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(
                                        3.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_downloading),
                                error = painterResource(id = R.drawable.ic_error)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentCliente.nombre,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "nombre-${currentCliente.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        Text(
                            text = currentCliente.direccion.ifBlank { currentCliente.ubicacion },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // --- 2. TARJETA DE RESUMEN FINANCIERO (Deuda y Saldo a Favor) ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ), // 👈 Borde sutil elegante
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Saldo Pendiente
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "DEUDA PENDIENTE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${
                                        String.format(
                                            Locale.getDefault(),
                                            "%.2f",
                                            currentCliente.saldoPendiente
                                        )
                                    }",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentCliente.saldoPendiente > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier.height(32.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Saldo a Favor
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "SALDO A FAVOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${
                                        String.format(
                                            Locale.getDefault(),
                                            "%.2f",
                                            currentCliente.saldoAFavor
                                        )
                                    }",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentCliente.saldoAFavor > 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // --- 3. ACCIONES RÁPIDAS (Llamar, Venta, Mapa, Mensaje en 1 sola fila) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Call,
                            label = "Llamar",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            callIntent?.let { context.startActivity(it) } ?: Toast.makeText(
                                context,
                                "Sin teléfono registrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        QuickActionButton(
                            icon = Icons.Default.ShoppingCart,
                            label = "Venta",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            onNavigateToVenta(currentCliente.id)
                        }

                        QuickActionButton(
                            icon = Icons.Default.Place,
                            label = "Mapa",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            mapIntent?.let { context.startActivity(it) } ?: Toast.makeText(
                                context,
                                "Sin ubicación registrada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        QuickActionButton(
                            icon = Icons.Default.Sms,
                            label = "Mensaje",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            messageIntent?.let { context.startActivity(it) } ?: Toast.makeText(
                                context,
                                "Sin teléfono registrado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // --- 4. INSTRUCCIONES / NOTAS DEL CLIENTE (Solo si existen) ---
                    if (currentCliente.notas.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NoteAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Notas de Entrega",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currentCliente.notas,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // --- 5. 🔴 NUEVA SECCIÓN: ÚLTIMAS 3 COMPRAS DEL CLIENTE ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Últimas Compras",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (ultimasVentas.isNotEmpty()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${ultimasVentas.size}",
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 2.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (ultimasVentas.isEmpty()) {
                                Text(
                                    text = "Este cliente aún no ha registrado compras.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                ultimasVentas.forEachIndexed { index, venta ->
                                    VentaHistorialRow(venta = venta)
                                    if (index < ultimasVentas.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fila que representa una compra en el historial del cliente.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun VentaHistorialRow(venta: VentaConDatos) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault()) }
    val fechaFormateada = remember(venta.fecha) { dateFormat.format(Date(venta.fecha)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = fechaFormateada,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${venta.totalProductos} ${if (venta.totalProductos == 1) "artículo" else "artículos"} • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = if (venta.metodoPago == "Efectivo") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = venta.metodoPago,
                        color = if (venta.metodoPago == "Efectivo") Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Text(
            text = "$${String.format(Locale.getDefault(), "%.2f", venta.total)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

        @Composable
        fun QuickActionButton(
            icon: ImageVector,
            label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 3.dp, // 👈 Elevación para dar tridimensionalidad
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.15f)) // 👈 Borde sutil del mismo tono
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}