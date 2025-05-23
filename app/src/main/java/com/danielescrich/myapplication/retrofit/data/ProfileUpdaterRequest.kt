package com.danielescrich.myapplication.retrofit.data

data class ProfileUpdaterRequest(
    val id: Int,
    val correoElectronico: String?,
    val nuevaPassword: String?,
    val imagenBase64: String? = null
)
