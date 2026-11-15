@REM Maven Wrapper Script for Windows
@echo off
setlocal
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
)
set MAVEN_CMD="C:\Users\Arun vemula\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
if exist %MAVEN_CMD% (
    call %MAVEN_CMD% %*
) else (
    call mvn %*
)
exit /b %ERRORLEVEL%
