package id.ac.campusfacility.repository;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.model.InventoryItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryRepository {

    public List<InventoryItem> findAll() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items ORDER BY item_name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data seluruh inventaris: " + e.getMessage(), e);
        }
        return list;
    }

    public List<InventoryItem> findActive() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE is_active = 1 ORDER BY item_name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data inventaris aktif: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<InventoryItem> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM inventory_items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data barang berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<InventoryItem> findByCode(String itemCode) {
        if (itemCode == null || itemCode.isBlank()) return Optional.empty();
        String sql = "SELECT * FROM inventory_items WHERE LOWER(item_code) = LOWER(?) LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemCode.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari barang berdasarkan kode: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public InventoryItem save(InventoryItem item) {
        String sql = """
            INSERT INTO inventory_items (item_code, item_name, description, total_quantity, condition_notes, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getItemCode().trim().toUpperCase());
            stmt.setString(2, item.getItemName().trim());
            stmt.setString(3, item.getDescription() != null ? item.getDescription().trim() : null);
            stmt.setInt(4, item.getTotalQuantity());
            stmt.setString(5, item.getConditionNotes() != null ? item.getConditionNotes().trim() : null);
            stmt.setInt(6, item.isActive() ? 1 : 0);
            stmt.setString(7, now);
            stmt.setString(8, now);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getLong(1));
                }
            }
            item.setCreatedAt(LocalDateTime.parse(now));
            item.setUpdatedAt(LocalDateTime.parse(now));
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan data inventaris: " + e.getMessage(), e);
        }
    }

    public void update(InventoryItem item) {
        String sql = """
            UPDATE inventory_items SET item_code = ?, item_name = ?, description = ?,
                                      total_quantity = ?, condition_notes = ?, is_active = ?, updated_at = ?
            WHERE id = ?;
        """;
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getItemCode().trim().toUpperCase());
            stmt.setString(2, item.getItemName().trim());
            stmt.setString(3, item.getDescription() != null ? item.getDescription().trim() : null);
            stmt.setInt(4, item.getTotalQuantity());
            stmt.setString(5, item.getConditionNotes() != null ? item.getConditionNotes().trim() : null);
            stmt.setInt(6, item.isActive() ? 1 : 0);
            stmt.setString(7, now);
            stmt.setLong(8, item.getId());
            stmt.executeUpdate();
            item.setUpdatedAt(LocalDateTime.parse(now));
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui data inventaris: " + e.getMessage(), e);
        }
    }

    public void softDelete(Long id) {
        String sql = "UPDATE inventory_items SET is_active = 0, updated_at = ? WHERE id = ?";
        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, now);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menonaktifkan barang inventaris: " + e.getMessage(), e);
        }
    }

    public void hardDelete(Long id) {
        String sql = "DELETE FROM inventory_items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus barang inventaris secara permanen: " + e.getMessage(), e);
        }
    }

    public boolean isReferenced(Long itemId) {
        if (itemId == null) return false;
        String sql = "SELECT COUNT(*) FROM inventory_loan_requests WHERE item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memeriksa referensi peminjaman inventaris: " + e.getMessage(), e);
        }
        return false;
    }

    public int getReservedQuantity(Long itemId) {
        if (itemId == null) return 0;
        String sql = """
            SELECT COALESCE(SUM(quantity), 0) FROM inventory_loan_requests
            WHERE item_id = ? AND status IN ('APPROVED', 'HANDED_OVER');
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung kuantitas inventaris yang dipinjam: " + e.getMessage(), e);
        }
        return 0;
    }

    private InventoryItem mapResultSetToItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getLong("id"));
        item.setItemCode(rs.getString("item_code"));
        item.setItemName(rs.getString("item_name"));
        item.setDescription(rs.getString("description"));
        item.setTotalQuantity(rs.getInt("total_quantity"));
        item.setConditionNotes(rs.getString("condition_notes"));
        item.setActive(rs.getInt("is_active") == 1);
        String createdAt = rs.getString("created_at");
        if (createdAt != null) item.setCreatedAt(LocalDateTime.parse(createdAt));
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) item.setUpdatedAt(LocalDateTime.parse(updatedAt));
        return item;
    }
}
