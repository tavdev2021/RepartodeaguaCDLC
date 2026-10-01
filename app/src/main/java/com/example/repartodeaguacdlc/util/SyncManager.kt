package com.example.repartodeaguacdlc.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.repartodeaguacdlc.data.worker.UpdateAddressWorker

object SyncManager {

    fun programarSincronizacionDireccion(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<UpdateAddressWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_direcciones",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

}