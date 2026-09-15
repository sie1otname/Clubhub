package com.example.clubhub.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.data.ClubDAO;
import com.example.clubhub.model.User;

public class ClubFormFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_club_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText name = view.findViewById(R.id.etClubName);
        EditText category = view.findViewById(R.id.etClubCategory);
        EditText description = view.findViewById(R.id.etClubDescription);

        view.findViewById(R.id.btnSaveClub).setOnClickListener(v -> {
            User currentUser = AuthManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String clubName = name.getText().toString().trim();
            String clubCategory = category.getText().toString().trim();
            String clubDescription = description.getText().toString().trim();

            if (clubName.isEmpty()) {
                Toast.makeText(getContext(), "Le nom du club est obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }
            if (clubCategory.isEmpty()) clubCategory = "Autre";
            if (clubDescription.isEmpty()) clubDescription = "Aucune description";

            ClubDAO dao = new ClubDAO(requireContext());
            long result = dao.addClub(clubName, clubDescription, clubCategory, currentUser.getEmail());
            dao.close();

            if (result == -1) {
                Toast.makeText(getContext(), "Impossible de créer le club", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Club créé", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}
