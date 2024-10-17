package game;

import card.Card;
import java.util.List;

public class PointSaladScoring implements Scoring {

    @Override
    public int calculateScore(List<Card> hand) {
        int totalScore = 0;

        for (Card card : hand) {
            if (card.isCriteriaSideUp()) {
                // Implement the logic for calculating points based on card criteria
                String criteria = card.getCriteria();
                totalScore += evaluateCriteria(criteria, hand);
            }
        }
        return totalScore;
    }

    private int evaluateCriteria(String criteria, List<Card> hand) {
        // Logic to calculate score based on criteria (e.g., MOST veggies, SETs of
        // cards, etc.)
        // For now, let's assume a basic implementation:
        return criteria.contains("MOST") ? 5 : 2; // Example scoring
    }
}
