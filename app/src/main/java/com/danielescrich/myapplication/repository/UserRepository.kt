package com.danielescrich.myapplication.repository

import com.danielescrich.myapplication.retrofit.RetrofitInstance
import com.danielescrich.myapplication.retrofit.data.LoginRequest
import com.danielescrich.myapplication.retrofit.data.RegisterRequest
import com.danielescrich.myapplication.retrofit.data.UserEntity

class UserRepository {

    suspend fun registerUser(registerRequest: RegisterRequest): UserEntity {
        return RetrofitInstance.userService.register(registerRequest)
    }

    suspend fun loginUser(loginRequest: LoginRequest): UserEntity {
        return RetrofitInstance.userService.login(loginRequest)
    }

}

