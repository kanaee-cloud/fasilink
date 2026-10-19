package id.ac.campusfacility.service;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.dto.DashboardSummary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardService {

    public DashboardSummary getDashboardSummary() {
        int totalRooms = 0;
        int activeRooms = 0;
        int totalInventoryItems = 0;
        int totalQuantityItems = 0;
        int reservedQuantityItems = 0;
        int totalSchedules = 0;
        int pendingBookings = 0;
        int pendingLoans = 0;

        try (Connection conn = DatabaseManager.getConnection()) {
            // Ruangan
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*), COALESCE(SUM(CASE WHEN is_active=1 THEN 1 ELSE 0 END), 0) FROM rooms");
                 ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    totalRooms = rs.getInt(1);
                    activeRooms = rs.getInt(2);
                }
            }

            // Inventaris
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*), COALESCE(SUM(total_quantity), 0) FROM inventory_items WHERE is_active=1");
                 ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    totalInventoryItems = rs.getInt(1);
                    totalQuantityItems = rs.getInt(2);
                }
            }

            // Peminjaman inventaris aktif
            try (PreparedStatement s = conn.prepareStatement("SELECT COALESCE(SUM(quantity), 0) FROM inventory_loan_requests WHERE status IN ('APPROVED', 'HANDED_OVER')");
                 ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    reservedQuantityItems = rs.getInt(1);
                }
            }

            // Jadwal kuliah aktif
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM schedules WHERE is_active=1");
                 ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    totalSchedules = rs.getInt(1);
                }
            }

            // Pending room bookings
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM room_booking_requests WHERE status = 'PENDING'");
                 ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    pendingBookings = rs.getInt(1);
                }
            }

            // Pending inventory loans
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM inventory_loan_requests WHERE status = 'PENDING'");
                 ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    pendingLoans = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil ringkasan dashboard: " + e.getMessage(), e);
        }

        int availableQuantity = Math.max(0, totalQuantityItems - reservedQuantityItems);

        return new DashboardSummary(
                totalRooms,
                activeRooms,
                totalInventoryItems,
                totalQuantityItems,
                availableQuantity,
                totalSchedules,
                pendingBookings,
                pendingLoans
        );
    }
}
