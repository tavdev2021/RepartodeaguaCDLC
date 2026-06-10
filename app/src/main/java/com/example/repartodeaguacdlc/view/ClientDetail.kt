package com.example.repartodeaguacdlc.view

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onBack: () -> Unit) {

    val context = LocalContext.current
    val cliente by clientesViewModel.selectedClient.collectAsState()

    val mapIntent = remember(cliente?.ubicacion) {
        cliente?.ubicacion?.let { ubicacion ->
            val gmmIntentUri = "geo:0,0?q=$ubicacion&z=15".toUri()
            Intent(Intent.ACTION_VIEW, gmmIntentUri)
        }
    }

    val callIntent = remember(cliente?.telefono) {
        cliente?.telefono?.let { telefono ->
            val callUri = "tel:$telefono"
            Intent(Intent.ACTION_DIAL, callUri.toUri())
        }
    }

    val messageIntent = remember(cliente?.telefono) {
        cliente?.telefono?.let { telefono ->
            val messageUri = "smsto:$telefono"
            Intent(Intent.ACTION_SENDTO, messageUri.toUri())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "nombre-${cliente?.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        text = cliente?.nombre ?: stringResource(R.string.appbar_title_details)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) { // <--- AQUÍ SE USA onBack
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(cliente!!.id) }) {
                        Icon(
                            imageVector = Icons.TwoTone.Edit,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {


            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "card-${cliente?.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                )
            ) {
                // 2. Diseño del contenido del detalle
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ElevatedButton(
                                onClick = {
                                    if (callIntent != null) {
                                        if (callIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(callIntent)
                                        } else {
                                            Toast.makeText(context, "No se encontro una aplicacion para llamar", Toast.LENGTH_SHORT).show()
                                            // Si no se encuentra ninguna app para llamar, muestra un mensaje")
                                        }
                                    } else {
                                        Toast.makeText(context, "Número de teléfono no disponible.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 2.dp,
                                    disabledElevation = 0.dp
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Llamar"
                                )
                            }
                            Text(text = "Llamar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ElevatedButton(
                                onClick = {
                                    if (messageIntent != null) {
                                        if (messageIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(messageIntent)
                                        } else {
                                            Toast.makeText(context, "No se encontro una aplicacion para enviar mensajes", Toast.LENGTH_SHORT).show()
                                            // Si no se encuentra ninguna app para llamar, muestra un mensaje")
                                        }
                                    } else {
                                        Toast.makeText(context, "Número de teléfono no disponible.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 2.dp,
                                    disabledElevation = 0.dp
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Mensaje"
                                )
                            }
                            Text(text = "Mensaje")

                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ElevatedButton(
                                onClick = {

                                    if (mapIntent != null) {
                                        // Comprueba si hay una app que pueda manejar el intent
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        } else {
                                            // Si no se encuentra Google Maps, muestra un mensaje
                                            Toast.makeText(
                                                context,
                                                "No se encontró una aplicación de mapas.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Ubicación no disponible.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                },
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 2.dp,
                                    disabledElevation = 0.dp
                                ),
                            ) {
                                Icon(imageVector = Icons.Default.Place, contentDescription = "Mapa")
                            }
                            Text(text = "Mapa")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ElevatedButton(
                                onClick = {
                                    cliente?.let { onNavigateToVenta(it.id) }
                                },
                                elevation = ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 2.dp,
                                    disabledElevation = 0.dp
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Venta"
                                )
                            }
                            Text(text = "Venta")
                        }
                    }

                    DetailColumn(
                        icon = Icons.Default.Phone,
                        label = "Teléfono:",
                        value = cliente!!.telefono
                    )
                    DetailColumn(
                        icon = Icons.Default.Place,
                        label = "Ubicación:",
                        value = cliente!!.ubicacion
                    )
                    DetailColumn(
                        icon = Icons.Default.Create,
                        label = "Notas:",
                        value = cliente!!.notas
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AsyncImage(
                    model = cliente?.imagenUrl,
                    contentDescription = "Imagen del Cliente",
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "image-${cliente?.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .size(100.dp)
                        .clip(CircleShape)// Hace la imagen circular
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.secondary,
                            CircleShape
                        ),// Pone un borde en la imagen
                    contentScale = ContentScale.Crop, // Escala la imagen para llenar el espacio
                    placeholder = painterResource(id = R.drawable.ic_downloading), // Icono de placeholder mientras carga
                    error = painterResource(id = R.drawable.ic_error) // Icono si hay error de carga
                )
            }

        }
    }
}

@Composable
fun DetailColumn(icon: ImageVector, label: String, value: String) {

    Row(modifier = Modifier
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
