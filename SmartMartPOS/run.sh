#!/bin/sh
cd "$(dirname "$0")"
[ -f SmartMartPOS.jar ] || ./build.sh
java -jar SmartMartPOS.jar
