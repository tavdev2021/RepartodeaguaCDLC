package com.example.repartodeaguacdlc.view

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.twotone.Email
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
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
import coil.compose.AsyncImage
import com.example.common.view.LoadingOverlay
import com.example.repartodeaguacdlc.R
import com.example.repartodeaguacdlc.viewmodel.ClientesUpdateViewModel
import com.example.repartodeaguacdlc.viewmodel.ClientesViewModel
import com.google.android.gms.location.Priority
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

    // 1. Obtener el contexto de Android
    val context = LocalContext.current

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val cliente by clientesViewModel.selectedClient.collectAsState()

    LaunchedEffect(cliente) {
        viewModel.loadClientData(cliente)
    }

    // Observar los valores de los campos desde el ViewModel
    val fullName by viewModel.fullName.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val email by viewModel.emailRegisterClient.collectAsState()
    val location by viewModel.locationClient.collectAsState()
    val notasClient by viewModel.notasClient.collectAsState()

    // Observar los errores de los campos desde el ViewModel
    val fullNameError by viewModel.fullNameError.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    val emailError by viewModel.emailErrorRegister.collectAsState()
    val locationError by viewModel.locationError.collectAsState()
    val isFetchingLocation by viewModel.isFetchingLocation.collectAsState()
    val notesClientError by viewModel.notesClientError.collectAsState()

    val error by viewModel.error.collectAsState()
    val focusManager = LocalFocusManager.current
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual(
        fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
        viewModel: ClientesUpdateViewModel,
        context: android.content.Context
    ) {
        viewModel.setFetchinglocation(true)

        val priority = Priority.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateLocation(location.latitude, location.longitude)
                    Toast.makeText(context, "Ubicación precisa obtenida", Toast.LENGTH_SHORT).show()
                    viewModel.setFetchinglocation(false)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            lastLocation?.let {
                                viewModel.updateLocation(it.latitude, it.longitude)
                                Toast.makeText(context, "Ubicación aproximada obtenida", Toast.LENGTH_SHORT).show()
                                viewModel.setFetchinglocation(false)
                            } ?: Toast.makeText(
                                context,
                                "No se pudo obtener la ubicación. Activa el GPS y sal a cielo abierto",
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.setFetchinglocation(false)
                        }
                }
            }
    }

// Launcher para solicitar permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            // Si se conceden, obtenemos la ubicación
            obtenerUbicacionActual(fusedLocationClient, viewModel, context)
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
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
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),// Pone un borde en la imagen
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


                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChangeRegister(it) },
                    label = { Text(stringResource(R.string.email_register_cliente)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.TwoTone.Email, contentDescription = "Email Icon")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        showKeyboardOnFocus = true, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(if (emailError != null) 4.dp else 8.dp)) // Menos espacio si hay error

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
                    leadingIcon = {
                        Icon(Icons.TwoTone.LocationOn, contentDescription = "Location Icon")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        showKeyboardOnFocus = true, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    isError = locationError != null,
                    supportingText = {
                        locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = {
                        if (isFetchingLocation) {
                            // INDICADOR DE CARGA
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }) {
                                Icon(
                                    Icons.TwoTone.LocationSearching,
                                    contentDescription = "Map Icon",
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
                        viewModel.updateCliente(
                            clienteId = clienteId,
                            onUpdateComplete = {
                                onUpdateSuccess()
                            })
                    }),
                    isError = notesClientError != null,
                    supportingText = {
                        notesClientError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
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
                            clienteId = clienteId,
                            onUpdateComplete = {
                                onUpdateSuccess()
                            })
                    },
                    enabled = !isLoading && fullName.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && location.isNotBlank() && notasClient.isNotBlank()
                            && fullNameError == null && phoneError == null && emailError == null && locationError == null && notesClientError == null,
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