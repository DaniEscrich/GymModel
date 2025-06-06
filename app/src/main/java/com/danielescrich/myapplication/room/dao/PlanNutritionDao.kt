package com.danielescrich.myapplication.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.danielescrich.myapplication.room.entity.ItemNutritionEntity
import com.danielescrich.myapplication.room.entity.PlanNutritionEntity

@Dao
interface PlanNutritionDao {

    @Insert
    suspend fun insertarPlan(plan: PlanNutritionEntity): Long

    @Insert
    suspend fun insertarItems(items: List<ItemNutritionEntity>)

    @Query("SELECT * FROM planes_comida WHERE userId = :userId AND finalizado = 0 ORDER BY fechaCreacion DESC LIMIT 1")
    suspend fun obtenerPlanEnCurso(userId: Int): PlanNutritionEntity?

    @Query("SELECT * FROM items_comida WHERE planId = :planId")
    suspend fun obtenerItemsDePlan(planId: Int): List<ItemNutritionEntity>

    @Query("UPDATE items_comida SET completado = :completado WHERE planId = :planId AND dia = :dia AND comida = :comida")
    suspend fun actualizarItemManual(planId: Int, dia: String, comida: String, completado: Boolean)

    @Query("DELETE FROM items_comida WHERE planId = :planId")
    suspend fun eliminarItemsDePlan(planId: Int)

    @Query("DELETE FROM planes_comida WHERE id = :planId")
    suspend fun eliminarPlan(planId: Int)

    @Query("UPDATE planes_comida SET finalizado = 1 WHERE id = :planId")
    suspend fun marcarPlanComoFinalizado(planId: Int)

    @Query("UPDATE planes_comida SET finalizado = 1 WHERE userId = :userId")
    suspend fun finalizarPlanesDelUsuario(userId: Int)

}
