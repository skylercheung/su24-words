package words.g4;

import words.core.*;

import java.io.*;
import java.nio.Buffer;
import java.util.*;
import java.util.logging.Level;

public class Group4PlayerSkyler extends Player {

    private double bidFraction = 0.5; // fraction of calculated EV to form bid
    private int bidThreshold = 91; // max score in scrabblewordlist.txt; METHOXYBENZENES or OXYPHENBUTAZONE
    private int bidTotal = 0;
    private int prevBid = 0;

    /**
     * This method is called at the start of a new game.
     * This is where you'll learn your playerID—save this!
     * @param playerID the ID (0 through numPlayers - 1) of your player this game.
     * @param numPlayers the number of players in this game
     */
    @Override
    public void startNewGame(int playerID, int numPlayers) {
        myID = playerID; // store my ID

        createScrabbleWordList();
        initializeScrabbleWordlist(); // read the file containing all the words and create new file of Scrabble valid words

//        int maxScore = Integer.MIN_VALUE;
//        String maxWord = "";
//        try (BufferedReader br = new BufferedReader((new FileReader("files/wordlist.txt")))) {
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                int score = ScrabbleValues.getWordScore(line);
//                if (score >= maxScore) {
//                    maxScore = score;
//                    maxWord = line;
//                }
//            }
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "An error occurred.", e);
//        }
//
//        System.err.println("yo" + maxScore + maxWord);

        this.numPlayers = numPlayers; // so we know how many players are in the game

        bidTotal = 0;
    }

    /**
     * This is a helper method that reads the list of words and stores valid Scrabble words in a List
     * so that the returnWord() method can choose a word from it at the end of a round.
     */
    public void initializeScrabbleWordlist() {
        String line = null;
        ArrayList<Word> wtmp = new ArrayList<>(300000);
        try {
            BufferedReader r = new BufferedReader(new FileReader("files/scrabblewordlist.txt"));
            while (null != (line = r.readLine())) {
                wtmp.add(new Word(line.trim()));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred.", e);
        }
        wordlist = wtmp.toArray(new Word[0]);
    }

    private boolean isValidScrabbleWord(String word) {
        Map<Character, Integer> letterFreq = new HashMap<>();
        for (char c : word.toCharArray()) {
            c = Character.toUpperCase(c);
            letterFreq.put(c, letterFreq.getOrDefault(c, 0) + 1);
            if (letterFreq.get(c) > ScrabbleValues.getLetterFrequency(c)) {
                return false;
            }
        }
        return true;
    }

    public void createScrabbleWordList() {
        try (BufferedReader r = new BufferedReader(new FileReader("files/wordlist.txt"));
             BufferedWriter w = new BufferedWriter(new FileWriter("files/scrabblewordlist.txt"))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (isValidScrabbleWord(line.trim())) {
                    w.write(line.trim());
                    w.newLine();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred.", e);
        }
    }

    /**
     * This method is called at the start of a new round in the game.
     * The player can some initialization if it so chooses.
     * @param secretstate This player's secret state, including its secret letters
     */
    @Override
    public void startNewRound(SecretState secretstate){
        myLetters.clear(); // clear the letters that I have
        // this puts the secret letters into the currentLetters List
        myLetters.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());

        playerLetters.clear(); // clear the letters that all the players have
        for (int i = 0; i < numPlayers; i++) {
            playerLetters.add(new LinkedList<Character>()); // initialize each player's list of letters
        }
        /*
        Note that although the letters that I have will be in the playerLetters List, the playerLetters
        List doesn't include my secret letters.
         */
        bidTotal = 0;
    }

    /**
     * Method is called when it is this player's turn to submit a bid. They should guarantee that
     * the value they return is between 0 and secretstate.getScore(). The highest bid wins, but
     * the winner only pays the price of the second-highest bid.
     *
     * This implementation bids according to expected value of score
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

        // adds previous bid if won
        if (!playerBidList.isEmpty() && myID == playerBidList.get(totalRounds - 1).getWinnerID()) {
            bidTotal += prevBid;
        }

        int myBid = (int)(Math.random() * secretstate.getScore()) / 8;

        double totalProb = 0.0;
        double expectedValue = 0.0;

        List<Character> currentLetterPool = new ArrayList<>(myLetters);
        currentLetterPool.add(bidLetter.getCharacter());

        for (PlayerBids playerBids : playerBidList) {
            currentLetterPool.add(playerBids.getTargetLetter().getCharacter());
        }

        for (Word word : wordlist) {
            if (isValidScrabbleWord(word.word)) {
                double wordProb = calculateWordProb(word, currentLetterPool);
                int wordScore = ScrabbleValues.getWordScore(word.word);

                expectedValue += wordProb * wordScore;
                totalProb += wordProb;
            }
        }

        if (totalProb > 0) {
            expectedValue /= totalProb;
        }

        myBid = (int) (expectedValue * bidFraction) + ScrabbleValues.letterScore(bidLetter.getCharacter()) / 2;
        myBid = Math.min(myBid, bidThreshold - bidTotal);

        myBid = Math.min(myBid, 12);

        prevBid = myBid;

        return myBid;
    }

    private double calculateWordProb(Word word, List<Character> currentLetterPool) {
        double probability = 1.0;

        Map<Character, Integer> letterCount = new HashMap<>();
        for (char c : currentLetterPool) {
            letterCount.put(c, letterCount.getOrDefault(c, 0) + 1);
        }

        for (char c : word.word.toCharArray()) {
            probability *= ScrabbleValues.getLetterFrequency(c);
        }

        probability /= word.word.length();

        return probability;
    }
}
