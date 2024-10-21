package game;

import player.Player;
import pile.Pile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import card.Card;
// TODO : Error handling for inputs

public class TurnManager {
    private ArrayList<Player> players;
    private List<Pile> piles; // Use piles directly instead of Market
    private int currentPlayer;

    public TurnManager(ArrayList<Player> players, ArrayList<Pile> piles) {
        this.players = players;
        this.piles = piles; // Use piles directly
        this.currentPlayer = 0; // Start with player 0

        Collections.shuffle(this.players);
        System.out.println("Players shuffled. Random start player is Player " + this.players.get(0).getId());
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

    public void redistributeVeggieCards(Pile emptyPile, int veggieIndex) {
        Pile biggestPile = getBiggestPileExcluding(emptyPile);
        // > 0 to handle last card in pile for redistribution
        if (biggestPile != null && biggestPile.cards.size() > 0) {
            // Logging which pile and from where the card is coming
            System.out.println("Before redistribution: Biggest Pile Size = " + biggestPile.cards.size());
            System.out.println("Taking card from the bottom of Pile " + piles.indexOf(biggestPile) + ": "
                    + biggestPile.cards.get(biggestPile.cards.size() - 1));

            // Draw the card from the bottom of the biggest pile for the veggie slot
            emptyPile.veggieCards[veggieIndex] = biggestPile.cards.remove(biggestPile.cards.size() - 1);
            emptyPile.veggieCards[veggieIndex].flipCard(); // Make sure it’s flipped to veggie side

            // Log redistribution action
            System.out.println(
                    "Redistributed a veggie card to Pile " + piles.indexOf(emptyPile) + " at slot " + veggieIndex);
            System.out.println("After redistribution: Biggest Pile Size = " + biggestPile.cards.size());
        } else {
            System.out.println("No more veggie cards available for redistribution.");
        }
    }

    // This method ensures the market is filled before every player's turn
    private void checkAndRefillMarket() {
        for (Pile pile : piles) {
            // Check each veggie slot (index 0 and 1) and refill if empty
            for (int veggieIndex = 0; veggieIndex < pile.veggieCards.length; veggieIndex++) {
                if (pile.getVeggieCard(veggieIndex) == null) {
                    redistributeVeggieCards(pile, veggieIndex);
                }
            }
        }
    }

    public void startTurns() throws IOException, ClassNotFoundException {
        boolean keepPlaying = true;
        // Client player sees the market via sendMessage
        for (Player player : players) {
            player.sendMessage("The market is:\n" + printMarket());
            // player.sendMessage("It's your turn!");
            // player.displayHand());
        }
        while (keepPlaying) {
            Player thisPlayer = players.get(currentPlayer);

            // Check if the market still has available cards
            if (isMarketEmpty()) {
                keepPlaying = false;
                break;
            }
            checkAndRefillMarket();
            // Display the market and hand for both server and client

            // Client player sees the market via sendMessage
            thisPlayer.sendMessage("The market is:\n" + printMarket());
            thisPlayer.sendMessage("It's your turn! Your hand is:\n" +
                    thisPlayer.displayHand());
            // show other players hands
            for (Player player : players) {
                if (player.getId() != thisPlayer.getId()) {
                    thisPlayer.sendMessage(player.displayHand());
                }
            }

            // Inform other players that they are waiting for their turn
            for (Player player : players) {
                if (player != thisPlayer) {
                    player.sendMessage("Waiting for your turn...");
                }
            }
            // Process player action
            if (!thisPlayer.isBot()) {
                handlePlayerTurn(thisPlayer);
            } else {
                handleBotTurn(thisPlayer);
            }

            // Prompt the player to flip a card
            if (!thisPlayer.isBot()) {
                promptFlipCard(thisPlayer);
            }

            System.out.println("Starting player: " + currentPlayer);
            // Move to the next player
            currentPlayer = (currentPlayer + 1) % players.size();
            System.out.println("Starting player: " + currentPlayer);

        }

        calculateAndDisplayScores();

    }

    private void handlePlayerTurn(Player player) throws IOException, ClassNotFoundException {
        System.out.println("Handling turn for Player " + player.getId());
        player.sendMessage("Choose a pile to take a card from (number) or one/two veggie cards (A-F):");

        boolean validInput = false;

        while (!validInput) {
            String action = player.receiveInput();
            System.out.println("Player " + player.getId() + " input received: " + action);

            if (action == null || action.isEmpty()) {
                player.sendMessage("No input received. Please provide a valid action.");
                continue;
            }

            if (action.matches("\\d")) {
                int pileIndex = Integer.parseInt(action);
                if (pileIndex < piles.size() && !piles.get(pileIndex).isEmpty()) {
                    if (piles.get(pileIndex).getPointCard() == null) {
                        player.sendMessage("Pile is empty. Try again.");
                        continue;
                    }
                    player.addCardToHand(piles.get(pileIndex).buyPointCard());
                    player.sendMessage("You took a point card from pile " + pileIndex);
                    validInput = true;

                } else {
                    player.sendMessage("Invalid pile or pile is empty. Try again.");
                }
            } else if (action.matches("[A-Fa-f]{1,2}")) {
                if (action.length() == 1 || action.length() == 2) {
                    takeVeggieCards(player, action);
                    validInput = true;
                } else {
                    player.sendMessage("Invalid selection. You can only choose one or two veggie cards.");
                }
            } else {
                player.sendMessage(
                        "Invalid input. You can only choose a pile number or one/two veggie cards (A-F). Try again.");
            }
        }
    }

    private void promptFlipCard(Player player) throws IOException, ClassNotFoundException {
        List<Card> pointCards = new ArrayList<>();
        for (Card card : player.getHand()) {
            if (card.isCriteriaSideUp()) {
                pointCards.add(card);
            }
        }

        if (pointCards.isEmpty()) {
            player.sendMessage("You have no criteria cards to flip.");
            return;
        }

        player.sendMessage("Do you want to flip a criteria card to its veggie side? (yes/no)");
        String response = player.receiveInput().trim().toLowerCase();

        if (response.equals("yes")) {
            player.sendMessage("Your hand is:\n" + player.displayHand());
            for (int i = 0; i < pointCards.size(); i++) {
                player.sendMessage("[" + i + "] " + pointCards.get(i));
            }

            boolean validIndex = false;
            while (!validIndex) {
                player.sendMessage("Enter the index of the card you want to flip:");
                String cardIndexStr = player.receiveInput().trim();

                try {
                    int cardIndex = Integer.parseInt(cardIndexStr);
                    if (cardIndex >= 0 && cardIndex < pointCards.size()) {
                        pointCards.get(cardIndex).flipCard();
                        player.sendMessage("Card flipped: " + pointCards.get(cardIndex));

                        // Notify all players about the flip
                        for (Player p : players) {
                            if (p.getId() != player.getId()) {
                                p.sendMessage("Player " + player.getId() + " has flipped card " + cardIndex
                                        + " to its veggie side.");
                            }
                        }
                        validIndex = true;
                    } else {
                        player.sendMessage("Invalid card index. Please try again.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid input. Please enter a valid number.");
                }
            }
        } else {
            player.sendMessage("You chose not to flip a card.");
        }
    }

    private boolean takeVeggieCards(Player player, String action) throws IOException {
        int takenVeggies = 0;

        for (int charIndex = 0; charIndex < action.length(); charIndex++) {
            char c = action.charAt(charIndex);
            int pileIndex;

            // Map A and D to pile 0, B and E to pile 1, C and F to pile 2
            switch (Character.toUpperCase(c)) {
                case 'A':
                case 'D':
                    pileIndex = 0;
                    break;
                case 'B':
                case 'E':
                    pileIndex = 1;
                    break;
                case 'C':
                case 'F':
                    pileIndex = 2;
                    break;
                default:
                    player.sendMessage("Invalid pile selected. Please choose a valid pile (A-F).");
                    System.out.println("Invalid pile selected: " + c); // Debugging
                    return false; // Re-prompt the player
            }

            System.out.println("Selected pile index: " + pileIndex); // Debugging

            // Calculate the veggie index (A-C map to row 1, D-F to row 2)
            int veggieIndex = (Character.toUpperCase(c) < 'D') ? 0 : 1; // Row 1 or Row 2
            System.out.println("Selected veggie index: " + veggieIndex); // Debugging

            Pile selectedPile = piles.get(pileIndex); // Correctly map to the pile
            System.out.println("Selected pile: " + selectedPile); // Debugging

            if (selectedPile.getVeggieCard(veggieIndex) != null && takenVeggies < 2) {
                System.out.println("Veggie card available in pile: " + pileIndex + ", veggie index: " + veggieIndex); // Debugging
                player.addCardToHand(selectedPile.buyVeggieCard(veggieIndex));
                takenVeggies++;
            } else { // Pile is empty or no more veggie cards
                player.sendMessage("The selected pile or veggie card is empty. Try again.");
                System.out.println("Pile or veggie card empty: " + pileIndex + ", veggie index: " + veggieIndex); // Debugging
                return false; // Re-prompt the player
            }
        }
        return true; // Return true if all selections were valid
    }

    private void handleBotTurn(Player bot) throws IOException, ClassNotFoundException {
        System.out.println("Handling bot turn for Player " + bot.getId());
        // Bot randomly selects a point or veggie card
        if (Math.random() > 0.5) {
            // Bot takes a point card
            for (int i = 0; i < piles.size(); i++) {
                if (!piles.get(i).isEmpty()) {
                    Card pointCard = piles.get(i).buyPointCard();
                    if (pointCard != null) {
                        bot.addCardToHand(pointCard);
                        break;
                    }
                }
            }
        } else {
            // Bot takes two veggie cards
            takeVeggieCards(bot, "AB");
        }
    }

    private void calculateAndDisplayScores() throws IOException {
        int highestScore = 0;
        Player winner = null;
        StringBuilder scoreMessage = new StringBuilder("Final Scores:\n");

        for (Player player : players) {
            int score = player.calculateScore();
            scoreMessage.append("Player ").append(player.getId()).append("'s score: ").append(score).append("\n");

            if (score > highestScore) {
                highestScore = score;
                winner = player;
            }
        }

        if (winner != null) {
            scoreMessage.append("The winner is Player ").append(winner.getId()).append(" with a score of ")
                    .append(highestScore).append("!\n");
        } else {
            scoreMessage.append("No winner could be determined.\n");
        }

        // Display the result on the server side
        System.out.println(scoreMessage.toString());

        // Broadcast the final result to all players
        for (Player player : players) {
            player.sendMessage(scoreMessage.toString());
        }
        // TODO close connections after game ends

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
