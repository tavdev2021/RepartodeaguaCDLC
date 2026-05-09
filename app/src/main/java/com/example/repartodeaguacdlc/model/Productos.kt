package com.example.repartodeaguacdlc.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "productos")
data class Productos(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val precio: Double,
    val imagenRes: Int,
    @Ignore var cantidad: Int = 0 // @Ignore porque la cantidad elegida no se guarda en el catálogo
) {
    // Constructor secundario para Room ya que @Ignore requiere uno
    constructor(id: Int, nombre: String, precio: Double, imagenRes: Int) : this(id, nombre, precio, imagenRes, 0)
}