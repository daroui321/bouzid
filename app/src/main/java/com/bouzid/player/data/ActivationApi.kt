package com.bouzid.player.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

data class ActivationConfig(
    val activation: ActivationSettings = ActivationSettings()
)

data class ActivationSettings(
    val enabled: Boolean = false,
    val message: String = ""
)

data class ActivationRequest(
    val email: String
)

data class ActivationResponse(
    val success: Boolean = false,
    val message: String = ""
)

interface ActivationService {
    @GET("config.json")
    suspend fun getConfig(): ActivationConfig

    @POST("activate.php")
    suspend fun activate(@Body request: ActivationRequest): ActivationResponse
}

object ActivationApi {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Config.ACTIVATION_CONFIG_URL.substringBeforeLast("/") + "/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ActivationService = retrofit.create(ActivationService::class.java)
}
