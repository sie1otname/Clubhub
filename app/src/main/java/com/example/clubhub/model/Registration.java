package com.example.clubhub.model;

public class Registration {
    public static final String STATUS_REGISTERED = "REGISTERED";

    private final int id;
    private final int eventId;
    private final String memberEmail;
    private final String status;
    private final long registrationDate;
    private final boolean attended;
    private final int rating;
    private final String feedback;

    public Registration(int id, int eventId, String memberEmail, String status,
                        long registrationDate, boolean attended, int rating, String feedback) {
        this.id = id;
        this.eventId = eventId;
        this.memberEmail = memberEmail;
        this.status = status;
        this.registrationDate = registrationDate;
        this.attended = attended;
        this.rating = rating;
        this.feedback = feedback;
    }

    public int getId() { return id; }
    public int getEventId() { return eventId; }
    public String getMemberEmail() { return memberEmail; }
    public String getStatus() { return status; }
    public long getRegistrationDate() { return registrationDate; }
    public boolean isAttended() { return attended; }
    public int getRating() { return rating; }
    public String getFeedback() { return feedback; }
    public boolean hasFeedback() { return rating > 0; }
}
