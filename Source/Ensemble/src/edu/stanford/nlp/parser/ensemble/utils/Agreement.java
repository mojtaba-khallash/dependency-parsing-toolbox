package edu.stanford.nlp.parser.ensemble.utils;

import java.io.*;
import java.util.*;

public class Agreement {

    public static void main(String[] args) throws IOException {
//		runSentenceSelection(
//				"/home/mcclosky/data/scr/StanfordParsed/gigaword/apw_eng/words_and_tags",
//				"/home/mcclosky/data/gigaword-selected/apw_eng");

        File develOutputs = new File("/home/mcclosky/data/gigaword-selected/devel_outputs");
        String[] filenames = develOutputs.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.contains("libsvm") || name.contains("mstparser");
            }
        });
        selectSentencesForAgreement(develOutputs, filenames, 100, develOutputs
                + "/" + "dev-6malt+mst-agree-100");
    }

    public static void runSentenceSelection(String input, String output)
            throws IOException {
        int numSentences = 0;
        @SuppressWarnings("unused")
        int[] numSentencesAboveThreshold = new int[21];
        // number of tokens in sentences in numSentencesAboveThreshold
        // (this is used for average length calculations)
        @SuppressWarnings("unused")
        int[] numTokensAboveThreshold = new int[21];

        File inputDirectory = new File(input);
        File outputDirectory = new File(output);

        // make output directories
        Map<Integer, File> agreementPercentToOutputDir = new HashMap<Integer, File>();
        for (int i = 70; i <= 100; i += 10) {
            File subDir = new File(outputDirectory, Integer.toString(i));
            subDir.mkdirs();
            agreementPercentToOutputDir.put(i, subDir);
        }

        // first, find all the simple filenames (ones with words and tags only)
        String[] wordsAndTagsFilenames = inputDirectory.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".parse.gz");
            }
        });

        /*
         * given the simple filenames, we find out which filenames start with
         * them since parses using the words and tags filenames use them as a
         * prefix.
         */
        for (final String wordsAndTagsFilename : wordsAndTagsFilenames) {
            System.out.println("filename: " + wordsAndTagsFilename);
            String[] allParses = inputDirectory.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(wordsAndTagsFilename)
                            && !name.equals(wordsAndTagsFilename);
                }
            });

            System.out.println("# parses: " + allParses.length);
            if (allParses != null && allParses.length > 1) {
                // we have parses for this words and tags filename

                for (int i = 70; i <= 100; i += 10) {
                    File outputDir = agreementPercentToOutputDir.get(i);
                    File outputFile = new File(outputDir, wordsAndTagsFilename);
                    try {
                        numSentences += selectSentencesForAgreement(
                                inputDirectory, allParses, i, outputFile.getAbsolutePath());
                    } catch (IOException e) {
                        // skip cases where not all files are available (this is
                        // mostly an issue while the parses are being generated)
                        System.out.println("(skipping due to IOError/permission problem)");
                        continue;
                    }
                }
            }

            System.out.println("Sentences so far: " + numSentences);
            // for (int i = 0; i < 21; i++) {
            // double averageLength = (double) numTokensAboveThreshold[i]
            // / numSentencesAboveThreshold[i];
            // double percentSentences = (double) numSentencesAboveThreshold[i]
            // / numSentences;
            //
            // System.out.format("%4s %.1f %.1f\n", i * 5,
            // percentSentences * 100, averageLength);
            // }
        }
    }

    /**
     * Given files in a specific directory and an agreement threshold, writes
     * sentences that agree at least that amount to an output file.
     *
     * @return number of sentences processed
     */
    @SuppressWarnings("unchecked")
    public static int selectSentencesForAgreement(File inputDirectory,
            String[] inputFilenames, double agreementThreshold,
            String outputFilename) throws IOException {
        BufferedReader[] is = makeReaders(inputDirectory, inputFilenames);

        List<Token>[] sents = new List[is.length];
        int numSentences = 0;

        BufferedWriter bw = FileUtils.openForWriting(outputFilename);
        while ((sents[0] = Token.readNextSentCoNLLX(is[0])) != null) {
            for (int i = 1; i < is.length; i++) {
                sents[i] = Token.readNextSentCoNLLX(is[i]);
            }

            numSentences++;

            double agreement = labelledNodesInAgreement(sents);
            if (agreement * 100 >= agreementThreshold) {
                List<Token> bestParse = getHighestAgreementParse(sents);
                Token.writeSentCoNLLX(bestParse, bw);
            }
        }

        bw.close();
        return numSentences;
    }

    @SuppressWarnings("unused")
    private static void collectAgreementStats(int numSentences,
            int[] numSentencesAboveThreshold, int[] numTokensAboveThreshold,
            BufferedReader[] readers, List<Token>[] sentences)
            throws IOException {

        // number of tokens in this sentence (assume all parsers
        // agree on this...)
        int numTokens = sentences[0].size();
        double agreementPercent = labelledNodesInAgreement(sentences);

        for (int i = 0; i < 21; i++) {
            if (agreementPercent >= i * 0.05) {
                numSentencesAboveThreshold[i]++;
                numTokensAboveThreshold[i] += numTokens;
            }
        }
    }

    public static BufferedReader[] makeReaders(File baseDirectory,
            String[] filenames) throws IOException {

        BufferedReader[] is = new BufferedReader[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            String filename = baseDirectory + "/" + filenames[i];
            is[i] = FileUtils.openForReading(filename);
        }
        return is;
    }

    /**
     * Returns the sentence with the highest average agreement with all other
     * sentences.
     *
     * @param sentences array of sentences
     * @return sentence with highest average agreement
     */
    @SuppressWarnings("unchecked")
    public static List<Token> getHighestAgreementParse(List<Token>[] sentences) {
        double agreements[][] = new double[sentences.length][sentences.length];

        // collect pairwise agreements
        for (int i = 0; i < sentences.length; i++) {
            for (int j = i + 1; j < sentences.length; j++) {
                List<Token>[] justTheseSentences = new List[]{sentences[i],
                    sentences[j]};
                double a = Agreement.labelledNodesInAgreement(justTheseSentences);
                agreements[i][j] = a;
                agreements[j][i] = a;
            }
        }

        // then add up the agreements and find the largest
        double totalAgreement[] = new double[sentences.length];
        double highestSoFar = 0;
        int bestSoFar = -1;
        for (int i = 0; i < sentences.length; i++) {
            for (int j = 0; j < sentences.length; j++) {
                if (i == j) {
                    continue;
                }

                totalAgreement[i] += agreements[i][j];
            }

            if (totalAgreement[i] > highestSoFar) {
                bestSoFar = i;
                highestSoFar = totalAgreement[i];
            }
        }

        return sentences[bestSoFar];
    }

    /**
     * Calculate the percentage of how many tokens are the same in all parses of
     * the sentences.
     *
     * @param sentences list of parses of the same sentences
     * @return percentage of nodes (0.0 to 1.0)
     */
    public static double labelledNodesInAgreement(List<Token>[] sentences) {
        // how many tokens all parsers agree on
        int numTokensAllAgreed = 0;
        int numTokens = sentences[0].size();

        for (int tokenIndex = 0; tokenIndex < numTokens; tokenIndex++) {
            Set<String> tokens = new HashSet<String>();
            for (List<Token> sent : sentences) {
                Token currentToken = sent.get(tokenIndex);
                // this is sort of a hack to avoid importing Pair so we can
                // avoid JavaNLP dependencies
                tokens.add(Integer.toString(currentToken.head)
                        + currentToken.label);
            }

            if (tokens.size() == 1) {
                numTokensAllAgreed++;
            }
        }

        return (double) numTokensAllAgreed / numTokens;
    }

    /**
     * Calculate the percentage of how many tokens are the same in all parses of
     * the sentences.
     *
     * @param sentences list of parses of the same sentences
     * @return percentage of nodes (0.0 to 1.0)
     */
    public static double nodesInAgreement(List<Token>[] sentences) {
        // how many tokens all parsers agree on
        int numTokensAllAgreed = 0;
        int numTokens = sentences[0].size();

        for (int tokenIndex = 0; tokenIndex < numTokens; tokenIndex++) {
            Set<Integer> parents = new HashSet<Integer>();
            for (List<Token> sent : sentences) {
                Token currentToken = sent.get(tokenIndex);
                parents.add(currentToken.head);
            }

            if (parents.size() == 1) {
                numTokensAllAgreed++;
            }
        }

        return (double) numTokensAllAgreed / numTokens;
    }
}