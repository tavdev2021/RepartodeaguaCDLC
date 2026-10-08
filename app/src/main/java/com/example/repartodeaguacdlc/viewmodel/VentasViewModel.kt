package com.example.repartodeaguacdlc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.model.DetalleVentaEntity
import com.example.common.model.Productos
import com.example.common.model.VentaConDatos
import com.example.common.model.VentaEntity
import com.example.repartodeaguacdlc.repository.ProductosRepositoryRoom
import com.example.repartodeaguacdlc.repository.VentasRepositoryRoom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.emptyList

class VentasViewModel(private val ventasRepository: VentasRepositoryRoom,
                      private val productosRepository: ProductosRepositoryRoom
): ViewModel() {

    init {
        productosRepository.sincronizarCatalogoFirebase()
    }

    private val _routeId = MutableStateFlow("")

    fun setRouteId(routeId: String) {
        _routeId.value = routeId
    }

    // 1. Solo guardamos los IDs y las cantidades seleccionadas (Memoria volatil)
    private val _cantidades = MutableStateFlow<Map<String, Int>>(emptyMap())

    // 2. Combinamos el catálogo de la DB con nuestras cantidades locales
    val productos: StateFlow<List<Productos>> = combine(
        productosRepository.allProductos, // Viene de Room (Tiempo real)
        _cantidades // Cantidades que el usuario toca
    ) { catalogo, cantidades ->
        catalogo.map { producto ->
            // Si el Admin cambia el precio en DB, Room emite,
            // y aquí mezclamos el nuevo precio con la cantidad que ya teníamos.
            producto.copy(cantidad = cantidades[producto.id] ?: 0)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Función auxiliar para obtener la medianoche de hoy
    private fun getInicioDia(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val ventasHoyCount: StateFlow<Int> = _routeId.flatMapLatest { routeId ->
        if (routeId.isBlank()) flowOf(0)
    else ventasRepository.getVentasHoyCount (routeId,getInicioDia())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ingresosHoy: StateFlow<Double> = _routeId.flatMapLatest { routeId ->
        if (routeId.isBlank()) flowOf(0.0)
        else ventasRepository.getIngresosHoy(routeId,getInicioDia())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = 0.0)


    @OptIn(ExperimentalCoroutinesApi::class)
    val ultimaVenta: StateFlow<List<VentaConDatos>> = _routeId.flatMapLatest { routeId ->
        if (routeId.isBlank()) flowOf(emptyList())
        else ventasRepository.getUltimaVenta(routeId, getInicioDia())
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList())

    // Transformamos el estado para obtener totales automáticamente
    val totalPagar: StateFlow<Double> = productos.map { lista ->
        lista.sumOf { it.precio * it.cantidad }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalArticulos: StateFlow<Int> = productos.map { lista ->
        lista.sumOf { it.cantidad }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun actualizarCantidad(productoId: String, nuevaCantidad: Int) {
        if (nuevaCantidad < 0) return
        val current = _cantidades.value.toMutableMap()
        current[productoId] = nuevaCantidad
        _cantidades.value = current
    }
    fun finalizarVenta(
        clienteId: String,
        routeId: String,
        metodoPago: String,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {
            val productosSeleccionados = productos.value.filter { it.cantidad > 0 }
            if (productosSeleccionados.isEmpty())
                return@launch

            val ventaId = UUID.randomUUID().toString() // Generamos un ID único para la venta

            val venta = VentaEntity(
                id = ventaId,
                clienteId = clienteId,
                routeId = routeId,
                fecha = System.currentTimeMillis(),
                total = productosSeleccionados.sumOf { producto ->
                    producto.precio * producto.cantidad },
                metodoPago = metodoPago,
                isSynced = false, // 👈 Nuevo: Indica que está pendiente de subir
                ultimaActualizacion = System.currentTimeMillis() // 👈 Nuevo: Marca de tiempo
            )

            val detalles = productosSeleccionados.map { producto ->
                DetalleVentaEntity(
                    id = UUID.randomUUID().toString(),
                    ventaId = ventaId, // Se autogenera en la transacción
                    productoId = producto.id,
                    cantidad = producto.cantidad,
                    precioUnitario = producto.precio
                )
            }

            ventasRepository.guardarVenta(venta, detalles)
            limpiarCarrito()
            onSuccess()
        }
    }

    fun iniciarSincronizacionContinua(routeId: String) {
        ventasRepository.iniciarSincronizacionContinua(routeId)
        productosRepository.sincronizarCatalogoFirebase()
    }

    fun activarRespaldoPendiente() {
        ventasRepository.scheduleSync()
    }

    fun limpiarCarrito() {
        _cantidades.value = emptyMap()
    }

}