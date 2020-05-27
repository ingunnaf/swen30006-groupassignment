// Whist.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


@SuppressWarnings("serial")
public class WhistwithComments extends CardGame {
    //global variables
    public enum Suit
    {
        SPADES, HEARTS, DIAMONDS, CLUBS
    }

    public enum Rank
    {
        // Reverse order of rank importance (see rankGreater() below)
        // Order of cards is tied to card images
        ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO
    }
    //global variable
    final String trumpImage[] = {"bigspade.gif","bigheart.gif","bigdiamond.gif","bigclub.gif"};

    static final Random random = ThreadLocalRandom.current();

    // return random Enum value
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    //move to player
    // return random Card from Hand
    public static Card randomCard(Hand hand){
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }

    // return random Card from ArrayList
    public static Card randomCard(ArrayList<Card> list){
        int x = random.nextInt(list.size());
        return list.get(x);
    }

    //smart npc needs this???
    public boolean rankGreater(Card card1, Card card2) {
        return card1.getRankId() < card2.getRankId(); // Warning: Reverse rank order of cards (see comment on enum)
    }

    private final String version = "1.0";
    public final int nbPlayers = 4;
    public final int nbStartCards = 13;
    public final int winningScore = 11;
    private final int handWidth = 400;
    private final int trickWidth = 40;
    private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    private final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };
    private final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(575, 25),
            new Location(650, 575)
    };
    //private Actor[] scoreActors = {null, null, null, null };

    private Player[] players = {null, null, null, null};
    /** The above line is neweewwwwww and should replace scoreActors, hands and scores arrays*/

    private final Location trickLocation = new Location(350, 350); //dont move
    private final Location textLocation = new Location(350, 450); //dont move
    private final int thinkingTime = 2000;
    private Hand[] hands; //used to deal out the deck, but subsequently the hands are stored within players
    private Location hideLocation = new Location(-500, - 500);
    private Location trumpsActorLocation = new Location(50, 50);
    private boolean enforceRules=false; //properties

    public void setStatus(String string) { setStatusText(string); }

//private int[] scores = new int[nbPlayers];

    Font bigFont = new Font("Serif", Font.BOLD, 36);



    private void createPlayers(String p0, String p1, String p2, String p3) {
        /** The arguments for this function should be modified to be enumerations of the different
         * types of players we will create in each specific game - they should be loaded in from properties file*/
        players[0] = new Player(p0);
        players[1] = new Player(p1);
        players[2] = new Player(p2);
        players[3] = new Player(p3);
    } //instantiating players
    private void initScore() {
        for (int i = 0; i < nbPlayers; i++) {

            //scores[i] = 0;
            players[i].setScore(0);
            //scoreActors[i] = new TextActor("0", Color.WHITE, bgColor, bigFont);
            players[i].setScoreActor(new TextActor("0", Color.WHITE, bgColor, bigFont));
            addActor(players[i].getScoreActor(), scoreLocations[i]);
        }
    }
    /** The grey was the old and has been commented out, the lines there are the modification*/
    private void updateScore(int player) {
        //removeActor(scoreActors[player]);
        removeActor(players[player].getScoreActor());
        players[player].setScoreActor(new TextActor(String.valueOf(scores[player]), Color.WHITE, bgColor, bigFont));
        //scoreActors[player] = new TextActor(String.valueOf(scores[player]), Color.WHITE, bgColor, bigFont);
        //addActor(scoreActors[player], scoreLocations[player]);
        addActor(players[player].getScoreActor(), scoreLocations[player]);
    }  // updateScore (player[1].getScore)

    private Card selected;

    private void initRound() {
        hands = deck.dealingOut(nbPlayers, nbStartCards); // Last element of hands is leftover cards; these are ignored
        for (int i = 0; i < nbPlayers; i++) {
            hands[i].sort(Hand.SortType.SUITPRIORITY, true); //player.getHand.sort(...)
        }


        // Set up human player for interaction
        CardListener cardListener = new CardAdapter()  // Human Player plays card
        {
            public void leftDoubleClicked(Card card) { selected = card; hands[0].setTouchEnabled(false); }
        }; //this stuff is moved to HumanPlayer class
        hands[0].addCardListener(cardListener);  //HumanPlayer.getHand.addCardListener(///)
        // graphics
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            layouts[i] = new RowLayout(handLocations[i], handWidth);
            layouts[i].setRotationAngle(90 * i);
            // layouts[i].setStepDelay(10);
            hands[i].setView(this, layouts[i]); //Player.gethand...
            hands[i].setTargetArea(new TargetArea(trickLocation));
            hands[i].draw();
        }
