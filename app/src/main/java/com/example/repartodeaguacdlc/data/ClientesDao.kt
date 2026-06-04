package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.common.model.Clientes
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientesDao {

    // CREATE: Insertar un nuevo cliente
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliente(cliente: Clientes)

    // READ: Obtener todos los clientes (en tiempo real con Flow)
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun getAllClientes(): Flow<List<Clientes>>

    // READ: Obtener un cliente específico por ID
    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getClienteById(id: Int): Clientes?

    // UPDATE: Actualizar los datos de un cliente existente
    @Update
    suspend fun updateCliente(cliente: Clientes)

    // DELETE: Borrar un cliente
    @Delete
    suspend fun deleteCliente(cliente: Clientes)

    // Opcional: Borrar todos los clientes
    @Query("DELETE FROM clientes")
    suspend fun deleteAllClientes()
}