package id.ac.campusfacility.config;

import id.ac.campusfacility.model.User;
import id.ac.campusfacility.model.enums.Role;
import id.ac.campusfacility.repository.UserRepository;
import id.ac.campusfacility.repository.RoomRepository;
import id.ac.campusfacility.repository.InventoryRepository;
import id.ac.campusfacility.repository.ScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerTest {

    @Test
    @DisplayName("Inisialisasi database default dan verifikasi seed data")
    void testInitializeDefaultDatabase() {
        DatabaseInitializer.initialize();

        File dbFile = new File(AppConfig.getDatabasePath());
        assertTrue(dbFile.exists(), "File database harus dibuat di direktori data/");

        UserRepository userRepo = new UserRepository();
        Optional<User> admin = userRepo.findByUsername("admin");
        assertTrue(admin.isPresent(), "Akun demo admin harus ada");
        assertEquals(Role.ADMIN, admin.get().getRole());

        Optional<User> user = userRepo.findByUsername("user");
        assertTrue(user.isPresent(), "Akun demo user harus ada");
        assertEquals(Role.USER, user.get().getRole());

        RoomRepository roomRepo = new RoomRepository();
        assertFalse(roomRepo.findAll().isEmpty(), "Data seed ruangan harus terisi");

        InventoryRepository invRepo = new InventoryRepository();
        assertFalse(invRepo.findAll().isEmpty(), "Data seed inventaris harus terisi");

        ScheduleRepository schedRepo = new ScheduleRepository();
        assertFalse(schedRepo.findAllActive().isEmpty(), "Data seed jadwal harus terisi");
    }
}
