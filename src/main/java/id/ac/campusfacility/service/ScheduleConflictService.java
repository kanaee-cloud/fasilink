package id.ac.campusfacility.service;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.dto.ScheduleConflict;
import id.ac.campusfacility.model.ClassGroup;
import id.ac.campusfacility.model.Room;
import id.ac.campusfacility.model.Schedule;
import id.ac.campusfacility.model.enums.ConflictType;
import id.ac.campusfacility.repository.AcademicDataRepository;
import id.ac.campusfacility.repository.RoomRepository;
import id.ac.campusfacility.repository.ScheduleRepository;
import id.ac.campusfacility.util.TimeRange;
import id.ac.campusfacility.validation.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleConflictService {

    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final AcademicDataRepository academicDataRepository;

    public ScheduleConflictService() {
        this.scheduleRepository = new ScheduleRepository();
        this.roomRepository = new RoomRepository();
        this.academicDataRepository = new AcademicDataRepository();
    }

    public ScheduleConflictService(ScheduleRepository scheduleRepository,
                                   RoomRepository roomRepository,
                                   AcademicDataRepository academicDataRepository) {
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
        this.academicDataRepository = academicDataRepository;
    }

    /**
     * Helper pemeriksaan konflik jadwal universal yang dapat dipakai bersama modul peminjaman
     * dan algoritma pencarian alternatif jadwal milik Arsal.
     */
    public List<ScheduleConflict> checkConflicts(long termId, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                                                Long roomId, Long lecturerId, Long classGroupId, Long excludeScheduleId) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        // 1. Validasi waktu dasar
        if (startTime == null || endTime == null) {
            throw new ValidationException("Waktu mulai dan selesai wajib diisi.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Jam mulai harus lebih awal daripada jam selesai.");
        }
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new ValidationException("Hari tidak valid (harus 1 sampai 7).");
        }

        // 2. Periksa jadwal perkuliahan yang tumpang tindih
        List<Schedule> overlapping = scheduleRepository.findPotentialConflicts(
                termId, dayOfWeek, startTime, endTime, roomId, lecturerId, classGroupId, excludeScheduleId
        );

        for (Schedule s : overlapping) {
            if (roomId != null && roomId.equals(s.getRoomId())) {
                conflicts.add(new ScheduleConflict(
                        ConflictType.ROOM,
                        "Ruangan " + s.getRoomCode() + " bentrok dengan perkuliahan " + s.getCourseName() +
                        " (" + s.getTimeRangeFormatted() + ")",
                        s.getRoomCode()
                ));
            }
            if (lecturerId != null && lecturerId.equals(s.getLecturerId())) {
                conflicts.add(new ScheduleConflict(
                        ConflictType.LECTURER,
                        "Dosen " + s.getLecturerName() + " sudah memiliki jadwal mengajar " + s.getCourseName() +
                        " di kelas " + s.getClassCode() + " (" + s.getTimeRangeFormatted() + ")",
                        s.getLecturerName()
                ));
            }
            if (classGroupId != null && classGroupId.equals(s.getClassGroupId())) {
                conflicts.add(new ScheduleConflict(
                        ConflictType.CLASS_GROUP,
                        "Kelas " + s.getClassCode() + " sudah memiliki jadwal kuliah " + s.getCourseName() +
                        " (" + s.getTimeRangeFormatted() + ")",
                        s.getClassCode()
                ));
            }
        }

        // 3. Periksa kapasitas ruangan vs jumlah mahasiswa kelas
        if (roomId != null && classGroupId != null) {
            Room room = roomRepository.findById(roomId).orElse(null);
            ClassGroup classGroup = academicDataRepository.findAllActiveClassGroups().stream()
                    .filter(cg -> cg.getId().equals(classGroupId))
                    .findFirst().orElse(null);

            if (room != null && classGroup != null && classGroup.getStudentCount() != null) {
                if (room.getCapacity() < classGroup.getStudentCount()) {
                    conflicts.add(new ScheduleConflict(
                            ConflictType.CAPACITY,
                            "Kapasitas ruangan " + room.getRoomCode() + " (" + room.getCapacity() + " kursi) " +
                            "kurang untuk kelas " + classGroup.getClassCode() + " (" + classGroup.getStudentCount() + " mahasiswa).",
                            room.getRoomCode()
                    ));
                }
            }
        }

        // 4. Periksa ketersediaan dosen jika data ketersediaan diisi
        if (lecturerId != null) {
            checkLecturerAvailability(termId, dayOfWeek, startTime, endTime, lecturerId, conflicts);
        }

        return conflicts;
    }

    private void checkLecturerAvailability(long termId, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                                          Long lecturerId, List<ScheduleConflict> conflicts) {
        String sql = """
            SELECT start_time, end_time FROM lecturer_availability
            WHERE term_id = ? AND day_of_week = ? AND lecturer_id = ?;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, termId);
            stmt.setInt(2, dayOfWeek);
            stmt.setLong(3, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean foundWindow = false;
                boolean insideWindow = false;
                while (rs.next()) {
                    foundWindow = true;
                    LocalTime winStart = LocalTime.parse(rs.getString("start_time"));
                    LocalTime winEnd = LocalTime.parse(rs.getString("end_time"));
                    // Harus berada sepenuhnya di dalam jendela waktu ketersediaan
                    if (!startTime.isBefore(winStart) && !endTime.isAfter(winEnd)) {
                        insideWindow = true;
                        break;
                    }
                }

                if (foundWindow && !insideWindow) {
                    conflicts.add(new ScheduleConflict(
                            ConflictType.LECTURER_UNAVAILABLE,
                            "Waktu yang dipilih berada di luar jendela waktu ketersediaan dosen pada hari tersebut.",
                            "Dosen ID: " + lecturerId
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memeriksa ketersediaan dosen: " + e.getMessage(), e);
        }
    }

    public boolean hasConflicts(long termId, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                               Long roomId, Long lecturerId, Long classGroupId, Long excludeScheduleId) {
        return !checkConflicts(termId, dayOfWeek, startTime, endTime, roomId, lecturerId, classGroupId, excludeScheduleId).isEmpty();
    }
}
