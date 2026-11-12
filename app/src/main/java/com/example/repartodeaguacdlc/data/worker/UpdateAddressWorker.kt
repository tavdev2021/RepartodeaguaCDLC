package com.example.repartodeaguacdlc.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.common.util.LocationHelper
import com.example.repartodeaguacdlc.data.AppDatabase
import com.example.repartodeaguacdlc.repository.ClientesRepositoryRoom
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateAddressWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.clientesDao()
        val pendientes = dao.getClientesSinDireccion()

        if (pendientes.isEmpty()) {
            return@withContext Result.success()
        }

        var huboAlMenosUnError = false

        val repository = ClientesRepositoryRoom(
            clientesDao = dao,
            context = applicationContext,
            firestore = FirebaseFirestore.getInstance()
        )

        for (cliente in pendientes) {
            val direccionResult = LocationHelper.obtenerDireccionLegible(
                applicationContext,
                cliente.ubicacion
            )

            if (!direccionResult.isNullOrBlank()) {
                // 🟢 Se obtuvo la dirección real: Actualizamos localmente y marcamos para subir a Firestore
                repository.updateCliente(cliente.copy(direccion = direccionResult))
            } else {
                // 🔴 No había internet o falló la geocodificación
                huboAlMenosUnError = true
            }
        }

        if (huboAlMenosUnError) {
            Result.retry() // 👈 Reintentar cuando vuelva el internet
        } else {
            Result.success()
        }
    }
}