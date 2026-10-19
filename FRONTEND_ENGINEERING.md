# FRONTEND ENGINEERING SPECIFICATION
## Campus Facility & Schedule Manager — JavaFX Desktop Application

> **Tujuan dokumen:** menjadi spesifikasi front-end yang dapat diikuti Antigravity saat mengimplementasikan antarmuka aplikasi desktop pengelolaan fasilitas kampus.
>
> **Dokumen acuan:** `IMPLEMENTATION_PLAN.md`.
>
> **Instruksi utama untuk Antigravity:** baca dokumen ini dan `IMPLEMENTATION_PLAN.md` sebelum mengubah kode. Periksa repository terlebih dahulu. Implementasikan UI secara bertahap, gunakan JavaFX + FXML + JavaFX CSS, dan pastikan setiap halaman terhubung ke service yang sesuai. Jangan membuat web app, jangan menambahkan framework front-end web, dan jangan menganggap mockup atau tombol tanpa aksi sebagai fitur selesai.

---

## 1. Tujuan dan Ruang Lingkup Front-end

Front-end adalah antarmuka desktop yang digunakan oleh dua role:

1. **Admin:** mengelola data fasilitas dan akademik, memproses pengajuan, serta menyetujui atau menolak permintaan perubahan jadwal.
2. **User:** melihat jadwal dan fasilitas, mengajukan peminjaman, melacak status pengajuan, serta mencari alternatif jadwal.

Front-end harus membantu pengguna memahami tiga hal tanpa menebak:

- Data atau fasilitas apa yang tersedia.
- Tindakan apa yang dapat dilakukan pengguna pada halaman saat ini.
- Status pengajuan atau hasil validasi yang sedang berlaku.

### 1.1 Yang wajib dibangun

- Login dan logout.
- Shell aplikasi utama: sidebar, header, area konten, judul halaman, dan informasi akun.
- Dashboard berbeda untuk Admin dan User.
- Halaman jadwal kampus, data ruangan, dan inventaris.
- Formulir pengajuan peminjaman ruangan dan barang.
- Daftar pengajuan dan detail status.
- Halaman pencarian alternatif jadwal beserta hasil rekomendasi.
- Halaman admin untuk data akademik, persetujuan peminjaman, pengembalian barang, serta persetujuan perubahan jadwal.
- Loading, empty, error, validation, success, dan confirmation state.
- Navigasi berdasarkan role dan sesi login.

### 1.2 Yang tidak dibangun pada MVP

- Front-end berbasis web, HTML, React, Vue, Angular, atau Tailwind.
- Aplikasi Android/APK.
- Sinkronisasi langsung dengan SIAKAD.
- Notifikasi WhatsApp/email/push.
- Dashboard analitik kompleks.
- Kalender drag-and-drop yang rumit.
- Multi-window navigation untuk setiap halaman jika tidak diperlukan.
- Desain responsif untuk browser/mobile. Aplikasi ini adalah aplikasi desktop; yang perlu ditangani adalah perubahan ukuran window desktop.

---

## 2. Teknologi dan Komponen yang Dipakai

| Kebutuhan | Teknologi/komponen | Aturan penggunaan |
|---|---|---|
| Bahasa UI | Java 21 | Selaraskan versi dengan `IMPLEMENTATION_PLAN.md` dan `pom.xml`. |
| Framework desktop | JavaFX | Semua halaman berjalan dalam satu aplikasi desktop Java. |
| Struktur tampilan | FXML | Pisahkan susunan komponen dari kode controller. |
| Styling | JavaFX CSS | Gunakan satu stylesheet utama dan class CSS yang konsisten. Jangan menggunakan CSS browser. |
| Layout | `BorderPane`, `VBox`, `HBox`, `GridPane`, `StackPane`, `AnchorPane` jika memang perlu | Utamakan layout yang dapat menyesuaikan ukuran window. Hindari penempatan semua elemen memakai koordinat tetap. |
| Navigasi | Satu `Stage` utama dengan shell dan content container | Ganti konten halaman di area utama; jangan membuat Stage baru untuk setiap navigasi. Dialog boleh menggunakan `Dialog`/`Alert`. |
| Tabel data | `TableView`, `TableColumn`, `FilteredList`, `SortedList` bila diperlukan | Gunakan untuk data jadwal, ruangan, inventaris, dan pengajuan. Sertakan empty state. |
| Form input | `TextField`, `PasswordField`, `TextArea`, `ComboBox`, `DatePicker`, `Spinner`, `CheckBox` | Validasi di UI untuk umpan balik cepat dan validasi ulang di service. |
| Tombol dan status | `Button`, `Label`, `Hyperlink`, `ProgressIndicator`, `ProgressBar` jika relevan | Setiap kontrol interaktif harus memiliki aksi yang jelas. Status tidak hanya dibedakan melalui warna. |
| Konfirmasi | `Alert`, `Dialog<ButtonType>` | Gunakan untuk aksi yang penting/destruktif atau keputusan yang tidak mudah dibatalkan. |
| Data tampilan | `ObservableList` dan JavaFX properties jika sesuai | Controller mengubah model tampilan, bukan menulis query SQL. |
| Operasi yang berpotensi lama | `Task<T>` atau `Service<T>` JavaFX | Hindari query atau pemrosesan berat di JavaFX Application Thread. |
| Pengujian UI dasar | JUnit 5 untuk logika pendukung; pengujian manual untuk alur UI | Uji semua state penting dan ukuran window yang didukung. |

### 2.1 Library UI eksternal

Untuk MVP, **gunakan kontrol bawaan JavaFX terlebih dahulu**. Jangan menambahkan MaterialFX, JFoenix, Ikonli, ControlsFX, atau library visual lain hanya demi mempercantik tampilan. Library tambahan boleh dipertimbangkan jika ada kebutuhan yang jelas, dependensinya kompatibel, dan penggunaannya dijelaskan dalam `README.md` serta `pom.xml`.

Untuk ikon, prioritas:

1. Ikon sederhana dengan teks atau simbol yang mudah dipahami.
2. Ikon vector lokal yang memang disertakan di repository.
3. Library ikon hanya jika benar-benar diperlukan dan telah ditambahkan secara sengaja.

Jangan mengandalkan emoji sebagai ikon navigasi utama karena tampilannya dapat berbeda menurut sistem operasi/font.

---

## 3. Prinsip Desain UI

### 3.1 Karakter visual

Gunakan desain desktop yang:

- Bersih, rapi, profesional, dan cocok untuk aplikasi administrasi kampus.
- Mengutamakan keterbacaan tabel dan formulir.
- Konsisten antara halaman Admin dan User.
- Menggunakan warna aksen seperlunya; jangan memenuhi seluruh halaman dengan warna terang.
- Tidak menyerupai template dashboard web yang dipaksakan ke desktop.

### 3.2 Design tokens

Definisikan token sebagai class CSS atau selector root di `src/main/resources/css/app.css`. Warna di bawah merupakan baseline yang boleh diubah bila repository telah memiliki tema yang konsisten.

| Token | Nilai awal | Kegunaan |
|---|---|---|
| `color-primary` | `#2457A7` | Tombol utama, elemen navigasi aktif, fokus penting. |
| `color-primary-hover` | `#1C4587` | Hover tombol utama. |
| `color-background` | `#F5F7FA` | Latar area konten. |
| `color-surface` | `#FFFFFF` | Kartu, panel, tabel, dan formulir. |
| `color-sidebar` | `#172B4D` | Latar sidebar. |
| `color-text` | `#172033` | Teks utama. |
| `color-text-muted` | `#657187` | Keterangan dan metadata. |
| `color-border` | `#DCE2EA` | Garis pemisah, border field, border kartu. |
| `color-success` | `#237A4B` | Status berhasil/disetujui. |
| `color-warning` | `#946200` | Status menunggu/peringatan. |
| `color-danger` | `#B42318` | Error, penolakan, dan tindakan berisiko. |
| `color-info` | `#2457A7` | Informasi netral. |

