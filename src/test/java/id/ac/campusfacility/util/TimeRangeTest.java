package id.ac.campusfacility.util;

import id.ac.campusfacility.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeRangeTest {

    @Test
    @DisplayName("Uji aturan bentrok waktu: candidateStart < existingEnd AND candidateEnd > existingStart")
    void testTimeOverlaps() {
        TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(10, 0));

        // Bersentuhan di batas persis (TIDAK bentrok)
        assertFalse(range.overlaps(LocalTime.of(8, 0), LocalTime.of(9, 0)));
        assertFalse(range.overlaps(LocalTime.of(10, 0), LocalTime.of(11, 0)));

        // Tumpang tindih sebagian (BENTROK)
        assertTrue(range.overlaps(LocalTime.of(9, 30), LocalTime.of(10, 30)));
        assertTrue(range.overlaps(LocalTime.of(8, 30), LocalTime.of(9, 30)));

        // Tumpang tindih penuh di dalam (BENTROK)
        assertTrue(range.overlaps(LocalTime.of(9, 15), LocalTime.of(9, 45)));

        // Melingkupi rentang (BENTROK)
        assertTrue(range.overlaps(LocalTime.of(8, 0), LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("Waktu mulai setelah atau sama dengan jam selesai harus ditolak")
    void testInvalidTimeRange() {
        assertThrows(ValidationException.class, () ->
                new TimeRange(LocalTime.of(10, 0), LocalTime.of(9, 0)));
        assertThrows(ValidationException.class, () ->
                new TimeRange(LocalTime.of(10, 0), LocalTime.of(10, 0)));
    }
}
