
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Asus
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        //Load dictionary
        Set<String> dictionary = new HashSet<>();
        dictionary = loadDict(dictionary);

        while (true) {
            String givenWord = getInput();
            boolean isCorrect = checkSpelling(givenWord, dictionary);
            if (isCorrect) {
                System.out.println("Correct word");
            } else {
                provideSuggestion(givenWord, dictionary);
            }
            boolean cont = askToContinue();
            if (!cont) {
                break;
            }
        }
//        DoubleMetaphone dm = new DoubleMetaphone();
//        System.out.println(dm.isDoubleMetaphoneEqual(dm.doubleMetaphone("helo"), dm.doubleMetaphone("hello")));
    }

    private static String getInput() {
        String input;

        while (true) {
            System.out.print("Enter a word: ");
            Scanner sc = new Scanner(System.in);
            input = sc.nextLine().trim().toLowerCase();
            if (input.matches("[a-zA-Z]+")) {
                return input;
            } else if (input.matches("(.*)(.?)\\d+(.*)(.?)")) {
                System.out.println("Word cannot contain numbers, please try again.");
            } else if (input.matches("(.*)(.?)[^a-zA-Z0-9](.*)(.?)")) {
                System.out.println("Word cannot contain special characters or space, please try again.");
            } else {
                System.out.println("Please enter a word contain [a-z] and [A-Z] only.");
            }
        }
    }

    private static boolean checkSpelling(String givenWord, Set<String> dictionary) {
        return dictionary.contains(givenWord);
    }

    private static void provideSuggestion(String givenWord, Set<String> dictionary) {
        List<String> closeWord = new ArrayList<>();
//        List<String> closeWordDM = new ArrayList<>();
        MultiMap<String, String> closeWordDMMap = new MultiValueMap<>();
        closeWord = removeDup(closeWord);

        //1 wrong character
        for (int i = 0; i < givenWord.length(); i++) {
            for (char c = 'a'; c <= 'z'; c++) {
                char[] charArr = givenWord.toCharArray();
                charArr[i] = c;
                String word = "";
                for (int j = 0; j < charArr.length; j++) {
                    word = word + charArr[j];
                }
                if (dictionary.contains(word)) {
                    closeWord.add(word);
                }
            }
        }

        //1 redundant letter
        for (int i = 0; i < givenWord.length(); i++) {
            char[] charArr = givenWord.toCharArray();
            char[] charArr2 = new char[charArr.length - 1];
            int charArr2Index = 0;
            for (int j = 0; j < charArr.length; j++) {
                if (j == i) {
                    continue;
                }
                charArr2[charArr2Index] = charArr[j];
                charArr2Index++;
            }
            String word = "";
            for (int k = 0; k < charArr2.length; k++) {
                word = word + charArr2[k];
            }
            if (dictionary.contains(word)) {
                closeWord.add(word);
            }
        }

        //1 less letter
        for (int i = 0; i < givenWord.length() + 1; i++) {
            char[] charArr = givenWord.toCharArray();
            char[] charArr2 = new char[charArr.length + 1];
            for (int j = 0; j < charArr.length; j++) {
                if (j < i) {
                    charArr2[j] = charArr[j];
                } else {
                    charArr2[j + 1] = charArr[j];
                }
            }
            for (char c = 'a'; c <= 'z'; c++) {
                charArr2[i] = c;
                String word = "";
                for (int j = 0; j < charArr2.length; j++) {
                    word = word + charArr2[j];
                }
                if (dictionary.contains(word)) {
                    closeWord.add(word);
                }
            }
        }

        //2 character swap place 
        for (int i = 0; i < givenWord.length() - 1; i++) {
            char[] charArr = givenWord.toCharArray();
            char temp = charArr[i];
            charArr[i] = charArr[i + 1];
            charArr[i + 1] = temp;
            String word = "";
            for (int j = 0; j < charArr.length; j++) {
                word = word + charArr[j];
            }
            if (dictionary.contains(word)) {
                closeWord.add(word);
            }
        }

        DoubleMetaphone dm = new DoubleMetaphone();
        dm.setMaxCodeLen(100);
        String giveWordDM = dm.doubleMetaphone(givenWord);

//        //Change close word list to double metaphone list 
//        for (String word : closeWord) {
//            System.out.println("word " + word);
//            word = dm.doubleMetaphone(word);
//            System.out.println("dm " + word);
//            closeWordDM.add(word);
//        }
        //Change close word list to double metaphone map 
        for (String word : closeWord) {
            closeWordDMMap.put(dm.doubleMetaphone(word), word);
        }

        //Clear close word set
        closeWord.clear();

        //Add word that similar in double metaphone
        for (Map.Entry<String, Object> entry : closeWordDMMap.entrySet()) {
            if(dm.isDoubleMetaphoneEqual(entry.getKey(), giveWordDM)) closeWord.add(entry.getValue().toString());
//            if (entry.getKey().compareTo(giveWordDM) == 0) {
//                closeWord.add(entry.getValue());
//            }
        }

        //Display
        if (!closeWord.isEmpty()) {
            System.out.print("You mean: ");
            for (int i = 0; i < closeWord.size(); i++) {
                if (i == closeWord.size() - 1) {
                    System.out.println(closeWord.get(i));
                    break;
                }
                System.out.print(closeWord.get(i) + " ");
            }
        } else {
            System.out.println("Incorrect word but unable to get suggestion yet");
        }
    }

    private static Set<String> loadDict(Set<String> dictionary) throws FileNotFoundException {
        File dict = new File("words_alpha.txt");
        if (dict.exists()) {
            Scanner scanner = new Scanner(dict);
            scanner.useDelimiter("\\s+");
            while (scanner.hasNext()) {
                dictionary.add(scanner.nextLine().toLowerCase());
            }
        } else {
            System.out.println("Dictionary not found");
            System.exit(0);
        }
        return dictionary;
    }

    private static List<String> removeDup(List<String> closeWord) {
        Set<String> noDupSet = new LinkedHashSet<>();
        noDupSet.addAll(closeWord);
        closeWord.clear();
        closeWord.addAll(noDupSet);
        return closeWord;
    }

    private static boolean askToContinue() {
        System.out.print("Do you want to continue? (Y) for YES and (N) for NO: ");
        while (true) {
            Scanner sc = new Scanner(System.in);
            String answer = sc.nextLine();
            if (answer.matches("[ynYN]")) {
                if (answer.equalsIgnoreCase("Y")) {
                    return true;
                } else if (answer.equalsIgnoreCase("N")) {
                    return false;
                }
            } else {
                System.out.print("Please type (Y) for YES and (N) for NO (Y and N can be in lower case): ");
            }
        }
    }
}
