package com.example.admin_app.model

import androidx.annotation.DrawableRes

data class AccionRapida (
    @DrawableRes val icon: Int,
    val text: String
    //val action: () -> Unit
)