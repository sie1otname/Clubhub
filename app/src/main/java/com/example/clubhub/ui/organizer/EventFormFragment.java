package com.example.clubhub.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.data.ClubDAO;
import com.example.clubhub.data.EventDAO;
import com.example.clubhub.model.Club;
import com.example.clubhub.model.User;

import java.util.List;

public class EventFormFragment extends Fragment {

    private List<Club> clubs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Spinner spinnerClub = view.findViewById(R.id.spinnerClub);
        EditText etTitle = view.findViewById(R.id.etEventTitle);
        EditText etDescription = view.findViewById(R.id.etEventDescription);
        EditText etLocation = view.findViewById(R.id.etEventLocation);
        EditText etDate = view.findViewById(R.id.etEventDate);
        EditText etCapacity = view.findViewById(R.id.etEventCapacity);
        Button btnSave = view.findViewById(R.id.btnSaveEvent);

        User currentUser = AuthManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Session organizer introuvable", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(false);
            return;
        }

        ClubDAO clubDAO = new ClubDAO(requireContext());
        clubs = clubDAO.getClubsByOrganizer(currentUser.getEmail());
        clubDAO.close();

        ArrayAdapter<Club> clubAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, clubs);
        clubAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClub.setAdapter(clubAdapter);

        if (clubs.isEmpty()) {
            btnSave.setEnabled(false);
            Toast.makeText(getContext(), "Crée d'abord un club dans Mes clubs", Toast.LENGTH_LONG).show();
        }

        btnSave.setOnClickListener(v -> {
            if (clubs.isEmpty()) return;

            Club selectedClub = (Club) spinnerClub.getSelectedItem();
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String capacityText = etCapacity.getText().toString().trim();

            if (title.isEmpty() || location.isEmpty() || date.isEmpty() || capacityText.isEmpty()) {
                Toast.makeText(getContext(), "Titre, lieu, date et capacité sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityText);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "La capacité doit être un nombre", Toast.LENGTH_SHORT).show();
                return;
            }
            if (capacity <= 0) {
                Toast.makeText(getContext(), "La capacité doit être supérieure à 0", Toast.LENGTH_SHORT).show();
                return;
            }
            if (description.isEmpty()) description = "Aucune description";

            EventDAO dao = new EventDAO(requireContext());
            long result = dao.addEvent(
                    selectedClub.getId(), currentUser.getEmail(), title, description, location, date, capacity);
            dao.close();

            if (result == -1) {
                Toast.makeText(getContext(), "Impossible d'enregistrer l'événement", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Événement créé", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}
