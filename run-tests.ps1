# Create the build directory if it doesn't exist
if (-Not (Test-Path -Path "build")) {
    New-Item -ItemType Directory -Path "build"
}

# Compile the source code and tests
# Assuming your Java source is in /src and your tests are in /test
Write-Host "Compiling source code and tests..."

javac -cp "lib/*" -d build $(Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!"
    exit 1
}

Write-Host "Compilation successful!"

# Run the tests using JUnit 5's ConsoleLauncher
Write-Host "Running tests..."

java -cp "lib/*;build" org.junit.platform.console.ConsoleLauncher --scan-classpath --classpath build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed!"
    exit 1
}

Write-Host "All tests passed!"
