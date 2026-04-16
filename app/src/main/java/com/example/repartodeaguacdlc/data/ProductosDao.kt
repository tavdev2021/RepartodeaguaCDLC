package com.example.repartodeaguacdlc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartodeaguacdlc.model.Productos
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductosDao {
    @Query("SELECT * FROM productos")
    fun getAllProductos(): Flow<List<Productos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Productos>)
}