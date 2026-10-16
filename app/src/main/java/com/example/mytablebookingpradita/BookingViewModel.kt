package com.example.mytablebookingpradita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mytablebookingpradita.data.local.BookingDao
import com.example.mytablebookingpradita.data.local.BookingEntity
import kotlinx.coroutines.launch

class BookingViewModel(private val dao: BookingDao) : ViewModel() {
    // Mengambil data riwayat secara real-time
    val bookingHistory = dao.getAllBookings()

    // Menyimpan data booking baru ke database lokal
    fun addBooking(roomName: String, tableNumber: Int) {
        viewModelScope.launch {
            val newBooking = BookingEntity(
                roomName = roomName,
                tableNumber = tableNumber,
                timestamp = System.currentTimeMillis(),
                status = "Aktif"
            )
            dao.insertBooking(newBooking)
        }
    }
}

// Factory untuk membuat ViewModel yang membutuhkan parameter (Dao)
class BookingViewModelFactory(private val dao: BookingDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}