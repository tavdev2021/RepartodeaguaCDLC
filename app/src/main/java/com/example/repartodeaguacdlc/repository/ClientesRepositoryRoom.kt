package com.example.repartodeaguacdlc.repository

import com.example.repartodeaguacdlc.data.ClientesDao
import com.example.common.model.Clientes
import kotlinx.coroutines.flow.Flow

class ClientesRepositoryRoom(private val clientesDao: ClientesDao) {

    // Exponer el flujo de clientes (Read All)
    val allClientes: Flow<List<Clientes>> = clientesDao.getAllClientes()

    // Obtener Cliente por ID
    suspend fun getClienteById(id: Int): Clientes? {
        return clientesDao.getClienteById(id)
    }

    // Crear Cliente
    suspend fun insertCliente(cliente: Clientes) {
        clientesDao.insertCliente(cliente)
    }

    // Actualizar Cliente
    suspend fun updateCliente(cliente: Clientes){
        clientesDao.updateCliente(cliente)
    }

    // Eliminar Cliente
    suspend fun deleteCliente(cliente: Clientes){
        clientesDao.deleteCliente(cliente)
    }

    // Eliminar todos los clientes
    suspend fun deleteAllClientes(){
        clientesDao.deleteAllClientes()
    }
}
