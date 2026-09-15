package com.example.clubhub.model;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;

    // Constructeur principal
    public User(String firstName, String lastName, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Surcharge du constructeur (utile pour AuthManager)
    public User(String username, String email, String password, Role role) {
        // Quand on a juste un nom complet ou pseudo
        this.firstName = username;
        this.lastName = "";
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        if (lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName + " (" + role + ")";
        } else {
            return firstName + " (" + role + ")";
        }
    }
}
