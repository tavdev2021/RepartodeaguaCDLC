package com.example.repartodeaguacdlc.view

import android.icu.util.Calendar
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.text.DateFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AuthViewModel,
               onAddNewClient:() -> Unit,
               onPedidos:() -> Unit,
               onClientes:() -> Unit,
               onSettings:() -> Unit,
               onLogout: () -> Unit) {

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current


    //Obtener el dia y fecha del sistema
    val calendar = Calendar.getInstance().time
    val dateFormat = DateFormat.getDateInstance(DateFormat.FULL).format(calendar)

    val acciones = listOf(
        AccionRapida(R.drawable.nuevo_cliente, "Nuevo Cliente",onAddNewClient),
        AccionRapida(R.drawable.lista_clientes, "Clientes", onClientes),
        AccionRapida(R.drawable.corte_caja, "Corte de caja", onPedidos),
        AccionRapida(R.drawable.inventario, "Inventario", onPedidos),
        AccionRapida(R.drawable.ruta, "Ruta", onSettings)
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
                            .padding(top = 40.dp, start = 10.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        //Foto de perfil
                        //val imageUrl = firebaseUser.photoUrl
                        val imageUrl = "https://i.pravatar.cc/300"
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

                                )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier
                                    .size(50.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }

                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top

                        ) {

                            Text(
                                (stringResource(R.string.name_hello) + " " + firebaseUser.displayName?.split(
                                    " "
                                )
                                    ?.firstOrNull()),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(start = 8.dp)
                            )

                            Text(
                                stringResource(R.string.name_route),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(start = 8.dp, top = 0.dp)
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

                        Text(
                            "$dateFormat",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    //Implementacion de Cards para datos rapidos

                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly) {
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(140.dp)
                                .clickable(onClick = { }),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                                    .padding(start = 16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(painter = painterResource(id = R.drawable.water_drop),
                                    contentDescription = "Money Icon",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 8.dp),
                                    contentScale = ContentScale.Fit)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Ventas de hoy",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall

                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "42",
                                    fontSize = 32.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold

                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .height(140.dp)
                                .clickable(onClick = { }),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                                    .padding(start = 16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(painter = painterResource(id = R.drawable.money_range),
                                    contentDescription = "Money Icon",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 8.dp),
                                    contentScale = ContentScale.Fit)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Ingresos de hoy",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall

                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "$3,450",
                                    fontSize = 32.sp,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold

                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // --- (NUEVO) IMPLEMENTACIÓN DE LAZYROW CON CARDS ---
                        Column {
                            Text(
                                text = "Acciones Rápidas",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .padding(start = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(100.dp)
                                .clickable(onClick = {
                                    Toast.makeText(context, "Realizar Nueva Venta", Toast.LENGTH_SHORT).show()
                                }),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically

                                ) {
                                    Icon(painter = painterResource(id = R.drawable.add_shopping_cart),
                                        contentDescription = "Cart Icon",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(start = 8.dp),
                                        tint =  MaterialTheme.colorScheme.background)

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            text = "Realizar Venta",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.background
                                        )

                                        Text(
                                            text = "Registrar nuevo pedido",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.background
                                        )
                                    }

                                    Icon(painter = painterResource(id = R.drawable.chevron_forward),
                                        contentDescription = "Forward Icon",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(end = 8.dp),
                                        tint = MaterialTheme.colorScheme.background)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

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
                                            Image(
                                                painter = painterResource(id = accion.icon),
                                                contentDescription = accion.text,
                                                modifier = Modifier.size(48.dp)
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