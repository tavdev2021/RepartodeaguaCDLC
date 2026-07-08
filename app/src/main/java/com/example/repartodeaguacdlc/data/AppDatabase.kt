package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.common.model.Clientes
import kotlin.concurrent.Volatile
import com.example.common.model.Productos
import com.example.common.model.VentaEntity
import com.example.common.model.DetalleVentaEntity

@Database(entities = [
    Clientes::class,
    Productos::class,
    VentaEntity::class,
    DetalleVentaEntity::class
                     ],
    version = 4,
    exportSchema = false
)

abstract class AppDatabase: RoomDatabase() {
    abstract fun clientesDao(): ClientesDao
    abstract fun productosDao(): ProductosDao
    abstract fun ventasDao(): VentasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration(true) // Permitir migración destructiva para evitar crash durante desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}