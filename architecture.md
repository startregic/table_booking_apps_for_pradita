Table Booking Apps 🎓🪑

Dokumen ini merupakan kelanjutan arsitektur teknis yang berfokus pada integrasi backend real-time, pengujian (testing), dan strategi deployment (P3 & P4).

🚀 Arsitektur Data & Integrasi Real-Time

Pada fase ini, fokus utama adalah bagaimana aplikasi berkomunikasi dengan database secara real-time tanpa lag, yang menjadi inti dari aplikasi booking meja ini.

### 1. Struktur Folder (Clean Architecture - Simplified)
Struktur ini disusun agar mudah *di-scale* (dikembangkan) ke depannya:

```text
app/src/main/java/com/pradita/tablebooking/
├── di/                     # Dependency Injection (Hilt / Koin)
├── data/
│   ├── models/             # Data class (Table, User, Booking)
│   ├── remote/             # Konfigurasi API / Firebase Realtime DB
│   └── repository/         # Implementasi Repository
├── ui/
│   ├── components/         # Reusable Jetpack Compose (Button, Card, dll)
│   ├── screens/            
│   │   ├── dashboard/      # UI Dashboard 
│   │   ├── scan/           # UI QR Scanner
│   │   └── booking/        # UI Proses Booking
│   └── viewmodels/         # State Management (ViewModel)
├── utils/                  # Helper class (Constants, Extensions)
└── MainActivity.kt         # Entry point aplikasi

2. Alur Data Real-Time (Data Flow)

Aplikasi ini menggunakan pola Observer melalui WebSockets / Realtime Subscriptions.

Subscribe: Saat DashboardScreen dibuka, aplikasi memanggil repository.listenToTables(). Ini membuka koneksi WebSocket ke Supabase/Firebase.

Action (Scan): Mahasiswa A memindai QR Code di meja "M-01". Aplikasi mengirim request HTTP POST/PATCH ke server untuk mengubah status meja menjadi OCCUPIED.

Broadcast: Server menerima perubahan, lalu secara otomatis mengirimkan event update ke semua klien (HP mahasiswa lain) yang sedang terhubung.

Rebuild UI: State Management (Riverpod) menangkap event tersebut, memperbarui state internal, dan UI DashboardScreen pada HP Mahasiswa B otomatis mengubah warna meja "M-01" menjadi merah tanpa perlu refresh.

🛡️ Testing, Security, & Deployment

Fase ini memastikan aplikasi stabil, aman dari kecurangan, dan siap dirilis kepada mahasiswa Pradita.

1. Strategi Pengujian (Testing Architecture)

Sesuai standar kualitas, aplikasi harus melewati pengujian otomatis:

Unit Testing (Logic & State):

Menguji BookingController.

Test Case: Memastikan validasi error muncul jika format data QR tidak sesuai. Memastikan state berubah dari loading -> success saat data berhasil di-fetch.

Widget Testing (UI):

Menguji 6 State UI pada fitur Booking.

Test Case: Memastikan CircularProgressIndicator muncul saat isLoading bernilai true. Memastikan tombol Submit ter-disable saat proses booking berjalan.

Mocking: Menggunakan library seperti mockito atau mocktail untuk mensimulasikan respons dari Supabase/Firebase selama testing agar tidak membebani database produksi.

2. Keamanan (Security Rules & Validasi)

Untuk mencegah eksploitasi (misal: mahasiswa booking meja dari kosan tanpa datang ke kampus):

Row Level Security (RLS): Diterapkan di Supabase agar pengguna hanya bisa melihat data meja, namun hanya bisa melakukan insert ke tabel bookings jika mereka menyertakan user_id mereka sendiri yang terotentikasi.

Location/Network Validation (Opsional/Pengembangan): Memvalidasi apakah HP pengguna terkoneksi ke Wi-Fi kampus (Pradita Network) atau menggunakan GPS (Geofencing radius 100m dari area kantin) saat proses scan QR terjadi.

QR Code Dinamis (Advance): Jika menggunakan QR statis berisiko difoto dan di-scan dari rumah, pertimbangkan untuk menggunakan tablet di meja yang menampilkan QR Code yang berubah setiap 30 detik (seperti token OTP).

3. CI/CD & Deployment

GitHub Actions: Setiap kode yang di-push ke branch main akan secara otomatis menjalankan perintah flutter test. Jika ada tes yang gagal, kode tidak bisa digabungkan (merge).

Build Release: Mengonfigurasi build.gradle (Android) untuk memisahkan environment (Staging untuk tes internal, Production untuk rilis akhir) sebelum di-build menjadi format
