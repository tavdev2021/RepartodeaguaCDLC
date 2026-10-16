package com.example.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clientes")
data class Clientes(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val routeId: String = "",
    val telefono: String = "",
    val ubicacion: String = "",
    val direccion: String = "",
    val notas: String = "",
    val imagenUrl: String? = null,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val activo: Boolean = true,
    val saldoPendiente : Double = 0.0,
    val saldoAFavor: Double = 0.0
)

//DTO para serializar/deserializar limpiamente con Firebase-Firestore
data class ClienteFirestoreDto(
    val id: String = "",
    val nombre: String = "",
    val routeId: String = "",
    val telefono: String = "",
    val ubicacion: String = "",
    val direccion: String = "",
    val notas: String = "",
    val imagenUrl: String? = null,
    val fechaRegistro: Long = 0L,
    val ultimaActualizacion: Long = 0L,
    val activo: Boolean = true,
    val saldoPendiente : Double = 0.0,
    val saldoAFavor: Double = 0.0
)