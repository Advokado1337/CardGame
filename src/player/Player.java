package player;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import card.Card;

public class Player {
    private int id;
    private boolean isBot;
    private Socket connectionSocket;
    private ObjectInputStream inFromClient;
    private ObjectOutputStream outToClient;

    private ArrayList<Card> hand;

    public Player(int id, boolean isBot, Socket connectionSocket, ObjectInputStream inFromClient,
            ObjectOutputStream outToClient) {
        this.id = id;
        this.isBot = isBot;
        this.connectionSocket = connectionSocket;
        this.inFromClient = inFromClient;
        this.outToClient = outToClient;
        this.hand = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public boolean isBot() {
        return isBot;
    }

    public void addCardToHand(Card card) {
        hand.add(card);
    }

    public void sendMessage(String message) throws IOException {
        if (!isBot && outToClient != null) {
            outToClient.writeObject(message); // Send a message to the client
        }
    }

    public String receiveInput() throws IOException, ClassNotFoundException {
        if (isBot()) {
        } else if (connectionSocket == null && id == 0) {
            System.out.println("Server, it's your turn. Type your action:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } else if (inFromClient != null) {
            return (String) inFromClient.readObject();
        }
        return null;
    }

    public String displayHand() {
        StringBuilder handDisplay = new StringBuilder();
        handDisplay.append("Player " + id + "'s hand:\n");
        for (Card card : hand) {
            if (card == null) {
                handDisplay.append("null\n");
                continue;
            }
            handDisplay.append(card.toString()).append("\n");
        }
        return handDisplay.toString();
    }

}
