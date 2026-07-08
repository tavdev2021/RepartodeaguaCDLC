package com.example.repartodeaguacdlc.repository

import com.example.common.model.DetalleVentaEntity
import com.example.common.model.VentaEntity
import com.example.repartodeaguacdlc.data.VentasDao

class VentasRepositoryRoom(
    private val ventasDao: VentasDao
) {

    suspend fun guardarVenta(venta: VentaEntity, detalles: List<DetalleVentaEntity>) {
        ventasDao.registrarVentaCompleta(venta, detalles)
    }

    fun getVentasHoyCount(inicioDia: Long) = ventasDao.getVentasHoyCount(inicioDia)

    fun getIngresosHoy(inicioDia: Long) = ventasDao.getIngresosHoy(inicioDia)

    fun getUltimas3Ventas(inicioDia: Long) = ventasDao.getUltimas3VentasConDatos(inicioDia)

}