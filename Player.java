public class Player extends Actor {
    //

    private Hand hand;
    private int score;
    private Actor scoreActor;

    public Player(Hand hand, Actor scoreActor) { // should it generate a hand based on a hand or based on a full/ remaining deck?
        this.hand = hand; //
        this.score = 0;
        this.scoreActor = scoreActor;
    }

    // return random Card from Hand
    public Card randomCard(Hand hand){
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }

    // return random Card from ArrayList
    public static Card randomCard(ArrayList<Card> list){
        int x = random.nextInt(list.size());
        return list.get(x);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setScoreActor(TextActor textActor) {
        this.scoreActor = textActor;
    }

    public TextActor getScoreActor() {
        return this.scoreActor;
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