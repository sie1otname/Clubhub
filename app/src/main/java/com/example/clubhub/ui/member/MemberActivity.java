package com.example.clubhub.ui.member;

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
import com.example.clubhub.data.EventDAO;
import com.example.clubhub.data.RegistrationDAO;
import com.example.clubhub.model.Event;
import com.example.clubhub.model.Registration;
import com.example.clubhub.model.User;
import com.example.clubhub.ui.common.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemberActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvSelectedEvent;
    private ListView listViewEvents;
    private Button btnRegister;
    private Button btnCancelRegistration;
    private Button btnMyRegistrations;
    private Button btnFeedback;
    private Button btnRefresh;
    private Button btnLogout;

    private AuthManager authManager;
    private EventDAO eventDAO;
    private RegistrationDAO registrationDAO;
    private Event selectedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        authManager = AuthManager.getInstance();
        eventDAO = new EventDAO(this);
        registrationDAO = new RegistrationDAO(this);

        tvWelcome = findViewById(R.id.textViewMember);
        tvSelectedEvent = findViewById(R.id.tvSelectedEvent);
        listViewEvents = findViewById(R.id.listViewEvents);
        btnRegister = findViewById(R.id.btnRegister);
        btnCancelRegistration = findViewById(R.id.btnCancelRegistration);
        btnMyRegistrations = findViewById(R.id.btnMyRegistrations);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLogout = findViewById(R.id.btnLogoutMember);

        User currentUser = authManager.getCurrentUser();
        tvWelcome.setText(currentUser != null
                ? "Découvrir les événements — " + currentUser.getFirstName()
                : "Découvrir les événements");

        listViewEvents.setOnItemClickListener((adapterView, view, position, id) -> {
            selectedEvent = (Event) adapterView.getItemAtPosition(position);
            updateSelectedEventStatus();
        });

        btnRegister.setOnClickListener(v -> registerForSelectedEvent());
        btnCancelRegistration.setOnClickListener(v -> cancelSelectedRegistration());
        btnMyRegistrations.setOnClickListener(v -> showMyRegistrations());
        btnFeedback.setOnClickListener(v -> showFeedbackDialog());
        btnRefresh.setOnClickListener(v -> loadEvents());
        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
        if (selectedEvent != null) updateSelectedEventStatus();
    }

    private void loadEvents() {
        List<Event> events = eventDAO.getAllEvents();
        listViewEvents.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                events
        ));

        if (events.isEmpty()) {
            tvSelectedEvent.setText("Aucun événement disponible pour le moment.");
        } else if (selectedEvent == null) {
            tvSelectedEvent.setText("Sélectionne un événement pour t'inscrire.");
        }
    }

    private void registerForSelectedEvent() {
        if (selectedEvent == null) {
            Toast.makeText(this, "Sélectionne d'abord un événement", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session utilisateur introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = registrationDAO.registerMember(selectedEvent.getId(), currentUser.getEmail());
        if (result == RegistrationDAO.RESULT_ALREADY_REGISTERED) {
            Toast.makeText(this, "Tu es déjà inscrit à cet événement", Toast.LENGTH_SHORT).show();
        } else if (result == RegistrationDAO.RESULT_EVENT_FULL) {
            Toast.makeText(this, "Cet événement est complet", Toast.LENGTH_SHORT).show();
        } else if (result == -1) {
            Toast.makeText(this, "Erreur pendant l'inscription", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Inscription confirmée", Toast.LENGTH_SHORT).show();
        }
        updateSelectedEventStatus();
    }

    private void cancelSelectedRegistration() {
        if (selectedEvent == null) {
            Toast.makeText(this, "Sélectionne d'abord un événement", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session utilisateur introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        Registration registration = registrationDAO.getRegistration(selectedEvent.getId(), currentUser.getEmail());
        if (registration != null && registration.isAttended()) {
            Toast.makeText(this, "Impossible d'annuler après validation de la présence", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean cancelled = registrationDAO.cancelRegistration(selectedEvent.getId(), currentUser.getEmail());
        Toast.makeText(this,
                cancelled ? "Inscription annulée" : "Tu n'étais pas inscrit à cet événement",
                Toast.LENGTH_SHORT).show();
        updateSelectedEventStatus();
    }

    private void updateSelectedEventStatus() {
        if (selectedEvent == null) {
            tvSelectedEvent.setText("Sélectionne un événement pour t'inscrire.");
            return;
        }

        User currentUser = authManager.getCurrentUser();
        Registration registration = currentUser == null ? null :
                registrationDAO.getRegistration(selectedEvent.getId(), currentUser.getEmail());
        int registrationCount = registrationDAO.countRegistrationsForEvent(selectedEvent.getId());

        String status = "Pas encore inscrit";
        if (registration != null) {
            status = registration.isAttended() ? "Présence confirmée ✓" : "Inscrit ✓ — présence non confirmée";
            if (registration.hasFeedback()) status += "\nAvis envoyé : " + registration.getRating() + "/5";
        }

        tvSelectedEvent.setText(
                selectedEvent.getTitle() +
                        "\n" + selectedEvent.getDate() + " • " + selectedEvent.getLocation() +
                        "\nPlaces : " + registrationCount + "/" + selectedEvent.getCapacity() +
                        "\nStatut : " + status
        );
    }

    private void showFeedbackDialog() {
        if (selectedEvent == null) {
            Toast.makeText(this, "Sélectionne d'abord un événement", Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) return;

        Registration registration = registrationDAO.getRegistration(selectedEvent.getId(), currentUser.getEmail());
        if (registration == null) {
            Toast.makeText(this, "Tu dois d'abord être inscrit à cet événement", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!registration.isAttended()) {
            Toast.makeText(this, "Ton organizer doit confirmer ta présence avant ton avis", Toast.LENGTH_LONG).show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        EditText ratingInput = new EditText(this);
        ratingInput.setHint("Note de 1 à 5");
        ratingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (registration.getRating() > 0) ratingInput.setText(String.valueOf(registration.getRating()));

        EditText feedbackInput = new EditText(this);
        feedbackInput.setHint("Ton commentaire (optionnel)");
        feedbackInput.setMinLines(3);
        feedbackInput.setGravity(android.view.Gravity.TOP);
        if (registration.getFeedback() != null) feedbackInput.setText(registration.getFeedback());

        container.addView(ratingInput);
        container.addView(feedbackInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Avis — " + selectedEvent.getTitle())
                .setView(container)
                .setPositiveButton("Envoyer", null)
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int rating;
            try {
                rating = Integer.parseInt(ratingInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                ratingInput.setError("Entre une note entre 1 et 5");
                return;
            }

            int result = registrationDAO.submitFeedback(
                    selectedEvent.getId(),
                    currentUser.getEmail(),
                    rating,
                    feedbackInput.getText().toString()
            );

            if (result == RegistrationDAO.FEEDBACK_INVALID_RATING) {
                ratingInput.setError("La note doit être entre 1 et 5");
                return;
            }
            if (result <= 0) {
                Toast.makeText(this, "Impossible d'enregistrer l'avis", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Avis enregistré", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            updateSelectedEventStatus();
        }));

        dialog.show();
    }

    private void showMyRegistrations() {
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) return;

        List<Registration> registrations = registrationDAO.getRegistrationsByMember(currentUser.getEmail());
        if (registrations.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Mes inscriptions")
                    .setMessage("Tu n'es inscrit à aucun événement.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        List<String> lines = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.CANADA_FRENCH);
        for (Registration registration : registrations) {
            Event event = eventDAO.getEventById(registration.getEventId());
            String eventTitle = event != null ? event.getTitle() : "Événement supprimé";
            String presence = registration.isAttended() ? "Présent ✓" : "Présence à confirmer";
            String rating = registration.hasFeedback() ? " • Avis " + registration.getRating() + "/5" : "";
            lines.add(eventTitle + "\n" + presence + rating +
                    "\nInscrit le " + dateFormat.format(new Date(registration.getRegistrationDate())));
        }

        new AlertDialog.Builder(this)
                .setTitle("Mes inscriptions")
                .setItems(lines.toArray(new String[0]), null)
                .setPositiveButton("Fermer", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventDAO.close();
        registrationDAO.close();
    }
}
