package edu.stanford.nlp.parser.ensemble.utils;

import java.io.*;
import java.util.*;

public class HighAgreementLowSimilarity {

    static final double MAX_COSINE = 0.01;
    HashMap<String, Integer> wordIndex;
    double[] trainVector;
    double trainVectorLength;
    static Set<String> stopWords = new HashSet<String>(Arrays.asList(
            "a",
            "about",
            "above",
            "across",
            "after",
            "afterwards",
            "again",
            "against",
            "all",
            "almost",
            "alone",
            "along",
            "already",
            "also",
            "although",
            "always",
            "am",
            "among",
            "amongst",
            "amoungst",
            "amount",
            "an",
            "and",
            "another",
            "any",
            "anyhow",
            "anyone",
            "anything",
            "anyway",
            "anywhere",
            "are",
            "around",
            "as",
            "at",
            "back",
            "be",
            "became",
            "because",
            "become",
            "becomes",
            "becoming",
            "been",
            "before",
            "beforehand",
            "behind",
            "being",
            "below",
            "beside",
            "besides",
            "between",
            "beyond",
            "bill",
            "both",
            "bottom",
            "but",
            "by",
            "call",
            "can",
            "cannot",
            "cant",
            "co",
            "computer",
            "con",
            "could",
            "couldnt",
            "cry",
            "de",
            "describe",
            "detail",
            "do",
            "done",
            "down",
            "due",
            "during",
            "each",
            "eg",
            "eight",
            "either",
            "eleven",
            "else",
            "elsewhere",
            "empty",
            "enough",
            "etc",
            "even",
            "ever",
            "every",
            "everyone",
            "everything",
            "everywhere",
            "except",
            "few",
            "fifteen",
            "fify",
            "fill",
            "find",
            "fire",
            "first",
            "five",
            "for",
            "former",
            "formerly",
            "forty",
            "found",
            "four",
            "from",
            "front",
            "full",
            "further",
            "get",
            "give",
            "go",
            "had",
            "has",
            "hasnt",
            "have",
            "he",
            "hence",
            "her",
            "here",
            "hereafter",
            "hereby",
            "herein",
            "hereupon",
            "hers",
            "herself",
            "him",
            "himself",
            "his",
            "how",
            "however",
            "hundred",
            "i",
            "ie",
            "if",
            "in",
            "inc",
            "indeed",
            "interest",
            "into",
            "is",
            "it",
            "its",
            "itself",
            "keep",
            "last",
            "latter",
            "latterly",
            "least",
            "less",
            "ltd",
            "made",
            "many",
            "may",
            "me",
            "meanwhile",
            "might",
            "mill",
            "mine",
            "more",
            "moreover",
            "most",
            "mostly",
            "move",
            "much",
            "must",
            "my",
            "myself",
            "name",
            "namely",
            "neither",
            "never",
            "nevertheless",
            "next",
            "nine",
            "no",
            "nobody",
            "none",
            "noone",
            "nor",
            "not",
            "nothing",
            "now",
            "nowhere",
            "of",
            "off",
            "often",
            "on",
            "once",
            "one",
            "only",
            "onto",
            "or",
            "other",
            "others",
            "otherwise",
            "our",
            "ours",
            "ourselves",
            "out",
            "over",
            "own",
            "part",
            "per",
            "perhaps",
            "please",
            "put",
            "rather",
            "re",
            "s",
            "same",
            "see",
            "seem",
            "seemed",
            "seeming",
            "seems",
            "serious",
            "several",
            "she",
            "should",
            "show",
            "side",
            "since",
            "sincere",
            "six",
            "sixty",
            "so",
            "some",
            "somehow",
            "someone",
            "something",
            "sometime",
            "sometimes",
            "somewhere",
            "still",
            "such",
            "system",
            "take",
            "ten",
            "than",
            "that",
            "the",
            "their",
            "them",
            "themselves",
            "then",
            "thence",
            "there",
            "thereafter",
            "thereby",
            "therefore",
            "therein",
            "thereupon",
            "these",
            "they",
            "thick",
            "thin",
            "third",
            "this",
            "those",
            "though",
            "three",
            "through",
            "throughout",
            "thru",
            "thus",
            "to",
            "together",
            "too",
            "top",
            "toward",
            "towards",
            "twelve",
            "twenty",
            "two",
            "un",
            "under",
            "until",
            "up",
            "upon",
            "us",
            "very",
            "via",
            "was",
            "we",
            "well",
            "were",
            "what",
            "whatever",
            "when",
            "whence",
            "whenever",
            "where",
            "whereafter",
            "whereas",
            "whereby",
            "wherein",
            "whereupon",
            "wherever",
            "whether",
            "which",
            "while",
            "whither",
            "who",
            "whoever",
            "whole",
            "whom",
            "whose",
            "why",
            "will",
            "with",
            "within",
            "without",
            "would",
            "yet",
            "you",
            "your",
            "yours",
            "yourself",
            "yourselves"));