Catatan: JavaFX CSS tidak mendukung semua custom property CSS browser secara identik. Antigravity harus menggunakan sintaks JavaFX CSS yang kompatibel (misalnya class selector dan `-fx-` properties). Bila ingin mendefinisikan nilai bersama, gunakan pendekatan yang benar-benar didukung JavaFX; jangan menyalin sintaks CSS web tanpa pemeriksaan.

### 3.3 Tipografi dan jarak

- Font utama: `System` atau font sans-serif bawaan sistem.
- Judul halaman: 22–26 px.
- Judul section/kartu: 15–18 px.
- Isi tabel/form: 12–14 px.
- Helper text: 11–12 px, tetap terbaca.
- Skala jarak acuan: 4, 8, 12, 16, 20, 24, dan 32 px.
- Radius kartu/panel: sekitar 8–12 px.
- Border tipis dan shadow sangat ringan; jangan memberikan shadow berat pada setiap elemen.

Gunakan ukuran yang konsisten. Jangan mengatur font berbeda-beda per node tanpa alasan.

### 3.4 Status harus memiliki teks

Status tidak boleh hanya ditunjukkan oleh warna atau ikon. Gunakan teks, misalnya:

- `Menunggu`
- `Disetujui`
- `Ditolak`
- `Dipinjam`
- `Dikembalikan`
- `Perlu konfirmasi`
- `Tidak tersedia`

Warna adalah pelengkap, bukan satu-satunya petunjuk.

---

## 4. Ukuran Window dan Kerangka Aplikasi

### 4.1 Ukuran dasar

- Ukuran awal yang disarankan: **1366 × 768 px**.
- Ukuran minimum: **1100 × 680 px**, kecuali keterbatasan layout nyata mengharuskan penyesuaian.
- Window dapat diperbesar dan diperkecil.
- Jangan memaksa ukuran window menjadi fixed jika konten dapat diatur secara fleksibel.
- Tabel yang memiliki banyak kolom harus menyediakan scroll horizontal bila perlu, bukan memotong informasi tanpa penanda.

### 4.2 Layout shell

Gunakan konsep berikut:

```text
┌────────────────────────────────────────────────────────────────────┐
│ Header: nama aplikasi / judul halaman / user / logout              │
├────────────────┬───────────────────────────────────────────────────┤
│                │ Breadcrumb opsional                                │
│ Sidebar        │ Judul halaman                    Aksi utama         │
│ navigasi       ├───────────────────────────────────────────────────┤
│                │ Filter / pencarian                                  │
│                ├───────────────────────────────────────────────────┤
│                │ Konten: kartu, tabel, form, atau hasil pencarian   │
│                │                                                   │
│                │                                                   │
└────────────────┴───────────────────────────────────────────────────┘
```

Konfigurasi baseline:

- Sidebar: lebar sekitar 220–250 px.
- Header: tinggi sekitar 56–68 px.
- Content padding: 20–28 px.
- Gunakan `BorderPane` sebagai shell; sidebar dapat berupa `VBox`, header `HBox`, konten utama `StackPane` atau `BorderPane`.
- Halaman konten boleh memiliki `ScrollPane` jika panjang; jangan membungkus seluruh shell dalam scroll yang membuat sidebar ikut menghilang.

### 4.3 Shell dan navigasi

Sediakan satu shell utama setelah login:

- `AppShell.fxml`
- `AppShellController.java`
- `NavigationService.java` atau pengelola navigasi setara.
- `SessionContext`/`UserSession` untuk menyimpan informasi user yang login selama aplikasi berjalan.

Navigasi harus:

1. Memeriksa role user sebelum membuka halaman.
2. Menandai menu yang aktif.
3. Mengganti konten pusat tanpa membuka Stage baru untuk setiap page.
4. Menangani kegagalan load halaman dengan pesan yang dapat dipahami.
5. Menampilkan dialog konfirmasi ketika logout jika ada form yang belum disimpan, bila mekanisme dirty-form sudah diterapkan.
6. Menghapus sesi login saat logout dan kembali ke halaman login.

Jangan menjadikan menyembunyikan menu sebagai satu-satunya pemeriksaan akses. Lakukan pemeriksaan hak akses juga sebelum memanggil service untuk tindakan terbatas.

---

## 5. Navigasi Berdasarkan Role

### 5.1 Menu User

Susunan menu yang direkomendasikan:

1. Dashboard
2. Jadwal Kampus
3. Ruangan & Ketersediaan
4. Peminjaman Ruangan
5. Inventaris Barang
6. Peminjaman Barang
7. Pengajuan Saya
8. Cari Alternatif Jadwal
9. Profil/Akun bila diperlukan

Menu dapat digabung jika membuat sidebar terlalu panjang, tetapi fungsi inti harus mudah ditemukan. Jangan memasukkan menu admin ke sidebar user.

### 5.2 Menu Admin

1. Dashboard
2. Jadwal Kampus
3. Data Ruangan
4. Data Inventaris
5. Data Akademik
6. Ketersediaan Dosen
7. Manajemen Jadwal
8. Pengajuan Peminjaman Ruangan
9. Pengajuan Peminjaman Barang
10. Serah-Terima & Pengembalian
11. Permintaan Perubahan Jadwal
12. Riwayat & Laporan
13. Manajemen User bila disertakan

Jika menu terlalu panjang, kelompokkan menjadi section berlabel seperti **Data Kampus**, **Peminjaman**, dan **Laporan**. Pertahankan label yang pendek dan jelas.

### 5.3 Identitas menu

Setiap menu perlu memiliki:

- Label teks.
- Ikon opsional yang konsisten, bila tersedia.
- State default, hover, dan active.
- Area klik yang cukup luas.
- Fokus keyboard yang terlihat.

Jangan hanya menggunakan ikon tanpa label pada sidebar versi MVP.

---

## 6. Struktur Folder Front-end

Gunakan struktur ini atau adaptasikan ke struktur repository yang sudah ada. Hindari duplikasi FXML/CSS dengan nama berbeda untuk halaman yang sebenarnya sama.

```text
src/main/java/id/ac/campusfacility/
├── MainApp.java
├── app/
│   ├── AppContext.java
│   └── NavigationService.java
├── session/
│   └── UserSession.java
├── controller/
│   ├── auth/
│   │   └── LoginController.java
│   ├── shell/
│   │   └── AppShellController.java
│   ├── user/
│   │   ├── UserDashboardController.java
│   │   ├── ScheduleBrowserController.java
│   │   ├── RoomAvailabilityController.java
│   │   ├── RoomLoanFormController.java
│   │   ├── InventoryCatalogController.java
│   │   ├── ItemLoanFormController.java
│   │   ├── MyRequestsController.java
│   │   └── AlternativeScheduleController.java
│   └── admin/
│       ├── AdminDashboardController.java
│       ├── RoomManagementController.java
│       ├── InventoryManagementController.java
│       ├── AcademicDataController.java
│       ├── LecturerAvailabilityController.java
│       ├── ScheduleManagementController.java
│       ├── RoomLoanApprovalController.java
│       ├── ItemLoanApprovalController.java
│       ├── ItemReturnController.java
│       ├── ScheduleChangeApprovalController.java
│       └── ReportsController.java
│
└── resources/
    ├── fxml/
    │   ├── auth/login.fxml
    │   ├── shell/app-shell.fxml
    │   ├── common/
    │   ├── user/
    │   └── admin/
    ├── css/
    │   └── app.css
    └── images/
        └── (aset logo/ikon lokal jika tersedia)
```

### 6.1 Aturan struktur

