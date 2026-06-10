package com.example.repartodeaguacdlc.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.model.Clientes
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientesUpdateViewModel(private val clientesRepository: ClientesRepositoryRoom
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

    private val _emailRegisterClient = MutableStateFlow("")
    val emailRegisterClient: StateFlow<String> = _emailRegisterClient.asStateFlow()

    private val _locationClient = MutableStateFlow("")
    val locationClient: StateFlow<String> = _locationClient.asStateFlow()

    private val _notasClient = MutableStateFlow("")
    val notasClient: StateFlow<String> = _notasClient.asStateFlow()

    // Estados para los errores de los campos
    private val _fullNameError = MutableStateFlow<String?>(null)
    val fullNameError: StateFlow<String?> = _fullNameError.asStateFlow()
    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()
    private val _emailErrorRegister = MutableStateFlow<String?>(null)
    val emailErrorRegister: StateFlow<String?> = _emailErrorRegister.asStateFlow()
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()
    private val _notesClientError = MutableStateFlow<String?>(null)
    val notesClientError: StateFlow<String?> = _notesClientError.asStateFlow()


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

    fun onEmailChangeRegister(newEmailRegister: String) {
        _emailRegisterClient.value = newEmailRegister
        _emailErrorRegister.value = validateEmail(newEmailRegister)
    }

    fun onLocationChangeClient(newLocation: String) {
        _locationClient.value = newLocation
        _locationError.value = validateLocationClient(newLocation)
    }

    fun onNotesClientChange(addNotesClient: String) {
        _notasClient.value = addNotesClient
        _notesClientError.value = validateNotasClient(addNotesClient)
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

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "El email no puede estar vacío."
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Introduce un formato de email válido."
        }
        return null // Válido
    }

    private fun validateLocationClient(location: String): String? {
        if (location.isBlank()) {
            return "La ubicación no puede estar vacía."
        }
        return null // Válido
    }

    private fun validateNotasClient(notas: String): String? {
        if (notas.isBlank()) {
            return "Las notas no pueden estar vacías."
        }
        return null // Válido
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _locationClient.value = "$latitude, $longitude"
    }

    private fun validateRegisterForm(): Boolean {
        val isFullNameValid = validateFullName(_fullName.value) == null
        val isPhoneValid = validatePhoneNumber(_phone.value) == null
        val isEmailValid = validateEmail(_emailRegisterClient.value) == null
        val isLocationValid = validateLocationClient(_locationClient.value) == null
        val isNotesValid = validateNotasClient(_notasClient.value) == null

        // Actualizar todos los errores para mostrarlos en la UI si es necesario
        _fullNameError.value = validateFullName(_fullName.value)
        _phoneError.value = validatePhoneNumber(_phone.value)
        _emailErrorRegister.value = validateEmail(_emailRegisterClient.value)
        _locationError.value = validateLocationClient(_locationClient.value)
        _notesClientError.value = validateNotasClient(_notasClient.value)

        return isFullNameValid && isPhoneValid && isEmailValid && isLocationValid && isNotesValid
    }

    fun updateCliente(clienteId: String, onUpdateComplete: () -> Unit) {
        if (!validateRegisterForm())
        // No intentar la actualizacion si hay errores de validación
            return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            try {
                delay(1000)
                // 1. Crear el objeto Cliente
                val updatedCliente = Clientes(
                    id = clienteId,
                    nombre = _fullName.value,
                    telefono = _phone.value,
                    email = _emailRegisterClient.value,
                    ubicacion = _locationClient.value,
                    notas = _notasClient.value,
                    imagenUrl = "https://ui-avatars.com/api/?name=${_fullName.value}&size=512&length=3",
                    fechaRegistro = System.currentTimeMillis()
                )

                // 2. Guardar en Room (Local)
                clientesRepository.updateCliente(updatedCliente)


                // 3. Éxito: Mostrar mensaje de éxito
                _isSuccess.value = true
                onUpdateComplete()

            } catch (e: Exception) {
                _error.value = "Error al guardar localmente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadClientData(cliente: Clientes?) {
        cliente?.let {
            _fullName.value = it.nombre
            _phone.value = it.telefono
            _emailRegisterClient.value = it.email
            _locationClient.value = it.ubicacion
            _notasClient.value = it.notas
            // Limpia los errores al cargar un nuevo cliente
            clearErrorRegister()
        }
    }

    fun deleteClient(cliente: Clientes) {
        viewModelScope.launch {
            clientesRepository.deleteCliente(cliente)
        }
    }

    fun resetSuccess() {
        _isSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun clearInputsRegister() {
        _fullName.value = ""
        _phone.value = ""
        _emailRegisterClient.value = ""
        _locationClient.value = ""
        _notasClient.value = ""
    }

    fun clearErrorRegister() {
        _fullNameError.value = null
        _phoneError.value = null
        _emailErrorRegister.value = null
        _locationError.value = null
        _notesClientError.value = null
    }
}