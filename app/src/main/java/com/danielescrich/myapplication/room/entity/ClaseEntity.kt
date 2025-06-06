package com.danielescrich.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clases")
data class ClaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcion: String?,
    val diaSemana: String,
    val hora: String,
    val maxUsuarios: Int = 7,
    val apuntados: Int = 0,
    val semana: Int = 0
)
