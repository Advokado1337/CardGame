package pile;

import card.Card;

import java.util.ArrayList;
import java.util.List;

public class Pile {
    public List<Card> cards = new ArrayList<>();
    public Card[] activeCards = new Card[2]; // Two visible active cards

    public Pile(List<Card> cards) {
        this.cards = cards;
        if (cards.size() >= 2) {
            this.activeCards[0] = cards.remove(0);
            this.activeCards[1] = cards.remove(0);
            this.activeCards[0].flipCard();
            this.activeCards[1].flipCard();
        } else {
            System.out.println("Not enough cards to assign to active cards");
        }
    }

    // Get top special card (formerly point card)
    public Card getSpecialCard() {
        return cards.isEmpty() ? null : cards.get(0);
    }

    // Buy the top special card (formerly point card)
    public Card buySpecialCard() {
        return cards.isEmpty() ? null : cards.remove(0);
    }

    // Get active card at a specified index (formerly veggie card)
    public Card getActiveCard(int index) {
        return activeCards[index];
    }

    // Buy an active card and replace it (formerly veggie card)
    public Card buyActiveCard(int index) {
        Card boughtCard = activeCards[index];
        if (!cards.isEmpty()) {
            activeCards[index] = cards.remove(0);
            activeCards[index].flipCard();
        } else {
            activeCards[index] = null;
        }
        return boughtCard;
    }

    // Check if the pile is empty (both cards and active cards)
    public boolean isEmpty() {
        return cards.isEmpty() && activeCards[0] == null && activeCards[1] == null;
    }

}
