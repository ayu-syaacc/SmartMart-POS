@echo off
cd /d "%~dp0"
if not exist SmartMartPOS.jar call build.bat
java -jar SmartMartPOS.jar
