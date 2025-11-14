package com.example.repartodeaguacdlc.model

import androidx.compose.ui.graphics.vector.ImageVector

data class AccionRapida (
    val icon: ImageVector,
    val text: String,
    val action:() -> Unit
)