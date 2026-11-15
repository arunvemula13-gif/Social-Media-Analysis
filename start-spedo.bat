@echo off
title SPEDO Social Media Analytics Server
echo ===================================================
echo   Starting SPEDO Intelligence Platform on Port 8080
echo ===================================================
echo.
cd /d "%~dp0"
java -jar target\spedo-analytics-1.0.0.jar
pause
