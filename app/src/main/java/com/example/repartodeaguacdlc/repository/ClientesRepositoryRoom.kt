package com.example.repartodeaguacdlc.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.repartodeaguacdlc.data.ClientesDao
import com.example.common.model.Clientes
import com.example.repartodeaguacdlc.data.worker.SyncClientesWorker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ClientesRepositoryRoom(
    private val clientesDao: ClientesDao,
    private val context: Context,
    private val firestore: FirebaseFirestore
) {

    fun scheduleSync() {
        // Definimos las restriccione: Solo ejecutar si hay internet

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Creamos la peticion de trabajo de un solo uso
        val syncRequest = OneTimeWorkRequestBuilder<SyncClientesWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        // Encolamos el trabajo de forma unica para no saturar con muchos procesos iguales
        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncClientesWorker", // Identificador unico del trabajo"
            ExistingWorkPolicy.REPLACE, // Si ya hay uno esperando, lo reemplazamos por el nuevo que incluye a todos
            syncRequest
        )
    }

    fun iniciarSincronizacionContinua(routeId: String) {
        firestore.collection("clientes")
            .whereEqualTo("routeId", routeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                snapshot?.documentChanges?.forEach { change ->
                    // 1. Intentamos obtener el objeto de forma segura
                    val clienteRemoto = try {
                        change.document.toObject(Clientes::class.java)
                    } catch (e: Exception) {
                        null // Si los datos en la nube están mal, ignoramos este documento
                    }

                    // 2. Solo procesamos si el cliente NO es nulo
                    clienteRemoto?.let { cliente ->
                        CoroutineScope(Dispatchers.IO).launch {
                            // 3. Lo guardamos en Room marcándolo como sincronizado
                            clientesDao.insertCliente(cliente.copy(isSynced = true))
                        }
                    }
                }
            }
    }


    // Exponer el flujo de clientes (Read All)
    //val allClientes: Flow<List<Clientes>> = clientesDao.getAllClientes()

    fun getAllClientes(routeId: String): Flow<List<Clientes>> {
        return clientesDao.getAllClientes(routeId)
    }

    // Obtener Cliente por ID (Read One)
    fun getClienteByIdFlow(id: String): Flow<Clientes?> {
        return clientesDao.getClienteByIdFlow(id)
    }

    // Obtener Cliente por ID
    suspend fun getClienteById(id: String): Clientes? {
        return clientesDao.getClienteById(id)
    }

    // Crear Cliente
    suspend fun insertCliente(cliente: Clientes) {
        clientesDao.insertCliente(cliente.copy(isSynced = false))

        // Lanzamos la sincronización en segundo plano
        scheduleSync()
    }

    // Actualizar Cliente
    suspend fun updateCliente(cliente: Clientes){
        // Marcamos el objeto No Sincronizado y actualizamos la fecha
        val clienteActualizado = cliente.copy(
            isSynced = false,
            ultimaActualizacion = System.currentTimeMillis()
            )

        // Guardamos en la base de datos Local
        clientesDao.updateCliente(clienteActualizado)

        // Despertamos al Worker para que lo suba a Firestore
        scheduleSync()
    }

    // Eliminar Cliente
    suspend fun deleteCliente(cliente: Clientes){
        clientesDao.deleteCliente(cliente)
    }

    // Eliminar todos los clientes
    suspend fun deleteAllClientes(){
        clientesDao.deleteAllClientes()
    }
}
