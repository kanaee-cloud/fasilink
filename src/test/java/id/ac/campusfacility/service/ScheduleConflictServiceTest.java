package id.ac.campusfacility.service;

import id.ac.campusfacility.BaseTest;
import id.ac.campusfacility.dto.ScheduleConflict;
import id.ac.campusfacility.model.enums.ConflictType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleConflictServiceTest extends BaseTest {

    private final ScheduleConflictService conflictService = new ScheduleConflictService();

    @Test
    @DisplayName("Mendeteksi bentrok jadwal pada ruangan yang sama")
    void testRoomConflictDetected() {
        // Pada seed: Hari 1 (Senin), R101 (ID 1) ada kelas IF2101 jam 08:00 - 10:30
        List<ScheduleConflict> conflicts = conflictService.checkConflicts(
                1L,
                1, // Senin
                LocalTime.of(9, 0), LocalTime.of(11, 0),
                1L, // Room ID 1 (R101)
                2L, // Dosen beda
                2L, // Kelas beda
                null
        );

        assertFalse(conflicts.isEmpty());
        assertTrue(conflicts.stream().anyMatch(c -> c.getType() == ConflictType.ROOM));
    }

    @Test
    @DisplayName("Jadwal tepat di batas jam tidak dianggap bentrok")
    void testTouchingTimesDoNotConflict() {
        // Pada seed: Hari 1 (Senin), R101 jam 08:00 - 10:30
        // Jadwal baru mulai jam 10:30 - 12:00 di ruangan yang sama
        List<ScheduleConflict> conflicts = conflictService.checkConflicts(
                1L,
                1, // Senin
                LocalTime.of(10, 30), LocalTime.of(12, 0),
                1L, // Room ID 1
                2L,
                2L,
                null
        );

        assertTrue(conflicts.isEmpty(), "Jadwal bersentuhan di batas jam tidak boleh bentrok");
    }

    @Test
    @DisplayName("Mendeteksi bentrok jadwal dosen yang sama")
    void testLecturerConflictDetected() {
        // Pada seed: Hari 1 (Senin), Dosen ID 1 (Dr. Hendra) mengajar jam 08:00 - 10:30
        List<ScheduleConflict> conflicts = conflictService.checkConflicts(
                1L,
                1, // Senin
                LocalTime.of(9, 30), LocalTime.of(11, 30),
                2L, // Ruangan beda (R102)
                1L, // Dosen ID 1 (Sama)
                2L, // Kelas beda
                null
        );

        assertFalse(conflicts.isEmpty());
        assertTrue(conflicts.stream().anyMatch(c -> c.getType() == ConflictType.LECTURER));
    }
}
