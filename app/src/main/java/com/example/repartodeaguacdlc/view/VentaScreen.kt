package com.example.repartodeaguacdlc.view

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.common.model.Clientes
import com.example.repartodeaguacdlc.R
import com.example.common.model.Productos
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.example.repartodeaguacdlc.viewmodel.VentasViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    clienteId: String,
    clientesViewModel: ClientesViewModel,
    ventasViewModel: VentasViewModel,
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
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium)
                    }
                }
            )
        },
        bottomBar = {
            val context = LocalContext.current

            cliente?.let { currentCliente ->

                // Barra inferior con el total y botón finalizar
                VentaBottomBar(
                    total = total,
                    cantidadArticulos = articulos,
                    metodoPago = metodoSeleccionado,
                    cliente = currentCliente,
                    onFinalizarVentaCalculada = { montoPagado, montoCredito, nuevoSaldoPendiente, nuevoSaldoAFavor, montoAbonoAnterior ->
                        ventasViewModel.finalizarVenta(
                            clienteId = clienteId,
                            routeId = currentCliente.routeId,
                            metodoPago = metodoSeleccionado,
                            montoPagado = montoPagado,
                            montoCredito = montoCredito,
                            nuevoSaldoPendiente = nuevoSaldoPendiente,
                            nuevoSaldoAFavor = nuevoSaldoAFavor,
                            montoAbonoAnterior = montoAbonoAnterior,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Venta generada con exito",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onBackToClientList() // Regresa a la lista de clientes
                            }
                        )

                    }
                )
            }
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer), // Azul muy claro
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
                            color = MaterialTheme.colorScheme.secondaryContainer,
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
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column(modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)) {
                            Text("Cliente Seleccionado", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                            Text(cliente?.nombre ?: "Cargando...", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = when {
                                    cliente == null -> "Cargando direccion..."
                                    cliente!!.direccion.isNotBlank() -> cliente!!.direccion
                                    else -> cliente!!.ubicacion
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2)
                        }
                        // Icono de gota de fondo (decorativo)
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f),
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
                        "Crédito" to Icons.Default.Receipt
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
                                containerColor = if (esSeleccionado) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                                contentColor = if (esSeleccionado) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
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
                                    tint = if (esSeleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (producto.cantidad > 0) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Subtotal: $${String.format("%.2f", subtotal)}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Botones conectados a las lambdas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Fondo gris claro de la elipse
                    .padding(6.dp)
                ) {

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape) // Asegura ripple circular
                        .background(
                            if (producto.cantidad > 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant // Igual al fondo cuando está apagado
                        )
                        .clickable(enabled = producto.cantidad > 0) { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Menos",
                        modifier = Modifier
                            .size(18.dp),
                        tint = if (producto.cantidad > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }

                Text("${producto.cantidad}",
                    modifier = Modifier
                        .padding(horizontal = 12.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary) // Fondo azul sólido
                        .clickable{ onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Mas",
                        tint = MaterialTheme.colorScheme.onPrimary,
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
    metodoPago: String,
    cliente: Clientes,
    onFinalizarVentaCalculada: (
        montoPagado: Double,
        montoCredito: Double,
        nuevoSaldoPendiente: Double,
        nuevoSaldoAFavor: Double,
        montoAbonoAnterior: Double
    ) -> Unit
) {

    val LIMITE_CREDITO_MAXIMO = 1000.0
    val esEfectivo = metodoPago == "Efectivo"
    val esCredito = metodoPago == "Crédito"

    // 1. Estado para el dinero recibido o pago parcial
    var montoEntregadoText by remember(total, metodoPago) { mutableStateOf("") }
    var aplicarSaldoAFavor by remember (total, metodoPago) { mutableStateOf(false) }
    var aplicarSobranteADeuda by remember (total, metodoPago) { mutableStateOf(false) }
    var guardarCambioComoSaldoAFavor by remember (total, metodoPago) { mutableStateOf(false) }

    // 1. Saldo a favor usado
    val saldoAFavorUsado = if (aplicarSaldoAFavor && cliente.saldoAFavor > 0) minOf(cliente.saldoAFavor, total) else 0.0
    val saldoAFavorRemanente = cliente.saldoAFavor - saldoAFavorUsado
    val totalDespuesDeSaldoAFavor = total - saldoAFavorUsado

    val montoEntregadoDouble = if (montoEntregadoText.isBlank()) {
        if (esEfectivo) totalDespuesDeSaldoAFavor else 0.0
    } else {
        montoEntregadoText.toDoubleOrNull() ?: 0.0
    }

    val efectivoPagoConfirmado = (totalDespuesDeSaldoAFavor == 0.0) || (
            montoEntregadoText.isNotBlank() && montoEntregadoDouble >= totalDespuesDeSaldoAFavor
            )

    // 🔴 2. CÁLCULOS INTELIGENTES EN EFECTIVO CON DEUDA ANTERIOR
    val excedenteEfectivo = if (esEfectivo && montoEntregadoDouble > totalDespuesDeSaldoAFavor) {
        montoEntregadoDouble - totalDespuesDeSaldoAFavor
    } else 0.0

    // 🔴 2. ABONO POTENCIAL (Muestra la cantidad disponible aunque el chip aún no esté seleccionado)
    val abonoPotencial = if (esEfectivo && cliente.saldoPendiente > 0) {
        minOf(excedenteEfectivo, cliente.saldoPendiente)
    } else 0.0

    // Si el cliente entrega más dinero del total de hoy y debe dinero anterior:
    val abonoADeudaAutocobrado = if (aplicarSobranteADeuda) abonoPotencial else 0.0

    val cambioRemanenteSobrante = maxOf(0.0, excedenteEfectivo - abonoADeudaAutocobrado)

    val cambioEfectivoFinal = if (esEfectivo && !guardarCambioComoSaldoAFavor) {
        cambioRemanenteSobrante
    } else 0.0

    val faltanteEfectivo = if (esEfectivo && montoEntregadoDouble < totalDespuesDeSaldoAFavor) {
        totalDespuesDeSaldoAFavor - montoEntregadoDouble
    } else 0.0

    // 🔴 3. DEUDA PROYECTADA
    val montoCreditoGeneradoHoy = if (esCredito) maxOf(0.0, totalDespuesDeSaldoAFavor - montoEntregadoDouble) else 0.0

    val nuevaDeudaProyectada = if (esEfectivo) {
        maxOf(0.0, cliente.saldoPendiente - abonoADeudaAutocobrado)
    } else {
        maxOf(0.0, cliente.saldoPendiente + montoCreditoGeneradoHoy)
    }
        val excedeLimiteCredito = esCredito && nuevaDeudaProyectada > LIMITE_CREDITO_MAXIMO

    // 🔴 4. MONTO TOTAL EN EFECTIVO RECIBIDO HOY (Para el Corte de Caja)
    val montoPagadoEfectivoHoy = if (esEfectivo) {
        minOf(montoEntregadoDouble, totalDespuesDeSaldoAFavor) + if (guardarCambioComoSaldoAFavor) cambioRemanenteSobrante else 0.0
    } else {
        minOf(montoEntregadoDouble, totalDespuesDeSaldoAFavor)
    }

    // 🔴 5. NUEVO SALDO A FAVOR PROYECTADO
    val nuevoSaldoAFavorProyectado = saldoAFavorRemanente + if (esEfectivo && guardarCambioComoSaldoAFavor) cambioRemanenteSobrante else 0.0

    val esPagoValido = cantidadArticulos > 0 && !excedeLimiteCredito && (
            (esEfectivo && efectivoPagoConfirmado) || (esCredito)
            )

    Surface(
        shadowElevation = 12.dp,
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
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
                    Text("TOTAL A PAGAR", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)


                    if (cantidadArticulos == 1)
                        Text("$cantidadArticulos artículo", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    else
                    Text("$cantidadArticulos artículos", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                }
                Text("$${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
                )
            }

            // 🟢 OPCIÓN: USAR SALDO A FAVOR DISPONIBLE DEL CLIENTE
            if (cliente.saldoAFavor > 0 && cantidadArticulos > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                FilterChip(
                    selected = aplicarSaldoAFavor,
                    onClick = { aplicarSaldoAFavor = !aplicarSaldoAFavor },
                    label = {
                        Text(
                            "Usar Saldo a Favor disponible ($${String.format(Locale.getDefault(), "%.2f", cliente.saldoAFavor)})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        if (aplicarSaldoAFavor) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = Color(0xFF2E7D32),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            }

            // 🔴 Modo 1: Calculos y herramientas en efectivo
            if (esEfectivo && cantidadArticulos > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 🔴 ALERTA DE DEUDA ANTERIOR EN VENTA EN EFECTIVO
                if (cliente.saldoPendiente > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Deuda anterior pendiente:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("$${String.format(Locale.getDefault(), "%.2f", cliente.saldoPendiente)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Campo para ingresar la cantidad recibida
                    OutlinedTextField(
                        value = montoEntregadoText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                montoEntregadoText = filtered
                            }
                        },
                        label = { Text("Monto Recibido $") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    val hayCambioValido =
                        montoEntregadoDouble >= totalDespuesDeSaldoAFavor && montoEntregadoText.isNotBlank()

                    // Tarjeta con el cambio calculado
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (guardarCambioComoSaldoAFavor)
                            Color(0xFFE8F5E9)
                        else if (hayCambioValido)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            if (hayCambioValido) MaterialTheme.colorScheme.tertiary else Color.Transparent
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (guardarCambioComoSaldoAFavor) "A FAVOR HOY" else "CAMBIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (guardarCambioComoSaldoAFavor || hayCambioValido)
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = if (guardarCambioComoSaldoAFavor)
                                "$${String.format(Locale.getDefault(), "%.2f", cambioRemanenteSobrante)}"
                                else "$${String.format(Locale.getDefault(), "%.2f", cambioEfectivoFinal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (guardarCambioComoSaldoAFavor || hayCambioValido) MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Chips para Billetes Rápidos ($100, $200, $500, Exacto)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val billetes = listOf(
                        "Exacto" to String.format(
                            Locale.getDefault(),
                            "%.2f",
                            totalDespuesDeSaldoAFavor
                        ),
                        "$100" to "100.00",
                        "$200" to "200.00",
                        "$500" to "500.00"
                    )

                    billetes.forEach { (etiqueta, valor) ->

                        val seleccionado = montoEntregadoText == valor
                        FilterChip(
                            selected = seleccionado,
                            onClick = { montoEntregadoText = valor },
                            label = {
                                Text(
                                    etiqueta,
                                    fontSize = 12.sp,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Si entregó dinero extra y tiene deuda previa: Opción de Abonar el excedente
                if (abonoPotencial > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChip(
                        selected = aplicarSobranteADeuda,
                        onClick = { aplicarSobranteADeuda = !aplicarSobranteADeuda },
                        label = { Text("Abonar $${String.format(Locale.getDefault(), "%.2f", abonoPotencial)} a la deuda anterior", fontSize = 11.sp) },
                        leadingIcon = { if (aplicarSobranteADeuda) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Caso Guardar el cambio como saldo a favor
                val hayCambioParaGuardar = (cambioRemanenteSobrante > 0) || guardarCambioComoSaldoAFavor

                if (hayCambioParaGuardar) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChip(
                        selected = guardarCambioComoSaldoAFavor,
                        onClick = { guardarCambioComoSaldoAFavor = !guardarCambioComoSaldoAFavor },
                        label = {
                            Text(
                                "Guardar cambio ($${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        cambioRemanenteSobrante
                                    )
                                }) como saldo a favor", fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            if (guardarCambioComoSaldoAFavor) Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

                // Advertencia si la cantidad ingresada es menor al total
                if (montoEntregadoText.isNotBlank() && montoEntregadoDouble < totalDespuesDeSaldoAFavor) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Faltan $${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    faltanteEfectivo
                                )
                            } para completar el pago",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

            // 🔴 MODO 2: CÁLCULOS Y HERRAMIENTAS EN CRÉDITO (Casos #1, #2 y #4)
            if (esCredito && cantidadArticulos > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Campo de Pago Parcial Hoy
                    OutlinedTextField(
                        value = montoEntregadoText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                montoEntregadoText = filtered
                            }
                        },
                        label = { Text("Pago hoy ($)") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    // Tarjeta de Deuda de esta compra
                    Surface(
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("A CRÉDITO HOY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("$${String.format(Locale.getDefault(), "%.2f", montoCreditoGeneradoHoy)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resumen de Deuda Acumulada del Cliente
                Text(
                    text = "Deuda total proyectada: $${String.format(Locale.getDefault(), "%.2f", nuevaDeudaProyectada)} " +
                            "(Anterior: $${String.format(Locale.getDefault(), "%.2f", cliente.saldoPendiente)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (excedeLimiteCredito) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 🔴 ADVERTENCIA DE LÍMITE DE CRÉDITO DE $1,000 EXCEDIDO
                if (excedeLimiteCredito) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Límite de crédito superado ($${String.format("%.2f", nuevaDeudaProyectada)} de $1,000.00 máx). El cliente debe abonar primero o pagar en efectivo.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    onFinalizarVentaCalculada(
                        montoPagadoEfectivoHoy,
                        montoCreditoGeneradoHoy,
                        nuevaDeudaProyectada,
                        nuevoSaldoAFavorProyectado,
                        abonoADeudaAutocobrado
                    )
                },
                enabled = esPagoValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Finalizar Venta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}