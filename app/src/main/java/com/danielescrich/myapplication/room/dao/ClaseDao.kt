package com.danielescrich.myapplication.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danielescrich.myapplication.room.entity.ClaseEntity

@Dao
interface ClaseDao {

    @Query("SELECT * FROM clases ORDER BY diaSemana ASC, hora ASC")
    fun getAllClases(): LiveData<List<ClaseEntity>>

    @Query("SELECT * FROM clases WHERE diaSemana = :dia")
    fun getClasesByDia(dia: String): LiveData<List<ClaseEntity>>

    @Query("SELECT * FROM clases WHERE diaSemana = :dia AND semana = :semana ORDER BY hora ASC")
    fun getClasesPorDiaYSemana(dia: String, semana: Int): LiveData<List<ClaseEntity>>

    @Insert
    suspend fun addClase(clase: ClaseEntity): Long

    @Insert
    suspend fun addClases(clases: List<ClaseEntity>): List<Long>

    @Update
    suspend fun updateClase(clase: ClaseEntity): Int

    @Delete
    suspend fun deleteClase(clase: ClaseEntity): Int

    @Query("SELECT * FROM clases")
    suspend fun getAllClasesDirect(): List<ClaseEntity>

    @Query("SELECT * FROM clases WHERE diaSemana = :dia AND semana = :semana")
    suspend fun getClasesPorDiaYSemanaDirect(dia: String, semana: Int): List<ClaseEntity>

}
