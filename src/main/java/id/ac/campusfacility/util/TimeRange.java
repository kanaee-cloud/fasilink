package id.ac.campusfacility.util;

import id.ac.campusfacility.validation.ValidationException;
import java.time.LocalTime;
import java.util.Objects;

public class TimeRange {
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new ValidationException("Jam mulai dan jam selesai tidak boleh kosong.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Jam mulai harus lebih awal daripada jam selesai.");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sesuai aturan: candidateStart < existingEnd AND candidateEnd > existingStart.
     * Titik sentuh (misal 09.00-10.00 dan 10.00-11.00) TIDAK bentrok.
     */
    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        if (otherStart == null || otherEnd == null) {
            return false;
        }
        return this.startTime.isBefore(otherEnd) && this.endTime.isAfter(otherStart);
    }

    public boolean overlaps(TimeRange other) {
        if (other == null) {
            return false;
        }
        return overlaps(other.startTime, other.endTime);
    }

    public static boolean checkOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRange timeRange = (TimeRange) o;
        return Objects.equals(startTime, timeRange.startTime) && Objects.equals(endTime, timeRange.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public String toString() {
        return startTime + " - " + endTime;
    }
}
