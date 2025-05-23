package com.danielescrich.myapplication.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.danielescrich.myapplication.room.entity.UsuarioRoomEntity

@Dao
interface UsuarioDao {
    @Query("SELECT nombre FROM usuario WHERE id = :id")
    suspend fun getNombreUsuarioPorId(id: Int): String?

    @Insert
    suspend fun insertUsuario(usuario: UsuarioRoomEntity)

    @Query("SELECT id FROM usuario WHERE nombre = :nombre")
    suspend fun getIdByNombre(nombre: String): Int?
}