//	    for (int i = 1; i < nbPlayers; i++)  // This code can be used to visually hide the cards in a hand (make them face down)
//	      hands[i].setVerso(true);
        // End graphics
    }

    private Optional<Integer> playRound() {  // Returns winner, if any
        // Select and display trump suit
        final Suit trumps = randomEnum(Suit.class); //player needs to access this - static variable?
        final Actor trumpsActor = new Actor("sprites/"+trumpImage[trumps.ordinal()]);
        addActor(trumpsActor, trumpsActorLocation);
        // End trump suit
        Hand trick; //npc players need to access this
        int winner;
        Card winningCard;
        Suit lead;  //npc players need to know this
        int nextPlayer = random.nextInt(nbPlayers); // randomly select player to lead for this round
        for (int i = 0; i < nbStartCards; i++) {
            trick = new Hand(deck);
            selected = null;
            if (0 == nextPlayer) {  // Select lead depending on player type
                hands[0].setTouchEnabled(true); //HumanPlayer.getHand ( )
                setStatus("Player 0 double-click on card to lead.");
                while (null == selected) delay(100);
            } else {
                setStatusText("Player " + nextPlayer + " thinking...");
                delay(thinkingTime);
                selected = randomCard(hands[nextPlayer]);
            }
            // Lead with selected card
            trick.setView(this, new RowLayout(trickLocation, (trick.getNumberOfCards()+2)*trickWidth));
            trick.draw();
            selected.setVerso(false);
            // No restrictions on the card being lead
            lead = (Suit) selected.getSuit();  //npc needs to know this
            selected.transfer(trick, true); // transfer to trick (includes graphic effect)
            winner = nextPlayer;
            winningCard = selected;
            // End Lead
            for (int j = 1; j < nbPlayers; j++) {
                if (++nextPlayer >= nbPlayers) nextPlayer = 0;  // From last back to first
                selected = null;
                if (0 == nextPlayer) {
                    hands[0].setTouchEnabled(true); //HumanPlayer
                    setStatus("Player 0 double-click on card to follow.");
                    while (null == selected) delay(100);
                } else {
                    setStatusText("Player " + nextPlayer + " thinking...");
                    delay(thinkingTime);
                    selected = randomCard(hands[nextPlayer]); //npcs are playing: selected = Player[nextPlayer].returnSelected()
                }
                // Follow with selected card
                trick.setView(this, new RowLayout(trickLocation, (trick.getNumberOfCards()+2)*trickWidth));
                trick.draw();
                selected.setVerso(false);  // In case it is upside down
                // Check: Following card must follow suit if possible
                if (selected.getSuit() != lead && hands[nextPlayer].getNumberOfCardsWithSuit(lead) > 0) {
                    // Rule violation
                    String violation = "Follow rule broken by player " + nextPlayer + " attempting to play " + selected;
                    System.out.println(violation);
                    if (enforceRules)
                        try {
                            throw(new BrokeRuleException(violation));
                        } catch (BrokeRuleException e) {
                            e.printStackTrace();
                            System.out.println("A cheating player spoiled the game!");
                            System.exit(0);
                        }
                }
                // End Check
                selected.transfer(trick, true); // transfer to trick (includes graphic effect)
                System.out.println("winning: suit = " + winningCard.getSuit() + ", rank = " + winningCard.getRankId());
                System.out.println(" played: suit = " +    selected.getSuit() + ", rank = " +    selected.getRankId());
                if ( // beat current winner with higher card
                        (selected.getSuit() == winningCard.getSuit() && rankGreater(selected, winningCard)) ||
                                // trumped when non-trump was winning
                                (selected.getSuit() == trumps && winningCard.getSuit() != trumps)) {
                    System.out.println("NEW WINNER");
                    winner = nextPlayer;
                    winningCard = selected;
                }
                // End Follow
            }
            delay(600);
            trick.setView(this, new RowLayout(hideLocation, 0));
            trick.draw();
            nextPlayer = winner;
            setStatusText("Player " + nextPlayer + " wins trick.");
            scores[nextPlayer]++;  //Player[nextPlayer].setScore()
            updateScore(nextPlayer); //just the drawing part??
            if (winningScore == scores[nextPlayer]) return Optional.of(nextPlayer);
        }
        removeActor(trumpsActor);
        return Optional.empty();
    }

    public Whist()
    {
        super(700, 700, 30);
        setTitle("Whist (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
        createPlayers();
        initScore();
        Optional<Integer> winner;
        do {
            initRound();
            winner = playRound();
        } while (!winner.isPresent());
        addActor(new Actor("sprites/gameover.gif"), textLocation);
        setStatusText("Game over. Winner is player: " + winner.get());
        refresh();
    }

    public static void main(String[] args)
    {
        // System.out.println("Working Directory = " + System.getProperty("user.dir"));
        new Whist();
    }

}
