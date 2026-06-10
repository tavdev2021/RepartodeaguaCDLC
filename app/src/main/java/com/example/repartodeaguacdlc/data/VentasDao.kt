package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.common.model.DetalleVentaEntity
import com.example.common.model.VentaEntity

@Dao
interface VentasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertVenta(venta: VentaEntity)

    @Insert
    suspend fun insertDetalleVenta(detalles: List<DetalleVentaEntity>)

    @Transaction
    suspend fun registrarVentaCompleta(venta: VentaEntity, detalles: List<DetalleVentaEntity>) {
        insertVenta(venta)
        insertDetalleVenta(detalles)
    }
}