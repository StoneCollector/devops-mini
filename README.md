# Smart Catering Services

A Spring Boot 3.3 (Java 21) web application for automating catering slot management, slot booking, vendor confirmation/rejection, customer cancellations, and system-wide administration. Built as a comprehensive DevOps project covering the full lifecycle from Git workflows through Jenkins CI/CD, Selenium testing, Docker containerization, and Ansible provisioning.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.3 (Spring MVC, Spring Data JPA)
- **Build Tool**: Maven 3.8+
- **Frontend / Templating**: Thymeleaf, semantic HTML5, custom vanilla CSS (responsive, clean design system with no external UI framework dependencies)
- **Database**: MySQL 8 (database name: `cbms_db`)
- **Server**: Embedded Apache Tomcat (port `8082`)

## User Roles & Capabilities

1. **Customer**:
   - Register and login with email and password.
   - Browse upcoming catering slots with date and menu/cuisine filtering.
   - View slot vendor names and real-time available capacity.
   - Book catering slots with customized guest counts.
   - View "My Bookings" reservation history with live status (`PENDING`, `CONFIRMED`, `CANCELLED`).
   - Cancel active bookings with a confirmation step, automatically releasing and restoring slot capacity.

2. **Vendor**:
   - Register and login as a catering Vendor.
   - Create, publish, edit, and delete catering slots (`date`, `timeSlot`, `menuType`, `capacity`).
   - View "My Slots" dashboard with live booked vs remaining capacity statistics.
   - Slot deletion protection: Slots with active bookings cannot be deleted.
   - View "Bookings on My Slots" dashboard showing customer reservations for their slots.
   - Confirm (`CONFIRMED`) or reject (`CANCELLED`) pending customer bookings, with automatic capacity restoration on rejection.

3. **Admin**:
   - Oversight dashboard to monitor the entire system.
   - User Management: View all registered users (Customers, Vendors, Admins) and activate or deactivate accounts.
   - Bookings Directory: System-wide read-only view of all catering reservations.

## Prerequisites

- Java 21 JDK
- Maven 3.8+
- MySQL 8

## Setup Local Database

1. Start your local MySQL 8 server on port `3306`.
2. Create the database:
   ```sql
   CREATE DATABASE cbms_db;
   ```
3. Set the MySQL database password via environment variable before running:
   - **PowerShell**:
     ```powershell
     $env:SPRING_DATASOURCE_PASSWORD="your_mysql_password"
     ```
   - **Command Prompt**:
     ```cmd
     set SPRING_DATASOURCE_PASSWORD=your_mysql_password
     ```
   - **Bash / Linux / macOS**:
     ```bash
     export SPRING_DATASOURCE_PASSWORD=your_mysql_password
     ```

## Running Locally

To run the application locally:
```bash
mvn spring-boot:run
```
Or execute `./run.bat` on Windows.

Once started, access the application in your browser at:
`http://localhost:8082`

### Seed Accounts (Out of the Box)
- **Admin**: `admin@smartcatering.com` / `admin123`
- **Vendor**: `chef.mario@smartcatering.com` / `vendor123`
- **Customer**: `customer@example.com` / `customer123`

## Testing

### Unit Tests
Unit tests run via Surefire and exclude integration tests:
```bash
mvn test
```

### Selenium WebDriver Integration Tests
The integration test suite utilizes Selenium 4.x, WebDriverManager (automatic Chrome/ChromeDriver resolution), JUnit 5, AssertJ, and the Page Object Model (POM). Tests run via `maven-failsafe-plugin` during the `verify` phase.

1. **Standard Execution (Headless Mode - Recommended for CI/CD):**
   ```bash
   mvn verify "-Dheadless=true"
   ```

2. **Graphical Browser Mode (Local Development):**
   ```bash
   mvn verify
   ```

3. **Running Against an Already Running Application:**
   If the application is already running on a specific port (e.g., `8085` or `8082`):
   ```bash
   mvn verify "-Dapp.baseUrl=http://localhost:8085" "-Dheadless=true"
   ```
   *If `app.baseUrl` is not supplied, Spring Boot automatically boots an embedded Tomcat instance on a random available port.*

4. **Running an Individual Integration Test Journey:**
   ```bash
   # Journey 1: Customer registration, login, profile validation, and logout
   mvn verify "-Dit.test=AuthFlowIT" "-Dheadless=true"

   # Journey 2: Invalid credentials, error alerts, and unauthenticated redirects
   mvn verify "-Dit.test=InvalidLoginAndAccessIT" "-Dheadless=true"

   # Journey 3: Customer books a slot, verifies PENDING status, and checks capacity decrease
   mvn verify "-Dit.test=CustomerBookingIT" "-Dheadless=true"

   # Journey 4: Vendor creates slot, verifies in vendor list, and confirms customer visibility
   mvn verify "-Dit.test=VendorSlotCreationIT" "-Dheadless=true"

   # Journey 5: Customer booking confirmation lifecycle by vendor
   mvn verify "-Dit.test=VendorConfirmBookingIT" "-Dheadless=true"
   ```

5. **Screenshot on Failure Demo:**
   A deliberate failing test tagged with `@Tag("demo")` is excluded from regular builds. To run it and demonstrate automatic failure screenshot capture:
   ```bash
   mvn verify "-Dgroups=demo" "-Dheadless=true"
   ```
   Failure screenshots are saved to:
   `target/screenshots/{ClassName}_{methodName}_{yyyyMMdd_HHmmss}.png`

6. **HTML Test Reports:**
   After running `mvn verify`, the aggregated Failsafe HTML report is available at:
   `target/site/failsafe-report.html`


## Branch Naming Rules

This repository follows a consistent branching convention:

| Branch | Purpose |
|---|---|
| `main` | Stable, deployable code only |
| `develop` | Integration branch for merged features |
| `feature/<short-description>` | New functionality, e.g. `feature/vendor-role-and-rebrand` |
| `bugfix/<short-description>` | Non-critical fixes, e.g. `bugfix/slot-capacity-count` |
| `hotfix/<short-description>` | Urgent fixes applied directly against `main` |
| `release/<version>` | Release staging, e.g. `release/1.0.0` |

**Rules:**
- All feature/bugfix branches are cut from `develop`, not `main`
- Branch names are lowercase, hyphen-separated, no spaces or underscores
- Merge into `develop` via pull request only — no direct pushes to `develop` or `main`
- `main` only receives merges from `release/*` or `hotfix/*` branches
- Delete feature branches after merge to keep the branch list clean