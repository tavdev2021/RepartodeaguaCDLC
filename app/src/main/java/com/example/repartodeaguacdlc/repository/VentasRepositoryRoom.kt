package com.example.repartodeaguacdlc.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.common.model.DetalleVentaEntity
import com.example.common.model.VentaEntity
import com.example.common.model.VentaFirestoreDto
import com.example.repartodeaguacdlc.data.VentasDao
import com.example.repartodeaguacdlc.data.worker.SyncVentasWorker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VentasRepositoryRoom(
    private val ventasDao: VentasDao,
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Programar subida de ventas offline
    fun scheduleSync () {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncVentasWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncVentasWorker",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    // Descargar vntas desde Firestore a Room (al arrancar la app o reinstalar)
    fun iniciarSincronizacionContinua(routeId: String) {
        if (routeId.isBlank()) return

        firestore.collection("ventas")
            .whereEqualTo("routeId", routeId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->
                    val dto = try {
                        change.document.toObject(VentaFirestoreDto::class.java)
                    } catch (e: Exception) {
                        null
                    }

                    dto?.let { ventaDto ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val ventaEntity = VentaEntity(
                                id = ventaDto.id,
                                clienteId = ventaDto.clienteId,
                                routeId = ventaDto.routeId,
                                fecha = ventaDto.fecha,
                                total = ventaDto.total,
                                metodoPago = ventaDto.metodoPago,
                                isSynced = true,
                                ultimaActualizacion = ventaDto.ultimaActualizacion
                            )

                            // Guardar en Room la venta y sus detalles
                            ventasDao.registrarVentaCompleta(ventaEntity, ventaDto.detalles)
                        }
                    }
                }
            }
    }

    // Guardar venta localmente y programar subida a Firestore con WorkManager
    suspend fun guardarVenta(venta: VentaEntity, detalles: List<DetalleVentaEntity>) {
        ventasDao.registrarVentaCompleta(venta, detalles)
        scheduleSync()
    }

    fun getVentasHoyCount(routeId: String, inicioDia: Long) = ventasDao.getVentasHoyCount(routeId, inicioDia)

    fun getIngresosHoy(routeId: String, inicioDia: Long) = ventasDao.getIngresosHoy(routeId, inicioDia)

    fun getUltimas3Ventas(routeId: String, inicioDia: Long) = ventasDao.getUltimas3VentasConDatos(routeId, inicioDia)

}