package words.g5;

import words.core.Letter;
import words.core.PlayerBids;
import words.core.SecretState;
import words.core.ScrabbleValues;

import java.util.ArrayList;
import java.util.List;

public class Group5Player extends words.core.Player {
    private int maxVowelBid = 16;
    private int minVowelBid = 2;
    private int vowelBid = 10;

    private boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) >= 0;
    }

    private int countCharacterList(List<Character> letters, Character letter) {
        int count = 0;
        for (int i=0; i<letters.size(); ++i) {
            if (letters.get(i) == letter) {
                ++count;
            }
        }
        return count;
    }

    private int countLetterArrayList(ArrayList<Letter> letters, Letter letter) {
        int count = 0;
        for (int i=0; i<letters.size(); ++i) {
            if (letters.get(i).getCharacter() == letter.getCharacter()) {
                ++count;
            }
        }
        return count;
    }

    private int countLetterArr(char[] chars, char letter) {
        int count = 0;

        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == letter) {
                ++count;
            }
        }
        return count;
    }

    private double[] countVowelsAndConsonants() {
        double numVowels = 0.0;
        double numConsonants = 0.0;
        for (Character c : myLetters) {
            if (isVowel(c)) {
                ++numVowels;
            } else {
                ++numConsonants;
            }
        }

        return new double[]{numVowels, numConsonants};
    }

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds, ArrayList<String> playerList, SecretState secretstate, int playerID) {
        String currentBest = returnWord();

        //if we can already make a 7+ letter word, bid 1
        if (currentBest.length() >= 7) {
            return Math.min(1, secretstate.getScore());
        }

        //if we have vowels and consonants in a suboptimal ratio, bid more for vowels
        double[] num = countVowelsAndConsonants();
        if ((num[0] / num[1]) < 0.33) {
            vowelBid = Math.min(vowelBid + 1, maxVowelBid);
        } else if (vowelBid > minVowelBid) {
            vowelBid = Math.max(vowelBid - 1, minVowelBid);
        }

        ArrayList<Letter> secretLetters = secretstate.getSecretLetters();

        Character bidChar = bidLetter.getCharacter();
        int numChar = countCharacterList(myLetters, bidChar) + countLetterArrayList(secretLetters, bidLetter);

        myLetters.add(bidChar);
        String possibleBest = returnWord();
        myLetters.remove(bidChar);

        int numCharInBest = countLetterArr(possibleBest.toCharArray(), bidChar);
        int bid;

        //if we need this character to make the best word given our current letters
        if (possibleBest.contains(bidChar.toString()) && numChar < numCharInBest) {
            if (possibleBest.length() >= 7) {
                bid = 25;
            } else {
                bid = ScrabbleValues.getWordScore(possibleBest) / 2 + 1;
            }
        }
        else if (isVowel(bidChar) && !myLetters.contains(bidChar) && !secretLetters.contains(bidLetter)) {
            bid = vowelBid;
        }
        else {
            bid = 8;
        }

        return Math.min(bid, secretstate.getScore());
    }
}