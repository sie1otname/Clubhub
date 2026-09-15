package com.example.clubhub.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.clubhub.R;
import com.example.clubhub.auth.AuthManager;
import com.example.clubhub.data.ClubDAO;
import com.example.clubhub.model.Club;
import com.example.clubhub.model.User;

import java.util.List;

public class ClubListFragment extends Fragment {
    private ListView listView;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_club_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentUser = AuthManager.getInstance().getCurrentUser();
        listView = view.findViewById(R.id.listClubs);

        view.findViewById(R.id.btnAddClub).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ClubFormFragment())
                        .addToBackStack(null)
                        .commit());

        listView.setOnItemClickListener((parent, v, position, id) -> {
            Club club = (Club) parent.getItemAtPosition(position);
            new AlertDialog.Builder(requireContext())
                    .setTitle(club.getName())
                    .setMessage("Catégorie : " + club.getCategory() + "\n\n" + club.getDescription())
                    .setPositiveButton("Fermer", null).show();
        });

        listView.setOnItemLongClickListener((parent, v, position, id) -> {
            Club club = (Club) parent.getItemAtPosition(position);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Supprimer le club")
                    .setMessage("Supprimer \"" + club.getName() + "\" ? Les événements du club seront aussi supprimés.")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        if (currentUser == null) return;
                        ClubDAO dao = new ClubDAO(requireContext());
                        dao.deleteClub(club.getId(), currentUser.getEmail());
                        dao.close();
                        loadClubs();
                    })
                    .setNegativeButton("Non", null).show();
            return true;
        });

        loadClubs();
    }

    @Override public void onResume() {
        super.onResume();
        if (listView != null) loadClubs();
    }

    private void loadClubs() {
        if (currentUser == null) return;
        ClubDAO dao = new ClubDAO(requireContext());
        List<Club> clubs = dao.getClubsByOrganizer(currentUser.getEmail());
        dao.close();
        listView.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, clubs));
    }
}
