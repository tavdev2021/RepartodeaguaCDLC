package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import com.example.repartodeaguacdlc.viewmodel.AddNewClientViewModel

@Suppress("UNCHECKED_CAST")
class AddNewClientViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getDatabase(context)
        val repository = ClientesRepositoryRoom(database.clientesDao())

        if (modelClass.isAssignableFrom(AddNewClientViewModel::class.java)) {
        return AddNewClientViewModel(clientesRepository = repository) as T
    }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}