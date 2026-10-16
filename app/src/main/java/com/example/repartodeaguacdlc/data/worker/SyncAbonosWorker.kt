package com.example.repartodeaguacdlc.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.common.model.AbonoFirestoreDto
import com.example.repartodeaguacdlc.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncAbonosWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams){
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val abonosDao = database.abonosDao()
        val firestore = FirebaseFirestore.getInstance()

        // 1.- Buscamos abonos pendientes de subir (isSynced = false)
        val unsyncedAbonos = abonosDao.getUnsyncedAbonos()
        if (unsyncedAbonos.isEmpty()) return Result.success()

        return try {
            // 2.- Subimos cada uno de los abonos pendientes a la coleccion "abonos" en Firebase
            for (abono in unsyncedAbonos) {
                val abonoDto = AbonoFirestoreDto(
                    id = abono.id,
                    clienteId = abono.clienteId,
                    routeId = abono.routeId,
                    fecha = abono.fecha,
                    montoAbono = abono.montoAbono,
                    metodoPago = abono.metodoPago,
                    notas = abono.notas,
                    ultimaActualizacion = abono.ultimaActualizacion
                )

                firestore.collection("abonos")
                    .document(abono.id)
                    .set(abonoDto)
                    .await()

                // 3.- Marcamos el abono como sincronizado en Room
                abonosDao.markAbonoAsSynced(abono.id)
            }
            Result.success()
        } catch (e: Exception) {
            // Si no hay red o falla la conexión, WorkManager reintentará automáticamente
            Result.retry()
            }
        }
    }