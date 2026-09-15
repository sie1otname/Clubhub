package com.example.clubhub.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.clubhub.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private final SQLiteDatabase db;

    public EventDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public void close() { db.close(); }

    public long addEvent(int clubId, String organizerEmail, String title, String description,
                         String location, String date, int capacity) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EVENT_CLUB_ID, clubId);
        values.put(DatabaseHelper.COL_EVENT_ORGANIZER_EMAIL, organizerEmail);
        values.put(DatabaseHelper.COL_EVENT_TITLE, title);
        values.put(DatabaseHelper.COL_EVENT_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_EVENT_LOCATION, location);
        values.put(DatabaseHelper.COL_EVENT_DATE, date);
        values.put(DatabaseHelper.COL_EVENT_CAPACITY, capacity);
        return db.insert(DatabaseHelper.TABLE_EVENT, null, values);
    }

    public List<Event> getAllEvents() {
        return queryEvents(null, null);
    }

    public List<Event> getEventsByOrganizer(String organizerEmail) {
        return queryEvents("e." + DatabaseHelper.COL_EVENT_ORGANIZER_EMAIL + "=?", new String[]{organizerEmail});
    }

    public Event getEventById(int id) {
        List<Event> events = queryEvents("e." + DatabaseHelper.COL_EVENT_ID + "=?", new String[]{String.valueOf(id)});
        return events.isEmpty() ? null : events.get(0);
    }

    private List<Event> queryEvents(String selection, String[] selectionArgs) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, c." + DatabaseHelper.COL_CLUB_NAME + " AS club_name " +
                "FROM " + DatabaseHelper.TABLE_EVENT + " e " +
                "JOIN " + DatabaseHelper.TABLE_CLUB + " c ON e." + DatabaseHelper.COL_EVENT_CLUB_ID + " = c." + DatabaseHelper.COL_CLUB_ID;
        if (selection != null) sql += " WHERE " + selection;
        sql += " ORDER BY e." + DatabaseHelper.COL_EVENT_ID + " DESC";

        Cursor cursor = db.rawQuery(sql, selectionArgs);
        if (cursor.moveToFirst()) {
            do {
                events.add(new Event(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_CLUB_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow("club_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_ORGANIZER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EVENT_CAPACITY))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public boolean deleteEvent(int id, String organizerEmail) {
        return db.delete(
                DatabaseHelper.TABLE_EVENT,
                DatabaseHelper.COL_EVENT_ID + "=? AND " + DatabaseHelper.COL_EVENT_ORGANIZER_EMAIL + "=?",
                new String[]{String.valueOf(id), organizerEmail}
        ) > 0;
    }

    public void updateEvent(int id, int clubId, String organizerEmail, String title, String description,
                            String location, String date, int capacity) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EVENT_CLUB_ID, clubId);
        values.put(DatabaseHelper.COL_EVENT_TITLE, title);
        values.put(DatabaseHelper.COL_EVENT_DESCRIPTION, description);
        values.put(DatabaseHelper.COL_EVENT_LOCATION, location);
        values.put(DatabaseHelper.COL_EVENT_DATE, date);
        values.put(DatabaseHelper.COL_EVENT_CAPACITY, capacity);
        db.update(
                DatabaseHelper.TABLE_EVENT, values,
                DatabaseHelper.COL_EVENT_ID + "=? AND " + DatabaseHelper.COL_EVENT_ORGANIZER_EMAIL + "=?",
                new String[]{String.valueOf(id), organizerEmail}
        );
    }
}
