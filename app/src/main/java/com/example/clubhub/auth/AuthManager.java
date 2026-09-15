package com.example.clubhub.auth;

import com.example.clubhub.model.User;

/**
 * Handles the current authenticated session.
 * User lookup is delegated to UserManager so authentication and admin user
 * management share the same source of truth.
 */
public final class AuthManager {
    private static AuthManager instance;
    private User currentUser;

    private AuthManager() { }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public boolean loginUser(String email, String password) {
        User user = UserManager.authenticate(email, password);
        if (user == null) return false;
        currentUser = user;
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean changePassword(String email, String newPassword) {
        User user = UserManager.findUserByEmail(email);
        if (user == null || newPassword == null || newPassword.trim().isEmpty()) return false;
        user.setPassword(newPassword.trim());
        return UserManager.updateUser(user);
    }
}
