# Pembagian Tugas Proyek
## Campus Facility & Schedule Manager

**Anggota 1:** Naura  
**Anggota 2:** Arsal  
**Jenis aplikasi:** Desktop application berbasis Java  
**Teknologi utama:** Java, JavaFX, FXML, JavaFX CSS, JDBC, SQLite, Maven

---

## 1. Tujuan Pembagian Tugas

Pekerjaan dibagi kepada dua anggota dengan porsi tanggung jawab yang seimbang. Setiap anggota mengerjakan **Frontend (FE) sebesar 25% dan Backend (BE) sebesar 25%** dari total ruang lingkup proyek.

Dengan pembagian ini, setiap anggota bertanggung jawab atas tampilan pengguna sekaligus logika backend dari modul yang menjadi bagiannya. Total kontribusi yang direncanakan adalah:

| Anggota | Frontend | Backend | Total porsi tanggung jawab |
|---|---:|---:|---:|
| Naura | 25% | 25% | 50% |
| Arsal | 25% | 25% | 50% |
| **Total proyek** | **50%** | **50%** | **100%** |

Persentase tersebut merupakan target pembagian beban kerja, bukan ukuran otomatis dari jumlah file atau baris kode. Tingkat kesulitan dan hasil kerja perlu ditinjau bersama selama pengerjaan.

---

## 2. Ringkasan Pembagian Modul

| Area | Naura | Arsal |
|---|---|---|
| Fokus utama | Manajemen fasilitas dan data dasar | Transaksi peminjaman dan pencarian alternatif jadwal |
| Frontend | Login, dashboard, ruangan, jadwal kampus, inventaris | Formulir peminjaman, persetujuan, status pengajuan, alternatif jadwal, riwayat |
| Backend | Autentikasi dan otorisasi, CRUD data fasilitas, pemeriksaan ketersediaan serta stok | Proses peminjaman dan pengembalian, approval, validasi ulang bentrok, algoritma alternatif jadwal |
| Integrasi | Menyediakan service dan data dasar yang digunakan modul transaksi | Menggunakan service/data Naura dan menghubungkan alur transaksi dengan tampilan pengguna |

**Catatan:** kedua anggota tetap wajib memahami alur aplikasi secara keseluruhan. Modul yang menjadi tanggung jawab seseorang tidak berarti boleh dibuat tanpa mengikuti kontrak data dan arsitektur bersama.

---

# 3. Tugas Naura — Anggota 1

## A. Frontend (25%) — Manajemen dan Tampilan Data Fasilitas

### A1. Login dan navigasi utama
- Membuat halaman login menggunakan JavaFX dan FXML.
- Membuat validasi tampilan awal untuk kolom username/email dan password.
- Menyediakan pesan kesalahan yang mudah dipahami ketika login gagal.
- Membuat kerangka aplikasi setelah login, termasuk header, sidebar, area konten, dan navigasi halaman.
- Menampilkan menu sesuai role `ADMIN` atau `USER`; pembatasan akses sesungguhnya tetap harus diperiksa di backend.

### A2. Dashboard Admin dan User
- Membuat dashboard Admin yang menampilkan ringkasan data fasilitas, jumlah ruangan, inventaris, dan informasi yang tersedia dari service.
- Membuat dashboard User yang menyediakan jalan pintas ke jadwal kampus, pencarian ruangan, serta inventaris.
- Menyediakan kondisi tampilan untuk loading, data kosong, dan kegagalan mengambil data.
- Tidak memasukkan angka statistik permanen atau data contoh sebagai data resmi aplikasi.

### A3. Halaman data ruangan
- Membuat halaman daftar ruangan dalam `TableView` atau komponen JavaFX yang sesuai.
- Menyediakan pencarian/filter sederhana jika masuk ruang lingkup UI.
- Menyediakan formulir tambah dan ubah data ruangan untuk Admin.
- Menampilkan detail ruangan, kapasitas, lokasi, dan status sesuai model data yang disepakati.
- Menyediakan konfirmasi sebelum penghapusan data yang berdampak pada data lain.

