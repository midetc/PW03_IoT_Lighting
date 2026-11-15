package ua.kpi.pw03.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LightingApi {
    @GET("zones")
    suspend fun getZones(): ZonesResponse

    @GET("forecast")
    suspend fun getForecast(): List<ForecastDto>

    @GET("secure/zones")
    suspend fun getSecureZones(): ZonesResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}
