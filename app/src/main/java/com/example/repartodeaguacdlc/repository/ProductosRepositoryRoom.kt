package com.example.repartodeaguacdlc.repository

import com.example.repartodeaguacdlc.data.ProductosDao
import com.example.repartodeaguacdlc.data.VentasDao
import com.example.repartodeaguacdlc.model.DetalleVentaEntity
import com.example.repartodeaguacdlc.model.Productos
import com.example.repartodeaguacdlc.model.VentaEntity

class ProductosRepositoryRoom(
    private val productosDao: ProductosDao,
    private val ventasDao: VentasDao
){

    val allProductos = productosDao.getAllProductos()

    suspend fun guardarVenta(venta: VentaEntity, detalles: List<DetalleVentaEntity>) {
        ventasDao.registrarVentaCompleta(venta, detalles)
    }

    suspend fun insertarCatalogoInicial(productos: List<Productos>) {
        productosDao.insertAll(productos)
    }

}