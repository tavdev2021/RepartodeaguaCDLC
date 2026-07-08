package com.example.common.util

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual(
        fusedLocationClient: FusedLocationProviderClient,
        onStart: () -> Unit,
        onSuccess: (latitude: Double, longitude: Double, message: String) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit,
        timeoutMillis: Long = 20
    ) {
        onStart()

        // 1. Creamos un token para poder cancelar la petición
        val cts = CancellationTokenSource()

        // 2. Iniciamos un temporizador en segundo plano
        CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMillis.seconds)
            cts.cancel() // 👈 Si pasan 10s y no hay respuesta, cancelamos
        }

        val priority = Priority.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.getCurrentLocation(priority, cts.token)
            .addOnCompleteListener { task ->
                val location = if (task.isSuccessful) task.result
                else
                    null

                if (location != null) {
                    onSuccess(location.latitude, location.longitude, "Ubicación precisa obtenida")
                    onFinish()
                } else {
                    // Si el GPS falló, no hay señal o se acabó el tiempo (cancelación),
                    // intentamos obtener la última conocida.
                    ultimaUbicacionObtenida(fusedLocationClient, onSuccess, onError, onFinish)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaUbicacionObtenida(
        fusedLocationClient: FusedLocationProviderClient,
        onSuccess: (Double, Double, String) -> Unit,
        onError: (String) -> Unit,
        onFinish: () -> Unit
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    onSuccess(lastLocation.latitude, lastLocation.longitude, "Ubicación aproximada obtenida")
                } else {
                    onError("No se pudo obtener la ubicación. Activa el GPS y sal a cielo abierto")
                }
                onFinish()
            }
            .addOnFailureListener {
                onError("Error al conectar con los servicios de ubicación")
                onFinish()
            }
    }
}