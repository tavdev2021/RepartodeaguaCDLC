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
    @Query("SELECT * FROM clientes WHERE routeId = :routeId AND activo = 1 ORDER BY nombre ASC")
    fun getAllClientes(routeId: String): Flow<List<Clientes>>

    @Query("UPDATE clientes SET activo = :activo, isSynced = 0, ultimaActualizacion = :timestamp WHERE id = :clienteId")
    suspend fun cambiarEstadoCliente(clienteId: String, activo: Boolean, timestamp: Long = System.currentTimeMillis())

    // READ: Obtener un cliente específico por ID
    @Query("SELECT * FROM clientes WHERE id = :id")
    fun getClienteByIdFlow(id: String): Flow<Clientes?>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getClienteById(id: String): Clientes?

    // Para que el Worker sepa a quiénes les falta la dirección
    @Query("SELECT * FROM clientes WHERE direccion = '' OR direccion IS NULL")
    suspend fun getClientesSinDireccion(): List<Clientes>

    // UPDATE: Actualizar los datos de un cliente existente
    @Update
    suspend fun updateCliente(cliente: Clientes)

    // DELETE: Borrar un cliente
    @Delete
    suspend fun deleteCliente(cliente: Clientes)

    // Opcional: Borrar todos los clientes
    @Query("DELETE FROM clientes")
    suspend fun deleteAllClientes()

    //Buscamos en la DB local todo lo que tenga isSynced = 0 (false "No sincronizado")
    @Query("SELECT * FROM clientes WHERE isSynced = 0")
    suspend fun getUnsyncedClientes(): List<Clientes>

    //Una vez que Firebase confirma que recibio los datos de los clientes no sincronizados, marcamos como sincronizado en Room
    @Query("UPDATE clientes SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    //
}