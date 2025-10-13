package com.example.repartodeaguacdlc.viewmodel

import androidx.lifecycle.ViewModel
import com.example.repartodeaguacdlc.model.Clientes
import com.example.repartodeaguacdlc.repository.ClientesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ClientesViewModel: ViewModel()  {

    private val clientesRepository = ClientesRepository()

    private val _clientes = MutableStateFlow<List<Clientes>>(emptyList())
    val clientes : StateFlow<List<Clientes>> = _clientes

    init {
        loadClientes()
    }

    private fun loadClientes(){
        _clientes.value = clientesRepository.clientes

    }
}