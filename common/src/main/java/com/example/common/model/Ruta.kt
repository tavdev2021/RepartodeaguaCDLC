package com.example.common.model

import java.util.UUID

data class Ruta(
    val id: String = UUID.randomUUID().toString(), // Genera el UUID automáticamente
    val nombre: String = ""
)
