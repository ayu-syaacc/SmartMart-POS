@echo off
rem Kompilasi SmartMart POS (butuh JDK 11 atau lebih baru)
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -d out src\smartmart\*.java
if errorlevel 1 exit /b 1
jar cfe SmartMartPOS.jar smartmart.Main -C out .
echo Selesai: SmartMartPOS.jar
