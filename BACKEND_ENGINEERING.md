# BACKEND ENGINEERING SPECIFICATION

## Campus Facility & Schedule Manager — Java Desktop Application

> **Dokumen ini adalah spesifikasi engineering backend untuk Antigravity.** Baca bersama `IMPLEMENTATION_PLAN.md` dan `FRONTEND_ENGINEERING.md` sebelum mengubah kode.
>
> **Aturan utama:** inspeksi repository terlebih dahulu, implementasikan per tahap, pertahankan aplikasi tetap dapat dibangun, dan jangan menyatakan fitur selesai sebelum build serta pengujian yang relevan benar-benar dijalankan.

---

## 1. Tujuan Backend

Backend mengelola data dan memastikan aturan bisnis aplikasi tetap benar, terlepas dari tampilan yang digunakan. Backend bertanggung jawab atas:

1. autentikasi, sesi aplikasi, dan otorisasi `ADMIN`/`USER`;
2. pengelolaan master data ruangan, inventaris, semester, mata kuliah, kelompok kelas, dosen, dan jadwal;
3. validasi konflik jadwal kelas, dosen, dan ruangan;
4. pengajuan, persetujuan, penolakan, dan pembatalan peminjaman ruangan;
5. pengajuan, persetujuan, penolakan, serah-terima, dan pengembalian barang;
6. pencarian alternatif jadwal yang hanya menampilkan kandidat yang lolos *hard constraints*;
7. permintaan perubahan jadwal dan pembaruan jadwal resmi setelah persetujuan;
8. pengumpulan data dashboard, riwayat, dan laporan sederhana;
9. integritas transaksi, validasi, logging yang aman, dan pengujian.

Backend bukan server web. Untuk MVP, logika bisnis berjalan di dalam aplikasi desktop JavaFX dan akses database menggunakan JDBC.

## 2. Batasan dan Asumsi MVP

Gunakan asumsi berikut kecuali `IMPLEMENTATION_PLAN.md` atau repository yang sudah ada memberikan keputusan yang lebih spesifik.

- Jenis aplikasi: desktop, bukan web/API server.
- Bahasa: Java 21 LTS; sesuaikan bila environment kampus mensyaratkan versi lain.
- Build tool: Maven.
- UI: JavaFX/FXML. Backend tidak boleh bergantung pada kelas atau komponen JavaFX.
- Database: SQLite melalui JDBC, dengan database lokal pada satu komputer.
- Role: `ADMIN` dan `USER`.
- Jadwal kuliah: jadwal berulang mingguan di semester aktif.
- Perubahan jadwal yang disetujui memperbarui slot jadwal berulang untuk sisa semester aktif.
- Peminjaman ruangan berlaku untuk satu tanggal dan satu rentang waktu pada setiap pengajuan.
- Satu pengajuan peminjaman barang berisi satu jenis barang dan satu jumlah. Pengembalian parsial tidak termasuk MVP.
- Tidak ada integrasi SIAKAD, email, WhatsApp, cloud, atau database bersama pada MVP.
- Data ketersediaan dosen dikelola admin. Data yang belum diisi berarti **belum terverifikasi**, bukan berarti dosen bebas.
- Semua teks antarmuka/pesan validasi menggunakan Bahasa Indonesia yang jelas.
- Gunakan soft deactivate (`is_active = false`) untuk data master yang sudah direferensikan oleh riwayat atau jadwal.

### Di luar cakupan MVP

- Login SSO kampus.
- Sinkronisasi data akademik eksternal.
- REST API atau microservices.
- Notifikasi eksternal.
- Pengoperasian multi-komputer dengan SQLite yang berbagi file melalui jaringan.
- Peminjaman berulang, pengembalian sebagian barang, audit trail enterprise, dan workflow persetujuan bertingkat.

---

## 3. Stack dan Dependensi Backend

| Kebutuhan | Pilihan awal | Aturan |
|---|---|---|
| Bahasa | Java 21 | Gunakan versi yang konsisten pada `pom.xml`, README, dan konfigurasi IDE. |
| Database | SQLite | Database file lokal di `data/`, bukan di `src/main/resources`. |
| Driver | SQLite JDBC | Gunakan versi stabil yang kompatibel dengan Java yang dipakai. |
| Query | JDBC + `PreparedStatement` | Dilarang menggabungkan input pengguna ke string SQL. |
| Password hashing | BCrypt dengan library yang terpelihara, atau algoritma password hashing yang sudah tersedia di repository | Password tidak boleh disimpan atau dicatat sebagai teks biasa. Jangan membuat algoritma hashing sendiri. |
| Testing | JUnit 5 | Uji service dan aturan bisnis; tambahkan pengujian repository/integrasi untuk SQLite. |
| Logging | `java.util.logging` atau logging library yang sudah ada | Jangan mencatat password, hash password, atau data sensitif yang tidak diperlukan. Hindari menambah dependency logging tanpa alasan. |
| Date/time | `java.time` | Gunakan `LocalDate`, `LocalTime`, `LocalDateTime`, `DayOfWeek`, dan `Duration`. |

### Aturan dependency

- Audit `pom.xml` terlebih dahulu. Jangan menambahkan dependency duplikat jika fungsi yang dibutuhkan sudah tersedia.
- Hindari framework ORM besar untuk MVP; gunakan JDBC agar alur SQL mudah dipahami dan dipresentasikan.
- Semua dependency tambahan harus beralasan, kompatibel, dan tercatat di dokumentasi.
- Jangan menggunakan API yang sudah deprecated jika tersedia opsi stabil.

---

## 4. Arsitektur dan Batas Antarlapisan

Gunakan alur berikut:

```text
JavaFX View (FXML)
       ↓
Controller
       ↓
Service (business rules + authorization)
       ↓
Repository (SQL / mapping database)
       ↓
SQLite database
```

Backend terdiri dari model, DTO, enum, repository, service, validator, konfigurasi database, dan utilitas waktu. Backend tidak boleh mengetahui `Button`, `TableView`, `Alert`, `Stage`, `FXML`, atau node JavaFX lain.

### Tanggung jawab tiap lapisan

| Lapisan | Tanggung jawab | Tidak boleh |
|---|---|---|
| `model` | Representasi entity domain | Menjalankan SQL atau menampilkan UI. |
| `dto` | Membawa input/output beberapa entity atau hasil query gabungan | Mengandung logika persistensi kompleks. |
| `repository` | Query SQL, insert/update/delete, mapping `ResultSet` | Memutuskan apakah request bisnis boleh disetujui. |
| `service` | Validasi aturan bisnis, hak akses, state transition, koordinasi transaksi | Mengakses node JavaFX atau membangun pesan dialog. |
| `config` | Database path, koneksi, schema initialization/versioning, konfigurasi aplikasi | Menyimpan aturan fitur spesifik di satu class besar. |
| `validator` atau helper domain | Aturan murni yang dapat dipakai ulang, seperti overlap waktu | Bergantung pada JavaFX. |
| `session` | Informasi pengguna yang sedang login selama aplikasi hidup | Menjadi pengganti otorisasi di service. |
| `controller` | Memanggil service dan mengubah hasil menjadi tampilan | Menjalankan SQL atau menyalin aturan bisnis kompleks. |

### Prinsip utama

1. Semua validasi penting harus berada pada service/domain layer, bukan hanya di UI.
2. Repository tidak mengatur approval, otorisasi, atau aturan jadwal; repository hanya menjalankan operasi data.
3. Controller tidak menjalankan SQL secara langsung.
4. Aturan konflik jadwal harus memiliki satu implementasi yang dipakai saat membuat jadwal, mencari kandidat, dan menyetujui perubahan.
5. Gunakan transaksi database untuk operasi yang memodifikasi lebih dari satu data atau perlu validasi ulang atomik.
6. Hindari satu class `Service` atau `Repository` raksasa untuk semua modul.
7. Gunakan enum untuk role/status dan DTO untuk hasil pencarian alternatif.
8. Bila satu service memerlukan data dari beberapa repository, injeksikan repository tersebut melalui constructor atau dependency factory yang disepakati.

---

## 5. Struktur Package yang Direkomendasikan

Adaptasikan struktur ini dengan repository yang sudah tersedia. Jangan membuat ulang project jika struktur yang sekarang sudah rapi.

