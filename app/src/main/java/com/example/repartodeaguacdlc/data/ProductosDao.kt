package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.common.model.Productos
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductosDao {
    @Query("SELECT * FROM productos")
    fun getAllProductos(): Flow<List<Productos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Productos>)

    // 🔴 1. Elimina de Room los productos cuyo ID ya no existe en la lista activa de Firebase
    @Query("DELETE FROM productos WHERE id NOT IN (:ids)")
    suspend fun deleteProductosNotIn(ids: List<String>)

    // 🔴 2. Elimina todos los productos si el catálogo en Firebase quedó totalmente vacío
    @Query("DELETE FROM productos")
    suspend fun deleteAllProductos()

    // 🔴 3. Transacción atómica: Inserta activos y elimina obsoletos en un solo paso
    @Transaction
    suspend fun sincronizarCatalogo(productos: List<Productos>) {
        if (productos.isEmpty()) {
            deleteAllProductos()
        } else {
            insertAll(productos)
            val idsActivos = productos.map { it.id }
            deleteProductosNotIn(idsActivos)
        }
    }
}