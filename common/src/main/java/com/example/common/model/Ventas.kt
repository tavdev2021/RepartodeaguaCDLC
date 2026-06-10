package com.example.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val clienteId: String,
    val fecha: Long,
    val total: Double,
    val metodoPago: String,

    // Campos para el SyncWorker
    val isSynced: Boolean = false,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "detalle_ventas")
data class DetalleVentaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ventaId: String,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Double
)
