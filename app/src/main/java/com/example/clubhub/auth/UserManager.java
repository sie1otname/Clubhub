package com.example.clubhub.auth;

import com.example.clubhub.model.Role;
import com.example.clubhub.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Single in-memory source of truth for demo users in the MVP.
 * Club/event/registration data is persisted in SQLite; demo user accounts
 * are recreated when the app process restarts.
 */
public final class UserManager {
    private static final String DEMO_MEMBER_EMAIL = "member@clubhub.com";
    private static final List<User> users = new ArrayList<>();

    static {
        seedDemoUsers();
    }

    private UserManager() { }

    private static void seedDemoUsers() {
        users.clear();
        users.add(new User("Admin", "", "admin@clubhub.com", "1234", Role.ADMINISTRATOR));
        users.add(new User("Organizer", "One", "organizer@clubhub.com", "organizerpass", Role.ORGANIZER));
        users.add(new User("Organizer", "Two", "organizer2@clubhub.com", "organizer2pass", Role.ORGANIZER));
        users.add(new User("Demo", "Member", DEMO_MEMBER_EMAIL, "memberpass", Role.MEMBER));
    }

    public static User authenticate(String email, String password) {
        User user = findUserByEmail(email);
        return user != null && user.getPassword().equals(password) ? user : null;
    }

    public static boolean addUser(User user) {
        if (user == null || user.getEmail() == null || findUserByEmail(user.getEmail()) != null) {
            return false;
        }
        return users.add(user);
    }

    public static boolean deleteUser(String email) {
        return users.removeIf(user ->
                user.getRole() == Role.MEMBER &&
                user.getEmail().equalsIgnoreCase(email) &&
                !user.getEmail().equalsIgnoreCase(DEMO_MEMBER_EMAIL));
    }

    public static boolean updateUser(User updated) {
        if (updated == null || updated.getEmail() == null) return false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(updated.getEmail())) {
                users.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public static User findUserByEmail(String email) {
        if (email == null) return null;
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) return user;
        }
        return null;
    }

    public static List<User> getAll() {
        return new ArrayList<>(users);
    }

    public static void resetMembers() {
        users.removeIf(user ->
                user.getRole() == Role.MEMBER &&
                !user.getEmail().equalsIgnoreCase(DEMO_MEMBER_EMAIL));
    }
}
