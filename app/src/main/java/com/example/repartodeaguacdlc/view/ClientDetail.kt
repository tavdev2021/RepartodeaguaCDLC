package com.example.repartodeaguacdlc.view

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.Edit
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
import coil.compose.AsyncImage
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ClientDetail(
    animatedVisibilityScope: AnimatedVisibilityScope,
    clientesViewModel: ClientesViewModel,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToVenta: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cliente by clientesViewModel.selectedClient.collectAsState()
    val isLoading by clientesViewModel.isLoading.collectAsState()

    // Intents
    val mapIntent = remember(cliente?.ubicacion) {
        cliente?.ubicacion?.let { Intent(Intent.ACTION_VIEW, "geo:0,0?q=$it&z=15".toUri()) }
    }
    val callIntent = remember(cliente?.telefono) {
        cliente?.telefono?.let { Intent(Intent.ACTION_DIAL, "tel:$it".toUri()) }
    }
    val messageIntent = remember(cliente?.telefono) {
        cliente?.telefono?.let { Intent(Intent.ACTION_SENDTO, "smsto:$it".toUri()) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del Cliente", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    cliente?.let {
                        IconButton(onClick = { onNavigateToEdit(it.id) }) {
                            Icon(Icons.TwoTone.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (cliente != null) {
                val currentCliente = cliente!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- HEADER SECCION ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = currentCliente.imagenUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "image-${currentCliente.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                                .size(140.dp)
                                .clip(CircleShape)
                                .border(
                                    4.dp,
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_downloading),
                            error = painterResource(id = R.drawable.ic_error)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentCliente.nombre,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "nombre-${currentCliente.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        Text(
                            text = "ID: ${currentCliente.id.take(8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // --- ACCIONES RAPIDAS (Tiles) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Phone,
                            label = "Llamar",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            callIntent?.let { context.startActivity(it) } ?: Toast.makeText(context, "No disponible", Toast.LENGTH_SHORT).show()
                        }
                        QuickActionButton(
                            icon = Icons.Default.ShoppingCart,
                            label = "Venta",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            onNavigateToVenta(currentCliente.id)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Place,
                            label = "Mapa",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            mapIntent?.let { context.startActivity(it) } ?: Toast.makeText(context, "Sin ubicación", Toast.LENGTH_SHORT).show()
                        }
                        QuickActionButton(
                            icon = Icons.Default.Email,
                            label = "Mensaje",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            messageIntent?.let { context.startActivity(it) }
                        }
                    }

                    // --- SECCION DETALLES ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "card-${currentCliente.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Información de contacto",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            DetailRow(Icons.Default.Phone, "Teléfono", currentCliente.telefono)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                            DetailRow(Icons.Default.Place, "Ubicación", currentCliente.ubicacion)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                            DetailRow(Icons.Default.Create, "Notas adicionales", currentCliente.notas)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value.ifBlank { "Sin especificar" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}