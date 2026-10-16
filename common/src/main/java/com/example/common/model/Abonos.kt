package com.example.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "abonos")
data class AbonoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val clienteId: String = "",
    val routeId: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val montoAbono: Double = 0.0,
    val metodoPago: String = "Efectivo",
    val notas: String = "Abono a deuda",
    val isSynced: Boolean = false,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class AbonoFirestoreDto(
    val id: String = "",
    val clienteId: String = "",
    val routeId: String = "",
    val fecha: Long = 0L,
    val montoAbono: Double = 0.0,
    val metodoPago: String = "Efectivo",
    val notas: String = "",
    val ultimaActualizacion: Long = 0L
)
