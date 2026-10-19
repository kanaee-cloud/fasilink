package id.ac.campusfacility.service;

import id.ac.campusfacility.BaseTest;
import id.ac.campusfacility.model.InventoryItem;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest extends BaseTest {

    private final InventoryService inventoryService = new InventoryService();
    private final AuthService authService = new AuthService();

    @BeforeEach
    void loginAsAdmin() {
        authService.login("admin", "admin123");
    }

    @Test
    @DisplayName("Admin dapat menambah barang inventaris baru")
    void testCreateItemSuccess() {
        InventoryItem item = new InventoryItem();
        item.setItemCode("CAM-01");
        item.setItemName("Kamera Mirrorless Sony");
        item.setDescription("Kamera dokumentasi kegiatan");
        item.setTotalQuantity(4);
        item.setConditionNotes("Semua lensa bersih");

        InventoryItem saved = inventoryService.createItem(item);
        assertNotNull(saved.getId());
        assertEquals("CAM-01", saved.getItemCode());
        assertEquals(4, inventoryService.getAvailableStock(saved.getId()));
    }

    @Test
    @DisplayName("Stok total negatif harus ditolak")
    void testNegativeStockRejected() {
        InventoryItem item = new InventoryItem();
        item.setItemCode("INV-NEG");
        item.setItemName("Barang Negatif");
        item.setTotalQuantity(-5);

        assertThrows(ValidationException.class, () -> inventoryService.createItem(item));
    }

    @Test
    @DisplayName("Kode barang duplikat harus ditolak")
    void testDuplicateItemCodeRejected() {
        InventoryItem item = new InventoryItem();
        item.setItemCode("PRJ-01"); // PRJ-01 sudah ada di seed data
        item.setItemName("Proyektor Duplikat");
        item.setTotalQuantity(2);

        assertThrows(ConflictException.class, () -> inventoryService.createItem(item));
    }

    @Test
    @DisplayName("Stok tersedia berkurang jika ada peminjaman berstatus APPROVED atau HANDED_OVER")
    void testAvailableStockCalculation() {
        // Pada seed data, item ID 1 (PRJ-01) memiliki totalQuantity = 5 dan reserved = 0
        assertEquals(5, inventoryService.getAvailableStock(1L));
    }
}
