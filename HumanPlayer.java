import ch.aplu.jcardgame.*;

public class HumanPlayer extends AbstractPlayer {

    public HumanPlayer(Hand h1) {
        this.hand = h1;
        CardListener cardListener = new CardAdapter();  // Human Player plays card
        super.hand.addCardListener(cardListener);
    }

    public Card returnMove(Hand hand) {
        /** this isn't a working function but is just here to comply with abstractplayer outline*/
        return hand.getCard(0); // just a placeholder
    }

    //public void leftDoubleClicked(Card card) { selected = card; hands[0].setTouchEnabled(false); }

    public String getType() {
        return "human";
    }
}
