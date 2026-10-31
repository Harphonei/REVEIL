@echo off
cd /d "%~dp0"
java -jar "dist\REVEIL.jar"
if errorlevel 1 pause
