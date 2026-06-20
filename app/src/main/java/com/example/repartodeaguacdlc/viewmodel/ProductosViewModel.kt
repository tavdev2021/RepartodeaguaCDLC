package com.example.repartodeaguacdlc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartodeaguacdlc.R
import com.example.common.model.DetalleVentaEntity
import com.example.common.model.Productos
import com.example.common.model.VentaEntity
import com.example.repartodeaguacdlc.repository.ProductosRepositoryRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ProductosViewModel(private val productosRepository: ProductosRepositoryRoom): ViewModel() {

    // Observamos directamente el Flow del repositorio
    private val _productos = MutableStateFlow<List<Productos>>(emptyList())
    val productos: StateFlow<List<Productos>> = _productos.asStateFlow()

    init {
        viewModelScope.launch {
            productosRepository.allProductos.collect { lista ->
                // Si la DB está vacía, podrías insertar el catálogo inicial aquí una sola vez
                if (lista.isEmpty()) {
                    insertarPreciosBase()
                } else {
                    _productos.value = lista
                }
            }
        }
    }

    private suspend fun insertarPreciosBase() {
        val catalogo = listOf(
            Productos(nombre = "Garrafón 20L", precio = 45.0, imagenRes = R.drawable.garrafon_agua),
            Productos(nombre = "Botella 1L", precio = 12.0, imagenRes = R.drawable.splash),
            Productos(nombre = "Pack 6 unidades", precio = 60.0, imagenRes = R.drawable.agua_pack)
        )
        productosRepository.insertarCatalogoInicial(catalogo)
    }

    fun actualizarCantidad(productoId: Int, nuevaCantidad: Int) {
        if (nuevaCantidad < 0) return
        _productos.value = _productos.value.map {
            if (it.id == productoId) it.copy(cantidad = nuevaCantidad) else it
        }
    }

    // Transformamos el estado para obtener totales automáticamente
    val totalPagar = _productos.map { lista -> lista.sumOf { it.precio * it.cantidad } }
    val totalArticulos = _productos.map { lista -> lista.sumOf { it.cantidad } }

    fun finalizarVenta(clienteId: String, metodoPago: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val productosSeleccionados = _productos.value.filter { it.cantidad > 0 }
            if (productosSeleccionados.isEmpty())
                return@launch

            val ventaId = UUID.randomUUID().toString() // Generamos un ID único para la venta

            val venta = VentaEntity(
                id = ventaId,
                clienteId = clienteId,
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

            productosRepository.guardarVenta(venta, detalles)
            limpiarCarrito()
            onSuccess()
        }
    }

    fun limpiarCarrito() {
        _productos.value = _productos.value.map { it.copy(cantidad = 0) }
    }

}