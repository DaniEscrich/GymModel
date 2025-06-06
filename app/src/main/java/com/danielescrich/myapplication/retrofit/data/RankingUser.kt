package com.danielescrich.myapplication.retrofit.data

data class RankingUser(
    val nombreUsuario: String,
    val diasDesdeRegistro: Int,
    val progresosRegistrados: Int,
    val pesoPerdido: Double?
)