```text
src/main/java/id/ac/campusfacility/
├── MainApp.java
├── config/
│   ├── AppConfig.java
│   ├── DatabaseManager.java
│   ├── DatabaseInitializer.java
│   ├── DatabaseMigrator.java            # bila schema versioning dipakai
│   └── DependencyContainer.java         # factory sederhana, tanpa framework DI jika tidak perlu
├── model/
│   ├── User.java
│   ├── AcademicTerm.java
│   ├── Room.java
│   ├── InventoryItem.java
│   ├── Course.java
│   ├── ClassGroup.java
│   ├── Lecturer.java
│   ├── LecturerAvailability.java
│   ├── Schedule.java
│   ├── RoomBookingRequest.java
│   ├── InventoryLoanRequest.java
│   └── ScheduleChangeRequest.java
├── model/enums/
│   ├── Role.java
│   ├── BookingStatus.java
│   ├── InventoryLoanStatus.java
│   └── ScheduleChangeStatus.java
├── dto/
│   ├── LoginResult.java
│   ├── RoomAvailabilityQuery.java
│   ├── RoomAvailabilityResult.java
│   ├── AlternativeScheduleQuery.java
│   ├── AlternativeScheduleCandidate.java
│   ├── ScheduleConflict.java
│   ├── DashboardSummary.java
│   ├── ServiceResult.java                 # bila pola result object dipilih
│   └── ...
├── repository/
│   ├── UserRepository.java
│   ├── AcademicTermRepository.java
│   ├── RoomRepository.java
│   ├── InventoryRepository.java
│   ├── CourseRepository.java
│   ├── ClassGroupRepository.java
│   ├── LecturerRepository.java
│   ├── LecturerAvailabilityRepository.java
│   ├── ScheduleRepository.java
│   ├── RoomBookingRepository.java
│   ├── InventoryLoanRepository.java
│   └── ScheduleChangeRepository.java
├── repository/sqlite/                    # implementasi jika interface dipisah
├── service/
│   ├── AuthService.java
│   ├── AcademicTermService.java
│   ├── RoomService.java
│   ├── InventoryService.java
│   ├── AcademicDataService.java
│   ├── LecturerAvailabilityService.java
│   ├── ScheduleConflictService.java
│   ├── ScheduleService.java
│   ├── RoomBookingService.java
│   ├── InventoryLoanService.java
│   ├── AlternativeScheduleService.java
│   ├── ScheduleChangeService.java
│   └── DashboardService.java
├── session/
│   └── UserSession.java
├── validation/
│   ├── ValidationException.java
│   ├── AuthorizationException.java
│   ├── NotFoundException.java
│   ├── ConflictException.java
│   └── InvalidStateTransitionException.java
└── util/
    ├── TimeRange.java
    ├── TimeFormat.java
    └── PasswordHasher.java

src/test/java/id/ac/campusfacility/
├── service/
├── validation/
├── repository/
└── integration/
```

`repository/` boleh berisi implementasi JDBC langsung jika tim ingin struktur lebih sederhana. Jika interface dan implementasi dipisahkan, lakukan konsisten; jangan membuat abstraksi yang tidak digunakan.

---

## 6. Domain Model, Enum, dan DTO

### 6.1 Model domain

Buat entity untuk tabel utama. Setiap entity harus memiliki field yang diperlukan, constructor yang masuk akal, getter, dan validasi dasar yang tidak bergantung pada database. Gunakan `Long`/`long` untuk ID sesuai gaya repository, serta `LocalDate`/`LocalTime` untuk tanggal/jam ketika dipakai oleh service.

Jangan membuat entity memiliki query SQL atau referensi ke controller.

### 6.2 Enum

Gunakan enum berikut dan konversikan ke/dari string database di satu tempat yang konsisten:

```java
public enum Role {
    ADMIN,
    USER
}
```

