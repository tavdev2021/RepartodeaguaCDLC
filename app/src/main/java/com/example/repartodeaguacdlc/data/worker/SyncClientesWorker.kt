package com.example.repartodeaguacdlc.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repartodeaguacdlc.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncClientesWorker(
    context: Context,
    parameters: WorkerParameters
) :  CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        //Obtenemos las instancias de la DB local y de Firebase
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.clientesDao()
        val firestore = FirebaseFirestore.getInstance()

        //Obtenemos los clientes que no han sido sincronizados
        val unSyncedClientes = dao.getUnsyncedClientes()

        if (unSyncedClientes.isEmpty()) return Result.success()

        return try {
            for (cliente in unSyncedClientes) {
                //Subir a Firestore
                // Usamos el ID del cliente como nombre del documento para evitar duplicados
                firestore.collection("clientes")
                    .document(cliente.id)
                    .set(cliente.copy(isSynced = true)) // Lo enviamos marcado como sincronizado
                    .await()

                // Si no hubo error, lo marcamos en la base de datos local
                dao.markAsSynced(cliente.id)
            }
            Result.success()
        } catch (e: Exception) {
            // Si algo falla (ej. se corto la luz o el wifi),
            // WorkManager lo intentara automaticamente mas tarde.
            Result.retry()
        }
    }
}