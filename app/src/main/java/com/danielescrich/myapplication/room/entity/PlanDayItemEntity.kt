package com.danielescrich.myapplication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_dia_items")
data class PlanDayItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planId: Int,
    val dia: String,
    val ejercicio: String,
    val completado: Boolean = false
)
