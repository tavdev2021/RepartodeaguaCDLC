package com.example.repartodeaguacdlc.view

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.repartodeaguacdlc.R
import com.example.common.model.Productos
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.example.repartodeaguacdlc.viewmodel.VentasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    clienteId: String,
    clientesViewModel: ClientesViewModel,
    ventasViewModel: VentasViewModel,
    clienteDireccion: String = "Av. Libertador 1234, Centro",
    onBack: () -> Unit,
    onBackToClientList: () -> Unit
) {
    val cliente by clientesViewModel.selectedClient.collectAsStateWithLifecycle()
    val listaProductos by ventasViewModel.productos.collectAsStateWithLifecycle()
    val total by ventasViewModel.totalPagar.collectAsStateWithLifecycle()
    val articulos by ventasViewModel.totalArticulos.collectAsStateWithLifecycle()

    var metodoSeleccionado by remember { mutableStateOf("Efectivo") }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Realizar Venta", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        // Color azul similar al de la imagen
                        Text("Cancelar",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium)
                    }
                }
            )
        },
        bottomBar = {
            val context = LocalContext.current
            // Barra inferior con el total y botón finalizar
            VentaBottomBar(
                total = total,
                cantidadArticulos = articulos,
                onFinalizar = {

                    ventasViewModel.finalizarVenta(
                        clienteId = clienteId,
                        metodoPago = metodoSeleccionado,
                        onSuccess = {
                            Toast.makeText(context, "Venta generada con exito", Toast.LENGTH_SHORT).show()
                            onBackToClientList() // Regresa a la lista de clientes
                        }
                    )

                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección Cliente
            item {
                Text("Cliente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Azul muy claro
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFBBDEFB),
                            modifier = Modifier.size(48.dp)
                        ) {
                            //Foto de perfil
                            val imageUrl = cliente?.imagenUrl
                            //val imageUrl = "https://i.pravatar.cc/300"
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Imagen de perfil",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.secondary,
                                            CircleShape
                                        ),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.ic_downloading),
                                    error = painterResource(id = R.drawable.ic_error)
                                )
                            } else {

                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp),
                                    tint = Color(0xFF1976D2)
                                )
                            }
                        }
                        Column(modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)) {
                            Text("Cliente Seleccionado", color = Color(0xFF1976D2), style = MaterialTheme.typography.labelSmall)
                            Text(cliente?.nombre ?: "Cargando...", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(clienteDireccion, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        // Icono de gota de fondo (decorativo)
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color.LightGray.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(48.dp)
                        )
                    }
                }
            }

            // Sección Productos
            item {
                Text("Productos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            items(listaProductos.size) { index ->
                val producto = listaProductos[index]
                ProductoItem(
                    producto = producto,
                    onIncrement = {
                        ventasViewModel.actualizarCantidad(producto.id, producto.cantidad + 1)
                    },
                    onDecrement = {
                        ventasViewModel.actualizarCantidad(producto.id, producto.cantidad - 1)
                    }
                )
            }

            // Sección Método de pago
            item {
                Text("Método de Pago",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                // Espacio para los selectores de pago
                Spacer(modifier = Modifier.height(8.dp))

                // Selector de pago (Simulado)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val metodos = listOf(
                        "Efectivo" to Icons.Default.Payments,
                        "Tarjeta" to Icons.Default.CreditCard,
                        "Transf." to Icons.Default.AccountBalance
                    )

                    metodos.forEach { (nombre, icono) ->
                        val esSeleccionado = metodoSeleccionado == nombre

                        Card(
                            onClick = { metodoSeleccionado = nombre },
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esSeleccionado) Color(0xFF2196F3) else Color.White,
                                contentColor = if (esSeleccionado) Color.White else Color.Gray
                            ),
                            elevation = CardDefaults.cardElevation(if (esSeleccionado) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icono,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (esSeleccionado) Color.White else Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = nombre,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ProductoItem(
    producto: Productos, // Tu Data Class
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val subtotal = producto.precio * producto.cantidad

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier
            .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(producto.imagenUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_downloading)
                    .error(R.drawable.ic_error)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF03A9F4).copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)) {
                Text(
                    producto.nombre,
                    fontWeight = FontWeight.Bold)

                Text(
                    "$${producto.precio} c/u",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray)

                if (producto.cantidad > 0) {
                    Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Text("Subtotal: $${String.format("%.2f", subtotal)}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF3F51B5), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Botones conectados a las lambdas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)) // Fondo gris claro de la elipse
                    .padding(6.dp)
                ) {

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape) // Asegura ripple circular
                        .background(
                            if (producto.cantidad > 0) Color.White
                            else Color(0xFFF5F5F5) // Igual al fondo cuando está apagado
                        )
                        .clickable(enabled = producto.cantidad > 0) { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Menos",
                        modifier = Modifier
                            .size(18.dp),
                        tint = if (producto.cantidad > 0) Color(0xFF2196F3) else Color.LightGray
                    )
                }

                Text("${producto.cantidad}",
                    modifier = Modifier
                        .padding(horizontal = 12.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)) // Fondo azul sólido
                        .clickable{ onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Mas",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun VentaBottomBar(
    total: Double,
    cantidadArticulos: Int,
    onFinalizar: () -> Unit
) {
    Surface(
        shadowElevation = 16.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier
            .navigationBarsPadding()
            .padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL A PAGAR", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("$cantidadArticulos artículos", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                Text("$${String.format("%.2f", total)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = /* Próximamente lógica Room */
                    onFinalizar,
                enabled = cantidadArticulos > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Finalizar Venta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}