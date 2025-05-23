package com.danielescrich.myapplication.retrofit.service


import com.danielescrich.myapplication.retrofit.data.RankingUser
import retrofit2.http.GET

interface RankingService {
    @GET("/api/ranking")
    suspend fun getRanking(): List<RankingUser>
}

