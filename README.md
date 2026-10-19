# FasiLink — Campus Facility & Schedule Manager

Aplikasi desktop kampus berbasis Java untuk pengelolaan jadwal perkuliahan, ketersediaan ruangan, dan inventaris fasilitas akademik. Dibangun menggunakan Java 21 LTS, JavaFX (FXML + CSS), JDBC, dan SQLite.

---

## 1. Stack Teknologi & Spesifikasi

- **Bahasa:** Java 21 LTS (Oracle JDK 21.0.12.1)
- **UI Desktop:** JavaFX 21 (FXML + JavaFX CSS)
- **Basis Data:** SQLite 3 via JDBC (`org.xerial:sqlite-jdbc`)
- **Password Security:** BCrypt Hashing (`org.mindrot:jbcrypt`)
- **Build Tool:** Apache Maven 3.9
- **Testing:** JUnit 5 (Jupiter Engine)

---

## 2. Struktur Arsitektur (Layered Pattern)

```text
JavaFX View (FXML + CSS)
       ↓
Controller (Interaksi UI & Background Task)
       ↓
Service (Business Rules, Authorization, Conflict Check)
       ↓
Repository (Prepared Statement SQL Mapping)
       ↓
SQLite Database (PRAGMA foreign_keys = ON)
```

Struktur Paket:
```text
src/main/java/id/ac/campusfacility/
├── MainApp.java
├── config/             # AppConfig, DatabaseManager, DatabaseInitializer
├── model/              # User, Room, InventoryItem, AcademicTerm, Schedule, dll.
│   └── enums/          # Role, BookingStatus, InventoryLoanStatus, ConflictType
├── dto/                # ScheduleConflict, DashboardSummary
├── repository/         # UserRepository, RoomRepository, InventoryRepository, ScheduleRepository
├── service/            # AuthService, RoomService, InventoryService, ScheduleConflictService, ScheduleService, DashboardService
├── session/            # UserSession (in-memory session & role authorization)
├── controller/         # LoginController, AppShellController, AdminDashboardController, UserDashboardController, RoomManagementController, CampusScheduleController, InventoryManagementController
├── validation/         # ValidationException, AuthorizationException, ConflictException, NotFoundException
└── util/               # PasswordHasher, TimeRange, ViewNavigator
```

---

## 3. Fitur yang Sudah Tersedia (Modul Naura)

### A. Frontend
1. **Halaman Login & Validasi:**
   - Validasi input wajib.
   - Penanganan pesan error aman.
   - Demo credential hint.
2. **Kerangka Aplikasi (AppShell):**
   - Header dinamis dengan identitas akun, role badge, dan tombol logout.
   - Sidebar navigasi adaptif sesuai role (`ADMIN` vs `USER`).
   - Area konten utama dinamis tanpa membuka stage baru.
3. **Dashboard Real-Time:**
   - **Admin Dashboard:** Menampilkan statistik real-time dari database (ruangan aktif, stok inventaris, jadwal perkuliahan, antrean pengajuan menunggu).
   - **User Dashboard:** Menampilkan ringkasan ketersediaan fasilitas dan shortcut cepat ke jadwal, ruangan, dan inventaris.
4. **Manajemen Ruangan:**
   - Tabel daftar ruangan lengkap (kode, nama, gedung, lantai, kapasitas, status).
   - Pencarian real-time dan filter status.
   - Formulir tambah dan ubah ruangan (khusus Admin).
   - Perlindungan penghapusan data berelasi (*soft-deactivate*).
5. **Jadwal Perkuliahan Kampus:**
   - Tabel jadwal kuliah mingguan resmi (hari, jam, mata kuliah, kelas, dosen, ruangan).
   - Filter berdasarkan hari, ruangan, dan pencarian mata kuliah/dosen.
   - Tambah jadwal perkuliahan dengan pencegahan bentrok multi-constraint (khusus Admin).
6. **Manajemen & Katalog Inventaris:**
   - Tabel inventaris barang kampus.
   - Perhitungan stok tersedia real-time dari backend (`totalQuantity - activeLoans`).
   - Tambah dan ubah data inventaris dengan proteksi stok non-negatif (khusus Admin).
   - Perlindungan penghapusan data jika barang sedang aktif dipinjam.

### B. Backend
1. **Autentikasi & Password Hashing:** Menggunakan BCrypt (tidak ada password plaintext).
2. **Otorisasi Service-Layer:** Pengecekan role `ADMIN` pada setiap aksi mutasi data master di lapisan service.
3. **Integritas Database SQLite:** `PRAGMA foreign_keys = ON` pada setiap koneksi dan foreign key constraints lengkap.
4. **Helper Pemeriksaan Konflik Universal (`ScheduleConflictService`):**
   - Aturan overlap: `candidateStart < existingEnd AND candidateEnd > existingStart`.
   - Titik singgung batas jam (misal 08:00–10:00 dan 10:00–12:00) tidak bentrok.
   - Memeriksa bentrok ruangan, dosen, kelas, serta kapasitas ruangan.
   - Siap dipakai ulang oleh modul Arsal (peminjaman ruangan dan pencarian alternatif).

---

## 4. Akun Percobaan (Demo Seed)

Basis data akan dibuat otomatis dan diisi data seed awal saat aplikasi pertama kali dijalankan:

| Role | Username | Password | Deskripsi |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Memiliki hak penuh menambah/mengubah/menghapus fasilitas & jadwal |
| **User** | `user` | `user123` | Hak baca jadwal, ketersediaan ruangan, dan katalog inventaris |

File database tersimpan di: `data/campus_facility.db`.

---

## 5. Cara Menjalankan Aplikasi & Pengujian

### Prasyarat
- Java 21 LTS terpasang di sistem (`java -version`).
- Terminal PowerShell / Command Prompt di root direktori proyek.

### Perintah Build & Eksekusi

1. **Menjalankan Seluruh Unit & Integration Test:**
   ```bash
   .\mvnw.cmd test
   ```

2. **Kompilasi Proyek:**
   ```bash
   .\mvnw.cmd clean compile
   ```

3. **Menjalankan Aplikasi Desktop JavaFX:**
   ```bash
   .\mvnw.cmd javafx:run
   ```

---

## 6. Pembagian Tugas

- **Naura (Anggota 1):** Manajemen fasilitas, data dasar akademik, autentikasi, otorisasi, dashboard, jadwal dasar, inventaris, dan helper pemeriksaan konflik.
- **Arsal (Anggota 2):** Transaksi peminjaman ruangan dan barang, persetujuan admin (approval), pengembalian barang, serta algoritma pencarian alternatif jadwal kuliah.
