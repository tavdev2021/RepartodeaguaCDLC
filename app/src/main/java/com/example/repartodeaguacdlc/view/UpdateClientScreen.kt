package com.example.repartodeaguacdlc.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var bestAccuracy by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var isSearchingGPS by remember { mutableStateOf(false) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val cliente by clientesViewModel.selectedClient.collectAsStateWithLifecycle()
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    val location by viewModel.locationClient.collectAsStateWithLifecycle()
    val notasClient by viewModel.notasClient.collectAsStateWithLifecycle()

    val fullNameError by viewModel.fullNameError.collectAsStateWithLifecycle()
    val phoneError by viewModel.phoneError.collectAsStateWithLifecycle()
    val locationError by viewModel.locationError.collectAsStateWithLifecycle()

    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    val processLocationReceived: (Double, Double, Float) -> Unit = { lat, lon, accuracy ->
        if (isSearchingGPS) {
            currentAccuracy = accuracy
            if (accuracy <= 15f && accuracy < bestAccuracy) {
                bestAccuracy = accuracy
                viewModel.updateLocation(lat, lon)
            }
        }
    }

    fun activarSeguimientoGPS() {
        if (locationCallback == null) {
            locationCallback = LocationHelper.iniciarSeguimientoPreciso(
                fusedLocationClient = fusedLocationClient,
                onLocationReceived = processLocationReceived
            )
        }
    }

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

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            activarSeguimientoGPS()
        } else {
            Toast.makeText(context, "Se requiere ubicación exacta para esta función", Toast.LENGTH_SHORT).show()
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let { LocationHelper.detenerSeguimiento(fusedLocationClient, it) }
        }
    }

    LaunchedEffect(cliente) {
        viewModel.loadClientData(cliente)
    }

    LaunchedEffect(clienteId) {
        clientesViewModel.selectClient(clienteId)
    }

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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                title = { Text("Actualizar datos del cliente", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = "Volver a Home")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->

        if (cliente == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    stringResource(R.string.actualiza_datos_cliente),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN 1: INFORMACIÓN PERSONAL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            stringResource(R.string.informacion_personal_cliente),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { viewModel.onFullNameChange(it) },
                            label = { Text(stringResource(R.string.nombre_completo_cliente)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.TwoTone.Person, contentDescription = "Person Icon") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            isError = fullNameError != null,
                            supportingText = { fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        )

                        Spacer(modifier = Modifier.height(if (fullNameError != null) 4.dp else 12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.onPhoneNumberChange(it) },
                            label = { Text(stringResource(R.string.telefono_cliente)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.TwoTone.Phone, contentDescription = "Phone Icon") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            isError = phoneError != null,
                            supportingText = { phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN 2: UBICACIÓN Y DETALLES DE ENTREGA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            stringResource(R.string.ubicacion_detalles_cliente),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        val textoUbicacion = when {
                            !isSearchingGPS -> if (location.isNotBlank()) "Ubicacion Registrada" else "Sin ubicacion registrada"
                            location.isBlank() || currentAccuracy <= 0f || currentAccuracy >= 15f -> "Buscando señal GPS..."
                            currentAccuracy > 10f -> "Ubicación media obtenida..."
                            else -> "Ubicación precisa obtenida..."
                        }

                        val colorEstadoGps = when {
                            !isSearchingGPS -> MaterialTheme.colorScheme.primary
                            location.isBlank() || currentAccuracy <= 0f || currentAccuracy >= 15f -> Color.Red
                            currentAccuracy > 10f -> Color(0xFFFFB300)
                            else -> Color(0xFF4CAF50)
                        }

                        val errorLocation = when {
                            !isSearchingGPS -> locationError ?: ""
                            location.isBlank() || currentAccuracy <= 0f || currentAccuracy >= 15f -> "Buscando señal GPS... Colocate en un lugar abierto"
                            currentAccuracy > 10f -> "Precision del GPS Media..."
                            else -> "Precision del GPS Alta..."
                        }

                        OutlinedTextField(
                            value = textoUbicacion,
                            onValueChange = { },
                            label = { Text(stringResource(R.string.ubicacion_register_cliente)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    Icons.TwoTone.LocationOn,
                                    contentDescription = "Location Icon",
                                    tint = colorEstadoGps
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                showKeyboardOnFocus = false,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            isError = locationError != null,
                            supportingText = {
                                if (errorLocation.isNotBlank()) {
                                    Text(
                                        text = errorLocation,
                                        color = colorEstadoGps
                                    )
                                }
                            },
                            trailingIcon = {
                                if (isSearchingGPS) {
                                        Text(
                                            text = if (currentAccuracy > 0) "${currentAccuracy.toInt()}m" else "--",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colorEstadoGps
                                        )

                                        if (location.isBlank() || currentAccuracy > 10f) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                strokeWidth = 2.dp,
                                                color = colorEstadoGps
                                            )
                                        }
                                } else {
                                    IconButton(onClick = {
                                        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                        if (hasFineLocation) {
                                            bestAccuracy = Float.MAX_VALUE
                                            currentAccuracy = 0f
                                            isSearchingGPS = true
                                            Toast.makeText(context, "Afinando ubicación GPS...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Se requiere ubicación exacta para esta función", Toast.LENGTH_SHORT).show()
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }) {
                                        Icon(
                                            Icons.TwoTone.LocationSearching,
                                            contentDescription = "Buscar Ubicación",
                                            modifier = Modifier.size(28.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorEstadoGps,
                                unfocusedBorderColor = colorEstadoGps.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.height(if (locationError != null) 4.dp else 12.dp))

                        OutlinedTextField(
                            value = notasClient,
                            onValueChange = { viewModel.onNotesClientChange(it) },
                            label = { Text(stringResource(R.string.nota_register_cliente)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.TwoTone.Create, contentDescription = "Note Icon") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.updateCliente(context = context, clienteActual = cliente, onUpdateComplete = onUpdateSuccess)
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isSearchingGPS = false
                        focusManager.clearFocus()
                        viewModel.updateCliente(
                            context = context,
                            clienteActual = cliente,
                            onUpdateComplete = {
                                Toast.makeText(context, "Cliente actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                onUpdateSuccess()
                            }
                        )
                    },
                    enabled = !isLoading && fullName.isNotBlank() && phone.isNotBlank() && location.isNotBlank()
                            && fullNameError == null && phoneError == null && locationError == null,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Actualizar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { showDeleteConfirmationDialog = true }) {
                    Text("Eliminar Cliente", color = MaterialTheme.colorScheme.error, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar a este cliente? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        cliente?.let {
                            viewModel.deleteClient(it)
                            showDeleteConfirmationDialog = false
                            onClientDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LoadingOverlay(
        visible = isLoading,
        message = "Actualizando datos..."
    )
}