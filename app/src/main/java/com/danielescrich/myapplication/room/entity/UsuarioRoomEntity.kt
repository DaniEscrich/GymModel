package com.danielescrich.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioRoomEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)
