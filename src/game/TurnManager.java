package game;

import player.Player;
import pile.Pile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import card.Card;
import game.scoring.PointSaladScoring;
import game.scoring.IScoring;

public class TurnManager {
    private ArrayList<Player> players;
    private List<Pile> piles;
    private int currentPlayer;

    public TurnManager(ArrayList<Player> players, ArrayList<Pile> piles) {
        this.players = players;
        this.piles = piles;
        this.currentPlayer = 0;

        Collections.shuffle(this.players);
        System.out.println("Players shuffled. Random start player is Player " + this.players.get(0).getId());
    }

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
        if (biggestPile != null && biggestPile.cards.size() > 0) {

            emptyPile.activeCards[veggieIndex] = biggestPile.cards.remove(biggestPile.cards.size() - 1);
            emptyPile.activeCards[veggieIndex].flipCard();

        }
    }

    // This method ensures the market is filled before every player's turn
    private void checkAndRefillMarket() {
        for (Pile pile : piles) {
            for (int veggieIndex = 0; veggieIndex < pile.activeCards.length; veggieIndex++) {
                if (pile.getActiveCard(veggieIndex) == null) {
                    redistributeVeggieCards(pile, veggieIndex);
                }
            }
        }
    }

    public void startTurns() throws IOException, ClassNotFoundException {
        boolean keepPlaying = true;
        for (Player player : players) {
            player.sendMessage("The market is:\n" + printMarket());

        }
        while (keepPlaying) {
            Player thisPlayer = players.get(currentPlayer);

            // Check if the market still has available cards
            if (isMarketEmpty()) {
                keepPlaying = false;
                break;
            }
            checkAndRefillMarket();

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
            if (!thisPlayer.isBot()) {
                handlePlayerTurn(thisPlayer);
            } else {
                handleBotTurn(thisPlayer);
            }

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

            // Handle pile selection or veggie card selection
            if (action.matches("\\d")) {
                int pileIndex = Integer.parseInt(action);

                // Check if the pile is valid and not empty
                if (pileIndex < piles.size()) {
                    if (piles.get(pileIndex).isEmpty()) {
                        player.sendMessage("The selected pile is empty. Please choose a different pile.");
                        continue; // Reprompt for input
                    } else if (piles.get(pileIndex).getSpecialCard() == null) {
                        player.sendMessage("No point card available in this pile. Try again.");
                        continue; // Reprompt for input
                    } else {
                        player.addCardToHand(piles.get(pileIndex).buySpecialCard());
                        player.sendMessage("You took a point card from pile " + pileIndex);
                        validInput = true;
                    }
                } else {
                    player.sendMessage("Invalid pile number. Please try again.");
                }
            } else if (action.matches("[A-Fa-f]{1,2}")) {
                validInput = takeVeggieCards(player, action);
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
            int pileIndex = getPileIndex(c);

            if (pileIndex == -1) {
                player.sendMessage("Invalid pile selected. Please choose a valid pile (A-F).");
                System.out.println("Invalid pile selected: " + c);
                return false;
            } else {

                int veggieIndex = (Character.toUpperCase(c) < 'D') ? 0 : 1;
                Pile selectedPile = piles.get(pileIndex);

                if (selectedPile.getActiveCard(veggieIndex) == null || takenVeggies > 2) {
                    player.sendMessage("The selected pile or veggie card is empty. Try again.");
                    System.out.println("Pile or veggie card empty: " + pileIndex + ", veggie index: " + veggieIndex);
                    return false;
                }
            }

        }

        for (int charIndex = 0; charIndex < action.length(); charIndex++) {
            char c = action.charAt(charIndex);
            int pileIndex = getPileIndex(c);

            // Calculate the veggie index (A-C map to row 1, D-F to row 2)
            int veggieIndex = (Character.toUpperCase(c) < 'D') ? 0 : 1;

            Pile selectedPile = piles.get(pileIndex); // Correctly map to the pile

            System.out.println("Veggie card available in pile: " + pileIndex + ", veggie index: " + veggieIndex);
            player.addCardToHand(selectedPile.buyActiveCard(veggieIndex));
            takenVeggies++;

        }
        return true;
    }

    private int getPileIndex(char c) {
        switch (Character.toUpperCase(c)) {
            case 'A':
            case 'D':
                return 0;
            case 'B':
            case 'E':
                return 1;
            case 'C':
            case 'F':
                return 2;
            default:
                return -1;
        }
    }

    private void handleBotTurn(Player bot) throws IOException, ClassNotFoundException {
        System.out.println("Handling bot turn for Player " + bot.getId());
        if (Math.random() > 0.5) {
            for (int i = 0; i < piles.size(); i++) {
                if (!piles.get(i).isEmpty()) {
                    Card pointCard = piles.get(i).buySpecialCard();
                    if (pointCard != null) {
                        bot.addCardToHand(pointCard);
                        break;
                    }
                }
            }
        } else {
            takeVeggieCards(bot, "AB");
        }
    }

    // Method to handle scoring at the end of the game
    public void calculateAndDisplayScores() throws IOException {
        IScoring scoring = new PointSaladScoring();

        int highestScore = 0;
        Player winner = null;
        StringBuilder scoreMessage = new StringBuilder("Final Scores:\n");

        for (Player player : players) {
            int score = scoring.calculateScore(player.getHand(), players, player);
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

        System.out.println(scoreMessage.toString());

        for (Player player : players) {
            player.sendMessage(scoreMessage.toString());
        }
    }

    private boolean isMarketEmpty() {
        return piles.stream().allMatch(Pile::isEmpty);
    }

    public String printMarket() {
        StringBuilder pileString = new StringBuilder();

        pileString.append("Point Cards:\t");
        for (int p = 0; p < piles.size(); p++) {
            if (piles.get(p).getSpecialCard() == null) {
                pileString.append(String.format("[%d]%-43s\t", p, "Empty"));
            } else {
                pileString.append(String.format("[%d]%-43s\t", p, piles.get(p).getSpecialCard().toString()));
            }
        }

        pileString.append("\nVeggie Cards:\t");
        char veggieCardIndex = 'A';
        for (Pile pile : piles) {
            pileString.append(String.format("[%c]%-43s\t", veggieCardIndex,
                    pile.getActiveCard(0) != null ? pile.getActiveCard(0).toString() : "Empty"));
            veggieCardIndex++;
        }

        pileString.append("\n\t\t");
        for (Pile pile : piles) {
            pileString.append(String.format("[%c]%-43s\t", veggieCardIndex,
                    pile.getActiveCard(1) != null ? pile.getActiveCard(1).toString() : "Empty"));
            veggieCardIndex++;
        }

        return pileString.toString();
    }

}
