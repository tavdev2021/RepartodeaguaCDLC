package com.example.repartodeaguacdlc.view

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
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
import androidx.compose.runtime.produceState
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
import coil.compose.AsyncImage
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.model.Clientes
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetail(clienteId: Int,
                 clientesViewModel: ClientesViewModel,
                 onNavigateToEdit: (Int) -> Unit,
                 onBack: () -> Unit) {
    val isLoading by clientesViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // 1. Buscamos el cliente en la base de datos usando el ID
    // Usamos produceState o collectAsState para observar el resultado
    val cliente by produceState<Clientes?>(initialValue = null, clienteId) {
        value = clientesViewModel.getClienteById(id = clienteId)
    }

    // remember nos ayuda a mantener la misma instancia del Intent a través de las recomposiciones
    val mapIntent = remember(cliente?.ubicacion) {
        // Asegúrate de que el cliente no es nulo y la ubicación tampoco
        cliente?.ubicacion?.let { ubicacion ->
            // Crea el Uri para el intent del mapa. La 'q' es para la query y 'z' para el zoom.
            val gmmIntentUri = "geo:0,0?q=$ubicacion&z=15".toUri()
            Intent(Intent.ACTION_VIEW, gmmIntentUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = cliente?.nombre ?: stringResource(R.string.appbar_title_details)) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) { // <--- AQUÍ SE USA onBack
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            if (cliente == null) {

                // Overlay con blur elegante
                LoadingOverlay(
                    visible = isLoading,
                    message = stringResource(R.string.loading_cargar_cliente)
                )
            } else {
                // 2. Diseño del contenido del detalle
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ejemplo de visualización de datos
                    Text(text = "Información del Cliente", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    AsyncImage(
                        model = cliente!!.imagenUrl,
                        contentDescription = "Imagen del Cliente",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)// Hace la imagen circular
                            .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),// Pone un borde en la imagen
                        contentScale = ContentScale.Crop, // Escala la imagen para llenar el espacio
                        placeholder = painterResource(id = R.drawable.ic_downloading), // Icono de placeholder mientras carga
                        error = painterResource(id = R.drawable.ic_error) // Icono si hay error de carga
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        onNavigateToEdit(clienteId)
                    },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(50.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Editar")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                Toast.makeText(context,"Llamar", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Llamar")
                            }
                            Text(text = "Llamar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                Toast.makeText(context,"Mensaje", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = "Mensaje")
                            }
                            Text(text = "Mensaje")

                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {

                                if (mapIntent != null) {
                                    // Comprueba si hay una app que pueda manejar el intent
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        // Si no se encuentra Google Maps, muestra un mensaje
                                        Toast.makeText(context, "No se encontró una aplicación de mapas.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Ubicación no disponible.", Toast.LENGTH_SHORT).show()
                                }

                            }) {
                                Icon(imageVector = Icons.Default.Place, contentDescription = "Mapa")
                            }
                            Text(text = "Mapa")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                Toast.makeText(context,"Venta", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Venta")
                            }
                                Text(text = "Venta")
                            }
                        }

                    DetailColumn(icon = Icons.Default.Phone, label = "Teléfono:", value = cliente!!.telefono)
                    DetailColumn(icon = Icons.Default.Place, label = "Ubicación:", value = cliente!!.ubicacion)
                    DetailColumn(icon = Icons.Default.Create,label = "Notas:", value = cliente!!.notas)
                }
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