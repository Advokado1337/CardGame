package pile;

import card.Card;
import card.CardFactory;
import card.PointSaladCard;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Pile {
    public List<Card> cards = new ArrayList<>();
    public Card[] veggieCards = new Card[2]; // Two visible veggie cards

    // Constructor only focuses on its own cards
    public Pile(List<Card> cards) {
        this.cards = cards;
        if (cards.size() >= 2) {
            this.veggieCards[0] = cards.remove(0);
            this.veggieCards[1] = cards.remove(0);
            this.veggieCards[0].flipCard();
            this.veggieCards[1].flipCard();
        } else {
            System.out.println("Not enough cards to assign to veggie cards");
        }
    }

    // Get point card (first card in the pile)
    public Card getPointCard() {
        return cards.isEmpty() ? null : cards.get(0);
    }

    // Buy point card
    public Card buyPointCard() {
        return cards.isEmpty() ? null : cards.remove(0);
    }

    // Get veggie card at a specified index
    public Card getVeggieCard(int index) {
        return veggieCards[index];
    }

    // Buy veggie card and replace it
    public Card buyVeggieCard(int index) {
        Card boughtCard = veggieCards[index];
        if (!cards.isEmpty()) {
            veggieCards[index] = cards.remove(0);
            veggieCards[index].flipCard();
        } else {
            veggieCards[index] = null; // No more veggie cards available
        }
        return boughtCard;
    }

    // Check if the pile is empty (both cards and veggie cards)
    public boolean isEmpty() {
        return cards.isEmpty() && veggieCards[0] == null && veggieCards[1] == null;
    }

    // Static method to handle deck creation based on the number of players
    public static List<Pile> createPiles(int nrPlayers) {
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

        // Return the piles
        List<Pile> piles = new ArrayList<>();
        piles.add(new Pile(pile1));
        piles.add(new Pile(pile2));
        piles.add(new Pile(pile3));
        return piles;
    }

    // Shuffle deck helper method
    private static void shuffleDeck(ArrayList<Card> deck) {
        for (int i = 0; i < deck.size(); i++) {
            int randomIndex = (int) (Math.random() * deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(randomIndex));
            deck.set(randomIndex, temp);
        }
    }
}
