package com.example.repartodeaguacdlc.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import com.example.repartodeaguacdlc.viewmodel.ClientesUpdateViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ClientesUpdateViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientesUpdateViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = ClientesRepositoryRoom(database.clientesDao(),
                context = context,
                firestore = FirebaseFirestore.getInstance()
            )

            @Suppress("UNCHECKED_CAST")
            return ClientesUpdateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}