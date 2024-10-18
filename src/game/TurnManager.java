package game;

import player.Player;
import pile.Pile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private ArrayList<Player> players;
    private List<Pile> piles; // Use piles directly instead of Market
    private int currentPlayer;

    public TurnManager(ArrayList<Player> players, ArrayList<Pile> piles) {
        this.players = players;
        this.piles = piles; // Use piles directly
        this.currentPlayer = 0; // Start with player 0
    }

    // Find the biggest pile (move this logic from Pile)
    public Pile getBiggestPileExcluding(Pile excludePile) {
        int biggestSize = 0;
        Pile biggestPile = null;

        for (Pile pile : piles) {
            if (pile != excludePile && pile.cards.size() > biggestSize) {
                biggestSize = pile.cards.size();
                biggestPile = pile;
            }
        }
        return biggestPile;
    }

    // Handle the redistribution of cards (used in buyPointCard in Pile previously)
    public void redistributeCard(Pile emptyPile) {
        Pile biggestPile = getBiggestPileExcluding(emptyPile);

        if (biggestPile != null && biggestPile.cards.size() > 1) {
            emptyPile.cards.add(biggestPile.cards.remove(biggestPile.cards.size() - 1));
        } else {
            System.out.println("No more cards available for redistribution.");
        }
    }

    public void startTurns() throws IOException, ClassNotFoundException {
        boolean keepPlaying = true;
        // Client player sees the market via sendMessage
        for (Player player : players) {
            player.sendMessage("The market is:\n" + printMarket());
            player.sendMessage("It's your turn! Your hand is:\n" +
                    player.displayHand());
        }
        while (keepPlaying) {
            Player thisPlayer = players.get(currentPlayer);

            // Check if the market still has available cards
            if (isMarketEmpty()) {
                System.out.println("The market is empty. The game is over!");
                keepPlaying = false;
                break;
            }

            // Display the market and hand for both server and client
            if (thisPlayer.getId() == 0) {
                // Player 0 (server) sees the market directly
                System.out.println("The market is:\n" + printMarket());
                System.out.println("It's your turn! Your hand is:\n" +
                        thisPlayer.displayHand());
            } else {
                // Client player sees the market via sendMessage
                thisPlayer.sendMessage("The market is:\n" + printMarket());
                thisPlayer.sendMessage("It's your turn! Your hand is:\n" +
                        thisPlayer.displayHand());
            }

            // Process player action
            if (!thisPlayer.isBot()) {
                handlePlayerTurn(thisPlayer);
            } else {
                handleBotTurn(thisPlayer);
            }
            System.out.println("Starting player: " + currentPlayer);
            // Move to the next player
            currentPlayer = (currentPlayer + 1) % players.size();
            System.out.println("Starting player: " + currentPlayer);

        }

        // Once the game ends, calculate and display final scores
        calculateAndDisplayScores();
    }

    private void handlePlayerTurn(Player player) throws IOException, ClassNotFoundException {
        System.out.println("Handling turn for Player " + player.getId());
        player.sendMessage("Choose a pile to take a card from (number) or two veggie cards (A-F):");
        System.out.println("Waiting for input from Player " + player.getId());

        String action = player.receiveInput();
        System.out.println("Player " + player.getId() + " input received: " + action);

        // Check if the action is valid
        if (action == null || action.isEmpty()) {
            player.sendMessage("No input received. Please provide a valid action.");
            return;
        }

        System.out.println("Player " + player.getId() + " action: " + action);

        // Handling point card action (numeric input)
        if (action.matches("\\d")) {
            int pileIndex = Integer.parseInt(action);
            if (pileIndex < piles.size() && !piles.get(pileIndex).isEmpty()) {
                player.addCardToHand(piles.get(pileIndex).buyPointCard());
                player.sendMessage("You took a point card from pile " + pileIndex);
            } else {
                player.sendMessage("Pile is empty or invalid pile selected.");
            }
        } else {
            // Handling veggie card action (A-F input)
            takeVeggieCards(player, action);
        }
    }

    private void takeVeggieCards(Player player, String action) throws IOException {
        int takenVeggies = 0;

        for (char c : action.toCharArray()) {
            int pileIndex = Character.toUpperCase(c) - 'A'; // Convert A-F to pile index

            if (pileIndex < 0 || pileIndex >= piles.size()) {
                player.sendMessage("Invalid pile selected. Try again.");
                continue;
            }

            if (piles.get(pileIndex).getVeggieCard(0) != null && takenVeggies < 2) {
                player.addCardToHand(piles.get(pileIndex).buyVeggieCard(0));
                takenVeggies++;
            } else {
                player.sendMessage("Pile or veggie card is empty.");
            }

            if (takenVeggies == 2) {
                break; // Player can take a maximum of 2 veggie cards
            }
        }
    }

    private void handleBotTurn(Player bot) throws IOException {
        System.out.println("Handling bot turn for Player " + bot.getId());
        // Bot randomly selects a point or veggie card
        if (Math.random() > 0.5) {
            // Bot takes a point card
            for (int i = 0; i < piles.size(); i++) {
                if (!piles.get(i).isEmpty()) {
                    bot.addCardToHand(piles.get(i).buyPointCard());
                    break;
                }
            }
        } else {
            // Bot takes two veggie cards
            takeVeggieCards(bot, "AB");
        }
    }

    private void calculateAndDisplayScores() {
        for (Player player : players) {
            int score = player.calculateScore();
            System.out.println("Player " + player.getId() + "'s score: " + score);
        }
    }

    private boolean isMarketEmpty() {
        return piles.stream().allMatch(Pile::isEmpty);
    }

    // Old Market

    public String printMarket() {
        StringBuilder pileString = new StringBuilder();

        // Format for Point Cards
        pileString.append("Point Cards:\t");
        for (int p = 0; p < piles.size(); p++) {
            if (piles.get(p).getPointCard() == null) {
                pileString.append(String.format("[%d]%-43s\t", p, "Empty"));
            } else {
                pileString.append(String.format("[%d]%-43s\t", p, piles.get(p).getPointCard().toString()));
            }
        }

        pileString.append("\nVeggie Cards:\t");
        char veggieCardIndex = 'A';
        for (Pile pile : piles) {
            pileString.append(String.format("[%c]%-43s\t", veggieCardIndex,
                    pile.getVeggieCard(0) != null ? pile.getVeggieCard(0).toString() : "Empty"));
            veggieCardIndex++;
        }

        pileString.append("\n\t\t");
        for (Pile pile : piles) {
            pileString.append(String.format("[%c]%-43s\t", veggieCardIndex,
                    pile.getVeggieCard(1) != null ? pile.getVeggieCard(1).toString() : "Empty"));
            veggieCardIndex++;
        }

        return pileString.toString();
    }

}
