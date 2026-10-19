package id.ac.campusfacility.util;

import id.ac.campusfacility.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    @DisplayName("Password hashing dan verifikasi bekerja dengan benar")
    void testHashAndVerify() {
        String plain = "Rahasia123!";
        String hash = PasswordHasher.hash(plain);

        assertNotNull(hash);
        assertNotEquals(plain, hash);
        assertTrue(PasswordHasher.verify(plain, hash));
        assertFalse(PasswordHasher.verify("PasswordSalah", hash));
    }

    @Test
    @DisplayName("Password kosong harus memicu ValidationException")
    void testEmptyPassword() {
        assertThrows(ValidationException.class, () -> PasswordHasher.hash(""));
        assertThrows(ValidationException.class, () -> PasswordHasher.hash(null));
        assertFalse(PasswordHasher.verify(null, "hash"));
    }
}
