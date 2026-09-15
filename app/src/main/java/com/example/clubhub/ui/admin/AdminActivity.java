package com.example.clubhub.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.auth.UserManager;
import com.example.clubhub.data.DatabaseHelper;
import com.example.clubhub.model.Role;
import com.example.clubhub.model.User;
import com.example.clubhub.ui.common.LoginActivity;

import java.util.List;
import java.util.stream.Collectors;

public class AdminActivity extends AppCompatActivity {

    private ListView listViewUsers;
    private ArrayAdapter<String> adapter;
    private List<User> users;
    private final String userEmail = "admin@clubhub.com";
    private DatabaseHelper dbHelper;
    private Button btnResetDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Références UI
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        listViewUsers = findViewById(R.id.listViewUsers);
        Button btnAdd = findViewById(R.id.btnAddUser);
        Button btnEdit = findViewById(R.id.btnEditUser);
        Button btnDelete = findViewById(R.id.btnDeleteUser);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnChangePass = findViewById(R.id.btnChangePassword);

        tvWelcome.setText("Bienvenue, Administrateur");
        refreshList();

        // Boutons gestion utilisateurs
        btnAdd.setOnClickListener(v -> showAddDialog());
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> showDeleteDialog());

        // Bouton de changement du mot de passe admin
        btnChangePass.setOnClickListener(v -> showChangePasswordDialog());

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            AuthManager.getInstance().logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        dbHelper = new DatabaseHelper(this);
        btnResetDatabase = findViewById(R.id.btn_reset_database);

        btnResetDatabase.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Réinitialiser la base de données")
                    .setMessage("Cela va supprimer les clubs, événements, inscriptions et membres ajoutés. Continuer ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        dbHelper.resetDatabase();
                        UserManager.resetMembers();
                        refreshList();
                        Toast.makeText(this, "Base de données réinitialisée", Toast.LENGTH_SHORT).show();
                    })

                    .setNegativeButton("Annuler", null)
                    .show();
        });

    }

    // Rafraîchir la liste des utilisateurs
    private void refreshList() {
        users = UserManager.getAll();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                users.stream().map(User::toString).toArray(String[]::new));
        listViewUsers.setAdapter(adapter);
    }

    // Ajouter un membre
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un membre");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        EditText inputFirstName = new EditText(this);
        inputFirstName.setHint("Prénom");
        layout.addView(inputFirstName);

        EditText inputLastName = new EditText(this);
        inputLastName.setHint("Nom");
        layout.addView(inputLastName);

        EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email");
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(inputEmail);

        EditText inputPassword = new EditText(this);
        inputPassword.setHint("Mot de passe (par défaut : member-pwd)");
        inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String firstName = inputFirstName.getText().toString().trim();
            String lastName = inputLastName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String pwd = inputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pwd.isEmpty()) {
                pwd = "member-pwd";
            }

            boolean ok = UserManager.addUser(new User(firstName, lastName, email, pwd, Role.MEMBER));
            if (ok) {
                Toast.makeText(this, "Membre ajouté avec succès", Toast.LENGTH_SHORT).show();
                refreshList();
            } else {
                Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    // Modifier un membre
    private void showEditDialog() {
        List<User> members = users.stream()
                .filter(u -> u.getRole() == Role.MEMBER)
                .collect(Collectors.toList());

        if (members.isEmpty()) {
            Toast.makeText(this, "Aucun membre à modifier", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] emails = members.stream().map(User::getEmail).toArray(String[]::new);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner un membre à modifier");
        builder.setItems(emails, (dialog, which) -> {
            User selectedUser = members.get(which);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

            EditText inputFirstName = new EditText(this);
            inputFirstName.setHint("Prénom");
            inputFirstName.setText(selectedUser.getFirstName());
            layout.addView(inputFirstName);

            EditText inputLastName = new EditText(this);
            inputLastName.setHint("Nom");
            inputLastName.setText(selectedUser.getLastName());
            layout.addView(inputLastName);

            EditText inputPassword = new EditText(this);
            inputPassword.setHint("Nouveau mot de passe (laisser vide pour conserver)");
            inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(inputPassword);

            new AlertDialog.Builder(this)
                    .setTitle("Modifier le membre")
                    .setView(layout)
                    .setPositiveButton("Enregistrer", (d, w) -> {
                        selectedUser.setFirstName(inputFirstName.getText().toString().trim());
                        selectedUser.setLastName(inputLastName.getText().toString().trim());
                        String newPwd = inputPassword.getText().toString().trim();
                        if (!newPwd.isEmpty()) {
                            selectedUser.setPassword(newPwd);
                        }
                        UserManager.updateUser(selectedUser);
                        Toast.makeText(this, "Membre mis à jour", Toast.LENGTH_SHORT).show();
                        refreshList();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
        builder.show();
    }

    // Supprimer un membre
    private void showDeleteDialog() {
        List<User> members = users.stream()
                .filter(u -> u.getRole() == Role.MEMBER)
                .collect(Collectors.toList());

        if (members.isEmpty()) {
            Toast.makeText(this, "Aucun membre à supprimer", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] emails = members.stream().map(User::getEmail).toArray(String[]::new);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionner un membre à supprimer");
        builder.setItems(emails, (dialog, which) -> {
            String email = emails[which];
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Supprimer " + email + " ?")
                    .setPositiveButton("Oui", (d, w) -> {
                        boolean deleted = UserManager.deleteUser(email);
                        Toast.makeText(this,
                                deleted ? "Membre supprimé" : "Le compte membre de démonstration est protégé",
                                Toast.LENGTH_SHORT).show();
                        refreshList();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });
        builder.show();
    }

    //  Changer le mot de passe de l’administrateur
    private void showChangePasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nouveau mot de passe");

        new AlertDialog.Builder(this)
                .setTitle("Changer le mot de passe administrateur")
                .setView(input)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    String newPass = input.getText().toString().trim();
                    if (!newPass.isEmpty()) {
                        AuthManager.getInstance().changePassword(userEmail, newPass);
                        Toast.makeText(this, "Mot de passe modifié avec succès ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Mot de passe invalide ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