- Satu FXML untuk satu halaman utama atau satu komponen reusable yang cukup kompleks.
- Controller memiliki tanggung jawab satu halaman atau satu dialog, bukan seluruh aplikasi.
- Komponen kecil seperti status label boleh dibuat melalui helper/factory bila mengurangi duplikasi.
- Jangan menaruh semua FXML di root `resources` jika folder terpisah membuat proyek lebih jelas.
- Path resource harus diperiksa saat startup/build; jangan menggunakan path lokal absolut seperti `C:\Users\...`.
- Hindari membuat FXML programmatically yang panjang jika FXML lebih mudah dibaca; sebaliknya, jangan membuat file FXML terpisah untuk setiap label sederhana.

---

## 7. Komponen UI Reusable

Buat atau standardisasi komponen berikut hanya jika dipakai oleh beberapa halaman. Tujuannya konsistensi, bukan menambah abstraksi yang tidak dibutuhkan.

### 7.1 Komponen umum

| Komponen | Fungsi | Perilaku yang diharapkan |
|---|---|---|
| `PageHeader` | Judul, deskripsi singkat, dan tombol aksi utama | Judul konsisten dan tombol utama diletakkan di posisi yang mudah ditemukan. |
| `SearchField` | Pencarian teks pada tabel/list | Placeholder spesifik; tombol clear opsional; filter tidak menghapus data asli. |
| `StatusBadge` | Label status pengajuan | Menampilkan label teks dan class gaya sesuai status. |
| `EmptyState` | Memberi tahu ketika data kosong | Jelaskan mengapa kosong dan aksi berikutnya bila relevan. |
| `LoadingState` | Indikator loading | Mencegah user mengira aplikasi berhenti merespons. |
| `ErrorState` | Menampilkan error yang bisa dipahami | Tampilkan pesan aman dan tombol coba lagi bila operasi dapat diulang. |
| `FormField` | Label, kontrol input, helper/error text | Label selalu terlihat; pesan error berada dekat field terkait. |
| `ConfirmDialog` | Konfirmasi aksi penting | Menyebut aksi dan objek yang akan terdampak secara spesifik. |
| `InfoCard` | Angka ringkasan pada dashboard | Judul metrik dan nilainya jelas; jangan menampilkan angka dummy di aplikasi yang digunakan. |
| `FilterBar` | Mengelompokkan pencarian/filter tabel | Filter yang diterapkan terlihat dan mudah direset. |

### 7.2 Tombol

Standardisasi class CSS, misalnya:

- `.button-primary` — aksi utama pada halaman.
- `.button-secondary` — aksi alternatif.
- `.button-danger` — penolakan, penghapusan, atau pembatalan yang berisiko.
- `.button-quiet` — aksi tambahan dengan prioritas rendah.

Aturan:

- Setiap halaman idealnya hanya memiliki satu aksi visual utama.
- Gunakan kata kerja yang konkret: `Simpan`, `Ajukan Peminjaman`, `Cari Alternatif`, `Setujui`, `Tolak`, `Catat Pengembalian`.
- Jangan menggunakan label generik `OK` atau `Submit` jika label aksi spesifik memungkinkan.
- Tombol yang menjalankan operasi harus memiliki disabled/loading state ketika proses berlangsung.
- Tombol berbahaya tidak boleh berdekatan tanpa jarak yang cukup dengan tombol utama.

### 7.3 Tabel

Setiap `TableView` harus memiliki:

- Kolom berlabel jelas.
- Sort yang masuk akal untuk kolom terkait.
- Format tanggal/waktu konsisten.
- Tampilan ketika hasil filter kosong.
- Penanganan ketika data gagal dimuat.
- Pilihan row action yang jelas, tidak bergantung pada klik ganda tanpa petunjuk.
- Konfirmasi untuk tindakan berdampak tinggi.
- Scroll yang tetap menjaga header kolom terlihat sesuai perilaku JavaFX.

Jangan membuat satu `TableView` dengan terlalu banyak kolom lalu membuat semua kolom sangat sempit. Prioritaskan kolom penting dan sediakan halaman/detail view untuk informasi tambahan.

---

## 8. Spesifikasi Halaman per Fitur

## 8.1 Login

**FXML:** `fxml/auth/login.fxml`  
**Controller:** `LoginController.java`

### Elemen

- Nama/logo aplikasi jika aset tersedia.
- Judul `Campus Facility & Schedule Manager` atau nama final yang disepakati.
- Field username/email.
- Field password.
- Tombol `Masuk`.
- Area pesan validasi/error.
- Indikator loading saat autentikasi dilakukan.

### Perilaku

- Field wajib diisi.
- Tekan Enter pada password boleh menjalankan login.
- Password tidak ditampilkan dalam teks biasa.
- Error kredensial ditampilkan dengan pesan umum yang tidak membocorkan apakah username atau password tertentu valid.
- Login gagal tidak boleh mengosongkan username kecuali ada alasan UX yang jelas.
- Setelah login sukses, buka shell yang sesuai role.
- Jangan menyimpan password ke file, log, atau preferensi UI.

### Acceptance criteria

- [ ] Field username dan password tersedia.
- [ ] Field kosong diberi pesan yang jelas.
- [ ] Login sukses mengarahkan ke shell sesuai role.
- [ ] Login gagal menampilkan pesan aman.
- [ ] Akun nonaktif tidak bisa masuk.
- [ ] Tidak ada password hard-coded pada source code.

## 8.2 Shell dan Dashboard User

**FXML:** `fxml/shell/app-shell.fxml`, `fxml/user/user-dashboard.fxml`

### Dashboard User

Isi yang disarankan:

- Sapaan singkat yang menggunakan nama tampilan jika tersedia.
- Ringkasan pengajuan: menunggu, disetujui/aktif, dan ditolak atau selesai sesuai data aktual.
- Shortcut `Ajukan Peminjaman Ruangan`, `Pinjam Barang`, `Cari Alternatif Jadwal`.
- Daftar pengajuan terakhir milik user tersebut.
- Pengingat atau notifikasi dalam aplikasi bila ada data nyata.

### Aturan

- Jangan menampilkan angka/aktivitas contoh sebagai data produksi.
- Jika belum ada pengajuan, tampilkan empty state dan tautan aksi yang relevan.
- Data dashboard harus berasal dari service dengan filter user yang sedang login.
- Tombol shortcut harus membuka halaman/form yang tepat.

## 8.3 Dashboard Admin

**FXML:** `fxml/admin/admin-dashboard.fxml`

### Elemen

- Kartu jumlah ruangan aktif.
- Kartu pengajuan peminjaman menunggu.
- Kartu pengajuan perubahan jadwal menunggu.
- Kartu inventaris atau pengingat pengembalian sesuai data yang tersedia.
- Bagian `Perlu Ditindaklanjuti` berisi daftar pengajuan prioritas.
- Shortcut ke manajemen jadwal/ruangan/inventaris.

### Aturan

- Angka harus berasal dari service/repository, tidak ditulis tetap di controller.
- Klik pada kartu atau daftar dapat membawa admin ke halaman dengan filter sesuai konteks jika implementasinya tersedia.
- Jika tak ada yang perlu ditindaklanjuti, tampilkan pesan positif yang sederhana, bukan tabel kosong tanpa penjelasan.

## 8.4 Jadwal Kampus

**FXML:** `fxml/user/schedule-browser.fxml` atau FXML bersama yang dapat dipakai Admin/User dengan hak akses berbeda.

### Layout

- Judul dan deskripsi singkat.
- Filter semester aktif.
- Filter hari/tanggal, ruangan, mata kuliah/kelompok kelas, dan dosen sesuai data yang tersedia.
- Kolom pencarian teks opsional.
- Tabel jadwal.

### Kolom minimal

- Hari.
- Jam mulai.
- Jam selesai/durasi.
- Mata kuliah.
- Kelompok kelas.
- Dosen.
- Ruangan.

### Aturan

