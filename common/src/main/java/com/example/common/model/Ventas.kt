package com.example.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val clienteId: String = "",
    val routeId: String = "",
    val fecha: Long = 0L,
    val total: Double = 0.0,
    val metodoPago: String = "",
    val isSynced: Boolean = false,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "detalle_ventas")
data class DetalleVentaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ventaId: String = "",
    val productoId: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0
)

data class VentaConDatos(
    val id: String,
    val nombreCliente: String,
    val total: Double,
    val totalProductos: Int,
    val fecha: Long,
    val metodoPago: String = "Efectivo",
    val isSynced: Boolean = true
)

data class VentaFirestoreDto(
    val id: String = "",
    val clienteId: String = "",
    val routeId: String = "",
    val fecha: Long = 0L,
    val total: Double = 0.0,
    val metodoPago: String = "",
    val isSynced: Boolean = true,
    val ultimaActualizacion: Long = 0L,
    val detalles: List<DetalleVentaEntity> = emptyList()
)
