package id.ac.campusfacility.repository;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.model.Schedule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScheduleRepository {

    private static final String BASE_SELECT_JOIN = """
        SELECT s.*, 
               t.term_name, 
               c.course_code, c.course_name, 
               cg.class_code, 
               l.full_name AS lecturer_name, 
               r.room_code, r.room_name
        FROM schedules s
        JOIN academic_terms t ON s.term_id = t.id
        JOIN courses c ON s.course_id = c.id
        JOIN class_groups cg ON s.class_group_id = cg.id
        JOIN lecturers l ON s.lecturer_id = l.id
        JOIN rooms r ON s.room_id = r.id
    """;

    public List<Schedule> findAllActive() {
        List<Schedule> list = new ArrayList<>();
        String sql = BASE_SELECT_JOIN + " WHERE s.is_active = 1 ORDER BY s.day_of_week ASC, s.start_time ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSchedule(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data jadwal aktif: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Schedule> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = BASE_SELECT_JOIN + " WHERE s.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSchedule(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil jadwal berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Schedule> findByFilters(Long termId, Integer dayOfWeek, Long roomId, Long courseId) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_JOIN).append(" WHERE s.is_active = 1 ");
        List<Object> params = new ArrayList<>();

        if (termId != null) {
            sql.append(" AND s.term_id = ? ");
            params.add(termId);
        }
        if (dayOfWeek != null && dayOfWeek > 0) {
            sql.append(" AND s.day_of_week = ? ");
            params.add(dayOfWeek);
        }
        if (roomId != null) {
            sql.append(" AND s.room_id = ? ");
            params.add(roomId);
        }
        if (courseId != null) {
            sql.append(" AND s.course_id = ? ");
            params.add(courseId);
        }

        sql.append(" ORDER BY s.day_of_week ASC, s.start_time ASC ");

        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSchedule(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memfilter data jadwal: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Memeriksa potensi bentrok waktu terhadap jadwal aktif lain di semester dan hari yang sama.
     * Menggunakan aturan overlap: candidateStart < existingEnd AND candidateEnd > existingStart.
     */
    public List<Schedule> findPotentialConflicts(long termId, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                                                 Long roomId, Long lecturerId, Long classGroupId, Long excludeScheduleId) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_JOIN)
                .append(" WHERE s.is_active = 1 ")
                .append(" AND s.term_id = ? ")
                .append(" AND s.day_of_week = ? ")
                // Overlap condition: startTime < s.end_time AND endTime > s.start_time
                .append(" AND ? < s.end_time AND ? > s.start_time ")
                // Either matching room, lecturer, or classGroup
                .append(" AND (s.room_id = ? OR s.lecturer_id = ? OR s.class_group_id = ?) ");

        if (excludeScheduleId != null) {
            sql.append(" AND s.id <> ? ");
        }

        List<Schedule> conflicts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, termId);
            stmt.setInt(2, dayOfWeek);
            stmt.setString(3, startTime.toString());
            stmt.setString(4, endTime.toString());
            stmt.setLong(5, roomId != null ? roomId : -1L);
            stmt.setLong(6, lecturerId != null ? lecturerId : -1L);
            stmt.setLong(7, classGroupId != null ? classGroupId : -1L);
            if (excludeScheduleId != null) {
                stmt.setLong(8, excludeScheduleId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conflicts.add(mapResultSetToSchedule(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memeriksa bentrok jadwal pada database: " + e.getMessage(), e);
        }
        return conflicts;
    }

    public Schedule save(Schedule schedule) {
        String sql = """
            INSERT INTO schedules (term_id, course_id, class_group_id, lecturer_id, room_id,
                                  day_of_week, start_time, end_time, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, schedule.getTermId());
            stmt.setLong(2, schedule.getCourseId());
            stmt.setLong(3, schedule.getClassGroupId());
            stmt.setLong(4, schedule.getLecturerId());
            stmt.setLong(5, schedule.getRoomId());
            stmt.setInt(6, schedule.getDayOfWeek());
            stmt.setString(7, schedule.getStartTime().toString());
            stmt.setString(8, schedule.getEndTime().toString());
            stmt.setInt(9, schedule.isActive() ? 1 : 0);
            stmt.setString(10, now);
            stmt.setString(11, now);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    schedule.setId(generatedKeys.getLong(1));
                }
            }
            schedule.setCreatedAt(LocalDateTime.parse(now));
            schedule.setUpdatedAt(LocalDateTime.parse(now));
            return schedule;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan jadwal perkuliahan: " + e.getMessage(), e);
        }
    }

    public void update(Schedule schedule) {
        String sql = """
            UPDATE schedules SET term_id = ?, course_id = ?, class_group_id = ?, lecturer_id = ?,
                                room_id = ?, day_of_week = ?, start_time = ?, end_time = ?,
                                is_active = ?, updated_at = ?
            WHERE id = ?;
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, schedule.getTermId());
            stmt.setLong(2, schedule.getCourseId());
            stmt.setLong(3, schedule.getClassGroupId());
            stmt.setLong(4, schedule.getLecturerId());
            stmt.setLong(5, schedule.getRoomId());
            stmt.setInt(6, schedule.getDayOfWeek());
            stmt.setString(7, schedule.getStartTime().toString());
            stmt.setString(8, schedule.getEndTime().toString());
            stmt.setInt(9, schedule.isActive() ? 1 : 0);
            stmt.setString(10, now);
            stmt.setLong(11, schedule.getId());
            stmt.executeUpdate();
            schedule.setUpdatedAt(LocalDateTime.parse(now));
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui jadwal perkuliahan: " + e.getMessage(), e);
        }
    }

    public void softDelete(Long id) {
        String sql = "UPDATE schedules SET is_active = 0, updated_at = ? WHERE id = ?";
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, now);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menonaktifkan jadwal: " + e.getMessage(), e);
        }
    }

    public void hardDelete(Long id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus jadwal secara permanen: " + e.getMessage(), e);
        }
    }

    private Schedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setId(rs.getLong("id"));
        s.setTermId(rs.getLong("term_id"));
        s.setCourseId(rs.getLong("course_id"));
        s.setClassGroupId(rs.getLong("class_group_id"));
        s.setLecturerId(rs.getLong("lecturer_id"));
        s.setRoomId(rs.getLong("room_id"));
        s.setDayOfWeek(rs.getInt("day_of_week"));
        s.setStartTime(LocalTime.parse(rs.getString("start_time")));
        s.setEndTime(LocalTime.parse(rs.getString("end_time")));
        s.setActive(rs.getInt("is_active") == 1);

        String createdAt = rs.getString("created_at");
        if (createdAt != null) s.setCreatedAt(LocalDateTime.parse(createdAt));
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) s.setUpdatedAt(LocalDateTime.parse(updatedAt));

        // Joined fields
        s.setTermName(rs.getString("term_name"));
        s.setCourseCode(rs.getString("course_code"));
        s.setCourseName(rs.getString("course_name"));
        s.setClassCode(rs.getString("class_code"));
        s.setLecturerName(rs.getString("lecturer_name"));
        s.setRoomCode(rs.getString("room_code"));
        s.setRoomName(rs.getString("room_name"));
        return s;
    }
}
