package com.example.tablebookingpradita // Sesuaikan dengan nama package kamu!

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Mengatur perpindahan halaman
@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("Login") }
    var selectedRoom by remember { mutableStateOf("") }

    when (currentScreen) {
        "Login" -> LoginScreen(onLoginSuccess = { currentScreen = "Dashboard" })
        "Dashboard" -> DashboardScreen(onRoomSelected = { room ->
            selectedRoom = room
            currentScreen = "Detail"
        })
        "Detail" -> RoomDetailScreen(roomName = selectedRoom, onBackPressed = { currentScreen = "Dashboard" })
    }
}

// 1. Halaman Login
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var emailOrNim by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Judul sudah diubah menjadi Tareq Booking
        Text(text = "Tareq Booking", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Pradita University", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = emailOrNim,
            onValueChange = { emailOrNim = it },
            label = { Text("NIM / Email (@pradita.ac.id)") },
            placeholder = { Text("Contoh: 2410101017") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Validasi NIM (angka) atau email Pradita
                val isNim = emailOrNim.all { it.isDigit() } && emailOrNim.isNotEmpty()
                val isPraditaEmail = emailOrNim.endsWith("@pradita.ac.id")

                if (isNim || isPraditaEmail) {
                    onLoginSuccess()
                } else {
                    Toast.makeText(context, "Gunakan NIM atau Email Pradita!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login", fontSize = 18.sp)
        }
    }
}

// 2. Halaman Pilih Ruangan (4 Opsi)
@Composable
fun DashboardScreen(onRoomSelected: (String) -> Unit) {
    val rooms = listOf("Kantin Gedung 1", "Stulo Gedung 1", "Stulo Gedung 2", "Perpustakaan")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Pilih Ruangan", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp, top = 16.dp))

        rooms.forEach { room ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onRoomSelected(room) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = room, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// 3. Halaman Detail Meja (10 Meja)
@Composable
fun RoomDetailScreen(roomName: String, onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBackPressed, modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) {
            Text("Kembali")
        }

        Text(text = roomName, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(10) { index ->
                val tableNumber = index + 1
                // Logika dummy untuk simulasi penuh/kosong
                val terpakai = if (tableNumber % 3 == 0) 4 else (tableNumber % 4)
                val isPenuh = terpakai == 4

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPenuh) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Table $tableNumber", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPenuh) "Penuh (4/4)" else "Terpakai $terpakai/4",
                            color = if (isPenuh) Color.Red else Color(0xFF2E7D32),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}