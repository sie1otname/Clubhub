package com.example.clubhub.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.clubhub.model.Club;

import java.util.ArrayList;
import java.util.List;

public class ClubDAO {
    private final SQLiteDatabase db;

    public ClubDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public void close() { db.close(); }

    public long addClub(String name, String description, String category, String organizerEmail) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CLUB_NAME, name);
        values.put(DatabaseHelper.COL_CLUB_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_CLUB_CATEGORY, category);
        values.put(DatabaseHelper.COL_CLUB_ORGANIZER_EMAIL, organizerEmail);
        return db.insert(DatabaseHelper.TABLE_CLUB, null, values);
    }

    public List<Club> getClubsByOrganizer(String organizerEmail) {
        List<Club> clubs = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CLUB, null,
                DatabaseHelper.COL_CLUB_ORGANIZER_EMAIL + "=?",
                new String[]{organizerEmail}, null, null,
                DatabaseHelper.COL_CLUB_NAME + " ASC"
        );
        if (cursor.moveToFirst()) {
            do { clubs.add(fromCursor(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        return clubs;
    }

    public Club getClubById(int id) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CLUB, null,
                DatabaseHelper.COL_CLUB_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, "1"
        );
        Club club = cursor.moveToFirst() ? fromCursor(cursor) : null;
        cursor.close();
        return club;
    }

    public boolean deleteClub(int id, String organizerEmail) {
        int deleted = db.delete(
                DatabaseHelper.TABLE_CLUB,
                DatabaseHelper.COL_CLUB_ID + "=? AND " + DatabaseHelper.COL_CLUB_ORGANIZER_EMAIL + "=?",
                new String[]{String.valueOf(id), organizerEmail}
        );
        return deleted > 0;
    }

    private Club fromCursor(Cursor cursor) {
        return new Club(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLUB_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLUB_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLUB_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLUB_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CLUB_ORGANIZER_EMAIL))
        );
    }
}
