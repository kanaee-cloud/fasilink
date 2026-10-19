# IMPLEMENTATION PLAN — Campus Facility & Schedule Manager

> **Tujuan dokumen:** menjadi spesifikasi kerja untuk Antigravity saat membangun aplikasi desktop Java pengelolaan fasilitas kampus.
>
> **Aturan utama untuk agent:** baca seluruh dokumen ini sebelum mengubah kode. Periksa struktur repository yang sudah ada terlebih dahulu. Implementasikan secara bertahap, jaga agar aplikasi tetap dapat dijalankan, dan jangan menyatakan suatu fitur selesai sebelum kriteria penerimaannya diuji.

---

## 1. Ringkasan Proyek

Bangun aplikasi desktop untuk membantu kampus mengelola jadwal perkuliahan, peminjaman ruangan, dan peminjaman inventaris. Aplikasi memiliki dua role: **Admin** dan **User**.

Fitur pembeda utama adalah **pencarian alternatif jadwal kuliah**. Saat sebuah pertemuan kuliah perlu dipindahkan, sistem mencari slot alternatif dengan durasi yang sama, lalu memeriksa bentrok jadwal kelas, dosen, ruangan, kapasitas ruangan, ketersediaan dosen, serta peminjaman ruangan yang sudah disetujui.

### 1.1 Masalah yang ingin diselesaikan

1. Pengguna kesulitan mengetahui ruangan yang kosong pada hari dan jam tertentu.
2. Peminjaman ruangan dan barang belum tercatat dalam satu sistem.
3. Admin membutuhkan cara untuk memeriksa dan menyetujui pengajuan secara terstruktur.
4. Saat jadwal kuliah harus dipindahkan, pencarian jadwal pengganti perlu mempertimbangkan beberapa batasan sekaligus, bukan hanya ruangan kosong.

### 1.2 Target hasil

- Pengguna dapat melihat jadwal dan ketersediaan fasilitas.
- Pengguna dapat mengajukan peminjaman ruangan atau barang dan melacak statusnya.
- Admin dapat mengelola data akademik dan fasilitas, meninjau pengajuan, serta mencatat serah-terima dan pengembalian barang.
- Sistem dapat memberikan rekomendasi slot jadwal yang lolos validasi aturan yang ditentukan.
- Perubahan jadwal tidak langsung diterapkan sebelum disetujui pihak berwenang.

---

## 2. Asumsi dan Batasan MVP

Gunakan keputusan berikut untuk mencegah implementasi melebar. Jangan mengganti keputusan ini tanpa alasan teknis yang jelas.

1. **Jenis aplikasi:** aplikasi desktop, bukan web app atau APK.
2. **Bahasa dan UI:** Java dengan JavaFX; gunakan FXML untuk struktur halaman jika sesuai dengan repository.
3. **Build tool:** Maven.
4. **Database awal:** SQLite menggunakan JDBC. Database disimpan sebagai file lokal di folder `data/`, bukan di dalam `src/main/resources`.
5. **Target deployment awal:** satu komputer / satu instalasi dengan database lokal. Jangan menjanjikan sinkronisasi beberapa komputer. Apabila aplikasi kelak perlu dipakai beberapa komputer bersama-sama, database perlu dipindahkan ke server seperti PostgreSQL dan arsitektur perlu ditinjau kembali.
6. **Role:** hanya `ADMIN` dan `USER`.
7. **Jadwal akademik:** jadwal kelas merupakan jadwal berulang mingguan pada semester aktif. Pengajuan perubahan jadwal untuk MVP mengubah slot jadwal tersebut untuk sisa semester aktif, setelah disetujui admin.
8. **Peminjaman ruangan:** peminjaman dilakukan untuk satu tanggal dan satu rentang waktu pada setiap pengajuan.
9. **Peminjaman barang:** satu pengajuan berisi satu jenis barang dan jumlah unit yang diminta. Pengembalian sebagian tidak masuk MVP; pengembalian dilakukan untuk seluruh jumlah yang tercatat dalam pengajuan.
10. **Pencarian alternatif jadwal:** durasi kelas tidak boleh berubah. Pengguna memilih jadwal kuliah yang akan diubah, lalu sistem menyarankan hari, waktu, dan ruangan lain untuk sisa semester aktif.
11. **Tidak ada integrasi eksternal** dengan SIAKAD, email, WhatsApp, cloud, atau layanan kampus lain dalam MVP.
12. **Data ketersediaan dosen harus dikelola admin.** Sistem tidak boleh menyatakan slot sebagai terkonfirmasi apabila data ketersediaan dosen yang diperlukan belum tersedia.
13. Gunakan bahasa antarmuka Indonesia yang sederhana dan konsisten.

### Di luar cakupan MVP

- Login melalui akun kampus/SSO.
- Sinkronisasi otomatis dengan sistem akademik.
- Notifikasi email atau pesan instan.
- Akses multi-komputer melalui database bersama.
- Pengajuan peminjaman berulang, pengembalian parsial, tanda tangan digital, serta ekspor laporan lanjutan.

---

## 3. Stack Teknologi

| Bagian | Pilihan awal | Catatan |
|---|---|---|
| Bahasa | Java 21 LTS | Jika komputer/laboratorium mewajibkan versi lain, sesuaikan `pom.xml` secara konsisten. |
| UI desktop | JavaFX + FXML + CSS | Pisahkan tampilan dari logika bisnis. |
| Build | Maven | Semua dependensi dan perintah build harus tercatat di `pom.xml`/README. |
| Database | SQLite | Cocok untuk prototipe lokal satu komputer. |
| Akses database | JDBC | Semua query menggunakan parameter binding / `PreparedStatement`. |
| Password hashing | BCrypt atau algoritma password hashing yang terpelihara | Jangan menyimpan password dalam bentuk teks biasa. |
| Pengujian | JUnit 5 | Utamakan pengujian aturan bisnis dan konflik jadwal. |
| Kontrol versi | Git jika tersedia | Buat commit per modul/tahap jika workflow tim menggunakannya. |

### Pedoman implementasi

- Pilih versi library yang stabil dan saling kompatibel; jangan mencampur versi dependency tanpa alasan.
- Jangan menambahkan library UI besar atau framework lain kecuali benar-benar diperlukan.
- Gunakan `LocalDate`, `LocalTime`, `DayOfWeek`, dan `Duration` untuk pemrosesan tanggal/waktu.
- Simpan tanggal dan waktu dalam format ISO yang konsisten pada SQLite, misalnya `YYYY-MM-DD` dan `HH:mm`.
- Aktifkan SQLite foreign keys pada setiap koneksi (`PRAGMA foreign_keys = ON`).
- Simpan path database dalam konfigurasi dan pastikan folder `data/` dibuat jika belum ada.

---

## 4. Role dan Hak Akses

### 4.1 Admin

