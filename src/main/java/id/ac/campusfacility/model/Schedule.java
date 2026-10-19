package id.ac.campusfacility.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class Schedule {
    private Long id;
    private Long termId;
    private Long courseId;
    private Long classGroupId;
    private Long lecturerId;
    private Long roomId;
    private int dayOfWeek; // 1 = Monday, 7 = Sunday (ISO)
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Display fields for UI TableView / joined query
    private String termName;
    private String courseCode;
    private String courseName;
    private String classCode;
    private String lecturerName;
    private String roomCode;
    private String roomName;

    public Schedule() {
    }

    public Schedule(Long id, Long termId, Long courseId, Long classGroupId, Long lecturerId,
                    Long roomId, int dayOfWeek, LocalTime startTime, LocalTime endTime,
                    boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.termId = termId;
        this.courseId = courseId;
        this.classGroupId = classGroupId;
        this.lecturerId = lecturerId;
        this.roomId = roomId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getClassGroupId() {
        return classGroupId;
    }

    public void setClassGroupId(Long classGroupId) {
        this.classGroupId = classGroupId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayNameIndonesian() {
        return switch (dayOfWeek) {
            case 1 -> "Senin";
            case 2 -> "Selasa";
            case 3 -> "Rabu";
            case 4 -> "Kamis";
            case 5 -> "Jumat";
            case 6 -> "Sabtu";
            case 7 -> "Minggu";
            default -> "Hari-" + dayOfWeek;
        };
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeRangeFormatted() {
        if (startTime == null || endTime == null) return "-";
        return startTime + " - " + endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
