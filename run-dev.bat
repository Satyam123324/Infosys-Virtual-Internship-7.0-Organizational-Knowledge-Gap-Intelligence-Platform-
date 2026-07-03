@echo off
REM ============================================================
REM  run-dev.bat
REM  Forces JDK 17 for this project ONLY (does not touch your
REM  system settings). Just double-click this file, or run it
REM  from the terminal, every time you want to start the app.
REM ============================================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java:
java -version

echo.
echo Starting Spring Boot application (dev profile, H2 in-memory DB)...
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause
