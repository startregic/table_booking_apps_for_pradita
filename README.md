# table_booking_apps_for_pradita

Sebuah aplikasi Android untuk memonitoring ketersediaan meja dan kursi di area ramai secara *real-time*. Pengunjung yang menempati meja dapat melakukan konfirmasi (check-in/booking) dengan cara memindai QR Code di meja tersebut. Hal ini memungkinkan pengunjung lain mengetahui sisa ketersediaan meja dari jarak jauh, sehingga tidak perlu membuang waktu datang ke lokasi jika kapasitas sudah penuh.

## 🛠️ Rekomendasi Tech Stack & Software (Baseline)

Berikut adalah daftar teknologi yang disarankan untuk membangun aplikasi ini dengan fokus pada performa, sinkronisasi *real-time*, dan pemindaian QR Code yang cepat.

### 1. Front-End (Aplikasi Android)
*   **Bahasa Pemrograman:** [Kotlin](https://kotlinlang.org/) (Standar industri dan sangat direkomendasikan untuk pengembangan Android *native* modern).
*   **UI Framework:** [Jetpack Compose](https://developer.android.com/compose) (Untuk membangun antarmuka/UI yang responsif dengan kode yang lebih ringkas).
*   **QR/Barcode Scanner Library:** [Google ML Kit (Barcode Scanning)](https://developers.google.com/ml-kit/vision/barcode-scanning). Library ini sangat cepat, akurat, dan bisa berjalan secara *offline* di perangkat untuk membaca QR Code dari kamera.
*   *(Opsi Cross-Platform)*: Jika kamu berencana membuat aplikasi ini tersedia untuk iOS nantinya, pertimbangkan menggunakan **Flutter** (Dart) dengan *package* seperti `mobile_scanner`.

### 2. Back-End & Database (Sistem Real-Time)
Karena fitur krusial aplikasi ini adalah mengetahui sisa kursi yang berubah setiap detiknya, penggunaan *Backend as a Service* (BaaS) dengan fitur *WebSockets/Real-time* wajib digunakan.
*   **Database Pilihan 1: [Firebase Realtime Database](https://firebase.google.com/) atau Cloud Firestore.** 
    *   *Kenapa?* Ini adalah standar emas untuk aplikasi *real-time*. Ketika satu mahasiswa scan QR dan status meja berubah menjadi "Terpakai", aplikasi di HP mahasiswa lain akan langsung melakukan *update* tanpa perlu mereka melakukan *refresh/pull-to-refresh*.
*   **Database Pilihan 2: [Supabase](https://supabase.com/).** 
    *   *Kenapa?* Alternatif *open-source* dari Firebase yang menggunakan relasional database (PostgreSQL) namun tetap memiliki fitur *Realtime Subscriptions*.

### 3. UI/UX Design & Prototyping
*   **[Figma](https://www.figma.com/):** Gunakan untuk mendesain tampilan aplikasi (Mockup) sebelum mulai mengoding. Sangat penting untuk memvisualisasikan bagaimana alur pengguna saat menekan tombol "Scan" hingga meja berhasil dipesan.

### 4. Manajemen Proyek & Arsitektur
*   **GitHub Projects / Trello / Notion:** Untuk membuat *Kanban board* (Backlog, To-Do, In-Progress, Done) agar proses pengembangan terstruktur.
*   **[Draw.io](https://app.diagrams.net/):** Untuk membuat *Flowchart* (alur logika *booking*) dan Entity Relationship Diagram (ERD) jika menggunakan Supabase.

---

## 🚀 Fitur Utama (MVP - Minimum Viable Product)
1.  **Live Monitoring Dashboard:** Halaman utama yang menampilkan total meja, meja terpakai, dan ketersediaan kursi secara *real-time*.
2.  **QR Code Scanner:** Fitur kamera bawaan untuk *check-in* meja.
3.  **Timer / Auto Check-Out (Opsional namun penting):** Sistem pembatasan waktu (misal: 2 jam per sesi) atau notifikasi untuk mencegah pengguna menahan meja tanpa batas waktu atau lupa *check-out* saat pulang.