Admin dapat:

- Melihat dashboard admin dan daftar pengajuan yang membutuhkan tindakan.
- Mengelola data ruangan.
- Mengelola data barang/inventaris.
- Mengelola data mata kuliah, kelompok kelas, dosen, dan semester aktif.
- Mengelola jadwal kuliah dan rentang ketersediaan dosen.
- Melihat, menyetujui, atau menolak pengajuan peminjaman ruangan.
- Melihat, menyetujui, atau menolak pengajuan peminjaman barang.
- Mencatat barang yang diserahkan dan dikembalikan.
- Meninjau serta menyetujui/menolak permintaan perubahan jadwal.
- Melihat riwayat transaksi dan ringkasan sederhana.
- Mengaktifkan atau menonaktifkan akun user jika modul manajemen user disertakan.

### 4.2 User

User dapat:

- Melihat dashboard user.
- Melihat jadwal perkuliahan berdasarkan hari/minggu, ruangan, atau mata kuliah.
- Melihat informasi ketersediaan ruangan.
- Mengajukan peminjaman ruangan.
- Melihat katalog dan ketersediaan inventaris.
- Mengajukan peminjaman barang.
- Melihat status dan riwayat pengajuan miliknya sendiri.
- Mencari alternatif jadwal untuk jadwal kuliah yang dapat dia akses.
- Mengirim permintaan perubahan jadwal berdasarkan salah satu rekomendasi.
- Melihat keputusan permintaan perubahan jadwal.

### 4.3 Ketentuan hak akses

- Hak akses wajib diperiksa pada lapisan service/controller, bukan hanya dengan menyembunyikan tombol di UI.
- User hanya boleh melihat/mengubah pengajuan miliknya sendiri, kecuali endpoint/service yang memang khusus untuk admin.
- User tidak boleh langsung mengubah jadwal resmi. User hanya membuat permintaan; perubahan resmi dilakukan melalui proses persetujuan admin.
- Akun nonaktif tidak boleh login.
- Jangan mengandalkan nama tampilan atau nilai dari form untuk menentukan role.

---

## 5. Rancangan Modul dan Halaman

### 5.1 Halaman umum

1. **Login** — username/email kampus dan password.
2. **Shell aplikasi** — sidebar navigasi, area konten utama, identitas user, tombol logout.
3. **Dialog konfirmasi** — untuk tindakan penting seperti menolak pengajuan, menyetujui perubahan jadwal, atau mengubah status peminjaman.
4. **Pesan validasi** — tampilkan kesalahan dekat field terkait, bukan hanya stack trace atau popup teknis.

### 5.2 Halaman User

| Halaman | Isi dan aksi utama |
|---|---|
| Dashboard User | Ringkasan pengajuan aktif, status terbaru, shortcut ke peminjaman ruangan/barang dan pencarian jadwal. |
| Jadwal Kampus | Tabel/kalender jadwal; filter tanggal/hari, ruangan, mata kuliah, kelompok kelas, dan dosen. |
| Peminjaman Ruangan | Pilih tanggal, jam mulai/selesai, jumlah peserta, ruangan, dan tujuan; tampilkan ketersediaan serta peringatan konflik. |
| Katalog Inventaris | Daftar barang, deskripsi, total unit, estimasi unit yang tersedia, dan status aktif. |
| Peminjaman Barang | Pilih satu barang, jumlah, tanggal pinjam, perkiraan kembali, dan tujuan. |
| Pengajuan Saya | Daftar pengajuan ruangan/barang beserta status, tanggal, alasan penolakan jika ada, dan riwayat. |
| Cari Alternatif Jadwal | Pilih jadwal kelas yang akan diubah, preferensi hari/waktu, lihat rekomendasi valid, lalu ajukan perubahan. |
| Permintaan Perubahan Jadwal Saya | Daftar permintaan perubahan jadwal dan status persetujuan. |

### 5.3 Halaman Admin

| Halaman | Isi dan aksi utama |
|---|---|
| Dashboard Admin | Jumlah ruangan aktif, pengajuan menunggu, jadwal kuliah, barang, dan pengingat pengembalian barang. |
| Manajemen Ruangan | Tambah, lihat, ubah, aktifkan/nonaktifkan ruangan; kode ruangan harus unik. |
| Manajemen Inventaris | Tambah, lihat, ubah jumlah total, kondisi, dan status barang. |
| Data Akademik | Kelola semester, mata kuliah, kelompok kelas, dan dosen. |
| Ketersediaan Dosen | Atur jendela waktu dosen tersedia per hari pada semester aktif. |
| Manajemen Jadwal | Tambah, lihat, ubah, atau nonaktifkan jadwal berulang; cegah konflik sebelum menyimpan. |
| Persetujuan Peminjaman Ruangan | Tinjau detail permintaan, konflik, kapasitas, setujui atau tolak dengan alasan. |
| Persetujuan Peminjaman Barang | Tinjau jumlah yang diminta, stok yang tersedia, setujui atau tolak. |
| Serah-Terima/Pengembalian | Catat tanggal serah-terima, tanggal kembali, dan catatan kondisi barang. |
| Persetujuan Perubahan Jadwal | Lihat jadwal awal dan usulan, jalankan ulang validasi, setujui/tolak dengan alasan. |
| Riwayat & Laporan | Filter transaksi/peminjaman berdasarkan tanggal, status, ruangan, barang, dan pemohon. |
| Manajemen User | Opsional untuk MVP; membuat, mengaktifkan, menonaktifkan, atau mengatur role user. |

### 5.4 Pedoman UI

- Buat UI desktop yang rapi, konsisten, dan mudah digunakan; hindari dekorasi yang mengganggu keterbacaan.
- Semua tabel memiliki header yang jelas, empty state, dan pesan saat data gagal dimuat.
- Form harus melakukan validasi field wajib dan format sebelum memanggil service.
- Status tidak hanya dibedakan dengan warna; tuliskan label seperti `Menunggu`, `Disetujui`, `Ditolak`, dan `Dikembalikan`.
- Setelah aksi berhasil, tampilkan konfirmasi ringkas dan perbarui data yang terlihat.
- Saat loading atau menjalankan query, jangan membekukan UI JavaFX. Untuk operasi yang berpotensi lama, gunakan `Task`/`Service` JavaFX atau pendekatan background yang sesuai, lalu perbarui UI pada JavaFX Application Thread.
- Jangan menampilkan password, password hash, query SQL mentah, atau stack trace kepada user.

---

## 6. Struktur Arsitektur Kode

Gunakan pemisahan **View → Controller → Service → Repository → Database**. Controller menangani interaksi UI; service memuat aturan bisnis; repository memuat query SQL.

Contoh struktur folder (sesuaikan jika repository yang sudah ada memiliki konvensi lain):

