package com.example.repartodeaguacdlc.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.common.model.AbonoEntity
import com.example.common.model.AbonoFirestoreDto
import com.example.repartodeaguacdlc.data.AbonosDao
import com.example.repartodeaguacdlc.data.worker.SyncAbonosWorker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AbonosRepositoryRoom (
    private val abonosDao: AbonosDao,
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Encola el trabajo de subida de abonos en WorkManager al detectar red
    fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncAbonosWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncAbonosWorker",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    // Descarga abonos de la ruta desde Firestore hacia Room (Respaldo en la nube)
    fun iniciarSincronizacionContinua(routeId: String) {
        if (routeId.isBlank()) return

        firestore.collection("abonos")
            .whereEqualTo("routeId", routeId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null || snapshots == null) return@addSnapshotListener

                snapshots.documentChanges.forEach { change ->
                    val dto = try {
                        change.document.toObject(AbonoFirestoreDto::class.java)
                    } catch (e: Exception) {
                        null
                    }

                    dto?.let { abonoDto ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val entity = AbonoEntity(
                                id = abonoDto.id,
                                clienteId = abonoDto.clienteId,
                                routeId = abonoDto.routeId,
                                fecha = abonoDto.fecha,
                                montoAbono = abonoDto.montoAbono,
                                metodoPago = abonoDto.metodoPago,
                                notas = abonoDto.notas,
                                isSynced = true,
                                ultimaActualizacion = abonoDto.ultimaActualizacion
                            )
                            abonosDao.insertAbono(entity)
                        }
                    }
                }
            }
    }

    suspend fun guardarAbono(abono: AbonoEntity) {
        abonosDao.insertAbono(abono)
        scheduleSync()
    }

    fun getAbonosPorCliente(clienteId: String): Flow<List<AbonoEntity>> =
        abonosDao.getAbonosPorCliente(clienteId)

    fun getAbonosRecibidosHoy(inicioDia: Long): Flow<Double> =
        abonosDao.getAbonosRecibidosHoy(inicioDia)
}