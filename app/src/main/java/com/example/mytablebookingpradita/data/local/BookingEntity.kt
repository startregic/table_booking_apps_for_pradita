package com.example.mytablebookingpradita.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booking_history")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val roomName: String,
    val tableNumber: Int,
    val timestamp: Long,
    var status: String
)