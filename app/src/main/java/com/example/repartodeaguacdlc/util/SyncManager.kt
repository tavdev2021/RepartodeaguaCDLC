package com.example.repartodeaguacdlc.util

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.repartodeaguacdlc.data.worker.UpdateAddressWorker
import java.util.concurrent.TimeUnit

object SyncManager {

    fun programarSincronizacionDireccion(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 👈 Solo cuando haya internet
            .build()

        val request = OneTimeWorkRequestBuilder<UpdateAddressWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10000L, // Reintentar en 10 segundos si falla la red
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_direcciones",
            ExistingWorkPolicy.REPLACE, // 👈 REPLACE asegura que se encolen los clientes pendientes
            request
        )
    }
}