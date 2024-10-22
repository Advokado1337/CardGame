package test;

import card.PointSaladCard;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DeckTest {

    @Test // REQ ID 2 - The Deck Consists of 108 cards
    public void testDeckComposition() throws IOException {
        // Load the deck from the PointSaladManifest.json file
        List<PointSaladCard> deck = loadDeckFromManifest();

        // Verify that the deck contains exactly 108 cards
        Assertions.assertEquals(108, deck.size(), "The deck should contain exactly 108 cards.");

        // Verify that there are exactly 18 cards for each of the six vegetables
        int[] vegetableCounts = new int[PointSaladCard.Vegetable.values().length];
        for (PointSaladCard card : deck) {
            vegetableCounts[card.getVegetable().ordinal()]++;
        }

        for (int count : vegetableCounts) {
            Assertions.assertEquals(18, count, "There should be exactly 18 cards for each vegetable.");
        }
    }

    @Test // REQ ID 3 - The Deck is Formed Based on the Number of Players
    public void testDeckFormationForPlayers() throws IOException {
        // Load the deck from the PointSaladManifest.json file
        List<PointSaladCard> deck = loadDeckFromManifest();

        // Test for different player counts
        verifyDeckForPlayerCount(deck, 2, 6, 36);
        verifyDeckForPlayerCount(deck, 3, 9, 54);
        verifyDeckForPlayerCount(deck, 4, 12, 72);
        verifyDeckForPlayerCount(deck, 5, 15, 90);
        verifyDeckForPlayerCount(deck, 6, 18, 108);
    }

    private List<PointSaladCard> loadDeckFromManifest() throws IOException {
        List<PointSaladCard> deck = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream("PointSaladManifest.json");
                Scanner scanner = new Scanner(fis, "UTF-8").useDelimiter("\\A")) {

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

    // REQ ID 3 - The Deck is Formed Based on the Number of Players
    private void verifyDeckForPlayerCount(List<PointSaladCard> deck, int playerCount, int expectedVegetableCount,
            int expectedTotalCount) {
        List<PointSaladCard> formedDeck = new ArrayList<>();
        int[] vegetableCounts = new int[PointSaladCard.Vegetable.values().length];

        for (PointSaladCard card : deck) {
            if (vegetableCounts[card.getVegetable().ordinal()] < expectedVegetableCount) {
                formedDeck.add(card);
                vegetableCounts[card.getVegetable().ordinal()]++;
            }
        }

        // Verify the total number of cards
        Assertions.assertEquals(expectedTotalCount, formedDeck.size(),
                "The deck should contain " + expectedTotalCount + " cards for " + playerCount + " players.");

        // Verify the number of each vegetable
        for (int count : vegetableCounts) {
            Assertions.assertEquals(expectedVegetableCount, count, "There should be " + expectedVegetableCount
                    + " cards for each vegetable for " + playerCount + " players.");
        }
    }
}