- Urutkan secara default berdasarkan hari, jam mulai, kemudian ruangan.
- Jangan menyatakan jadwal kosong sebagai bukti seluruh fasilitas tersedia jika data belum lengkap.
- Admin mendapatkan aksi tambah/edit/nonaktifkan; User hanya melihat data yang diizinkan.
- Validasi bentrok dilakukan oleh service, bukan semata-mata filter UI.

## 8.5 Manajemen Ruangan (Admin) dan Ketersediaan Ruangan (User)

**FXML Admin:** `fxml/admin/room-management.fxml`  
**FXML User:** `fxml/user/room-availability.fxml`

### Admin: kolom tabel

- Kode ruangan.
- Nama ruangan.
- Gedung/lokasi.
- Kapasitas.
- Fasilitas ringkas.
- Status aktif.
- Aksi lihat/edit/nonaktifkan.

### Form tambah/edit

- Kode ruangan (wajib dan unik).
- Nama ruangan (wajib).
- Gedung/lokasi.
- Kapasitas (bilangan bulat positif).
- Fasilitas/keterangan.
- Status aktif/nonaktif.

### User: pencarian ketersediaan

- Tanggal.
- Waktu mulai.
- Waktu selesai.
- Jumlah peserta.
- Tombol `Cari Ruangan`.
- Hasil berupa daftar ruangan yang cocok.
- Keterangan waktu bentrok atau kapasitas tidak mencukupi bila relevan.

### Aturan

- Jam selesai harus lebih besar dari jam mulai.
- Kapasitas harus bilangan bulat positif.
- Ruangan nonaktif tidak dapat dipilih untuk pengajuan baru.
- Front-end boleh melakukan pre-check untuk mempercepat umpan balik, tetapi hasil final tetap dari backend.
- Jika proses pengecekan gagal, jangan tampilkan hasil sebelumnya seolah-olah merupakan hasil pencarian terbaru.

## 8.6 Manajemen Inventaris dan Katalog Barang

**FXML Admin:** `fxml/admin/inventory-management.fxml`  
**FXML User:** `fxml/user/inventory-catalog.fxml`

### Kolom tampilan

- Nama/kode barang.
- Deskripsi singkat.
- Total unit.
- Unit tersedia bila service menyediakan hasil yang akurat.
- Kondisi/status.
- Aksi yang sesuai role.

### Form Admin

- Kode barang bila sistem memerlukan kode unik.
- Nama barang.
- Deskripsi.
- Jumlah total unit.
- Kondisi umum.
- Status aktif.

### Aturan

- Kuantitas tidak boleh negatif.
- Data yang dipinjam atau sedang dalam transaksi tidak boleh dihapus permanen secara sembarangan; gunakan nonaktifkan bila model data memerlukan riwayat.
- User tidak melihat tombol edit/hapus inventaris.
- Ketersediaan harus mengikuti transaksi aktif yang disetujui, bukan hanya `total unit`.

## 8.7 Form Peminjaman Ruangan

**FXML:** `fxml/user/room-loan-form.fxml`

### Field

- Tanggal penggunaan.
- Waktu mulai.
- Waktu selesai.
- Jumlah peserta.
- Ruangan yang dipilih atau aksi untuk mencari ruangan tersedia.
- Tujuan/keterangan peminjaman.
- Tombol `Periksa Ketersediaan` jika diperlukan terpisah.
- Tombol `Ajukan Peminjaman`.
- Tombol batal/kembali.

### Perilaku

1. User mengisi tanggal, waktu, dan jumlah peserta.
2. User memeriksa ruangan yang tersedia.
3. Sistem menampilkan hasil ketersediaan terbaru.
4. User memilih ruangan dan mengisi tujuan.
5. Front-end memvalidasi field wajib.
6. Service melakukan validasi final dan menyimpan pengajuan sebagai status menunggu.
7. UI menampilkan keberhasilan beserta nomor/identitas pengajuan jika ada dan membuka detail atau daftar pengajuan.

### Validasi UI

- Tanggal penggunaan tidak boleh lewat untuk pengajuan baru.
- Waktu mulai dan selesai wajib diisi.
- Waktu selesai harus setelah waktu mulai.
- Jumlah peserta harus lebih dari nol.
- Ruangan harus dipilih dari hasil pencarian terbaru.
- Tujuan harus diisi sesuai batas panjang yang ditetapkan aplikasi.

### Ketentuan penting

- Jangan langsung menyatakan ruangan `Disetujui` saat user baru mengirim pengajuan.
- Jangan mengunci availability hanya dari hasil tampilan; service wajib memeriksa ulang saat submit dan saat admin menyetujui.
- Jika ruangan tidak lagi tersedia, jelaskan bahwa data berubah dan minta user mencari ulang.

## 8.8 Form Peminjaman Barang

**FXML:** `fxml/user/item-loan-form.fxml`

### Field

- Barang yang diminta.
- Jumlah unit.
- Tanggal pinjam.
- Perkiraan tanggal/waktu pengembalian sesuai model aplikasi.
- Tujuan.
- Tombol `Ajukan Peminjaman`.

### Validasi UI

- Barang harus aktif dan dipilih dari katalog.
- Jumlah unit harus bilangan bulat lebih besar dari nol.
- Jumlah tidak boleh lebih besar dari jumlah tersedia yang terakhir diketahui.
- Tanggal kembali harus setelah tanggal pinjam jika kedua field ada.
- Tujuan wajib diisi.

### Perilaku

- Tampilkan ringkasan barang, jumlah, waktu pinjam, dan waktu rencana kembali sebelum submit bila dapat dilakukan tanpa membuat alur terlalu panjang.
- Jika backend menyatakan stok sudah berubah, tampilkan pesan spesifik dan segarkan ketersediaan.
- Pengajuan baru berstatus menunggu, bukan langsung dipinjam.

## 8.9 Pengajuan Saya

**FXML:** `fxml/user/my-requests.fxml`

### Tampilan

Gunakan tab atau filter untuk:

- Semua.
- Peminjaman ruangan.
- Peminjaman barang.
- Perubahan jadwal.

Kolom tabel dasar:

- Nomor pengajuan.
- Jenis pengajuan.
- Tanggal dibuat.
- Ringkasan permintaan.
- Status.
- Aksi `Lihat Detail`.

Detail menampilkan:

- Semua data utama pengajuan.
- Status terkini.
- Waktu pengajuan/perubahan status jika tersedia.
- Alasan penolakan apabila ditolak.
- Informasi pengembalian untuk transaksi barang bila relevan.

### Aturan

- Data yang ditampilkan harus terbatas pada user aktif.
- Label status harus jelas dan konsisten dengan enum/status backend.
- Jangan mengizinkan pembatalan jika status atau aturan bisnis tidak mendukungnya.
- Jika pembatalan disediakan, minta konfirmasi dan tampilkan alasan bila diperlukan.

## 8.10 Cari Alternatif Jadwal

**FXML:** `fxml/user/alternative-schedule.fxml`

Halaman ini adalah salah satu fitur terpenting; UI harus membedakan antara **preferensi** dengan **syarat wajib**.

### Input pencarian

- Semester aktif.
- Mata kuliah/jadwal yang akan dipindahkan.
- Kelompok kelas.
- Dosen terkait, ditampilkan dari data jadwal dan tidak boleh diubah sembarangan oleh user.
- Durasi kelas yang dibaca dari jadwal awal dan dipertahankan.
- Hari yang diinginkan (preferensi).
- Rentang waktu yang diinginkan (preferensi).
- Ruangan/kapasitas bila ada kebutuhan khusus yang memang diperbolehkan.
- Tombol `Cari Alternatif`.

### Ringkasan sebelum pencarian

Tampilkan jadwal awal dengan jelas:

- Mata kuliah dan kelas.
- Dosen.
- Hari/waktu lama.
- Ruangan lama.
- Durasi.

### Hasil rekomendasi

Gunakan tabel atau kartu ringkas. Kolom minimal:

