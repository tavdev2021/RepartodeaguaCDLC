package com.example.common.viewmodel

import android.util.Patterns
import com.example.common.model.UserProfile
import com.example.common.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.time.Duration.Companion.milliseconds

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
): ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(repository.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow(repository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _emailLogin = MutableStateFlow("")
    val emailLogin: StateFlow<String> = _emailLogin.asStateFlow()

    private val _emailRegister = MutableStateFlow("")
    val emailRegister: StateFlow<String> = _emailRegister.asStateFlow()

    private val _passwordLogin = MutableStateFlow("")
    val passwordLogin: StateFlow<String> = _passwordLogin.asStateFlow()

    private val _passwordRegister = MutableStateFlow("")
    val passwordRegister: StateFlow<String> = _passwordRegister.asStateFlow()

    private val _confirmPasswordRegister = MutableStateFlow("")
    val confirmPasswordRegister: StateFlow<String> = _confirmPasswordRegister.asStateFlow()

    private val _rutaAsignada = MutableStateFlow("")
    val rutaAsignada: StateFlow<String> = _rutaAsignada.asStateFlow()

    private val _availableRoutes = MutableStateFlow<List<String>>(emptyList())
    val availableRoutes: StateFlow<List<String>> = _availableRoutes.asStateFlow()

    private val _rolAsignado = MutableStateFlow("")
    val rolAsignado: StateFlow<String> = _rolAsignado.asStateFlow()

    private val _availableRoles = MutableStateFlow<List<String>>(emptyList())
    val availableRoles: StateFlow<List<String>> = _availableRoles.asStateFlow()

    // Estados para los errores de los campos
    private val _fullNameError = MutableStateFlow<String?>(null)
    val fullNameError: StateFlow<String?> = _fullNameError.asStateFlow()

    private val _emailErrorLogin = MutableStateFlow<String?>(null)
    val emailErrorLogin: StateFlow<String?> = _emailErrorLogin.asStateFlow()

    private val _emailErrorRegister = MutableStateFlow<String?>(null)
    val emailErrorRegister: StateFlow<String?> = _emailErrorRegister.asStateFlow()

    private val _passwordErrorLogin = MutableStateFlow<String?>(null)
    val passwordErrorLogin: StateFlow<String?> = _passwordErrorLogin.asStateFlow()

    private val _passwordErrorRegister = MutableStateFlow<String?>(null)
    val passwordErrorRegister: StateFlow<String?> = _passwordErrorRegister.asStateFlow()

    private val _confirmPasswordErrorRegister = MutableStateFlow<String?>(null)
    val confirmPasswordErrorRegister: StateFlow<String?> = _confirmPasswordErrorRegister.asStateFlow()

    private val _rutaAsignadaError = MutableStateFlow<String?>(null)
    val rutaAsignadaError: StateFlow<String?> = _rutaAsignadaError.asStateFlow()

    private val _rolAsignadoError = MutableStateFlow<String?>(null)
    val rolAsignadoError: StateFlow<String?> = _rolAsignadoError.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<AuthEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    sealed class AuthEvent {
        object NavigateToHome : AuthEvent()
        object NavigateToLogin : AuthEvent()
    }


    init {

        fetchAvailableRoutes()
        fetchAvailableRoles()

        viewModelScope.launch {
            repository.getAuthState().collect { loggedIn ->
                _isAuthenticated.value = loggedIn
                _currentUser.value = repository.currentUser

                if(loggedIn) {
                    fetchUserData()
                }else{
                    _isLoading.value = false
                    _userProfile.value = null
                }

            }
        }
    }

    // --- Funciones para actualizar los campos desde la UI
    fun onFullNameChange(newName: String) {
        _fullName.value = newName
        _fullNameError.value = validateFullName(newName)
    }

    fun onEmailChangeLogin(newEmailLogin: String) {
        _emailLogin.value = newEmailLogin
        _emailErrorLogin.value = validateEmail(newEmailLogin)
    }

    fun onEmailChangeRegister(newEmailRegister: String) {
        _emailRegister.value = newEmailRegister
        _emailErrorRegister.value = validateEmail(newEmailRegister)
    }

    fun onPasswordChangeLogin(newPasswordLogin: String) {
        _passwordLogin.value = newPasswordLogin
        _passwordErrorLogin.value = validatePasswordLogin(newPasswordLogin)
    }

    fun onPasswordChangeRegister(newPasswordRegister: String) {
        _passwordRegister.value = newPasswordRegister
        _passwordErrorRegister.value = validatePasswordRegister(newPasswordRegister, _confirmPasswordRegister.value) // Pasar confirmPassword
        _confirmPasswordErrorRegister.value = validateConfirmPassword(newPasswordRegister, _confirmPasswordRegister.value)
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPasswordRegister.value = newConfirmPassword
        _confirmPasswordErrorRegister.value = validateConfirmPassword(_passwordRegister.value, newConfirmPassword)
        // También es buena idea re-validar el error de la contraseña original si esta cambia
        // por si la confirmación ahora es válida/inválida.
        if (_passwordErrorRegister.value?.contains("coinciden") == true || newConfirmPassword == _passwordRegister.value) {
            _passwordErrorRegister.value = validatePasswordRegister(_passwordRegister.value, newConfirmPassword) // Necesitarías una versión de validatePassword que acepte confirmación
        }
    }

    fun onRutaAsignadaChange(newRutaAsignada: String) {
        _rutaAsignada.value = newRutaAsignada
        _rutaAsignadaError.value = validateRutaAsignada(newRutaAsignada)
    }

    fun onRolAsignadoChange(newRolAsignado: String) {
        _rolAsignado.value = newRolAsignado
        _rolAsignadoError.value = validateRolAsignado(newRolAsignado)

        // Lógica condicional para la ruta
        if (newRolAsignado == "Administrador") {
            _rutaAsignada.value = "S/R" // "Sin Ruta" o "Administración"
            _rutaAsignadaError.value = null // Quitamos el error ya que el valor es válido
        } else {
            // Si vuelve a Repartidor, limpiamos para obligar a seleccionar una
            _rutaAsignada.value = ""
        }
    }

    // --- NUEVO: Función para obtener rutas (por ahora estática) ---
    private fun fetchAvailableRoutes() {
        // En el futuro, aquí harás una llamada a repository o Firestore
        _availableRoutes.value = listOf(
            "Arroyo Grande - Centro",
            "Arroyo Grande - Sur",
            "La Laja - El Timbinal",
            "La Cienega",
            "La Cañada - El pinzan"
        )
    }

    private fun fetchAvailableRoles() {
        _availableRoles.value = listOf(
            "Repartidor",
            "Administrador"
        )
    }

    // --- Funciones de Validación ---
    private fun validateFullName(fullName: String): String? {
        if (fullName.isBlank()) {
            return "El nombre no puede estar vacío."
        } else if (fullName.length < 5) {
            return "El nombre debe tener al menos 5 caracteres."
        } else if (!fullName.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
            return "El nombre solo puede contener letras y espacios."
        }
        return null // Válido
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "El email no puede estar vacío."
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Introduce un formato de email válido."
        }
        return null // Válido
    }

    private fun validatePasswordLogin(password: String): String? {
        if (password.isBlank()) {
            return "La contraseña no puede estar vacía."
        }
        return null
    }

    private fun validatePasswordRegister(password: String, confirmPasswordValue: String? = null): String? {
        if (password.isBlank()) {
            return "La contraseña no puede estar vacía."
        } else if (password.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres."
        } else if (!password.matches(Regex(".*[A-Z].*"))) {
            return "La contraseña debe contener al menos una letra mayúscula."
        } else if (!password.matches(Regex(".*[a-z].*"))) {
            return "La contraseña debe contener al menos una letra minúscula."
        } else if (!password.matches(Regex(".*\\d.*"))) {
            return "La contraseña debe contener al menos un número."
        } else if (!password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))) {
            return "La contraseña debe contener al menos un carácter especial (ej: !@#\\$%)."
        }
        // Si confirmPasswordValue no es null y no está vacío, y no coincide, añade error
        // Esto se puede hacer más elegante, pero es un ejemplo
        if (!confirmPasswordValue.isNullOrEmpty() && password != confirmPasswordValue) {
            // Podríamos devolver un error específico o dejar que validateConfirmPassword lo maneje.
            // Por ahora, solo validamos la contraseña en sí.
            // La validación de coincidencia se hará en validateConfirmPassword.
        }
        return null
    }

    private fun validateConfirmPassword(passwordValue: String, confirmPasswordValue: String): String? {
        if (confirmPasswordValue.isBlank()) {
            return "Confirma tu contraseña."
        }else if (passwordValue != confirmPasswordValue) {
            return "Las contraseñas no coinciden."
        }
        return null
    }

    private fun validateRutaAsignada(rutaAsignada: String): String? {
        if (rutaAsignada.isBlank()) {
            return "La ruta no puede estar vacía."
        }
        return null
    }

    private fun validateRolAsignado(rolAsignado: String): String? {
        if (rolAsignado.isBlank()) {
            return "La ruta no puede estar vacía."
        }
        return null
    }

    private fun validateLoginForm(): Boolean {
        // Ejecutar todas las validaciones y actualizar los errores
        // Esto es útil si el usuario no ha interactuado con todos los campos
        // pero intenta hacer login.
        val isEmailValid = validateEmail(_emailLogin.value) == null
        val isPasswordValid = validatePasswordLogin(_passwordLogin.value) == null

        _emailErrorLogin.value = validateEmail(_emailLogin.value) // Actualiza el error por si acaso
        _passwordErrorLogin.value = validatePasswordLogin(_passwordLogin.value) // Actualiza el error

        return isEmailValid && isPasswordValid
    }

    private fun validateRegisterForm(): Boolean {
        val isFullNameValid = validateFullName(_fullName.value) == null
        val isEmailValid = validateEmail(_emailRegister.value) == null
        val isPasswordValid = validatePasswordRegister(_passwordRegister.value, _confirmPasswordRegister.value) == null
        val isConfirmPasswordValid = validateConfirmPassword(_passwordRegister.value, _confirmPasswordRegister.value) == null

        // Actualizar todos los errores para mostrarlos en la UI si es necesario
        _fullNameError.value = validateFullName(_fullName.value)
        _emailErrorRegister.value = validateEmail(_emailRegister.value)
        _passwordErrorRegister.value = validatePasswordRegister(_passwordRegister.value, _confirmPasswordRegister.value)
        _confirmPasswordErrorRegister.value = validateConfirmPassword(_passwordRegister.value, _confirmPasswordRegister.value)

        return isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
    }

    fun login(expectedRole: String) {
    if (!validateLoginForm()) {
            // No intentar el login si hay errores de validación
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(_emailLogin.value, _passwordLogin.value)

            result.onSuccess {
                val user = repository.currentUser
                val profile = repository.getUserProfile(user?.uid ?: "")

                if (profile?.role == expectedRole) {
                    // Éxito: El rol coincide
                    _userProfile.value = profile
                    _isAuthenticated.value = true
                    _navigationEvent.emit(AuthEvent.NavigateToHome)
                } else {
                    // Error: El usuario existe pero no tiene el permiso para esta app
                    repository.logout() // Lo sacamos de Firebase Auth
                    _error.value = "No tienes permisos para acceder a esta aplicación."
                    _isAuthenticated.value = false
                }
            }.onFailure {
                _error.value = it.message ?: "Error desconocido durante el login"
            }
            _isLoading.value = false
            _passwordLogin.value = ""}
            _currentUser.value = repository.currentUser
        }

    fun register() {
        if (!validateRegisterForm()) {
            // No intentar el registro si hay errores de validación
            return
        }
        viewModelScope.launch {
            _isLoading.value = true

            val imagenUrl = "https://ui-avatars.com/api/?name=${_fullName.value}&size=512&background=random&length=2"

            val result = repository.register(_fullName.value,_emailRegister.value, _passwordRegister.value, imagenUrl = imagenUrl, _rutaAsignada.value, _rolAsignado.value)
            result.onSuccess {
                // 1. Forzar la actualización del usuario actual con los datos nuevos
                _currentUser.value = repository.currentUser

                // 2. Cargar los datos de Firestore inmediatamente
                fetchUserData()

                // 3. Marcar como autenticado
                _isAuthenticated.value = true
            }.onFailure {
                _error.value = it.message ?: "Error desconocido durante el registro"
            _passwordRegister.value = ""
            _confirmPasswordRegister.value = ""
            }
            _isLoading.value = false
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            val uid = repository.currentUser?.uid
            if (uid != null) {
                // 👇 Usamos el método que trae el perfil completo (con rol) desde Firestore
                val profile = repository.getUserProfile(uid)
                _userProfile.value = profile
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000.milliseconds)
            try {
                repository.logout()
                clearInputsLogin()
                clearInputsRegister()
                clearErrorLogin()
                clearErrorRegister()
                _currentUser.value = null
                _userProfile.value = null
                _isAuthenticated.value = false
                _isLoading.value = false
                _navigationEvent.emit(AuthEvent.NavigateToLogin)
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError(){
        _error.value = null
    }

    fun clearInputsLogin(){
        _emailLogin.value = ""
        _passwordLogin.value = ""
    }

    fun clearErrorLogin(){
        _emailErrorLogin.value = null
        _passwordErrorLogin.value = null
    }

    fun clearInputsRegister(){
        _fullName.value = ""
        _emailRegister.value = ""
        _passwordRegister.value = ""
        _confirmPasswordRegister.value = ""
        _rutaAsignada.value = ""
    }

    fun clearErrorRegister(){
        _fullNameError.value = null
        _emailErrorRegister.value = null
        _passwordErrorRegister.value = null
        _confirmPasswordErrorRegister.value = null
        _rutaAsignadaError.value = null
    }
}