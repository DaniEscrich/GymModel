package com.danielescrich.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planes_entrenamiento")
data class PlanTrainingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val fechaCreacion: String,
    val nombrePlan: String = "Plan de Entrenamiento",
    val enCurso: Boolean = true
)

