#! /bin/bash

task() {
    echo "[INFO] Compiling Java sources..."

    find src -iname "*.java" > sources.txt
    javac -d bin -cp "lib/*" @sources.txt
    rm -f sources.txt

    echo "[INFO] Building JAR file..."
    jar cf out/Framework.jar -C bin/ .

    echo "[INFO] Exporting JAR file to Demo app..."
    cp -f out/Framework.jar ../../Testing/Demo/lib/Framework.jar
}

task

