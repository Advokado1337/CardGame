package player;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList; // Import to handle card collections

import card.Card; // Assuming Card class exists in game package

public class Player {
    private int id;
    private boolean isBot; // True if this is a bot, false if it's a human player (client)
    private Socket connectionSocket; // For online players
    private ObjectInputStream inFromClient;
    private ObjectOutputStream outToClient;

    // New attribute to store player's hand (cards)
    private ArrayList<Card> hand; // This will hold the player's cards

    // Constructor for bot players (no need for sockets)
    public Player(int id, boolean isBot, Socket connectionSocket, ObjectInputStream inFromClient,
            ObjectOutputStream outToClient) {
        this.id = id;
        this.isBot = isBot;
        this.connectionSocket = connectionSocket;
        this.inFromClient = inFromClient;
        this.outToClient = outToClient;
        this.hand = new ArrayList<>(); // Initialize the hand as an empty list
    }

    // Getter for player ID
    public int getId() {
        return id;
    }

    // Getter for the player's hand (this may be used in other parts of the game)
    public ArrayList<Card> getHand() {
        return hand;
    }

    // Check if the player is a bot
    public boolean isBot() {
        return isBot;
    }

    // Add a card to the player's hand
    public void addCardToHand(Card card) {
        hand.add(card); // Adds the given card to the player's hand
    }

    // Send a message to the player (for non-bots)
    public void sendMessage(String message) throws IOException {
        if (!isBot && outToClient != null) {
            outToClient.writeObject(message); // Send a message to the client
        }
    }

    // Receive input from the player (for non-bots)
    // In Player.java
    public String receiveInput() throws IOException, ClassNotFoundException {
        if (isBot()) {
            return performBotAction();
        } else if (connectionSocket == null && id == 0) {
            // For the server player (Player 0), use console input or other method
            System.out.println("Server, it's your turn. Type your action:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } else if (inFromClient != null) {
            // Receive input from the client
            return (String) inFromClient.readObject();
        }
        return null; // In case the client is not connected
    }

    // Simulate bot action (if player is a bot)
    public String performBotAction() {
        if (isBot) {
            return "Bot player " + id + " takes a random action."; // Example bot action
        }
        return null;
    }

    // Display the player's hand (could be used in the game UI or turn logic)
    public String displayHand() {
        StringBuilder handDisplay = new StringBuilder();
        handDisplay.append("Player " + id + "'s hand:\n");
        for (Card card : hand) {
            // handle null
            if (card == null) {
                handDisplay.append("null\n");
                continue;
            }
            handDisplay.append(card.toString()).append("\n"); // Assuming the Card class has a toString method
        }
        return handDisplay.toString();
    }

    // // Calculate and return the player's score
    // public int calculateScore() {
    // PointSaladScoring scoring = new PointSaladScoring();
    // return scoring.calculateScore(this.hand); // Pass the player's hand to the
    // scoring logic
    // }

}
