package com.example.repartodeaguacdlc.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    clienteId: Int,
    clientesViewModel: ClientesViewModel,
    clienteDireccion: String = "Av. Libertador 1234, Centro",
    onBack: () -> Unit
) {
    val cliente by clientesViewModel.selectedClient.collectAsState()
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
            // Barra inferior con el total y botón finalizar
            VentaBottomBarMock()
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
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = Color(0xFF1976D2)
                            )
                        }
                        Column(modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)) {
                            Text("Cliente Seleccionado", color = Color(0xFF1976D2), style = MaterialTheme.typography.labelSmall)
                            Text(cliente?.nombre ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
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

            items(6) { index ->
                val imagen = listOf(R.drawable.garrafon_agua, R.drawable.agua_pack, R.drawable.splash, R.drawable.splash, R.drawable.splash, R.drawable.splash)
                val titulos = listOf("Agua Purificada", "Pack 12x 500ml", "Dispensador", "Agua en bolsita 500ml Caja", "Hielo en bolsa", "Hielo en barra")
                val precios = listOf("$5.00 c/u", "$8.00 / pack", "$10.00 c/u", "$50.00 c/u", "$50.00 c/u", "$60.00 c/u")
                val subtotales = listOf("$10.00", "16.00", "$20.00", "$100.00", "$100.00", "$120.00")

                ProductoItemMock(imagen[index],titulos[index], precios[index], subtotales[index])
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

@Composable
fun ProductoItemMock(imagen: Int, nombre: String, precio: String, subtotal: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder de imagen

                Image(painter = painterResource(id = imagen), contentDescription = null,
                        modifier = Modifier
                        .size(64.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(Color(0xFF03A9F4).copy(alpha = 0.3f)))

            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(nombre, fontWeight = FontWeight.Bold)
                Text(precio, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                // Badge de Subtotal
                Surface(
                    color = Color(0xFFE8EAF6),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        subtotal,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        ),
                        color = Color(0xFF3F51B5),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controles de cantidad
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(
                        18.dp
                    ))
                }

                Text("2", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2196F3), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VentaBottomBarMock() {
    val context = LocalContext.current
    Surface(
        shadowElevation = 16.dp,
        color = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier
            .padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL A PAGAR", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Text("6 artículos", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                Text("$46.00", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Próximamente lógica Room */
                    Toast.makeText(context, "Venta finalizada", Toast.LENGTH_SHORT).show()},
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