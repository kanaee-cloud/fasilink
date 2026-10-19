package id.ac.campusfacility.controller;

import id.ac.campusfacility.model.*;
import id.ac.campusfacility.repository.AcademicDataRepository;
import id.ac.campusfacility.service.RoomService;
import id.ac.campusfacility.service.ScheduleService;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.ValidationException;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class CampusScheduleController {

    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> colDay;
    @FXML private TableColumn<Schedule, String> colTime;
    @FXML private TableColumn<Schedule, String> colCourseCode;
    @FXML private TableColumn<Schedule, String> colCourseName;
    @FXML private TableColumn<Schedule, String> colClass;
    @FXML private TableColumn<Schedule, String> colLecturer;
    @FXML private TableColumn<Schedule, String> colRoom;
    @FXML private TableColumn<Schedule, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> dayFilterCombo;
    @FXML private ComboBox<String> roomFilterCombo;
    @FXML private Button btnAddSchedule;
    @FXML private Label summaryLabel;

    private final ScheduleService scheduleService = new ScheduleService();
    private final RoomService roomService = new RoomService();
    private final AcademicDataRepository academicRepo = new AcademicDataRepository();

    private final ObservableList<Schedule> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        btnAddSchedule.setVisible(isAdmin);
        btnAddSchedule.setManaged(isAdmin);
        colActions.setVisible(isAdmin);

        setupColumns();
        setupFilters();
        loadSchedules();
    }

    private void setupColumns() {
        colDay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDayNameIndonesian()));
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTimeRangeFormatted()));
        colCourseCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseCode()));
        colCourseName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseName()));
        colClass.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getClassCode()));
        colLecturer.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLecturerName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getRoomCode() + " - " + cell.getValue().getRoomName()
        ));

        // Action Column: Delete Schedule (Admin Only)
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnDelete = new Button("Hapus");
            {
                btnDelete.getStyleClass().add("btn-danger");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                btnDelete.setOnAction(e -> {
                    Schedule s = getTableView().getItems().get(getIndex());
                    handleDeleteSchedule(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    private void setupFilters() {
        dayFilterCombo.getItems().setAll(
                "Semua Hari", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
        );
        dayFilterCombo.setValue("Semua Hari");

        roomFilterCombo.getItems().setAll("Semua Ruangan");
        for (Room r : roomService.getActiveRooms()) {
            roomFilterCombo.getItems().add(r.getRoomCode());
        }
        roomFilterCombo.setValue("Semua Ruangan");

        FilteredList<Schedule> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        dayFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));
        roomFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(filteredData));

        SortedList<Schedule> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(scheduleTable.comparatorProperty());
        scheduleTable.setItems(sortedData);
    }

    private void applyFilter(FilteredList<Schedule> filteredData) {
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String selectedDay = dayFilterCombo.getValue();
        String selectedRoom = roomFilterCombo.getValue();

        filteredData.setPredicate(s -> {
            boolean matchesSearch = query.isEmpty() ||
                    (s.getCourseName() != null && s.getCourseName().toLowerCase().contains(query)) ||
                    (s.getCourseCode() != null && s.getCourseCode().toLowerCase().contains(query)) ||
                    (s.getLecturerName() != null && s.getLecturerName().toLowerCase().contains(query)) ||
                    (s.getClassCode() != null && s.getClassCode().toLowerCase().contains(query));

            boolean matchesDay = "Semua Hari".equals(selectedDay) ||
                    (s.getDayNameIndonesian() != null && s.getDayNameIndonesian().equalsIgnoreCase(selectedDay));

            boolean matchesRoom = "Semua Ruangan".equals(selectedRoom) ||
                    (s.getRoomCode() != null && s.getRoomCode().equalsIgnoreCase(selectedRoom));

            return matchesSearch && matchesDay && matchesRoom;
        });

        updateSummaryLabel(filteredData.size());
    }

    @FXML
    public void handleRefresh() {
        loadSchedules();
    }

    public void loadSchedules() {
        summaryLabel.setText("Memuat jadwal perkuliahan dari database...");
        Task<List<Schedule>> task = new Task<>() {
            @Override
            protected List<Schedule> call() {
                return scheduleService.getAllActiveSchedules();
            }
        };

        task.setOnSucceeded(e -> {
            masterData.setAll(task.getValue());
            updateSummaryLabel(masterData.size());
        });

        task.setOnFailed(e -> {
            summaryLabel.setText("Gagal mengambil data jadwal.");
            showError("Error", "Gagal memuat jadwal dari database: " + task.getException().getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateSummaryLabel(int count) {
        summaryLabel.setText("Menampilkan " + count + " sesi perkuliahan aktif.");
    }

    @FXML
    public void handleOpenAddDialog() {
        Optional<AcademicTerm> termOpt = academicRepo.getActiveTerm();
        if (termOpt.isEmpty()) {
            showError("Tidak Ada Semester Aktif", "Belum ada semester aktif yang dikonfigurasi di sistem.");
            return;
        }

        AcademicTerm activeTerm = termOpt.get();
        List<Course> courses = academicRepo.findAllActiveCourses();
        List<ClassGroup> classGroups = academicRepo.findAllActiveClassGroups();
        List<Lecturer> lecturers = academicRepo.findAllActiveLecturers();
        List<Room> rooms = roomService.getActiveRooms();

        if (courses.isEmpty() || classGroups.isEmpty() || lecturers.isEmpty() || rooms.isEmpty()) {
            showError("Data Dasar Belum Lengkap", "Pastikan master data mata kuliah, kelas, dosen, dan ruangan sudah tersedia sebelum membuat jadwal.");
            return;
        }

        Dialog<Schedule> dialog = new Dialog<>();
        dialog.setTitle("Tambah Jadwal Perkuliahan");
        dialog.setHeaderText("Buat jadwal kuliah baru pada " + activeTerm.getTermName());

        ButtonType btnSaveType = new ButtonType("Simpan Jadwal", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSaveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));

        ComboBox<Course> cbCourse = new ComboBox<>(FXCollections.observableArrayList(courses));
        cbCourse.getSelectionModel().selectFirst();
        cbCourse.setMaxWidth(Double.MAX_VALUE);

        ComboBox<ClassGroup> cbClass = new ComboBox<>(FXCollections.observableArrayList(classGroups));
        cbClass.getSelectionModel().selectFirst();
        cbClass.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Lecturer> cbLecturer = new ComboBox<>(FXCollections.observableArrayList(lecturers));
        cbLecturer.getSelectionModel().selectFirst();
        cbLecturer.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Room> cbRoom = new ComboBox<>(FXCollections.observableArrayList(rooms));
        cbRoom.getSelectionModel().selectFirst();
        cbRoom.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbDay = new ComboBox<>(FXCollections.observableArrayList(
                "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
        ));
        cbDay.getSelectionModel().selectFirst();
        cbDay.setMaxWidth(Double.MAX_VALUE);

        TextField tfStart = new TextField("08:00");
        TextField tfEnd = new TextField("10:30");

        grid.add(new Label("Semester:"), 0, 0);
        grid.add(new Label(activeTerm.getTermName()), 1, 0);
        grid.add(new Label("Mata Kuliah:"), 0, 1);
        grid.add(cbCourse, 1, 1);
        grid.add(new Label("Kelompok Kelas:"), 0, 2);
        grid.add(cbClass, 1, 2);
        grid.add(new Label("Dosen Pengampu:"), 0, 3);
        grid.add(cbLecturer, 1, 3);
        grid.add(new Label("Ruangan:"), 0, 4);
        grid.add(cbRoom, 1, 4);
        grid.add(new Label("Hari:"), 0, 5);
        grid.add(cbDay, 1, 5);
        grid.add(new Label("Jam Mulai (HH:mm):"), 0, 6);
        grid.add(tfStart, 1, 6);
        grid.add(new Label("Jam Selesai (HH:mm):"), 0, 7);
        grid.add(tfEnd, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSaveType) {
                try {
                    Schedule s = new Schedule();
                    s.setTermId(activeTerm.getId());
                    s.setCourseId(cbCourse.getValue().getId());
                    s.setClassGroupId(cbClass.getValue().getId());
                    s.setLecturerId(cbLecturer.getValue().getId());
                    s.setRoomId(cbRoom.getValue().getId());
                    s.setDayOfWeek(cbDay.getSelectionModel().getSelectedIndex() + 1);
                    s.setStartTime(LocalTime.parse(tfStart.getText().trim()));
                    s.setEndTime(LocalTime.parse(tfEnd.getText().trim()));
                    s.setActive(true);
                    return s;
                } catch (Exception ex) {
                    showError("Format Waktu Salah", "Gunakan format jam HH:mm (contoh: 08:00 dan 10:30).");
                    return null;
                }
            }
            return null;
        });

        Optional<Schedule> result = dialog.showAndWait();
        result.ifPresent(schedule -> {
            try {
                scheduleService.createSchedule(schedule);
                showSuccess("Jadwal Berhasil Disimpan", "Jadwal perkuliahan baru telah berhasil ditambahkan ke sistem.");
                loadSchedules();
            } catch (ValidationException | ConflictException e) {
                showError("Validasi Bentrok / Jadwal Ditolak", e.getMessage());
            } catch (Exception e) {
                showError("Kesalahan Sistem", "Gagal menyimpan jadwal: " + e.getMessage());
            }
        });
    }

    private void handleDeleteSchedule(Schedule schedule) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus Jadwal");
        alert.setHeaderText("Hapus Jadwal Kuliah: " + schedule.getCourseName());
        alert.setContentText("Apakah Anda yakin ingin menonaktifkan jadwal ini?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    scheduleService.deleteSchedule(schedule.getId());
                    showSuccess("Sukses", "Jadwal perkuliahan berhasil dihapus.");
                    loadSchedules();
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
