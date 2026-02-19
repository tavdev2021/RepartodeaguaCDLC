package com.example.repartodeaguacdlc.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartodeaguacdlc.model.Clientes
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ClientesViewModel(
    val clientesRepository: ClientesRepositoryRoom
): ViewModel() {

    // 2. Texto de búsqueda.
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 3. RESULTADOS FILTRADOS:
    // Usamos 'combine' directamente para exponer el StateFlow.
    // Si 'searchText' está vacío, devolverá 'allClientes' (la lista completa).
    val searchResults: StateFlow<List<Clientes>> = combine(_searchText, clientesRepository.allClientes) { text, clientes ->
        if (text.isBlank()) {
            // Si la búsqueda está vacía, mostramos la lista completa.
            clientes
        } else {
            // Si hay texto, filtramos la lista de clientes
            // La búsqueda no distingue mayúsculas/minúsculas.
            clientes.filter { cliente ->
                cliente.nombre.contains(text, ignoreCase = true)
                // Opcional: || cliente.direccion.contains(text, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Mantiene el flujo activo 5 s tras cerrar la pantalla
        initialValue = emptyList()
    )

    // Función que llamarás desde el OutlinedTextField / SearchBar en tu UI
    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    suspend fun getClienteById(id: Int): Clientes? {
        _isLoading.value = true
        delay(500)
        return try {
            _isLoading.value = false
            clientesRepository.getClienteById(id)
        } catch (
            e: Exception
        ) {
            // Manejar errores
            null
        }
    }


    /**
     * Inicia el escáner de Google Play Services para obtener un código QR.
     * @parametro context El contexto de la Activity/Composable necesario para el scanner.
     */
    fun startQRScanner(context: Context) {
        val scanner = GmsBarcodeScanning.getClient(context)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // barcode.rawValue contiene el texto del código QR
                val qrContent = barcode.rawValue
                if (!qrContent.isNullOrBlank()) {
                    // Actualizamos el texto de búsqueda con el contenido del QR
                    onSearchTextChanged(qrContent)
                    Toast.makeText(context, "QR Escaneado: $qrContent", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al escanear: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                // El usuario canceló el escaneo
                Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
    }
}