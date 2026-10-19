package id.ac.campusfacility.service;

import id.ac.campusfacility.dto.ScheduleConflict;
import id.ac.campusfacility.model.Schedule;
import id.ac.campusfacility.repository.ScheduleRepository;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ConflictException;
import id.ac.campusfacility.validation.NotFoundException;
import id.ac.campusfacility.validation.ValidationException;
import java.util.List;

public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleConflictService conflictService;

    public ScheduleService() {
        this.scheduleRepository = new ScheduleRepository();
        this.conflictService = new ScheduleConflictService();
    }

    public ScheduleService(ScheduleRepository scheduleRepository, ScheduleConflictService conflictService) {
        this.scheduleRepository = scheduleRepository;
        this.conflictService = conflictService;
    }

    public List<Schedule> getAllActiveSchedules() {
        return scheduleRepository.findAllActive();
    }

    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Jadwal dengan ID " + id + " tidak ditemukan."));
    }

    public List<Schedule> searchSchedules(Long termId, Integer dayOfWeek, Long roomId, Long courseId) {
        return scheduleRepository.findByFilters(termId, dayOfWeek, roomId, courseId);
    }

    public Schedule createSchedule(Schedule schedule) {
        UserSession.getInstance().requireAdmin();
        validateScheduleInput(schedule);

        // Periksa bentrok
        List<ScheduleConflict> conflicts = conflictService.checkConflicts(
                schedule.getTermId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoomId(),
                schedule.getLecturerId(),
                schedule.getClassGroupId(),
                null
        );

        if (!conflicts.isEmpty()) {
            StringBuilder sb = new StringBuilder("Jadwal tidak dapat disimpan karena terjadi konflik:\n");
            for (ScheduleConflict c : conflicts) {
                sb.append("- ").append(c.getMessage()).append("\n");
            }
            throw new ConflictException(sb.toString().trim());
        }

        schedule.setActive(true);
        return scheduleRepository.save(schedule);
    }

    public void updateSchedule(Schedule schedule) {
        UserSession.getInstance().requireAdmin();
        if (schedule.getId() == null) {
            throw new ValidationException("ID jadwal wajib disertakan untuk pembaruan data.");
        }
        validateScheduleInput(schedule);

        // Periksa bentrok dengan mengecualikan dirinya sendiri
        List<ScheduleConflict> conflicts = conflictService.checkConflicts(
                schedule.getTermId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoomId(),
                schedule.getLecturerId(),
                schedule.getClassGroupId(),
                schedule.getId()
        );

        if (!conflicts.isEmpty()) {
            StringBuilder sb = new StringBuilder("Pembaruan jadwal ditolak karena konflik:\n");
            for (ScheduleConflict c : conflicts) {
                sb.append("- ").append(c.getMessage()).append("\n");
            }
            throw new ConflictException(sb.toString().trim());
        }

        scheduleRepository.update(schedule);
    }

    public void deleteSchedule(Long id) {
        UserSession.getInstance().requireAdmin();
        Schedule schedule = getScheduleById(id);
        scheduleRepository.softDelete(id);
    }

    private void validateScheduleInput(Schedule s) {
        if (s == null) {
            throw new ValidationException("Data jadwal tidak boleh kosong.");
        }
        if (s.getTermId() == null) {
            throw new ValidationException("Semester wajib dipilih.");
        }
        if (s.getCourseId() == null) {
            throw new ValidationException("Mata kuliah wajib dipilih.");
        }
        if (s.getClassGroupId() == null) {
            throw new ValidationException("Kelompok kelas wajib dipilih.");
        }
        if (s.getLecturerId() == null) {
            throw new ValidationException("Dosen pengampu wajib dipilih.");
        }
        if (s.getRoomId() == null) {
            throw new ValidationException("Ruangan kuliah wajib dipilih.");
        }
        if (s.getDayOfWeek() < 1 || s.getDayOfWeek() > 7) {
            throw new ValidationException("Hari tidak valid (pilih hari 1 sampai 7).");
        }
        if (s.getStartTime() == null || s.getEndTime() == null) {
            throw new ValidationException("Jam mulai dan jam selesai wajib diisi.");
        }
        if (!s.getStartTime().isBefore(s.getEndTime())) {
            throw new ValidationException("Jam mulai harus lebih awal daripada jam selesai.");
        }
    }
}
