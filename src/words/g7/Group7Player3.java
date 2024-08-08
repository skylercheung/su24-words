package words.g7;

import words.core.*;

import java.util.*;

public class Group7Player3 extends Player {
    // set a max price
    private static final int MAX_BID = 15;
    private Random rand = new Random();

    private int[] letterFrequency = new int[26];
    private List<Word> wordsSizeBiggerThan7 = new ArrayList<>();
    private Set<Character> vowel = new HashSet<>(Arrays.asList('A', 'E', 'I', 'O', 'U'));


    @Override
    public void startNewGame(int playerID, int numPlayers) {
        super.startNewGame(playerID, numPlayers);
        int total = 0;
        // Calculate 7 or more letters words' frequency
        for (Word word : wordlist) {
            if(word.word.length() >= 7){
                wordsSizeBiggerThan7.add(word);
                for (char c : word.word.toCharArray()) {
                    letterFrequency[c - 'A']++;
                    total++;
                }
            }
        }
        for(int i = 0; i < 26; i++){
            letterFrequency[i] = (int) Math.ceil((double) letterFrequency[i] / total * 100);
        }
    }

    @Override
    public void startNewRound(SecretState secretstate) {
        super.startNewRound(secretstate);
    }

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds,
                   ArrayList<String> playerList, SecretState secretstate, int playerID) {
        // if a 7 letters word is composed, bid 1
        if(checkIfCanBuildWordSizeBiggerThan7()){
            return 2;
        }

        // if already has 3 vowels, bid the remaining vowels for 1
        if(vowel.contains(bidLetter.getCharacter()) && myLetters.stream().filter(vowel::contains).count() >= 3){
            return 2;
        }

        if (myLetters.stream().filter(ch -> ch == bidLetter.getCharacter()).count() >= 2) {
            return 4;
        }



        int currentScore = secretstate.getScore();
        int letterValue = bidLetter.getValue();

        // determine the bid by the current score & letter value
        int bid = Math.min(currentScore, letterValue);

        int frequency = letterFrequency[bidLetter.getCharacter() - 'A'];
        if(frequency >= 10){
            bid += 6;
        }else if(frequency >= 8){
            bid += 5;
        }else if(frequency >= 6){
            bid += 4;
        }else if(frequency >= 4) {
            bid += 3;
        }else if(frequency <= 2) {
            bid -= 0;
        }else if(frequency <= 1) {
            bid -= 3;
        }

        if(bidLetter.getCharacter().equals('Z')){
            return 3;
        }else if(bidLetter.getCharacter().equals('Q')){
            return 3;
        }else if(bidLetter.getCharacter().equals('J')){
            return 3;
        }else if(bidLetter.getCharacter().equals('X')){
            return 3;
        }else if(bidLetter.getCharacter().equals('W')){
            return 3;
        }else if(bidLetter.getCharacter().equals('K')){
            return 3;
        }else if(bidLetter.getCharacter().equals('V')){
            return 3;
        }

        // adjust bid based on bid history
        bid = adjustBidBasedOnHistory(bid, bidLetter, playerBidList);


        // add some random bid
        bid += rand.nextInt(2);

        // make sure the bid is within a reasonable range
        bid = Math.min(bid, MAX_BID);
        bid = Math.max(bid, 2); //

        return bid;
    }

    private int adjustBidBasedOnHistory(int initialBid, Letter bidLetter, List<PlayerBids> playerBidList) {
        // check the letter's bid history, if the bid was higher, make the bid 1 more than the second highest bid
        for (PlayerBids playerBid : playerBidList) {
            if (playerBid.getTargetLetter().equals(bidLetter)) {
                int highestBid = playerBid.getBidValues().stream().max(Integer::compare).orElse(0);
                int secondHighestBid = playerBid.getBidValues().stream().filter(bid -> bid != highestBid).max(Integer::compare).orElse(0);
                if (highestBid > initialBid) {
                    initialBid = secondHighestBid + 1;
                }else if (highestBid < initialBid){
                    initialBid = highestBid;
                }
                break;
            }
        }
        return initialBid;
    }


    private boolean checkIfCanBuildWordSizeBiggerThan7(){
        for(Word word : wordsSizeBiggerThan7){
            if(canBuildWord(word.word)){
                return true;
            }
        }
        return false;
    }



    private boolean canBuildWord(String word) {
        Map<Character, Integer> letterCount = new HashMap<>();
        for (char letter : myLetters) {
            letterCount.put(letter, letterCount.getOrDefault(letter, 0) + 1);
        }
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (!letterCount.containsKey(ch) || letterCount.get(ch) == 0) {
                // if the letter is not in myLetters or there are no more of this letter left, return false
                return false;
            }
            letterCount.put(ch, letterCount.get(ch) - 1);
        }

        return true;
    }
}


