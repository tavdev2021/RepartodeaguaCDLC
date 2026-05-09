package com.example.repartodeaguacdlc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Int,
    val fecha: Long, // System.currentTimeMillis()
    val total: Double,
    val metodoPago: String
)

@Entity(tableName = "detalle_ventas")
data class DetalleVentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Double
)
