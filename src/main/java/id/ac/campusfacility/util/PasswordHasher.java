package id.ac.campusfacility.util;

import id.ac.campusfacility.validation.ValidationException;
import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {

    private static final int LOG_ROUNDS = 10;

    private PasswordHasher() {
    }

    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new ValidationException("Password tidak boleh kosong.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isBlank() || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Jika format hash rusak/tidak valid
            return false;
        }
    }
}
