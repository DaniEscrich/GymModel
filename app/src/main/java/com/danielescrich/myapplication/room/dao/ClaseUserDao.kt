package com.danielescrich.myapplication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.danielescrich.myapplication.room.entity.ClaseUserEntity

@Dao
interface ClaseUserDao {

    @Query("SELECT * FROM clase_usuario WHERE claseId = :claseId AND usuarioId = :usuarioId AND semana = :semana")
    suspend fun getRelacion(claseId: Int, usuarioId: kotlin.String, semana: Int): ClaseUserEntity?

    @Insert
    suspend fun apuntarUsuario(relacion: ClaseUserEntity)

    @Delete
    suspend fun desapuntarUsuario(relacion: ClaseUserEntity)

    @Query("SELECT usuarioId FROM clase_usuario WHERE claseId = :claseId AND semana = :semana")
    suspend fun getUsuariosPorClaseYSemana(claseId: Int, semana: Int): List<Int>


    @Query("SELECT COUNT(*) FROM clase_usuario WHERE usuarioId = :usuarioId")
    suspend fun getTotalClasesAsistidas(usuarioId: Int): Int

    @Query("SELECT * FROM clase_usuario WHERE usuarioId = :usuarioId")
    suspend fun obtenerClasesDeUsuario(usuarioId: Int): List<ClaseUserEntity>


}


