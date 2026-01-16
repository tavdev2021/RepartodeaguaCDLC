package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.repartodeaguacdlc.model.Clientes
import kotlin.concurrent.Volatile

@Database(entities = [Clientes::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun clientesDao(): ClientesDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
            INSTANCE = instance
            instance
            }
        }
    }
}