### A4. Halaman jadwal kampus
- Membuat tampilan jadwal perkuliahan berdasarkan tanggal/hari, jam, mata kuliah, kelas, dosen, dan ruangan sesuai data yang tersedia.
- Menyediakan filter yang disepakati, misalnya tanggal, hari, ruangan, kelas, atau mata kuliah.
- Membedakan tampilan informasi jadwal resmi dari permintaan perubahan jadwal yang belum disetujui.
- Memastikan informasi waktu dan tanggal konsisten dengan format yang dipakai aplikasi.

### A5. Halaman inventaris barang
- Membuat tabel inventaris yang menampilkan nama barang, jumlah tersedia, kondisi, dan informasi relevan lainnya.
- Membuat formulir tambah, ubah, dan pengelolaan data inventaris untuk Admin.
- Menampilkan jumlah stok dari backend; frontend tidak boleh menghitung atau menentukan stok resmi secara mandiri.

### Deliverables Frontend Naura
- FXML halaman login, dashboard, data ruangan, jadwal kampus, dan inventaris.
- Controller Java untuk halaman-halaman tersebut.
- JavaFX CSS dan komponen UI reusable sesuai design system proyek.
- Navigasi berdasarkan role yang terhubung ke service.
- Catatan cara menjalankan dan menguji halaman yang dibuat.

## B. Backend (25%) — Autentikasi, Data Fasilitas, dan Ketersediaan

### B1. Autentikasi dan otorisasi
- Membuat model pengguna beserta role `ADMIN` dan `USER`.
- Mengimplementasikan proses login melalui service dan repository.
- Menyimpan password dalam bentuk hash menggunakan metode yang sesuai; jangan menyimpan password sebagai teks biasa.
- Memastikan fitur administratif hanya dapat dilakukan oleh Admin melalui pemeriksaan di lapisan service.
- Mengembalikan hasil login atau pesan kesalahan yang tidak membocorkan informasi sensitif.

### B2. Repository dan service ruangan
- Membuat operasi create, read, update, dan delete (CRUD) untuk data ruangan.
- Memvalidasi data wajib, kapasitas, identitas ruangan, dan nilai yang tidak masuk akal.
- Mencegah penghapusan ruangan yang masih terkait dengan jadwal atau transaksi aktif tanpa kebijakan yang jelas.
- Menyediakan fungsi untuk mengambil daftar ruangan dan detail ruangan bagi frontend maupun modul peminjaman.

### B3. Repository dan service jadwal dasar
- Membuat operasi pengelolaan jadwal resmi dan data pendukung yang menjadi tanggung jawab Naura sesuai skema database bersama.
- Memvalidasi format waktu dan urutan jam mulai/jam selesai.
- Menyediakan fungsi pencarian jadwal berdasarkan tanggal/hari, kelas, dosen, mata kuliah, dan ruangan sesuai kebutuhan proyek.
- Membuat atau berkoordinasi dalam fungsi pemeriksaan konflik jadwal dasar agar dapat digunakan kembali oleh modul Arsal.

### B4. Repository dan service inventaris
- Membuat operasi CRUD inventaris barang.
- Memvalidasi jumlah stok agar tidak negatif.
- Menyediakan fungsi untuk memeriksa data dan jumlah barang tersedia.
- Menentukan dengan Arsal kontrak pengurangan/pengembalian stok agar satu transaksi tidak mengubah stok dua kali.

### Deliverables Backend Naura
- Model/entity, repository, service, dan validasi untuk pengguna, ruangan, jadwal dasar, serta inventaris.
- Skema tabel atau perubahan skema yang menjadi tanggung jawabnya, disepakati dengan Arsal sebelum digunakan.
- Unit test/integration test untuk login, role, CRUD ruangan, pengelolaan jadwal dasar, dan validasi stok.
- Dokumentasi singkat mengenai method/service yang boleh dipanggil modul Arsal.

