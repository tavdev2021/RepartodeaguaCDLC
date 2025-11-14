package com.example.repartodeaguacdlc.model

import android.widget.ImageView

data class Productos (
    val imagen: ImageView,
    val text: String,
    val action:() -> Unit
)