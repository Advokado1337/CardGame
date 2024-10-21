$SRC_DIR = "src"
$BUILD_DIR = "build"
$LIB_DIR = "lib"
$MAIN_CLASS = "main.PointSalad"  # Adjust according to the correct package name

# Create the build directory if it doesn't exist
if (-not (Test-Path $BUILD_DIR)) {
    New-Item -ItemType Directory -Path $BUILD_DIR | Out-Null
}

# Create a classpath string that includes all .jar files in the lib directory
$LIB_CP = ""
if (Test-Path $LIB_DIR) {
    $LIB_CP = (Get-ChildItem -Filter *.jar -Path $LIB_DIR | ForEach-Object { $_.FullName }) -join ";"
}

# Find and compile all Java source files
Write-Host "Compiling Java source files..."
$sourceFiles = Get-ChildItem -Recurse -Filter *.java -Path $SRC_DIR | ForEach-Object { $_.FullName }
& javac -cp $LIB_CP -d $BUILD_DIR $sourceFiles

# Check if the compilation was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful."
    
    # Debugging: Check for compiled files
    if (-Not (Test-Path "$BUILD_DIR/main/PointSalad.class")) {
        Write-Host "Error: main.PointSalad class not found in build directory."
        exit 1
    }

    # Run the main class with the correct classpath
    Write-Host "Running the $MAIN_CLASS..."
    $classpath = "$BUILD_DIR;$LIB_CP"
    & java -cp $classpath $MAIN_CLASS $args
} else {
    Write-Host "Compilation failed."
}