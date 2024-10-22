package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import card.PointSaladCard;
import game.scoring.*;
import player.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoringTest {

    private List<Player> players;
    private Player currentPlayer;
    private Scoring scoring;

    @BeforeEach // REQ ID 13-14 Scoring System
    public void setup() {
        // Setup players and scoring system before each test
        players = new ArrayList<>();

        // Current player (Player 0)
        currentPlayer = new Player(0, false, null, null, null);
        players.add(currentPlayer);

        // Add an opponent (Player 1)
        Player opponent = new Player(1, false, null, null, null);
        players.add(opponent);

        // Create a scoring instance
        scoring = new PointSaladScoring();
    }

    @Test
    public void testScoringMostVegetables() {
        // Create a card with MOST CARROT criteria
        PointSaladCard cardWithMostCarrotCriteria = new PointSaladCard(PointSaladCard.CardType.POINT,
                "MOST CARROT = 5", null);

        // Add the card to the current player's hand
        currentPlayer.addCardToHand(cardWithMostCarrotCriteria);

        // Add CARROT vegetables to current player's hand
        PointSaladCard carrotCard1 = new PointSaladCard(PointSaladCard.CardType.VEGETABLE, "",
                PointSaladCard.Vegetable.CARROT);
        PointSaladCard carrotCard2 = new PointSaladCard(PointSaladCard.CardType.VEGETABLE, "",
                PointSaladCard.Vegetable.CARROT);

        currentPlayer.addCardToHand(carrotCard1);
        currentPlayer.addCardToHand(carrotCard2);

        // Add fewer CARROTS to opponent's hand
        PointSaladCard opponentCarrotCard = new PointSaladCard(PointSaladCard.CardType.VEGETABLE, "",
                PointSaladCard.Vegetable.CARROT);
        players.get(1).addCardToHand(opponentCarrotCard);

        // Calculate the score for the current player
        int score = scoring.calculateScore(currentPlayer.getHand(), players, currentPlayer);

        // Expect score to be 5 because current player has the most CARROTS
        assertEquals(5, score);
    }

    @Test
    public void testScoringTotalVegetables() {
        // Create a card with TOTAL criteria (TOTAL VEGGIES = 3 points)
        PointSaladCard totalVegetablesCriteria = new PointSaladCard(PointSaladCard.CardType.POINT, "TOTAL VEGGIES = 3",
                null);

        // Add the card to the current player's hand
        currentPlayer.addCardToHand(totalVegetablesCriteria);

        // Add various vegetables to current player's hand
        PointSaladCard carrotCard = new PointSaladCard(PointSaladCard.CardType.VEGETABLE, "",
                PointSaladCard.Vegetable.CARROT);
        PointSaladCard lettuceCard = new PointSaladCard(PointSaladCard.CardType.VEGETABLE, "",
                PointSaladCard.Vegetable.LETTUCE);
        PointSaladCard tomatoCard = new PointSaladCard(PointSaladCard.CardType.VEGETABLE, "",
                PointSaladCard.Vegetable.TOMATO);

        currentPlayer.addCardToHand(carrotCard);
        currentPlayer.addCardToHand(lettuceCard);
        currentPlayer.addCardToHand(tomatoCard);

        // Calculate the score for the current player
        int score = scoring.calculateScore(currentPlayer.getHand(), players, currentPlayer);

        // Expect score to be 3 based on TOTAL VEGGIES
        assertEquals(3, score);
    }

}