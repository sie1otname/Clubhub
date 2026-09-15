# ClubHub Phase 6 — Portfolio cleanup

- Removed obsolete restaurant, recipe, ingredient, and nutrition code.
- Renamed package from `com.example.utaste` to `com.example.clubhub`.
- Flattened UI packages into `admin`, `organizer`, `member`, and `common`.
- Renamed role-specific layouts to `activity_organizer.xml` and `activity_member.xml`.
- Moved organizer analytics into the organizer package.
- Unified `AuthManager` and `UserManager` around one demo-user source of truth.
- Removed the unused SQLite `Users` table and old network permission.
- Updated login/admin branding and application theme.
- Replaced the old course-project README with a recruiter-friendly ClubHub README.
