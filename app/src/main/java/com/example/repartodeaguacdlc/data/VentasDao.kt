package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.common.model.DetalleVentaEntity
import com.example.common.model.VentaConDatos
import com.example.common.model.VentaEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT COUNT(*) FROM ventas WHERE fecha >= :inicioDia")
    fun getVentasHoyCount(inicioDia: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM ventas WHERE fecha >= :inicioDia")
    fun getIngresosHoy(inicioDia: Long): Flow<Double>

    @Query(""" 
    SELECT 
    v.id, 
    c.nombre AS nombreCliente,
    v.total, 
    v.fecha, 
    COALESCE((SELECT SUM(cantidad) FROM detalle_ventas WHERE ventaId = v.id), 0) AS totalProductos 
    FROM ventas v 
    JOIN clientes c ON v.clienteId = c.id 
    WHERE v.fecha >= :inicioDia 
    ORDER BY v.fecha DESC 
    LIMIT 3 
    """)

    fun getUltimas3VentasConDatos(inicioDia: Long): Flow<List<VentaConDatos>>
}