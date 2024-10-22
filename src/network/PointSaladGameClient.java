package network;

import java.io.IOException;
import java.util.Scanner;

public class PointSaladGameClient extends GameClient {

    public PointSaladGameClient(String ipAddress, int port) throws IOException {
        super(ipAddress, port);
    }

    @Override
    protected void handleServerMessage(String serverMessage, Scanner scanner) throws IOException {
        if (serverMessage.contains("Choose a pile")) {
            String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                    "\\d|[A-Fa-f]{1,2}");
            outToServer.writeObject(playerAction);

        } else if (serverMessage.contains("Invalid selection") || serverMessage.contains("Invalid pile")) {
            System.out.println("Invalid selection! Please choose again.");
            String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                    "\\d|[A-Fa-f]{1,2}");
            outToServer.writeObject(playerAction);

        } else if (serverMessage.contains("The selected pile or veggie card is empty")) {
            System.out.println("Invalid selection! The selected pile or veggie card is empty. Please choose again.");
            String playerAction = getValidInput(scanner, "Enter your action (pile number or veggie card letters): ",
                    "\\d|[A-Fa-f]{1,2}");
            outToServer.writeObject(playerAction);

        } else if (serverMessage.contains("Do you want to flip a criteria card")) {
            String playerAction = getValidInput(scanner, "Enter your action (flip: yes/no): ",
                    "(?i)yes|no");
            outToServer.writeObject(playerAction);

        } else if (serverMessage.contains("Enter the index")) {
            String playerAction = getValidInput(scanner, "Enter the index (index): ",
                    "\\d+");
            outToServer.writeObject(playerAction);

        } else if (serverMessage.contains("Final scores")) {
            clientSocket.close(); // close connections
            System.out.println(serverMessage);

        } else if (serverMessage.contains("No point card available in this pile. Try again.")) {
            String playerAction = getValidInput(scanner,
                    "No point card available in this pile. Please choose another pile or veggie card: ",
                    "\\d|[A-Fa-f]{1,2}");
            outToServer.writeObject(playerAction);
        }
    }
}