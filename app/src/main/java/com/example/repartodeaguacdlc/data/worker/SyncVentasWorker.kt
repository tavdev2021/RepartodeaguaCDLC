package com.example.repartodeaguacdlc.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.common.model.VentaFirestoreDto
import com.example.repartodeaguacdlc.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncVentasWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.ventasDao()
        val firestore = FirebaseFirestore.getInstance()

        val unSyncedVentas = dao.getUnsyncedVentas()

        if (unSyncedVentas.isEmpty()) return Result.success()

        return try {
            for (venta in unSyncedVentas) {
                val detalles = dao.getDetallesForVenta(venta.id)

                val dto = VentaFirestoreDto(
                    id = venta.id,
                    clienteId = venta.clienteId,
                    routeId = venta.routeId,
                    fecha = venta.fecha,
                    total = venta.total,
                    metodoPago = venta.metodoPago,
                    isSynced = true,
                    ultimaActualizacion = venta.ultimaActualizacion,
                    detalles = detalles
                )

                // Subir a Firestore
                firestore.collection("ventas")
                    .document(venta.id)
                    .set(dto)
                    .await()

                // Marcar como sincronizado en Room
                dao.markVentaAsSynced(venta.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry() // Si se interrumpe el internet, WorkManager reintentara automaticamente
        }
    }
}