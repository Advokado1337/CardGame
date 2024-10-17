package game;

import player.Player;
import pile.Pile;
import java.io.IOException;
import java.util.ArrayList;

public class TurnManager {
    private ArrayList<Player> players;
    private ArrayList<Pile> piles; // Use piles directly instead of Market
    private int currentPlayer;

    public TurnManager(ArrayList<Player> players, ArrayList<Pile> piles) {
        this.players = players;
        this.piles = piles; // Use piles directly
        this.currentPlayer = 0; // Start with player 0
    }

    public void startTurns() throws IOException, ClassNotFoundException {
        boolean keepPlaying = true;

        while (keepPlaying) {
            Player thisPlayer = players.get(currentPlayer);

            // Check if the market still has available cards
            if (isMarketEmpty()) {
                System.out.println("The market is empty. The game is over!");
                keepPlaying = false;
                break;
            }

            // Display the player's hand and the current market
            thisPlayer.sendMessage("It's your turn! Your hand is:\n" + thisPlayer.displayHand());
            thisPlayer.sendMessage("The market is:\n" + printMarket());

            // Process player action
            if (!thisPlayer.isBot()) {
                handlePlayerTurn(thisPlayer);
            } else {
                handleBotTurn(thisPlayer);
            }

            // Move to the next player
            currentPlayer = (currentPlayer + 1) % players.size();
        }

        // Once the game ends, calculate and display final scores
        calculateAndDisplayScores();
    }

    private void handlePlayerTurn(Player player) throws IOException, ClassNotFoundException {
        player.sendMessage("Choose a pile to take a card from (number) or two veggie cards (A-F):");
        String action = player.receiveInput();

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
    private String printMarket() {
        StringBuilder marketString = new StringBuilder();
        String formatCard = "%-20s"; // Adjusting the width for cleaner output

        // Point cards
        marketString.append("Point Cards:\n");
        for (int i = 0; i < piles.size(); i++) {
            if (piles.get(i).getPointCard() == null) {
                marketString.append(String.format("[%d] ", i))
                        .append(String.format(formatCard, "Empty"))
                        .append("\t");
            } else {
                marketString.append(String.format("[%d] ", i))
                        .append(String.format(formatCard, piles.get(i).getPointCard()))
                        .append("\t");
            }
        }

        // Veggie cards - First Row (A-C)
        marketString.append("\nVeggie Cards:\n");
        char veggieCardIndex = 'A';
        for (int i = 0; i < piles.size(); i++) {
            marketString.append(String.format("[%c] ", veggieCardIndex))
                    .append(String.format(formatCard,
                            piles.get(i).getVeggieCard(0) != null ? piles.get(i).getVeggieCard(0).toString() : "Empty"))
                    .append("\t");
            veggieCardIndex++;
        }

        // Veggie cards - Second Row (D-F)
        marketString.append("\n");
        for (int i = 0; i < piles.size(); i++) {
            marketString.append(String.format("[%c] ", veggieCardIndex))
                    .append(String.format(formatCard,
                            piles.get(i).getVeggieCard(1) != null ? piles.get(i).getVeggieCard(1).toString() : "Empty"))
                    .append("\t");
            veggieCardIndex++;
        }

        return marketString.toString();
    }

}
