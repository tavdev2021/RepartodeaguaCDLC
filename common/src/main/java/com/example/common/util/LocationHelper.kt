package com.example.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object


LocationHelper {

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

    suspend fun obtenerDireccionLegible(context: Context, coordenadas: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val partes = coordenadas.split(",")
                if (partes.size < 2) return@withContext null
                val lat = partes[0].trim().toDoubleOrNull() ?: return@withContext null
                val lng = partes[1].trim().toDoubleOrNull() ?: return@withContext null

                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // API 33+ (Android 13+): Uso asíncrono moderno con GeocodeListener
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(direcciones: MutableList<android.location.Address>) {
                                if (direcciones.isNotEmpty()) {
                                    val dir = direcciones[0]

                                    val calle = dir.thoroughfare ?: ""
                                    val num = dir.subThoroughfare ?: ""
                                    val colonia = dir.subLocality ?: ""
                                    val puebloOComunidad = dir.locality ?: dir.subAdminArea ?: "" // 👈 Nombre del Pueblo / Municipio
                                    val estado = dir.adminArea ?: "" // 👈 Estado (ej. Guerrero)

                                    val resultado = when {
                                        // 1. Ciudad / Calle con nombre registrado
                                        calle.isNotBlank() -> {
                                            val numeroTexto = if (num.isNotBlank() && num != "SN") " $num" else ""
                                            val sectorTexto = if (colonia.isNotBlank()) ", $colonia" else if (puebloOComunidad.isNotBlank()) ", $puebloOComunidad" else ""
                                            "$calle$numeroTexto$sectorTexto".trim()
                                        }

                                        // 2. Pueblo / Zona Rural sin nombre de calle (Limpio de Plus Codes)
                                        puebloOComunidad.isNotBlank() -> {
                                            if (estado.isNotBlank()) "$puebloOComunidad, $estado" else puebloOComunidad
                                        }

                                        // 3. Fallback limpiando códigos extra
                                        else -> {
                                            val lineaCompleta = dir.getAddressLine(0) ?: ""
                                            // Remueve Plus Codes como "M8F6+FV," si vinieran en el texto
                                            lineaCompleta.replace(Regex("^[A-Z0-9]+\\+[A-Z0-9]+,?\\s*"), "")
                                        }
                                    }

                                    continuation.resume(resultado.ifBlank { null })
                                } else {
                                    continuation.resume(null)
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(null)
                            }
                        })
                    }
                } else {
                    // Dispositivos anteriores a Android 13 (Síncrono tradicional)
                    @Suppress("DEPRECATION")
                    val direcciones = geocoder.getFromLocation(lat, lng, 1)

                    if (!direcciones.isNullOrEmpty()) {
                        val dir = direcciones[0]
                        val calle = dir.thoroughfare ?: ""
                        val num = dir.subThoroughfare ?: ""
                        val colonia = dir.subLocality ?: ""

                        val resultado = if (calle.isNotBlank()) "$calle $num, $colonia".trim().trimEnd(',')
                        else dir.getAddressLine(0) ?: ""

                        resultado.ifBlank { null }
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                null // Si no hay internet o falla el Geocoder, retorna NULL para reintentar
            }
        }
    }
}