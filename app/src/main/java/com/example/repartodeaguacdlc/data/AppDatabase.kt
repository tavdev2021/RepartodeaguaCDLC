package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.repartodeaguacdlc.model.Clientes
import kotlin.concurrent.Volatile
import com.example.repartodeaguacdlc.model.Productos
import com.example.repartodeaguacdlc.model.VentaEntity
import com.example.repartodeaguacdlc.model.DetalleVentaEntity

@Database(entities = [
    Clientes::class,
    Productos::class,
    VentaEntity::class,
    DetalleVentaEntity::class
                     ],
    version = 2,
    exportSchema = false
)

abstract class AppDatabase: RoomDatabase() {
    abstract fun clientesDao(): ClientesDao
    abstract fun productosDao(): ProductosDao
    abstract fun ventasDao(): VentasDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration(false)
                .build()
            INSTANCE = instance
            instance
            }
        }
    }
}