---

# 4. Tugas Arsal — Anggota 2

## A. Frontend (25%) — Peminjaman dan Pencarian Jadwal

### A1. Halaman peminjaman ruangan
- Membuat formulir pengajuan peminjaman ruangan.
- Menyediakan pilihan tanggal, jam mulai, jam selesai, durasi, ruangan, dan tujuan peminjaman sesuai aturan produk.
- Menampilkan ketersediaan berdasarkan hasil service, bukan berdasarkan asumsi frontend.
- Menampilkan kesalahan ketika waktu tidak valid atau ruangan tidak tersedia.
- Mencegah pengiriman formulir ketika kolom wajib belum lengkap.

### A2. Halaman peminjaman barang
- Membuat formulir untuk memilih barang, jumlah, tanggal peminjaman, dan informasi pengambilan/pengembalian yang dibutuhkan.
- Menampilkan stok tersedia yang diperoleh dari backend.
- Memvalidasi jumlah positif pada frontend untuk kemudahan pengguna, sambil tetap mengandalkan backend sebagai pemeriksaan final.

### A3. Halaman status dan riwayat pengajuan
- Menampilkan daftar pengajuan milik user yang sedang login.
- Menampilkan status seperti `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, atau status lain yang disepakati di dokumen backend.
- Menampilkan detail pengajuan, waktu pengajuan, keputusan Admin, dan catatan bila tersedia.
- Menyediakan tampilan daftar pengajuan untuk Admin yang perlu ditinjau, bila halaman ini termasuk alur yang disepakati.

### A4. Halaman persetujuan Admin
- Membuat tampilan detail pengajuan dan pilihan tindakan menyetujui atau menolak.
- Menampilkan ringkasan tanggal, jam, ruangan/barang, pemohon, dan catatan yang relevan sebelum keputusan dibuat.
- Meminta konfirmasi untuk tindakan yang mengubah status transaksi.
- Menampilkan hasil dari backend; jangan mengubah status hanya dengan mengganti label di UI.

### A5. Halaman pencarian alternatif jadwal
- Membuat formulir input mata kuliah/kelas, durasi, rentang atau preferensi hari, serta preferensi waktu yang memang tersedia pada skema.
- Menampilkan daftar kandidat jadwal dari backend, termasuk tanggal/hari, waktu, dosen, ruangan, dan penjelasan relevan.
- Membedakan batasan wajib (hard constraints) dari preferensi (soft preferences) bila informasi tersebut ditampilkan.
- Menjelaskan ketika tidak ada alternatif yang memenuhi batasan.
- Menyediakan aksi untuk memilih kandidat dan mengajukan permintaan perubahan; UI tidak langsung mengubah jadwal resmi.

### Deliverables Frontend Arsal
- FXML formulir peminjaman ruangan dan barang, status/riwayat, persetujuan Admin, serta pencarian alternatif jadwal.
- Controller Java yang memanggil service dan menangani keadaan loading, kosong, sukses, dan error.
- JavaFX CSS yang mengikuti design system bersama.
- Petunjuk pengujian alur pengajuan, approval, dan pencarian jadwal.

## B. Backend (25%) — Transaksi Peminjaman, Approval, dan Alternatif Jadwal

### B1. Pengajuan peminjaman ruangan
- Membuat model, repository, dan service pengajuan peminjaman ruangan.
- Memeriksa data wajib, validitas tanggal/jam, dan ketersediaan ruangan.
- Memvalidasi konflik menggunakan service jadwal/ruangan yang disepakati dengan Naura.
- Memastikan User hanya dapat mengajukan dan melihat transaksi miliknya sendiri, kecuali Admin memiliki izin untuk melihat semua transaksi.
- Membuat status pengajuan awal secara konsisten.

### B2. Pengajuan dan pengembalian barang
- Membuat proses pengajuan peminjaman barang beserta jumlah yang diminta.
- Memeriksa stok pada saat pengajuan dan memvalidasi ulang ketika disetujui.
- Menerapkan perubahan stok/transaksi secara konsisten dalam database transaction.
- Mencatat pengambilan dan pengembalian barang serta kondisi barang jika dibutuhkan.
- Mencegah stok negatif, pengurangan ganda, atau pengembalian yang dicatat lebih dari sekali.

### B3. Approval dan penolakan pengajuan
- Membuat service Admin untuk menyetujui atau menolak pengajuan.
- Memastikan hanya Admin dapat memutuskan pengajuan.
- Memvalidasi ulang ketersediaan ruangan atau stok ketika approval dilakukan karena kondisi dapat berubah sejak pengajuan dibuat.
- Melakukan pembaruan status dan data terkait dalam transaksi database apabila beberapa operasi harus berhasil bersama-sama.
- Menyimpan identitas pengambil keputusan, waktu keputusan, dan catatan penolakan bila skema mendukungnya.
- Mencegah pengajuan yang sudah diputuskan diproses ulang secara tidak sah.

### B4. Algoritma pencarian alternatif jadwal
- Membuat service yang menerima parameter mata kuliah/kelas, durasi, dan preferensi pencarian.
- Menghasilkan kandidat yang mempertahankan durasi kelas.
- Memeriksa konflik kelas, jadwal dosen, ruangan, kapasitas yang diperlukan, dan pemakaian ruangan yang relevan.
- Membedakan hard constraints dari soft preferences: kandidat yang melanggar batasan wajib harus dikeluarkan, bukan sekadar diberi skor rendah.
- Memberi peringkat kandidat valid berdasarkan preferensi yang disepakati.
- Mengembalikan daftar alternatif yang valid atau hasil kosong yang dapat dijelaskan.
- Menambahkan test untuk kasus kandidat valid, kandidat konflik, durasi tidak muat, dan tidak ada alternatif.

### B5. Permintaan perubahan jadwal resmi
- Menyimpan kandidat yang dipilih sebagai permintaan perubahan, bukan langsung mengganti jadwal resmi.
- Mengarahkan permintaan ke proses persetujuan Admin atau pihak berwenang sesuai ruang lingkup proyek.
- Ketika permintaan disetujui, menjalankan validasi ulang terhadap kondisi terkini.
- Memperbarui jadwal resmi hanya jika semua validasi tetap lolos dan approval sah.
- Menolak atau menandai permintaan untuk ditinjau ulang apabila kandidat tidak lagi tersedia.
- Menyimpan riwayat perubahan agar status dan keputusan dapat ditelusuri.

### Deliverables Backend Arsal
- Model/entity, repository, service, dan validasi untuk peminjaman, pengembalian, keputusan Admin, dan permintaan perubahan jadwal.
- Service algoritma alternatif jadwal beserta aturan ranking yang terdokumentasi.
- Unit test/integration test untuk transaksi, approval, konflik, stok, dan perubahan jadwal.
- Dokumentasi method/service yang dipanggil frontend dan ketergantungan modul pada service Naura.

---

# 5. Tanggung Jawab Bersama

Tugas berikut dikerjakan dan disepakati oleh Naura serta Arsal. Pekerjaan bersama tidak dimaksudkan untuk mengaburkan kepemilikan modul, tetapi untuk memastikan seluruh bagian dapat digabungkan.

## 5.1 Sebelum implementasi
- Membaca `IMPLEMENTATION_PLAN.md`, `FRONTEND_ENGINEERING.md`, dan `BACKEND_ENGINEERING.md`.
- Menyepakati versi Java, JavaFX, Maven, SQLite, struktur package, penamaan class, serta status transaksi.
- Menyepakati skema database dan aturan penamaan kolom sebelum masing-masing membuat repository.
- Menyepakati kontrak service: parameter input, nilai balik, exception/error, dan aturan akses.
- Menentukan penggunaan data seed untuk demo dan membedakannya dari data yang dimasukkan pengguna.

## 5.2 Integrasi FE dan BE
- Frontend memanggil service; controller tidak boleh berisi query SQL.
- Semua validasi penting wajib dilakukan di backend, meskipun validasi awal juga ada di frontend.
- Jangan membuat dua implementasi berbeda untuk pemeriksaan konflik jadwal atau perubahan stok.
- Pastikan operasi database yang saling bergantung menggunakan transaksi.
- Jangan menyatakan fitur berhasil sebelum alur dari UI hingga database berhasil diuji.

## 5.3 Pengujian akhir
Keduanya menguji setidaknya skenario berikut:
- Login berhasil dan gagal serta pembatasan akses Admin/User.
- Admin dapat mengelola ruangan, jadwal dasar, dan inventaris.
- User hanya dapat mengakses data/transaksi yang memang boleh dilihatnya.
- Pengajuan ruangan yang bentrok ditolak.
- Stok barang tidak dapat menjadi negatif.
- Approval memvalidasi ulang ketersediaan.
- Pengembalian barang tidak tercatat ganda.
- Alternatif jadwal tidak melanggar hard constraints.
- Jadwal resmi tidak berubah sebelum perubahan disetujui.
- Aplikasi menangani data kosong dan error tanpa crash.

---

# 6. Kontrak Integrasi yang Wajib Disepakati

Sebelum membuat modul yang saling memanggil, sepakati hal-hal berikut dalam sesi singkat dan dokumentasikan dalam README atau dokumen backend:

1. **Struktur model:** field, tipe data, ID, dan relasi untuk User, Room, Schedule, InventoryItem, LoanRequest, dan ScheduleChangeRequest.
2. **Status transaksi:** gunakan enum atau konstanta yang konsisten, bukan string bebas yang berbeda antaranggota.
3. **Service ketersediaan:** Naura menyediakan service atau metode pemeriksaan jadwal/ruangan yang dapat dipakai Arsal.
4. **Service inventaris:** Naura menyediakan pembacaan stok; Arsal menangani perubahan stok yang berkaitan dengan transaksi melalui mekanisme yang disepakati.
5. **Approval:** Arsal menangani alur transaksi/approval, tetapi wajib memakai validasi ketersediaan dari modul yang bertanggung jawab atas data dasar.
6. **Pencarian alternatif:** algoritma Arsal menggunakan sumber data jadwal, dosen, kelas, dan ruangan yang dikelola melalui model/repository bersama.
7. **Error handling:** sepakati cara service melaporkan validation error, akses ditolak, data tidak ditemukan, dan konflik.
8. **Database migration/initialization:** hanya gunakan satu mekanisme schema/init database agar perubahan masing-masing anggota tidak saling menimpa.

---

# 7. Urutan Pengerjaan dan Milestone

| Milestone | Penanggung jawab utama | Hasil yang diharapkan |
|---|---|---|
| 1. Audit repository dan setup project | Naura + Arsal | Project dapat di-build; versi Java/Maven dan dependensi disepakati |
| 2. Skema database dan model inti | Naura + Arsal | Struktur data serta relasi disepakati sebelum modul berkembang |
| 3. Shell UI dan navigasi berbasis role | Naura | Login, layout utama, navigasi, dan dasar styling |
| 4. Autentikasi dan manajemen data fasilitas | Naura | Login yang terhubung ke backend; CRUD ruangan/jadwal dasar/inventaris |
| 5. UI pengajuan dan daftar transaksi | Arsal | Form peminjaman, status, riwayat, serta tampilan approval |
| 6. Backend peminjaman dan approval | Arsal | Pengajuan, validasi ulang, keputusan, dan pengembalian berjalan |
| 7. Pencarian alternatif jadwal | Arsal, dengan dukungan data Naura | Kandidat valid dicari dan diberi peringkat; tidak mengubah jadwal resmi langsung |
| 8. Integrasi menyeluruh | Naura + Arsal | Frontend terhubung ke backend; tidak ada placeholder untuk alur inti |
| 9. Pengujian, perbaikan, dan demo | Naura + Arsal | Test lulus atau kegagalan terdokumentasi; demo alur utama siap |

Milestone dapat disesuaikan dengan kondisi repository, tetapi perubahan urutan perlu disepakati berdua agar salah satu anggota tidak menunggu kontrak modul yang belum tersedia.

---

# 8. Definition of Done

Sebuah tugas dianggap selesai jika:

- [ ] Kode sesuai struktur dan pola arsitektur yang disepakati.
- [ ] Fitur terhubung dengan service/database yang benar; bukan hanya tampilan statis.
- [ ] Validasi input dan error handling tersedia.
- [ ] Hak akses diperiksa di backend untuk operasi yang dilindungi.
- [ ] Tidak ada query SQL di JavaFX controller.
- [ ] Test relevan dijalankan dan hasil aktual dicatat.
- [ ] Build project berjalan tanpa error yang disebabkan perubahan tersebut.
- [ ] Tidak ada data hardcoded yang menyamar sebagai data resmi.
- [ ] Instruksi penggunaan atau pengujian diperbarui jika diperlukan.
- [ ] Perubahan telah ditinjau anggota lainnya dan berhasil diintegrasikan.

---

# 9. Format Laporan Progres Setiap Anggota

Gunakan format singkat berikut ketika melaporkan hasil kepada kelompok atau Antigravity:

```markdown
## Laporan Progres — [Naura/Arsal]

