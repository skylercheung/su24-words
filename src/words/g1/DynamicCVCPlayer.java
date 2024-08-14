package words.g1;

import words.core.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class DynamicCVCPlayer extends Player {

    private Logger log = Logger.getLogger(GameController.class.getName());

    private final char[] INITIAL_PRIORITY_LETTERS = {'A', 'R', 'T', 'O', 'L', 'N', 'E', 'D', 'S', 'I', 'U'};
    private char[] priorityLetters;
    private List<String> sevenLetterWords;
    private static int PRIORITY_BID = 8; // (50/7) + 1 = 8
    private static int n = 5; // Number of top frequent letters to prioritize
    private static int fixBid = 3; // 固定出价

    public DynamicCVCPlayer() {
        this(n); // 默认取前5个频率最高的字母
    }

    public DynamicCVCPlayer(int n) {
        this.sevenLetterWords = new ArrayList<>();
        this.priorityLetters = INITIAL_PRIORITY_LETTERS;
        loadSevenLetterWords("files/7letterWords.txt");
    }

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        ArrayList<Letter> secretLetters = secretstate.getSecretLetters();
        ArrayList<Character> secretChars = new ArrayList<>();

        for (Letter letter : secretLetters) {
            char character = letter.getCharacter();
            secretChars.add(character);

            // 检查字符是否在 INITIAL_PRIORITY_LETTERS 中
            if (!isInInitialPriorityLetters(character)) {
                myLetters.remove((Character) character); // 从 myLetters 中移除字符
            }
        }

        log.info("----------Secret letters: " + secretChars);
        log.info("----------My letters: " + myLetters);

        int totalBidsNum = (8 - secretLetters.size()) * numPlayers;
        int currentBidsNum = 1;

        for (List<Character> innerList : playerLetters) {
            currentBidsNum += innerList.size(); // 累加每个内层列表的大小
        }

        log.info("----------Total bids number: " + totalBidsNum + ", current bids number: " + currentBidsNum);

        char letter = bidLetter.getCharacter();
        int totalLettersWon = myLetters.size();

        updateparam(totalBidsNum, currentBidsNum, letter);

        // 如果玩家手中没有字母，并且竞标字母在优先字母列表中，则出价
        if (totalLettersWon == 0 && isInitialPriorityLetter(letter)) {
            return Math.min(PRIORITY_BID + ScrabbleValues.letterScore(letter), secretstate.getScore());
        }

        // 当玩家有1~5个字母时，根据已有字母更新优先字母列表
        if (totalLettersWon > 0 && totalLettersWon < 6) {
            updatePriorityLetters();
            log.info("----------Priority letters: " + new String(priorityLetters));
        }

        // 当玩家有6个字母时，根据已有字母更新优先字母列表（此时无视n）
        if (totalLettersWon == 6) {
            updatePriorityLettersForSix();
            log.info("----------Priority letters: " + new String(priorityLetters));
        }

        // 只有已经按计划拿到七个绝对能组成7-letter-word的字母时，才进行防守性出价
        if (totalLettersWon > 6) {
            return fixBid;
        }

        // 确保我们只有在优先字母列表中时才竞标
        if (isPriorityLetter(letter)) {
            return Math.min(PRIORITY_BID + ScrabbleValues.letterScore(letter), secretstate.getScore());
        }

        return fixBid;
    }

    private void updateparam(int totalBidsNum, int currentBidsNum, char letter) {
        // 更新参数
        double ratio = (double) currentBidsNum / (double) totalBidsNum;

        // 根据赛程调整出价，赛程越靠后，出价越高
        if (ratio > 0.7) {
            PRIORITY_BID = 12;
        } else if (ratio > 0.4 && ratio <= 0.7) {
            PRIORITY_BID = 8;
        } else {
            PRIORITY_BID = 4;
        }

        // 根据赛程调整优先字母数量，赛程越靠后，优先字母数量越多
        if (ratio > 0.7) {
            n = 8;
        } else if (ratio > 0.4 && ratio <= 0.7) {
            n = 6;
        } else {
            n = 4;
        }

        // 如果是head-to-head比赛，则全程极为激进，需要抢字母
        if (numPlayers <= 3) {
            PRIORITY_BID = 12;
            n = 12;
            // 防守时，只有对常见字母出高价，这样可以避免一些稀有高分字母破坏7-letter-word的组成
            if (isInInitialPriorityLetters(letter))
            {
                fixBid = 5;
            } else {
                fixBid = 0;
            }
        }

        log.info("----------ratio: " + ratio + ", PRIORITY_BID: " + PRIORITY_BID + ", n: " + n);
    }

    private void loadSevenLetterWords(String filePath) {
        try {
            this.sevenLetterWords = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePriorityLetters() {
        Map<Character, Integer> letterFrequency = new HashMap<>();
        for (String word : sevenLetterWords) {
            if (containsAllLetters(word, myLetters)) {
                for (char c : word.toCharArray()) {
                    if (!myLetters.contains(c)) {
                        letterFrequency.put(c, letterFrequency.getOrDefault(c, 0) + 1);
                    }
                }
            }
        }

        // 删除不在 INITIAL_PRIORITY_LETTERS 中的键值对
        letterFrequency.keySet().removeIf(c -> !isInInitialPriorityLetters(c));

        log.info("----------Letter frequency: " + letterFrequency);

        // Sort the letters by frequency and get the top N
        this.priorityLetters = letterFrequency.entrySet().stream()
                .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString().toCharArray();
    }

    private void updatePriorityLettersForSix() {
        Set<Character> possibleLetters = new HashSet<>();
        for (String word : sevenLetterWords) {
            if (containsAllLetters(word, myLetters)) {
                for (char c : word.toCharArray()) {
                    if (!myLetters.contains(c)) {
                        possibleLetters.add(c);
                    }
                }
            }
        }
        log.info("----------------Possible letters: " + possibleLetters);
        this.priorityLetters = new char[possibleLetters.size()];
        int i = 0;
        for (char c : possibleLetters) {
            this.priorityLetters[i++] = c;
        }
    }

    private boolean containsAllLetters(String word, List<Character> letters) {
        for (char letter : letters) {
            if (word.indexOf(letter) == -1) {
                return false;
            }
        }
        return true;
    }

    private boolean isInitialPriorityLetter(char letter) {
        for (char priorityLetter : INITIAL_PRIORITY_LETTERS) {
            if (priorityLetter == letter) {
                return true;
            }
        }
        return false;
    }

    private boolean isPriorityLetter(char letter) {
        for (char priorityLetter : priorityLetters) {
            if (priorityLetter == letter) {
                return true;
            }
        }
        return false;
    }

    // 检查字符是否在 INITIAL_PRIORITY_LETTERS 中的方法
    private boolean isInInitialPriorityLetters(char character) {
        for (char priorityLetter : INITIAL_PRIORITY_LETTERS) {
            if (priorityLetter == character) {
                return true;
            }
        }
        return false;
    }
}
