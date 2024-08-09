package words.g4;

import words.core.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Group4Player extends Player {

    private double bidFraction = 0.1; // fraction of calculated EV to form bid
    private int bidThreshold = 91; // max score in scrabblewordlist.txt; METHOXYBENZENES or OXYPHENBUTAZONE
    private int bidTotal = 0;
    private int prevBid = 0;
    private int round = 0; // track current round

    private final Map<Word, int[]> initializeAvailableWords = new HashMap<>();
    private int[] initializeRemainingCharacters = new int[26];

    private Map<Word, int[]> currAvailableWords;
    private int[] remainingCharacters = new int[26];

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
        initializeScrabbleWordlist(); // read the file containing all the words and create a new file of Scrabble valid words

        this.numPlayers = numPlayers; // so we know how many players are in the game

        // Initialize the remaining characters to the max frequency for Scrabble
        for (int i = 0; i < 26; i++) {
            initializeRemainingCharacters[i] = ScrabbleValues.getLetterFrequency((char) ('A' + i));
        }

        bidTotal = 0;
        prevBid = 0;
        round = 0;
    }

    /**
     * Resets the available words & characters every new round.
     */
    public void resetAvailableWordsAndCharacters() {
        // Reset the list of available words from the remaining unbid characters
        currAvailableWords = new HashMap<>();

        for (Map.Entry<Word, int[]> entry : initializeAvailableWords.entrySet()) {
            currAvailableWords.put(entry.getKey(), Arrays.copyOf(entry.getValue(), 26));
        }

        // Reset the list of available characters
        remainingCharacters = Arrays.copyOf(initializeRemainingCharacters, 26);
    }

    /**
     * Checks every current available word against the current remaining characters.
     * If the current word needs more characters than remaining available characters,
     * remove the word for this round.
     */
    public void updateAvailableWordsAndCharacters() {
        ArrayList<Word> toRemove = new ArrayList<>();

        for (Map.Entry<Word, int[]> entry : currAvailableWords.entrySet()) {
            int[] currStringCount = entry.getValue();
            for (int i = 0; i < 26; i++) {
                if (currStringCount[i] > remainingCharacters[i]) {
                    toRemove.add(entry.getKey());
                    break;
                }
            }
        }

        for (Word w : toRemove) {
            currAvailableWords.remove(w);
        }
    }

    @Override
    public void startNewRound(SecretState secretstate) {
        resetAvailableWordsAndCharacters();

        // Clear the letters that I have
        myLetters.clear();

        // Put the secret letters into the currentLetters List
        myLetters.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());

        // Update the list of available characters after accounting for my secret letters
        for (Character c : myLetters) {
            remainingCharacters[c - 'A'] = Math.max(remainingCharacters[c - 'A'] - 1, 0);
        }

        updateAvailableWordsAndCharacters();

        // Clear the letters that all the players have
        playerLetters.clear();
        for (int i = 0; i < numPlayers; i++) {
            playerLetters.add(new LinkedList<Character>()); // initialize each player's list of letters
        }

        round = 0;
        bidTotal = 0; 
    }

    private void filterImpossibleWords(Character currLetter) {
        remainingCharacters[currLetter - 'A'] = Math.max(remainingCharacters[currLetter - 'A'] - 1, 0);
        updateAvailableWordsAndCharacters();
    }

    /**
     * This is a helper method that reads the list of words and stores valid Scrabble words in a List
     * so that the returnWord() method can choose a word from it at the end of a round.
     */
    public void initializeScrabbleWordlist() {
        String line;
        ArrayList<Word> wtmp = new ArrayList<>(300000);
        try {
            BufferedReader r = new BufferedReader(new FileReader("files/scrabblewordlist.txt"));
            while ((line = r.readLine()) != null) {
                Word word = new Word(line.trim());
                wtmp.add(word);

                // Initialize Possible Word and the list of characters in the word
                int[] currString = new int[26];
                for (char c : line.trim().toCharArray()) {
                    currString[c - 'A'] += 1;
                }
                initializeAvailableWords.put(word, currString);
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

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        // adds previous bid if won
        if (!playerBidList.isEmpty() && round != 0 && myID == playerBidList.get(round - 1).getWinnerID()) {
            bidTotal += prevBid;
        }

        System.err.println("hidfhajdf" + bidTotal + "    adfh lol " + bidThreshold);

        int myBid = (int)(Math.random() * secretstate.getScore()) / 8;

        double totalProb = 0.0;
        double expectedValue = 0.0;

        for (Word word : currAvailableWords.keySet()) { // Iterate over currAvailableWords, not wordlist
            double wordProb = calculateWordProb(word);
            int wordScore = ScrabbleValues.getWordScore(word.word);

            expectedValue += wordProb * wordScore;
            totalProb += wordProb;
        }

        if (totalProb > 0) {
            expectedValue /= totalProb;
        }

        myBid = (int) (expectedValue * bidFraction) + ScrabbleValues.letterScore(bidLetter.getCharacter()) / 3;

        myBid = Math.min(myBid, bidThreshold - bidTotal);
        prevBid = myBid;
        round += 1;

        System.err.println("what is my bid" + myBid);

        return myBid;
    }

    private double calculateWordProb(Word word) {
        double probability = 1.0;

        for (char c : word.word.toCharArray()) {
            probability *= ScrabbleValues.getLetterFrequency(c);
        }

        probability /= word.word.length();

        return probability;
    }

    @Override
    public void bidResult(PlayerBids currentBids) {
        // Update the list of which player has which letters
        char currChar = currentBids.getTargetLetter().getCharacter();

        playerLetters.get(currentBids.getWinnerID()).add(currChar);

        // See if I won the bid and add it to my letters if so
        if (myID == currentBids.getWinnerID()) {
            myLetters.add(currChar);
        }

        filterImpossibleWords(currChar);
    }
}
