package com.example.repartodeaguacdlc.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Create
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Phone
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.util.LocationHelper
import com.example.common.view.LoadingOverlay
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.data.AddNewClientViewModelFactory
import com.example.repartodeaguacdlc.viewmodel.AddNewClientViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewClient(
    routeId: String,
    onNavigateToHomeFromAddNewClient: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: AddNewClientViewModel = viewModel(
        factory = AddNewClientViewModelFactory(context)
    )

    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    val location by viewModel.locationClient.collectAsStateWithLifecycle()
    val notasClient by viewModel.notasClient.collectAsStateWithLifecycle()

    val fullNameError by viewModel.fullNameError.collectAsStateWithLifecycle()
    val phoneError by viewModel.phoneError.collectAsStateWithLifecycle()
    val locationError by viewModel.locationError.collectAsStateWithLifecycle()

    val error by viewModel.error.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var bestAccuracy by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

    val processLocationReceived: (Double, Double, Float) -> Unit = { lat, lon, accuracy ->
        currentAccuracy = accuracy
        if (accuracy <= 15f && accuracy < bestAccuracy) {
            bestAccuracy = accuracy
            viewModel.updateLocation(lat, lon)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            locationCallback = LocationHelper.iniciarSeguimientoPreciso(
                fusedLocationClient,
                onLocationReceived = processLocationReceived
            )
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation && hasCoarseLocation) {
            locationCallback = LocationHelper.iniciarSeguimientoPreciso(
                fusedLocationClient,
                onLocationReceived = processLocationReceived
            )
        } else {
            locationPermissionLauncher.launch(
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

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Cliente registrado exitosamente", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
            onNavigateToHomeFromAddNewClient()
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
                title = { Text(stringResource(R.string.appbar_regstrarnuevocliente), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHomeFromAddNewClient) {
                        Icon(Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = "Volver a Home")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    ) { padding ->

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
                text = stringResource(R.string.ingresa_datos_cliente),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. SECCIÓN 1: INFORMACIÓN PERSONAL
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
                        text = stringResource(R.string.informacion_personal_cliente),
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

            // 3. SECCIÓN 2: UBICACIÓN Y DETALLES DE ENTREGA
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
                        text = stringResource(R.string.ubicacion_detalles_cliente),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // Lógica exacta conservada
                    val textoUbicacion = when {
                        location.isBlank() || currentAccuracy <= 0f || currentAccuracy >= 15f -> "Buscando señal GPS..."
                        currentAccuracy > 10f -> "Ubicación media obtenida..."
                        else -> "Ubicación precisa obtenida..."
                    }

                    val colorEstadoGps = when {
                        location.isBlank() || currentAccuracy <= 0f || currentAccuracy >= 15f -> Color.Red
                        currentAccuracy > 10f -> Color(0xFFFFB300)
                        else -> Color(0xFF4CAF50)
                    }

                    val errorLocation = when {
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
                            Text(
                                text = errorLocation,
                                color = colorEstadoGps
                            )
                        },
                        trailingIcon = {
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
                            viewModel.registerClient(context, routeId)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. BOTÓN DE REGISTRO CON ELEVACIÓN Y FLECHA
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registerClient(context, routeId)
                },
                enabled = !isLoading && fullName.isNotBlank() && phone.isNotBlank() && location.isNotBlank()
                        && fullNameError == null && phoneError == null && locationError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.button_register_cliente),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    LoadingOverlay(
        visible = isLoading,
        message = stringResource(R.string.loading_registrar_cliente)
    )
}