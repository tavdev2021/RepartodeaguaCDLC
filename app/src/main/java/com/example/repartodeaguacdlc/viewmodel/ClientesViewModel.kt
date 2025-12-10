package com.example.repartodeaguacdlc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartodeaguacdlc.model.Clientes
import com.example.repartodeaguacdlc.repository.ClientesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ClientesViewModel: ViewModel() {

    private val clientesRepository = ClientesRepository()

    // 1. Mantenemos la lista COMPLETA de clientes en un StateFlow privado.
    // Esta será nuestra "fuente de la verdad".
    private val _allClientes = MutableStateFlow<List<Clientes>>(emptyList())

    // 2. (NUEVO) StateFlow para guardar el texto de búsqueda que el usuario escribe.
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    // 3. (MODIFICADO) Este StateFlow ahora contendrá la lista FILTRADA de clientes.
    // La UI observará este `searchResults` en lugar del StateFlow original.
    private val _searchResults = MutableStateFlow<List<Clientes>>(emptyList())
    val searchResults: StateFlow<List<Clientes>> = _searchResults

    init {
        // Cargamos la lista completa de clientes al iniciar el ViewModel.
        loadClientes()

        // 4. (NUEVO) Usamos `combine` para reaccionar a los cambios en la búsqueda o en la lista de clientes.
        // viewModelScope se asegura de que esta corrutina se cancele cuando el ViewModel se destruya.
        viewModelScope.launch {
            // `combine` se ejecutará cada vez que `_searchText` o `_allClientes` emitan un nuevo valor.
            combine(_searchText, _allClientes) { text, clientes ->
                if (text.isBlank()) {
                    // Si la búsqueda está vacía, mostramos la lista completa.
                    clientes
                } else {
                    // Si hay texto, filtramos la lista de clientes.
                    // La búsqueda no distingue mayúsculas/minúsculas.
                    clientes.filter { cliente ->
                        cliente.nombre.contains(text, ignoreCase = true)
                        // Puedes añadir más campos a la búsqueda si quieres:
                        // || cliente.direccion.contains(text, ignoreCase = true)
                    }
                }
            }.collect { clientesFiltrados ->
                // Actualizamos el StateFlow de resultados con la lista filtrada.
                _searchResults.value = clientesFiltrados
            }
        }
    }

    private fun loadClientes() {
        // Carga los clientes desde el repositorio a nuestra lista interna.
        _allClientes.value = clientesRepository.clientes
    }

    // 5. (NUEVO) Función para ser llamada desde la UI cuando el texto de búsqueda cambia.
    fun onSearchTextChanged(text: String) {
        // Actualiza el StateFlow con el nuevo texto de búsqueda.
        _searchText.value = text
    }
}