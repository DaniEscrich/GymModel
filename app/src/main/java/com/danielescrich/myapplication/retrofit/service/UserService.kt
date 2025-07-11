package com.danielescrich.myapplication.retrofit.service


import com.danielescrich.myapplication.retrofit.data.IAFavorite
import com.danielescrich.myapplication.retrofit.data.IAFavoriteRequest
import com.danielescrich.myapplication.retrofit.data.IARequest
import com.danielescrich.myapplication.retrofit.data.IAResponse
import com.danielescrich.myapplication.retrofit.data.ImagePerfilRequest
import com.danielescrich.myapplication.retrofit.data.LoginRequest
import com.danielescrich.myapplication.retrofit.data.ProfileUpdaterRequest
import com.danielescrich.myapplication.retrofit.data.RegisterRequest
import com.danielescrich.myapplication.retrofit.data.UserEntity
import com.danielescrich.myapplication.retrofit.model.ChatResponse
import com.danielescrich.myapplication.retrofit.response.DataProfileResponse
import com.danielescrich.myapplication.retrofit.response.IAFavoriteResponse
import com.danielescrich.myapplication.retrofit.response.ImageProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    suspend fun obtenerImagenPerfil(@Path("id") id: Int): Response<ImageProfileResponse>

    @GET("api/perfil/datos/{id}")
    suspend fun obtenerDatosPerfil(@Path("id") id: Int): Response<DataProfileResponse>


    @GET("/api/ia/favoritos/{userId}")
    suspend fun obtenerFavoritos(@Path("userId") userId: Int): List<IAFavorite>

    @DELETE("/api/ia/favorito/{id}")
    suspend fun eliminarFavorito(@Path("id") id: Int): Response<Void>


    @POST("/api/ia/favorito")
    suspend fun guardarFavorito(@Body data: IAFavoriteRequest): IAFavoriteResponse

    @POST("api/perfil/imagen/{id}")
    suspend fun actualizarImagenPerfil(@Path("id") id: Int,@Body request: ImagePerfilRequest): Response<Unit>

    @POST("api/chat-coach")
    suspend fun enviarMensajeChat(@Body mensaje: Map<String, String>): ChatResponse





}

