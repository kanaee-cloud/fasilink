package id.ac.campusfacility.repository;

import id.ac.campusfacility.config.DatabaseManager;
import id.ac.campusfacility.model.AcademicTerm;
import id.ac.campusfacility.model.ClassGroup;
import id.ac.campusfacility.model.Course;
import id.ac.campusfacility.model.Lecturer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AcademicDataRepository {

    public Optional<AcademicTerm> getActiveTerm() {
        String sql = "SELECT * FROM academic_terms WHERE is_active = 1 LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                AcademicTerm term = new AcademicTerm();
                term.setId(rs.getLong("id"));
                term.setTermName(rs.getString("term_name"));
                term.setStartDate(LocalDate.parse(rs.getString("start_date")));
                term.setEndDate(LocalDate.parse(rs.getString("end_date")));
                term.setActive(rs.getInt("is_active") == 1);
                return Optional.of(term);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data semester aktif: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<AcademicTerm> findAllTerms() {
        List<AcademicTerm> list = new ArrayList<>();
        String sql = "SELECT * FROM academic_terms ORDER BY start_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AcademicTerm term = new AcademicTerm();
                term.setId(rs.getLong("id"));
                term.setTermName(rs.getString("term_name"));
                term.setStartDate(LocalDate.parse(rs.getString("start_date")));
                term.setEndDate(LocalDate.parse(rs.getString("end_date")));
                term.setActive(rs.getInt("is_active") == 1);
                list.add(term);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil daftar semester: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Course> findAllActiveCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE is_active = 1 ORDER BY course_code ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getLong("id"));
                c.setCourseCode(rs.getString("course_code"));
                c.setCourseName(rs.getString("course_name"));
                c.setActive(rs.getInt("is_active") == 1);
                list.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil daftar mata kuliah: " + e.getMessage(), e);
        }
        return list;
    }

    public List<ClassGroup> findAllActiveClassGroups() {
        List<ClassGroup> list = new ArrayList<>();
        String sql = "SELECT * FROM class_groups WHERE is_active = 1 ORDER BY study_program ASC, class_code ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ClassGroup cg = new ClassGroup();
                cg.setId(rs.getLong("id"));
                cg.setStudyProgram(rs.getString("study_program"));
                cg.setCohortYear(rs.getInt("cohort_year"));
                cg.setClassCode(rs.getString("class_code"));
                int studentCount = rs.getInt("student_count");
                cg.setStudentCount(rs.wasNull() ? null : studentCount);
                cg.setActive(rs.getInt("is_active") == 1);
                list.add(cg);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil daftar kelas: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Lecturer> findAllActiveLecturers() {
        List<Lecturer> list = new ArrayList<>();
        String sql = "SELECT * FROM lecturers WHERE is_active = 1 ORDER BY full_name ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Lecturer l = new Lecturer();
                l.setId(rs.getLong("id"));
                l.setLecturerCode(rs.getString("lecturer_code"));
                l.setFullName(rs.getString("full_name"));
                l.setEmail(rs.getString("email"));
                l.setActive(rs.getInt("is_active") == 1);
                list.add(l);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil daftar dosen: " + e.getMessage(), e);
        }
        return list;
    }
}
