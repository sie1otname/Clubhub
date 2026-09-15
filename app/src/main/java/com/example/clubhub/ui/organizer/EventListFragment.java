package com.example.clubhub.ui.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.data.EventDAO;
import com.example.clubhub.data.RegistrationDAO;
import com.example.clubhub.model.Event;
import com.example.clubhub.model.Registration;
import com.example.clubhub.model.User;
import com.example.clubhub.ui.common.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventListFragment extends Fragment {

    private EventAdapter adapter;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentUser = AuthManager.getInstance().getCurrentUser();

        RecyclerView recyclerView = view.findViewById(R.id.rvEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter();
        recyclerView.setAdapter(adapter);
        loadEvents();

        adapter.setListener(new EventAdapter.Listener() {
            @Override public void onClick(Event event) { showEventDetails(event); }

            @Override public void onLongClick(Event event) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer l'événement")
                        .setMessage("Supprimer \"" + event.getTitle() + "\" ?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            if (currentUser == null) return;
                            EventDAO dao = new EventDAO(requireContext());
                            dao.deleteEvent(event.getId(), currentUser.getEmail());
                            dao.close();
                            loadEvents();
                        })
                        .setNegativeButton("Non", null)
                        .show();
            }
        });

        view.findViewById(R.id.btnManageClubs).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ClubListFragment())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.btnAnalytics).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EventAnalyticsActivity.class)));

        view.findViewById(R.id.btnLogoutOrganizer).setOnClickListener(v -> {
            AuthManager.getInstance().logout();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        view.findViewById(R.id.fabAdd).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EventFormFragment())
                        .addToBackStack(null)
                        .commit());
    }

    @Override public void onResume() {
        super.onResume();
        if (adapter != null) loadEvents();
    }

    private void showEventDetails(Event event) {
        RegistrationDAO registrationDAO = new RegistrationDAO(requireContext());
        int registrationCount = registrationDAO.countRegistrationsForEvent(event.getId());
        int attendedCount = registrationDAO.countAttendedForEvent(event.getId());
        registrationDAO.close();

        new AlertDialog.Builder(requireContext())
                .setTitle(event.getTitle())
                .setMessage("Club : " + event.getClubName() +
                        "\n\n" + event.getDescription() +
                        "\n\nLieu : " + event.getLocation() +
                        "\nDate : " + event.getDate() +
                        "\nInscriptions : " + registrationCount + "/" + event.getCapacity() +
                        "\nPrésents : " + attendedCount)
                .setPositiveButton("Voir les inscrits", (dialog, which) -> showRegistrations(event))
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void showRegistrations(Event event) {
        RegistrationDAO dao = new RegistrationDAO(requireContext());
        List<Registration> registrations = dao.getRegistrationsByEvent(event.getId());
        dao.close();

        if (registrations.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Inscrits — " + event.getTitle())
                    .setMessage("Aucun membre inscrit pour le moment.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.CANADA_FRENCH);
        String[] lines = new String[registrations.size()];
        for (int i = 0; i < registrations.size(); i++) {
            Registration registration = registrations.get(i);
            String attendance = registration.isAttended() ? "✓ PRÉSENT" : "○ ABSENT / À CONFIRMER";
            lines[i] = registration.getMemberEmail() + "\n" + attendance +
                    "\nInscrit le " + format.format(new Date(registration.getRegistrationDate()));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Présences — " + event.getTitle())
                .setItems(lines, (dialog, which) -> {
                    Registration selected = registrations.get(which);
                    RegistrationDAO updateDao = new RegistrationDAO(requireContext());
                    boolean newValue = !selected.isAttended();
                    boolean updated = updateDao.setAttendance(selected.getId(), newValue);
                    updateDao.close();
                    if (updated) {
                        Toast.makeText(requireContext(),
                                newValue ? "Présence confirmée" : "Présence retirée",
                                Toast.LENGTH_SHORT).show();
                        showRegistrations(event);
                    }
                })
                .setPositiveButton("Fermer", null)
                .show();
    }

    private void loadEvents() {
        if (currentUser == null || adapter == null) return;
        EventDAO dao = new EventDAO(requireContext());
        adapter.setItems(dao.getEventsByOrganizer(currentUser.getEmail()));
        dao.close();
    }
}
