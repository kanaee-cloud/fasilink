package id.ac.campusfacility.model;

public class Lecturer {
    private Long id;
    private String lecturerCode;
    private String fullName;
    private String email;
    private boolean isActive;

    public Lecturer() {
    }

    public Lecturer(Long id, String lecturerCode, String fullName, String email, boolean isActive) {
        this.id = id;
        this.lecturerCode = lecturerCode;
        this.fullName = fullName;
        this.email = email;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLecturerCode() {
        return lecturerCode;
    }

    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return lecturerCode + " - " + fullName;
    }
}
