package network;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private Socket clientSocket;
    private ObjectOutputStream outToServer;
    private ObjectInputStream inFromServer;

    public GameClient(String ipAddress, int port) throws IOException {
        this.clientSocket = new Socket(ipAddress, port);
        this.outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.inFromServer = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void start() throws IOException, ClassNotFoundException {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Read message from the server
            String serverMessage = (String) inFromServer.readObject();
            System.out.println("Server: " + serverMessage);

            // Handle player action prompt for pile or veggie selection
            if (serverMessage.contains("Choose a pile")) {
                String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                        "\\d|[A-Fa-f]{1,2}");
                outToServer.writeObject(playerAction);
            }
            // Handle flipping card prompt
            else if (serverMessage.contains("Do you want to flip a criteria card")) {
                String playerAction = getValidInput(scanner, "Enter your action (yes/no): ", "(?i)yes|no");
                outToServer.writeObject(playerAction);
            }
            // Handle card index prompt
            else if (serverMessage.contains("Enter the index")) {
                String playerAction = getValidInput(scanner, "Enter the index (number): ", "\\d+");
                outToServer.writeObject(playerAction);
            }
            // Handle "try again" or reprompt situations
            else if (serverMessage.contains("Invalid input") || serverMessage.contains("Try again")) {
                System.out.println("Invalid input. Please try again.");
            }
            // Handle game end condition
            else if (serverMessage.contains("Final scores")) {
                System.out.println(serverMessage);
                clientSocket.close();
                break;
            }
        }
    }

    // Helper method to get valid input (no restructuring, just simplifying
    // reprompt)
    private String getValidInput(Scanner scanner, String prompt, String validPattern) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine();
            if (input.matches(validPattern)) {
                return input;
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }
}
