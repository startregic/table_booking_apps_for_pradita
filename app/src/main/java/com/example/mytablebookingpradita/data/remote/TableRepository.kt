package com.example.mytablebookingpradita.data.remote

sealed class ApiState<out T> {
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data: T) : ApiState<T>()
    data class Error(val message: String) : ApiState<Nothing>()
    object Empty : ApiState<Nothing>()
}

class TableRepository(private val api: TableApiService) {
    suspend fun fetchTables(roomName: String): ApiState<List<TableResponse>> {
        return try {
            val response = api.getTablesByRoom(roomName)
            if (response.isSuccessful) {
                val body = response.body()
                if (body.isNullOrEmpty()) ApiState.Empty
                else ApiState.Success(body)
            } else {
                ApiState.Error("HTTP Gagal: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiState.Error("Koneksi Error: ${e.localizedMessage}")
        }
    }
}