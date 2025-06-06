package com.danielescrich.myapplication.room.dao

import androidx.room.*
import com.danielescrich.myapplication.room.entity.PlanTrainingEntity
import com.danielescrich.myapplication.room.entity.PlanDayItemEntity

@Dao
interface PlanEntrenamientoDao {

    @Insert
    suspend fun insertarPlan(plan: PlanTrainingEntity): Long

    @Insert
    suspend fun insertarItems(items: List<PlanDayItemEntity>)

    @Query("SELECT * FROM planes_entrenamiento WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun obtenerUltimoPlan(userId: Int): PlanTrainingEntity?

    @Query("SELECT * FROM plan_dia_items WHERE planId = :planId")
    suspend fun obtenerItemsDePlan(planId: Int): List<PlanDayItemEntity>

    @Update
    suspend fun actualizarItem(item: PlanDayItemEntity)

    @Query("""
        UPDATE plan_dia_items
        SET completado = :completado
        WHERE planId = :planId AND dia = :dia AND ejercicio = :ejercicio
    """)
    suspend fun actualizarItemManual(planId: Int, dia: String, ejercicio: String, completado: Boolean)

    // 🔴 NUEVOS MÉTODOS PARA ELIMINAR PLAN Y SUS ÍTEMS
    @Query("DELETE FROM planes_entrenamiento WHERE id = :planId")
    suspend fun eliminarPlan(planId: Int)

    @Query("DELETE FROM plan_dia_items WHERE planId = :planId")
    suspend fun eliminarItemsDePlan(planId: Int)

    @Query("SELECT * FROM planes_entrenamiento WHERE userId = :userId AND enCurso = 1 LIMIT 1")
    suspend fun obtenerPlanEnCurso(userId: Int): PlanTrainingEntity?

    @Query("UPDATE planes_entrenamiento SET enCurso = 0 WHERE id = :planId")
    suspend fun marcarPlanComoFinalizado(planId: Int)

    @Query("UPDATE planes_entrenamiento SET enCurso = 0 WHERE userId = :userId")
    suspend fun finalizarPlanesDelUsuario(userId: Int)



}
