#! /bin/bash

task() {
    echo "[INFO] Compiling Framework with Maven (Java 21)..."
    mvn clean compile

    echo "[INFO] Building with Maven (Java 21)..."
    mvn clean package

    echo "[INFO] Copying JAR to out/ ..."
    mkdir -p out
    cp -f target/Framework.jar out/Framework.jar

    echo "[INFO] Exporting JAR file to Demo app..."
    cp -f out/Framework.jar ../../Testing/Demo/lib/Framework.jar
}

task