```java
public enum BookingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

```java
public enum InventoryLoanStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    HANDED_OVER,
    RETURNED
}
```

```java
public enum ScheduleChangeStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
```

Jangan menggunakan string seperti `"approved"`, `"APPROVED"`, dan `"Approve"` secara acak di berbagai class. Database menyimpan satu format kanonis, disarankan huruf kapital sesuai enum.

### 6.3 DTO yang direkomendasikan

- `LoginResult`: user yang telah terautentikasi tanpa password/hash.
- `RoomAvailabilityQuery`: tanggal, jam mulai, jam selesai, jumlah peserta, dan filter opsional.
- `RoomAvailabilityResult`: entity ruangan, status tersedia, dan alasan jika tidak tersedia.
- `AlternativeScheduleQuery`: jadwal awal, preferensi hari/waktu, rentang kandidat, serta aturan pencarian yang diperlukan.
- `AlternativeScheduleCandidate`: hari, jam mulai/selesai, ruangan, durasi, skor preferensi, dan catatan validasi.
- `ScheduleConflict`: jenis konflik (`ROOM`, `LECTURER`, `CLASS_GROUP`, `LECTURER_AVAILABILITY`, `CAPACITY`, `BOOKING`), pesan yang dapat ditampilkan, dan referensi record yang bermasalah jika aman.
- `DashboardSummary`: angka ringkasan yang dibutuhkan satu dashboard; hindari query berulang dari controller.

DTO tidak boleh membawa `Connection`, `PreparedStatement`, `ResultSet`, atau komponen JavaFX.

---

## 7. Database Configuration dan Lifecycle

### 7.1 Lokasi database

- Simpan database lokal di direktori `data/` pada root runtime aplikasi atau direktori aplikasi yang dapat ditulis.
- Jangan mengemas database runtime di `src/main/resources`, karena folder resource pada packaging tertentu dapat bersifat read-only.
- Pastikan direktori dibuat bila belum tersedia.
- Path database harus ditetapkan pada satu tempat seperti `AppConfig`, bukan tersebar di repository.
- Jangan commit database yang berisi data nyata. Untuk demo, gunakan data dummy dan ikuti `.gitignore`.

### 7.2 `DatabaseManager`

`DatabaseManager` bertanggung jawab menyediakan koneksi JDBC SQLite. Implementasi minimal:

- membangun URL `jdbc:sqlite:<absolute-path>`;
- membuat parent directory jika belum tersedia;
- mengembalikan koneksi baru untuk unit of work;
- menjalankan `PRAGMA foreign_keys = ON` pada **setiap connection**;
- menerapkan `busy_timeout` yang wajar bila diperlukan;
- menangani error koneksi dengan exception internal yang jelas;
- tidak menyimpan satu `Connection` global untuk seluruh umur aplikasi.

Gunakan `try-with-resources` untuk menutup koneksi, statement, dan result set. Untuk transaksi, gunakan satu koneksi yang sama sepanjang operasi.

### 7.3 Inisialisasi dan schema versioning

- Buat `DatabaseInitializer` atau class sejenis yang dipanggil saat aplikasi mulai sebelum repository dipakai.
- Gunakan `CREATE TABLE IF NOT EXISTS` untuk pembuatan awal dan seed demo yang idempotent.
- Jika schema kemungkinan berubah selama pengembangan, simpan versi schema melalui tabel `schema_version` atau `PRAGMA user_version` dan jalankan migrasi terurut.
- Jangan menghapus dan membuat ulang database setiap kali aplikasi dibuka.
- Jangan menjalankan seed demo berulang sehingga duplikat. Gunakan kode/username unik atau pemeriksaan keberadaan data.
- Bila migrasi gagal, batalkan startup dengan pesan log yang jelas; jangan diam-diam melanjutkan dengan schema setengah jadi.

### 7.4 Transaksi dan SQLite

- Gunakan `connection.setAutoCommit(false)` untuk transaksi multi-langkah, lalu `commit()` jika semua berhasil dan `rollback()` ketika gagal.
- Tetapkan timeout busy yang masuk akal untuk menghindari error transient `database is locked`; tetap tangani error tersebut, jangan mengulang transaksi tanpa batas.
- Hindari menahan transaksi selama user berpikir atau selama UI menampilkan dialog. Validasi dan commit dilakukan segera pada operasi submit/approval.
- Untuk operasi approval, lakukan validasi ulang menggunakan data paling mutakhir pada transaksi yang sama sebelum menyimpan perubahan.
- SQLite cocok untuk target satu komputer. Jangan mendesain aplikasi seolah SQLite pada satu file adalah solusi multi-user lintas komputer.

---

## 8. Database Schema yang Wajib Didukung

Gunakan `IMPLEMENTATION_PLAN.md` sebagai sumber utama nama kolom dan relasi. Skema di bawah merangkum kebutuhan backend. Tambahkan `CHECK`, `UNIQUE`, foreign key, dan index yang relevan.

### 8.1 `users`

Field inti:

- `id` primary key;
- `full_name` not null;
- `username` not null dan unik;
- `password_hash` not null;
- `role` (`ADMIN`/`USER`);
- `is_active` integer/boolean `0` atau `1`;
- `created_at`, `updated_at`.

Ketentuan:
- `username` tidak boleh duplikat. Tentukan apakah username case-insensitive, lalu terapkan konsisten.
- Password asli tidak pernah disimpan.
- User nonaktif tidak boleh login.

### 8.2 `academic_terms`

Field inti:

- `id`, `term_name`, `start_date`, `end_date`, `is_active`.

Ketentuan:
- `start_date <= end_date`;
- sebaiknya hanya ada satu semester aktif;
- pengubahan semester aktif harus divalidasi terhadap jadwal dan permintaan yang berjalan.

### 8.3 `rooms`

Field inti:

- `id`, `room_code` unik, `room_name`, `building`, `floor`, `capacity`, `description`, `is_active`, timestamps.

Ketentuan:
- `capacity > 0`;
- ruangan nonaktif tidak dapat dipilih untuk jadwal/peminjaman baru;
- jangan menghapus permanen ruangan yang dirujuk catatan historis.

### 8.4 `inventory_items`

Field inti:

- `id`, `item_code` unik, `item_name`, `description`, `total_quantity`, `condition_notes`, `is_active`, timestamps.

Ketentuan:
- `total_quantity >= 0`;
- item nonaktif tidak dapat menerima pengajuan baru;
- perubahan jumlah total tidak boleh membuat jumlah barang yang sedang dipinjam/direservasi melebihi jumlah total baru.

### 8.5 Data akademik

Tabel:

- `courses`: kode unik, nama mata kuliah, status aktif.
- `class_groups`: program studi, angkatan, kode kelas, jumlah mahasiswa opsional.
- `lecturers`: kode dosen unik, nama, email opsional, status aktif.
- `lecturer_availability`: dosen, semester, hari 1–7, jam mulai/selesai, catatan opsional.

Ketentuan:
- bila `student_count` tersedia, nilainya harus positif;
- `day_of_week` memakai satu konvensi konsisten. Rekomendasi: ISO `1=Monday` sampai `7=Sunday`, sesuai `DayOfWeek.getValue()`;
- `start_time < end_time`;
- rentang ketersediaan dosen yang tumpang tindih pada hari/semester sama harus dicegah atau dinormalisasi;
- nonaktifkan data yang dipakai riwayat, jangan menghapus catatan terkait.

### 8.6 `schedules`

Jadwal kuliah berulang mingguan.

Field inti:

- `id`, `term_id`, `course_id`, `class_group_id`, `lecturer_id`, `room_id`;
- `day_of_week`, `start_time`, `end_time`, `is_active`, timestamps.

Ketentuan:
- `start_time < end_time`;
- semua foreign key valid dan semua data master terkait aktif saat jadwal baru dibuat;
- jadwal harus berada pada semester yang benar;
- saat membuat/mengubah jadwal, cegah konflik room, lecturer, dan class group;
- pada pencarian kandidat, jadwal yang sedang diganti harus dikecualikan berdasarkan `schedule_id`.

### 8.7 `room_booking_requests`

Field inti:

- `id`, `requester_user_id`, `room_id`, `booking_date`, `start_time`, `end_time`;
- `expected_attendees`, `purpose`, `status`;
- `reviewed_by_user_id`, `reviewed_at`, `rejection_reason`;
- timestamps.

Ketentuan:
- jumlah peserta positif;
- rentang waktu valid dan sesuai aturan jam operasional jika jam operasional disediakan;
- alasan penolakan wajib saat status menjadi `REJECTED`;
- catatan review hanya dapat diisi oleh admin terotorisasi.

### 8.8 `inventory_loan_requests`

Field inti:

- `id`, `requester_user_id`, `item_id`, `quantity`, `loan_date`, `expected_return_date`, `purpose`, `status`;
- `reviewed_by_user_id`, `reviewed_at`, `rejection_reason`;
- `handed_over_at`, `returned_at`, `return_condition_notes`;
- timestamps.

Ketentuan:
- `quantity > 0`;
- tanggal kembali yang diharapkan tidak lebih awal dari tanggal pinjam;
- alasan penolakan wajib saat ditolak;
- pencatatan serah-terima hanya berlaku untuk pengajuan yang `APPROVED`;
- pencatatan pengembalian hanya berlaku untuk pengajuan `HANDED_OVER`;
- pengembalian pada MVP dilakukan untuk seluruh jumlah barang.

### 8.9 `schedule_change_requests`

Field inti:

- `id`, `requester_user_id`, `schedule_id`;
- `proposed_day_of_week`, `proposed_start_time`, `proposed_end_time`, `proposed_room_id`;
- `reason`, `status`;
- `reviewed_by_user_id`, `reviewed_at`, `rejection_reason`;
- timestamps.

Ketentuan:
- usulan mempertahankan durasi kelas awal;
- `schedule_id` harus masih aktif dan dapat diubah;
- jadwal asli tidak boleh berubah saat request baru dibuat;
- saat admin menyetujui, sistem harus validasi ulang seluruh konflik dan update jadwal asli + request status dalam satu transaksi;
- jika jadwal asli sudah berubah setelah request dibuat, jangan menerapkan usulan secara otomatis. Tolak atau minta admin meninjau ulang berdasarkan konflik/state stale.

### 8.10 Indeks dan constraint

Tambahkan indeks pada kolom yang sering digunakan dalam pencarian/approval, misalnya:

- `users(username)` unique;
- `rooms(room_code)` unique;
- `inventory_items(item_code)` unique;
- `schedules(term_id, day_of_week, room_id, is_active)`;
- `schedules(term_id, day_of_week, lecturer_id, is_active)`;
- `schedules(term_id, day_of_week, class_group_id, is_active)`;
- `room_booking_requests(room_id, booking_date, status)`;
- `room_booking_requests(requester_user_id, status)`;
- `inventory_loan_requests(item_id, status)`;
- `inventory_loan_requests(requester_user_id, status)`;
- `schedule_change_requests(status)`.

Jangan menambah indeks berlebihan pada setiap kolom. Evaluasi query nyata yang dipakai.

---

## 9. Validasi dan Aturan Bisnis Global

### 9.1 Validasi waktu

Dua rentang waktu saling tumpang tindih jika:

```text
candidateStart < existingEnd AND candidateEnd > existingStart
```

Contoh:

- 09.00–10.00 dan 10.00–11.00: tidak bentrok;
- 09.30–10.30 dan 10.00–11.00: bentrok;
- 09.00–11.00 dan 09.30–10.00: bentrok.

Buat utility domain teruji, misalnya `TimeRange.overlaps(...)`. Jangan menulis ulang formula berbeda di beberapa service.

### 9.2 Validasi input umum

- Field wajib tidak boleh null/kosong setelah trim.
- ID yang dirujuk harus ada.
- Numeric count/quantity harus sesuai batas; jumlah peserta dan quantity tidak boleh nol atau negatif.
- Jam selesai harus lebih besar dari jam mulai.
- Tanggal pinjam dan tanggal kembali harus valid sesuai aturan fitur.
- Ruangan/barang/dosen/data akademik yang nonaktif tidak boleh digunakan pada transaksi baru.
- Pesan validasi harus menjelaskan field atau batasan yang dilanggar tanpa membeberkan query SQL/stack trace.

### 9.3 Otorisasi

- Semua operasi yang mengubah master data, menyetujui/menolak pengajuan, mencatat serah-terima, mengembalikan barang, dan menyetujui perubahan jadwal membutuhkan `ADMIN`.
- `USER` hanya dapat melihat/mengubah/membatalkan pengajuan miliknya sendiri sesuai status yang mengizinkan.
- User tidak boleh mengubah jadwal resmi; user hanya membuat permintaan perubahan.
- Otorisasi diperiksa di service setiap kali operasi dipanggil. Penyembunyian tombol di UI bukan security boundary.
- Service harus menerima konteks user saat ini secara eksplisit, atau menggunakan `UserSession` yang terkontrol. Hindari menerima role atau user ID dari form sebagai bukti identitas.

### 9.4 Soft deactivation

- Gunakan `is_active = false` untuk menonaktifkan user, room, inventory item, course, class group, dan lecturer ketika data telah dipakai.
- Tolak nonaktifkan ruangan/dosen jika ada konsekuensi terhadap jadwal aktif yang tidak ditangani; berikan pesan konflik yang jelas.
- Histori transaksi harus tetap dapat dilihat setelah data master dinonaktifkan.

---

## 10. AuthService dan Session

### 10.1 `AuthService`

Method kontrak awal (sesuaikan gaya method repository yang dipakai):

```java
LoginResult login(String username, char[] password);
void changePassword(long currentUserId, char[] currentPassword, char[] newPassword);
```

Bila pengubahan password tidak termasuk MVP, jangan membuat UI atau method kosong hanya demi memenuhi daftar.

`login(...)` wajib:

1. validasi input;
2. mencari user berdasarkan username yang dinormalisasi;
3. menolak username yang tidak ditemukan dengan pesan generik yang sama seperti password salah;
4. menolak akun nonaktif;
5. memverifikasi password menggunakan password hashing yang dipilih;
6. menghasilkan user yang telah terautentikasi tanpa membocorkan hash;
7. tidak mencatat password ke log.

### 10.2 Akun demo

Jika seed akun demo dibuat:

- buat akun secara idempotent;
- kredensial demo hanya untuk lokal/demonstrasi dan dijelaskan di README;
- jangan memakai password pribadi atau kredensial nyata kampus;
- hash password menggunakan mekanisme yang sama dengan login;
- jangan mencatat password ke log.

### 10.3 `UserSession`

- menyimpan ID, nama tampilan, username, dan role yang sudah terautentikasi;
- dibersihkan ketika logout;
- tidak menyimpan password atau hash;
- tidak dianggap menggantikan pemeriksaan role di service;
- tidak boleh dibuat dari value role yang dikirim form user.

---

## 11. Repository Layer

### 11.1 Pedoman repository

- Query SQL berada di repository.
- Gunakan parameter binding dengan `PreparedStatement`.
- Gunakan `try-with-resources`.
- Mapping `ResultSet` menjadi model dilakukan secara konsisten, dapat memakai row mapper sederhana.
- Gunakan operasi `Optional<T>` atau pendekatan yang konsisten untuk hasil satu record yang mungkin tidak ditemukan.
- Bedakan hasil “tidak ditemukan” dari “database error”. Jangan mengubah semua exception menjadi list kosong.
- Jangan memberikan `Connection` global kepada controller.
- Hindari N+1 query untuk dashboard/daftar besar bila satu query join/aggregate lebih tepat.

### 11.2 Kontrak repository contoh

Gunakan sebagai panduan, bukan keharusan menyalin semua signature persis:

```java
Optional<User> findByUsername(String username);
Optional<User> findById(long id);

List<Room> findAllActive();
Optional<Room> findById(long id);
Room insert(Room room);
boolean update(Room room);
boolean deactivate(long roomId);

List<Schedule> findActiveByTerm(long termId);
List<Schedule> findByDayAndTerm(long termId, int dayOfWeek);