- Hari dan tanggal efektif jika relevan dengan model jadwal.
- Waktu mulai–selesai.
- Ruangan.
- Kesesuaian preferensi (misalnya `Sesuai preferensi` / `Alternatif lain`).
- Indikator validasi.
- Tombol `Ajukan Perubahan`.

Tampilkan alasan yang membantu, misalnya `Memenuhi durasi dan jadwal dosen` atau `Tidak sesuai preferensi waktu tetapi valid`. **Hasil yang tidak lolos hard constraint tidak boleh ditampilkan sebagai rekomendasi yang dapat dipilih.** Jika UI menyediakan panel kandidat yang ditolak untuk transparansi, tampilkan terpisah dan beri label jelas `Tidak valid`, tanpa tombol ajukan.

### Aturan pencarian dan pengajuan

- Durasi harus sama dengan jadwal awal.
- Jadwal kelas lain tidak boleh bentrok.
- Dosen harus tersedia berdasarkan data admin.
- Ruangan harus tersedia dan kapasitasnya memadai bila jumlah peserta diketahui.
- Peminjaman ruangan yang disetujui harus ikut diperiksa.
- Preferensi hari/waktu digunakan untuk mengurutkan kandidat valid, bukan untuk melanggar hard constraint.
- Jika data ketersediaan dosen belum ada, UI harus menunjukkan `Perlu konfirmasi` atau penjelasan yang dikembalikan service; jangan mengklaim jadwal terkonfirmasi.
- Saat user memilih kandidat, tampilkan ringkasan jadwal awal dan usulan baru sebelum user menekan konfirmasi.
- Permintaan yang dikirim menunggu persetujuan; jadwal resmi tidak berubah sebelum disetujui admin.
- Saat admin menyetujui, backend harus mengulang validasi karena keadaan dapat berubah sesudah pencarian.

### Empty/error states

- Tidak ada hasil valid: jelaskan bahwa belum ditemukan alternatif yang memenuhi semua syarat, serta sarankan mengubah preferensi lunak jika memungkinkan.
- Gagal memuat data: tampilkan pesan dan tombol coba lagi.
- Data jadwal awal tidak lengkap: jelaskan bagian yang harus dilengkapi Admin.
- Hasil lama tidak boleh tertinggal sebagai seolah-olah hasil pencarian terbaru setelah pencarian gagal.

## 8.11 Halaman Persetujuan Admin

Halaman ini dapat dipisah menurut jenis permintaan agar operasionalnya jelas.

### Persetujuan peminjaman ruangan/barang

- Tabel pengajuan menunggu.
- Filter status dan tanggal.
- Detail pengajuan.
- Panel hasil validasi terbaru.
- Tombol `Setujui` dan `Tolak`.
- Field alasan penolakan yang wajib jika permintaan ditolak.

Saat menekan `Setujui`, sistem harus:

1. Menampilkan detail objek/ruangan/barang dan waktu yang akan disetujui.
2. Meminta konfirmasi.
3. Memanggil service approval.
4. Menampilkan hasil sukses atau konflik terbaru dari backend.
5. Memperbarui daftar setelah operasi selesai.

### Persetujuan perubahan jadwal

- Tampilkan jadwal lama dan usulan baru secara berdampingan atau dalam dua section yang mudah dibandingkan.
- Tampilkan alasan pemohon.
- Tampilkan hasil validasi bentrok terbaru.
- Tombol setuju/tolak dengan konfirmasi.
- Alasan penolakan harus disimpan jika model bisnis mensyaratkannya.
- Jangan menyetujui berdasarkan hasil pencarian lama tanpa memvalidasi ulang.

## 8.12 Data Akademik, Ketersediaan Dosen, dan Manajemen Jadwal

Untuk MVP, form admin harus menyediakan input yang jelas dan membatasi nilai yang tidak valid.

### Data akademik

Entitas yang sesuai dengan model data: semester aktif, mata kuliah, kelompok kelas, dan dosen. Gunakan `ComboBox` untuk memilih relasi yang sudah ada; jangan meminta admin mengetik nama relasi bebas jika pilihan terdaftar tersedia.

### Ketersediaan dosen

- Pilih dosen.
- Pilih hari.
- Masukkan waktu mulai dan selesai.
- Tampilkan daftar rentang waktu tersimpan.
- Cegah atau jelaskan rentang waktu yang terbalik/duplikat/bentrok menurut aturan service.
- Beri penjelasan bahwa data ini digunakan untuk validasi rekomendasi alternatif jadwal.

### Manajemen jadwal

- Pilih semester aktif.
- Pilih mata kuliah, kelompok kelas, dosen, dan ruangan.
- Pilih hari dan jam mulai/selesai.
- Tampilkan durasi yang dihitung.
- Saat simpan, tampilkan kesalahan konflik dari service pada konteks form.
- Jadwal yang telah memiliki transaksi terkait tidak boleh dihapus begitu saja jika dapat merusak riwayat; gunakan deaktivasi/perubahan status sesuai model data.

## 8.13 Riwayat dan Laporan

MVP cukup menyediakan tabel dengan filter praktis; jangan membangun chart kompleks kecuali diminta.

Filter yang disarankan:

- Rentang tanggal.
- Jenis transaksi.
- Status.
- Ruangan/barang.
- Pemohon (khusus admin).

Pastikan laporan menampilkan data nyata dari service dan tidak mencampur transaksi yang tidak berhubungan. Ekspor ke PDF/Excel berada di luar MVP kecuali disepakati kemudian.

---

## 9. Aturan Implementasi FXML dan Controller

### 9.1 Prinsip pemisahan

- FXML menentukan struktur/layout node.
- CSS mengatur tampilan.
- Controller menghubungkan event UI dan data tampilan ke service.
- Service mengatur aturan bisnis.
- Repository mengakses database.

Controller **tidak boleh** menjalankan SQL atau mengakses file database secara langsung.

### 9.2 Injeksi dependency/controller factory

Gunakan mekanisme yang konsisten untuk memberikan service kepada controller, misalnya `FXMLLoader.setControllerFactory(...)` yang mengambil dependency dari `AppContext`. Jangan membuat service/repository/database baru di setiap controller jika seharusnya menggunakan dependency yang sama.

Jika repository belum memakai dependency injection framework, jangan menambah framework baru hanya untuk ini. Factory sederhana buatan proyek sudah cukup.

### 9.3 Event dan lifecycle

- Gunakan `initialize()` untuk konfigurasi kontrol yang memang perlu dilakukan setelah FXML diload.
- Hindari melakukan query lambat langsung di `initialize()` pada JavaFX Application Thread.
- Pisahkan method event yang jelas, misalnya `handleSearch()`, `handleSave()`, dan `handleLogout()`.
- Jangan menaruh logika yang sama pada banyak handler; pindahkan bagian yang benar-benar reusable ke helper/service.
- Lepas listener/binding yang tidak lagi diperlukan jika lifecycle halaman membuat listener berulang dan membocorkan referensi.

### 9.4 Data binding

- Gunakan `ObservableList` untuk sumber data `TableView`.
- Gunakan `FilteredList`/`SortedList` saat pencarian dan sort lokal cocok untuk jumlah data MVP.
- Untuk daftar besar, filter di service/repository; jangan memuat seluruh data tanpa batas.
- Saat filter berubah, hasil tampilan harus konsisten dengan filter yang sedang terlihat.
- Gunakan formatter tanggal/waktu tunggal untuk tampilan agar format antartabel sama.

### 9.5 Penanganan operasi asynchronous

Gunakan `Task<T>`/`Service<T>` untuk operasi yang dapat memakan waktu, terutama login yang melibatkan hashing, pencarian jadwal, pembacaan banyak data, dan submit yang memerlukan beberapa validasi.

Pola umum:

