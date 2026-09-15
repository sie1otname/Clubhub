package com.example.clubhub.ui.organizer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.data.EventDAO;
import com.example.clubhub.data.RegistrationDAO;
import com.example.clubhub.model.Event;
import com.example.clubhub.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAnalyticsActivity extends AppCompatActivity {

    private TextView tvGlobal;
    private ListView listViewAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_analytics);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Statistiques ClubHub");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvGlobal = findViewById(R.id.tvGlobal);
        listViewAnalytics = findViewById(R.id.listViewAnalytics);
        loadAnalytics();
    }

    private void loadAnalytics() {
        RegistrationDAO registrationDAO = new RegistrationDAO(this);
        EventDAO eventDAO = new EventDAO(this);
        User currentUser = AuthManager.getInstance().getCurrentUser();

        List<Event> events = currentUser == null
                ? new ArrayList<>()
                : eventDAO.getEventsByOrganizer(currentUser.getEmail());

        int totalRegistrations = 0;
        int totalAttended = 0;
        int totalFeedback = 0;
        double weightedRatingTotal = 0.0;
        List<String> lines = new ArrayList<>();

        for (Event event : events) {
            int registrations = registrationDAO.countRegistrationsForEvent(event.getId());
            int attended = registrationDAO.countAttendedForEvent(event.getId());
            int feedbackCount = registrationDAO.countFeedbackForEvent(event.getId());
            double averageRating = registrationDAO.getAverageRatingForEvent(event.getId());
            double attendanceRate = registrations == 0 ? 0.0 : (attended * 100.0 / registrations);

            totalRegistrations += registrations;
            totalAttended += attended;
            totalFeedback += feedbackCount;
            weightedRatingTotal += averageRating * feedbackCount;

            String ratingText = feedbackCount == 0
                    ? "Aucun avis"
                    : String.format(Locale.CANADA_FRENCH, "%.1f/5 (%d avis)", averageRating, feedbackCount);

            lines.add(
                    event.getTitle() + "\n" +
                            event.getClubName() +
                            "\nInscriptions : " + registrations + "/" + event.getCapacity() +
                            " • Présents : " + attended +
                            String.format(Locale.CANADA_FRENCH, " • %.0f%%", attendanceRate) +
                            "\nNote : " + ratingText
            );
        }

        double globalAttendanceRate = totalRegistrations == 0 ? 0.0 : totalAttended * 100.0 / totalRegistrations;
        double globalAverageRating = totalFeedback == 0 ? 0.0 : weightedRatingTotal / totalFeedback;

        String ratingSummary = totalFeedback == 0
                ? "Aucun avis reçu"
                : String.format(Locale.CANADA_FRENCH, "Note moyenne : %.1f/5 (%d avis)", globalAverageRating, totalFeedback);

        tvGlobal.setText(
                "Événements : " + events.size() +
                        "\nInscriptions : " + totalRegistrations +
                        "\nPrésences : " + totalAttended +
                        String.format(Locale.CANADA_FRENCH, " (%.0f%%)", globalAttendanceRate) +
                        "\n" + ratingSummary
        );

        if (lines.isEmpty()) lines.add("Crée un événement pour commencer à voir des statistiques.");

        listViewAnalytics.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                lines
        ));

        eventDAO.close();
        registrationDAO.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
