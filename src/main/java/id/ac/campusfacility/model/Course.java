package id.ac.campusfacility.model;

public class Course {
    private Long id;
    private String courseCode;
    private String courseName;
    private boolean isActive;

    public Course() {
    }

    public Course(Long id, String courseCode, String courseName, boolean isActive) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}
