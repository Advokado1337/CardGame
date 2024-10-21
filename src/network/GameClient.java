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
            // First action
            if (serverMessage.contains("Choose a pile")) {
                System.out.print("Enter your action (pile number or veggie card letters): ");
                String playerAction = scanner.nextLine();

                // Send the player action back to the server
                outToServer.writeObject(playerAction);

                // Second action
            } else if (serverMessage.contains("Do you want to flip a criteria card")) {
                System.out.print("Enter your action (flip): ");
                String playerAction = scanner.nextLine();

                // Send the player action back to the server
                outToServer.writeObject(playerAction);
            } else if (serverMessage.contains("Enter the index")) {
                System.out.print("Enter the index (index): ");
                String playerAction = scanner.nextLine();

                // Send the player action back to the server
                outToServer.writeObject(playerAction);
            } else if (serverMessage.contains("Final scores")) {
                // close connectiosn
                clientSocket.close();
                System.out.println(serverMessage);
                break;
            }

        }
    }
}
