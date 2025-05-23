package com.danielescrich.myapplication.retrofit.data

data class RegisterRequest(
    val nombreUsuario: String,
    val correoElectronico: String,
    val password: String
)
