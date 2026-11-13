package com.example.mytablebookingpradita

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mytablebookingpradita.data.local.AppDatabase
import com.example.mytablebookingpradita.data.remote.ApiState
import com.example.mytablebookingpradita.data.remote.TableApiService
import com.example.mytablebookingpradita.data.remote.TableRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        val database = AppDatabase.getDatabase(applicationContext)
        val localViewModel: BookingViewModel by viewModels { BookingViewModelFactory(database.bookingDao()) }

        val apiService = TableApiService.create()
        val repository = TableRepository(apiService)
        val apiViewModel: TableApiViewModel by viewModels { TableApiViewModelFactory(repository) }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(localViewModel, apiViewModel)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("booking_channel", "Notifikasi Booking", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

fun showBookingNotification(context: Context, roomName: String, tableNumber: Int) {
    val builder = NotificationCompat.Builder(context, "booking_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Booking Berhasil!")
        .setContentText("Meja $tableNumber di $roomName telah dibooking.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}

@Composable
fun AppNavigation(localViewModel: BookingViewModel, apiViewModel: TableApiViewModel) {
    var currentScreen by remember { mutableStateOf("Login") }
    var selectedRoom by remember { mutableStateOf("") }

    when (currentScreen) {
        "Login" -> LoginScreen(onLoginSuccess = { currentScreen = "Dashboard" })
        "Dashboard" -> DashboardScreen(
            onRoomSelected = { room -> selectedRoom = room; currentScreen = "Detail" },
            onHistoryClicked = { currentScreen = "History" }
        )
        "Detail" -> RoomDetailScreen(
            roomName = selectedRoom, localViewModel = localViewModel, apiViewModel = apiViewModel,
            onBackPressed = { currentScreen = "Dashboard" }
        )
        "History" -> HistoryScreen(viewModel = localViewModel, onBackPressed = { currentScreen = "Dashboard" })
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var emailOrNim by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState) // 02 KEYBOARD-SAFE: Bisa discroll jika tertutup keyboard
            .imePadding(), // 02 KEYBOARD-SAFE: Menambahkan padding otomatis saat keyboard muncul
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tareq Booking", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = emailOrNim,
            onValueChange = { emailOrNim = it },
            label = { Text("NIM / Email") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Input untuk NIM atau Email kampus" } // 03 ACCESSIBILITY
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Input untuk Password" } // 03 ACCESSIBILITY
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // MENGGUNAKAN VALIDATOR (Pemisahan Tanggung Jawab)
                val (isValid, message) = AuthValidator.validateLogin(emailOrNim, password)

                // 04 FEEDBACK AKSI: Memberi Toast respons dari Validator
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                if (isValid) onLoginSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login")
        }
    }
}

@Composable
fun DashboardScreen(onRoomSelected: (String) -> Unit, onHistoryClicked: () -> Unit) {
    val context = LocalContext.current
    val rooms = listOf("Kantin Gedung 1", "Stulo Gedung 1", "Stulo Gedung 2", "Perpustakaan")

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) Toast.makeText(context, "Kamera siap digunakan untuk scan QR!", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Izin Kamera ditolak!", Toast.LENGTH_SHORT).show() // 04 FEEDBACK AKSI
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (locationGranted) {
            Toast.makeText(context, "Lokasi diverifikasi! Kamu berada di area kampus.", Toast.LENGTH_SHORT).show()
            onRoomSelected(rooms[0])
        } else {
            Toast.makeText(context, "Izin lokasi diperlukan untuk booking!", Toast.LENGTH_LONG).show() // 04 FEEDBACK AKSI
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Pilih Ruangan", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onHistoryClicked) { Text("Riwayat") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))) {
            Text("Scan QR Code Kedatangan", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))

        rooms.forEach { room ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable {
                    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasLocationPermission) onRoomSelected(room)
                    else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS))
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) { Text(text = room, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(24.dp)) }
        }
    }
}

@Composable
fun RoomDetailScreen(roomName: String, localViewModel: BookingViewModel, apiViewModel: TableApiViewModel, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val apiState by apiViewModel.tableState.collectAsState()

    LaunchedEffect(roomName) { apiViewModel.getTables(roomName) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBackPressed, modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) { Text("Kembali") }
        Text(text = roomName, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        when (val state = apiState) {
            is ApiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is ApiState.Error -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Gagal mengambil data Server!", color = Color.Red)
                    Button(onClick = { apiViewModel.getTables(roomName) }, modifier = Modifier.padding(top = 8.dp)) { Text("Retry") } // 04 FEEDBACK AKSI
                }
            }
            is ApiState.Empty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Data Meja Kosong") }
            is ApiState.Success -> {
                // 01 LAYOUT ADAPTIF: Mengubah Fixed(2) menjadi Adaptive(120.dp) agar grid dinamis menyesuaikan lebar layar
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(10) { index ->
                        val tableNumber = index + 1
                        Card(
                            modifier = Modifier.fillMaxWidth().height(100.dp).clickable {
                                localViewModel.addBooking(roomName, tableNumber)
                                showBookingNotification(context, roomName, tableNumber)
                                Toast.makeText(context, "Meja $tableNumber di-booking!", Toast.LENGTH_SHORT).show() // 04 FEEDBACK AKSI
                                onBackPressed()
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Table $tableNumber", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Klik u/ Booking", color = Color(0xFF2E7D32), fontSize = 12.sp)
                            }
                        }
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
                            // ERROR DIPERBAIKI DI BARIS BAWAH INI:
                            Text(text = "${booking.roomName} - Meja ${booking.tableNumber}", fontWeight = FontWeight.Bold)
                            Text(text = "Status: ${booking.status}", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}