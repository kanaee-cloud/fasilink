package id.ac.campusfacility.config;

import id.ac.campusfacility.util.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection()) {
            createTables(conn);
            seedDefaultData(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menginisialisasi database: " + e.getMessage(), e);
        }
    }

    public static void initialize(Connection conn) {
        try {
            createTables(conn);
            seedDefaultData(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menginisialisasi database pada koneksi kustom: " + e.getMessage(), e);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 1. Users
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'USER')),
                    is_active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // 2. Academic Terms
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS academic_terms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    term_name TEXT NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1
                );
            """);

            // 3. Rooms
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_code TEXT NOT NULL UNIQUE,
                    room_name TEXT NOT NULL,
                    building TEXT NOT NULL,
                    floor INTEGER NOT NULL,
                    capacity INTEGER NOT NULL CHECK(capacity > 0),
                    description TEXT,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // 4. Inventory Items
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_code TEXT NOT NULL UNIQUE,
                    item_name TEXT NOT NULL,
                    description TEXT,
                    total_quantity INTEGER NOT NULL CHECK(total_quantity >= 0),
                    condition_notes TEXT,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // 5. Courses
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_code TEXT NOT NULL UNIQUE,
                    course_name TEXT NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1
                );
            """);

            // 6. Class Groups
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS class_groups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    study_program TEXT NOT NULL,
                    cohort_year INTEGER NOT NULL,
                    class_code TEXT NOT NULL,
                    student_count INTEGER CHECK(student_count IS NULL OR student_count > 0),
                    is_active INTEGER NOT NULL DEFAULT 1,
                    UNIQUE(study_program, cohort_year, class_code)
                );
            """);

            // 7. Lecturers
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lecturers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    lecturer_code TEXT NOT NULL UNIQUE,
                    full_name TEXT NOT NULL,
                    email TEXT,
                    is_active INTEGER NOT NULL DEFAULT 1
                );
            """);

            // 8. Lecturer Availability
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lecturer_availability (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    lecturer_id INTEGER NOT NULL REFERENCES lecturers(id),
                    term_id INTEGER NOT NULL REFERENCES academic_terms(id),
                    day_of_week INTEGER NOT NULL CHECK(day_of_week BETWEEN 1 AND 7),
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    notes TEXT
                );
            """);

            // 9. Schedules (Jadwal Perkuliahan Mingguan)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schedules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    term_id INTEGER NOT NULL REFERENCES academic_terms(id),
                    course_id INTEGER NOT NULL REFERENCES courses(id),
                    class_group_id INTEGER NOT NULL REFERENCES class_groups(id),
                    lecturer_id INTEGER NOT NULL REFERENCES lecturers(id),
                    room_id INTEGER NOT NULL REFERENCES rooms(id),
                    day_of_week INTEGER NOT NULL CHECK(day_of_week BETWEEN 1 AND 7),
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // 10. Room Booking Requests (Modul Arsal)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS room_booking_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    requester_user_id INTEGER NOT NULL REFERENCES users(id),
                    room_id INTEGER NOT NULL REFERENCES rooms(id),
                    booking_date TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    expected_attendees INTEGER NOT NULL CHECK(expected_attendees > 0),
                    purpose TEXT NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
                    reviewed_by_user_id INTEGER REFERENCES users(id),
                    reviewed_at TEXT,
                    rejection_reason TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // 11. Inventory Loan Requests (Modul Arsal)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory_loan_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    requester_user_id INTEGER NOT NULL REFERENCES users(id),
                    item_id INTEGER NOT NULL REFERENCES inventory_items(id),
                    quantity INTEGER NOT NULL CHECK(quantity > 0),
                    loan_date TEXT NOT NULL,
                    expected_return_date TEXT NOT NULL,
                    purpose TEXT NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'HANDED_OVER', 'RETURNED')),
                    reviewed_by_user_id INTEGER REFERENCES users(id),
                    reviewed_at TEXT,
                    rejection_reason TEXT,
                    handed_over_at TEXT,
                    returned_at TEXT,
                    return_condition_notes TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // 12. Schedule Change Requests (Modul Arsal)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schedule_change_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    requester_user_id INTEGER NOT NULL REFERENCES users(id),
                    schedule_id INTEGER NOT NULL REFERENCES schedules(id),
                    proposed_day_of_week INTEGER NOT NULL CHECK(proposed_day_of_week BETWEEN 1 AND 7),
                    proposed_start_time TEXT NOT NULL,
                    proposed_end_time TEXT NOT NULL,
                    proposed_room_id INTEGER NOT NULL REFERENCES rooms(id),
                    reason TEXT NOT NULL,
                    status TEXT NOT NULL CHECK(status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
                    reviewed_by_user_id INTEGER REFERENCES users(id),
                    reviewed_at TEXT,
                    rejection_reason TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            // Create essential indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_rooms_code ON rooms(room_code);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inventory_code ON inventory_items(item_code);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_schedules_term_day_room ON schedules(term_id, day_of_week, room_id, is_active);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_schedules_lecturer ON schedules(term_id, day_of_week, lecturer_id, is_active);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_schedules_class ON schedules(term_id, day_of_week, class_group_id, is_active);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_booking_room_date ON room_booking_requests(room_id, booking_date, status);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_loan_item_status ON inventory_loan_requests(item_id, status);");
        }
    }

    private static void seedDefaultData(Connection conn) throws SQLException {
        String now = LocalDateTime.now().toString();

        // 1. Seed Users (Admin & User)
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
            checkStmt.setString(1, "admin");
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertUser = """
                        INSERT INTO users (full_name, username, password_hash, role, is_active, created_at, updated_at)
                        VALUES (?, ?, ?, ?, 1, ?, ?);
                    """;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUser)) {
                        insertStmt.setString(1, "Administrator Kampus");
                        insertStmt.setString(2, "admin");
                        insertStmt.setString(3, PasswordHasher.hash("admin123"));
                        insertStmt.setString(4, "ADMIN");
                        insertStmt.setString(5, now);
                        insertStmt.setString(6, now);
                        insertStmt.executeUpdate();
                    }
                }
            }

            checkStmt.setString(1, "user");
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertUser = """
                        INSERT INTO users (full_name, username, password_hash, role, is_active, created_at, updated_at)
                        VALUES (?, ?, ?, ?, 1, ?, ?);
                    """;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUser)) {
                        insertStmt.setString(1, "Mahasiswa Pengguna");
                        insertStmt.setString(2, "user");
                        insertStmt.setString(3, PasswordHasher.hash("user123"));
                        insertStmt.setString(4, "USER");
                        insertStmt.setString(5, now);
                        insertStmt.setString(6, now);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }

        // 2. Seed Academic Term
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM academic_terms")) {
            if (rs.next() && rs.getInt(1) == 0) {
                s.execute("""
                    INSERT INTO academic_terms (term_name, start_date, end_date, is_active)
                    VALUES ('Semester Ganjil 2026/2027', '2026-09-01', '2027-01-31', 1);
                """);
            }
        }

        // 3. Seed Rooms
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM rooms")) {
            if (rs.next() && rs.getInt(1) == 0) {
                s.execute(String.format("""
                    INSERT INTO rooms (room_code, room_name, building, floor, capacity, description, is_active, created_at, updated_at)
                    VALUES 
                    ('R101', 'Ruang Teori 101', 'Gedung Kuliah Bersama A', 1, 40, 'Dilengkapi AC, Proyektor, dan Whiteboard', 1, '%s', '%s'),
                    ('R102', 'Ruang Teori 102', 'Gedung Kuliah Bersama A', 1, 35, 'Dilengkapi AC dan Whiteboard', 1, '%s', '%s'),
                    ('LAB-KOM', 'Laboratorium Komputer Terpadu', 'Gedung Sains & Teknologi', 2, 45, '45 Unit PC Workstation + LAN Gigabit', 1, '%s', '%s'),
                    ('AULA-UTAMA', 'Auditorium Utama Kampus', 'Gedung Rektorat', 3, 250, 'Panggung representatif, Sound System, 2 Proyektor Besar', 1, '%s', '%s');
                """, now, now, now, now, now, now, now, now));
            }
        }

        // 4. Seed Inventory Items
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM inventory_items")) {
            if (rs.next() && rs.getInt(1) == 0) {
                s.execute(String.format("""
                    INSERT INTO inventory_items (item_code, item_name, description, total_quantity, condition_notes, is_active, created_at, updated_at)
                    VALUES 
                    ('PRJ-01', 'Proyektor Epson EB-X51', 'Proyektor portable 3800 lumens HDMI/VGA', 5, 'Semua berfungsi baik dengan kabel lengkap', 1, '%s', '%s'),
                    ('HDMI-01', 'Kabel HDMI 10 Meter', 'High speed gold plated HDMI cable', 10, 'Kondisi baik di dalam tas aksesoris', 1, '%s', '%s'),
                    ('SPK-01', 'Sound System Portable Wireless', 'Speaker portable 12 inch + 2 Mic wireless', 3, 'Baterai sehat, siap pakai seminar/kegiatan', 1, '%s', '%s'),
                    ('PTR-01', 'Laser Pointer Wireless Presenter', 'Pointer merah dengan tombol navigasi slide USB', 8, 'Normal, baterai baru terisi', 1, '%s', '%s');
                """, now, now, now, now, now, now, now, now));
            }
        }

        // 5. Seed Courses
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM courses")) {
            if (rs.next() && rs.getInt(1) == 0) {
                s.execute("""
                    INSERT INTO courses (course_code, course_name, is_active)
                    VALUES 
                    ('IF2101', 'Pemrograman Berorientasi Objek (Java)', 1),
                    ('IF2102', 'Basis Data Terdistribusi', 1),
                    ('IF2103', 'Rekayasa Perangkat Lunak', 1),
                    ('IF2104', 'Jaringan Komputer', 1);
                """);
            }
        }

        // 6. Seed Class Groups
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM class_groups")) {
            if (rs.next() && rs.getInt(1) == 0) {
                s.execute("""
                    INSERT INTO class_groups (study_program, cohort_year, class_code, student_count, is_active)
                    VALUES 
                    ('Teknik Informatika', 2024, 'IF-3A', 35, 1),
                    ('Teknik Informatika', 2024, 'IF-3B', 38, 1),
                    ('Sistem Informasi', 2024, 'SI-3A', 32, 1);
                """);
            }
        }

        // 7. Seed Lecturers
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM lecturers")) {
            if (rs.next() && rs.getInt(1) == 0) {
                s.execute("""
                    INSERT INTO lecturers (lecturer_code, full_name, email, is_active)
                    VALUES 
                    ('DSN001', 'Dr. Hendra Wijaya, S.Kom., M.T.', 'hendra.wijaya@campus.ac.id', 1),
                    ('DSN002', 'Ir. Ratna Kusuma, M.Kom.', 'ratna.kusuma@campus.ac.id', 1),
                    ('DSN003', 'Budi Santoso, S.T., M.Kom.', 'budi.santoso@campus.ac.id', 1);
                """);
            }
        }

        // 8. Seed Schedules
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM schedules")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Senin: IF2101 di R101 jam 08:00 - 10:30
                // Selasa: IF2102 di LAB-KOM jam 10:00 - 12:30
                // Rabu: IF2103 di R102 jam 13:00 - 15:30
                s.execute(String.format("""
                    INSERT INTO schedules (term_id, course_id, class_group_id, lecturer_id, room_id, day_of_week, start_time, end_time, is_active, created_at, updated_at)
                    VALUES 
                    (1, 1, 1, 1, 1, 1, '08:00', '10:30', 1, '%s', '%s'),
                    (1, 2, 2, 2, 3, 2, '10:00', '12:30', 1, '%s', '%s'),
                    (1, 3, 1, 3, 2, 3, '13:00', '15:30', 1, '%s', '%s');
                """, now, now, now, now, now, now));
            }
        }
    }
}
