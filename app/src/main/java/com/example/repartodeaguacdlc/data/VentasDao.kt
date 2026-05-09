package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Transaction
import com.example.repartodeaguacdlc.model.DetalleVentaEntity
import com.example.repartodeaguacdlc.model.VentaEntity

@Dao
interface VentasDao {
    @Insert
    suspend fun insertVenta(venta: VentaEntity): Long

    @Insert
    suspend fun insertDetalleVenta(detalles: List<DetalleVentaEntity>)

    @Transaction
    suspend fun registrarVentaCompleta(venta: VentaEntity, detalles: List<DetalleVentaEntity>) {
        val idVenta = insertVenta(venta)
        val detallesConId = detalles.map { it.copy(ventaId = idVenta) }
        insertDetalleVenta(detallesConId)
    }
}