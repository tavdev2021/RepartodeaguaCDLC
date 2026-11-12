package com.example.common.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para validar las reglas de negocio y matemáticas de PaymentCalculator.
 */
class PaymentCalculatorTest {

    // =========================================================================
    // CASOS DE PRUEBA EN EFECTIVO
    // =========================================================================

    @Test
    fun `pago en efectivo exacto calcula cambio de cero y es pago valido`() {
        // 1. Arrange (Preparar datos)
        val total = 100.0
        val cantidadArticulos = 2
        val metodoPago = "Efectivo"
        val saldoPendiente = 0.0
        val saldoAFavor = 0.0
        val montoEntregadoText = "100.00"

        // 2. Act (Ejecutar el cálculo)
        val resultado = PaymentCalculator.calculate(
            total = total,
            cantidadArticulos = cantidadArticulos,
            metodoPago = metodoPago,
            clienteSaldoPendiente = saldoPendiente,
            clienteSaldoAFavor = saldoAFavor,
            montoEntregadoText = montoEntregadoText,
            aplicarSaldoAFavor = false,
            aplicarSobranteADeuda = false,
            guardarCambioComoSaldoAFavor = false
        )

        // 3. Assert (Verificar resultados)
        assertEquals(0.0, resultado.cambioEfectivoFinal, 0.01)
        assertEquals(100.0, resultado.montoPagadoEfectivoHoy, 0.01)
        assertTrue(resultado.esPagoValido)
        assertFalse(resultado.excedeLimiteCredito)
    }

    @Test
    fun `pago en efectivo con billete mayor calcula cambio correcto`() {
        // Arrange
        val total = 150.0
        val montoEntregadoText = "200.00"

        // Act
        val resultado = PaymentCalculator.calculate(
            total = total,
            cantidadArticulos = 3,
            metodoPago = "Efectivo",
            clienteSaldoPendiente = 0.0,
            clienteSaldoAFavor = 0.0,
            montoEntregadoText = montoEntregadoText,
            aplicarSaldoAFavor = false,
            aplicarSobranteADeuda = false,
            guardarCambioComoSaldoAFavor = false
        )

        // Assert: Entregó $200 para $150 -> Cambio debe ser $50
        assertEquals(50.0, resultado.cambioEfectivoFinal, 0.01)
        assertEquals(50.0, resultado.cambioRemanenteSobrante, 0.01)
        assertTrue(resultado.esPagoValido)
    }

    @Test
    fun `pago en efectivo con sobrante aplicado a deuda anterior reduce la deuda`() {
        // Arrange: Compra $100, Debe $200 anteriores, Paga con $150
        val total = 100.0
        val saldoPendiente = 200.0
        val montoEntregadoText = "150.00" // Sobran $50

        // Act: Activar opción de abonar sobrante a deuda
        val resultado = PaymentCalculator.calculate(
            total = total,
            cantidadArticulos = 2,
            metodoPago = "Efectivo",
            clienteSaldoPendiente = saldoPendiente,
            clienteSaldoAFavor = 0.0,
            montoEntregadoText = montoEntregadoText,
            aplicarSaldoAFavor = false,
            aplicarSobranteADeuda = true, // 👈 Se activa abono automático
            guardarCambioComoSaldoAFavor = false
        )

        // Assert
        assertEquals(50.0, resultado.abonoADeudaAutocobrado, 0.01) // Se abonaron $50
        assertEquals(150.0, resultado.nuevaDeudaProyectada, 0.01) // Deuda pasa de $200 a $150
        assertEquals(0.0, resultado.cambioEfectivoFinal, 0.01) // No hay cambio
        assertTrue(resultado.esPagoValido)
    }

    @Test
    fun `pago en efectivo guardando cambio como saldo a favor acumula el saldo`() {
        // Arrange: Compra $100, Paga con $150 (Sobran $50)
        val resultado = PaymentCalculator.calculate(
            total = 100.0,
            cantidadArticulos = 2,
            metodoPago = "Efectivo",
            clienteSaldoPendiente = 0.0,
            clienteSaldoAFavor = 20.0, // Tenía $20 previos
            montoEntregadoText = "150.00",
            aplicarSaldoAFavor = false,
            aplicarSobranteADeuda = false,
            guardarCambioComoSaldoAFavor = true // 👈 Guardar cambio
        )

        // Assert
        assertEquals(0.0, resultado.cambioEfectivoFinal, 0.01)
        assertEquals(70.0, resultado.nuevoSaldoAFavorProyectado, 0.01) // $20 previas + $50 cambio = $70
        assertTrue(resultado.esPagoValido)
    }

    // =========================================================================
    // CASOS DE PRUEBA EN CRÉDITO
    // =========================================================================

    @Test
    fun `venta a credito dentro del limite de mil pesos es valida`() {
        // Arrange: Compra $300 a crédito con $200 de deuda previa -> Total $500 (Menor a $1,000)
        val resultado = PaymentCalculator.calculate(
            total = 300.0,
            cantidadArticulos = 5,
            metodoPago = "Crédito",
            clienteSaldoPendiente = 200.0,
            clienteSaldoAFavor = 0.0,
            montoEntregadoText = "0.00",
            aplicarSaldoAFavor = false,
            aplicarSobranteADeuda = false,
            guardarCambioComoSaldoAFavor = false
        )

        // Assert
        assertEquals(300.0, resultado.montoCreditoGeneradoHoy, 0.01)
        assertEquals(500.0, resultado.nuevaDeudaProyectada, 0.01)
        assertFalse(resultado.excedeLimiteCredito)
        assertTrue(resultado.esPagoValido)
    }

    @Test
    fun `venta a credito que excede limite de mil pesos invalida el pago`() {
        // Arrange: Compra $600 a crédito con $500 de deuda previa -> Total $1,100 (Súpera los $1,000 máx)
        val resultado = PaymentCalculator.calculate(
            total = 600.0,
            cantidadArticulos = 10,
            metodoPago = "Crédito",
            clienteSaldoPendiente = 500.0,
            clienteSaldoAFavor = 0.0,
            montoEntregadoText = "0.00",
            aplicarSaldoAFavor = false,
            aplicarSobranteADeuda = false,
            guardarCambioComoSaldoAFavor = false
        )

        // Assert
        assertEquals(1100.0, resultado.nuevaDeudaProyectada, 0.01)
        assertTrue(resultado.excedeLimiteCredito) // 👈 Superó el límite
        assertFalse(resultado.esPagoValido) // 👈 El botón "Finalizar Venta" se desactiva
    }
}