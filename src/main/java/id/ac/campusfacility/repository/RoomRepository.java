package id.ac.campusfacility.repository;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.model.Room;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomRepository {

    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY building ASC, floor ASC, room_code ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data seluruh ruangan: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Room> findActive() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE is_active = 1 ORDER BY building ASC, floor ASC, room_code ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data ruangan aktif: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Room> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data ruangan berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Room> findByCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) return Optional.empty();
        String sql = "SELECT * FROM rooms WHERE LOWER(room_code) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomCode.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari ruangan berdasarkan kode: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Room save(Room room) {
        String sql = """
            INSERT INTO rooms (room_code, room_name, building, floor, capacity, description, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, room.getRoomCode().trim().toUpperCase());
            stmt.setString(2, room.getRoomName().trim());
            stmt.setString(3, room.getBuilding().trim());
            stmt.setInt(4, room.getFloor());
            stmt.setInt(5, room.getCapacity());
            stmt.setString(6, room.getDescription() != null ? room.getDescription().trim() : null);
            stmt.setInt(7, room.isActive() ? 1 : 0);
            stmt.setString(8, now);
            stmt.setString(9, now);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.setId(generatedKeys.getLong(1));
                }
            }
            room.setCreatedAt(LocalDateTime.parse(now));
            room.setUpdatedAt(LocalDateTime.parse(now));
            return room;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan data ruangan: " + e.getMessage(), e);
        }
    }

    public void update(Room room) {
        String sql = """
            UPDATE rooms SET room_code = ?, room_name = ?, building = ?, floor = ?,
                             capacity = ?, description = ?, is_active = ?, updated_at = ?
            WHERE id = ?;
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomCode().trim().toUpperCase());
            stmt.setString(2, room.getRoomName().trim());
            stmt.setString(3, room.getBuilding().trim());
            stmt.setInt(4, room.getFloor());
            stmt.setInt(5, room.getCapacity());
            stmt.setString(6, room.getDescription() != null ? room.getDescription().trim() : null);
            stmt.setInt(7, room.isActive() ? 1 : 0);
            stmt.setString(8, now);
            stmt.setLong(9, room.getId());
            stmt.executeUpdate();
            room.setUpdatedAt(LocalDateTime.parse(now));
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui data ruangan: " + e.getMessage(), e);
        }
    }

    public void softDelete(Long id) {
        String sql = "UPDATE rooms SET is_active = 0, updated_at = ? WHERE id = ?";
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, now);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menonaktifkan ruangan: " + e.getMessage(), e);
        }
    }

    public void hardDelete(Long id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus ruangan secara permanen: " + e.getMessage(), e);
        }
    }

    public boolean isReferenced(Long roomId) {
        if (roomId == null) return false;
        String sqlSchedule = "SELECT COUNT(*) FROM schedules WHERE room_id = ?";
        String sqlBooking = "SELECT COUNT(*) FROM room_booking_requests WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlSchedule)) {
                stmt.setLong(1, roomId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlBooking)) {
                stmt.setLong(1, roomId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memeriksa referensi ruangan: " + e.getMessage(), e);
        }
        return false;
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getLong("id"));
        r.setRoomCode(rs.getString("room_code"));
        r.setRoomName(rs.getString("room_name"));
        r.setBuilding(rs.getString("building"));
        r.setFloor(rs.getInt("floor"));
        r.setCapacity(rs.getInt("capacity"));
        r.setDescription(rs.getString("description"));
        r.setActive(rs.getInt("is_active") == 1);
        String createdAt = rs.getString("created_at");
        if (createdAt != null) r.setCreatedAt(LocalDateTime.parse(createdAt));
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) r.setUpdatedAt(LocalDateTime.parse(updatedAt));
        return r;
    }
}