1. Validasi field sederhana pada UI.
2. Nonaktifkan tombol submit dan tampilkan loading state.
3. Jalankan service di background thread yang sesuai.
4. Setelah selesai, ubah komponen UI di JavaFX Application Thread.
5. Tampilkan hasil sukses/error.
6. Aktifkan kembali tombol dengan aman.

Jangan memakai `Thread.sleep()` pada UI thread. Jangan menjalankan operasi UI dari thread background.

---

## 10. Standar Form dan Validasi Front-end

### 10.1 Pola field

Setiap field penting harus memiliki:

- Label permanen di atas/sebelah kontrol.
- Tanda wajib bila field diperlukan.
- Placeholder hanya sebagai contoh, bukan pengganti label.
- Pesan validasi dekat field.
- Format atau batasan yang dijelaskan jika tidak jelas dari jenis kontrol.

### 10.2 Waktu dan tanggal

- Gunakan `DatePicker` untuk tanggal.
- Gunakan kontrol waktu yang konsisten. Jika menggunakan `ComboBox` daftar slot waktu, tetapkan interval secara konsisten dan pastikan backend tetap menerima/validasi waktu yang benar. Jika menggunakan `TextField`, parser harus menolak format salah dan memberi contoh `HH:mm`.
- Gunakan format tampilan `dd MMM yyyy` dan `HH:mm` atau format lokal Indonesia yang konsisten.
- Untuk rentang waktu, validasi bahwa akhir sesudah mulai.
- Durasi jadwal alternatif dihitung dari data jadwal asal; user tidak boleh mengubah durasi pada form pencarian.

### 10.3 Pesan error

Pesan sebaiknya menyebut apa yang salah dan cara memperbaiki, contohnya:

- `Waktu selesai harus setelah waktu mulai.`
- `Jumlah peserta harus lebih dari 0.`
- `Ruangan ini baru saja tidak tersedia. Cari ulang untuk melihat pilihan terbaru.`
- `Jadwal tidak dapat disetujui karena dosen memiliki jadwal lain pada waktu tersebut.`

Hindari pesan yang hanya berbunyi `Error`, `Exception occurred`, `Invalid input`, atau menampilkan stack trace kepada pengguna.

### 10.4 Pembedaan validasi UI dan backend

Validasi front-end digunakan untuk mempercepat umpan balik, tetapi tidak dianggap sumber kebenaran akhir. Backend/service tetap wajib memvalidasi ulang:

- Hak akses.
- Bentrok waktu.
- Ketersediaan ruangan.
- Ketersediaan dosen.
- Kapasitas ruangan jika relevan.
- Ketersediaan inventaris.
- Status transaksi dan pengajuan.
- Kondisi data saat admin menyetujui permintaan.

Jika validasi backend berbeda dengan hasil pre-check UI, backend menjadi sumber kebenaran dan UI wajib menampilkan pesan yang sesuai.

---

## 11. State yang Wajib Ada pada Halaman Data

Setiap halaman yang memuat data dinamis minimal memiliki state berikut:

### 11.1 Loading

- Menampilkan indikator loading dan teks seperti `Memuat data...`.
- Nonaktifkan aksi yang berpotensi membuat data tidak konsisten.
- Hindari menampilkan tabel lama tanpa penanda seolah-olah hasilnya terbaru.

### 11.2 Success/data tersedia

- Tampilkan data.
- Tampilkan filter aktif.
- Tampilkan aksi yang diizinkan untuk role.

### 11.3 Empty state

Contoh:

- `Belum ada data ruangan.`
- `Belum ada pengajuan untuk filter ini.`
- `Tidak ditemukan jadwal alternatif yang memenuhi syarat.`

Sertakan aksi yang relevan seperti `Tambah Ruangan` (admin) atau `Reset Filter` bila sesuai.

### 11.4 Error state

- Tampilkan pesan sederhana dan aman.
- Sediakan `Coba Lagi` jika operasi dapat diulang.
- Simpan detail teknis ke logging yang sesuai, bukan dialog user.

### 11.5 Submitting/saving

- Tombol berubah atau memiliki indikator proses.
- Cegah klik ganda untuk operasi yang sama.
- Setelah gagal, pertahankan isian yang masih valid sejauh memungkinkan.
- Setelah sukses, segarkan data dan tampilkan konfirmasi.

### 11.6 Stale data/conflict state

Untuk ketersediaan dan approval, jika backend menginformasikan data berubah sejak terakhir dimuat:

- Beri penjelasan.
- Segarkan data.
- Jangan menganggap status lama masih valid.
- Jangan mengulangi submit secara otomatis tanpa sepengetahuan user.

---

## 12. Aksesibilitas dan Kemudahan Penggunaan

- Semua tombol dan field penting memiliki label yang eksplisit.
- Urutan tab mengikuti alur membaca form.
- Fokus keyboard terlihat jelas.
- Tombol tidak hanya diwakili ikon.
- Pesan error terasosiasi secara visual dengan field yang bermasalah.
- Teks penting tidak terlalu kecil.
- Kontras teks/latar cukup untuk dibaca.
- Informasi status memiliki label tertulis, bukan warna saja.
- Jangan menggunakan tooltip sebagai satu-satunya cara menjelaskan fungsi inti.
- Berikan ruang klik yang memadai, terutama untuk aksi tabel.
- Form tetap bisa digunakan tanpa mouse untuk alur utama bila kontrol JavaFX yang dipilih mendukungnya.

---

## 13. Pengelolaan Tema dan CSS

**File utama:** `src/main/resources/css/app.css`

Sediakan class CSS yang konsisten, contohnya:

- `.app-root`
- `.app-header`
- `.app-sidebar`
- `.nav-item`
- `.nav-item-active`
- `.page-container`
- `.page-title`
- `.page-description`
- `.surface-card`
- `.section-title`
- `.field-label`
- `.field-error`
- `.button-primary`
- `.button-secondary`
- `.button-danger`
- `.status-pending`
- `.status-approved`
- `.status-rejected`
- `.status-completed`
- `.table-view`
- `.empty-state`
- `.loading-state`

Aturan CSS:

1. Gunakan stylesheet utama untuk gaya lintas halaman.
2. Hindari inline style pada FXML/Java kecuali untuk nilai dinamis yang memang perlu.
3. Hindari copy-paste blok styling yang sama ke banyak halaman.
4. Jangan mengubah tampilan kontrol global secara ekstrem sampai komponen native kehilangan affordance-nya.
5. Pastikan hover, focus, disabled, dan selected state tidak saling membingungkan.
6. Terapkan class status yang sesuai berdasarkan status data aktual.
7. Jangan menambahkan file stylesheet duplikat tanpa alasan yang jelas.

---

## 14. Kontrak Data antara Front-end dan Backend

Front-end tidak boleh menentukan sendiri status resmi transaksi. Gunakan model/DTO/enums yang dibagikan bersama layer backend.

### 14.1 Status

Gunakan enum atau konstanta bersama untuk status yang disepakati di `IMPLEMENTATION_PLAN.md`. Jangan menulis nilai status yang tidak konsisten di berbagai controller.

Contoh konsep status yang mungkin digunakan (ikuti enum/domain model final yang benar-benar dibuat):

- Peminjaman: menunggu, disetujui, ditolak, dipinjam, dikembalikan, dibatalkan jika aturan bisnis mendukungnya.
- Permintaan perubahan jadwal: menunggu, disetujui, ditolak.

Jangan membuat status tambahan hanya untuk mempermudah tampilan tanpa dukungan model bisnis.

### 14.2 Format tanggal dan waktu

- Objek Java yang diutamakan: `LocalDate`, `LocalTime`, `DayOfWeek`, `Duration`.
- Format tampilan dapat berbeda dari format penyimpanan.
- Gunakan formatter helper bersama, misalnya `DateTimeFormatters`, bukan membuat pola format berbeda pada setiap halaman.

### 14.3 Model tampilan

