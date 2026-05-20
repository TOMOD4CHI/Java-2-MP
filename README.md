# Auto-École Management System

Desktop application for managing a driving school: candidates, instructors, vehicles, sessions, exams, payments, and documents. Built with JavaFX and backed by a MySQL database.

## Tech Stack
- Java 17
- JavaFX 17 (FXML UI)
- Maven
- MySQL (JDBC)
- iText 8.0.5 (PDF invoices/receipts)

## Project Structure
- src/main/java/org/cpi2
  - controllers: JavaFX controllers for each screen
  - entities: domain models (candidate, session, vehicle, payment, etc.)
  - service: business logic and orchestration
  - repository: JDBC data access and DatabaseConfig
  - utils: validation, PDF generators, event utilities
- src/main/resources
  - fxmls: UI layouts
  - css: styling
  - images: assets
- autoecole.sql: database schema and seed data
- external_dependencies: optional local JARs (for manual classpath setups)

## Architecture & Workflow
1. `org.cpi2.Main` loads the login view (`login.fxml`).
2. Controllers handle UI events and call services.
3. Services enforce business rules and coordinate data operations.
4. Repositories access MySQL via JDBC using `DatabaseConfig`.
5. PDF outputs (invoices/receipts/reports) are generated through utilities.

## Local Setup
1. Install Java 17, Maven, and MySQL.
2. Create a database named `autoecole` and import `autoecole.sql`.
3. Update connection settings in `src/main/java/org/cpi2/repository/DatabaseConfig.java` if needed.
4. Build the project:
   - `mvn test` (runs tests only)
   - `mvn package` (builds the jar)
5. Run the application by launching `org.cpi2.Main` from your IDE (JavaFX application).
