package com.example.clubhub;

import com.example.clubhub.auth.UserManager;
import com.example.clubhub.model.Role;
import com.example.clubhub.model.User;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserManagerTest {

    @Test
    public void authenticate_returnsDemoMemberForValidCredentials() {
        User user = UserManager.authenticate("member@clubhub.com", "memberpass");
        assertNotNull(user);
        assertTrue(user.getRole() == Role.MEMBER);
    }

    @Test
    public void addUser_allowsNewMemberToAuthenticate() {
        String email = "portfolio-test@clubhub.com";
        UserManager.deleteUser(email);

        boolean added = UserManager.addUser(
                new User("Portfolio", "Test", email, "testpass", Role.MEMBER)
        );

        assertTrue(added);
        assertNotNull(UserManager.authenticate(email, "testpass"));
        UserManager.deleteUser(email);
    }

    @Test
    public void addUser_refusesDuplicateEmail() {
        boolean added = UserManager.addUser(
                new User("Duplicate", "Admin", "admin@clubhub.com", "x", Role.MEMBER)
        );
        assertFalse(added);
    }
}
