package id.ac.campusfacility.session;

import id.ac.campusfacility.model.User;
import id.ac.campusfacility.model.enums.Role;
import id.ac.campusfacility.validation.AuthorizationException;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == Role.ADMIN;
    }

    public void requireLoggedIn() {
        if (!isLoggedIn()) {
            throw new AuthorizationException("Anda harus login untuk melakukan tindakan ini.");
        }
    }

    public void requireAdmin() {
        requireLoggedIn();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Hanya Administrator yang memiliki hak akses untuk tindakan ini.");
        }
    }

    public void clear() {
        this.currentUser = null;
    }
}
