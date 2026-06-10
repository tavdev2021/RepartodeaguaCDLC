package com.example.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clientes")
data class Clientes(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val telefono: String,
    val email: String,
    val ubicacion: String,
    val notas: String,
    val imagenUrl: String? = null,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val ultimaActualizacion: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)