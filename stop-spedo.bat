@echo off
title Stop SPEDO Server
color 0C
echo ==========================================================
echo               Stopping SPEDO Server
echo ==========================================================
echo.

setlocal enabledelayedexpansion
set FOUND=0

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo [*] Terminating process PID %%a listening on port 8080...
    taskkill /F /PID %%a >nul 2>&1
    set FOUND=1
)

:: Also terminate any stray java processes running spedo JAR and clean stale lock
wmic process where "commandline like '%%spedo-analytics%%'" call terminate >nul 2>&1
if exist "data\spedo_db.lock.db" del /f /q "data\spedo_db.lock.db" >nul 2>&1

if %FOUND%==1 (
    echo.
    echo [SUCCESS] SPEDO server stopped. Port 8080 and database are now free.
) else (
    echo [*] Server stopped. Port 8080 and database are now free.
)

timeout /t 2 >nul