List<RoomBookingRequest> findByRequester(long userId);
List<RoomBookingRequest> findPending();
```

Repository boleh menerima `Connection` sebagai parameter untuk method yang dipakai dalam transaksi, misalnya `update(Connection connection, ...)`. Ini membantu memastikan beberapa operasi menggunakan transaksi yang sama. Tentukan konvensi ini sejak awal dan gunakan konsisten.

---

## 12. Service Layer dan Tanggung Jawab Detail

### 12.1 `RoomService`

Tanggung jawab:

- CRUD master ruangan untuk admin;
- validasi kode unik, nama, kapasitas positif, dan data wajib;
- menolak pemakaian ruangan nonaktif pada transaksi baru;
- menyediakan daftar ruangan aktif untuk user;
- mengintegrasikan `ScheduleConflictService`/availability query untuk pengecekan ruangan.

Operasi yang disarankan:

- `listActiveRooms()`;
- `getRoom(roomId)`;
- `createRoom(actor, command)`;
- `updateRoom(actor, roomId, command)`;
- `deactivateRoom(actor, roomId)`;
- `findAvailableRooms(query)`.

### 12.2 `InventoryService`

Tanggung jawab:

- CRUD katalog inventaris;
- menghitung stok tersedia berdasarkan stok total dikurangi jumlah unit yang sudah dipesan/dipinjam sesuai status;
- memvalidasi perubahan jumlah total;
- mengembalikan status item aktif/nonaktif.

Stok yang dianggap terikat bergantung pada status dan periode pinjam:

- `APPROVED`: jumlah unit dicadangkan selama rentang `loan_date` sampai `expected_return_date`;
- `HANDED_OVER`: unit secara fisik masih berada pada peminjam, sehingga tetap dianggap tidak tersedia sampai status berubah menjadi `RETURNED`, termasuk jika tanggal pengembalian perkiraan sudah lewat;
- `RETURNED`, `REJECTED`, `CANCELLED`: tidak lagi mengurangi stok untuk periode selanjutnya.

Jangan mengurangi stok saat status `PENDING`, karena pengajuan belum disetujui. Saat admin menyetujui request, hitung ulang stok pada transaksi yang sama.

Untuk request baru dengan rentang tanggal tertentu, stok tersedia harus dihitung berdasarkan jumlah unit yang sudah dicadangkan oleh request `APPROVED` dengan periode yang bertumpang tindih, serta semua request `HANDED_OVER` yang belum dikembalikan. Dua rentang tanggal bertumpang tindih bila `candidateLoanDate <= existingExpectedReturnDate AND candidateExpectedReturnDate >= existingLoanDate`.

Jika model tanggal yang dipakai inklusif, pinjaman tanggal 10–12 dianggap beririsan dengan permintaan 12–14. Dua peminjaman berurutan hanya boleh dianggap tidak beririsan jika aturan pengembalian/serah-terima secara eksplisit menetapkan batas tanggal berbeda.

Untuk MVP, satu pengajuan berisi satu jenis barang dan satu jumlah; pengembalian parsial tidak didukung. Jangan menggunakan rumus global yang mengurangi semua request `APPROVED` selamanya tanpa memperhitungkan tanggalnya. Namun request `HANDED_OVER` yang belum dikembalikan harus tetap mengurangi stok karena barang belum kembali secara fisik.

### 12.3 `AcademicDataService`

Tanggung jawab:

- CRUD semester, mata kuliah, kelompok kelas, dan dosen;
- validasi kode unik dan data wajib;
- menjaga integritas foreign key;
- mencegah lebih dari satu semester aktif;
- memvalidasi bahwa data master aktif sebelum dipakai untuk jadwal baru.

### 12.4 `LecturerAvailabilityService`

Tanggung jawab:

- CRUD rentang waktu dosen tersedia untuk semester;
- validasi hari 1–7 serta `start_time < end_time`;
- mencegah atau menangani rentang ketersediaan duplikat/tumpang tindih pada dosen, semester, hari yang sama;
- mengecek apakah keseluruhan slot kandidat berada di dalam rentang yang menyatakan dosen tersedia.

**Aturan penting:** ketersediaan berarti slot kandidat harus sepenuhnya terliput oleh rentang yang tersimpan. Bila tidak ditemukan rentang untuk hari tersebut, status harus `UNVERIFIED`/belum diketahui dan slot tidak boleh dianggap terkonfirmasi.

### 12.5 `ScheduleConflictService`

Jadikan service ini sumber kebenaran untuk konflik. Fungsi utamanya adalah mengecek kandidat jadwal terhadap jadwal aktif lain dan ketersediaan dosen.

Jenis konflik:

- `ROOM`: ruangan telah dipakai jadwal aktif lain;
- `LECTURER`: dosen mempunyai jadwal lain;
- `CLASS_GROUP`: kelompok kelas memiliki jadwal lain;
- `LECTURER_AVAILABILITY`: slot berada di luar rentang ketersediaan dosen atau data belum diatur;
- `CAPACITY`: kapasitas ruangan tidak cukup;
- `ROOM_BOOKING`: bentrok dengan peminjaman ruangan yang sudah disetujui;
- `INVALID_DATA`: data kunci/record terkait tidak lagi valid.

Input evaluasi kandidat minimal:

- semester;
- hari;
- jam mulai dan selesai;
- ID ruangan;
- ID dosen;
- ID kelompok kelas;
- `scheduleIdToExclude` yang nullable jika membuat jadwal baru;
- jumlah mahasiswa bila tersedia.

Method konseptual:

```java
List<ScheduleConflict> findScheduleConflicts(ScheduleCandidate candidate);
boolean isScheduleCandidateValid(ScheduleCandidate candidate);
```

Ketentuan:

- periksa konflik menggunakan rentang waktu overlap yang sama;
- kecualikan jadwal yang sedang dipindahkan dengan `scheduleIdToExclude`;
- jangan hanya memeriksa ruangan: dosen dan kelompok kelas juga harus bebas;
- pada rekomendasi, jangan menganggap data dosen yang belum diatur sebagai `AVAILABLE`;
- jika beberapa konflik ada, kumpulkan supaya UI dapat menjelaskan alasan tanpa menampilkan stack trace.

### 12.6 `ScheduleService`

Tanggung jawab:

- menampilkan jadwal kampus dengan filter;
- membuat, memperbarui, dan menonaktifkan jadwal oleh admin;
- menjalankan validasi semua foreign key dan konflik sebelum perubahan tersimpan;
- mencegah durasi 0/negatif;
- memakai transaksi jika perubahan melibatkan beberapa record;
- tidak mengubah jadwal resmi melalui fungsi user biasa.

Saat update jadwal, kecualikan ID jadwal yang sedang diedit saat pemeriksaan konflik.

### 12.7 `RoomBookingService`

Tanggung jawab:

- submit pengajuan user;
- mengambil pengajuan hanya milik user terkait atau seluruh antrean admin;
- membatalkan pengajuan yang masih memenuhi syarat;
- menyetujui/menolak request oleh admin;
- memvalidasi ulang ruangan, tanggal/waktu, kapasitas, jadwal kuliah pada tanggal tersebut, dan peminjaman approved lain ketika menyetujui;
- menyimpan reviewer, waktu review, dan alasan penolakan;
- mencegah perubahan status yang tidak sah.

Contoh kontrak:

```java
long submitRequest(User actor, RoomBookingCommand command);
void cancelRequest(User actor, long requestId);
void approveRequest(User admin, long requestId);
void rejectRequest(User admin, long requestId, String reason);
List<RoomBookingRequest> listMyRequests(User actor);
List<RoomBookingRequest> listPendingForAdmin(User admin);
```

#### Validasi konflik peminjaman ruangan

Saat pengajuan dikirim, lakukan validasi awal untuk memberi umpan balik. Saat admin menyetujui, ulangi pemeriksaan di transaksi:

1. ruangan masih aktif;
2. tanggal dan waktu valid;
3. kapasitas memadai;
4. tidak bentrok dengan jadwal kuliah berulang pada hari tersebut;
5. tidak bentrok dengan peminjaman lain berstatus `APPROVED` pada tanggal yang sama;
6. request masih berstatus `PENDING`.

Pengajuan `PENDING` tidak secara otomatis memblokir ketersediaan permanen, tetapi harus terlihat dalam antrean admin. Dengan demikian dua request bertumpang tindih tidak boleh keduanya disetujui: approval kedua harus mengulang validasi dan gagal jika slot telah disetujui oleh request pertama.

### 12.8 `InventoryLoanService`

Tanggung jawab:

- submit pengajuan jumlah barang;
- mengecek item masih aktif, quantity positif, dan tanggal valid;
- meninjau stok saat approval;
- approve/reject/cancel sesuai state transition;
- mencatat serah-terima;
- mencatat pengembalian beserta kondisi barang;
- mencatat actor/reviewer dan timestamp transaksi.

Contoh kontrak:

```java
long submitRequest(User actor, InventoryLoanCommand command);
void cancelRequest(User actor, long requestId);
void approveRequest(User admin, long requestId);
void rejectRequest(User admin, long requestId, String reason);
void recordHandover(User admin, long requestId);
void recordReturn(User admin, long requestId, String conditionNotes);
int getAvailableQuantity(long itemId);
```

#### State transition yang sah

```text
PENDING → APPROVED
PENDING → REJECTED
PENDING → CANCELLED
APPROVED → HANDED_OVER
HANDED_OVER → RETURNED
```

Transisi di atas adalah baseline. Jangan membuat status berubah ke sembarang nilai hanya karena field diubah. Request yang sudah `REJECTED`, `CANCELLED`, atau `RETURNED` tidak dapat diproses lagi tanpa aturan baru yang didefinisikan eksplisit.

#### Penghitungan stok dan race condition

- Hitung stok di service melalui repository pada saat submit untuk informasi awal dan **ulang saat approval**.
- Approval dan penyimpanan status harus terjadi di transaksi yang sama.
- Jika stok tidak cukup, jangan mengubah status menjadi `APPROVED`.
- Perubahan `total_quantity` tidak boleh membuat unit yang sudah dialokasikan melebihi total stok baru.
- Bila operasi mengembalikan `database is locked`, tangani secara jelas dan jangan mengulang proses tanpa batas.

### 12.9 `AlternativeScheduleService`

Tanggung jawab:

- mencari alternatif slot untuk jadwal yang dipilih;
- mempertahankan durasi asli mata kuliah;
- menghormati hard constraints;
- memberi ranking berdasarkan preferensi user setelah kandidat valid didapat;
- mengembalikan alasan bila tidak ada rekomendasi;
- tidak langsung memodifikasi jadwal resmi.

Input pencarian minimal:

- `scheduleId` yang hendak dipindahkan;
- preferred day(s) opsional;
- preferred time window opsional;
- batas rentang hari dan jam pencarian yang ditentukan aplikasi;
- kapasitas peserta jika diketahui;
- prioritas preferensi yang didukung.

Output kandidat minimal:

- ID jadwal asal;
- hari usulan;
- jam mulai dan selesai;
- ID/kode/nama ruangan;
- durasi;
- skor atau urutan preferensi;
- status validasi;
- informasi ringkas mengapa kandidat cocok.

#### Algoritma pencarian

1. Muat jadwal asal, semester, mata kuliah, kelas, dosen, ruangan, dan durasi aslinya.
2. Pastikan jadwal masih aktif dan semester masih benar.
3. Buat kandidat hari/waktu berdasarkan rentang pencarian yang ditetapkan aplikasi. Gunakan interval langkah yang konsisten dan configurable; baseline dapat menggunakan langkah 30 menit jika tidak ada konfigurasi lain.
4. Untuk setiap kandidat, jam selesai = jam mulai + durasi asli. Jangan mengubah durasi untuk membuat kandidat terlihat cocok.
5. Bentuk kandidat ruangan aktif yang kapasitasnya cukup bila jumlah mahasiswa diketahui.
6. Periksa ketersediaan dosen. Kandidat harus sepenuhnya berada di dalam rentang availability dosen. Jika data hari itu belum ada, catat bahwa kandidat belum terverifikasi dan jangan memunculkannya sebagai kandidat valid/terkonfirmasi.
7. Gunakan `ScheduleConflictService` untuk memeriksa konflik dosen, kelompok kelas, dan ruangan. Kecualikan jadwal asal dengan `scheduleIdToExclude`.
8. Periksa konflik dengan peminjaman ruangan `APPROVED` yang berlaku untuk tanggal/hari kandidat sesuai model jadwal dan aturan aplikasi.
9. Hanya kandidat yang lolos semua hard constraints yang masuk hasil `VALID`.
10. Beri skor berdasarkan soft preferences, misalnya preferensi hari, kedekatan dengan jam yang diinginkan, atau minim perubahan ruangan. Jangan pernah memberi skor tinggi kepada kandidat yang melanggar hard constraint.
11. Urutkan kandidat paling sesuai terlebih dahulu. Terapkan tie-break deterministik seperti hari, waktu, kode ruangan agar hasil konsisten saat skor sama.
12. Jika tidak ada kandidat, kembalikan hasil kosong beserta ringkasan alasan umum yang dihitung dari evaluasi, bukan sekadar pesan “error”.

#### Hard constraints vs soft preferences

**Hard constraints (wajib lulus):**

- durasi tetap sama dengan jadwal asal;
- kelompok kelas tidak bentrok;
- dosen tidak bentrok;
- dosen terbukti tersedia untuk seluruh slot;
- ruangan aktif dan bebas;
- kapasitas cukup jika jumlah mahasiswa diketahui;
- jadwal tetap di dalam semester aktif;
- kandidat tidak bertentangan dengan peminjaman ruangan yang disetujui jika berlaku.

**Soft preferences (dipakai untuk ranking):**

- hari yang disukai pengguna;
- jam yang paling mendekati waktu pilihan;
- preferensi gedung atau ruangan tertentu, bila fitur ini tersedia;
- perubahan ruangan sesedikit mungkin, bila relevan.

Tidak boleh menggunakan soft preference untuk mengesampingkan hard constraint.

### 12.10 `ScheduleChangeService`

Tanggung jawab:

- mengirim permintaan perubahan jadwal berdasarkan kandidat yang valid;
- mencatat alasan dan snapshot slot usulan yang jelas;
- menampilkan permintaan milik user dan antrean admin;
- approve/reject/cancel sesuai state transition;
- melakukan validasi ulang terhadap data terkini saat approval;
- memperbarui jadwal asli dan status request dalam satu transaksi.

Contoh kontrak:

```java
long submitChangeRequest(User actor, ScheduleChangeCommand command);
void cancelChangeRequest(User actor, long requestId);
void approveChangeRequest(User admin, long requestId);
void rejectChangeRequest(User admin, long requestId, String reason);
List<ScheduleChangeRequest> listMyChangeRequests(User actor);
List<ScheduleChangeRequest> listPendingChangeRequests(User admin);
```

#### Approval perubahan jadwal

Dalam satu transaksi database:

1. pastikan actor adalah admin;
2. muat request dan pastikan status `PENDING`;
3. muat jadwal asal terkini dan pastikan masih aktif;
4. pastikan durasi usulan sama dengan durasi jadwal saat ini;
5. pastikan slot dosen masih di dalam availability;
6. validasi ulang konflik room, lecturer, class group, dan booking dengan mengecualikan jadwal asal;
7. update jadwal asli;
8. update request menjadi `APPROVED`, simpan reviewer dan waktu review;
9. commit hanya jika seluruh langkah berhasil;
10. rollback jika satu langkah gagal.

Jika ada konflik baru atau jadwal asal telah berubah sejak request dibuat, jangan memaksakan approval. Biarkan request tetap dapat ditangani sesuai aturan state yang disepakati dan tampilkan alasan yang jelas kepada admin.

### 12.11 `DashboardService`

- Kumpulkan metrik dalam jumlah query yang wajar.
- Admin: total ruangan aktif, jumlah jadwal aktif, pengajuan peminjaman `PENDING`, perubahan jadwal `PENDING`, barang aktif, dan pengingat pengembalian jika tanggal tersedia.
- User: pengajuan miliknya yang pending/approved/aktif, permintaan perubahan jadwal, serta status terbaru.
- Jangan mengisi dashboard dengan angka hardcoded.
- Jangan membuat query ke database dari controller hanya untuk menghitung label/counter.
- Jika metrik gagal dimuat, laporkan kegagalan secara eksplisit; jangan mengubahnya diam-diam menjadi angka nol.

---

## 13. Jadwal Berulang vs Peminjaman pada Tanggal Tertentu

Aplikasi memiliki dua jenis model waktu yang perlu dibedakan:

1. **Jadwal kuliah:** berulang berdasarkan hari dalam minggu dan semester aktif.
2. **Peminjaman ruangan:** tanggal kalender tertentu (`booking_date`) dan jam tertentu.

Untuk mengecek booking room pada tanggal tertentu:

1. ubah tanggal menjadi `DayOfWeek` ISO;
2. ambil jadwal kuliah aktif pada semester yang mencakup tanggal tersebut dan hari itu;
3. evaluasi overlap waktu untuk ruangan yang sama;
4. cek peminjaman approved di ruangan dan tanggal yang sama;
5. abaikan booking request yang `PENDING` untuk ketersediaan final, tetapi tampilkan dalam antrean review.

Untuk pengecekan alternatif jadwal kuliah berulang, kandidat harus bebas pada setiap pertemuan berulang dari tanggal berlakunya perubahan sampai akhir semester. Periksa konflik jadwal mingguan di `schedules` dan booking ruangan satu kali yang berstatus `APPROVED` pada setiap tanggal kandidat. Pada MVP, jadwal dianggap pola mingguan tetap selama semester. Jangan menyatakan kandidat valid hanya karena ruangan kosong pada satu tanggal jika jadwal kuliahnya berlaku setiap minggu.

### Batasan yang perlu dijelaskan saat demo

MVP tidak memodelkan hari libur, minggu ujian, jadwal khusus satu kali, perubahan jadwal hanya untuk satu pertemuan, atau pengecualian kalender akademik. Dokumentasikan batasan ini di README.

---

## 14. State Machine untuk Pengajuan

### 14.1 Peminjaman ruangan

```text
PENDING → APPROVED
PENDING → REJECTED
PENDING → CANCELLED
```

### 14.2 Peminjaman barang

```text
PENDING → APPROVED
PENDING → REJECTED
PENDING → CANCELLED
APPROVED → HANDED_OVER → RETURNED
```

### 14.3 Perubahan jadwal

```text
PENDING → APPROVED
PENDING → REJECTED
PENDING → CANCELLED
```

### 14.4 Aturan transisi

- State transition divalidasi dalam service, bukan dengan mengubah status secara langsung dari controller.
- `APPROVED` tidak boleh diperlakukan sebagai `HANDED_OVER` untuk barang; status tersebut memiliki makna berbeda.
- Request terminal (`REJECTED`, `CANCELLED`, `RETURNED`) tidak dapat diproses ulang tanpa flow pembukaan ulang yang didefinisikan eksplisit.
- Alasan penolakan wajib dan tidak boleh hanya spasi.
- Setiap review menyimpan `reviewed_by_user_id` dan `reviewed_at`.
- Waktu gunakan format ISO atau format konsisten yang ditentukan satu utilitas.
- Jika pembatalan disediakan, batasi ke status yang diizinkan dan jangan menghapus record.

---

## 15. Error Handling dan Kontrak Hasil Service

Gunakan pola error yang konsisten. Pilih salah satu:

1. exception domain yang spesifik, seperti `ValidationException`, `AuthorizationException`, `NotFoundException`, `ConflictException`, `InvalidStateTransitionException`; atau
2. wrapper `ServiceResult<T>` dengan status dan pesan yang eksplisit.

Jangan mencampur pola tanpa alasan. Rekomendasi: gunakan exception domain untuk kegagalan yang menghentikan operasi, lalu mapping ke pesan UI di controller atau lapisan presenter.

### Jenis error

| Kondisi | Jenis hasil/error | Perilaku yang diharapkan |
|---|---|---|
| Input invalid | `ValidationException` | Jelaskan field/aturan yang salah. Tidak menulis data. |
| User tidak memiliki akses | `AuthorizationException` | Operasi dibatalkan; jangan bocorkan data terlarang. |
| Data tidak ditemukan | `NotFoundException` | Tampilkan bahwa data sudah tidak ada atau tidak dapat diakses. |
| Jadwal/ruangan/stok konflik | `ConflictException` | Tampilkan alasan konflik dan minta user/admin meninjau ulang. |
| State request tidak sesuai | `InvalidStateTransitionException` | Jangan menyimpan status baru. |
| Database gagal | exception teknis terbungkus | Log detail teknis; tampilkan pesan aman dan actionable. |

### Format pesan

- Baik: `Ruangan R-204 sudah digunakan pada rentang waktu tersebut.`
- Baik: `Stok tersedia hanya 2 unit, sedangkan pengajuan meminta 4 unit.`
- Baik: `Data ketersediaan dosen untuk hari Rabu belum diatur; rekomendasi belum dapat dikonfirmasi.`
- Buruk: `SQLITE_CONSTRAINT_FOREIGNKEY` langsung ke user.
- Buruk: stack trace penuh dalam dialog.

Jangan menelan exception dengan `catch (Exception e) { return false; }`. Jika exception ditangani, catat/log dengan konteks aman lalu lemparkan atau kembalikan hasil kegagalan yang eksplisit.

---

## 16. Keamanan Backend

- Password disimpan sebagai salted adaptive hash melalui library yang tepat; jangan menulis hashing buatan sendiri.
- Gunakan `PreparedStatement` pada semua query dengan input dinamis.
- Jangan log password, hash password, atau rahasia lain.
- Jangan mengirim model `User` yang memuat `password_hash` ke UI. Sediakan model/DTO aman untuk identitas user.
- Lakukan authorization check di setiap service method yang memerlukan role tertentu.
- User hanya dapat mengakses request miliknya melalui method `listMy...`/`getMy...`; jangan menerima owner ID seolah sudah tepercaya dari form.
- Validasi semua data di backend walaupun UI sudah memvalidasi.
- Gunakan transaksi untuk approval dan proses multi-langkah.
- Tutup semua resource JDBC dengan `try-with-resources`.
- Jangan menyimpan data mahasiswa/dosen asli pada seed atau repository.
- Gunakan soft deactivate bagi data master yang sudah direferensikan.
- Jangan memaparkan stack trace ke pengguna.
- Hindari akses database dari thread UI; controller sebaiknya menjalankan service berpotensi lama melalui `Task`/`Service` JavaFX yang sesuai. Backend tetap tidak boleh bergantung pada JavaFX.

---

## 17. Integrasi dengan Front-end

### 17.1 Aturan integrasi

Front-end memanggil service methods, bukan query repository. Bentuk method/DTO disepakati sebelum kedua orang membangun modul masing-masing.

Kontrak minimal:

- Controller memberi service actor yang telah terautentikasi atau `UserSession` yang tervalidasi.
- Service mengembalikan model/DTO atau melempar error domain yang jelas.
- UI memetakan status enum ke label Bahasa Indonesia, tanpa mengganti status domain yang disimpan.
- Tanggal/jam dipindahkan sebagai `LocalDate`/`LocalTime`, lalu diformat hanya untuk display.
- Hasil pencarian alternatif menyertakan data terstruktur, bukan satu string yang harus diparsing kembali oleh UI.

### 17.2 Jangan melakukan ini

- Jangan membuat query SQL di controller.
- Jangan melakukan perhitungan stok/jadwal sendiri di UI sebagai sumber kebenaran.
- Jangan percaya `role`, `requester_user_id`, atau `reviewed_by_user_id` yang dikirim dari input form.
- Jangan mengubah jadwal resmi dari tombol user; user hanya mengajukan.
- Jangan menandai kandidat jadwal `VALID` hanya berdasarkan warna atau keputusan UI.
- Jangan membuat tombol approval mengubah status dahulu lalu memeriksa konflik setelahnya.

---

## 18. Pengujian Backend yang Wajib

Gunakan JUnit 5. Pisahkan unit tests untuk fungsi murni, service tests untuk aturan domain, dan integration tests untuk query/transaksi SQLite. Gunakan database sementara/file terpisah untuk tests supaya data pengguna tidak terganggu.

### 18.1 Authentication dan authorization

- login berhasil dengan kredensial benar;
- password salah ditolak dengan pesan generik;
- akun nonaktif tidak dapat login;
- password tidak tersimpan sebagai plaintext;
- user tidak dapat menjalankan operasi admin;
- user tidak dapat membaca pengajuan user lain;
- logout membersihkan session.

### 18.2 Master data

- kode ruangan duplikat ditolak;
- kode barang duplikat ditolak;
- kapasitas ruangan nol/negatif ditolak;
- total stok negatif ditolak;
- foreign key invalid ditolak;
- data yang dipakai transaksi dinonaktifkan tanpa menghapus history.

### 18.3 Time overlap dan jadwal

- rentang 09:00–10:00 dan 10:00–11:00 tidak konflik;
- rentang 09:30–10:30 dan 10:00–11:00 konflik;
- room conflict terdeteksi;
- lecturer conflict terdeteksi;
- class group conflict terdeteksi;
- jadwal yang sedang diubah dikecualikan dari pencarian conflict;
- jam selesai <= jam mulai ditolak;
- data ketersediaan dosen yang tidak ada tidak dianggap bebas.

### 18.4 Peminjaman ruangan

- user membuat request yang valid;
- request user lain tidak terbaca oleh user yang sedang login;
- user dapat membatalkan request pending miliknya bila aturan memperbolehkan;
- hanya admin dapat approve/reject;
- konflik schedule dan booking ditolak saat approval;
- approval kedua terhadap request bentrok gagal jika request pertama sudah disetujui;
- alasan penolakan wajib;
- request yang bukan `PENDING` tidak dapat disetujui lagi.

### 18.5 Peminjaman barang

- quantity nol/negatif ditolak;
- item nonaktif tidak dapat dipinjam;
- request quantity lebih besar dari stok tidak dapat disetujui;
- stok berkurang secara logis untuk request `APPROVED` dengan rentang pinjam yang bertumpang tindih dan semua request `HANDED_OVER` yang belum dikembalikan;
- stok kembali tersedia untuk periode berikutnya setelah `RETURNED` atau request dibatalkan/ditolak;
- serah-terima hanya berlaku setelah approval;
- pengembalian hanya berlaku setelah handover;
- approval untuk beberapa request yang secara total melampaui stok pada rentang tanggal yang beririsan tidak boleh menghasilkan stok negatif.

### 18.6 Alternatif dan perubahan jadwal

- durasi kandidat selalu sama dengan jadwal asal;
- slot dengan room conflict tidak muncul sebagai valid;
- slot dengan lecturer conflict tidak muncul sebagai valid;
- slot dengan class group conflict tidak muncul sebagai valid;
- slot di luar availability dosen tidak muncul sebagai valid;
- jadwal asal tidak konflik dengan kandidatnya sendiri karena tidak dikecualikan;
- soft preference memengaruhi ranking, bukan kelolosan hard constraints;
- tidak ada kandidat menghasilkan empty result yang benar, bukan kandidat palsu;
- request perubahan tidak mengubah jadwal resmi sebelum approval;
- approval mengulang validasi dan update jadwal + request dilakukan atomik;
- jika konflik terjadi pada saat approval, tidak ada perubahan parsial pada jadwal maupun request.

### 18.7 Database dan transaksi

- foreign keys aktif pada koneksi baru;
- seed bisa dijalankan berulang tanpa duplikasi;
- rollback bekerja saat operasi multi-langkah gagal;
- query menggunakan parameter binding;
- repository menutup connection/statement/result set;
- aplikasi memunculkan pesan error aman pada database failure.

---

## 19. Definition of Done (DoD)

Sebuah modul backend hanya dianggap selesai jika:

- [ ] Model/DTO/enum yang dibutuhkan tersedia dan konsisten.
- [ ] Repository menggunakan JDBC dan parameter binding.
- [ ] Service menerapkan validasi dan otorisasi.
- [ ] State transition dibatasi sesuai aturan.
- [ ] Operasi multi-langkah memakai transaksi yang sesuai.
- [ ] Tidak ada query SQL di controller.
- [ ] Error ditangani eksplisit dan aman.
- [ ] Unit test untuk aturan utama tersedia dan lulus.
- [ ] Integration test untuk operasi database kritis tersedia dan lulus.
- [ ] `mvn test` berhasil dijalankan dan hasil aktual dilaporkan.
- [ ] Dokumentasi method/kontrak diperbarui bila ada perubahan.

Jangan menandai checklist sebagai selesai hanya karena source code telah ditulis.

---

## 20. Urutan Implementasi Backend

Antigravity harus mengerjakan tahapan berikut secara berurutan. Jika repository sudah menyediakan sebagian fitur, audit dan tes fitur itu terlebih dahulu; jangan membuat duplikasi.

### BE-0 — Audit repository dan environment

**Pekerjaan**

- Baca `IMPLEMENTATION_PLAN.md` dan `FRONTEND_ENGINEERING.md`.
- Periksa file/folder proyek, `pom.xml`, Java/Maven version, package naming, dan pola kode yang telah ada.
- Identifikasi fitur yang sudah dibuat dan test yang sudah tersedia.
- Catat perbedaan antara implementasi yang ada dan dua dokumen spesifikasi.

**Kriteria selesai**

- Ada ringkasan temuan sebelum kode diubah.
- Tidak ada file existing yang dihapus/ditimpa tanpa alasan.
- Perintah build/test baseline sudah dicoba dan hasil aktual dicatat.

### BE-1 — Database foundation

**Pekerjaan**

- Implementasikan database path config, `DatabaseManager`, inisialisasi schema, foreign keys, dan seed demo idempotent.
- Tambahkan constraint/index dasar dan schema versioning bila diperlukan.
- Implementasikan model/enum inti.
- Buat unit/integration test koneksi dan schema.

**Kriteria selesai**

- Database dibuat otomatis saat first run.
- Foreign keys aktif pada setiap koneksi baru.
- Seed yang dijalankan berulang tidak membuat duplikasi.
- `mvn test` lulus untuk test awal.

### BE-2 — Auth dan authorization

**Pekerjaan**

- Implementasikan user repository, password hashing/verifikasi, `AuthService`, dan session contract.
- Implementasikan pemeriksaan role di service yang memerlukan akses admin.
- Pastikan user hanya dapat membaca data/requests miliknya.

**Kriteria selesai**

- Skenario login valid/invalid/nonaktif teruji.
- Operasi admin ditolak untuk user.
- Password plaintext tidak disimpan atau dicatat.

### BE-3 — Master data CRUD

**Pekerjaan**

- Implementasikan repository dan service untuk semester, ruangan, inventaris, mata kuliah, kelompok kelas, dosen, dan ketersediaan dosen.
- Implementasikan validasi unique code, foreign key, nilai positif, status aktif, dan semester aktif.
- Pastikan deactivation mempertahankan history.

**Kriteria selesai**

- Data dapat dibuat, dicari, diperbarui, dan dinonaktifkan sesuai role.
- Invalid input diuji.
- Tidak ada operasi langsung dari controller ke database.

### BE-4 — Schedule and conflict validation

**Pekerjaan**

- Implementasikan `ScheduleRepository`, `ScheduleService`, `ScheduleConflictService`, `TimeRange`, dan validasi availability dosen.
- Cegah konflik room/lecturer/class group.
- Implementasikan filter jadwal untuk UI.

**Kriteria selesai**

- Semua jenis konflik teruji.
- Jadwal asal dapat dikecualikan pada update/pencarian alternatif.
- Data availability yang belum diatur tidak dianggap tersedia.

### BE-5 — Room booking workflow

**Pekerjaan**

- Implementasikan submit, list-my-requests, list-pending, cancel, approve, reject.
- Implementasikan cek room capacity, recurring lecture schedule pada tanggal yang dipilih, dan booking approved lain.
- Lakukan validasi ulang dalam transaksi ketika approval.

**Kriteria selesai**

- User dapat membuat dan memantau request miliknya.
- Admin dapat approve/reject dengan alasan yang tepat.
- Dua request bentrok tidak bisa keduanya disetujui.
- Ownership dan permission diuji.

### BE-6 — Inventory loan workflow

**Pekerjaan**

- Implementasikan stok tersedia, request, approval/rejection, cancellation, handover, return.
- Implementasikan state machine dan transaksi approval.
- Validasi perubahan total quantity.

**Kriteria selesai**

- Stok tidak pernah negatif berdasarkan skenario test yang didukung.
- Transisi status yang tidak sah ditolak.
- History serah-terima/pengembalian dapat dibaca.

### BE-7 — Alternative schedule search

**Pekerjaan**

- Implementasikan DTO input/output, candidate generator, evaluasi hard constraints, scoring soft preferences, dan sorting deterministik.
- Kecualikan jadwal asal saat cek bentrok.
- Berikan empty result dengan alasan yang berguna jika tidak ada kandidat valid.

**Kriteria selesai**

- Semua kandidat keluaran telah lolos hard constraints.
- Durasi tetap sama.
- Ranking dapat diuji dan konsisten.
- Tidak ada rekomendasi yang menganggap data availability dosen yang kosong sebagai bebas.

### BE-8 — Schedule change workflow

**Pekerjaan**

- Implementasikan request, list, cancel, approve, reject.
- Saat approval, validasi ulang slot dan update jadwal asli + request status dalam satu transaksi.
- Tangani stale request atau konflik baru tanpa perubahan parsial.

**Kriteria selesai**

- Jadwal resmi tidak berubah saat request baru dibuat.
- Hanya admin yang bisa memutuskan.
- Konflik pada saat approval menyebabkan rollback.

### BE-9 — Dashboard, history, integration hardening

**Pekerjaan**

- Implementasikan query agregasi `DashboardService`.
- Pastikan controller FE dapat memanggil kontrak service yang sudah disepakati.
- Periksa error messages, logging, state transition, dan query performance wajar.
- Lengkapi tests dan dokumentasi.

**Kriteria selesai**

- Angka dashboard bersumber dari database.
- Seluruh test kritis lulus.
- Kontrak service/DTO tertulis dan dipakai konsisten oleh dua anggota.

### BE-10 — Release readiness

**Pekerjaan**

- Jalankan seluruh test dan build.
- Periksa data seed, schema, backup/reset database untuk demo, dan README.
- Dokumentasikan batasan MVP dan troubleshooting.

**Kriteria selesai**

- Build/test benar-benar dijalankan dan hasilnya tercatat.
- Tidak ada tombol penting yang tergantung pada method backend kosong.
- README menjelaskan cara run, demo data, lokasi database, dan batasan.

---

## 21. Pembagian Backend untuk Dua Anggota

Pembagian proyek tetap **25% FE + 25% BE per anggota**. Dokumen ini hanya menjelaskan sisi backend secara rinci; pembagian front-end mengikuti `FRONTEND_ENGINEERING.md`.

### Orang 1 — Fondasi, autentikasi, dan master data

**Tanggung jawab backend**

- `DatabaseManager`, schema initialization/versioning, seed demo.
- Model, enum, dan repository untuk user, semester, ruangan, inventaris, data akademik, dosen, availability, dan jadwal.
- `AuthService`, `UserSession` contract, serta authorization baseline.
- `RoomService`, `InventoryService`, `AcademicDataService`, `LecturerAvailabilityService`.
- `ScheduleConflictService` dan `ScheduleService` untuk CRUD/validasi jadwal awal.
- `DashboardService` untuk metrik master data dasar jika diperlukan.
- Integration notes untuk kontrak yang dipakai Orang 2.

**Deliverables**

- Schema/database dapat diinisialisasi otomatis.
- Master data dapat dioperasikan melalui service/repository.
- Login dan role bekerja.
- Jadwal kuliah divalidasi dari sisi ruangan, dosen, dan kelompok kelas.
- Method contract didokumentasikan.

### Orang 2 — Workflow transaksi dan alternatif jadwal

**Tanggung jawab backend**

- `RoomBookingService` dan repository workflow peminjaman ruangan.
- `InventoryLoanService` dan repository workflow peminjaman barang.
- `AlternativeScheduleService` untuk generate, validasi, dan ranking kandidat.
- `ScheduleChangeService` untuk request dan approval update jadwal.
- Unit/integration tests pada approval, konflik, state transition, dan race condition yang mungkin terjadi pada target aplikasi lokal.

**Dependensi dari Orang 1**

Orang 2 membutuhkan kontrak repository/service berikut sebelum membangun workflow penuh:

- koneksi database/transaksi;
- model `Room`, `InventoryItem`, `Schedule`, `LecturerAvailability`, `User`;
- query ketersediaan ruangan/stok;
- `ScheduleConflictService`;
- format status dan date/time yang disepakati.

**Deliverables**

- Peminjaman ruangan berfungsi sampai approval.
- Peminjaman barang berfungsi sampai handover dan return.
- Rekomendasi alternatif lolos semua hard constraints.
- Jadwal baru berlaku hanya setelah admin approve.
- Test kritis dan kontrak integrasi tersedia.

### Tanggung jawab bersama

- Menyetujui kontrak entity, enum, DTO, nama tabel/kolom, dan signature method sebelum integrasi.
- Tidak membuat model atau service duplikat.
- Review perubahan schema karena schema dipakai kedua modul.
- Menjalankan regression tests saat merge/integrasi.
- Menulis laporan milestone berisi hasil aktual build/test, bukan asumsi.

---

## 22. Kontrak Integrasi Antaranggota

Sebelum implementasi yang berjalan paralel, sepakati hal berikut dalam repository atau catatan singkat:

1. package root dan strategi penamaan;
2. class/field model inti;
3. enum status persis dan transisi yang sah;
4. tipe ID yang dipakai;
5. representasi waktu (`LocalDate`, `LocalTime`, hari ISO 1–7);
6. lokasi database dan cara mendapatkan connection;
7. apakah repository memakai interface dan bagaimana transaksi dipass ke method;
8. signature service yang dibutuhkan controller;
9. bentuk hasil error/exception;
10. cara menjalankan test dan seed demo.

Jika Person 2 memerlukan method dari Person 1 yang belum tersedia, definisikan kontrak method terlebih dahulu. Jangan membuat SQL paralel di service baru hanya karena repository yang dibutuhkan belum selesai. Jika rancangan berubah, update dokumen lalu beri tahu anggota lain.

---

## 23. README Backend yang Wajib Ada

README harus mencantumkan:

- stack dan versi aktual;
- cara build/test/run;
- cara inisialisasi/reset database demo;
- lokasi database runtime;
- akun demo untuk admin/user bila dibuat, dengan peringatan bahwa itu hanya untuk demo lokal;
- struktur package dan tanggung jawab service;
- skema ringkas relasi data;
- alur status peminjaman ruangan, peminjaman barang, dan perubahan jadwal;
- cara menambah/migrasi schema;
- batasan MVP, khususnya SQLite satu komputer, jadwal kuliah mingguan, dan data availability dosen;
- troubleshooting untuk kegagalan koneksi, schema, atau locked database.

Jangan menulis bahwa fitur telah tersedia jika kode atau pengujian belum membuktikannya.

---

## 24. Instruksi Operasional Khusus untuk Antigravity

### Instruksi umum

1. **Baca sebelum edit.** Baca penuh `IMPLEMENTATION_PLAN.md`, `FRONTEND_ENGINEERING.md`, dan dokumen ini.
2. **Audit repository.** Periksa kode, dependency, tests, schema, package, dan status build sebelum membuat perubahan.
3. **Implementasikan bertahap.** Ikuti `BE-0` sampai `BE-10`. Jangan membangun seluruh backend dalam satu perubahan besar.
4. **Jaga aplikasi tetap runnable.** Jalankan build dan test pada setiap milestone serta perbaiki regression sebelum lanjut.
5. **Sumber kebenaran aturan bisnis adalah service/domain layer.** Jangan menduplikasi aturan konflik di controller atau repository.
6. **Jangan membuat fitur pura-pura.** Jangan mengembalikan data hardcoded untuk menggantikan query yang belum dibuat. Bila belum ada, laporkan sebagai belum selesai.
7. **Jangan melemahkan validasi.** Jika tidak ada slot jadwal valid, periksa data/aturan dan laporkan; jangan menghapus hard constraint agar hasil muncul.
8. **Jangan menyembunyikan error.** Tangani dan log error teknis dengan aman; berikan pesan domain yang jelas.
9. **Tidak mengklaim test berhasil tanpa menjalankannya.** Sertakan perintah dan hasil aktual.
10. **Jaga kontrak FE/BE.** Sesuaikan method dan DTO dengan dokumen front-end; bila ada perubahan signature, perbarui semua pemanggil dan dokumentasi.
11. **Hindari overengineering.** Jangan menambah server, ORM besar, framework DI, atau abstraksi yang tidak diperlukan untuk proyek desktop MVP.
12. **Jangan menimpa data user.** Jangan menghapus database/file yang telah ada tanpa memeriksa isi dan memberi alasan teknis yang jelas.

### Prompt awal yang bisa langsung diberikan ke Antigravity

```text
Baca secara menyeluruh:
- IMPLEMENTATION_PLAN.md
- FRONTEND_ENGINEERING.md
- BACKEND_ENGINEERING.md

