package test;

import card.PointSaladCard;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PileTest {

    @Test // REQ 4 - Shuffle and create equal draw piles with point card visible
          // REQ 5 - Flip over two cards from each draw pile to form the vegetable market
    public void testShuffleAndCreateDrawPiles() throws IOException {
        // Load the deck from the PointSaladManifest.json file
        List<PointSaladCard> deck = loadDeckFromManifest();

        // Shuffle the deck
        Collections.shuffle(deck);

        // Split the deck into three roughly equal draw piles
        List<PointSaladCard> pile1 = new ArrayList<>();
        List<PointSaladCard> pile2 = new ArrayList<>();
        List<PointSaladCard> pile3 = new ArrayList<>();

        for (int i = 0; i < deck.size(); i++) {
            if (i % 3 == 0) {
                pile1.add(deck.get(i));
            } else if (i % 3 == 1) {
                pile2.add(deck.get(i));
            } else {
                pile3.add(deck.get(i));
            }
        }

        // Verify that the piles are roughly equal in size
        int totalCards = deck.size();
        int expectedPileSize = totalCards / 3;

        Assertions.assertTrue(Math.abs(pile1.size() - expectedPileSize) <= 1,
                "Pile 1 size is not within the expected range.");
        Assertions.assertTrue(Math.abs(pile2.size() - expectedPileSize) <= 1,
                "Pile 2 size is not within the expected range.");
        Assertions.assertTrue(Math.abs(pile3.size() - expectedPileSize) <= 1,
                "Pile 3 size is not within the expected range.");

        // Verify that the point card sides are visible in each pile
        for (PointSaladCard card : pile1) {
            Assertions.assertTrue(card.isCriteriaSideUp(),
                    "Pile 1 contains a card with the point card side not visible.");
        }
        for (PointSaladCard card : pile2) {
            Assertions.assertTrue(card.isCriteriaSideUp(),
                    "Pile 2 contains a card with the point card side not visible.");
        }
        for (PointSaladCard card : pile3) {
            Assertions.assertTrue(card.isCriteriaSideUp(),
                    "Pile 3 contains a card with the point card side not visible.");
        }

        // Flip over two cards from each draw pile to form the vegetable market
        List<PointSaladCard> vegetableMarket = new ArrayList<>();
        flipCardsToMarket(pile1, vegetableMarket);
        flipCardsToMarket(pile2, vegetableMarket);
        flipCardsToMarket(pile3, vegetableMarket);

        // Verify that the vegetable market has 6 cards
        Assertions.assertEquals(6, vegetableMarket.size(), "The vegetable market should contain 6 cards.");

        // Verify that the cards in the vegetable market have the vegetable side visible
        for (PointSaladCard card : vegetableMarket) {
            Assertions.assertFalse(card.isCriteriaSideUp(),
                    "The vegetable market contains a card with the vegetable card side visible.");
        }
    }

    private void flipCardsToMarket(List<PointSaladCard> pile, List<PointSaladCard> vegetableMarket) {
        for (int i = 0; i < 2 && !pile.isEmpty(); i++) {
            PointSaladCard card = pile.remove(pile.size() - 1);
            card.flipCard(); // Assuming there's a method to flip the card
            vegetableMarket.add(card);
        }
    }

    private List<PointSaladCard> loadDeckFromManifest() throws IOException {
        List<PointSaladCard> deck = new ArrayList<>();
        try (FileInputStream file = new FileInputStream("PointSaladManifest.json");
                Scanner scanner = new Scanner(file, "UTF-8").useDelimiter("\\A")) {

            // Read the entire JSON file into a String
            String jsonString = scanner.hasNext() ? scanner.next() : "";

            // Parse the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // Get the "cards" array from the JSONObject
            JSONArray cardsArray = jsonObject.getJSONArray("cards");

            // Iterate over each card in the array
            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardJson = cardsArray.getJSONObject(i);

                // Get the criteria object from the card JSON
                JSONObject criteriaObj = cardJson.getJSONObject("criteria");

                // Add each vegetable card to the deck with its corresponding criteria
                deck.add(new PointSaladCard(PointSaladCard.CardType.POINT, criteriaObj.getString("PEPPER"),
                        PointSaladCard.Vegetable.PEPPER));
                deck.add(new PointSaladCard(PointSaladCard.CardType.POINT, criteriaObj.getString("LETTUCE"),
                        PointSaladCard.Vegetable.LETTUCE));
                deck.add(new PointSaladCard(PointSaladCard.CardType.POINT, criteriaObj.getString("CARROT"),
                        PointSaladCard.Vegetable.CARROT));
                deck.add(new PointSaladCard(PointSaladCard.CardType.POINT, criteriaObj.getString("CABBAGE"),
                        PointSaladCard.Vegetable.CABBAGE));
                deck.add(new PointSaladCard(PointSaladCard.CardType.POINT, criteriaObj.getString("ONION"),
                        PointSaladCard.Vegetable.ONION));
                deck.add(new PointSaladCard(PointSaladCard.CardType.POINT, criteriaObj.getString("TOMATO"),
                        PointSaladCard.Vegetable.TOMATO));
            }
        }
        return deck;
    }
}