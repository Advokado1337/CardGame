Prerequisites
Before you begin, ensure you have the following installed on your machine:

Java Development Kit (JDK) 8 or higher
Git
PowerShell

Running the Tests
Ensure you are in the project directory.
Run the tests using the following command:
./run-test.ps1
This command will execute all the tests in the project.

Playing the Game
Ensure you are in the project directory.
To start the game server, use the following command:
./build.ps1 server <number-of-players> <number-of-bots>
Replace <number-of-players> with the number of human players and <number-of-bots> with the number of bot players. For example:
./build.ps1 server 2 1

To connect as a client, use the following command:
./build.ps1 client 127.0.0.1

This command connects to the server running on 127.0.0.1 (localhost).
