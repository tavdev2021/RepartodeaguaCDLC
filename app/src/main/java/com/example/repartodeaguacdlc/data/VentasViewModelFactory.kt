package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartodeaguacdlc.repository.ProductosRepositoryRoom
import com.example.repartodeaguacdlc.repository.VentasRepositoryRoom
import com.example.repartodeaguacdlc.viewmodel.VentasViewModel

class VentasViewModelFactory(private val context: Context):
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VentasViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val ventasRepo = VentasRepositoryRoom(database.ventasDao(), context.applicationContext)
            val productosRepo = ProductosRepositoryRoom(database.productosDao())
            @Suppress("UNCHECKED_CAST")
            return VentasViewModel(ventasRepo, productosRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}