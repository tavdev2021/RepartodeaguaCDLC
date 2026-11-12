package com.example.common.domain

/**
 * Resultado inmutable del cálculo de pago.
 * Contiene todos los valores calculados listos para ser consumidos por la UI.
 */
data class PaymentCalculationResult(
    val totalDespuesDeSaldoAFavor: Double,
    val saldoAFavorUsado: Double,
    val saldoAFavorRemanente: Double,
    val montoEntregadoDouble: Double,
    val hayCambioValido: Boolean,
    val excedenteEfectivo: Double,
    val abonoPotencial: Double,
    val abonoADeudaAutocobrado: Double,
    val cambioRemanenteSobrante: Double,
    val cambioEfectivoFinal: Double,
    val faltanteEfectivo: Double,
    val montoCreditoGeneradoHoy: Double,
    val nuevaDeudaProyectada: Double,
    val excedeLimiteCredito: Boolean,
    val montoPagadoEfectivoHoy: Double,
    val nuevoSaldoAFavorProyectado: Double,
    val esPagoValido: Boolean
)

/**
 * Calculador puro de reglas de negocio para cobro en Efectivo y Crédito.
 * No depende de Android Framework ni de Jetpack Compose (100% testeable con Unit Tests).
 */
object PaymentCalculator {

    const val LIMITE_CREDITO_MAXIMO = 1000.0

    fun calculate(
        total: Double,
        cantidadArticulos: Int,
        metodoPago: String,
        clienteSaldoPendiente: Double,
        clienteSaldoAFavor: Double,
        montoEntregadoText: String,
        aplicarSaldoAFavor: Boolean,
        aplicarSobranteADeuda: Boolean,
        guardarCambioComoSaldoAFavor: Boolean
    ): PaymentCalculationResult {
        val esEfectivo = metodoPago == "Efectivo"
        val esCredito = metodoPago == "Crédito"

        // 1. Saldo a favor usado y saldo remanente
        val saldoAFavorUsado = if (aplicarSaldoAFavor && clienteSaldoAFavor > 0) minOf(clienteSaldoAFavor, total) else 0.0
        val saldoAFavorRemanente = clienteSaldoAFavor - saldoAFavorUsado
        val totalDespuesDeSaldoAFavor = total - saldoAFavorUsado

        // 2. Interpretación de monto ingresado
        val montoEntregadoDouble = if (montoEntregadoText.isBlank()) {
            0.0
        } else {
            montoEntregadoText.toDoubleOrNull() ?: 0.0
        }

        val efectivoPagoConfirmado = (totalDespuesDeSaldoAFavor == 0.0) || (
                montoEntregadoText.isNotBlank() && montoEntregadoDouble >= totalDespuesDeSaldoAFavor
                )

        // 3. Cálculos en efectivo con deuda previa
        val excedenteEfectivo = if (esEfectivo && montoEntregadoDouble > totalDespuesDeSaldoAFavor) {
            montoEntregadoDouble - totalDespuesDeSaldoAFavor
        } else 0.0

        val abonoPotencial = if (esEfectivo && clienteSaldoPendiente > 0) {
            minOf(excedenteEfectivo, clienteSaldoPendiente)
        } else 0.0

        val abonoADeudaAutocobrado = if (aplicarSobranteADeuda) abonoPotencial else 0.0
        val cambioRemanenteSobrante = maxOf(0.0, excedenteEfectivo - abonoADeudaAutocobrado)

        val cambioEfectivoFinal = if (esEfectivo && !guardarCambioComoSaldoAFavor) {
            cambioRemanenteSobrante
        } else 0.0

        val faltanteEfectivo = if (esEfectivo && montoEntregadoDouble < totalDespuesDeSaldoAFavor) {
            totalDespuesDeSaldoAFavor - montoEntregadoDouble
        } else 0.0

        val hayCambioValido = montoEntregadoDouble >= totalDespuesDeSaldoAFavor && montoEntregadoText.isNotBlank()

        // 4. Deuda proyectada e importe a crédito
        val montoCreditoGeneradoHoy = if (esCredito) maxOf(0.0, totalDespuesDeSaldoAFavor - montoEntregadoDouble) else 0.0

        val nuevaDeudaProyectada = if (esEfectivo) {
            maxOf(0.0, clienteSaldoPendiente - abonoADeudaAutocobrado)
        } else {
            maxOf(0.0, clienteSaldoPendiente + montoCreditoGeneradoHoy)
        }

        val excedeLimiteCredito = esCredito && nuevaDeudaProyectada > LIMITE_CREDITO_MAXIMO

        // 5. Monto total en efectivo recibido hoy (Corte de caja)
        val montoPagadoEfectivoHoy = if (esEfectivo) {
            minOf(montoEntregadoDouble, totalDespuesDeSaldoAFavor) + if (guardarCambioComoSaldoAFavor) cambioRemanenteSobrante else 0.0
        } else {
            minOf(montoEntregadoDouble, totalDespuesDeSaldoAFavor)
        }

        // 6. Nuevo saldo a favor proyectado
        val nuevoSaldoAFavorProyectado = saldoAFavorRemanente + if (esEfectivo && guardarCambioComoSaldoAFavor) cambioRemanenteSobrante else 0.0

        val esPagoValido = cantidadArticulos > 0 && !excedeLimiteCredito && (
                (esEfectivo && efectivoPagoConfirmado) || (esCredito)
                )

        return PaymentCalculationResult(
            totalDespuesDeSaldoAFavor = totalDespuesDeSaldoAFavor,
            saldoAFavorUsado = saldoAFavorUsado,
            saldoAFavorRemanente = saldoAFavorRemanente,
            montoEntregadoDouble = montoEntregadoDouble,
            hayCambioValido = hayCambioValido,
            excedenteEfectivo = excedenteEfectivo,
            abonoPotencial = abonoPotencial,
            abonoADeudaAutocobrado = abonoADeudaAutocobrado,
            cambioRemanenteSobrante = cambioRemanenteSobrante,
            cambioEfectivoFinal = cambioEfectivoFinal,
            faltanteEfectivo = faltanteEfectivo,
            montoCreditoGeneradoHoy = montoCreditoGeneradoHoy,
            nuevaDeudaProyectada = nuevaDeudaProyectada,
            excedeLimiteCredito = excedeLimiteCredito,
            montoPagadoEfectivoHoy = montoPagadoEfectivoHoy,
            nuevoSaldoAFavorProyectado = nuevoSaldoAFavorProyectado,
            esPagoValido = esPagoValido
        )
    }
}
