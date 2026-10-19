package id.ac.campusfacility.service;

import id.ac.campusfacility.model.User;
import id.ac.campusfacility.repository.UserRepository;
import id.ac.campusfacility.session.UserSession;
import id.ac.campusfacility.util.PasswordHasher;
import id.ac.campusfacility.validation.AuthorizationException;
import id.ac.campusfacility.validation.ValidationException;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username wajib diisi.");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password wajib diisi.");
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new ValidationException("Username atau password salah.");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            throw new AuthorizationException("Akun Anda telah dinonaktifkan. Silakan hubungi Administrator.");
        }

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah.");
        }

        // Set session user
        UserSession.getInstance().setCurrentUser(user);
        return user;
    }

    public void logout() {
        UserSession.getInstance().clear();
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserSession.getInstance().requireLoggedIn();
        User currentUser = UserSession.getInstance().getCurrentUser();

        // Hanya diri sendiri atau admin yang boleh ubah password
        if (!currentUser.getId().equals(userId) && !UserSession.getInstance().isAdmin()) {
            throw new AuthorizationException("Anda tidak memiliki izin mengubah password akun ini.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password baru minimal 6 karakter.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User tidak ditemukan."));

        // Verifikasi password lama jika bukan admin yang override
        if (!UserSession.getInstance().isAdmin() || currentUser.getId().equals(userId)) {
            if (!PasswordHasher.verify(oldPassword, targetUser.getPasswordHash())) {
                throw new ValidationException("Password lama tidak sesuai.");
            }
        }

        String newHash = PasswordHasher.hash(newPassword);
        userRepository.updatePassword(userId, newHash);
    }
}
