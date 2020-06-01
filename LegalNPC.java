import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LegalNPC extends AbstractPlayer {

    public LegalNPC(Hand hand) { // should it generate a hand based on a hand or based on a full/ remaining deck?
        this.setHand(hand); //
        this.setScore(0);
    }

    public Hand returnLegal(Hand trick) {
        Hand legalHand;
        if (trick.isEmpty()) {
            return this.getHand();
        }
        else {
            Suit lead = (Suit) trick.getFirst().getSuit();
            legalHand = this.getHand().extractCardsWithSuit(lead);
            if (legalHand.isEmpty()) {
                return this.getHand();
            }
            else {
                return legalHand;
            }
        }
    }
    @Override
    public Card returnMove(Hand trick) {
        Hand legalHand = this.returnLegal(trick);
        Card chosenCard;
        int x = random.nextInt(legalHand.getNumberOfCards());
        chosenCard = legalHand.get(x);
        return this.getHand().getCard(chosenCard.getSuit(),chosenCard.getRank());
    }

    public Card randomCard() {
        int x = random.nextInt(this.getHand().getNumberOfCards());
        return this.getHand().get(x);
    }
    
    public String getType() {
        return "legal";
    }

}
