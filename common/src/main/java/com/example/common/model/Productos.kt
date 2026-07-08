package com.example.common.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Productos @Ignore constructor(
    @PrimaryKey val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "",
    @Ignore var cantidad: Int = 0
) {
    // Room usará este constructor automáticamente
    constructor(id: String, nombre: String, precio: Double, imagenUrl: String) : this(id, nombre, precio, imagenUrl, 0)
}