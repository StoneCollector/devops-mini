@echo off
setlocal

title Smart Catering Services - Local Server
cd /d "%~dp0"

echo ===============================================================================
echo            SMART CATERING SERVICES - APPLICATION LAUNCHER
echo ===============================================================================
echo.

REM 1. Database Configuration
if "%SPRING_DATASOURCE_USERNAME%"=="" set "SPRING_DATASOURCE_USERNAME=root"
if "%SPRING_DATASOURCE_PASSWORD%"=="" set "SPRING_DATASOURCE_PASSWORD=DOPSnet"
if "%SPRING_DATASOURCE_URL%"=="" set "SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/cbms_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false"

echo [1/4] Environment Configuration:
echo       * Database User: %SPRING_DATASOURCE_USERNAME%
echo       * Web Server Port: 8082
echo.

REM 2. Pre-flight Checks
echo [2/4] Checking prerequisites...
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java JDK was not found in system PATH.
    echo         Please install JDK 17+ and add its bin folder to PATH.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven was not found in system PATH.
    echo         Please install Apache Maven and add its bin folder to PATH.
    pause
    exit /b 1
)
echo       * Java and Maven detected successfully.
echo.

REM 3. Port Conflict Resolution (Port 8082)
echo [3/4] Checking port 8082 availability...
for /f "tokens=5" %%p in ('netstat -aon 2^>nul ^| findstr ":8082" ^| findstr "LISTENING"') do (
    echo [INFO] Detected existing process on port 8082 with PID %%p. Freeing port...
    taskkill /F /PID %%p >nul 2>&1
    timeout /t 1 /nobreak >nul
)
echo       * Port 8082 is ready.
echo.

REM 4. Optional Database Seeding
if /i "%~1"=="--seed" goto do_seed
if /i "%~1"=="seed" goto do_seed
goto start_app

:do_seed
echo [SEED] Importing comprehensive mock dataset into cbms_db...
where mysql >nul 2>&1
if not errorlevel 1 (
    mysql -u %SPRING_DATASOURCE_USERNAME% -p%SPRING_DATASOURCE_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS cbms_db;" >nul 2>&1
    mysql -u %SPRING_DATASOURCE_USERNAME% -p%SPRING_DATASOURCE_PASSWORD% cbms_db < seed_mock_data.sql
    if not errorlevel 1 (
        echo [SEED] SUCCESS: Mock database seeded with vendors, customers, and bookings!
    ) else (
        echo [SEED] Warning: Seed script exited with an error. Continuing launch...
    )
) else (
    echo [SEED] Warning: mysql CLI not found in PATH. Skipping SQL seed.
)
echo.

:start_app
echo [4/4] Starting Smart Catering Services on http://localhost:8082 ...
echo       * Application URL: http://localhost:8082
echo       * Credentials:     See TEST_CREDENTIALS.md for sample logins
echo       * Stop Server:     Press Ctrl+C in this terminal
echo ===============================================================================
echo.

call mvn spring-boot:run

if errorlevel 1 (
    echo.
    echo [ERROR] Application failed to start or exited with an error.
)

pause