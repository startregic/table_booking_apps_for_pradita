package com.example.mytablebookingpradita.data.remote

import com.google.gson.annotations.SerializedName

data class TableResponse(
    @SerializedName("table_id") val tableId: Int,
    @SerializedName("room_name") val roomName: String,
    @SerializedName("status") val status: String
)