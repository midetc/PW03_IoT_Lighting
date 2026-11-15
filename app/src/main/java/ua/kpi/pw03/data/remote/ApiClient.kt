package ua.kpi.pw03.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    fun create(tokenStore: TokenStore): LightingApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthHeaderInterceptor(tokenStore))
            .addInterceptor(MockApiInterceptor())
            .build()
        return Retrofit.Builder()
            .baseUrl("http://iot.local/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LightingApi::class.java)
    }
}
