package com.example.repartodeaguacdlc.view

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import coil.compose.AsyncImage
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesList(// 1. Inyecta tu ClientesViewModel
    clientesViewModel: ClientesViewModel) {

    // 2. Recolecta los estados del ViewModel
    val searchText by clientesViewModel.searchText.collectAsState()
    val searchResults by clientesViewModel.searchResults.collectAsState()


    //Contexto de la App
    val context = LocalContext.current
    val messageqr = stringResource(R.string.funcion_qr_searchbar)

    //Estados de SearchBar sin ViewModel
    //var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                // 4. Itera sobre los resultados de búsqueda del ViewModel
                items(searchResults) { cliente ->
                    ListItem(
                        headlineContent = {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp) // Añade un poco de espacio entre las tarjetas
                                    .clickable {
                                        // Acción al hacer clic en la tarjeta, por ejemplo, navegar a los detalles del cliente
                                        Toast
                                            .makeText(
                                                context,
                                                "Cliente: ${cliente.nombre}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
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
                                    // Asumiendo que tu objeto `cliente` tiene una propiedad `imageUrl` con la URL de la imagen.
                                    AsyncImage(
                                        model = cliente.imageUrl, // Reemplaza con la URL de la imagen del cliente
                                        contentDescription = "Imagen del Cliente",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape), // Hace la imagen circular
                                        contentScale = ContentScale.Crop, // Escala la imagen para llenar el espacio
                                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground), // Icono de placeholder mientras carga
                                        error = painterResource(id = R.drawable.ic_launcher_foreground) // Icono si hay error de carga
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
                                        // Text(text = cliente.direccion, style = MaterialTheme.typography.bodySmall)
                                    }
                                    // Ejemplo de icono adicional a la derecha
                                    Icon(
                                        imageVector = Icons.AutoMirrored.TwoTone.ArrowForward,
                                        contentDescription = "Icono de Favorito",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            // FIN DE LA CARD FORMATEADA
                        },
                    )
                }
            }
        }

    }
}