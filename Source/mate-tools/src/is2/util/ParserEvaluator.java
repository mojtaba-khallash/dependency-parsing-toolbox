package is2.util;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.parser.Parser;

public class ParserEvaluator {

    public static final String PUNCT = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    public static class Results {

        public int total;
        public int corr;
        public float las;
        public float ula;
    }

    public static Results evaluate(String act_file, String pred_file) throws Exception {

        CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
        CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);

        int total = 0, corr = 0, corrL = 0;
        int numsent = 0, corrsent = 0, corrsentL = 0;
        SentenceData09 goldInstance = goldReader.getNext();
        SentenceData09 predInstance = predictedReader.getNext();

        while (goldInstance != null) {

            int instanceLength = goldInstance.length();

            if (instanceLength != predInstance.length()) {
                Parser.out.println("Lengths do not match on sentence " + numsent);
            }

            int[] goldHeads = goldInstance.heads;
            String[] goldLabels = goldInstance.labels;
            int[] predHeads = predInstance.pheads;
            String[] predLabels = predInstance.plabels;

            boolean whole = true;
            boolean wholeL = true;

            // NOTE: the first item is the root info added during nextInstance(), so we skip it.

            int punc = 0;
            for (int i = 1; i < instanceLength; i++) {
                if (predHeads[i] == goldHeads[i]) {
                    corr++;

                    if (goldLabels[i].equals(predLabels[i])) {
                        corrL++;
                    } else {
                        //		Parser.out.println(numsent+" error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                        wholeL = false;
                    }
                } else {
                    //		Parser.out.println(numsent+"error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                    whole = false;
                    wholeL = false;
                }
            }
            total += ((instanceLength - 1) - punc); // Subtract one to not score fake root token

            if (whole) {
                corrsent++;
            }
            if (wholeL) {
                corrsentL++;
            }
            numsent++;

            goldInstance = goldReader.getNext();
            predInstance = predictedReader.getNext();
        }

        Results r = new Results();

        r.total = total;
        r.corr = corr;
        r.las = (float) Math.round(((double) corrL / total) * 100000) / 1000;
        r.ula = (float) Math.round(((double) corr / total) * 100000) / 1000;
        Parser.out.print("Total: " + total + " \tCorrect: " + corr + " ");
        Parser.out.println("LAS: " + (double) Math.round(((double) corrL / total) * 100000) / 1000 + " \tTotal: " + (double) Math.round(((double) corrsentL / numsent) * 100000) / 1000
                + " \tULA: " + (double) Math.round(((double) corr / total) * 100000) / 1000 + " \tTotal: " + (double) Math.round(((double) corrsent / numsent) * 100000) / 1000);

        return r;
    }

    public static float round(double v) {

        return Math.round(v * 10000F) / 10000F;
    }
}