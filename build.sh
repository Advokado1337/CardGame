#!/bin/bash

# Determine path separator based on OS
if [ "$(uname)" = "Linux" ] || [ "$(uname)" = "Darwin" ]; then
    SEP=":"
else
    SEP=";"
fi

# Variables for directories and files
SRC_DIR="src"
BUILD_DIR="build"
LIB_DIR="lib"
MAIN_CLASS="main.PointSalad" # Specify the correct package for PointSalad

# Create the build directory if it doesn't exist
mkdir -p "$BUILD_DIR"

# Create a classpath string that includes all .jar files in the lib directory
LIB_CP=$(find "$LIB_DIR" -name "*.jar" | tr '\n' "$SEP")

# Find and compile all Java source files
echo "Compiling Java source files..."
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$LIB_CP" -d "$BUILD_DIR" @sources.txt

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."

    # Run the main class with the correct classpath and pass all script arguments
    echo "Running the $MAIN_CLASS..."
    java -cp "$BUILD_DIR$SEP$LIB_CP" "$MAIN_CLASS" "$@"

else
    echo "Compilation failed."
fi
