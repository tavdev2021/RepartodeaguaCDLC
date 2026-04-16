package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartodeaguacdlc.repository.ProductosRepositoryRoom
import com.example.repartodeaguacdlc.viewmodel.ProductosViewModel

class ProductosViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductosViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = ProductosRepositoryRoom(database.productosDao(), database.ventasDao())
            @Suppress("UNCHECKED_CAST")
            return ProductosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}