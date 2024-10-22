package game.scoring;

import card.Card;
import player.Player;

import java.util.List;

public interface IScoring {

    public int calculateScore(List<Card> hand, List<Player> players, Player currentPlayer);

}
// TODO: Check scoring test
// TODO: Check 7-12 in some way?
// TODO: maybe use bot in scoring test