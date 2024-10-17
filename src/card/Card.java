package card;

public abstract class Card {
    private String criteria;
    private boolean criteriaSideUp;

    public Card(String criteria) {
        this.criteria = criteria;
        this.criteriaSideUp = true;
    }

    public String getCriteria() {
        return criteria;
    }

    public boolean isCriteriaSideUp() {
        return criteriaSideUp;
    }

    public void flipCard() {
        this.criteriaSideUp = !this.criteriaSideUp;
    }

    // Each specific card type will define its card type
    public abstract String getCardType();

    // Each specific card type will define its toString method
    @Override
    public abstract String toString();

}
