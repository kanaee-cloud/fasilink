package id.ac.campusfacility.model;

public class ClassGroup {
    private Long id;
    private String studyProgram;
    private Integer cohortYear;
    private String classCode;
    private Integer studentCount;
    private boolean isActive;

    public ClassGroup() {
    }

    public ClassGroup(Long id, String studyProgram, Integer cohortYear, String classCode,
                      Integer studentCount, boolean isActive) {
        this.id = id;
        this.studyProgram = studyProgram;
        this.cohortYear = cohortYear;
        this.classCode = classCode;
        this.studentCount = studentCount;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudyProgram() {
        return studyProgram;
    }

    public void setStudyProgram(String studyProgram) {
        this.studyProgram = studyProgram;
    }

    public Integer getCohortYear() {
        return cohortYear;
    }

    public void setCohortYear(Integer cohortYear) {
        this.cohortYear = cohortYear;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDisplayName() {
        return classCode + " (" + studyProgram + " " + cohortYear + (studentCount != null ? ", " + studentCount + " mhs" : "") + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
