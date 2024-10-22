package card;

public class PointSaladCard extends Card {

    public enum CardType { // Perhaps Useless?
        VEGETABLE,
        POINT
    }

    public enum Vegetable {
        CARROT,
        ONION,
        PEPPER,
        TOMATO,
        LETTUCE,
        CABBAGE
    }

    // Now we can create a PointCityCard that defines its card type if we want in
    // the future.

    private CardType cardType;
    private Vegetable vegetable;

    public PointSaladCard(CardType cardType, String criteria, Vegetable vegetable) {
        super(criteria);
        this.cardType = cardType;
        this.vegetable = vegetable;
    }

    @Override
    public String getCardType() {
        return cardType == CardType.POINT ? "POINT" : vegetable.toString(); // Vegetable name if flipped
    }

    public Vegetable getVegetable() {
        return vegetable;
    }

    @Override
    public String toString() {
        if (isCriteriaSideUp()) {
            return "Point Criteria: " + getCriteria() + " (" + vegetable + ")";
        } else {
            return "Vegetable Card: " + vegetable;
        }
    }

}
