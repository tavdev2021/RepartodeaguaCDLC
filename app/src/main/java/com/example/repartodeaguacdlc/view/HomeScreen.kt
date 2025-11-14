package com.example.repartodeaguacdlc.view

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.model.AccionRapida
import com.example.repartodeaguacdlc.viewmodel.AuthViewModel
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AuthViewModel,
               clientesViewModel: ClientesViewModel,
               onAddNewClient:() -> Unit,
               onPedidos:() -> Unit,
               onClientes:() -> Unit,
               onSettings:() -> Unit,
               onLogout: () -> Unit) {

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val clientes by clientesViewModel.clientes.collectAsState()


    //Contexto de la App
    val context = LocalContext.current
    val messageqr = stringResource(R.string.funcion_qr_searchbar)

    //Estados de SearchBar sin ViewModel
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    // Lista de ejemplo para la búsqueda
    val searchResults = remember(query, clientes) {
        if (query.isBlank()) {
            clientes
        } else {
            clientes.filter { it.nombre.contains(query, ignoreCase = true) }
        }
    }
    val acciones = listOf(
        AccionRapida(Icons.Default.Add, "Nuevo Cliente",onAddNewClient),
        AccionRapida(Icons.AutoMirrored.Filled.List, "Ver Pedidos", onPedidos),
        AccionRapida(Icons.Default.Person, "Lista de clientes", onClientes),
        AccionRapida(Icons.Default.Settings, "Ajustes", onSettings)
    )

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) onLogout()
    }

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
                        .fillMaxHeight(0.2f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                stringResource(R.string.name_hello),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(start = 16.dp)
                            )

                            Text(
                                firebaseUser.displayName?.split(" ")
                                    ?.firstOrNull()
                                    ?: "No disponible",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        //Foto de perfil
                        //val imageUrl = firebaseUser.photoUrl
                        val imageUrl = "https://i.pravatar.cc/300"
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(60.dp)
                                    .clip(CircleShape)
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
                                    .size(100.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-50).dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {

                    // --- Implementación del SearchBar ---
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = {
                                // Aquí puedes manejar la acción de búsqueda (ej. navegar a otra pantalla)
                                active = false
                            },
                            active = active,
                            onActiveChange = { active = it },
                            placeholder = { Text(stringResource(R.string.searchbar_buscar_clientes)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Icono de búsqueda"
                                )
                            },
                            trailingIcon = {

                                IconButton(onClick = {
                                    Toast.makeText(context, messageqr, Toast.LENGTH_SHORT).show() })
                                {
                                    Icon(
                                        painter = painterResource(id = R.drawable.qr_code),
                                        contentDescription = "Icono Qr scan"
                                    )
                                }
                            }
                        ) {
                            // Contenido que se muestra cuando el SearchBar está activo
                            LazyColumn {
                                items(searchResults) { cliente ->
                                    ListItem(
                                        headlineContent = { Text(cliente.nombre)},
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .clickable(
                                                // Navegar a la pantalla de detalles del cliente
                                                onClick = {
                                                    Toast.makeText(
                                                        context,
                                                        "Cliente seleccionado: ${cliente.nombre}",
                                                        Toast.LENGTH_SHORT)
                                                        .show()
                                                }
                                            ),
                                        leadingContent = {
                                            Icon(
                                                Icons.TwoTone.AccountCircle,
                                                contentDescription = "Icono de cliente"
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                               Icons.TwoTone.Star,
                                                contentDescription = "Icono de favorito"

                                            )
                                        },
                                        tonalElevation = 4.dp,
                                        shadowElevation = 4.dp
                                    )
                                }
                            }
                        }

                        // --- (NUEVO) IMPLEMENTACIÓN DE LAZYROW CON CARDS ---
                        Column {
                            Text(
                                text = "Acciones Rápidas",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(acciones) { accion ->
                                    Card(
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(130.dp)
                                            .clickable(onClick = accion.action),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = accion.icon,
                                                contentDescription = accion.text,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = accion.text,
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodySmall

                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // --- FIN DE LA IMPLEMENTACIÓN DE LAZYROW ---


                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {

                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-25).dp)
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(horizontal = 8.dp)
                                    .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(stringResource(R.string.button_logout), fontSize = 16.sp)
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