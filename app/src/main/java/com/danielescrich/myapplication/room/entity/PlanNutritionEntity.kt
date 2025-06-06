package com.danielescrich.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planes_comida")
data class PlanNutritionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val fechaCreacion: String,
    val nombrePlan: String = "Plan de Comida",
    val finalizado: Boolean = false
)
