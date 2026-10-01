package com.example.repartodeaguacdlc.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.common.util.LocationHelper
import com.example.repartodeaguacdlc.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateAddressWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(applicationContext)
        val pendientes = db.clientesDao().getClientesSinDireccion()

        if (pendientes.isEmpty()) {
            return@withContext Result.success()
        }

            try {

                pendientes.forEach { cliente ->
                    val direccionResult = LocationHelper.obtenerDireccionLegible(
                        applicationContext,
                        cliente.ubicacion
                    )

                    // Si obtuvimos una dirección real (distinta a las coordenadas numéricas)
                    if (direccionResult != cliente.ubicacion) {
                        db.clientesDao().updateCliente(cliente.copy(direccion = direccionResult))
                    }
                }
                return@withContext Result.success()
            } catch (e: Exception) {
                return@withContext Result.retry()
            }
    }
}