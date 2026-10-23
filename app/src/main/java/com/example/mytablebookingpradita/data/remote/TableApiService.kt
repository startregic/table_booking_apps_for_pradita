package com.example.mytablebookingpradita.data.remote

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TableApiService {
    @GET("tables")
    suspend fun getTablesByRoom(@Query("room") roomName: String): Response<List<TableResponse>>

    companion object {
        private const val BASE_URL = "https://run.mocky.io/v3/"
        fun create(): TableApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TableApiService::class.java)
        }
    }
}