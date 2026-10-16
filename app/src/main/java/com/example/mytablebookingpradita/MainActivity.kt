package com.example.mytablebookingpradita // Pastikan package ini sesuai dengan milikmu

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytablebookingpradita.data.local.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Database dan ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val viewModel: BookingViewModel by viewModels {
            BookingViewModelFactory(database.bookingDao())
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: BookingViewModel) {
    var currentScreen by remember { mutableStateOf("Login") }
    var selectedRoom by remember { mutableStateOf("") }

    when (currentScreen) {
        "Login" -> LoginScreen(onLoginSuccess = { currentScreen = "Dashboard" })
        "Dashboard" -> DashboardScreen(
            onRoomSelected = { room ->
                selectedRoom = room
                currentScreen = "Detail"
            },
            onHistoryClicked = { currentScreen = "History" }
        )
        "Detail" -> RoomDetailScreen(
            roomName = selectedRoom,
            viewModel = viewModel,
            onBackPressed = { currentScreen = "Dashboard" }
        )
        "History" -> HistoryScreen(
            viewModel = viewModel,
            onBackPressed = { currentScreen = "Dashboard" }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var emailOrNim by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tareq Booking", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pradita University", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = emailOrNim, onValueChange = { emailOrNim = it }, label = { Text("NIM / Email (@pradita.ac.id)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val isNim = emailOrNim.all { it.isDigit() } && emailOrNim.isNotEmpty()
                val isPraditaEmail = emailOrNim.endsWith("@pradita.ac.id")

                if (isNim || isPraditaEmail) {
                    onLoginSuccess()
                } else {
                    Toast.makeText(context, "Gunakan NIM atau Email Pradita!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login", fontSize = 18.sp)
        }
    }
}

@Composable
fun DashboardScreen(onRoomSelected: (String) -> Unit, onHistoryClicked: () -> Unit) {
    val rooms = listOf("Kantin Gedung 1", "Stulo Gedung 1", "Stulo Gedung 2", "Perpustakaan")
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Pilih Ruangan", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onHistoryClicked) { Text("Riwayat") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        rooms.forEach { room ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onRoomSelected(room) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(text = room, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(24.dp))
            }
        }
    }
}

@Composable
fun RoomDetailScreen(roomName: String, viewModel: BookingViewModel, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBackPressed, modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) { Text("Kembali") }
        Text(text = roomName, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(10) { index ->
                val tableNumber = index + 1
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp).clickable {
                        viewModel.addBooking(roomName, tableNumber)
                        Toast.makeText(context, "Meja $tableNumber di-booking!", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Table $tableNumber", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Tersedia (Klik u/ Booking)", color = Color(0xFF2E7D32), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: BookingViewModel, onBackPressed: () -> Unit) {
    val historyList by viewModel.bookingHistory.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBackPressed, modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) { Text("Kembali") }
        Text("Riwayat Booking (Offline)", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (historyList.isEmpty()) {
            Text("Belum ada riwayat meja yang dibooking.", color = Color.Gray)
        } else {
            LazyColumn {
                items(historyList) { booking ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${booking.roomName} - Meja ${booking.tableNumber}", fontWeight = FontWeight.Bold)
                            Text("Status: ${booking.status}", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}