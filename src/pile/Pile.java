package pile;

import card.Card;
import card.CardFactory;
import card.PointSaladCard;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pile {
    public List<Card> cards = new ArrayList<>();
    public Card[] veggieCards = new Card[2]; // Two visible veggie cards
    private List<Pile> piles; // Reference to all piles //TODO this is a bad idea

    // Constructor using CardFactory
    public Pile(List<Card> cards, List<Pile> allPiles) {
        this.cards = cards;
        this.piles = allPiles;

        // Check if there are enough cards to assign to veggieCards
        if (cards.size() >= 2) {
            this.veggieCards[0] = cards.remove(0);
            this.veggieCards[1] = cards.remove(0);
            this.veggieCards[0].flipCard(); // Flip to veggie side
            this.veggieCards[1].flipCard(); // Flip to veggie side
        } else {
            System.out.println("Not enough cards to assign to veggie cards");
        }
    }

    // Get point card (first card in the pile) or pull from other piles if empty
    public Card getPointCard() {
        if (cards.isEmpty()) {
            // remove from the bottom of the biggest of the other piles
            int biggestPileIndex = 0;
            int biggestSize = 0;
            for (int i = 0; i < piles.size(); i++) {
                if (i != piles.indexOf(this) && piles.get(i).cards.size() > biggestSize) {
                    biggestSize = piles.get(i).cards.size();
                    biggestPileIndex = i;
                }
            }
            if (biggestSize > 1) {
                cards.add(piles.get(biggestPileIndex).cards.remove(piles.get(biggestPileIndex).cards.size() - 1));
            } else {
                return null; // No more cards available
            }
        }
        return cards.get(0); // Return the first card as the point card
    }

    // Buy point card (removes first card) or pull from other piles if empty
    public Card buyPointCard() {
        if (cards.isEmpty()) {
            // remove from the bottom of the biggest of the other piles
            int biggestPileIndex = 0;
            int biggestSize = 0;
            for (int i = 0; i < piles.size(); i++) {
                if (i != piles.indexOf(this) && piles.get(i).cards.size() > biggestSize) {
                    biggestSize = piles.get(i).cards.size();
                    biggestPileIndex = i;
                }
            }
            if (biggestSize > 1) {
                cards.add(piles.get(biggestPileIndex).cards.remove(piles.get(biggestPileIndex).cards.size() - 1));
            } else {
                return null; // No more cards available
            }
        }
        return cards.remove(0); // Remove and return the first card (point card)
    }

    // Get veggie card at specified index
    public Card getVeggieCard(int index) {
        return veggieCards[index];
    }

    // Buy veggie card (remove it and replace with a new one from the pile)
    public Card buyVeggieCard(int index) {
        Card boughtCard = veggieCards[index];
        if (cards.size() <= 1) {
            // Remove from the bottom of the biggest of the other piles
            int biggestPileIndex = 0;
            int biggestSize = 0;
            for (int i = 0; i < piles.size(); i++) {
                if (i != piles.indexOf(this) && piles.get(i).cards.size() > biggestSize) {
                    biggestSize = piles.get(i).cards.size();
                    biggestPileIndex = i;
                }
            }
            if (biggestSize > 1) {
                cards.add(piles.get(biggestPileIndex).cards.remove(piles.get(biggestPileIndex).cards.size() - 1));
                veggieCards[index] = cards.remove(0);
                veggieCards[index].flipCard(); // Ensure the new veggie card is flipped
            } else {
                veggieCards[index] = null; // No more veggie cards available
            }
        } else {
            veggieCards[index] = cards.remove(0);
            veggieCards[index].flipCard(); // Ensure the new veggie card is flipped
        }

        return boughtCard;
    }

    // Check if the pile is empty (both cards and veggie cards)
    public boolean isEmpty() {
        return cards.isEmpty() && veggieCards[0] == null && veggieCards[1] == null;
    }

    // Method to set the piles using JSON file
    public void setPiles(int nrPlayers) {
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

            System.out.println("Pepper deck size: " + deckPepper.size());
            System.out.println("Lettuce deck size: " + deckLettuce.size());
            System.out.println("Carrot deck size: " + deckCarrot.size());
            System.out.println("Cabbage deck size: " + deckCabbage.size());
            System.out.println("Onion deck size: " + deckOnion.size());
            System.out.println("Tomato deck size: " + deckTomato.size());

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
        int cardsPerVeggie = nrPlayers / 2 * 6;

        ArrayList<Card> deck = new ArrayList<>();
        for (int i = 0; i < cardsPerVeggie; i++) {
            deck.add(deckPepper.remove(0));
            deck.add(deckLettuce.remove(0));
            deck.add(deckCarrot.remove(0));
            deck.add(deckCabbage.remove(0));
            deck.add(deckOnion.remove(0));
            deck.add(deckTomato.remove(0));
        }

        System.out.println("Main deck size after shuffling: " + deck.size());

        // Shuffle the combined deck and divide it into 3 piles
        shuffleDeck(deck);
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

        // Create the piles and add them to the list
        piles.add(new Pile(pile1, piles));
        piles.add(new Pile(pile2, piles));
        piles.add(new Pile(pile3, piles));
    }

    // Shuffle deck helper method
    private void shuffleDeck(ArrayList<Card> deck) {
        for (int i = 0; i < deck.size(); i++) {
            int randomIndex = (int) (Math.random() * deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(randomIndex));
            deck.set(randomIndex, temp);
        }
    }
}