    public void makeWordIndex(String trainFile) throws IOException {
        wordIndex = new HashMap<String, Integer>();
        BufferedReader is = new BufferedReader(new FileReader(trainFile));
        List<Token> sent;
        int value = 0;
        while ((sent = Token.readNextSentCoNLLX(is)) != null) {
            for (Token t : sent) {
                if ((t.pos.startsWith("NN") || t.pos.startsWith("VB")) && !stopWords.contains(t.form.toLowerCase())) {
                    wordIndex.put(t.form, value);
                    value++;
                }
            }
        }
        is.close();
        System.err.printf("Found %d words in training.\n", wordIndex.size());
        trainVector = new double[value];
        Arrays.fill(trainVector, 0.0);
    }

    public void makeWordVector(String trainFile) throws IOException {
        BufferedReader is = new BufferedReader(new FileReader(trainFile));
        List<Token> sent;
        while ((sent = Token.readNextSentCoNLLX(is)) != null) {
            for (Token t : sent) {
                if ((t.pos.startsWith("NN") || t.pos.startsWith("VB")) && !stopWords.contains(t.form.toLowerCase())) {
                    Integer pos = wordIndex.get(t.form);
                    if (pos != null) {
                        trainVector[pos] += 1.0;
                    }
                }
            }
        }
        is.close();

        trainVectorLength = vectorLength(trainVector);
        System.err.println("Length of train vector = " + trainVectorLength);
    }

    public static double vectorLength(double[] v) {
        double sum = 0.0;
        for (int i = 0; i < v.length; i++) {
            sum += (v[i] * v[i]);
        }
        sum = Math.sqrt(sum);
        return sum;
    }

    public double[] makeVector(List<Token> sent) {
        double[] v = new double[trainVector.length];
        Arrays.fill(v, 0.0);
        for (Token t : sent) {
            if ((t.pos.startsWith("NN") || t.pos.startsWith("VB")) && !stopWords.contains(t.form.toLowerCase())) {
                Integer pos = wordIndex.get(t.form);
                if (pos != null) {
                    v[pos] += 1.0;
                }
            }
        }
        return v;
    }

    public double cosine(double[] v) {
        double vl = vectorLength(v);
        if (vl == 0) {
            return 0;
        }
        double sum = 0.0;
        for (int i = 0; i < trainVector.length; i++) {
            sum += trainVector[i] * v[i];
        }
        double cos = sum / (vl * trainVectorLength);
        return cos;
    }

    /*
     * private static void readStopWords(String fn) throws IOException {
     * BufferedReader is = new BufferedReader(new FileReader(fn)); String line;
     * stopWords = new HashSet<String>(); while((line = is.readLine()) != null){
     * stopWords.add(line.trim().toLowerCase()); } }
     */
    public static void main(String[] args) throws Exception {
        String trainFile = args[0];
        File inputDirectory = new File(args[1]);
        String outputDirectory = args[2];

        // stopwords are hard coded now
        // readStopWords("data/stopwords.txt");
        HighAgreementLowSimilarity selector = new HighAgreementLowSimilarity();
        selector.makeWordIndex(trainFile);
        selector.makeWordVector(trainFile);

        // select sents from inputDirectory that have 0 cosine sim with trainVector
        String[] wordsAndTagsFilenames = inputDirectory.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".parse.gz");
            }
        });

        int sentCount = 0;
        int lenSum = 0;
        for (String wordsAndTagsFilename : wordsAndTagsFilenames) {
            System.err.println("File: " + wordsAndTagsFilename);
            BufferedReader is = FileUtils.openForReading(inputDirectory.getAbsolutePath() + File.separator + wordsAndTagsFilename);
            BufferedWriter os = FileUtils.openForWriting(outputDirectory + File.separator + wordsAndTagsFilename);
            List<Token> sent;
            while ((sent = Token.readNextSentCoNLLX(is)) != null) {
                double[] vector = selector.makeVector(sent);
                double cos = selector.cosine(vector);
                // System.err.println("COS = " + cos);
                if (cos <= MAX_COSINE && valid(sent)) {
                    sentCount++;
                    Token.writeSentCoNLLX(sent, os);
                    lenSum += sent.size();
                }
            }
            is.close();
            os.close();
        }
        System.err.printf("Found %d sentences with cosine < %f.\n", sentCount, MAX_COSINE);
        double avgLen = (double) lenSum / (double) sentCount;
        System.err.printf("Avg sentence length = %f\n", avgLen);
    }

    private static boolean valid(List<Token> sent) {
        if (sent.size() < 3) {
            return false;
        }
        boolean foundNoun = false;
        boolean foundVerb = false;
        for (int i = 0; i < sent.size(); i++) {
            Token t = sent.get(i);
            if (t.pos.startsWith("NN") && !stopWords.contains(t.form.toLowerCase())) {
                foundNoun = true;
            } else if (t.pos.startsWith("VB") && !stopWords.contains(t.form.toLowerCase())) {
                foundVerb = true;
            }
        }
        if (foundNoun && foundVerb) {
            return true;
        }
        return false;
    }
}