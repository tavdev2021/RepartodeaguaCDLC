package com.example.admin_app.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin_app.R
import com.example.common.view.LoadingOverlay
import com.example.common.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onBackNavigation: () -> Unit
) {

    val context = LocalContext.current
    val availableRoutes by viewModel.availableRoutes.collectAsStateWithLifecycle()

    // Observar los valores de los campos desde el ViewModel
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val email by viewModel.emailRegister.collectAsStateWithLifecycle()
    val password by viewModel.passwordRegister.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPasswordRegister.collectAsStateWithLifecycle()
    val rutaAsignada by viewModel.rutaAsignada.collectAsStateWithLifecycle()
    val routeIdSelected by viewModel.routeIdSelected.collectAsStateWithLifecycle()

    // Observar los errores de los campos desde el ViewModel
    val fullNameError by viewModel.fullNameError.collectAsStateWithLifecycle()
    val emailError by viewModel.emailErrorRegister.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordErrorRegister.collectAsStateWithLifecycle()
    val confirmPasswordError by viewModel.confirmPasswordErrorRegister.collectAsStateWithLifecycle()
    val rutaAsignadaError by viewModel.rutaAsignadaError.collectAsStateWithLifecycle()


    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var expandedRuta by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                title = { Text(stringResource(R.string.appbar_crearcuenta_register)) },
                navigationIcon = {
                    IconButton(onClick = onBackNavigation) {
                        Icon(Icons.AutoMirrored.TwoTone.ArrowBack, contentDescription = "Volver a Login")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                stringResource(R.string.ingresa_datos_register),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { viewModel.onFullNameChange(it) },
                label = { Text(stringResource(R.string.nombre_completo_register)) },
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

            Spacer(modifier = Modifier.height(if (fullNameError != null) 8.dp else 16.dp)) // Menos espacio si hay error


            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChangeRegister(it) },
                label = { Text(stringResource(R.string.email_register)) },
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

            Spacer(modifier = Modifier.height(if (emailError != null) 8.dp else 16.dp)) // Menos espacio si hay error

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChangeRegister(it) },
                label = { Text(stringResource(R.string.contrasena_register)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.TwoTone.Lock, contentDescription = "Password Icon")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    showKeyboardOnFocus = true, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(R.drawable.visibility_on)
                    else (painterResource(R.drawable.visibility_off))
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, description)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(if (passwordError != null) 8.dp else 16.dp)) // Menos espacio si hay error

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text(stringResource(R.string.confirmar_contrasena)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.TwoTone.Lock, contentDescription = "Password Icon")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    showKeyboardOnFocus = true, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                isError = confirmPasswordError != null,
                supportingText = {
                    confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible)
                        painterResource(R.drawable.visibility_on)
                    else (painterResource(R.drawable.visibility_off))
                    val description =
                        if (confirmPasswordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(painter = image, description)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(if (confirmPasswordError != null) 8.dp else 16.dp)) // Menos espacio si hay error


            ExposedDropdownMenuBox(
                expanded = expandedRuta,
                onExpandedChange = { expandedRuta = !expandedRuta },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = rutaAsignada,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Ruta Asignada") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    leadingIcon = { Icon(Icons.TwoTone.Route, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRuta) },
                    isError = rutaAsignadaError != null,
                    supportingText = {
                        rutaAsignadaError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                ExposedDropdownMenu(
                    expanded = expandedRuta,
                    onDismissRequest = { expandedRuta = false }
                ) {
                    availableRoutes.forEach { route ->
                        DropdownMenuItem(
                            text = { Text(route.nombre) },
                            onClick = {
                                viewModel.onRouteSelected(route)
                                expandedRuta = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (rutaAsignadaError != null) 8.dp else 16.dp)) // Menos espacio si hay error

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registerUserByAdmin(
                        context = context,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Repartidor registrado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            onRegisterSuccess()
                        }
                    )
                },
                enabled = !isLoading && routeIdSelected.isNotBlank() && fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && rutaAsignada.isNotBlank() && fullNameError == null && emailError == null && passwordError == null && confirmPasswordError == null && rutaAsignadaError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {

                Text(stringResource(R.string.button_register), fontSize = 16.sp)
            }
        }
    }

    // Overlay con blur elegante
    LoadingOverlay(
        visible = isLoading,
        message = stringResource(R.string.loading_registrar_usuario)
    )
}