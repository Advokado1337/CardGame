package game.scoring;

import card.Card;
import player.Player;

import java.util.List;

public interface Scoring {
    // TODO Implement Scoring
    public int calculateScore(List<Card> hand, List<Player> players, Player currentPlayer);

}
