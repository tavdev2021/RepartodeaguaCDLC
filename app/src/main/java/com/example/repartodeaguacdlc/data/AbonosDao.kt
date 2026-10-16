package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.common.model.AbonoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AbonosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbono(abono: AbonoEntity)

    @Query("SELECT * FROM abonos WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun getAbonosPorCliente(clienteId: String): Flow<List<AbonoEntity>>

    @Query("SELECT * FROM abonos WHERE isSynced = 0")
    suspend fun getUnsyncedAbonos(): List<AbonoEntity>

    @Query("UPDATE abonos SET isSynced = 1 WHERE id = :id")
    suspend fun markAbonoAsSynced(id: String)

    // Suma todos los abonos en efectivo recibidos en el dia para el corte de caja del repartidor
    @Query("SELECT COALESCE(SUM(montoAbono), 0.0) FROM abonos WHERE fecha >= :inicioDia and montoAbono > 0")
    fun getAbonosRecibidosHoy(inicioDia: Long): Flow<Double>
}