package com.example.clubhub.model;

public class Event {
    private final int id;
    private final int clubId;
    private final String clubName;
    private final String organizerEmail;
    private final String title;
    private final String description;
    private final String location;
    private final String date;
    private final int capacity;

    public Event(int id, int clubId, String clubName, String organizerEmail, String title,
                 String description, String location, String date, int capacity) {
        this.id = id;
        this.clubId = clubId;
        this.clubName = clubName;
        this.organizerEmail = organizerEmail;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.capacity = capacity;
    }

    public int getId() { return id; }
    public int getClubId() { return clubId; }
    public String getClubName() { return clubName; }
    public String getOrganizerEmail() { return organizerEmail; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        String club = (clubName == null || clubName.isEmpty()) ? "Club" : clubName;
        return title + "\n" + club + " • " + date + " • " + location;
    }
}
