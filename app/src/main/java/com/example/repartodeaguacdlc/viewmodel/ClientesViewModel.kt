package com.example.repartodeaguacdlc.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.model.Clientes
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientesViewModel(
    private val clientesRepository: ClientesRepositoryRoom
): ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val searchResults: StateFlow<List<Clientes>> =
        combine(_searchText, clientesRepository.allClientes) { text, clientes ->
            if (text.isBlank()) {
                clientes
            } else {
                clientes.filter { cliente ->
                    cliente.nombre.contains(text, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedClient = MutableStateFlow<Clientes?>(null)
    val selectedClient: StateFlow<Clientes?> = _selectedClient.asStateFlow()

    private var selectedClientId: String? = null

    fun selectClient(id: String) {
        viewModelScope.launch {
            selectedClientId = id //Guardamos el Id del cliente seleccionado
            _isLoading.value = true
            try {
                _selectedClient.value = clientesRepository.getClienteById(id)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSelectedClient() {
        // Si tenemos un ID guardado, simplemente volvemos a llamar a selectClient con él.
        selectedClientId?.let { id ->
            selectClient(id)
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    suspend fun getClienteById(id: String): Clientes? {
        _isLoading.value = true
        return try {
            _isLoading.value = false
            clientesRepository.getClienteById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun startQRScanner(context: Context) {
        val scanner = GmsBarcodeScanning.getClient(context)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val qrContent = barcode.rawValue
                if (!qrContent.isNullOrBlank()) {
                    onSearchTextChanged(qrContent)
                    Toast.makeText(context, "QR Escaneado: $qrContent", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al escanear: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
    }
}