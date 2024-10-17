package card;

public class CardFactory {

    public static Card createPointSaladCard(PointSaladCard.CardType type, String criteria,
            PointSaladCard.Vegetable vegetable) {
        return new PointSaladCard(type, criteria, vegetable);
    }

}
