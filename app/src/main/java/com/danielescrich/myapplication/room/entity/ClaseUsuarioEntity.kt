package com.danielescrich.myapplication.room.entity

import androidx.room.Entity

@Entity(
    tableName = "clase_usuario",
    primaryKeys = ["claseId", "usuarioId", "semana"]
)
data class ClaseUsuarioEntity(
    val claseId: Int,
    val usuarioId: Int,
    val semana: Int
)