Bila satu row tabel menggabungkan data dari beberapa entitas, gunakan DTO/view model, misalnya `LoanRequestRow`, `ScheduleRow`, atau `AlternativeScheduleOption`. Hindari query atau pemanggilan repository dari cell/table callback.

---

## 15. Pembagian Pekerjaan Front-end untuk Dua Orang

Komposisi tugas keseluruhan tim: **Orang 1 = 25% FE + 25% BE; Orang 2 = 25% FE + 25% BE**. Dokumen ini hanya merinci pembagian FE. Backend dan aturan bisnis tetap mengikuti `IMPLEMENTATION_PLAN.md`.

### 15.1 Orang 1 — Front-end manajemen fasilitas

**Halaman utama:**

- Login dan shell UI (kolaborasi dengan Orang 2 untuk integrasi role/navigasi).
- Dashboard Admin dan Dashboard User.
- Jadwal Kampus.
- Manajemen Ruangan untuk Admin.
- Ketersediaan Ruangan untuk User.
- Manajemen Inventaris untuk Admin.
- Katalog Inventaris untuk User.
- Komponen reusable: page header, filter bar, status badge, empty/loading/error state.

**Deliverables:**

- FXML halaman yang menjadi tanggung jawabnya.
- Controller untuk event UI dan pemanggilan service.
- CSS sesuai design token.
- Tabel, filter, form dan dialog yang dapat digunakan.
- Screenshot atau bukti pengujian manual untuk alur halaman tersebut.

**Bukan tanggung jawab FE Orang 1:** menulis SQL dari controller, menentukan status bisnis baru, atau membuat aturan approval sendiri di UI.

### 15.2 Orang 2 — Front-end transaksi dan pencarian jadwal

**Halaman utama:**

- Form peminjaman ruangan.
- Form peminjaman barang.
- Pengajuan Saya dan detail pengajuan.
- Cari Alternatif Jadwal.
- Halaman admin untuk persetujuan peminjaman.
- Serah-terima/pengembalian barang.
- Persetujuan perubahan jadwal.
- Tampilan empty/error/conflict/confirmation yang khusus pada proses transaksi.

**Deliverables:**

- FXML halaman transaksi dan pencarian alternatif.
- Controller untuk interaksi dan pemanggilan service.
- Komponen ringkasan jadwal awal/usulan baru.
- Form validasi UI dan konfirmasi sebelum pengajuan/approval.
- Bukti pengujian manual untuk keberhasilan, penolakan, konflik, dan empty result.

**Bukan tanggung jawab FE Orang 2:** menghitung ketersediaan final di controller atau memutuskan approval langsung di UI.

### 15.3 Pekerjaan bersama

- Menyetujui nama dan struktur CSS.
- Menyetujui shell utama dan pola navigasi.
- Menyetujui kontrak controller-service dan DTO yang ditampilkan.
- Mengintegrasikan branch/kode masing-masing.
- Menguji aplikasi pada satu build yang sama.
- Meninjau konsistensi label, warna status, format tanggal, dan pola dialog.

### 15.4 Pembagian yang tidak boleh dilakukan

Jangan memisahkan proyek dengan cara membuat dua aplikasi yang tidak terintegrasi. Kedua anggota harus mengerjakan halaman di satu codebase dan mengikuti komponen serta pola desain bersama.

---

## 16. Urutan Implementasi Front-end

Antigravity harus mengikuti urutan ini agar fondasi konsisten sebelum halaman kompleks dibuat.

### FE-0 — Audit repository

- Baca `IMPLEMENTATION_PLAN.md` dan dokumen ini.
- Periksa `pom.xml`, versi Java/JavaFX, package, struktur FXML, serta stylesheet yang sudah ada.
- Catat komponen yang sudah dapat digunakan kembali.
- Jangan menghapus atau mengganti kode yang berfungsi tanpa alasan.

**Selesai jika:** struktur existing dipahami, keputusan teknis tercatat, dan tidak ada duplikasi stack.

### FE-1 — Aplikasi dasar dan shell

- Pastikan `MainApp` dapat membuka JavaFX Stage.
- Buat/rapikan login layout.
- Buat shell dengan header, sidebar, content container, dan stylesheet dasar.
- Implementasikan navigasi placeholder hanya sementara pada tahap ini; tandai jelas bahwa placeholder akan diganti.
- Tetapkan minimum window size.

**Selesai jika:** aplikasi dapat diluncurkan dan berpindah ke shell tanpa error resource/FXML.

### FE-2 — Design system dan komponen umum

- Tambahkan token warna, tipografi, spacing, tombol, tabel, dan status.
- Buat helper reusable yang memang dipakai lintas halaman.
- Buat pola loading, empty state, error state, dan confirmation dialog.

**Selesai jika:** dua halaman yang berbeda dapat menggunakan gaya yang sama tanpa copy-paste CSS berulang.

### FE-3 — Login dan role-based shell

- Integrasikan login dengan service autentikasi.
- Terapkan session context.
- Render menu berdasarkan role.
- Terapkan logout.

**Selesai jika:** Admin dan User melihat menu yang sesuai, sesi kosong tidak dapat memasuki halaman privat, dan logout kembali ke login.

### FE-4 — Halaman data dasar

- Implementasikan dashboard dengan data service.
- Implementasikan jadwal kampus.
- Implementasikan manajemen ruangan/ketersediaan.
- Implementasikan inventaris/katalog.

**Selesai jika:** tiap halaman dapat loading, menampilkan data, memfilter jika tersedia, dan menangani data kosong/error.

### FE-5 — Form transaksi

- Implementasikan form peminjaman ruangan.
- Implementasikan form peminjaman barang.
- Implementasikan daftar/detail pengajuan.
- Tampilkan validasi, loading submit, dan konfirmasi sukses.

**Selesai jika:** alur submit terhubung ke service dan tidak sekadar membuat pesan sukses palsu.

### FE-6 — Pencarian alternatif jadwal

- Implementasikan pemilihan jadwal awal.
- Tampilkan preferensi sebagai preferensi, bukan persyaratan wajib.
- Tampilkan rekomendasi yang dikembalikan service.
- Implementasikan konfirmasi sebelum mengajukan perubahan.
- Tampilkan kondisi tidak ada alternatif dan data tidak lengkap.

**Selesai jika:** UI tidak membuat kandidat valid sendiri dan hanya memberi aksi pengajuan pada kandidat yang diizinkan service.

### FE-7 — UI proses admin

- Implementasikan persetujuan peminjaman.
- Implementasikan serah-terima/pengembalian barang.
- Implementasikan persetujuan perubahan jadwal.
- Implementasikan validasi error terbaru dan refresh data sesudah aksi.

**Selesai jika:** admin dapat menjalankan alur keputusan end-to-end dan hasilnya mencerminkan status backend.

### FE-8 — Konsistensi dan pengujian akhir

- Periksa seluruh halaman untuk label, tombol, jarak, warna status, dialog, dan format tanggal.
- Uji ukuran window minimum dan ukuran umum.
- Uji keyboard/fokus pada form penting.
- Pastikan tidak ada tombol tanpa aksi atau data dummy yang terlihat di mode normal.
- Jalankan build dan semua pengujian yang relevan.

**Selesai jika:** tidak ada error JavaFX/FXML yang diketahui, alur utama dapat dipakai, dan semua kasus di checklist diuji.

---

## 17. Checklist Pengujian Front-end

### Global

- [ ] Aplikasi dapat diluncurkan dari entry point.
- [ ] Semua resource FXML/CSS ditemukan.
- [ ] Window dapat di-resize tanpa elemen utama saling menutupi.
- [ ] Navigasi menu aktif ditandai dengan benar.
- [ ] Aksi logout menghapus sesi aktif.
- [ ] Menu Admin tidak muncul pada User dan sebaliknya.
- [ ] Tidak ada stack trace yang ditampilkan ke pengguna.
- [ ] Operasi loading tidak membekukan UI.

