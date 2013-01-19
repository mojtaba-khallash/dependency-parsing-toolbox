package edu.stanford.nlp.parser.ensemble.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LengthDistribution {

    private Map<Integer, Integer> lengths = new HashMap<Integer, Integer>();
    private int totalCounts = 0;
    private Random random = new Random(0);
    private int _maxLength;

    /**
     * Construct a length distribution from the lengths of sentences in file
     * (CoNLL-X format)
     *
     * @param filename path to filename to measure the counts in
     * @param maxLength we count all lengths that are greater than maxLength in
     * one bucket (for the purposes of smoothing). Recommended value is around
     * 80.
     * @throws IOException
     */
    public LengthDistribution(String filename, int maxLength)
            throws IOException {
        _maxLength = maxLength;

        BufferedReader is = FileUtils.openForReading(filename);

        List<Token> sentence;
        while ((sentence = Token.readNextSentCoNLLX(is)) != null) {
            int length = sentence.size();
            int currentValue = getCount(length);
            lengths.put(length, currentValue + 1);
            totalCounts += 1;
        }
    }

    private int getCount(int length) {
        if (length > _maxLength) {
            length = _maxLength;
        }

        return lengths.containsKey(length) ? lengths.get(length) : 0;
    }

    /**
     * Tells you whether or not to accept a sentence according this length
     * distribution.
     *
     * @param length
     * @return
     */
    public boolean acceptLength(int length) {
        double prob = (double) getCount(length) / totalCounts;
        double sample = random.nextDouble();
        return sample <= prob;
    }

    @Override
    public String toString() {
        return "LengthDistribution [maxLength=" + _maxLength + ", lengths="
                + lengths + "]";
    }

    public static void main(String[] args) throws IOException {
        LengthDistribution lengths = new LengthDistribution(
                "/home/mcclosky/data/mihai-CoNLL08/train.ptb", 80);

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        outputDir.mkdirs();

        int totalSentences = 0;
        int acceptedSentences = 0;
        for (String inputFilename : inputDir.list()) {
            File outputFile = new File(outputDir, inputFilename);
            System.out.format("%s -> %s\n", inputFilename, outputFile.getAbsolutePath());

            BufferedReader inputReader = FileUtils.openForReading(new File(
                    inputDir, inputFilename).getAbsolutePath());
            BufferedWriter outputWriter = FileUtils.openForWriting(outputFile.getAbsolutePath());

            List<Token> sentence;
            while ((sentence = Token.readNextSentCoNLLX(inputReader)) != null) {
                totalSentences++;
                int length = sentence.size();
                if (lengths.acceptLength(length)) {
                    acceptedSentences++;
                    Token.writeSentCoNLLX(sentence, outputWriter);
                }
            }
            System.out.format("accepted: %.1f%% (%s of %s)\n",
                    (double) acceptedSentences / totalSentences * 100,
                    acceptedSentences, totalSentences);

            inputReader.close();
            outputWriter.close();
        }
    }
}