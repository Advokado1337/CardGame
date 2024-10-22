package network;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public abstract class GameClient {
    protected Socket clientSocket;
    protected ObjectOutputStream outToServer;
    protected ObjectInputStream inFromServer;

    public GameClient(String ipAddress, int port) throws IOException {
        this.clientSocket = new Socket(ipAddress, port);
        this.outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.inFromServer = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void start() throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String serverMessage;
            try {
                serverMessage = (String) inFromServer.readObject();
            } catch (SocketException e) {
                break;
            }

            System.out.println("Server: " + serverMessage);
            handleServerMessage(serverMessage, scanner);
        }
    }

    protected abstract void handleServerMessage(String serverMessage, Scanner scanner) throws IOException;

    protected String getValidInput(Scanner scanner, String prompt, String validPattern) {
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