package game.scoring;

import card.Card;
import card.PointSaladCard;
import player.Player;
import java.util.List;

public class PointSaladScoring implements Scoring {

    @Override
    public int calculateScore(List<Card> hand, List<Player> players, Player currentPlayer) {
        int totalScore = 0;

        for (Card card : hand) {
            PointSaladCard criteriaCard = (PointSaladCard) card;

            if (criteriaCard.isCriteriaSideUp()) {
                String criteria = criteriaCard.getCriteria();
                String[] parts = criteria.split(",");

                // Handle TOTAL, TYPE, and SET criteria
                if (criteria.contains("TOTAL") || criteria.contains("TYPE") || criteria.contains("SET")) {
                    // TOTAL vegetable count logic
                    if (criteria.contains("TOTAL")) {
                        int countVeg = countTotalVegetables(hand);
                        int thisHandCount = countVeg;
                        for (Player p : players) {
                            if (p != currentPlayer) {
                                int playerVeg = countTotalVegetables(p.getHand());
                                if (criteria.contains("MOST") && playerVeg > countVeg) {
                                    countVeg = playerVeg;
                                }
                                if (criteria.contains("FEWEST") && playerVeg < countVeg) {
                                    countVeg = playerVeg;
                                }
                            }
                        }
                        if (countVeg == thisHandCount) {
                            totalScore += parseScoreFromCriteria(criteria);
                        }
                    }

                    // TYPE criteria logic
                    if (criteria.contains("TYPE")) {
                        String[] expr = criteria.split("/");
                        int addScore = Integer.parseInt(expr[0].trim());
                        if (expr[1].contains("MISSING")) {
                            int missing = 0;
                            for (PointSaladCard.Vegetable vegetable : PointSaladCard.Vegetable.values()) {
                                if (countVegetables(hand, vegetable) == 0) {
                                    missing++;
                                }
                            }
                            totalScore += missing * addScore;
                        } else {
                            int atLeastPerVegType = Integer
                                    .parseInt(expr[1].substring(expr[1].indexOf(">=") + 2).trim());
                            int totalType = 0;
                            for (PointSaladCard.Vegetable vegetable : PointSaladCard.Vegetable.values()) {
                                int countVeg = countVegetables(hand, vegetable);
                                if (countVeg >= atLeastPerVegType) {
                                    totalType++;
                                }
                            }
                            totalScore += totalType * addScore;
                        }
                    }

                    // SET criteria logic
                    if (criteria.contains("SET")) {
                        int addScore = 12;
                        for (PointSaladCard.Vegetable vegetable : PointSaladCard.Vegetable.values()) {
                            int countVeg = countVegetables(hand, vegetable);
                            if (countVeg == 0) {
                                addScore = 0;
                                break;
                            }
                        }
                        totalScore += addScore;
                    }
                }
                // Handle MOST and FEWEST criteria
                else if (criteria.contains("MOST") || criteria.contains("FEWEST")) {
                    String[] expr = criteria.split(" ");
                    String veg = expr[1].trim();
                    try {
                        PointSaladCard.Vegetable vegetable = PointSaladCard.Vegetable.valueOf(veg);
                        int countVeg = countVegetables(hand, vegetable);
                        int thisHandCount = countVeg;

                        for (Player p : players) {
                            if (p != currentPlayer) {
                                int playerVeg = countVegetables(p.getHand(), vegetable);
                                if (criteria.contains("MOST") && playerVeg > countVeg) {
                                    countVeg = playerVeg;
                                }
                                if (criteria.contains("FEWEST") && playerVeg < countVeg) {
                                    countVeg = playerVeg;
                                }
                            }
                        }

                        if (countVeg == thisHandCount) {
                            totalScore += parseScoreFromCriteria(criteria);
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid vegetable in criteria: " + veg);
                    }
                }
                // Handle criteria with "+", "/" or multiple conditions
                else if (criteria.contains("+") || criteria.contains("/") || parts.length > 1) {
                    if (criteria.contains("+")) {
                        String expr = criteria.split("=")[0].trim();
                        String[] vegs = expr.split("\\+");
                        int[] nrVeg = new int[vegs.length];
                        int countSameKind = 1;
                        for (int j = 1; j < vegs.length; j++) {
                            if (vegs[0].trim().equals(vegs[j].trim())) {
                                countSameKind++;
                            }
                        }
                        if (countSameKind > 1) {
                            totalScore += (countVegetables(hand, PointSaladCard.Vegetable.valueOf(vegs[0].trim()))
                                    / countSameKind) * parseScoreFromCriteria(criteria);
                        } else {
                            for (int i = 0; i < vegs.length; i++) {
                                nrVeg[i] = countVegetables(hand, PointSaladCard.Vegetable.valueOf(vegs[i].trim()));
                            }
                            int min = nrVeg[0];
                            for (int x = 1; x < nrVeg.length; x++) {
                                if (nrVeg[x] < min) {
                                    min = nrVeg[x];
                                }
                            }
                            totalScore += min * parseScoreFromCriteria(criteria);
                        }
                    } else if (parts[0].contains("=")) {
                        String[] vegParts = parts[0].split(":");
                        String veg = vegParts[1].trim();
                        int countVeg = countVegetables(hand, PointSaladCard.Vegetable.valueOf(veg));
                        totalScore += (countVeg % 2 == 0) ? 7 : 3;
                    } else {
                        for (int i = 0; i < parts.length; i++) {
                            String[] veg = parts[i].split("/");
                            totalScore += Integer.parseInt(veg[0].trim())
                                    * countVegetables(hand, PointSaladCard.Vegetable.valueOf(veg[1].trim()));
                        }
                    }
                }
            }
        }
        return totalScore;
    }

    // Helper method to parse score from a criteria string
    private int parseScoreFromCriteria(String criteria) {
        try {
            return Integer.parseInt(criteria.substring(criteria.indexOf("=") + 1).trim());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing score from criteria: " + criteria);
            return 0;
        }
    }

    private int countVegetables(List<Card> hand, PointSaladCard.Vegetable vegetable) {
        int count = 0;
        for (Card card : hand) {
            PointSaladCard pointCard = (PointSaladCard) card;
            if (!pointCard.isCriteriaSideUp() && pointCard.getVegetable() == vegetable) {
                count++;
            }
        }
        return count;
    }

    private int countTotalVegetables(List<Card> hand) {
        int count = 0;
        for (Card card : hand) {
            PointSaladCard pointCard = (PointSaladCard) card;
            if (!pointCard.isCriteriaSideUp()) {
                count++;
            }
        }
        return count;
    }
}