### Form

- [ ] Field wajib kosong ditolak dengan pesan dekat field.
- [ ] Format tanggal/waktu salah ditolak.
- [ ] Rentang waktu terbalik ditolak.
- [ ] Tombol submit dinonaktifkan ketika operasi sedang berjalan.
- [ ] Klik ganda tidak membuat pengajuan ganda dengan mudah.
- [ ] Error dari service terlihat dan nilai input yang relevan tidak hilang secara tidak perlu.
- [ ] Form berhasil menampilkan konfirmasi dan memperbarui daftar setelah submit.

### Tabel dan pencarian

- [ ] Kolom mempunyai label jelas.
- [ ] Data kosong menampilkan empty state.
- [ ] Pencarian tanpa hasil menampilkan pesan khusus.
- [ ] Filter bisa di-reset jika tersedia.
- [ ] Data error tidak menyamar sebagai daftar kosong yang sukses.
- [ ] Perubahan data dan status memicu refresh tampilan.

### Peminjaman

- [ ] User tidak melihat aksi admin.
- [ ] Status `Menunggu` tidak ditampilkan sebagai `Disetujui`.
- [ ] Konflik terbaru dari backend menampilkan pesan yang jelas.
- [ ] Penolakan menampilkan alasan jika tersedia.
- [ ] Detail pengajuan milik user lain tidak dapat dibuka melalui manipulasi UI.

### Alternatif jadwal

- [ ] Jadwal awal dan durasi terlihat sebelum pencarian.
- [ ] Preferensi terpisah dari hard constraints.
- [ ] Hasil kandidat berasal dari service.
- [ ] Tidak ada kandidat tidak valid yang dapat diajukan.
- [ ] Tidak ada hasil valid ditangani dengan empty state.
- [ ] Data dosen yang belum lengkap menghasilkan peringatan, bukan klaim ketersediaan pasti.
- [ ] UI meminta konfirmasi sebelum mengirim perubahan jadwal.
- [ ] Persetujuan akhir menunggu hasil backend.

---

## 18. Kriteria Definition of Done (DoD)

Halaman dianggap selesai jika semua poin yang relevan terpenuhi:

1. FXML dan controller berada di struktur yang disepakati.
2. Styling menggunakan class dari stylesheet utama, bukan style duplikat tanpa alasan.
3. Label, judul, field, tombol, dan status menggunakan bahasa Indonesia yang konsisten.
4. Semua tombol/action penting memiliki handler yang berfungsi.
5. Data berasal dari service/model nyata, bukan hard-coded dummy pada mode normal.
6. Loading, empty, error, dan success state telah ditangani sesuai relevansi.
7. Form memiliki validasi front-end, sementara validasi resmi tetap dijalankan backend.
8. Hak akses berdasarkan role telah diterapkan pada UI dan didukung service.
9. UI tidak melakukan query SQL langsung.
10. Build dan pengujian relevan berhasil, atau error yang belum selesai ditulis dengan jelas.
11. Antigravity melaporkan file yang dibuat/diubah dan bukti pengujian, bukan hanya mengatakan “selesai”.

---

## 19. Instruksi Operasional Khusus untuk Antigravity

Sebelum memulai setiap tahap:

1. Baca spesifikasi ini dan `IMPLEMENTATION_PLAN.md`.
2. Audit repository dan gunakan ulang kode yang sesuai.
3. Nyatakan tahap yang akan dikerjakan dan file yang kemungkinan terdampak.
4. Implementasikan satu tahap yang dapat diuji; jangan menulis seluruh aplikasi dalam satu perubahan masif.
5. Jalankan Maven build/test yang relevan setelah perubahan.
6. Perbaiki error sebelum menyatakan tahap lulus.
7. Perbarui checklist atau ringkasan implementasi dengan fakta hasil pengujian.

### Larangan

- Jangan mengubah proyek menjadi web app.
- Jangan menggunakan React, HTML, Tailwind, Bootstrap, atau CSS browser.
- Jangan menaruh query SQL di controller.
- Jangan membuat tombol yang tidak melakukan aksi.
- Jangan menampilkan angka dashboard, slot jadwal, atau status transaksi dummy sebagai data resmi.
- Jangan menganggap validasi UI cukup untuk menjamin tidak ada bentrok.
- Jangan membuat UI menyatakan peminjaman atau perubahan jadwal disetujui sebelum backend mengonfirmasinya.
- Jangan menghapus fitur yang sudah ada tanpa memeriksa dampak dan menjelaskan alasan.
- Jangan menyatakan build/pengujian berhasil jika perintahnya belum dijalankan atau hasilnya belum diperiksa.

### Format laporan setelah setiap tahap

Antigravity harus melaporkan:

- **Tahap:** FE-0, FE-1, dan seterusnya.
- **File yang dibuat/diubah:** daftar ringkas.
- **Yang sudah berfungsi:** uraian spesifik.
- **Build/test:** perintah yang dijalankan dan hasil aktual.
- **Masalah yang tersisa:** error, dependency, data, atau integrasi yang belum tersedia.
- **Tahap berikutnya:** tahap paling logis berikutnya berdasarkan dependency.

---

## 20. Prompt Awal untuk Antigravity

Salin prompt berikut setelah file ini dan `IMPLEMENTATION_PLAN.md` ditempatkan di root repository.

> Baca seluruh isi `IMPLEMENTATION_PLAN.md` dan `FRONTEND_ENGINEERING.md` sebelum mengubah kode. Aplikasi ini adalah desktop app JavaFX, bukan web app. Audit repository, `pom.xml`, struktur package, FXML, CSS, service, dan model yang telah ada terlebih dahulu. Jangan menghapus kode yang berfungsi.
>
> Ikuti urutan FE-0 sampai FE-8. Mulai dari audit, lalu implementasikan fondasi aplikasi dan shell sebelum halaman fitur. Gunakan JavaFX + FXML + JavaFX CSS dan komponen JavaFX bawaan sebagai default. Pisahkan FXML, controller, service, dan repository. Controller tidak boleh menjalankan SQL. Semua tombol penting harus terhubung ke aksi nyata; semua halaman data harus mempunyai loading, empty, dan error state yang sesuai.
>
> Ikuti design tokens, struktur navigasi, spesifikasi halaman, role, dan kriteria penerimaan dalam dokumen. Gunakan data dari service; jangan menampilkan mock data sebagai data produksi. Untuk status transaksi, ketersediaan ruangan/barang, dan pencarian alternatif jadwal, backend/service adalah sumber kebenaran. Setelah setiap tahap, jalankan build/test yang relevan, perbaiki error, dan laporkan file yang diubah, hasil tes aktual, masalah yang tersisa, serta tahap berikutnya. Jangan lanjut ke tahap berikutnya bila tahap saat ini masih gagal build tanpa menjelaskan blocker.

---

## 21. Catatan Sinkronisasi dengan Implementation Plan

Dokumen ini menjelaskan **cara membangun dan menstandarkan front-end**. Untuk aturan bisnis seperti skema database, lifecycle status peminjaman, algoritma validasi bentrok, otorisasi, dan aturan perubahan jadwal, `IMPLEMENTATION_PLAN.md` serta implementasi service yang disetujui tetap menjadi acuan.

Jika kedua dokumen terlihat bertentangan:

1. Jangan mengambil keputusan diam-diam.
2. Utamakan konsistensi dengan aturan bisnis dan batasan MVP di `IMPLEMENTATION_PLAN.md`.
3. Catat konflik yang ditemukan dalam laporan Antigravity.
4. Lakukan perubahan minimum yang diperlukan agar build dan alur bisnis tetap konsisten.

**Hasil akhir yang diharapkan:** antarmuka desktop JavaFX yang konsisten, bisa digunakan, terhubung ke service, jelas menampilkan status, dan membantu pengguna menyelesaikan pekerjaan tanpa menebak-nebak.
