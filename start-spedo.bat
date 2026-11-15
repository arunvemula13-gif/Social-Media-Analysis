@echo off
title SPEDO Social Media Analytics Server
color 0B
echo ==========================================================
echo       SPEDO Social Media Analytics - Host Runner
echo ==========================================================
echo.

cd /d "%~dp0"

:: 1. Clear any zombie process on port 8080 and stale H2 database locks
echo [*] Checking port 8080 and database locks...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo [!] Found stale process %%a on port 8080. Freeing port...
    taskkill /F /PID %%a >nul 2>&1
)
if exist "data\spedo_db.lock.db" del /f /q "data\spedo_db.lock.db" >nul 2>&1

:: 2. Locate Java 17+ binary
set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe"
)

echo [*] Using Java runtime: "%JAVA_CMD%"
"%JAVA_CMD%" -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not found. Please install Java 17 or higher.
    pause
    exit /b 1
)

:: 3. Check for packaged JAR
if not exist "target\spedo-analytics-1.0.0.jar" (
    echo [*] Packaging project JAR with Maven...
    call mvnw.cmd package -DskipTests
)

:: 4. Start the Server with memory protection & auto-restart
echo [*] Starting SPEDO Server on http://localhost:8080 ...
echo [*] Press Ctrl+C in this window to stop the server.
echo.

:run_server
"%JAVA_CMD%" -Xms128m -Xmx512m -XX:+UseG1GC -jar target\spedo-analytics-1.0.0.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Server process exited unexpectedly with code %ERRORLEVEL%.
    echo [*] Restarting in 3 seconds... (Press Ctrl+C to cancel)
    timeout /t 3 /nobreak >nul
    goto run_server
)

pause
