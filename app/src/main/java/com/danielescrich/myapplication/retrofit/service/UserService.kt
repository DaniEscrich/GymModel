package com.danielescrich.myapplication.retrofit.service


import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import com.danielescrich.myapplication.retrofit.data.LoginRequest
import com.danielescrich.myapplication.retrofit.data.ProfileUpdaterRequest
import com.danielescrich.myapplication.retrofit.data.RegisterRequest
import com.danielescrich.myapplication.retrofit.data.UserEntity
import com.danielescrich.myapplication.retrofit.response.DatosPerfilResponse
import com.danielescrich.myapplication.retrofit.response.ImagenPerfilResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): UserEntity

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): UserEntity

    @POST("api/ia")
    suspend fun generarPlanIA(@Body request: IARequest): IAResponse

    @POST("api/consejo")
    suspend fun generarConsejoDelDia(): IAResponse

    @POST("api/perfil/actualizar")
    suspend fun actualizarPerfil(@Body request: ProfileUpdaterRequest): Response<Unit>

    @GET("api/perfil/imagen/{id}")
    suspend fun obtenerImagenPerfil(@Path("id") id: Int): Response<ImagenPerfilResponse>

    @GET("api/perfil/datos/{id}")
    suspend fun obtenerDatosPerfil(@Path("id") id: Int): Response<DatosPerfilResponse>



}

