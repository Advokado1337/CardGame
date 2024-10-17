package game;

import card.Card;
import java.util.List;

public interface Scoring {

    public int calculateScore(List<Card> hand);

}
