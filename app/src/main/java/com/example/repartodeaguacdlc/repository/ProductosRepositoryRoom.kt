package com.example.repartodeaguacdlc.repository

import com.example.repartodeaguacdlc.data.ProductosDao
import com.example.common.model.Productos
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductosRepositoryRoom(
    private val productosDao: ProductosDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
){
    val allProductos = productosDao.getAllProductos()

    fun sincronizarCatalogoFirebase() {
        // Escuchamos la colección de productos en Firebase
        firestore.collection("productos")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null)
                    // Manejar el error
                    return@addSnapshotListener
                    // Convertimos los documentos de Firebase a nuestra lista de objetos Productos

                val productosFirebase = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Productos::class.java)?.copy(id = doc.id)
                    // Nota: Room usa Int para ID, Firebase usa String.
                    // El hashCode() es una solución rápida, pero lo ideal es que ambos sean String.
                }
                // Guardamos en Room (esto actualizará automáticamente los Flows de la UI)
                CoroutineScope(Dispatchers.IO).launch {
                    insertarCatalogoInicial(productosFirebase)
                }
            }
    }

    suspend fun insertarCatalogoInicial(productos: List<Productos>) {
        productosDao.insertAll(productos)
    }
}