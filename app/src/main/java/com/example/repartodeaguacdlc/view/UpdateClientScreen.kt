package com.example.repartodeaguacdlc.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Create
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material.icons.twotone.LocationSearching
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.common.util.LocationHelper
import com.example.common.view.LoadingOverlay
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesUpdateViewModel
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateClientScreen(
    clienteId: String,
    clientesViewModel: ClientesViewModel,
    viewModel: ClientesUpdateViewModel,
    onUpdateSuccess: () -> Unit,
    onBack: () -> Unit,
    onClientDeleted: () -> Unit
) {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estados de control de GPS
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var isSearchingGPS by remember { mutableStateOf(false) } // Control manual de busqueda
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Observar los valores de los campos desde el ViewModel
    val cliente by clientesViewModel.selectedClient.collectAsStateWithLifecycle()
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    val location by viewModel.locationClient.collectAsStateWithLifecycle()
    val notasClient by viewModel.notasClient.collectAsStateWithLifecycle()

    // Observar los errores de los campos desde el ViewModel
    val fullNameError by viewModel.fullNameError.collectAsStateWithLifecycle()
    val phoneError by viewModel.phoneError.collectAsStateWithLifecycle()
    val locationError by viewModel.locationError.collectAsStateWithLifecycle()

    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    // Funcion interna para arancar el GPS (se llama tras validar permisos)
    fun activarSeguimientoGPS() {
        if (locationCallback == null) {
            locationCallback = LocationHelper.iniciarSeguimientoPreciso(
                fusedLocationClient = fusedLocationClient,
                onLocationReceived = { lat, lon, accuracy ->
                    currentAccuracy = accuracy
                    // CRITICO: Solo actualiza si el usuario pulsó el botón y la precisión es <= 15m
                    if (isSearchingGPS && accuracy <= 15f) {
                        viewModel.updateLocation(lat, lon)
                        isSearchingGPS = false // Apagamos la búsqueda automáticamente
                        Toast.makeText(context, "Ubicación detectada con precision", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // 1. Configuracion del lanzador de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            activarSeguimientoGPS()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Verificar al entrar si tenemos permisos
    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(hasFineLocation) {
            activarSeguimientoGPS()
        } else {
             // No hay permisos, los pedimos
            Toast.makeText(context, "Se requiere ubicación exacta para esta función", Toast.LENGTH_SHORT).show()
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // 3. Limpieza al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let { LocationHelper.detenerSeguimiento(fusedLocationClient, it) }
        }
    }

    // Carga de datos inicial del cliente
    LaunchedEffect(cliente) {
        viewModel.loadClientData(cliente)
    }

    LaunchedEffect(clienteId) {
        clientesViewModel.selectClient(clienteId)
    }

    // 4. MANEJO DE ÉXITO Y ERROR
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Cliente actualizado exitosamente", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onUpdateSuccess()
        }
    }


    LaunchedEffect(error) {
        error?.let { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = androidx.compose.material3.SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Actualizar datos del cliente") },
                navigationIcon = {
                    IconButton(onClick = onBack)
                    {
                        Icon(Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = "Volver a Home")
                    }
                }
            )
        }
    ) { padding ->

        if (cliente == null) {
            // Si el cliente es nulo, mostramos un indicador de carga en el centro.
            // Esto evita el crash.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = padding)
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AsyncImage(
                    model = cliente!!.imagenUrl,
                    contentDescription = "Imagen del Cliente",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)// Hace la imagen circular
                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),// Pone un borde en la imagen
                    contentScale = ContentScale.Crop, // Escala la imagen para llenar el espacio
                    placeholder = painterResource(id = R.drawable.ic_downloading), // Icono de placeholder mientras carga
                    error = painterResource(id = R.drawable.ic_error) // Icono si hay error de carga
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.ingresa_datos_cliente),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.informacion_personal_cliente),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { viewModel.onFullNameChange(it) },
                    label = { Text(stringResource(R.string.nombre_completo_cliente)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.TwoTone.Person, contentDescription = "Person Icon")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        showKeyboardOnFocus = true, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    isError = fullNameError != null,
                    supportingText = {
                        fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(if (fullNameError != null) 4.dp else 8.dp)) // Menos espacio si hay error

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    label = { Text(stringResource(R.string.telefono_cliente)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.TwoTone.Phone, contentDescription = "Phone Icon")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone,
                        showKeyboardOnFocus = true, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    isError = phoneError != null,
                    supportingText = {
                        phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(if (phoneError != null) 4.dp else 8.dp)) // Menos espacio si hay error

                Text(
                    stringResource(R.string.ubicacion_detalles_cliente),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { viewModel.onLocationChangeClient(it) },
                    label = { Text(stringResource(R.string.ubicacion_register_cliente)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    leadingIcon = {
                        Icon(Icons.TwoTone.LocationOn, contentDescription = "Location Icon")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        showKeyboardOnFocus = false,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    isError = locationError != null,
                    supportingText = {
                        if (isSearchingGPS) Text("Buscando señal GPS... Colocate en un lugar abierto", color = MaterialTheme.colorScheme.primary)
                        else locationError?.let {Text(it) }
                    },
                    trailingIcon = {
                        if (isSearchingGPS) {
                            // Mostramos metros y cargador
                            Text(
                                text = "${currentAccuracy.toInt()}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentAccuracy > 0 && currentAccuracy <= 15f) Color(0xFF4CAF50) else Color.Red
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = {
                                val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFineLocation) {
                                    isSearchingGPS = true
                                } else {
                                    // Si no tiene la exacta, se la pedimos de nuevo explicando por qué
                                    Toast.makeText(context, "Se requiere ubicación exacta para esta función", Toast.LENGTH_SHORT).show()
                                    permissionLauncher.launch(arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION))
                                }
                            }) {
                                Icon(
                                    Icons.TwoTone.LocationSearching,
                                    contentDescription = "Buscar Ubicacion",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(if (locationError != null) 4.dp else 16.dp)) // Menos espacio si hay error

                OutlinedTextField(
                    value = notasClient,
                    onValueChange = { viewModel.onNotesClientChange(it) },
                    label = { Text(stringResource(R.string.nota_register_cliente)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.TwoTone.Create, contentDescription = "Note Icon")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                        showKeyboardOnFocus = true, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.updateCliente(
                            context = context,
                            clienteActual = cliente,
                            onUpdateComplete = {
                                onUpdateSuccess()
                            })
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.updateCliente(
                            context = context,
                            clienteActual = cliente,
                            onUpdateComplete = {
                                onUpdateSuccess()
                            })
                    },
                    enabled = !isSearchingGPS && !isLoading && fullName.isNotBlank() && phone.isNotBlank() && location.isNotBlank()
                            && fullNameError == null && phoneError == null &&  locationError == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {

                    Text("Actualizar", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de texto para eliminar
                TextButton(
                    onClick = {
                        // Mostrar el diálogo de confirmación en lugar de eliminar directamente
                        showDeleteConfirmationDialog = true
                    }
                ) {
                    Text(text = "Eliminar Cliente", color = MaterialTheme.colorScheme.error,
                        fontSize = 20.sp)
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                // Cierra el diálogo si el usuario presiona fuera de él
                showDeleteConfirmationDialog = false
            },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar a este cliente? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        cliente?.let {
                                viewModel.deleteClient(it)
                                showDeleteConfirmationDialog = false // Cierra el diálogo
                                onClientDeleted() // Navega hacia atrás o a la lista principal
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false // Cierra el diálogo
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Overlay con blur elegante
    LoadingOverlay(
        visible = isLoading,
        message = "Actualizando datos..."
    )
}