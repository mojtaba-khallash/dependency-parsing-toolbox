package edu.stanford.nlp.parser.ensemble.utils;

import edu.stanford.nlp.process.Morphology;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CleanUpStanford {

    public static final int WORD = 1;
    public static final int LEMMA = 2;
    public static final int POS = 3;
    public static final int LABEL = 7;
    public static final int HEAD = 6;

    public static void main(String[] args) throws Exception {
        Morphology morpha = new Morphology();
        BufferedReader is = new BufferedReader(new FileReader(args[0]));
        PrintStream os = new PrintStream(new FileOutputStream(args[1]));
        String line;
        ArrayList<String[]> sentence = new ArrayList<String[]>();

        while ((line = is.readLine()) != null) {
            if (line.trim().length() == 0) {
                analyze(sentence);
                print(os, sentence);
                os.println();
                sentence = new ArrayList<String[]>();
                continue;
            }
            String[] toks = line.split("[\t]+");
            //System.out.println(line);

            // convert all dep labels to upper case
            toks[LABEL] = toks[LABEL].toUpperCase();
            // convert NULL to ROOT
            if (toks[LABEL].equalsIgnoreCase("NULL")) {
                toks[LABEL] = "ROOT";
            }

            // generate lemmas
            toks[LEMMA] = morpha.stem(toks[WORD], toks[POS]).word();

            sentence.add(toks);
            /*
             * for(int i = 0; i < toks.length; i ++){ if(i > 0) os.print("\t");
             * os.print(toks[i]); } os.println();
             */
        }

        is.close();
        os.close();
    }

    private static void analyze(ArrayList<String[]> sent) {
        // check if there is a ROOT
        int foundRoot = 0;
        for (int i = 0; i < sent.size(); i++) {
            String[] toks = sent.get(i);
            if (Integer.parseInt(toks[HEAD]) == 0 && toks[LABEL].equals("ROOT")) {
                foundRoot++;
            }
        }
        if (foundRoot == 0) {
            System.err.println("Found sentence without ROOT!");
            print(System.err, sent);
        } else if (foundRoot > 1) {
            System.err.println("Found sentence with multiple ROOTs!");
            print(System.err, sent);
        }

        // check if there are cycles
        boolean foundCycle = false;
        for (int i = 0; i < sent.size(); i++) {
            if (hasCycles(sent, i)) {
                foundCycle = true;
                break;
            }
        }
        if (foundCycle) {
            System.err.println("Found sentence with cycles!");
            print(System.err, sent);
        }
    }

    private static boolean hasCycles(ArrayList<String[]> sent, int start) {
        Set<Integer> seen = new HashSet<Integer>();
        List<Integer> seq = new ArrayList<Integer>();
        for (int crt = start; crt >= 0; crt = Integer.parseInt(sent.get(crt)[HEAD]) - 1) {
            seq.add(crt);
            if (seen.contains(crt)) {
                System.err.print("CYCLE:");
                for (Integer i : seq) {
                    System.err.print(" " + i);
                }
                System.err.println();
                return true;
            }
            seen.add(crt);
        }
        return false;
    }

    private static void print(PrintStream os, ArrayList<String[]> sent) {
        for (int j = 0; j < sent.size(); j++) {
            String[] toks = sent.get(j);
            for (int i = 0; i < toks.length; i++) {
                if (i > 0) {
                    os.print("\t");
                }
                os.print(toks[i]);
            }
            os.println();
        }
    }
}