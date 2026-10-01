package com.example.repartodeaguacdlc.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Phone
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

    // 1. Obtener el contexto de Android
    val context = LocalContext.current

    // 2. Instanciar el ViewModel usando el Factory
    val viewModel: AddNewClientViewModel = viewModel(
        factory = AddNewClientViewModelFactory(context)
    )

    // Observar los valores de los campos desde el ViewModel
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val phone by viewModel.phone.collectAsStateWithLifecycle()
    val location by viewModel.locationClient.collectAsStateWithLifecycle()
    val notasClient by viewModel.notasClient.collectAsStateWithLifecycle()

    // Observar los errores de los campos desde el ViewModel
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

    // Estados para la UI
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }

// 1. Launcher para solicitar permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            // Permisos concedidos: Iniciamos el GPS
            locationCallback = LocationHelper.iniciarSeguimientoPreciso(
                fusedLocationClient,
                onLocationReceived = {lat, lon, accuracy ->
                    currentAccuracy = accuracy
                    if (accuracy <= 15f) viewModel.updateLocation(lat, lon)
                }
            )
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Control de inicio ()LaunchedEffect)
    LaunchedEffect(Unit) {

        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation && hasCoarseLocation) {

            // Permisos concedidos: Iniciamos el GPS

            locationCallback = LocationHelper.iniciarSeguimientoPreciso(
                fusedLocationClient,
                onLocationReceived = { lat, lon, accuracy ->
                    currentAccuracy = accuracy
                    // Si la precisión es mejor a 15 metros, actualizamos el ViewModel automáticamente
                    if (accuracy <= 15f) {
                        viewModel.updateLocation(lat, lon)
                    }
                }
            )
        } else {
            // Si no hay permisos, lanzamos el launcher
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 3. Limpieza al salir de la pantalla
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appbar_regstrarnuevocliente)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHomeFromAddNewClient)
                    {
                        Icon(Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = "Volver a Home")
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = padding)
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Image(
                painter = painterResource(id = R.drawable.clientes),
                contentDescription = "Image Cliente",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        3.dp,
                        MaterialTheme.colorScheme.secondary,
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
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
                    Text("Buscando señal GPS... Colocate en un lugar abierto", color = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {

                        Text(
                            text = "${currentAccuracy.toInt()}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = if(currentAccuracy > 0 && currentAccuracy <= 15f) Color(0xFF4CAF50) else Color.Red
                        )

                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }) {

                        }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(if (locationError != null) 4.dp else 8.dp)) // Menos espacio si hay error

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
                    viewModel.registerClient(context, routeId)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                focusManager.clearFocus()
                viewModel.registerClient(context, routeId)
            },
                enabled = !isLoading && fullName.isNotBlank() && phone.isNotBlank() && location.isNotBlank()
                        && fullNameError == null && phoneError == null && locationError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {

                Text(stringResource(R.string.button_register_cliente), fontSize = 16.sp)
            }
        }
    }

    // Overlay con blur elegante
    LoadingOverlay(
        visible = isLoading,
        message = stringResource(R.string.loading_registrar_cliente)
    )
}