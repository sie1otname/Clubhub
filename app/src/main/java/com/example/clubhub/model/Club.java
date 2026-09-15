package com.example.clubhub.model;

public class Club {
    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final String organizerEmail;

    public Club(int id, String name, String description, String category, String organizerEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.organizerEmail = organizerEmail;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getOrganizerEmail() { return organizerEmail; }

    @Override
    public String toString() {
        return name + (category == null || category.isEmpty() ? "" : " • " + category);
    }
}
