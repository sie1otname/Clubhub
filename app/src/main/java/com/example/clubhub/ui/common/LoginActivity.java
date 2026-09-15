package com.example.clubhub.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clubhub.ui.admin.AdminActivity;
import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.model.Role;
import com.example.clubhub.model.User;
import com.example.clubhub.ui.organizer.OrganizerActivity;
import com.example.clubhub.ui.member.MemberActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Lier les éléments du layout
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // Initialiser le gestionnaire d'authentification
        authManager = AuthManager.getInstance();


        // Quand on clique sur "Se connecter"
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Vérifie les identifiants
                if (authManager.loginUser(email, password)) {

                    // Récupère l'utilisateur connecté
                    User user = authManager.getCurrentUser();
                    Role role = user.getRole();

                    Toast.makeText(LoginActivity.this,
                            "Connexion réussie (" + role + ")",
                            Toast.LENGTH_SHORT).show();

                    // Rediriger selon le rôle
                    Intent intent = null;

                    if (role.equals(Role.ADMINISTRATOR)) {
                        intent = new Intent(LoginActivity.this, AdminActivity.class);
                    } else if (role.equals(Role.ORGANIZER)) {
                        intent = new Intent(LoginActivity.this, OrganizerActivity.class);
                    } else if (role.equals(Role.MEMBER)) {
                        intent = new Intent(LoginActivity.this, MemberActivity.class);
                    }

                    if (intent != null) {
                        startActivity(intent);
                        finish();
                    }


                } else {
                    Toast.makeText(LoginActivity.this,
                            "Identifiants invalides",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
