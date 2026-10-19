package id.ac.campusfacility.service;

import id.ac.campusfacility.model.InventoryItem;
import id.ac.campusfacility.repository.InventoryRepository;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.NotFoundException;
import id.ac.campusfacility.validation.ValidationException;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService() {
        this.inventoryRepository = new InventoryRepository();
    }

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<InventoryItem> getAllItems() {
        List<InventoryItem> items = inventoryRepository.findAll();
        for (InventoryItem item : items) {
            item.setAvailableQuantity(getAvailableStock(item.getId()));
        }
        return items;
    }

    public List<InventoryItem> getActiveItems() {
        List<InventoryItem> items = inventoryRepository.findActive();
        for (InventoryItem item : items) {
            item.setAvailableQuantity(getAvailableStock(item.getId()));
        }
        return items;
    }

    public InventoryItem getItemById(Long id) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Barang dengan ID " + id + " tidak ditemukan."));
        item.setAvailableQuantity(getAvailableStock(item.getId()));
        return item;
    }

    /**
     * Menghitung stok tersedia aktual dari backend:
     * availableQuantity = totalQuantity - sum(quantity berstatus APPROVED dan HANDED_OVER)
     */
    public int getAvailableStock(Long itemId) {
        InventoryItem item = inventoryRepository.findById(itemId).orElse(null);
        if (item == null || !item.isActive()) {
            return 0;
        }
        int reserved = inventoryRepository.getReservedQuantity(itemId);
        return Math.max(0, item.getTotalQuantity() - reserved);
    }

    public InventoryItem createItem(InventoryItem item) {
        UserSession.getInstance().requireAdmin();
        validateItemInput(item);

        Optional<InventoryItem> existing = inventoryRepository.findByCode(item.getItemCode());
        if (existing.isPresent()) {
            throw new ConflictException("Kode barang '" + item.getItemCode() + "' sudah digunakan.");
        }

        item.setActive(true);
        InventoryItem saved = inventoryRepository.save(item);
        saved.setAvailableQuantity(saved.getTotalQuantity());
        return saved;
    }

    public void updateItem(InventoryItem item) {
        UserSession.getInstance().requireAdmin();
        if (item.getId() == null) {
            throw new ValidationException("ID barang wajib disertakan untuk pembaruan data.");
        }
        validateItemInput(item);

        InventoryItem existing = getItemById(item.getId());
        if (!existing.getItemCode().equalsIgnoreCase(item.getItemCode().trim())) {
            Optional<InventoryItem> duplicate = inventoryRepository.findByCode(item.getItemCode());
            if (duplicate.isPresent()) {
                throw new ConflictException("Kode barang '" + item.getItemCode() + "' sudah digunakan oleh barang lain.");
            }
        }

        // Pastikan jumlah total baru tidak lebih kecil dari barang yang saat ini sedang dipinjam
        int reserved = inventoryRepository.getReservedQuantity(item.getId());
        if (item.getTotalQuantity() < reserved) {
            throw new ValidationException("Jumlah total tidak boleh kurang dari unit yang sedang aktif dipinjam (" + reserved + " unit).");
        }

        inventoryRepository.update(item);
    }

    public boolean deleteItem(Long id) {
        UserSession.getInstance().requireAdmin();
        InventoryItem item = getItemById(id);

        int reserved = inventoryRepository.getReservedQuantity(id);
        if (reserved > 0) {
            throw new ConflictException("Barang '" + item.getItemName() + "' sedang dalam peminjaman aktif (" + reserved + " unit). Tidak dapat dinonaktifkan.");
        }

        if (inventoryRepository.isReferenced(id)) {
            // Jika ada riwayat transaksi masa lalu, gunakan soft delete
            inventoryRepository.softDelete(id);
            return false;
        } else {
            inventoryRepository.hardDelete(id);
            return true;
        }
    }

    private void validateItemInput(InventoryItem item) {
        if (item == null) {
            throw new ValidationException("Data inventaris tidak boleh kosong.");
        }
        if (item.getItemCode() == null || item.getItemCode().trim().isBlank()) {
            throw new ValidationException("Kode barang wajib diisi.");
        }
        if (item.getItemName() == null || item.getItemName().trim().isBlank()) {
            throw new ValidationException("Nama barang wajib diisi.");
        }
        if (item.getTotalQuantity() == null || item.getTotalQuantity() < 0) {
            throw new ValidationException("Jumlah stok barang tidak boleh bernilai negatif.");
        }
    }
}
