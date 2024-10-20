package main;

import network.GameClient;
import network.GameServer;
// import game.Market;
import game.TurnManager;
import pile.Pile;
import player.Player;

import java.util.ArrayList;

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
    private static ArrayList<Pile> setupPiles(int numberOfPlayers) {
        ArrayList<Pile> piles = new ArrayList<>(Pile.createPiles(numberOfPlayers)); // Call the new method
        System.out.println("Piles setup:");
        for (int i = 0; i < piles.size(); i++) {
            System.out.println("Pile " + i + ": " + piles.get(i).toString());
        }
        return piles;
    }
}