### Tugas yang diselesaikan
- [Nama fitur/modul]

### File yang dibuat atau diubah
- `path/to/file`

### Pengujian
- Command yang dijalankan:
- Hasil aktual:

### Kendala atau dependensi
- [Kendala, keputusan yang dibutuhkan, atau modul anggota lain yang diperlukan]

### Pekerjaan berikutnya
- [Langkah berikutnya]
```

---

# 10. Prompt untuk Antigravity

Gunakan prompt berikut setelah menambahkan file ini ke root repository:

> Baca `PEMBAGIAN_TUGAS_NAURA_ARSAL.md`, `IMPLEMENTATION_PLAN.md`, `FRONTEND_ENGINEERING.md`, dan `BACKEND_ENGINEERING.md` sebelum mengubah kode. Perlakukan dokumen pembagian tugas sebagai acuan kepemilikan modul: Naura bertanggung jawab pada login, navigasi, dashboard, data dasar fasilitas, serta backend autentikasi dan pengelolaan fasilitas; Arsal bertanggung jawab pada frontend transaksi/pencarian alternatif dan backend peminjaman, approval, pengembalian, serta algoritma alternatif jadwal. Kedua anggota sama-sama terlibat dalam keputusan database, kontrak service, integrasi, dan pengujian akhir.
>
> Audit repository terlebih dahulu. Jangan langsung menulis ulang project atau mengklaim pekerjaan sudah selesai tanpa build dan pengujian yang benar-benar dijalankan. Implementasikan secara bertahap, pertahankan kode yang masih berfungsi, ikuti arsitektur Java/JavaFX/JDBC/SQLite yang disepakati, dan dokumentasikan file yang berubah, hasil test aktual, kendala, serta pekerjaan yang tersisa pada setiap milestone. Jangan mengganti data contoh sebagai data resmi dan jangan melewati validasi backend untuk konflik jadwal, akses role, stok, atau approval.

---

## Catatan Akhir

Pembagian ini memberi Naura fokus pada data dan manajemen fasilitas, sedangkan Arsal fokus pada transaksi dan pencarian alternatif jadwal. Karena algoritma alternatif jadwal berpotensi lebih kompleks dibanding CRUD, beban kerja perlu dievaluasi selama implementasi. Jika Arsal membutuhkan dukungan tambahan, Naura dapat membantu pengujian, validasi data dasar, atau integrasi service tanpa mengaburkan kepemilikan utama setiap modul.