```text
campus-facility-manager/
├── pom.xml
├── README.md
├── IMPLEMENTATION_PLAN.md
├── data/
│   └── .gitkeep
├── src/
│   ├── main/
│   │   ├── java/id/ac/campusfacility/
│   │   │   ├── MainApp.java
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java
│   │   │   │   └── DatabaseManager.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── InventoryItem.java
│   │   │   │   ├── Course.java
│   │   │   │   ├── ClassGroup.java
│   │   │   │   ├── Lecturer.java
│   │   │   │   ├── AcademicTerm.java
│   │   │   │   ├── Schedule.java
│   │   │   │   └── ...
│   │   │   ├── model/enums/
│   │   │   ├── dto/
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoomRepository.java
│   │   │   │   └── ...
│   │   │   ├── repository/sqlite/
│   │   │   ├── service/
│   │   │   ├── controller/
│   │   │   ├── session/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── fxml/
│   │       ├── css/app.css
│   │       └── images/
│   └── test/
│       └── java/id/ac/campusfacility/
│           ├── service/
│           └── util/
└── .gitignore
```

### 6.1 Aturan batas antarlapisan

- FXML tidak berisi aturan bisnis.
- Controller tidak menjalankan query SQL secara langsung.
- Repository tidak mengatur alert, node, atau komponen JavaFX.
- Service tidak bergantung pada komponen UI.
- Jangan menggunakan satu class `MainApp` atau satu controller raksasa untuk seluruh fitur.
- Gunakan DTO untuk hasil pencarian yang menggabungkan beberapa entitas atau hasil rekomendasi jadwal.
- Gunakan enum untuk role dan status agar ejaan status tidak tersebar sebagai magic string.
- Tangani exception dengan pesan yang dapat dipahami pada UI; detail teknis dicatat untuk debugging.

### 6.2 Service yang diharapkan

- `AuthService` — autentikasi, pemeriksaan akun aktif, hashing/verifikasi password.
- `RoomService` — CRUD ruangan dan pencarian ruangan yang memenuhi kebutuhan.
- `InventoryService` — CRUD barang dan perhitungan stok tersedia.
- `AcademicDataService` — semester, mata kuliah, kelompok kelas, dosen.
- `LecturerAvailabilityService` — aturan jendela waktu dosen.
- `ScheduleService` — CRUD jadwal dan validasi konflik jadwal.
- `RoomBookingService` — membuat, menyetujui, menolak, membatalkan pengajuan ruang.
- `InventoryLoanService` — membuat, menyetujui, menolak, mencatat serah-terima dan pengembalian barang.
- `AlternativeScheduleService` — menghasilkan rekomendasi jadwal yang lolos hard constraints.
- `ScheduleChangeService` — mengirim permintaan perubahan dan memproses keputusan admin.
- `DashboardService` — mengumpulkan ringkasan untuk dashboard.

---

## 7. Rancangan Database

Buat skema database terpusat dalam satu inisialisasi/migration class. Gunakan primary key, foreign key, unique constraints, not-null constraints, dan indeks yang relevan.

### 7.1 Tabel inti

#### `users`

- `id` — primary key.
- `full_name` — nama pengguna.
- `username` — unik, dipakai login.
- `password_hash` — hash password, bukan password asli.
- `role` — `ADMIN` atau `USER`.
- `is_active` — boolean/integer 0 atau 1.
- `created_at`, `updated_at`.

#### `academic_terms`

- `id` — primary key.
- `term_name` — misalnya semester ganjil 2026/2027.
- `start_date`, `end_date`.
- `is_active`.

Pastikan hanya ada satu semester aktif pada satu waktu melalui validasi service/database yang sesuai.

#### `rooms`

- `id` — primary key.
- `room_code` — unik.
- `room_name`.
- `building`, `floor`.
- `capacity` — bilangan positif.
- `description` — opsional.
- `is_active`.
- `created_at`, `updated_at`.

#### `inventory_items`

- `id` — primary key.
- `item_code` — unik.
- `item_name`.
- `description`.
- `total_quantity` — bilangan bulat tidak negatif.
- `condition_notes`.
- `is_active`.
- `created_at`, `updated_at`.

#### `courses`

- `id` — primary key.
- `course_code` — unik.
- `course_name`.
- `is_active`.

#### `class_groups`

- `id` — primary key.
- `study_program`.
- `cohort_year`.
- `class_code` — contoh 3A atau IF-3B.
- `student_count` — opsional tetapi jika tersedia harus bilangan positif.
- Kombinasi program, angkatan, dan kode kelas harus divalidasi agar tidak duplikat.

#### `lecturers`

- `id` — primary key.
- `lecturer_code` — unik.
- `full_name`.
- `email` — opsional.
- `is_active`.

#### `lecturer_availability`

- `id` — primary key.
- `lecturer_id` — foreign key.
- `term_id` — foreign key.
- `day_of_week` — 1–7 dengan konvensi yang konsisten dan didokumentasikan.
- `start_time`, `end_time` — `end_time` harus lebih besar daripada `start_time`.
- `notes` — opsional.

Tabel ini merekam **rentang waktu dosen menyatakan tersedia**. Untuk rekomendasi yang terkonfirmasi, slot kandidat harus sepenuhnya berada di dalam salah satu rentang tersedia pada hari tersebut. Jika tidak ada data ketersediaan untuk hari yang dicari, tampilkan informasi bahwa data dosen belum diatur dan jangan menyebut rekomendasi sebagai terkonfirmasi.

#### `schedules`

Jadwal kuliah berulang mingguan untuk semester aktif.

- `id` — primary key.
- `term_id` — foreign key.
- `course_id` — foreign key.
- `class_group_id` — foreign key.
- `lecturer_id` — foreign key.
- `room_id` — foreign key.
- `day_of_week` — 1–7 dengan konvensi yang sama seperti `lecturer_availability`.
- `start_time`, `end_time`.
- `is_active`.
- `created_at`, `updated_at`.

#### `room_booking_requests`

- `id` — primary key.
- `requester_user_id` — foreign key ke `users`.
- `room_id` — foreign key ke `rooms`.
- `booking_date`.
- `start_time`, `end_time`.
- `expected_attendees`.
- `purpose`.
- `status` — `PENDING`, `APPROVED`, `REJECTED`, atau `CANCELLED`.
- `reviewed_by_user_id` — nullable, foreign key ke `users`.
- `reviewed_at` — nullable.
- `rejection_reason` — nullable.
- `created_at`, `updated_at`.

#### `inventory_loan_requests`

