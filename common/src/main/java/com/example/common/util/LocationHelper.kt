package com.example.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun iniciarSeguimientoPreciso(
        fusedLocationClient: FusedLocationProviderClient,
        onLocationReceived: (latitude: Double, longitude: Double, accuracy: Float) -> Unit,
    ): LocationCallback {

        // Configuramos la petición para que use satélites GPS prioritariamente
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .apply {
                setMinUpdateIntervalMillis(1000L) // Actualiza cada segundo si es posible
                setWaitForAccurateLocation(true) // CLAVE: Obliga a que la ubicación sea precisa
            }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // Solo nos interesa la ubicacion más reciente
                result.lastLocation?.let { location ->
                    onLocationReceived(location.latitude, location.longitude, location.accuracy)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        return callback
    }

    fun detenerSeguimiento(fusedLocationClient: FusedLocationProviderClient, callback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(callback)
    }

    suspend fun obtenerDireccionLegible(context: Context, coordenadas: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val partes = coordenadas.split(",")
                val lat = partes[0].trim().toDouble()
                val lng = partes[1].trim().toDouble()

                val geocoder = Geocoder(context, Locale.getDefault())
                val direcciones = geocoder. getFromLocation(lat, lng, 1)

                if (!direcciones.isNullOrEmpty()) {
                    val direccion = direcciones[0]
                    // Construimos un formato amigable: Calle Número, Colonia
                    val calle = direccion.thoroughfare ?: ""
                    val num = direccion.subThoroughfare ?: ""
                    val colonia = direccion.subLocality ?: ""

                    if (calle.isNotEmpty()) "$calle $num, $colonia".trim().trimEnd(',')
                    else direccion.getAddressLine(0) // Fallback si no hay nombre de calle
                } else {
                    coordenadas // Si Google no encuentra nada, devolvemos los números
                }
            } catch (e: Exception) {
                coordenadas // Si no hay internet, devolvemos los números para no crashear
            }
        }
    }
}