package main;

import network.GameClient;
import network.GameServer;
// import game.Market;
import game.TurnManager;
import pile.Pile;
import player.Player;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import card.CardFactory;
import card.PointSaladCard;
import card.Card;

public class PointSalad {

    public static void main(String[] args) {

        if (args.length > 0 && args[0].equals("server")) {
            // Start the server mode
            try {
                int numberOfPlayers = Integer.parseInt(args[1]); // E.g., 2 players
                int numberOfBots = Integer.parseInt(args[2]); // E.g., 0 bots
                GameServer server = new GameServer(2048, numberOfPlayers, numberOfBots);

                // Set up players and piles for the game
                ArrayList<Player> players = new ArrayList<>(server.getPlayers()); // Fetch players from server
                ArrayList<Pile> piles = setupPiles(numberOfPlayers + numberOfBots); // Create piles for the game

                // Initialize the turn manager and start turns
                TurnManager turnManager = new TurnManager(players, piles);
                turnManager.startTurns();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length > 0 && args[0].equals("client")) {
            // Start the client mode
            try {
                String ipAddress = args[1]; // E.g., "127.0.0.1"
                GameClient client = new GameClient(ipAddress, 2048);

                client.start(); // Start interaction with the server
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Invalid or no arguments
            System.out.println("Invalid arguments. Use 'server [numberPlayers] [numberBots]' or 'client [ipAddress]'.");
        }
    }

    // Setup piles for the game
    private static ArrayList<Pile> setupPiles(int nrPlayers) {
        ArrayList<Card> deckPepper = new ArrayList<>();
        ArrayList<Card> deckLettuce = new ArrayList<>();
        ArrayList<Card> deckCarrot = new ArrayList<>();
        ArrayList<Card> deckCabbage = new ArrayList<>();
        ArrayList<Card> deckOnion = new ArrayList<>();
        ArrayList<Card> deckTomato = new ArrayList<>();

        // Load the cards from the JSON file
        try (InputStream fInputStream = new FileInputStream("PointSaladManifest.json");
                Scanner scanner = new Scanner(fInputStream, "UTF-8").useDelimiter("\\A")) {

            // Read and parse JSON
            String jsonString = scanner.hasNext() ? scanner.next() : "";
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray cardsArray = jsonObject.getJSONArray("cards");

            // Add each vegetable card to the deck with its corresponding criteria
            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardJson = cardsArray.getJSONObject(i);
                JSONObject criteriaObj = cardJson.getJSONObject("criteria");

                deckPepper.add(CardFactory.createPointSaladCard(PointSaladCard.CardType.POINT,
                        criteriaObj.getString("PEPPER"), PointSaladCard.Vegetable.PEPPER));
                deckLettuce.add(CardFactory.createPointSaladCard(PointSaladCard.CardType.POINT,
                        criteriaObj.getString("LETTUCE"), PointSaladCard.Vegetable.LETTUCE));
                deckCarrot.add(CardFactory.createPointSaladCard(PointSaladCard.CardType.POINT,
                        criteriaObj.getString("CARROT"), PointSaladCard.Vegetable.CARROT));
                deckCabbage.add(CardFactory.createPointSaladCard(PointSaladCard.CardType.POINT,
                        criteriaObj.getString("CABBAGE"), PointSaladCard.Vegetable.CABBAGE));
                deckOnion.add(CardFactory.createPointSaladCard(PointSaladCard.CardType.POINT,
                        criteriaObj.getString("ONION"), PointSaladCard.Vegetable.ONION));
                deckTomato.add(CardFactory.createPointSaladCard(PointSaladCard.CardType.POINT,
                        criteriaObj.getString("TOMATO"), PointSaladCard.Vegetable.TOMATO));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Shuffle each deck
        shuffleDeck(deckPepper);
        shuffleDeck(deckLettuce);
        shuffleDeck(deckCarrot);
        shuffleDeck(deckCabbage);
        shuffleDeck(deckOnion);
        shuffleDeck(deckTomato);

        // Calculate how many cards to assign per veggie type based on the number of
        // players
        // Each player gets 6 cards of each veggie type

        int totalPlayers = nrPlayers; // Include the host (server)
        int removeCount = (6 - totalPlayers) * 3; // Number of cards to remove from each veggie deck
        // Remove the specified number of cards from each veggie deck
        for (int i = 0; i < removeCount; i++) {
            if (!deckPepper.isEmpty())
                deckPepper.remove(0);
            if (!deckLettuce.isEmpty())
                deckLettuce.remove(0);
            if (!deckCarrot.isEmpty())
                deckCarrot.remove(0);
            if (!deckCabbage.isEmpty())
                deckCabbage.remove(0);
            if (!deckOnion.isEmpty())
                deckOnion.remove(0);
            if (!deckTomato.isEmpty())
                deckTomato.remove(0);
        }

        // Combine the remaining cards into a main deck
        ArrayList<Card> deck = new ArrayList<>();
        deck.addAll(deckPepper);
        deck.addAll(deckLettuce);
        deck.addAll(deckCarrot);
        deck.addAll(deckCabbage);
        deck.addAll(deckOnion);
        deck.addAll(deckTomato);

        System.out.println("Main deck size before shuffling: " + deck.size());

        // Shuffle the combined deck and divide it into 3 piles
        shuffleDeck(deck);
        System.out.println("Main deck size after shuffling: " + deck.size());

        ArrayList<Card> pile1 = new ArrayList<>();
        ArrayList<Card> pile2 = new ArrayList<>();
        ArrayList<Card> pile3 = new ArrayList<>();
        for (int i = 0; i < deck.size(); i++) {
            if (i % 3 == 0) {
                pile1.add(deck.get(i));
            } else if (i % 3 == 1) {
                pile2.add(deck.get(i));
            } else {
                pile3.add(deck.get(i));
            }
        }
        System.out.println("Pile 1 size: " + pile1.size());
        System.out.println("Pile 2 size: " + pile2.size());
        System.out.println("Pile 3 size: " + pile3.size());

        // Create and return the piles
        ArrayList<Pile> piles = new ArrayList<>();
        piles.add(new Pile(pile1));
        piles.add(new Pile(pile2));
        piles.add(new Pile(pile3));

        return piles;
    }

    // Shuffle deck helper method
    private static void shuffleDeck(List<Card> deck) {
        for (int i = 0; i < deck.size(); i++) {
            int randomIndex = (int) (Math.random() * deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(randomIndex));
            deck.set(randomIndex, temp);
        }
    }
}
