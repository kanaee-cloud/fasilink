package id.ac.campusfacility.model;

import java.time.LocalDate;

public class AcademicTerm {
    private Long id;
    private String termName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;

    public AcademicTerm() {
    }

    public AcademicTerm(Long id, String termName, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.id = id;
        this.termName = termName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return termName + (isActive ? " (Aktif)" : "");
    }
}
