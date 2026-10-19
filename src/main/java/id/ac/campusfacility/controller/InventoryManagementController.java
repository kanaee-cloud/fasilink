package id.ac.campusfacility.controller;

import id.ac.campusfacility.model.InventoryItem;
import id.ac.campusfacility.service.InventoryService;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class InventoryManagementController {

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colItemCode;
    @FXML private TableColumn<InventoryItem, String> colItemName;
    @FXML private TableColumn<InventoryItem, String> colDescription;
    @FXML private TableColumn<InventoryItem, Number> colTotalQuantity;
    @FXML private TableColumn<InventoryItem, Number> colAvailableQuantity;
    @FXML private TableColumn<InventoryItem, String> colCondition;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button btnAddItem;
    @FXML private Label summaryLabel;

    private final InventoryService inventoryService = new InventoryService();
    private final ObservableList<InventoryItem> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        btnAddItem.setVisible(isAdmin);
        btnAddItem.setManaged(isAdmin);
        colActions.setVisible(isAdmin);

        setupColumns();
        setupFilters();
        loadInventory();
    }

    private void setupColumns() {
        colItemCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemCode()));
        colItemName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItemName()));
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDescription() != null ? cell.getValue().getDescription() : "-"
        ));
        colTotalQuantity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTotalQuantity()));
        colAvailableQuantity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getAvailableQuantity()));
        colCondition.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getConditionNotes() != null ? cell.getValue().getConditionNotes() : "-"
        ));

        // Available quantity highlight
        colAvailableQuantity.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int val = item.intValue();
                    Label badge = new Label(val + " Unit");
                    badge.getStyleClass().add("badge");
                    if (val > 0) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-danger");
                    }
                    setGraphic(badge);
                }
            }
        });

        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().isActive() ? "Aktif" : "Nonaktif"
        ));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    badge.getStyleClass().add("Aktif".equals(item) ? "badge-info" : "badge-danger");
                    setGraphic(badge);
                }
            }
        });

        // Action Column: Edit & Delete (Admin Only)
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Ubah");
            private final Button btnDelete = new Button("Hapus");
            private final HBox pane = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                btnDelete.getStyleClass().add("btn-danger");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");

                btnEdit.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleOpenEditDialog(item);
                });

                btnDelete.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFilters() {
        statusFilterCombo.getItems().setAll("Semua Status", "Hanya Aktif", "Hanya Nonaktif");
        statusFilterCombo.setValue("Semua Status");

        FilteredList<InventoryItem> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));

        SortedList<InventoryItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<InventoryItem> filteredData) {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo.getValue();

        filteredData.setPredicate(item -> {
            boolean matchesSearch = query.isEmpty() ||
                    item.getItemCode().toLowerCase().contains(query) ||
                    item.getItemName().toLowerCase().contains(query);

            boolean matchesStatus = true;
            if ("Hanya Aktif".equals(statusFilter)) {
                matchesStatus = item.isActive();
            } else if ("Hanya Nonaktif".equals(statusFilter)) {
                matchesStatus = !item.isActive();
            }

            return matchesSearch && matchesStatus;
        });

        updateSummaryLabel(filteredData.size());
    }

    @FXML
    public void handleRefresh() {
        loadInventory();
    }

    public void loadInventory() {
        summaryLabel.setText("Memuat data inventaris dan menghitung stok tersedia...");
        Task<List<InventoryItem>> task = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                return inventoryService.getAllItems();
            }
        };

        task.setOnSucceeded(e -> {
            masterData.setAll(task.getValue());
            updateSummaryLabel(masterData.size());
        });

        task.setOnFailed(e -> {
            summaryLabel.setText("Gagal mengambil data inventaris.");
            showError("Error", "Gagal memuat inventaris: " + task.getException().getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateSummaryLabel(int count) {
        int totalUnits = masterData.stream().mapToInt(InventoryItem::getTotalQuantity).sum();
        int availableUnits = masterData.stream().mapToInt(InventoryItem::getAvailableQuantity).sum();
        summaryLabel.setText(String.format("Menampilkan %d jenis barang | Total: %d unit | Siap Dipinjam: %d unit",
                count, totalUnits, availableUnits));
    }

    @FXML
    public void handleOpenAddDialog() {
        showItemFormDialog(null);
    }

    private void handleOpenEditDialog(InventoryItem item) {
        showItemFormDialog(item);
    }

    private void showItemFormDialog(InventoryItem existing) {
        boolean isEdit = existing != null;
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Ubah Data Inventaris" : "Tambah Barang Inventaris Baru");
        dialog.setHeaderText(isEdit ? "Perbarui informasi barang " + existing.getItemCode() : "Masukkan detail barang inventaris baru");

        ButtonType btnSaveType = new ButtonType("Simpan Barang", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField tfCode = new TextField(isEdit ? existing.getItemCode() : "");
        TextField tfName = new TextField(isEdit ? existing.getItemName() : "");
        TextArea taDesc = new TextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        taDesc.setPrefRowCount(3);
        Spinner<Integer> spQuantity = new Spinner<>(0, 10000, isEdit ? existing.getTotalQuantity() : 1);
        TextArea taNotes = new TextArea(isEdit && existing.getConditionNotes() != null ? existing.getConditionNotes() : "");
        taNotes.setPrefRowCount(2);
        CheckBox cbActive = new CheckBox("Barang Aktif (Dapat dipinjam)");
        cbActive.setSelected(!isEdit || existing.isActive());

        grid.add(new Label("Kode Barang:"), 0, 0);
        grid.add(tfCode, 1, 0);
        grid.add(new Label("Nama Barang:"), 0, 1);
        grid.add(tfName, 1, 1);
        grid.add(new Label("Spesifikasi/Deskripsi:"), 0, 2);
        grid.add(taDesc, 1, 2);
        grid.add(new Label("Jumlah Total Unit:"), 0, 3);
        grid.add(spQuantity, 1, 3);
        grid.add(new Label("Catatan Kondisi:"), 0, 4);
        grid.add(taNotes, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(cbActive, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSaveType) {
                InventoryItem item = isEdit ? existing : new InventoryItem();
                item.setItemCode(tfCode.getText());
                item.setItemName(tfName.getText());
                item.setDescription(taDesc.getText());
                item.setTotalQuantity(spQuantity.getValue());
                item.setConditionNotes(taNotes.getText());
                item.setActive(cbActive.isSelected());
                return item;
            }
            return null;
        });

        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            try {
                if (isEdit) {
                    inventoryService.updateItem(item);
                    showSuccess("Sukses", "Data barang " + item.getItemCode() + " berhasil diperbarui.");
                } else {
                    inventoryService.createItem(item);
                    showSuccess("Sukses", "Barang baru " + item.getItemCode() + " berhasil ditambahkan.");
                }
                loadInventory();
            } catch (ValidationException | ConflictException e) {
                showError("Validasi Gagal", e.getMessage());
            } catch (Exception e) {
                showError("Kesalahan Sistem", "Gagal menyimpan barang: " + e.getMessage());
            }
        });
    }

    private void handleDeleteItem(InventoryItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Penghapusan Barang");
        confirm.setHeaderText("Hapus / Nonaktifkan Barang: " + item.getItemName());
        confirm.setContentText("Apakah Anda yakin ingin menghapus barang ini?\n\n" +
                "Jika barang sudah memiliki riwayat peminjaman, barang akan dinonaktifkan secara aman (soft deactivate).");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    boolean hardDeleted = inventoryService.deleteItem(item.getId());
                    if (hardDeleted) {
                        showSuccess("Sukses", "Barang " + item.getItemName() + " berhasil dihapus permanen.");
                    } else {
                        showSuccess("Informasi", "Barang " + item.getItemName() + " dinonaktifkan (soft deactivate) karena terikat data peminjaman.");
                    }
                    loadInventory();
                } catch (Exception e) {
                    showError("Gagal Menghapus", e.getMessage());
                }
            }
        });
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
