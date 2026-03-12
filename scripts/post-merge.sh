#!/bin/bash
set -e

export JAVA_HOME=$(dirname $(dirname $(which java)))
echo "Post-merge: Running Gradle build..."
./gradlew build --no-daemon -q 2>&1 | tail -5
echo "Post-merge: Build complete."