Jadikan ketiganya sebagai acuan yang saling melengkapi. IMPLEMENTATION_PLAN.md mengatur kebutuhan produk dan aturan bisnis; FRONTEND_ENGINEERING.md mengatur presentasi/UI; BACKEND_ENGINEERING.md mengatur arsitektur backend dan persistensi.

Mulai dari BE-0: audit repository, Java/Maven, struktur package, pom.xml, database/schema yang sudah ada, dan test. Jangan langsung menulis ulang project. Laporkan temuan, status build/test baseline, dan rencana perubahan minimal.

Setelah audit, implementasikan BE-1 (database foundation) saja. Jangan lanjut ke modul berikutnya sebelum build serta test tahap tersebut berhasil. Ikuti BE-0 sampai BE-10 sesuai urutan dan kriteria selesai.

Aturan yang tidak boleh dilanggar:
- controller tidak menjalankan SQL;
- aturan bisnis berada di service/domain layer;
- query memakai PreparedStatement;
- password disimpan sebagai hash, bukan plaintext;
- role/ownership diperiksa di service;
- approval peminjaman dan perubahan jadwal melakukan validasi ulang secara transaksional;
- pencarian alternatif mempertahankan durasi dan hanya mengembalikan kandidat yang memenuhi semua hard constraints;
- data ketersediaan dosen yang belum ada bukan berarti dosen tersedia;
- jadwal resmi tidak berubah sebelum perubahan disetujui admin;
- stok tidak boleh menjadi negatif;
- jangan membuat data dashboard atau hasil pencarian hardcoded sebagai pengganti backend nyata.

