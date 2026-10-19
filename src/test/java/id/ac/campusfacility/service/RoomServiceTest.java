package id.ac.campusfacility.service;

import id.ac.campusfacility.BaseTest;
import id.ac.campusfacility.model.Room;
import id.ac.campusfacility.model.User;
import id.ac.campusfacility.model.enums.Role;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.AuthorizationException;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest extends BaseTest {

    private final RoomService roomService = new RoomService();
    private final AuthService authService = new AuthService();

    @BeforeEach
    void loginAsAdmin() {
        authService.login("admin", "admin123");
    }

    @Test
    @DisplayName("Admin dapat membuat ruangan baru yang valid")
    void testCreateRoomSuccess() {
        Room r = new Room();
        r.setRoomCode("LAB-AI");
        r.setRoomName("Laboratorium Artificial Intelligence");
        r.setBuilding("Gedung Riset C");
        r.setFloor(3);
        r.setCapacity(30);
        r.setDescription("Dilengkapi GPU Server");

        Room created = roomService.createRoom(r);
        assertNotNull(created.getId());
        assertEquals("LAB-AI", created.getRoomCode());

        Room fetched = roomService.getRoomById(created.getId());
        assertEquals(30, fetched.getCapacity());
    }

    @Test
    @DisplayName("User non-admin dilarang membuat atau mengubah ruangan")
    void testNonAdminCannotCreateRoom() {
        authService.login("user", "user123"); // Login sebagai user biasa
        Room r = new Room();
        r.setRoomCode("R-TEST");
        r.setRoomName("Ruang Uji");
        r.setBuilding("Gedung A");
        r.setFloor(1);
        r.setCapacity(20);

        assertThrows(AuthorizationException.class, () -> roomService.createRoom(r));
    }

    @Test
    @DisplayName("Duplikasi kode ruangan harus ditolak")
    void testDuplicateRoomCodeRejected() {
        Room r = new Room();
        r.setRoomCode("R101"); // R101 sudah ada di seed data
        r.setRoomName("Ruang Duplikat");
        r.setBuilding("Gedung B");
        r.setFloor(2);
        r.setCapacity(40);

        assertThrows(ConflictException.class, () -> roomService.createRoom(r));
    }

    @Test
    @DisplayName("Kapasitas ruangan nol atau negatif harus ditolak")
    void testInvalidCapacityRejected() {
        Room r = new Room();
        r.setRoomCode("R999");
        r.setRoomName("Ruang Invalid");
        r.setBuilding("Gedung B");
        r.setFloor(1);
        r.setCapacity(0);

        assertThrows(ValidationException.class, () -> roomService.createRoom(r));
    }

    @Test
    @DisplayName("Ruangan yang direferensikan jadwal dilakukan soft-deactivate saat dihapus")
    void testSoftDeleteReferencedRoom() {
        // Room R101 (ID 1) sudah dipakai di seed schedules
        boolean isHardDeleted = roomService.deleteRoom(1L);
        assertFalse(isHardDeleted, "Ruangan berelasi harus di-soft-deactivate");

        Room r = roomService.getRoomById(1L);
        assertFalse(r.isActive(), "Status is_active ruangan harus menjadi false");
    }

    @Test
    @DisplayName("Pemeriksaan ketersediaan ruangan mendeteksi jadwal kuliah aktif")
    void testRoomAvailabilityCheck() {
        // Pada seed data: ID 1 (R101) ada jadwal perkuliahan hari Senin jam 08:00 - 10:30
        LocalDate monday = LocalDate.of(2026, 10, 19); // Senin (DayOfWeek = 1)
        
        // Jam 08:30 - 09:30 bertabrakan dengan jadwal kuliah
        boolean available1 = roomService.isRoomAvailable(1L, monday, LocalTime.of(8, 30), LocalTime.of(9, 30));
        assertFalse(available1, "Harus tidak tersedia karena bertabrakan dengan jadwal");

        // Jam 11:00 - 12:00 tidak ada jadwal di R101
        boolean available2 = roomService.isRoomAvailable(1L, monday, LocalTime.of(11, 0), LocalTime.of(12, 0));
        assertTrue(available2, "Harus tersedia jika tidak ada jadwal bentrok");
    }
}
