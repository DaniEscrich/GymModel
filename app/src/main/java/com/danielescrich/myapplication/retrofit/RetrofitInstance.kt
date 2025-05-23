package com.danielescrich.myapplication.retrofit

import com.danielescrich.myapplication.retrofit.common.Constants
import com.danielescrich.myapplication.retrofit.service.ProgressService
import com.danielescrich.myapplication.retrofit.service.RankingService
import com.danielescrich.myapplication.retrofit.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
    val progressService: ProgressService by lazy {
        retrofit.create(ProgressService::class.java)
    }
    val rankingService: RankingService by lazy {
        retrofit.create(RankingService::class.java)
    }


}
