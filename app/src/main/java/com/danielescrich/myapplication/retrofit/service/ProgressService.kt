package com.danielescrich.myapplication.retrofit.service

import com.danielescrich.myapplication.retrofit.data.Progress
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProgressService {

    @GET("api/progress/{userId}")
    suspend fun getProgressByUser(@Path("userId") userId: Int): List<Progress>

    @POST("api/progress")
    suspend fun addProgress(@Body progress: Progress): Progress
}


