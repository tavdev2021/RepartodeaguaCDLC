package com.example.repartodeaguacdlc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.model.Clientes
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.emptyList

class ClientesViewModel(
    private val clientesRepository: ClientesRepositoryRoom
): ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _routeId = MutableStateFlow("")

    fun setRouteId(routeId: String) {
        _routeId.value = routeId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Clientes>> = combine(_searchText, _routeId) { text, routeId ->
        text to routeId
    }.flatMapLatest { (text, routeId) ->
        if (routeId.isBlank()) {
            flowOf(emptyList())
        } else {
            clientesRepository.getAllClientes(routeId)
                .onEach {
                    _isLoading.value = false
                }
                .map { clientes ->

                    if (text.isBlank()) clientes
                    else
                        clientes.filter { cliente ->
                            cliente.nombre.contains(text, ignoreCase = true) ||
                            cliente.telefono.contains(text, ignoreCase = true) ||
                            cliente.id.contains(text, ignoreCase = true)
                        }
                }
        }
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedClientId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedClient: StateFlow<Clientes?> = _selectedClientId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                clientesRepository.getClienteByIdFlow(id)
            }
        }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
        )

    //private var selectedClientId: String? = null

    fun selectClient(id: String) {
            _selectedClientId.value = id //Guardamos el Id del cliente seleccionado
        }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    fun startQRScanner(qrContent: String?) {

        if (!qrContent.isNullOrBlank()) {
            onSearchTextChanged(qrContent)
        }
    }

    fun iniciarSincronizacion(routeId: String) {
            clientesRepository.iniciarSincronizacionContinua(routeId)
    }

    fun activarRespaldoPendiente() {
        clientesRepository.scheduleSync()
    }
}
