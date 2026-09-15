# ClubHub

ClubHub is an Android event and club management application built in Java. It provides role-based experiences for administrators, organizers, and members, with local persistence using SQLite.

## What the app does

- **Administrators** can manage member accounts and reset demo data.
- **Organizers** can create clubs, create events, manage registrations, confirm attendance, and view event analytics.
- **Members** can discover events, register or cancel, track their registrations, and leave feedback after attendance is confirmed.

## Core features

- Role-based authentication and navigation
- Club creation and organizer ownership
- Event CRUD with club assignment, location, date, and capacity
- Event registration with duplicate/capacity protection
- Attendance tracking
- Post-event ratings and feedback
- Organizer analytics: registrations, attendance rate, and average ratings
- SQLite persistence for clubs, events, and registrations

## Tech stack

- Java 11
- Android SDK / Android Studio
- SQLite (`SQLiteOpenHelper`)
- AndroidX Fragments and RecyclerView
- Material Components
- Gradle

## Architecture

The app separates responsibilities into four main layers:

```text
UI (Activities / Fragments / Adapters)
                ↓
             Models
                ↓
              DAOs
                ↓
       SQLite / DatabaseHelper
```

Main packages:

```text
com.example.clubhub
├── auth
│   ├── AuthManager
│   └── UserManager
├── data
│   ├── ClubDAO
│   ├── EventDAO
│   ├── RegistrationDAO
│   └── DatabaseHelper
├── model
│   ├── Club
│   ├── Event
│   ├── Registration
│   ├── Role
│   └── User
└── ui
    ├── admin
    ├── common
    ├── organizer
    └── member
```

## Data model

```text
Organizer
   │
   └── Club
        │
        └── Event
             │
             └── Registration ── Member
                    │
                    ├── attendance
                    ├── rating
                    └── feedback
```

## Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Administrator | `admin@clubhub.com` | `1234` |
| Organizer | `organizer@clubhub.com` | `organizerpass` |
| Organizer 2 | `organizer2@clubhub.com` | `organizer2pass` |
| Member | `member@clubhub.com` | `memberpass` |

These credentials are demo-only and intentionally stored locally for the MVP.

## Run locally

1. Clone or download the repository.
2. Open the project root in Android Studio.
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or Android device running API 24+.
5. Sign in with one of the demo accounts above.

## Suggested demo flow

1. Sign in as an organizer and create a club.
2. Create an event under that club.
3. Sign in as a member and register for the event.
4. Return as the organizer and confirm the member's attendance.
5. Return as the member and submit a rating/feedback.
6. Open the organizer analytics screen to see the updated statistics.

## Current MVP limitations

- User accounts are kept in memory for this demo build; clubs, events, and registrations are persisted in SQLite.
- Event dates are currently stored as formatted text rather than a dedicated date/time object.
- This build is local-first and does not yet use a remote backend.

## Possible next versions

- QR-code event check-in
- Push notifications
- Cloud authentication/database
- AI-powered feedback summaries
- Event recommendations
- Improved automated tests

## Background

ClubHub was refactored from an earlier Android course project into a standalone event-management application. The refactor focused on domain modeling, role-based workflows, DAO-based data access, and incremental migration while keeping the application functional after each phase.
