import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;



public abstract class AbstractPlayer {

    private Hand hand;
    private int score;
    private Actor scoreActor;

    static final Random random = ThreadLocalRandom.current();

    /*public Player(Hand hand) { // should it generate a hand based on a hand or based on a full/ remaining deck?
        this.hand = hand; //
        this.score = 0;
    } */

    // return random Card from players hand
  /*  public Card randomCard(){
        int x = random.nextInt(this.hand.getNumberOfCards());
        return this.hand.get(x);
    } */
    //npc

    // return random Card from ArrayList
    /**public static Card randomCard(ArrayList<Card> list){
     int x = random.nextInt(list.size());
     return list.get(x);
     }*/


    public abstract Card returnMove(Hand trick);

    public void setScore(int score) {
        this.score = score;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public void setScoreActor(TextActor textActor) {
        this.scoreActor = textActor;
    }

    public Actor getScoreActor() {
        return this.scoreActor;
    }

    public int getScore() {
        return this.score;
    }

    public Hand getHand() {
        return this.hand;
    }

    public void incrementScore() {
        this.score += 1;
    }

}


/**
 class LegalNPC extends NPC {
 public generateLegalMoves {
 }
 public
 }
 class smartNPC extends LegalNPC {
 }*/
