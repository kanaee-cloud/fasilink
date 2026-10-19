package id.ac.campusfacility.service;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.model.Room;
import id.ac.campusfacility.repository.RoomRepository;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.NotFoundException;
import id.ac.campusfacility.validation.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService() {
        this.roomRepository = new RoomRepository();
    }

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findActive();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ruangan dengan ID " + id + " tidak ditemukan."));
    }

    public Room createRoom(Room room) {
        UserSession.getInstance().requireAdmin();
        validateRoomInput(room);

        // Cek duplikasi kode ruangan
        Optional<Room> existing = roomRepository.findByCode(room.getRoomCode());
        if (existing.isPresent()) {
            throw new ConflictException("Kode ruangan '" + room.getRoomCode() + "' sudah digunakan.");
        }

        room.setActive(true);
        return roomRepository.save(room);
    }

    public void updateRoom(Room room) {
        UserSession.getInstance().requireAdmin();
        if (room.getId() == null) {
            throw new ValidationException("ID ruangan wajib disertakan untuk pembaruan data.");
        }
        validateRoomInput(room);

        Room existing = getRoomById(room.getId());
        if (!existing.getRoomCode().equalsIgnoreCase(room.getRoomCode().trim())) {
            Optional<Room> duplicate = roomRepository.findByCode(room.getRoomCode());
            if (duplicate.isPresent()) {
                throw new ConflictException("Kode ruangan '" + room.getRoomCode() + "' sudah digunakan oleh ruangan lain.");
            }
        }

        roomRepository.update(room);
    }

    public boolean deleteRoom(Long id) {
        UserSession.getInstance().requireAdmin();
        Room room = getRoomById(id);

        if (roomRepository.isReferenced(id)) {
            // Jika sudah ada jadwal atau peminjaman yang mereferensikan, lakukan soft deactivate
            roomRepository.softDelete(id);
            return false; // Ditandai soft-deactivate
        } else {
            // Jika belum direferensikan siapapun, dapat dihapus bersih
            roomRepository.hardDelete(id);
            return true; // Hard deleted
        }
    }

    /**
     * Memeriksa ketersediaan ruangan pada tanggal dan rentang jam tertentu.
     * Dapat digunakan langsung oleh modul peminjaman milik Arsal.
     */
    public boolean isRoomAvailable(Long roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (roomId == null || date == null || startTime == null || endTime == null) {
            return false;
        }
        if (!startTime.isBefore(endTime)) {
            return false;
        }

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || !room.isActive()) {
            return false;
        }

        // 1. Cek bentrok dengan jadwal perkuliahan berulang pada hari tersebut
        int dayOfWeek = date.getDayOfWeek().getValue(); // ISO 1=Monday
        String scheduleSql = """
            SELECT COUNT(*) FROM schedules
            WHERE room_id = ? AND day_of_week = ? AND is_active = 1
            AND ? < end_time AND ? > start_time;
        """;

        // 2. Cek bentrok dengan peminjaman ruangan yang telah APPROVED
        String bookingSql = """
            SELECT COUNT(*) FROM room_booking_requests
            WHERE room_id = ? AND booking_date = ? AND status = 'APPROVED'
            AND ? < end_time AND ? > start_time;
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(scheduleSql)) {
                stmt.setLong(1, roomId);
                stmt.setInt(2, dayOfWeek);
                stmt.setString(3, startTime.toString());
                stmt.setString(4, endTime.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Bentrok jadwal kuliah
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(bookingSql)) {
                stmt.setLong(1, roomId);
                stmt.setString(2, date.toString());
                stmt.setString(3, startTime.toString());
                stmt.setString(4, endTime.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Bentrok booking lain
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memeriksa ketersediaan ruangan: " + e.getMessage(), e);
        }

        return true;
    }

    private void validateRoomInput(Room room) {
        if (room == null) {
            throw new ValidationException("Data ruangan tidak boleh kosong.");
        }
        if (room.getRoomCode() == null || room.getRoomCode().trim().isBlank()) {
            throw new ValidationException("Kode ruangan wajib diisi.");
        }
        if (room.getRoomName() == null || room.getRoomName().trim().isBlank()) {
            throw new ValidationException("Nama ruangan wajib diisi.");
        }
        if (room.getBuilding() == null || room.getBuilding().trim().isBlank()) {
            throw new ValidationException("Gedung wajib diisi.");
        }
        if (room.getFloor() == null) {
            throw new ValidationException("Lantai ruangan wajib diisi.");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new ValidationException("Kapasitas ruangan harus berupa bilangan positif lebih dari nol.");
        }
    }
}
