package com.example.repartodeaguacdlc.model

import android.media.Image
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class AccionRapida (
    @DrawableRes val icon: Int,
    val text: String,
    val action: () -> Unit
)