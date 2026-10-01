package com.example.repartodeaguacdlc.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ClientesList(
    animatedVisibilityScope: AnimatedVisibilityScope,
    clientesViewModel: ClientesViewModel,
    onAddNewClient: () -> Unit,
    onClientClick: (String) -> Unit,
    onVentaClick: (String) -> Unit
) {

    //Contexto de la App
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Limpieza automatica al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose{
            keyboardController?.hide()
            clientesViewModel.onSearchTextChanged("")
        }
    }

    // 2. Recolecta los estados del ViewModel
    val isLoading by clientesViewModel.isLoading.collectAsStateWithLifecycle()
    val searchText by clientesViewModel.searchText.collectAsStateWithLifecycle()
    val searchResults by clientesViewModel.searchResults.collectAsStateWithLifecycle()

    // 2. Usamos Scaffold para estructurar la pantalla
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddNewClient() }, // 3. Acción al pulsar
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar nuevo cliente"
                )
            }
        }
    ) { paddingValues -> // El Scaffold nos da un padding que debemos aplicar

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchText,
                        onQueryChange = { newText ->
                            clientesViewModel.onSearchTextChanged(newText)
                        },
                        onSearch = { keyboardController?.hide() },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.searchbar_buscar_clientes)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Icono de búsqueda",
                            tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                val scanner = GmsBarcodeScanning.getClient(context)
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        clientesViewModel.startQRScanner(barcode.rawValue)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.qr_code),
                                    contentDescription = "Icono Qr scan",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Aquí puedes poner sugerencias si lo deseas en el futuro
            }

            if (isLoading) {
                // A) MIENTRAS CARGA ROOM (Solo unos milisegundos):
                // Mostramos un indicador de carga centrado o la vista en espera sin la pantalla de "Vacío"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (searchResults.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clientes en Ruta ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${searchResults.size}",
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

            if (searchResults.isEmpty()) {


                // Caso A: Estado vacio (No hay clientes o al busqueda dio 0 resultados)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Icono dinamico segun la situacion
                        Icon(
                            painter = painterResource(
                                id = if (searchText.isBlank()) R.drawable.lista_clientes else R.drawable.ic_error
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Mensaje dinamico segun la situacion
                        Text(
                            text = if (searchText.isBlank())
                                "No tienes clientes en esta ruta"
                            else
                                "No se encontraron coincidencias de busqueda",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (searchText.isBlank())
                                "Presiona el boton '+' en la esquina inferior para registrar el primer cliente."
                            else
                                "No se encontraron clientes que coincidan con \"$searchText\". Intenta con otro nombre.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {

                // Caso B: Lista de clientes (Cuando si hay clientes para mostrar)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Ocupa todo el espacio disponible
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 80.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 4. Itera sobre los resultados de búsqueda del ViewModel
                    items(searchResults) { cliente ->
                        Card(
                            modifier = Modifier
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "card-${cliente.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                                .fillMaxWidth()
                                .padding(vertical = 4.dp) // Añade un poco de espacio entre las tarjetas
                                .clickable {

                                    // Acción al hacer clic en la tarjeta, por ejemplo, navegar a los detalles del cliente

                                    // 1. Ocultar el teclado por precaucion
                                    keyboardController?.hide()

                                    // 2. Limpiar el texto de búsqueda en el ViewModel
                                    clientesViewModel.onSearchTextChanged("")

                                    // 3. Navegar a los detalles del cliente
                                    onClientClick(cliente.id) // Llama a la función de clic del cliente
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Añade una sombra sutil
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface, // Usa un color del tema
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // Asumiendo que tu objeto `cliente` tiene una propiedad `imagenUrl` con la URL de la imagen.
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(cliente.imagenUrl) // Reemplaza con la URL de la imagen del cliente
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .placeholder(R.drawable.ic_downloading) // Icono de placeholder mientras carga
                                        .error(R.drawable.ic_error) // Icono si hay error de carga
                                        .build(),
                                    contentDescription = "Imagen del Cliente",
                                    modifier = Modifier
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "image-${cliente.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        )
                                        .size(40.dp)
                                        .clip(CircleShape) // Hace la imagen circular
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.secondary,
                                            CircleShape
                                        ),// Pone un borde en la imagen
                                    contentScale = ContentScale.Crop, // Escala la imagen para llenar el espacio
                                )
                                // --- FIN DEL CAMBIO ---

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f) // Ocupa el espacio restante
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .sharedElement(
                                                sharedContentState = rememberSharedContentState(key = "nombre-${cliente.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope
                                            ),
                                        text = cliente.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    // Suponiendo que tu objeto 'cliente' tiene una propiedad 'direccion'
                                    Text(
                                        text = cliente.direccion.ifBlank { cliente.ubicacion },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )

                                    // Suponiendo que tu objeto 'cliente' tiene una propiedad 'telefono'
                                    if (cliente.telefono.isNotBlank()) {
                                        Text(
                                            text = cliente.telefono,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                // Ejemplo de icono adicional a la derecha
                                Button(
                                    onClick = {
                                        // Navegacion directa a la venta
                                        keyboardController?.hide()
                                        clientesViewModel.onSearchTextChanged("")
                                        onVentaClick(cliente.id)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.add_shopping_cart),
                                        contentDescription = "Agregar venta",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Vender",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // FIN DE LA CARD FORMATEADA
                    }
                }
            }
        }
    }
}