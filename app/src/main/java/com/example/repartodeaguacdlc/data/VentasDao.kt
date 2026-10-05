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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenta(venta: VentaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalleVenta(detalles: List<DetalleVentaEntity>)

    @Transaction
    suspend fun registrarVentaCompleta(venta: VentaEntity, detalles: List<DetalleVentaEntity>) {
        insertVenta(venta)
        insertDetalleVenta(detalles)
    }

    @Query("SELECT * FROM ventas WHERE isSynced = 0")
    suspend fun getUnsyncedVentas(): List<VentaEntity>

    @Query("SELECT * FROM detalle_ventas WHERE ventaId = :ventaId")
    suspend fun getDetallesForVenta(ventaId: String): List<DetalleVentaEntity>

    @Query("UPDATE ventas SET isSynced = 1 WHERE id = :ventaId")
    suspend fun markVentaAsSynced(ventaId: String)

    @Query("SELECT COUNT(*) FROM ventas WHERE routeId = :routeId AND fecha >= :inicioDia")
    fun getVentasHoyCount(routeId: String, inicioDia: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM ventas WHERE routeId = :routeId AND fecha >= :inicioDia")
    fun getIngresosHoy(routeId: String, inicioDia: Long): Flow<Double>

    @Query(""" 
    SELECT 
    v.id, 
    COALESCE(c.nombre, 'Cliente') AS nombreCliente,
    v.total, 
    v.fecha, 
    v.metodoPago,
    v.isSynced,
    COALESCE((SELECT SUM(cantidad) FROM detalle_ventas WHERE ventaId = v.id), 0) AS totalProductos 
    FROM ventas v 
    JOIN clientes c ON v.clienteId = c.id 
    WHERE v.routeId = :routeId AND v.fecha >= :inicioDia 
    ORDER BY v.fecha DESC 
    LIMIT 3 
    """)

    fun getUltimaVentaConDatos(routeId: String, inicioDia: Long): Flow<List<VentaConDatos>>
}