- `id` — primary key.
- `requester_user_id` — foreign key ke `users`.
- `item_id` — foreign key ke `inventory_items`.
- `quantity` — bilangan bulat positif.
- `loan_date`.
- `expected_return_date`.
- `purpose`.
- `status` — `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `HANDED_OVER`, atau `RETURNED`.
- `reviewed_by_user_id`, `reviewed_at`, `rejection_reason`.
- `handed_over_at`, `returned_at` — nullable.
- `return_condition_notes` — nullable.
- `created_at`, `updated_at`.

#### `schedule_change_requests`

- `id` — primary key.
- `requester_user_id` — foreign key ke `users`.
- `schedule_id` — foreign key ke jadwal yang akan diubah.
- `proposed_day_of_week`.
- `proposed_start_time`, `proposed_end_time`.
- `proposed_room_id` — foreign key ke `rooms`.
- `reason`.
- `status` — `PENDING`, `APPROVED`, `REJECTED`, atau `CANCELLED`.
- `reviewed_by_user_id`, `reviewed_at`, `rejection_reason`.
- `created_at`, `updated_at`.

### 7.2 Relasi dan integritas

- Setiap jadwal mengacu pada satu semester, mata kuliah, kelompok kelas, dosen, dan ruangan.
- Setiap pengajuan memiliki pemohon yang valid.
- Penonaktifan ruangan, dosen, barang, atau data akademik tidak boleh menghapus catatan transaksi lama.
- Utamakan soft deactivate (`is_active = false`) dibanding delete permanen jika data sudah digunakan oleh jadwal atau transaksi.
- Jangan menghapus jadwal atau data master yang masih direferensikan tanpa penanganan yang jelas.
- Tambahkan indeks untuk kolom yang sering difilter: status, tanggal peminjaman, `term_id`, `day_of_week`, `room_id`, `lecturer_id`, dan `class_group_id`.

---

## 8. Aturan Bisnis dan Validasi Wajib

### 8.1 Validasi waktu

Rentang waktu dianggap tumpang tindih jika:

```text
candidateStart < existingEnd AND candidateEnd > existingStart
```

Dengan aturan ini, jadwal pukul 09.00–10.00 dan 10.00–11.00 tidak bentrok. Namun jadwal 09.30–10.30 dan 10.00–11.00 bentrok.

Semua form waktu wajib memastikan:

- jam selesai lebih besar daripada jam mulai;
- tanggal wajib diisi;
- durasi jadwal kuliah tidak nol atau negatif;
- peserta tidak melebihi kapasitas ruangan;
- waktu peminjaman mengikuti jam operasional yang dikonfigurasi.

### 8.2 Konflik jadwal kuliah

Saat membuat atau mengubah jadwal, kandidat tidak boleh bertumpang tindih dengan jadwal aktif lain pada semester yang sama apabila terdapat salah satu kondisi berikut:

1. menggunakan ruangan yang sama;
2. melibatkan dosen yang sama;
3. melibatkan kelompok kelas yang sama.

Untuk pencarian alternatif, kecualikan record `schedule_id` yang sedang diubah saat memeriksa slot kandidat; jangan sampai jadwal bertabrakan dengan record dirinya sendiri. Setelah admin menyetujui permintaan, update jadwal asli dalam satu transaksi database.

### 8.3 Ketersediaan dosen

- Slot kandidat harus berada sepenuhnya dalam jendela ketersediaan dosen pada hari dan semester yang dipilih.
- Slot kandidat juga harus bebas dari jadwal kuliah dosen lain yang aktif.
- Jika ketersediaan dosen belum diatur, beri penjelasan yang jelas dan minta admin melengkapi datanya. Jangan memperlakukan data yang tidak diketahui sebagai bukti bahwa dosen tersedia.

### 8.4 Ketersediaan ruangan dan peminjaman

- Ruangan nonaktif tidak dapat dipilih.
- Kapasitas ruangan harus cukup untuk jumlah peserta.
- Untuk peminjaman tanggal tertentu, cek jadwal kuliah berulang yang jatuh pada hari itu dan seluruh peminjaman ruangan berstatus `APPROVED` yang waktunya bertumpang tindih.
- Pengajuan `PENDING` belum memblokir ruang secara permanen. Namun admin harus memeriksa semua pengajuan saat menyetujui satu permintaan. Setelah satu permintaan disetujui, permintaan lain yang bertabrakan tidak boleh ikut disetujui.
- Saat user mengirim permintaan, validasi konflik untuk memberikan umpan balik awal. Saat admin menyetujui, validasi harus dijalankan **sekali lagi** karena data bisa berubah sejak pengajuan dibuat.

### 8.5 Ketersediaan inventaris

Hitung jumlah yang sedang dicadangkan/dipinjam dari pengajuan berstatus `APPROVED` atau `HANDED_OVER`. Pengajuan `RETURNED`, `REJECTED`, dan `CANCELLED` tidak mengurangi stok tersedia.

```text
availableQuantity = totalQuantity - sum(quantity dari status APPROVED dan HANDED_OVER)
```

- Jumlah yang diminta harus lebih besar dari nol.
- Tanggal kembali yang diharapkan tidak boleh lebih awal daripada tanggal pinjam.
- Saat admin menyetujui, hitung stok sekali lagi di dalam transaksi database.
- Pada serah-terima, ubah status menjadi `HANDED_OVER` dan simpan waktunya.
- Pada pengembalian penuh, ubah status menjadi `RETURNED`, simpan waktu kembali dan catatan kondisi.
- Jangan menyelesaikan dua persetujuan bersamaan berdasarkan hasil stok lama; lakukan validasi dan pembaruan dalam transaksi.

### 8.6 Permintaan perubahan jadwal

- Rekomendasi hanya menampilkan slot yang lolos semua hard constraints.
- Durasi tetap sama dengan jadwal asli.
- Preferensi user seperti hari atau waktu yang disukai adalah soft preference: digunakan untuk mengurutkan kandidat, bukan untuk mengizinkan bentrok.
- Mengirim permintaan tidak langsung mengubah jadwal resmi.
- Saat admin menyetujui, jalankan ulang semua pengecekan dalam transaksi. Jika sudah tidak valid, batalkan proses persetujuan dan jelaskan penyebabnya; jadwal lama harus tetap utuh.
- Jika ditolak, simpan alasan penolakan.
- Catat siapa yang mengambil keputusan dan kapan.

---

## 9. Algoritma Pencarian Alternatif Jadwal

Implementasikan algoritma ini di service terpisah, bukan di controller JavaFX.

### 9.1 Input

- `scheduleId` dari jadwal berulang yang ingin diubah.
- Preferensi hari (satu atau beberapa hari dalam seminggu).
- Rentang waktu yang diinginkan, bila diisi.
- Preferensi ruangan/gedung, bila diisi.

### 9.2 Langkah pencarian

1. Ambil jadwal asli, semester, mata kuliah, kelompok kelas, dosen, ruangan saat ini, dan durasi jadwal.
2. Ambil ruangan aktif dengan kapasitas yang memenuhi kebutuhan kelompok kelas.
3. Untuk setiap hari yang dapat dicari, ambil jendela ketersediaan dosen pada semester tersebut.
4. Bentuk kandidat waktu mulai dengan interval pencarian yang konsisten, misalnya setiap 30 menit. Jadikan interval ini konfigurasi agar mudah diubah.
5. Hitung `endTime = startTime + duration jadwal asli`.
6. Singkirkan kandidat di luar jam operasional atau di luar jendela ketersediaan dosen.
7. Periksa bentrok jadwal aktif untuk dosen, kelompok kelas, dan ruangan, dengan mengecualikan jadwal asli yang sedang diganti.
8. Periksa peminjaman ruangan berstatus `APPROVED` yang dapat berbenturan dengan slot berulang itu selama rentang semester aktif. Karena slot akademik berlaku setiap minggu, periksa seluruh tanggal dalam semester yang jatuh pada hari kandidat.
9. Singkirkan kandidat yang melanggar salah satu aturan keras.
10. Urutkan kandidat valid berdasarkan preferensi user, lalu waktu paling dekat dengan preferensi dan kesesuaian kapasitas/ruangan.
11. Tampilkan kandidat valid beserta hari, jam, ruangan, kapasitas, dan durasi. Jika tidak ada kandidat, jelaskan bahwa tidak ada slot yang memenuhi seluruh batasan dan sarankan agar preferensi diperlebar atau data ketersediaan diperiksa.

### 9.3 Hard constraints vs soft preferences

**Hard constraints (tidak boleh dilanggar):**

- durasi pertemuan tetap;
- dosen tersedia;
- tidak bentrok dengan jadwal dosen;
- tidak bentrok dengan jadwal kelompok kelas;
- tidak bentrok dengan jadwal ruangan;
- kapasitas ruangan cukup;
- ruangan dan data terkait aktif;
- tidak bentrok dengan peminjaman ruangan yang telah disetujui selama semester aktif.

**Soft preferences (untuk pengurutan):**

- hari pilihan user;
- jam mulai yang disukai;
- gedung/ruangan pilihan jika masih ada beberapa kandidat valid.

### 9.4 Output

Buat DTO seperti `ScheduleAlternative` yang paling sedikit memuat:

- `dayOfWeek`;
- `startTime`;
- `endTime`;
- `roomId`, `roomCode`, `roomName`;
- `capacity`;
- `durationMinutes`;
- ringkasan mengapa kandidat ditampilkan atau prioritas relatifnya.

Jangan menampilkan slot yang tidak valid sebagai rekomendasi valid. Untuk MVP, lebih baik menampilkan sedikit rekomendasi yang benar daripada banyak rekomendasi yang masih menimbulkan bentrok.

---

## 10. Alur Utama Aplikasi

### 10.1 Login

1. User membuka aplikasi.
2. Sistem menampilkan halaman login.
3. `AuthService` memverifikasi username, password hash, dan status akun.
4. Jika valid, simpan sesi user di satu objek session sederhana.
5. Arahkan ke dashboard sesuai role.
6. Saat logout, bersihkan session dan kembali ke login.

### 10.2 Peminjaman ruangan

1. User memilih tanggal, jam mulai, jam selesai, jumlah peserta, dan tujuan.
2. Sistem memvalidasi input.
3. Sistem menampilkan ruangan yang cocok dan informasi konflik/ketersediaan.
4. User memilih ruangan dan mengirim permintaan.
5. Sistem menyimpan permintaan sebagai `PENDING`.
6. Admin melihat permintaan di antrean.
7. Saat admin menyetujui, sistem menjalankan pengecekan ketersediaan lagi dalam transaksi.
8. Jika lolos, status menjadi `APPROVED`; jika tidak, jangan setujui dan tampilkan konflik.
9. User dapat melihat status terbaru pada halaman Pengajuan Saya.

### 10.3 Peminjaman barang

1. User membuka katalog inventaris.
2. Sistem menampilkan total unit dan estimasi unit tersedia.
3. User memilih barang, jumlah, tanggal pinjam, tanggal pengembalian yang direncanakan, dan tujuan.
4. Sistem memvalidasi jumlah dan tanggal, lalu menyimpan status `PENDING`.
5. Admin menyetujui/menolak. Saat menyetujui, sistem memeriksa stok kembali.
6. Setelah barang diberikan, admin mencatat serah-terima (`HANDED_OVER`).
7. Saat barang dikembalikan, admin mencatat waktu dan kondisi, lalu status menjadi `RETURNED`.

### 10.4 Perubahan jadwal kuliah

1. User memilih jadwal kuliah aktif yang ingin dipindahkan.
2. User menentukan hari/waktu yang diinginkan.
3. Sistem mencari kandidat menggunakan algoritma pada Bagian 9.
4. User memilih salah satu kandidat valid dan memasukkan alasan.
5. Sistem menyimpan `schedule_change_requests` dengan status `PENDING`; jadwal asli tidak berubah.
6. Admin memeriksa detail jadwal lama, slot baru, dan alasan.
7. Admin menyetujui/menolak.
8. Jika disetujui, sistem melakukan validasi ulang dan mengubah record jadwal asli dalam satu transaksi.
9. Permintaan berubah menjadi `APPROVED`, dan jadwal baru muncul pada tampilan jadwal kampus.

---

## 11. Pembagian Jobdesk untuk 2 Orang

**Total pembagian:** masing-masing orang bertanggung jawab atas 25% frontend + 25% backend (total 50% pekerjaan per orang). Pembagian dilakukan berdasarkan modul agar tiap anggota memahami tampilan dan logika data modulnya.

### 11.1 Orang 1 — Master Data & Manajemen Fasilitas

#### Frontend — 25% dari proyek

- UI login bersama tim; tentukan satu pemilik implementasi agar tidak dibuat dua kali.
- Dashboard admin dan dashboard user dasar.
- Halaman daftar/form ruangan.
- Halaman daftar/form inventaris.
- Halaman data akademik: mata kuliah, kelompok kelas, dosen, semester.
- Halaman manajemen jadwal kuliah.
- Halaman pengaturan ketersediaan dosen.
- Komponen UI bersama: sidebar, header, tabel, field form, status label, konfirmasi umum.

#### Backend — 25% dari proyek

- Setup database awal, `DatabaseManager`, inisialisasi schema, foreign keys, dan seed demo.
- Model dan repository untuk user, ruangan, inventaris, data akademik, ketersediaan dosen, dan jadwal.
- `AuthService` dan validasi role dasar.
- CRUD master data.
- Validasi bentrok ketika admin membuat/mengubah jadwal.
- `DashboardService` untuk ringkasan data dasar.

#### Hasil kerja yang harus diserahkan

- Database dapat dibuat otomatis pada first run.
- Admin dapat mengelola master data dan jadwal.
- User dapat membuka serta membaca jadwal dan informasi fasilitas.
- API service/repository terdokumentasi agar dapat digunakan Orang 2.

### 11.2 Orang 2 — Peminjaman & Pencarian Alternatif Jadwal

#### Frontend — 25% dari proyek

- Halaman pencarian ketersediaan dan formulir peminjaman ruangan.
- Halaman katalog dan formulir peminjaman barang untuk user.
- Halaman Pengajuan Saya dan riwayat.
- Halaman pencarian alternatif jadwal dan hasil rekomendasi.
- Halaman permintaan perubahan jadwal.
- Antrean persetujuan admin untuk peminjaman ruangan/barang dan perubahan jadwal.
- Halaman/catatan serah-terima serta pengembalian barang.

#### Backend — 25% dari proyek

- `RoomBookingService`: buat pengajuan, cek konflik, setujui/tolak/batalkan.
- `InventoryLoanService`: validasi jumlah, hitung stok, setujui/tolak, serah-terima, pengembalian.
- `AlternativeScheduleService`: pencarian dan pengurutan kandidat valid.
- `ScheduleChangeService`: pengajuan perubahan, persetujuan, validasi ulang, dan pembaruan jadwal dalam transaksi.
- Pengujian aturan bisnis untuk bentrok waktu, stok, dan status transaksi.

#### Hasil kerja yang harus diserahkan

- Alur peminjaman ruangan dan barang berjalan dari sisi user sampai keputusan admin.
- Stok barang dihitung sesuai status transaksi dan tidak dapat disetujui melebihi jumlah tersedia.
- Pencarian alternatif hanya mengeluarkan slot yang lolos batasan.
- Jadwal asli tidak berubah sebelum permintaan perubahan disetujui.

### 11.3 Tanggung jawab bersama

Kedua anggota wajib bersama-sama:

- menyepakati nama kolom, model, enum status, format tanggal/waktu, dan method service sebelum integrasi;
- melakukan code review dan testing fitur anggota lain;
- menyelesaikan konflik merge Git jika dipakai;
- menjalankan tes integrasi sebelum demo;
- menulis dokumentasi run, akun demo, dan batasan aplikasi;
- tidak membuat schema atau class model duplikat.

### 11.4 Kontrak integrasi antarangota

- Semua operasi database dilakukan melalui repository.
- Semua validasi aturan bisnis dilakukan melalui service.
- Controller memanggil service, lalu menampilkan hasil.
- Sepakati method service dan DTO sebelum membangun controller yang memakainya.
- Jika Person 2 membutuhkan fitur dari Person 1 yang belum tersedia, buat interface/method contract terlebih dahulu; jangan menanam query SQL langsung ke controller.
- Setiap perubahan schema harus diberitahukan kepada anggota lain dan dicatat di dokumen/commit.

---

## 12. Tahapan Implementasi dan Kriteria Selesai

Kerjakan dalam urutan berikut. Setelah tiap tahap, jalankan build dan tes yang relevan sebelum melanjutkan.

### Tahap 0 — Pemeriksaan repository dan environment

**Tugas**

- Periksa apakah repository kosong atau sudah berisi kode.
- Periksa versi Java dan Maven yang tersedia.
- Jangan menimpa kode/folder yang sudah ada tanpa memeriksanya.
- Catat temuan dan pilih struktur yang kompatibel.

**Kriteria selesai**

- Antigravity mengetahui file/folder yang sudah ada, versi Java, build command, dan perubahan awal yang diperlukan.
- Tidak ada file pengguna yang terhapus atau tertimpa tanpa alasan.

### Tahap 1 — Scaffold, database, dan aplikasi dasar

**Tugas**

- Siapkan `pom.xml`, main class JavaFX, FXML dasar, CSS, dan README.
- Buat konfigurasi path database.
- Buat `DatabaseManager`, koneksi JDBC, inisialisasi schema, dan seed demo yang idempotent.
- Tambahkan `.gitignore` untuk file database lokal, target build, dan file IDE yang tidak perlu dibagikan.
- Pastikan foreign keys aktif untuk setiap koneksi.

**Kriteria selesai**

- Aplikasi dapat dikompilasi dan dibuka.
- Database lokal dibuat otomatis jika belum ada.
- Menjalankan aplikasi beberapa kali tidak membuat seed data duplikat.
- Perintah build/run tertulis di README.

### Tahap 2 — Login, session, dan navigasi berdasarkan role

**Tugas**

- Implementasikan login dan logout.
- Hash dan verifikasi password.
- Implementasikan objek session dengan user dan role saat ini.
- Buat shell layout dengan sidebar/header.
- Arahkan admin/user ke dashboard masing-masing.
- Cegah akun nonaktif login dan cegah akses fungsi admin dari user.

**Kriteria selesai**

- Login benar berhasil; password salah menghasilkan pesan yang aman dan mudah dipahami.
- Dashboard sesuai role.
- Logout menghapus session.
- Pemeriksaan hak akses berada di service/controller, bukan hanya UI.

### Tahap 3 — Master data dan CRUD

**Tugas**

- Bangun repository, service, controller, dan halaman CRUD ruangan.
- Bangun CRUD barang inventaris.
- Bangun CRUD semester, mata kuliah, kelompok kelas, dan dosen.
- Bangun pengaturan jendela ketersediaan dosen.
- Tambahkan search/filter sederhana dan validasi field.
- Gunakan soft deactivate untuk data yang sudah digunakan.

**Kriteria selesai**

- Admin dapat menambah, melihat, mengubah, dan menonaktifkan data.
- Kode ruangan/barang/mata kuliah/dosen yang harus unik tidak bisa diduplikasi.
- Input kapasitas, stok, dan jumlah peserta divalidasi.
- Data tetap ada setelah aplikasi ditutup dan dibuka lagi.

### Tahap 4 — Jadwal akademik dan tampilan jadwal

**Tugas**

- Implementasikan CRUD jadwal berulang mingguan.
- Implementasikan pengecekan bentrok waktu untuk ruangan, dosen, dan kelompok kelas.
- Tampilkan jadwal pada halaman user dengan filter yang berguna.
- Tampilkan hari dan waktu secara konsisten.
- Batasi jadwal pada semester aktif.

**Kriteria selesai**

- Jadwal valid dapat disimpan dan ditampilkan.
- Jadwal yang bentrok pada ruangan/dosen/kelompok kelas ditolak dengan alasan yang jelas.
- Slot yang bersentuhan tepat di batas waktu (misalnya 09.00–10.00 dan 10.00–11.00) tidak dianggap bentrok.
- User hanya melihat jadwal yang diizinkan untuk dilihat.

### Tahap 5 — Peminjaman ruangan

**Tugas**

- Buat form pengajuan dengan tanggal, waktu, jumlah peserta, tujuan, dan ruangan.
- Tampilkan ruangan aktif yang memenuhi kapasitas.
- Periksa konflik dengan jadwal kuliah pada hari tersebut dan peminjaman ruangan yang disetujui.
- Simpan permintaan sebagai `PENDING`.
- Bangun antrean keputusan admin serta aksi setujui/tolak.
- Saat persetujuan, lakukan pengecekan konflik ulang dalam transaksi.
- Bangun halaman status/riwayat user.

**Kriteria selesai**

- User dapat mengirim pengajuan valid dan melihat statusnya.
- Ruangan yang tidak cukup kapasitas atau sedang dipakai tidak dapat disetujui.
- Dua permintaan yang bertabrakan tidak dapat sama-sama disetujui.
- Penolakan menyimpan alasan.

### Tahap 6 — Peminjaman inventaris

**Tugas**

- Buat katalog inventaris dan form pengajuan satu jenis barang.
- Hitung estimasi stok tersedia dari transaksi aktif.
- Buat antrean persetujuan admin.
- Revalidasi stok saat approval.
- Buat aksi serah-terima dan pengembalian penuh.
- Simpan waktu serta kondisi saat pengembalian.

**Kriteria selesai**

- Jumlah negatif/nol dan tanggal kembali yang tidak valid ditolak.
- Pengajuan tidak dapat disetujui jika stok tidak cukup.
- Barang yang sudah dikembalikan kembali dihitung tersedia.
- Riwayat status dan waktu transaksi dapat dilihat admin dan pemohon sesuai hak akses.

### Tahap 7 — Pencarian alternatif dan pengajuan perubahan jadwal

**Tugas**

- Bangun DTO hasil rekomendasi.
- Implementasikan algoritma pada Bagian 9 dalam service terpisah.
- Tampilkan preferensi hari/waktu dan daftar kandidat valid.
- Beri informasi jika tidak ditemukan alternatif atau data dosen belum lengkap.
- User dapat memilih kandidat dan menambahkan alasan.
- Simpan permintaan sebagai `PENDING` tanpa mengubah jadwal asli.
- Admin dapat meninjau serta menyetujui/menolak.
- Saat menyetujui, lakukan validasi ulang dan perubahan jadwal dalam satu transaksi.

**Kriteria selesai**

- Semua rekomendasi memenuhi durasi dan hard constraints.
- Slot yang bentrok tidak pernah muncul sebagai kandidat valid.
- Mengirim permintaan tidak mengubah jadwal aktif.
- Persetujuan yang gagal karena konflik baru tidak mengubah jadwal lama.
- Jadwal resmi hanya berubah setelah persetujuan berhasil.

### Tahap 8 — Dashboard, riwayat, dan penyempurnaan UX

**Tugas**

- Isi dashboard dengan angka yang berasal dari database, bukan angka hardcoded.
- Tampilkan pengajuan yang menunggu keputusan dan pengembalian yang mendekati jatuh tempo.
- Sediakan filter status/tanggal untuk riwayat.
- Perbaiki empty state, pesan error, validasi, dan konsistensi komponen.
- Tambahkan konfirmasi sebelum aksi yang sulit dibatalkan.

**Kriteria selesai**

- Angka dashboard sesuai data tersimpan.
- Tidak ada tombol utama yang tidak melakukan aksi.
- Error database tidak membuat aplikasi crash tanpa pesan yang berguna.
- Navigasi dapat digunakan dengan urutan yang masuk akal.

### Tahap 9 — Pengujian akhir, dokumentasi, dan packaging

**Tugas**

- Jalankan unit test dan integration test.
- Uji semua alur berdasarkan role.
- Uji data kosong, data salah, bentrok, stok habis, dan perubahan data antara pengajuan dan persetujuan.
- Bersihkan demo data dari skenario pengujian sebelum demo akhir jika dibutuhkan.
- Tulis cara menjalankan, fitur, akun demo, batasan, struktur modul, dan troubleshooting di README.
- Siapkan cara menjalankan aplikasi dari terminal/IDE yang sesuai dengan lingkungan tugas.

**Kriteria selesai**

- Build berhasil dari kondisi bersih.
- Seluruh tes wajib lulus atau setiap tes yang gagal didokumentasikan beserta alasannya.
- Aplikasi dapat dijalankan sesuai petunjuk README.
- Tidak ada klaim fitur yang belum diimplementasikan.

---

## 13. Pengujian Wajib

### 13.1 Login dan role

- Username/password benar.
- Password salah.
- Akun nonaktif.
- User mencoba membuka fungsi admin.
- Logout dan login kembali.

### 13.2 Konflik jadwal

- Dua kelas berbeda menggunakan ruangan sama pada jam tumpang tindih.
- Dosen yang sama memiliki dua kelas pada jam tumpang tindih.
- Kelompok kelas yang sama memiliki dua kelas pada jam tumpang tindih.
- Jadwal yang bersisian tepat pada batas jam tidak ditolak.
- Slot alternatif di luar ketersediaan dosen tidak disarankan.
- Tidak ada data ketersediaan dosen untuk hari tersebut.
- Kapasitas ruangan kurang dari kebutuhan kelas.

### 13.3 Peminjaman ruangan

- Pengajuan pada ruangan kosong.
- Pengajuan pada waktu jadwal kuliah.
- Pengajuan bertabrakan dengan booking yang sudah disetujui.
- Dua pengajuan pending untuk waktu yang sama.
- Admin mencoba menyetujui keduanya.
- Jadwal berubah setelah pengajuan dibuat, sebelum admin menyetujui.
- Pengajuan ditolak dan alasan penolakan terlihat oleh pemohon.

### 13.4 Peminjaman barang

- Jumlah yang diminta sama dengan stok tersedia.
- Jumlah yang diminta melebihi stok.
- Dua permintaan yang jika dijumlahkan melebihi stok.
- Pengajuan yang ditolak tidak mengurangi stok.
- Pengajuan `APPROVED`/`HANDED_OVER` mengurangi stok tersedia.
- Pengembalian penuh membuat stok tersedia kembali.
- Tanggal pengembalian lebih awal daripada tanggal pinjam.

### 13.5 Perubahan jadwal

- Kandidat valid ditampilkan.
- Kandidat dengan konflik ruangan disaring.
- Kandidat dengan konflik dosen disaring.
- Kandidat dengan konflik kelompok kelas disaring.
- Durasi kandidat sama dengan durasi jadwal asli.
- User mengajukan perubahan; jadwal asli tetap sama.
- Admin menyetujui permintaan yang masih valid.
- Terjadi konflik baru sebelum persetujuan; approval gagal dan jadwal lama tetap sama.
- Admin menolak; alasan penolakan tersimpan.
- Tidak ada kandidat valid; UI menampilkan empty state yang menjelaskan situasinya.

---

## 14. Keamanan dan Kualitas Kode

- Gunakan `PreparedStatement`; jangan membangun SQL dengan menggabungkan input user.
- Hash password. Jangan mencatat password ke log.
- Jangan menampilkan detail SQL/stack trace ke pengguna biasa.
- Validasi semua input di service, walaupun validasi UI sudah ada.
- Gunakan transaksi database untuk operasi yang terdiri dari beberapa perubahan, terutama approval dan update jadwal.
- Selalu tutup koneksi, statement, dan result set dengan `try-with-resources`.
- Gunakan enum untuk role/status dan satu definisi untuk format tanggal/waktu.
- Hindari duplikasi aturan konflik di berbagai controller. Satu service validasi harus menjadi sumber kebenaran.
- Jangan menghapus catatan peminjaman yang telah selesai hanya karena item atau user dinonaktifkan.
- Jangan menyimpan database hasil runtime di folder resource yang akan dipaketkan sebagai read-only.
- Hindari API/dependency yang deprecated jika tersedia alternatif stabil.
- Jangan memasukkan secret/password pribadi atau data mahasiswa asli ke repository. Gunakan data dummy.

---

## 15. Kebutuhan README

README harus memuat:

1. Nama dan tujuan aplikasi.
2. Daftar fitur yang benar-benar tersedia.
3. Stack teknologi dan versi yang digunakan.
4. Prasyarat instalasi.
5. Perintah build, test, dan run.
6. Lokasi database lokal dan cara reset data demo.
7. Akun demo untuk role Admin dan User jika seed akun demo dibuat; jelaskan bahwa kredensial itu hanya untuk demonstrasi.
8. Struktur modul/paket yang penting.
9. Alur singkat peminjaman ruangan, peminjaman barang, dan perubahan jadwal.
10. Batasan MVP, terutama database lokal satu komputer dan ketersediaan dosen yang harus diatur.
11. Troubleshooting untuk masalah umum.

---

## 16. Instruksi Operasional untuk Antigravity

Gunakan aturan berikut selama mengimplementasikan proyek:

1. **Inspect before edit.** Periksa struktur proyek dan file yang sudah ada; jangan langsung membuat ulang seluruh project.
2. **Implement incrementally.** Ikuti urutan Tahap 0–9. Jangan membangun semua modul sekaligus dalam satu perubahan besar.
3. **Keep it runnable.** Setelah setiap tahap, jalankan build dan tes yang relevan. Perbaiki regresi sebelum melanjutkan.
4. **Use the architecture.** Jangan memindahkan query SQL ke controller atau aturan bisnis ke FXML.
5. **No fake functionality.** Jangan membuat tombol yang hanya terlihat aktif, data dashboard hardcoded, atau rekomendasi jadwal yang tidak menjalankan pemeriksaan konflik.
6. **No false success.** Jangan mengklaim build/test berhasil kecuali command benar-benar dijalankan dan berhasil.
7. **Explain blockers.** Jika library, environment, atau persyaratan tidak tersedia, jelaskan penyebabnya dan pilih alternatif sederhana yang kompatibel.
8. **Minimize dependencies.** Gunakan dependensi yang diperlukan saja dan dokumentasikan pilihannya.
9. **Preserve project conventions.** Jika repository sudah punya pola penamaan atau struktur yang baik, ikuti pola tersebut dan update dokumen jika dibutuhkan.
10. **Keep domain rules centralized.** Gunakan service yang sama untuk validasi saat submit, rekomendasi, dan approval ulang.
11. **Report each milestone.** Setelah menyelesaikan tahap, laporkan ringkas: file/modul yang dibuat, fitur yang selesai, command test/build yang dijalankan, hasilnya, serta pekerjaan yang belum selesai.
12. **Do not silently relax constraints.** Jika aturan validasi membuat tidak ada kandidat, jangan menghapus validasi demi membuat hasil muncul. Periksa data demo, konfigurasi, atau jelaskan penyebabnya.

### Urutan kerja pertama yang diminta

Mulai dari **Tahap 0 dan Tahap 1**. Periksa repository, pastikan Java/Maven cocok, kemudian siapkan skeleton JavaFX + Maven + SQLite dan README awal. Jangan mulai membuat semua halaman CRUD sebelum aplikasi dasar berhasil dijalankan. Setelah itu lanjutkan tahap berikutnya secara berurutan.

---

## 17. Checklist Demo Akhir

Sebelum presentasi, pastikan skenario ini dapat didemonstrasikan:

- [ ] Login sebagai Admin dan User menampilkan dashboard yang berbeda.
- [ ] Admin menambah ruangan, barang, dosen, kelompok kelas, mata kuliah, dan jadwal.
- [ ] User melihat jadwal kampus.
- [ ] User mengajukan peminjaman ruangan dan melihat status `PENDING`.
- [ ] Admin menyetujui pengajuan ruangan yang valid.
- [ ] Pengajuan ruangan yang bentrok tidak bisa disetujui.
- [ ] User mengajukan peminjaman barang.
- [ ] Admin menyetujui, mencatat serah-terima, lalu mencatat pengembalian barang.
- [ ] Stok tersedia berubah sesuai transaksi.
- [ ] Admin mengisi ketersediaan dosen.
- [ ] User mencari alternatif jadwal dan sistem hanya menampilkan kandidat valid.
- [ ] User mengirim permintaan perubahan jadwal; jadwal awal belum berubah.
- [ ] Admin menyetujui permintaan yang valid dan jadwal resmi diperbarui.
- [ ] Jika permintaan ditolak atau validasi ulang gagal, alasan ditampilkan dan jadwal lama tidak rusak.
- [ ] Aplikasi dapat ditutup dan dibuka kembali tanpa kehilangan data tersimpan.
- [ ] README memiliki instruksi run dan akun demo.

---

## 18. Kriteria Keberhasilan Proyek

Proyek dianggap mencapai MVP apabila:

1. Aplikasi JavaFX dapat dijalankan dan terhubung ke SQLite.
2. Login dan dua role bekerja sesuai hak akses.
3. Admin dapat mengelola master data dan jadwal akademik.
4. User dapat mencari informasi fasilitas serta mengajukan peminjaman ruangan/barang.
5. Admin dapat memproses pengajuan dan mencatat alur serah-terima/pengembalian.
6. Pencarian alternatif mempertimbangkan durasi, ketersediaan dosen, konflik kelas, konflik ruangan, kapasitas, dan booking yang disetujui.
7. Persetujuan perubahan jadwal memvalidasi ulang konflik dan tidak merusak jadwal lama jika proses gagal.
8. Pengujian inti lulus, dokumentasi tersedia, dan keterbatasan aplikasi dinyatakan secara jujur.

**Prioritas:** kebenaran data dan validasi konflik lebih penting daripada banyaknya halaman atau tampilan dekoratif. Selesaikan alur inti sampai berfungsi sebelum menambahkan fitur di luar MVP.
