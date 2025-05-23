package com.danielescrich.myapplication.retrofit.data

data class UpdateProfileRequest(
    val id: Int,
    val correoElectronico: String?,
    val passwordActual: String?,
    val passwordNueva: String?,
    val imagenBase64: String?
)
