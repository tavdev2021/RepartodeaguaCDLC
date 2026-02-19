package com.example.repartodeaguacdlc.view
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.data.ClientesViewModelFactory
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesList(
    onAddNewClient: () -> Unit,
    onClientClick: (Int) -> Unit
) {

    //Contexto de la App
    val context = LocalContext.current

    // 1. Instanciar el ViewModel usando el Factory
    val clientesViewModel: ClientesViewModel = viewModel(
        factory = ClientesViewModelFactory(context)
    )

    // 2. Recolecta los estados del ViewModel
    val searchText by clientesViewModel.searchText.collectAsState()
    val searchResults by clientesViewModel.searchResults.collectAsStateWithLifecycle()

    //Estados de SearchBar sin ViewModel
    //var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

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
                //.padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                query = searchText,
                onQueryChange = { newText ->
                    // 3. Llama a la función del ViewModel para actualizar la búsqueda
                    clientesViewModel.onSearchTextChanged(newText)

                },
                onSearch = {
                    // Aquí puedes manejar la acción de búsqueda (ej. navegar a otra pantalla)
                    active = false
                },
                active = active,
                onActiveChange = { active = false },
                placeholder = { Text(stringResource(R.string.searchbar_buscar_clientes)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Icono de búsqueda"
                    )
                },
                trailingIcon = {

                    IconButton(onClick = {
                        clientesViewModel.startQRScanner(context)
                    })
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.qr_code),
                            contentDescription = "Icono Qr scan"
                        )
                    }
                }
            ) {

            }

            // Contenido que se muestra cuando no hay resultados de búsqueda
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa todo el espacio disponible
                    .padding(horizontal = 16.dp)
            ) {
                // 4. Itera sobre los resultados de búsqueda del ViewModel
                items(searchResults) { cliente ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp) // Añade un poco de espacio entre las tarjetas
                                    .clickable {
                                        // Acción al hacer clic en la tarjeta, por ejemplo, navegar a los detalles del cliente
                                        onClientClick(cliente.id) // Llama a la función de clic del cliente
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Añade una sombra sutil
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // Usa un color del tema
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
                                        model = cliente.imagenUrl, // Reemplaza con la URL de la imagen del cliente
                                        contentDescription = "Imagen del Cliente",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape), // Hace la imagen circular
                                        contentScale = ContentScale.Crop, // Escala la imagen para llenar el espacio
                                        placeholder = painterResource(id = R.drawable.ic_downloading), // Icono de placeholder mientras carga
                                        error = painterResource(id = R.drawable.ic_error) // Icono si hay error de carga
                                    )
                                    // --- FIN DEL CAMBIO ---

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(
                                        modifier = Modifier.weight(1f) // Ocupa el espacio restante
                                    ) {
                                        Text(
                                            text = cliente.nombre,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        // Suponiendo que tu objeto 'cliente' tiene una propiedad 'direccion'
                                        Text(
                                            text = cliente.ubicacion,
                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        // Suponiendo que tu objeto 'cliente' tiene una propiedad 'email'
                                        //Text(text = cliente.email, style = MaterialTheme.typography.bodySmall)

                                        // Suponiendo que tu objeto 'cliente' tiene una propiedad 'telefono'
                                        Text(
                                            text = cliente.telefono,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    // Ejemplo de icono adicional a la derecha
                                    Icon(
                                        painter = painterResource(id = R.drawable.chevron_forward),
                                        contentDescription = "Fordward Icon",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(end = 8.dp),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            // FIN DE LA CARD FORMATEADA
                }
            }
        }
    }
}