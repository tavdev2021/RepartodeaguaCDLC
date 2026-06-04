package com.example.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Clientes(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val telefono: String,
    val email: String,
    val ubicacion: String,
    val notas: String,
    val imagenUrl: String? = null,
    val fechaRegistro: Long = System.currentTimeMillis()
)