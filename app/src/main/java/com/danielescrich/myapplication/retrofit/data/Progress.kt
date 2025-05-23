package com.danielescrich.myapplication.retrofit.data

import com.google.gson.annotations.SerializedName

data class Progress(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int,
    val weight: Float,
    val date: String
)
