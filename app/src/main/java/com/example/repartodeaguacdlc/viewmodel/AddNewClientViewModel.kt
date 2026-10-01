package com.example.repartodeaguacdlc.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.model.Clientes
import com.example.common.util.LocationHelper
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import com.example.repartodeaguacdlc.util.SyncManager.programarSincronizacionDireccion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AddNewClientViewModel(
    private val clientesRepository: ClientesRepositoryRoom
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _locationClient = MutableStateFlow("")
    val locationClient: StateFlow<String> = _locationClient.asStateFlow()

    private val _notasClient = MutableStateFlow("")
    val notasClient: StateFlow<String> = _notasClient.asStateFlow()

    // Estados para los errores de los campos
    private val _fullNameError = MutableStateFlow<String?>(null)
    val fullNameError: StateFlow<String?> = _fullNameError.asStateFlow()
    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()


    // --- Funciones para actualizar los campos desde la UI
    fun onFullNameChange(newName: String) {
        _fullName.value = newName
        _fullNameError.value = validateFullName(newName)
    }

    fun onPhoneNumberChange(newPhone: String) {

        //Filtra para que solo sean digitos
        val digitsOnly = newPhone.replace(Regex("[^0-9]"), "")

        //Limita a 10 digitos
        val newPhone = digitsOnly.take(10)
        _phone.value = newPhone
        _phoneError.value = validatePhoneNumber(newPhone)
    }

    fun onLocationChangeClient(newLocation: String) {
        _locationClient.value = newLocation
        _locationError.value = validateLocationClient(newLocation)
    }

    fun onNotesClientChange(addNotesClient: String) {
        _notasClient.value = addNotesClient
    }

    // --- Funciones de Validación ---
    private fun validateFullName(fullName: String): String? {
        if (fullName.isBlank()) {
            return "El nombre no puede estar vacío."
        } else if (fullName.length < 3) {
            return "El nombre debe tener al menos 3 caracteres."
        } else if (!fullName.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
            return "El nombre solo puede contener letras y espacios."
        }
        return null // Válido
    }

    private fun validatePhoneNumber(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) {
            return "El numero de telefono no puede estar vacío."
        } else if (phoneNumber.length !in 10..10) {
            return "El numero de telefono debe tener 10 caracteres."
        } else if (!phoneNumber.matches(Regex("^[0-9]+$"))) {
            return "El numero de telefono solo puede contener digitos numericos"
        }
        return null // Válido
    }

    private fun validateLocationClient(location: String): String? {
        if (location.isBlank()) {
            return "La ubicación no puede estar vacía."
        }
        return null // Válido
    }
    
    fun updateLocation(latitude: Double, longitude: Double) {
        _locationClient.value = "$latitude, $longitude"
    }
    private fun validateRegisterForm(): Boolean {
        val isFullNameValid = validateFullName(_fullName.value) == null
        val isPhoneValid = validatePhoneNumber(_phone.value) == null
        val isLocationValid = validateLocationClient(_locationClient.value) == null

        // Actualizar todos los errores para mostrarlos en la UI si es necesario
        _fullNameError.value = validateFullName(_fullName.value)
        _phoneError.value = validatePhoneNumber(_phone.value)
        _locationError.value = validateLocationClient(_locationClient.value)

        return isFullNameValid && isPhoneValid && isLocationValid
    }

    fun registerClient(context: Context, routeId: String) {
        if (!validateRegisterForm())
            // No intentar el registro si hay errores de validación
            return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            try {
                // 1. Intento inmediato de traducir coordenadas a dirección (Geocodificación)
                val direccionTraducida = LocationHelper.obtenerDireccionLegible(context, _locationClient.value)
                delay(1.seconds)
                // 2. Crear el objeto Cliente
                val nuevoCliente = Clientes(
                    nombre = _fullName.value,
                    routeId = routeId,
                    telefono = _phone.value,
                    ubicacion = _locationClient.value,
                    // Si hubo internet, guardamos la dirección. Si no, queda vacío ("").
                    direccion = if (direccionTraducida != _locationClient.value) direccionTraducida else "",
                    notas = _notasClient.value.ifBlank { "Sin notas" },
                    imagenUrl = "https://ui-avatars.com/api/?name=${_fullName.value}&size=512&background=E3F2FD&color=1976D2&bold=true&length=2",
                    fechaRegistro = System.currentTimeMillis(),
                    activo = true
                )

                // 3. Guardar en Room (Local)
                clientesRepository.insertCliente(nuevoCliente)

                // 4. Si falló la dirección inicial (sin internet), programamos WorkManager
                if (nuevoCliente.direccion.isEmpty()) {
                    programarSincronizacionDireccion(context)
                }

                // 3. Éxito: Limpiar los campos y errores de validación
                clearInputsRegister()
                clearErrorRegister()

                // 4. Éxito: Mostrar mensaje de éxito
                _isSuccess.value = true

            } catch (e: Exception) {
                _error.value = "Error al guardar localmente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _isSuccess.value = false
    }
    fun clearError(){
        _error.value = null
    }

    fun clearInputsRegister(){
        _fullName.value = ""
        _phone.value = ""
        _locationClient.value = ""
        _notasClient.value = ""
    }

    fun clearErrorRegister(){
        _fullNameError.value = null
        _phoneError.value = null
        _locationError.value = null
    }
}