@echo off
REM ============================================================
REM  run-prod.bat
REM  Runs the app against your local PostgreSQL database
REM  (reads credentials from .env file in this folder)
REM ============================================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Loading environment variables from .env ...

if not exist ".env" (
    echo ERROR: .env file not found in this folder!
    echo Please copy .env.example to .env and fill in your PostgreSQL password.
    pause
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    set "%%A=%%B"
)

echo Using Java:
java -version

echo.
echo Starting Spring Boot application (PostgreSQL, production-style profile)...
echo.

mvn spring-boot:run

pause
