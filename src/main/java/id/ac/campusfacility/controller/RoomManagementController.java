package id.ac.campusfacility.controller;

import id.ac.campusfacility.model.Room;
import id.ac.campusfacility.service.RoomService;
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

public class RoomManagementController {

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> colRoomCode;
    @FXML private TableColumn<Room, String> colRoomName;
    @FXML private TableColumn<Room, String> colBuilding;
    @FXML private TableColumn<Room, Number> colFloor;
    @FXML private TableColumn<Room, Number> colCapacity;
    @FXML private TableColumn<Room, String> colDescription;
    @FXML private TableColumn<Room, String> colStatus;
    @FXML private TableColumn<Room, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button btnAddRoom;
    @FXML private Label totalRoomsLabel;

    private final RoomService roomService = new RoomService();
    private final ObservableList<Room> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        btnAddRoom.setVisible(isAdmin);
        btnAddRoom.setManaged(isAdmin);
        colActions.setVisible(isAdmin);

        setupColumns();
        setupFilters();
        loadRooms();
    }

    private void setupColumns() {
        colRoomCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomCode()));
        colRoomName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoomName()));
        colBuilding.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBuilding()));
        colFloor.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getFloor()));
        colCapacity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCapacity()));
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDescription() != null ? cell.getValue().getDescription() : "-"
        ));

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
                    if ("Aktif".equals(item)) {
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.getStyleClass().add("badge-danger");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Action Column (Edit & Delete)
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
                    Room room = getTableView().getItems().get(getIndex());
                    handleOpenEditDialog(room);
                });

                btnDelete.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleDeleteRoom(room);
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

        FilteredList<Room> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));

        SortedList<Room> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(roomsTable.comparatorProperty());
        roomsTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Room> filteredData) {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo.getValue();

        filteredData.setPredicate(room -> {
            boolean matchesSearch = query.isEmpty() ||
                    room.getRoomCode().toLowerCase().contains(query) ||
                    room.getRoomName().toLowerCase().contains(query) ||
                    room.getBuilding().toLowerCase().contains(query);

            boolean matchesStatus = true;
            if ("Hanya Aktif".equals(statusFilter)) {
                matchesStatus = room.isActive();
            } else if ("Hanya Nonaktif".equals(statusFilter)) {
                matchesStatus = !room.isActive();
            }

            return matchesSearch && matchesStatus;
        });

        updateSummaryLabel(filteredData.size());
    }

    @FXML
    public void handleRefresh() {
        loadRooms();
    }

    public void loadRooms() {
        totalRoomsLabel.setText("Memuat data ruangan dari database...");
        Task<List<Room>> task = new Task<>() {
            @Override
            protected List<Room> call() {
                return roomService.getAllRooms();
            }
        };

        task.setOnSucceeded(e -> {
            masterData.setAll(task.getValue());
            updateSummaryLabel(masterData.size());
        });

        task.setOnFailed(e -> {
            totalRoomsLabel.setText("Gagal mengambil data ruangan.");
            showError("Error", "Gagal memuat data ruangan dari database: " + task.getException().getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateSummaryLabel(int count) {
        totalRoomsLabel.setText("Menampilkan " + count + " ruangan.");
    }

    @FXML
    public void handleOpenAddDialog() {
        showRoomFormDialog(null);
    }

    private void handleOpenEditDialog(Room room) {
        showRoomFormDialog(room);
    }

    private void showRoomFormDialog(Room existing) {
        boolean isEdit = existing != null;
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Ubah Data Ruangan" : "Tambah Ruangan Baru");
        dialog.setHeaderText(isEdit ? "Perbarui informasi ruangan " + existing.getRoomCode() : "Masukkan detail data ruangan baru");

        ButtonType btnSaveType = new ButtonType("Simpan Data", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField tfCode = new TextField(isEdit ? existing.getRoomCode() : "");
        TextField tfName = new TextField(isEdit ? existing.getRoomName() : "");
        TextField tfBuilding = new TextField(isEdit ? existing.getBuilding() : "");
        Spinner<Integer> spFloor = new Spinner<>(-2, 20, isEdit ? existing.getFloor() : 1);
        Spinner<Integer> spCapacity = new Spinner<>(1, 1000, isEdit ? existing.getCapacity() : 30);
        TextArea taDesc = new TextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        taDesc.setPrefRowCount(3);
        CheckBox cbActive = new CheckBox("Ruangan Aktif (Dapat digunakan)");
        cbActive.setSelected(!isEdit || existing.isActive());

        grid.add(new Label("Kode Ruang:"), 0, 0);
        grid.add(tfCode, 1, 0);
        grid.add(new Label("Nama Ruangan:"), 0, 1);
        grid.add(tfName, 1, 1);
        grid.add(new Label("Gedung:"), 0, 2);
        grid.add(tfBuilding, 1, 2);
        grid.add(new Label("Lantai:"), 0, 3);
        grid.add(spFloor, 1, 3);
        grid.add(new Label("Kapasitas (Kursi):"), 0, 4);
        grid.add(spCapacity, 1, 4);
        grid.add(new Label("Keterangan:"), 0, 5);
        grid.add(taDesc, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(cbActive, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                Room r = isEdit ? existing : new Room();
                r.setRoomCode(tfCode.getText());
                r.setRoomName(tfName.getText());
                r.setBuilding(tfBuilding.getText());
                r.setFloor(spFloor.getValue());
                r.setCapacity(spCapacity.getValue());
                r.setDescription(taDesc.getText());
                r.setActive(cbActive.isSelected());
                return r;
            }
            return null;
        });

        Optional<Room> result = dialog.showAndWait();
        result.ifPresent(room -> {
            try {
                if (isEdit) {
                    roomService.updateRoom(room);
                    showSuccess("Sukses", "Data ruangan " + room.getRoomCode() + " berhasil diperbarui.");
                } else {
                    roomService.createRoom(room);
                    showSuccess("Sukses", "Ruangan baru " + room.getRoomCode() + " berhasil ditambahkan.");
                }
                loadRooms();
            } catch (ValidationException | ConflictException e) {
                showError("Validasi Gagal", e.getMessage());
            } catch (Exception e) {
                showError("Kesalahan Sistem", "Gagal menyimpan ruangan: " + e.getMessage());
            }
        });
    }

    private void handleDeleteRoom(Room room) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Penghapusan Ruangan");
        confirm.setHeaderText("Hapus / Nonaktifkan Ruangan: " + room.getRoomCode());
        confirm.setContentText("Apakah Anda yakin ingin menghapus ruangan ini?\n\n" +
                "Catatan: Jika ruangan ini sudah digunakan dalam jadwal atau peminjaman, " +
                "ruangan akan dinonaktifkan secara aman (soft deactivate) agar riwayat tidak rusak.");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    boolean hardDeleted = roomService.deleteRoom(room.getId());
                    if (hardDeleted) {
                        showSuccess("Sukses", "Ruangan " + room.getRoomCode() + " berhasil dihapus permanen dari sistem.");
                    } else {
                        showSuccess("Informasi", "Ruangan " + room.getRoomCode() + " dinonaktifkan (soft deactivate) karena terikat data jadwal atau peminjaman.");
                    }
                    loadRooms();
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