Pada akhir setiap milestone, laporkan:
1. file/module yang dibuat atau diubah;
2. aturan/fitur yang benar-benar diimplementasikan;
3. command build dan test yang dijalankan beserta hasil aktual;
4. masalah atau acceptance criteria yang belum terpenuhi;
5. langkah milestone berikutnya.
```

### Format laporan setiap milestone

Gunakan format:

```text
Milestone: BE-X — [nama tahap]
Perubahan:
- [file/modul dan tujuannya]

Implementasi:
- [fitur/aturan yang sudah bekerja]

Verifikasi:
- Command: [perintah aktual]
- Hasil: [PASS/FAIL + ringkasan]

Belum selesai / risiko:
- [daftar aktual; tulis "tidak ada" hanya jika benar]

Langkah berikutnya:
- [tahap selanjutnya]
```

---

## 25. Final Backend Checklist

### Foundation
- [ ] Struktur repository dan build sudah diaudit.
- [ ] Database dibuat otomatis dan path-nya benar.
- [ ] Foreign keys aktif pada setiap connection.
- [ ] Schema/versioning dan seed tidak merusak data.
- [ ] Entity, DTO, enum, dan date/time convention konsisten.

### Authentication and access
- [ ] Password di-hash.
- [ ] Akun nonaktif tidak dapat login.
- [ ] Role diperiksa pada service.
- [ ] User hanya dapat mengakses pengajuannya sendiri.

### Master data and schedules
- [ ] CRUD divalidasi.
- [ ] Data master historis dinonaktifkan, tidak dihapus secara ceroboh.
- [ ] Time overlap memakai satu implementasi.
- [ ] Room, lecturer, dan class group conflicts diuji.
- [ ] Availability dosen yang belum diatur tidak dianggap valid.

### Workflows
- [ ] Room booking submit/review/cancel berfungsi.
- [ ] Inventory loan approval/handover/return berfungsi.
- [ ] State transitions ilegal ditolak.
- [ ] Approval memvalidasi ulang sebelum commit.
- [ ] Alternative schedule hanya mengembalikan kandidat valid.
- [ ] Schedule-change approval mengupdate jadwal + request secara atomik.

### Quality
- [ ] Tidak ada query SQL di controller.
- [ ] Semua query memakai binding.
- [ ] Resource JDBC ditutup.
- [ ] Error ditangani dengan pesan aman.
- [ ] Unit dan integration tests lulus.
- [ ] Build/test result aktual tercatat di laporan.
- [ ] README dan batasan MVP diperbarui.

---

## 26. Sumber Kebenaran dan Penanganan Konflik Dokumen

- `IMPLEMENTATION_PLAN.md` adalah acuan utama untuk ruang lingkup fitur, asumsi MVP, relasi data, aturan bisnis, dan kriteria keseluruhan proyek.
- `FRONTEND_ENGINEERING.md` adalah acuan utama untuk halaman, interaksi, state UI, dan kontrak konsumsi service oleh FE.
- `BACKEND_ENGINEERING.md` adalah acuan utama untuk lapisan backend, repository, transaksi, validasi backend, testing, dan urutan implementasi backend.
- Jika ada perbedaan, jangan diam-diam memilih interpretasi baru. Identifikasi konflik, prioritaskan batasan produk di `IMPLEMENTATION_PLAN.md`, lalu sesuaikan dua dokumen teknis dan dokumentasikan keputusan.
- Bila repository yang sudah ada berbeda, audit dahulu dan pertahankan solusi yang benar-benar berfungsi kecuali ada alasan teknis atau aturan produk yang mengharuskan perubahan.

**Hasil akhir yang diharapkan:** backend Java yang terstruktur, teruji, aman untuk kebutuhan MVP, tidak membiarkan konflik jadwal/stok lolos, serta dapat diintegrasikan dengan JavaFX front-end tanpa SQL atau aturan bisnis berada di controller.
