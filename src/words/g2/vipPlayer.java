package words.g2;

import words.core.*;

import java.util.ArrayList;
import java.util.List;

public class vipPlayer extends Player {

    /**
     * Method is called when it is this player's turn to submit a bid. They should guarantee that
     * the value they return is between 0 and secretstate.getScore(). The highest bid wins, but
     * the winner only pays the price of the second-highest bid.
     *
     * This implementation bids a random amount up to 1/8 of its score
     *
     * @param bidLetter the Letter currently up for bidding on
     * @param playerBidList an unmodifiable list of previous bids from this game
     * @param totalRounds the total number of rounds in the game, which is different from the current round
     * @param playerList list of all player names
     * @param secretstate set of data that is stored unique to each player (their score and secret letters)
     * @param playerID the ID of the player being asked to bid (can ignore)
     * @return the amount to bid for the letter
     */
    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        if(!secretstate.getSecretLetters().isEmpty()){
            char bidChar = bidLetter.getCharacter();
            List<Character> copyLetters=new ArrayList<Character>();
            for(Character c:myLetters){
                copyLetters.add(c);
            }

            String bestWord = returnTestWord(copyLetters,secretstate);

            if(bestWord.length()>=7){
                return Math.min(bidLetter.getValue(),5);
            }

            int myScore;
            if (this.scores!=null) {
                myScore = this.scores.get(this.myID);
            }
            else{
                myScore=100;
            }

            int myBid = 125 / (ScrabbleValues.letterScore(bidChar) * ScrabbleValues.getLetterFrequency(bidChar));
            myBid = Math.min(myBid,myScore-1);

            copyLetters.add(bidChar);

            String newBestWord = returnTestWord(copyLetters,secretstate);

            //if((ScrabbleValues.getWordScore(newBestWord)-ScrabbleValues.getWordScore(bestWord))> myBid/2 ){
            if(newBestWord.length()>bestWord.length()){
                return myBid;
            }
            else{
                return myBid/3;
            }

        }

        //DO NOT CHANGE
        char bidChar = bidLetter.getCharacter();
        List<Character> copyLetters=new ArrayList<Character>();
        for(Character c:myLetters){
            copyLetters.add(c);
        }

        String bestWord = returnTestWord(copyLetters,secretstate);

        if(bestWord.length()>=7){
            return Math.min(bidLetter.getValue(),5);
        }

        int myBid = 75 / (ScrabbleValues.letterScore(bidChar) * ScrabbleValues.getLetterFrequency(bidChar));
        int originalBid=myBid;

        copyLetters.add(bidChar);

        String newBestWord = returnTestWord(copyLetters,secretstate);

        if((ScrabbleValues.getWordScore(newBestWord)-ScrabbleValues.getWordScore(bestWord))> originalBid/2 ){// add /2 back
            return myBid;
        }
        else{
            return myBid/3;
        }
    }

    @Override
    public void startNewGame(int playerID, int numPlayers){
        myID = playerID; // store my ID
        logger.info(Integer.toString(playerID));

        initializeWordlist(); // read the file containing all the words

        this.numPlayers = numPlayers; // so we know how many players are in the game
    }

    public String returnTestWord(List<Character> letters, SecretState secretstate) {

        List<Character> copy = new ArrayList<Character>();
        for(Character c:letters){
            copy.add(c);
        }

        /*if(secretstate.getSecretLetters().size()!=0){
            copy.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());
        }*/
        // verbose way of building a String from a bunch of characters.
        char c[] = new char[copy.size()];
        for (int i = 0; i < c.length; i++) {
            c[i] = copy.get(i);
        }
        String s = new String(c);
        //logger.info(s);

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



}