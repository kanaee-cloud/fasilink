package id.ac.campusfacility.repository;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.model.User;
import id.ac.campusfacility.model.enums.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data user berdasarkan username: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data user berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil daftar user: " + e.getMessage(), e);
        }
        return list;
    }

    public User save(User user) {
        String sql = """
            INSERT INTO users (full_name, username, password_hash, role, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getFullName().trim());
            stmt.setString(2, user.getUsername().trim());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.setInt(5, user.isActive() ? 1 : 0);
            stmt.setString(6, now);
            stmt.setString(7, now);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }
            user.setCreatedAt(LocalDateTime.parse(now));
            user.setUpdatedAt(LocalDateTime.parse(now));
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan user: " + e.getMessage(), e);
        }
    }

    public void update(User user) {
        String sql = """
            UPDATE users SET full_name = ?, username = ?, role = ?, is_active = ?, updated_at = ?
            WHERE id = ?;
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName().trim());
            stmt.setString(2, user.getUsername().trim());
            stmt.setString(3, user.getRole().name());
            stmt.setInt(4, user.isActive() ? 1 : 0);
            stmt.setString(5, now);
            stmt.setLong(6, user.getId());
            stmt.executeUpdate();
            user.setUpdatedAt(LocalDateTime.parse(now));
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui user: " + e.getMessage(), e);
        }
    }

    public void updatePassword(Long userId, String newHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE id = ?";
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setString(2, now);
            stmt.setLong(3, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui password user: " + e.getMessage(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setFullName(rs.getString("full_name"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getInt("is_active") == 1);
        String createdAt = rs.getString("created_at");
        if (createdAt != null) u.setCreatedAt(LocalDateTime.parse(createdAt));
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) u.setUpdatedAt(LocalDateTime.parse(updatedAt));
        return u;
    }
}
