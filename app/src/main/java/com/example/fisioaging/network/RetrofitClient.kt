package com.example.fisioaging.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient


object RetrofitClient {
    private const val BASE_URL = "http://18.191.134.108:8080/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

    fun create(token: String? = null): ApiService {

        val clientBuilder = OkHttpClient.Builder()

        if (!token.isNullOrEmpty()) {
            clientBuilder.addInterceptor(AuthInterceptor(token))
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}