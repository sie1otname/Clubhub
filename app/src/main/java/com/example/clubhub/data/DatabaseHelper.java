package com.example.clubhub.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ClubHub.db";
    private static final int DATABASE_VERSION = 7;

    // ===== Clubs =====
    public static final String TABLE_CLUB = "Club";
    public static final String COL_CLUB_ID = "id";
    public static final String COL_CLUB_NAME = "name";
    public static final String COL_CLUB_DESCRIPTION = "description";
    public static final String COL_CLUB_CATEGORY = "category";
    public static final String COL_CLUB_ORGANIZER_EMAIL = "organizer_email";

    // ===== Events =====
    public static final String TABLE_EVENT = "Event";
    public static final String COL_EVENT_ID = "id";
    public static final String COL_EVENT_CLUB_ID = "club_id";
    public static final String COL_EVENT_ORGANIZER_EMAIL = "organizer_email";
    public static final String COL_EVENT_TITLE = "title";
    public static final String COL_EVENT_DESCRIPTION = "description";
    public static final String COL_EVENT_LOCATION = "location";
    public static final String COL_EVENT_DATE = "date";
    public static final String COL_EVENT_CAPACITY = "capacity";

    // ===== Registrations =====
    public static final String TABLE_REGISTRATION = "Registration";
    public static final String COL_REGISTRATION_ID = "id";
    public static final String COL_REGISTRATION_EVENT_ID = "event_id";
    public static final String COL_REGISTRATION_MEMBER_EMAIL = "member_email";
    public static final String COL_REGISTRATION_STATUS = "status";
    public static final String COL_REGISTRATION_DATE = "registration_date";
    public static final String COL_REGISTRATION_ATTENDED = "attended";
    public static final String COL_REGISTRATION_RATING = "rating";
    public static final String COL_REGISTRATION_FEEDBACK = "feedback";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_CLUB + " (" +
                        COL_CLUB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CLUB_NAME + " TEXT NOT NULL, " +
                        COL_CLUB_DESCRIPTION + " TEXT, " +
                        COL_CLUB_CATEGORY + " TEXT, " +
                        COL_CLUB_ORGANIZER_EMAIL + " TEXT NOT NULL" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_EVENT + " (" +
                        COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_EVENT_CLUB_ID + " INTEGER NOT NULL, " +
                        COL_EVENT_ORGANIZER_EMAIL + " TEXT NOT NULL, " +
                        COL_EVENT_TITLE + " TEXT NOT NULL, " +
                        COL_EVENT_DESCRIPTION + " TEXT, " +
                        COL_EVENT_LOCATION + " TEXT NOT NULL, " +
                        COL_EVENT_DATE + " TEXT NOT NULL, " +
                        COL_EVENT_CAPACITY + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + COL_EVENT_CLUB_ID + ") REFERENCES " + TABLE_CLUB + "(" + COL_CLUB_ID + ") ON DELETE CASCADE" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_REGISTRATION + " (" +
                        COL_REGISTRATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_REGISTRATION_EVENT_ID + " INTEGER NOT NULL, " +
                        COL_REGISTRATION_MEMBER_EMAIL + " TEXT NOT NULL, " +
                        COL_REGISTRATION_STATUS + " TEXT NOT NULL, " +
                        COL_REGISTRATION_DATE + " INTEGER NOT NULL, " +
                        COL_REGISTRATION_ATTENDED + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_REGISTRATION_RATING + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_REGISTRATION_FEEDBACK + " TEXT, " +
                        "UNIQUE(" + COL_REGISTRATION_EVENT_ID + ", " + COL_REGISTRATION_MEMBER_EMAIL + "), " +
                        "FOREIGN KEY(" + COL_REGISTRATION_EVENT_ID + ") REFERENCES " + TABLE_EVENT + "(" + COL_EVENT_ID + ") ON DELETE CASCADE" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATION + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUB + ";");
        db.execSQL("DROP TABLE IF EXISTS Users;");
        onCreate(db);
    }

    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_REGISTRATION, null, null);
            db.delete(TABLE_EVENT, null, null);
            db.delete(TABLE_CLUB, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
