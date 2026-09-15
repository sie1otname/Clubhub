package com.example.clubhub.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.clubhub.model.Registration;

import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    public static final long RESULT_ALREADY_REGISTERED = -2;
    public static final long RESULT_EVENT_FULL = -3;
    public static final int FEEDBACK_NOT_REGISTERED = -2;
    public static final int FEEDBACK_NOT_ATTENDED = -3;
    public static final int FEEDBACK_INVALID_RATING = -4;

    private final SQLiteDatabase db;

    public RegistrationDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public void close() { db.close(); }

    public long registerMember(int eventId, String memberEmail) {
        if (isRegistered(eventId, memberEmail)) return RESULT_ALREADY_REGISTERED;

        int capacity = getEventCapacity(eventId);
        if (capacity > 0 && countRegistrationsForEvent(eventId) >= capacity) {
            return RESULT_EVENT_FULL;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REGISTRATION_EVENT_ID, eventId);
        values.put(DatabaseHelper.COL_REGISTRATION_MEMBER_EMAIL, memberEmail);
        values.put(DatabaseHelper.COL_REGISTRATION_STATUS, Registration.STATUS_REGISTERED);
        values.put(DatabaseHelper.COL_REGISTRATION_DATE, System.currentTimeMillis());
        values.put(DatabaseHelper.COL_REGISTRATION_ATTENDED, 0);
        values.put(DatabaseHelper.COL_REGISTRATION_RATING, 0);
        values.put(DatabaseHelper.COL_REGISTRATION_FEEDBACK, "");
        return db.insert(DatabaseHelper.TABLE_REGISTRATION, null, values);
    }

    public boolean cancelRegistration(int eventId, String memberEmail) {
        Registration registration = getRegistration(eventId, memberEmail);
        if (registration == null || registration.isAttended()) return false;

        return db.delete(
                DatabaseHelper.TABLE_REGISTRATION,
                DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=? AND " +
                        DatabaseHelper.COL_REGISTRATION_MEMBER_EMAIL + "=?",
                new String[]{String.valueOf(eventId), memberEmail}
        ) > 0;
    }

    public boolean isRegistered(int eventId, String memberEmail) {
        return getRegistration(eventId, memberEmail) != null;
    }

    public Registration getRegistration(int eventId, String memberEmail) {
        List<Registration> registrations = queryRegistrations(
                DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=? AND " +
                        DatabaseHelper.COL_REGISTRATION_MEMBER_EMAIL + "=?",
                new String[]{String.valueOf(eventId), memberEmail}
        );
        return registrations.isEmpty() ? null : registrations.get(0);
    }

    public boolean setAttendance(int registrationId, boolean attended) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REGISTRATION_ATTENDED, attended ? 1 : 0);

        // If attendance is removed, feedback is removed too because only attendees can rate.
        if (!attended) {
            values.put(DatabaseHelper.COL_REGISTRATION_RATING, 0);
            values.put(DatabaseHelper.COL_REGISTRATION_FEEDBACK, "");
        }

        return db.update(
                DatabaseHelper.TABLE_REGISTRATION,
                values,
                DatabaseHelper.COL_REGISTRATION_ID + "=?",
                new String[]{String.valueOf(registrationId)}
        ) > 0;
    }

    public int submitFeedback(int eventId, String memberEmail, int rating, String feedback) {
        if (rating < 1 || rating > 5) return FEEDBACK_INVALID_RATING;

        Registration registration = getRegistration(eventId, memberEmail);
        if (registration == null) return FEEDBACK_NOT_REGISTERED;
        if (!registration.isAttended()) return FEEDBACK_NOT_ATTENDED;

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REGISTRATION_RATING, rating);
        values.put(DatabaseHelper.COL_REGISTRATION_FEEDBACK, feedback == null ? "" : feedback.trim());

        return db.update(
                DatabaseHelper.TABLE_REGISTRATION,
                values,
                DatabaseHelper.COL_REGISTRATION_ID + "=?",
                new String[]{String.valueOf(registration.getId())}
        );
    }

    public int countRegistrationsForEvent(int eventId) {
        return countWhere(DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)});
    }

    public int countAttendedForEvent(int eventId) {
        return countWhere(
                DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=? AND " +
                        DatabaseHelper.COL_REGISTRATION_ATTENDED + "=1",
                new String[]{String.valueOf(eventId)}
        );
    }

    public int countFeedbackForEvent(int eventId) {
        return countWhere(
                DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=? AND " +
                        DatabaseHelper.COL_REGISTRATION_RATING + ">0",
                new String[]{String.valueOf(eventId)}
        );
    }

    public double getAverageRatingForEvent(int eventId) {
        Cursor cursor = db.rawQuery(
                "SELECT AVG(" + DatabaseHelper.COL_REGISTRATION_RATING + ") FROM " +
                        DatabaseHelper.TABLE_REGISTRATION + " WHERE " +
                        DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=? AND " +
                        DatabaseHelper.COL_REGISTRATION_RATING + ">0",
                new String[]{String.valueOf(eventId)}
        );
        double average = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) average = cursor.getDouble(0);
        cursor.close();
        return average;
    }

    public List<Registration> getRegistrationsByEvent(int eventId) {
        return queryRegistrations(
                DatabaseHelper.COL_REGISTRATION_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );
    }

    public List<Registration> getRegistrationsByMember(String memberEmail) {
        return queryRegistrations(
                DatabaseHelper.COL_REGISTRATION_MEMBER_EMAIL + "=?",
                new String[]{memberEmail}
        );
    }

    public List<Registration> getAllRegistrations() {
        return queryRegistrations(null, null);
    }

    private int countWhere(String selection, String[] args) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATION + " WHERE " + selection,
                args
        );
        int count = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return count;
    }

    private List<Registration> queryRegistrations(String selection, String[] selectionArgs) {
        List<Registration> registrations = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_REGISTRATION,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COL_REGISTRATION_DATE + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                registrations.add(new Registration(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_EVENT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_MEMBER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_STATUS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_ATTENDED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_RATING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REGISTRATION_FEEDBACK))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return registrations;
    }

    private int getEventCapacity(int eventId) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EVENT,
                new String[]{DatabaseHelper.COL_EVENT_CAPACITY},
                DatabaseHelper.COL_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)},
                null, null, null, "1"
        );
        int capacity = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return capacity;
    }
}
