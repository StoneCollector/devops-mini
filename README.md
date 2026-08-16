# Catering Booking and Management System (CBMS)

A Spring Boot 3.3 (Java 21) web application for automating catering slot booking, confirmation, cancellation, and status tracking. Built as the MVP for a 14-week DevOps course project, covering the full lifecycle from Git through Jenkins CI/CD, Selenium testing, Docker, and Ansible provisioning.

## Tech Stack

- Java 21, Spring Boot 3.3
- Maven (build tool)
- Plain HTML/CSS (server-rendered templates, no frontend framework)
- MySQL 8
- Deployed via embedded Tomcat

## Prerequisites

- Java 21
- Maven 3.8+
- MySQL 8

## Setup Local Database

If MySQL Server isn't installed yet (Windows, zip distribution):

1. Extract the MySQL zip and add its `bin` folder to your system `PATH`.
2. Create a `my.ini` config file in the MySQL install folder:
   ```ini
   [mysqld]
   basedir=C:/mysql
   datadir=C:/mysql/data
   port=3306
   ```
3. Initialize the data directory (run as Administrator from the `bin` folder):
   ```
   mysqld --initialize --console
   ```
   Note the temporary root password printed in the console output.
4. Install and start the service:
   ```
   mysqld --install MySQL80
   net start MySQL80
   ```
5. Log in with the temporary password and set a real one:
   ```
   mysql -u root -p
   ```
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'yourNewPassword';
   ```

Then create the database for this application:
```sql
CREATE DATABASE cbms_db;
```

By default, the application expects to connect to `localhost:3306` with username `root` and the password set above — update `application.properties` or use the environment variables below rather than relying on an empty password.

## Environment Variables

You can override database settings using environment variables:
- `SPRING_DATASOURCE_URL` (e.g., `jdbc:mysql://localhost:3306/cbms_db`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Running Locally

To run the application locally:
```bash
mvn spring-boot:run
```

Once running, the application will be available at `http://localhost:8080`.

## Testing

To run unit tests:
```bash
mvn test
```

To run integration tests (if any are added):
```bash
mvn verify
```

## Branch Naming Rules

This repository follows a consistent branching convention:

| Branch | Purpose |
|---|---|
| `main` | Stable, deployable code only |
| `develop` | Integration branch for merged features |
| `feature/<short-description>` | New functionality, e.g. `feature/user-registration` |
| `bugfix/<short-description>` | Non-critical fixes, e.g. `bugfix/slot-capacity-count` |
| `hotfix/<short-description>` | Urgent fixes applied directly against `main` |
| `release/<version>` | Release staging, e.g. `release/1.0.0` |

**Rules:**
- All feature/bugfix branches are cut from `develop`, not `main`
- Branch names are lowercase, hyphen-separated, no spaces or underscores
- Merge into `develop` via pull request only — no direct pushes to `develop` or `main`
- `main` only receives merges from `release/*` or `hotfix/*` branches
- Delete feature branches after merge to keep the branch list clean