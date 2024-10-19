package network;

import player.Player;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private ServerSocket serverSocket;
    private List<Player> players = new ArrayList<>();

    public GameServer(int port, int numberPlayers, int numberOfBots) throws IOException {
        System.out.println("Starting server on port " + port);
        serverSocket = new ServerSocket(port);
        setupPlayers(numberPlayers, numberOfBots);
    }

    private void setupPlayers(int numberPlayers, int numberOfBots) throws IOException {
        System.out.println("Setting up " + numberPlayers + " players and " + numberOfBots + " bots.");
        if (numberPlayers + numberOfBots < 2) {
            System.out.println("Not enough players to start the game.");
            throw new IllegalArgumentException("Not enough players to start the game.");
        }
        if (numberPlayers + numberOfBots > 6) {
            System.out.println("Too many players. Maximum is 6.");
            throw new IllegalArgumentException("Too many players. Maximum is 6.");
        }

        // Add server as the first player (Player 0)
        players.add(new Player(0, false, null, null, null)); // The server is Player 0
        System.out.println("Server added as Player 0.");

        // Add bots
        for (int i = 1; i <= numberOfBots; i++) {
            System.out.println("Adding bot player " + i);
            players.add(new Player(i, true, null, null, null)); // Bot player
        }

        // Accept clients (remaining players)
        for (int i = numberOfBots + 1; i < numberPlayers + numberOfBots; i++) {
            System.out.println("Waiting for client " + i);
            Socket connectionSocket = serverSocket.accept();
            System.out.println("Client " + i + " connected.");
            ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
            // TODO Player is handled in the game server class
            Player player = new Player(i, false, connectionSocket, inFromClient, outToClient);
            players.add(player);
            System.out.println("Connected to player " + i);
            outToClient.writeObject("Connected as player " + i);
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void startGame() throws IOException, ClassNotFoundException {
        System.out.println("Game started!");

        // The server (Player 0) will play first, followed by other players
        for (Player player : players) {
            if (!player.isBot()) {
                System.out.println("Sending turn message to player " + player.getId());
                player.sendMessage("It's your turn! Type your action: ");
                String response = player.receiveInput();
                System.out.println("Player " + player.getId() + " says: " + response);
            } else {
                System.out.println("Player " + player.getId() + " is a bot.");
                String botAction = player.performBotAction();
                System.out.println(botAction);
            }
        }
    }
}
