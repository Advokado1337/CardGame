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

            // If the server is asking for an action, prompt the player and send the input
            // back
            if (serverMessage.contains("Choose a pile")) {
                // Use helper method for input
                String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                        "\\d|[A-Fa-f]{1,2}");
                outToServer.writeObject(playerAction);

                // Handle invalid selection case
            } else if (serverMessage.contains("Invalid selection") || serverMessage.contains("Invalid pile")) {
                System.out.println("Invalid selection! Please choose again.");
                String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                        "\\d|[A-Fa-f]{1,2}");
                outToServer.writeObject(playerAction);

                // Handle empty pile or veggie card
            } else if (serverMessage.contains("The selected pile or veggie card is empty")) {
                System.out
                        .println("Invalid selection! The selected pile or veggie card is empty. Please choose again.");
                String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                        "\\d|[A-Fa-f]{1,2}");
                outToServer.writeObject(playerAction);

                // Second action (flip a criteria card)
            } else if (serverMessage.contains("Do you want to flip a criteria card")) {
                String playerAction = getValidInput(scanner, "Enter your action (flip: yes/no): ",
                        "(?i)yes|no");
                outToServer.writeObject(playerAction);

                // Handle card flipping with index
            } else if (serverMessage.contains("Enter the index")) {
                String playerAction = getValidInput(scanner, "Enter the index (index): ",
                        "\\d+");
                outToServer.writeObject(playerAction);

                // Handle end of game with final scores
            } else if (serverMessage.contains("Final scores")) {
                clientSocket.close(); // close connections
                System.out.println(serverMessage);
                break;

            } else if (serverMessage.contains("No point card available in this pile. Try again.")) {
                // Prompt the player to enter a valid action again
                String playerAction = getValidInput(scanner,
                        "No point card available in this pile. Please choose another pile or veggie card: ",
                        "\\d|[A-Fa-f]{1,2}");
                outToServer.writeObject(playerAction);
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
