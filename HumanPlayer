import ch.aplu.jcardgame.*;

public class HumanPlayer extends AbstractPlayer {

    public HumanPlayer(Hand h1) {
        super(h1);
        CardListener cardListener = new CardAdapter();  // Human Player plays card
        super.hand.addCardListener(cardListener);
    }

    public Card returnMove(Card selected) {
        /** */
        return selected;
    }

    //public void leftDoubleClicked(Card card) { selected = card; hands[0].setTouchEnabled(false); }

    public String getType() {
        return "human";
    }
}
