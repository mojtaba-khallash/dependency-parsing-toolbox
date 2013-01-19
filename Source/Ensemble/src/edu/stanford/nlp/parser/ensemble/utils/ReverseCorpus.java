/**
 * Changes the order of a corpus to right-to-left. This is needed to build
 * right-to-left shift-reduce models.
 */
package edu.stanford.nlp.parser.ensemble.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ReverseCorpus {

    static final int INDEX_POS = 0;
    static final int HEAD_POS = 6;

    public static void main(String[] args) throws Exception {
        reverseCorpus(args[0], args[0] + ".reversed");
    }

    public static void reverseCorpus(String in, String out) throws IOException {
        InputStream fis = new FileInputStream(in);
        if (in.endsWith(".gz")) {
            fis = new GZIPInputStream(fis);
        }
        BufferedReader is = new BufferedReader(new InputStreamReader(fis));
        PrintStream os = new PrintStream(new FileOutputStream(out));
        List<String[]> sent = new ArrayList<String[]>();
        String line;

        while ((line = is.readLine()) != null) {
            if (line.trim().length() == 0) {
                List<String[]> rev = reverseSentence(sent);
                for (String[] toks : rev) {
                    for (int i = 0; i < toks.length; i++) {
                        if (i > 0) {
                            os.print("\t");
                        }
                        os.print(toks[i]);
                    }
                    os.println();
                }
                os.println();
                sent = new ArrayList<String[]>();
            } else {
                String[] toks = line.split("[\t]+");
                sent.add(toks);
            }
        }

        // some files do not end with an empty line...
        if (sent.size() > 0) {
            List<String[]> rev = reverseSentence(sent);
            for (String[] toks : rev) {
                for (int i = 0; i < toks.length; i++) {
                    if (i > 0) {
                        os.print("\t");
                    }
                    os.print(toks[i]);
                }
                os.println();
            }
        }

        is.close();
        os.close();
    }

    static List<String[]> reverseSentence(List<String[]> sent) {
        List<String[]> rev = new ArrayList<String[]>();
        int len = sent.size();
        for (int i = sent.size() - 1; i >= 0; i--) {
            String[] toks = sent.get(i);
            toks[INDEX_POS] = Integer.toString(len + 1 - Integer.parseInt(toks[INDEX_POS]));
            if (toks.length > HEAD_POS && !toks[HEAD_POS].equals("_") && !toks[HEAD_POS].equals("-")) {
                int oldHead = Integer.parseInt(toks[HEAD_POS]);
                if (oldHead != 0) {
                    toks[HEAD_POS] = Integer.toString(len + 1 - oldHead);
                }
            }
            rev.add(toks);
        }
        return rev;
    }
}