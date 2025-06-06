package com.danielescrich.myapplication.model

data class IAPlanItem(
    val texto: String,
    var completado: Boolean = false,
    val esTitulo: Boolean = false
)