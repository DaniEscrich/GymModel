package com.danielescrich.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_comida")
data class ItemNutritionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planId: Int,
    val dia: String,
    val comida: String,
    val completado: Boolean = false
)
