package words.g6;

import words.core.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class g6PlayerV2 extends Player {

    /**
     * Method is called when it is this player's turn to submit a bid. They should guarantee that
     * the value they return is between 0 and secretstate.getScore(). The highest bid wins, but
     * the winner only pays the price of the second-highest bid.
     * <p>
     * This implementation just bids the value of the letter
     *
     * @param bidLetter the Letter currently up for bidding on
     * @param playerBidList an unmodifiable list of previous bids from this game
     * @param totalRounds the total number of rounds in the game, which is different from the current round
     * @param playerList list of all player names
     * @param secretstate set of data that is stored unique to each player (their score and secret letters)
     * @param playerID the ID of the player being asked to bid (can ignore)
     * @return the amount to bid for the letter
     */
    private int totalRounds;
    private int remainingRounds;
    private int lettersPerPlayer;
    private Set<Word> wordSet;

    private ThreadLocalRandom random;


    public g6PlayerV2() {
        this.random = ThreadLocalRandom.current();
        this.wordSet = new HashSet<>();
    }


    public void startNewGame(int playerID, int numPlayers) {
        myID = playerID; // store my ID

        initializeWordlist(); // read the file containing all the words

        this.numPlayers = numPlayers; // so we know how many players are in the game

        // Initialize other variables needed for the game
        this.totalRounds = 0; // Reset or initialize total rounds
        this.remainingRounds = 0; // Reset or initialize remaining rounds
        this.lettersPerPlayer = 8 * numPlayers; // Calculate letters per player

        for (Word word : wordlist) {
            wordSet.add(word);
        }
    }

    @Override
    public void startNewRound(SecretState secretstate) {
        myLetters.clear(); // clear the letters that I have
        // this puts the secret letters into the currentLetters List
        myLetters.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());

        playerLetters.clear(); // clear the letters that all the players have
        for (int i = 0; i < numPlayers; i++) {
            playerLetters.add(new LinkedList<Character>()); // initialize each player's list of letters
        }

        // Reset or update any round-specific variables
        remainingRounds--; // Decrement the remaining rounds
    }


    public String returnHypoWord(List<Character> myLettersHypothetical) {

        // verbose way of building a String from a bunch of characters.
        char c[] = new char[myLettersHypothetical.size()];
        for (int i = 0; i < c.length; i++) {
            c[i] = myLettersHypothetical.get(i);
        }
        String s = new String(c);

        // iterate through our word list. If we find one we can build,
        // check to see if it's an improvement on the best one we've seen.
        Word ourletters = new Word(s);
        Word bestword = new Word("");
        for (Word w : wordlist) {
            if (ourletters.contains(w)) {
                if (w.score > bestword.score) {
                    bestword = w;
                }

            }
        }

        return bestword.word;

    }


    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        List<Character> myLettersHypothetical = new ArrayList<>(this.myLetters);

        char currentbidLetter = bidLetter.getCharacter();

        int myBid = 3;

        int scoreInThisRound = secretstate.getScore();

        if (scoreInThisRound > 50) {
            myBid = numPlayers;

        }


        myLettersHypothetical.add(currentbidLetter);
        String endingWordHypo = returnHypoWord(myLettersHypothetical);
        String endingWord = returnWord();

        int scoreHypo = ScrabbleValues.getWordScore(endingWordHypo);
        int scoreNow = ScrabbleValues.getWordScore(endingWord);

        System.err.println("scoreHypo : " + scoreHypo);
        System.err.println("scoreNow :" + scoreNow);


        if (scoreHypo > scoreNow) {

            myBid = this.random.nextInt(numPlayers, numPlayers + 3);
            ; // Slightly higher bid for important letters

        }

        System.err.println("Player bid list size:" +playerBidList.size());
        if (scoreNow > 50) {
            myBid = 1;
        }

        if(scoreInThisRound <25){
            myBid = 1;
        }

        //int charCount = Collections.frequency(myLetters, currentbidLetter);

        //if (charCount >= 2) {
          //  myBid = 2;

        //}
        return myBid;



    }
}