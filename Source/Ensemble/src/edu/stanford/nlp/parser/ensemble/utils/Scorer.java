package edu.stanford.nlp.parser.ensemble.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Scorer {

    static final int HEAD_POS = 6;
    static final int LABEL_POS = 7;

    public static class Score {

        public double las;
        public double uas;
        public int lcorrect;
        public int ucorrect;
        public int total;
    }

    public static void main(String[] args) throws Exception {
        String goldFile = args[0];
        String sysFile = args[1];

        Score s = evaluate(goldFile, sysFile);

        System.err.printf("LAS: %.2f %d/%d\n", s.las, s.lcorrect, s.total);
        System.err.printf("UAS: %.2f %d/%d\n", s.uas, s.ucorrect, s.total);
    }

    public static Score evaluate(String goldFile, String sysFile) throws IOException {
        int total = 0;
        int ucorrect = 0;
        int lcorrect = 0;

        BufferedReader gs = new BufferedReader(new FileReader(goldFile));
        BufferedReader ss = new BufferedReader(new FileReader(sysFile));
        String gl, sl;
        int lineCount = 0;
        while ((gl = gs.readLine()) != null) {
            lineCount++;
            gl = gl.trim();
            sl = ss.readLine();
            sl = sl.trim();

            if (gl.length() == 0 && sl.length() != 0) {
                throw new RuntimeException("GOLD sentence ended before SYS at line " + lineCount);
            }
            if (gl.length() != 0 && sl.length() == 0) {
                throw new RuntimeException("SYS sentence ended before GOLD at line " + lineCount);
            }

            if (gl.length() == 0) {
                continue; // EOS
            }
            String[] gtoks = gl.split("[\t]+");
            String[] stoks = sl.split("[\t]+");

            if (gtoks.length <= Math.min(HEAD_POS, LABEL_POS)) {
                gs.close();
                ss.close();
                return null;
            }

            int ghead = Integer.parseInt(gtoks[HEAD_POS]);
            String glabel = Token.normLabel(gtoks[LABEL_POS]);
            int shead = Integer.parseInt(stoks[HEAD_POS]);
            String slabel = Token.normLabel(stoks[LABEL_POS]);

            total++;
            if (ghead == shead) {
                ucorrect++;
                if (glabel.equalsIgnoreCase(slabel)) {
                    lcorrect++;
                }
            }
        }
        gs.close();
        ss.close();

        double las = 100.0 * (double) lcorrect / (double) total;
        double uas = 100.0 * (double) ucorrect / (double) total;

        Score s = new Score();
        s.las = las;
        s.uas = uas;
        s.total = total;
        s.ucorrect = ucorrect;
        s.lcorrect = lcorrect;

        return s;
    }
}