#!/bin/sh
# Kompilasi SmartMart POS (butuh JDK 11 atau lebih baru)
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac -encoding UTF-8 -d out src/smartmart/*.java
jar cfe SmartMartPOS.jar smartmart.Main -C out .
echo "Selesai: SmartMartPOS.jar"
