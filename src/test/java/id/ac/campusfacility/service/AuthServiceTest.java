package id.ac.campusfacility.service;

import id.ac.campusfacility.BaseTest;
import id.ac.campusfacility.model.User;
import id.ac.campusfacility.model.enums.Role;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest extends BaseTest {

    private final AuthService authService = new AuthService();

    @Test
    @DisplayName("Admin dapat login dengan kredensial yang benar")
    void testAdminLoginSuccess() {
        User user = authService.login("admin", "admin123");
        assertNotNull(user);
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(UserSession.getInstance().isLoggedIn());
        assertTrue(UserSession.getInstance().isAdmin());
    }

    @Test
    @DisplayName("User reguler dapat login dengan kredensial yang benar")
    void testUserLoginSuccess() {
        User user = authService.login("user", "user123");
        assertNotNull(user);
        assertEquals(Role.USER, user.getRole());
        assertTrue(UserSession.getInstance().isLoggedIn());
        assertFalse(UserSession.getInstance().isAdmin());
    }

    @Test
    @DisplayName("Login gagal jika password salah")
    void testLoginWrongPassword() {
        assertThrows(ValidationException.class, () -> authService.login("admin", "salahPassword"));
        assertFalse(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @DisplayName("Login gagal jika username tidak ditemukan")
    void testLoginUnknownUser() {
        assertThrows(ValidationException.class, () -> authService.login("nonexistent", "secret"));
        assertFalse(UserSession.getInstance().isLoggedIn());
    }

    @Test
    @DisplayName("Logout membersihkan sesi pengguna")
    void testLogout() {
        authService.login("admin", "admin123");
        assertTrue(UserSession.getInstance().isLoggedIn());

        authService.logout();
        assertFalse(UserSession.getInstance().isLoggedIn());
        assertNull(UserSession.getInstance().getCurrentUser());
    }
}
