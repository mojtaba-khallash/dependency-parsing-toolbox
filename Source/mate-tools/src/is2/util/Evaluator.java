package is2.util;

import is2.data.Parse;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.parser.Parser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;

public class Evaluator {

    public static void main(String[] args) {

        Options options = new Options(args);

        if (options.eval && options.significant1 == null) {

            Results r = evaluate(options.goldfile, options.outfile);

        } else if (options.significant1 != null && options.significant2 != null) {

            Parser.out.println("compare1 " + options.significant1);
            Parser.out.println("compare2 " + options.significant2);
            Parser.out.println("gold     " + options.goldfile);

            Results r1 = evaluate(options.goldfile, options.significant1, false);

            Parser.out.println("file 1 done ");

            Results r2 = evaluate(options.goldfile, options.significant2, false);

            double[] s1 = new double[r1.correctHead.size()];
            double[] s2 = new double[r1.correctHead.size()];

            for (int k = 0; k < r1.correctHead.size(); k++) {
                s1[k] = r1.correctHead.get(k);
                s2[k] = r2.correctHead.get(k);
            }

            try {
                double p = TestUtils.pairedTTest(s1, s2);
                Parser.out.print("significant to " + p);
            } catch (IllegalArgumentException | MathException e) {
                e.printStackTrace();
            }

//			significant(options.significant1, options.significant2) ;
        } else if (options.significant1 != null) {
            Results r = evaluate(options.goldfile, options.outfile, true);
//			significant(options.significant1, options.significant2) ;
        }
    }

    /**
     *
     * @param act_file
     * @param pred_file
     * @param what top, pos, length, mor
     */
    public static void evaluateTagger(String act_file, String pred_file, String what) {


        CONLLReader09 goldReader = new CONLLReader09(act_file);

        CONLLReader09 predictedReader = new CONLLReader09();
        predictedReader.startReading(pred_file);

        HashMap<String, Integer> errors = new HashMap<>();
        HashMap<String, StringBuffer> words = new HashMap<>();

        int total = 0, numsent = 0, corrT = 0;
        SentenceData09 goldInstance = goldReader.getNext();
        SentenceData09 predInstance = predictedReader.getNext();


        HashMap<Integer, int[]> correctL = new HashMap<>();
        HashMap<String, int[]> pos = new HashMap<>();
        HashMap<String, int[]> mor = new HashMap<>();

        float correctM = 0, allM = 0;

        while (goldInstance != null) {

            int instanceLength = goldInstance.length();

            if (instanceLength != predInstance.length()) {
                Parser.out.println("Lengths do not match on sentence " + numsent);
            }

            String gold[] = goldInstance.gpos;
            String pred[] = predInstance.ppos;

            String goldM[] = goldInstance.ofeats;
            String predM[] = predInstance.pfeats;


            // NOTE: the first item is the root info added during nextInstance(), so we skip it.

            for (int i = 1; i < instanceLength; i++) {

                int[] cwr = correctL.get(i);
                if (cwr == null) {
                    cwr = new int[2];
                    correctL.put(i, cwr);
                }
                cwr[1]++;
                int[] correctPos = pos.get(gold[i]);
                if (correctPos == null) {
                    correctPos = new int[2];
                    pos.put(gold[i], correctPos);
                }
                correctPos[1]++;

                int[] correctMor = mor.get(goldM[i]);
                if (correctMor == null) {
                    correctMor = new int[2];
                    mor.put(goldM[i], correctMor);
                }

                if ((goldM[i].equals("_") && predM[i] == null) || goldM[i].equals(predM[i])) {
                    correctM++;
                    correctMor[0]++;
                }
                allM++;
                correctMor[1]++;

                if (gold[i].equals(pred[i])) {
                    corrT++;
                    cwr[0]++;
                    correctPos[0]++;
                } else {
                    String key = "gold: '" + gold[i] + "' pred: '" + pred[i] + "'";
                    Integer cnt = errors.get(key);
                    StringBuffer errWrd = words.get(key);
                    if (cnt == null) {
                        errors.put(key, 1);
                        words.put(key, new StringBuffer().append(goldInstance.forms[i]));
                    } else {
                        errors.put(key, cnt + 1);
                        errWrd.append(" ").append(goldInstance.forms[i]);
                    }
                }
            }
            total += instanceLength - 1; // Subtract one to not score fake root token


            numsent++;

            goldInstance = goldReader.getNext();
            predInstance = predictedReader.getNext();
        }

        //	Parser.out.println("error gold:"+goldPos[i]+" pred:"+predPos[i]+" "+goldInstance.forms[i]+" snt "+numsent+" i:"+i);
        ArrayList<Entry<String, Integer>> opsl = new ArrayList<>();
        for (Entry<String, Integer> e : errors.entrySet()) {
            opsl.add(e);
        }

        Collections.sort(opsl, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {

                return o1.getValue() == o2.getValue() ? 0 : o1.getValue() > o2.getValue() ? -1 : 1;
            }
        });


        int cnt = 0;
        if (what.contains("top")) {
            Parser.out.println("top most errors:");
            for (Entry<String, Integer> e : opsl) {
                cnt++;
                if (e.getValue() > 10) {
                    Parser.out.println(e.getKey() + "  " + e.getValue() + " context: " + words.get(e.getKey()));
                }
            }
        }

        if (what.contains("length")) {
            for (int k = 0; k < 60; k++) {
                int[] cwr = correctL.get(k);
                if (cwr == null) {
                    continue;
                }
                Parser.out.print(k + ":" + cwr[0] + ":" + cwr[1] + ":" + (((float) Math.round(10000 * (float) ((float) cwr[0]) / (float) cwr[1])) / 100) + " ");
            }
            Parser.out.println();
        }

        if (what.contains("pos")) {
            for (Entry<String, int[]> e : pos.entrySet()) {

                Parser.out.print(e.getKey() + ":" + e.getValue()[0] + ":" + e.getValue()[1] + ":"
                        + (((float) Math.round(10000 * ((float) e.getValue()[0]) / ((float) e.getValue()[1]))) / 100) + " ");

            }
            Parser.out.print("");
        }
        Parser.out.println();
        if (what.contains("mor")) {
            for (Entry<String, int[]> e : mor.entrySet()) {

                Parser.out.print(e.getKey() + ":" + e.getValue()[0] + ":" + e.getValue()[1] + ":"
                        + (((float) Math.round(10000 * ((float) e.getValue()[0]) / ((float) e.getValue()[1]))) / 100) + " ");

            }
            Parser.out.print("");
        }
        Parser.out.println("\nTokens: " + total + " Correct: " + corrT + " " + (float) corrT / total + " Correct M.:" + (int) correctM + " morphology " + (correctM / total));
    }

    public static int errors(SentenceData09 s, boolean uas) {

        int errors = 0;
        for (int k = 1; k < s.length(); k++) {

            if (s.heads[k] != s.pheads[k] && (uas || !s.labels[k].equals(s.plabels[k]))) {
                errors++;
            }
        }
        return errors;
    }

    public static int errors(SentenceData09 s1, SentenceData09 s2, HashMap<String, Integer> r1, HashMap<String, Integer> r2) {

        int errors = 0;
        for (int k = 1; k < s1.length(); k++) {

            if (s1.heads[k] != s1.pheads[k] || (!s1.labels[k].equals(s1.plabels[k]))) {

                if (s2.heads[k] != s2.pheads[k] || (!s2.labels[k].equals(s2.plabels[k]))) {
                    // equal do nothing
                } else {

                    Integer cnt = r1.get(s1.labels[k]);
                    if (cnt == null) {
                        cnt = 0;
                    }
                    cnt++;
                    r1.put(s1.labels[k], cnt);
                }
            }

            if (s2.heads[k] != s2.pheads[k] || (!s2.labels[k].equals(s2.plabels[k]))) {

                if (s1.heads[k] != s1.pheads[k] || (!s1.labels[k].equals(s1.plabels[k]))) {
                    // equal do nothing
                } else {

                    Integer cnt = r2.get(s2.labels[k]);
                    if (cnt == null) {
                        cnt = 0;
                    }
                    cnt++;
                    r2.put(s2.labels[k], cnt);
                }
            }
        }
        return errors;
    }
    public static final String PUNCT = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    public static class Results {

        public int total;
        public int corr;
        public float las;
        public float ula;
        public float lpas;
        public float upla;
        ArrayList<Double> correctHead;
    }

    public static Results evaluate(String act_file, String pred_file) {
        return evaluate(act_file, pred_file, true);
    }

    public static Results evaluate(String act_file, String pred_file, boolean printEval) {
        return evaluate(act_file, pred_file, printEval, false);
    }

    public static Results evaluate(String act_file, String pred_file, boolean printEval, boolean sig) {

        CONLLReader09 goldReader = new CONLLReader09(act_file, -1);
        CONLLReader09 predictedReader = new CONLLReader09(pred_file, -1);

        int total = 0, corr = 0, corrL = 0, Ptotal = 0, Pcorr = 0, PcorrL = 0, BPtotal = 0, BPcorr = 0, BPcorrL = 0, corrLableAndPos = 0, corrHeadAndPos = 0;
        int corrLableAndPosP = 0, corrHeadAndPosP = 0, corrLableAndPosC = 0;
        int numsent = 0, corrsent = 0, corrsentL = 0, Pcorrsent = 0, PcorrsentL = 0, sameProj = 0;
        int proj = 0, nonproj = 0, pproj = 0, pnonproj = 0, nonProjOk = 0, nonProjWrong = 0;

        int corrOne = 0;

        int correctChnWoPunc = 0, correctLChnWoPunc = 0, CPtotal = 0;
        SentenceData09 goldInstance = goldReader.getNext();

        SentenceData09 predInstance = predictedReader.getNext();
        HashMap<String, Integer> label = new HashMap<>();
        HashMap<String, Integer> labelCount = new HashMap<>();
        HashMap<String, Integer> labelCorrect = new HashMap<>();
        HashMap<String, Integer> falsePositive = new HashMap<>();

        // does the node have the correct head?
        ArrayList<Double> correctHead = new ArrayList<>();

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

            boolean Pwhole = true;
            boolean PwholeL = true;


            int tlasS = 0, totalS = 0, corrLabels = 0, XLabels = 0;

            // NOTE: the first item is the root info added during nextInstance(), so we skip it.

            int punc = 0, bpunc = 0, totalChnWoPunc = 0;
            for (int i = 1; i < instanceLength; i++) {



                Parse p = new Parse(predHeads.length);
                for (int k = 0; k < p.heads.length; k++) {
                    p.heads[k] = (short) predHeads[k];
                }

                Parse g = new Parse(predHeads.length);
                for (int k = 0; k < g.heads.length; k++) {
                    g.heads[k] = (short) goldHeads[k];
                }

                {
                    Integer count = labelCount.get(goldLabels[i]);
                    if (count == null) {
                        count = 0;
                    }

                    count++;

                    labelCount.put(goldLabels[i], count);

                    if (goldLabels[i].equals(predLabels[i])) {
                        Integer correct = labelCorrect.get(goldLabels[i]);
                        if (correct == null) {
                            correct = 0;
                        }
                        correct++;
                        labelCorrect.put(goldLabels[i], correct);

                    } else {
                        Integer fp = falsePositive.get(predLabels[i]);
                        if (fp == null) {
                            fp = 0;
                        }
                        fp++;
                        falsePositive.put(predLabels[i], fp);
                    }
                }

                if (goldLabels[i].startsWith("PMOD")) {
                    XLabels++;
                }

                boolean tlas = false;
                if (predHeads[i] == goldHeads[i]) {
                    corr++;

                    if (goldInstance.gpos[i].equals(predInstance.ppos[i])) {
                        corrHeadAndPos++;
                    }
                    if (goldLabels[i].equals(predLabels[i])) {
                        corrL++;
                        //	if (predLabels[i].startsWith("PMOD")) 
                        corrLabels++;
                        //	else correctHead.add(0);
                        if (goldInstance.gpos[i].equals(predInstance.ppos[i])) {
                            tlasS++;
                            tlas = true;
                            corrLableAndPos++;
                        }
                    } else {
                        //	correctHead.add(0);
                        //		Parser.out.println(numsent+" error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                        wholeL = false;
                    }
                } else {

                    //correctHead.add(0);

                    //		Parser.out.println(numsent+"error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                    whole = false;
                    wholeL = false;

                    Integer count = label.get(goldLabels[i]);

                    if (count == null) {
                        count = 0;
                    }
                    count++;
                    label.put(goldLabels[i], count);



                    int d = Math.abs(goldInstance.heads[i] - i);
                }


                if (!("!\"#$%&''()*+,-./:;<=>?@[\\]^_{|}~``".contains(goldInstance.forms[i]))) {

                    if (predHeads[i] == goldHeads[i]) {
                        BPcorr++;

                        if (goldLabels[i].equals(predLabels[i])) {
                            BPcorrL++;
                        } else {
                            //		Parser.out.println(numsent+" error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                            //	PwholeL = false;
                        }
                    } else {
                        //		Parser.out.println(numsent+"error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                        //Pwhole = false; wholeL = false; 
                    }

                } else {
                    bpunc++;
                }

                if (!(",.:''``".contains(goldInstance.forms[i]))) {


                    if (predHeads[i] == goldHeads[i]) {
                        if (goldInstance.gpos[i].equals(predInstance.ppos[i])) {
                            corrHeadAndPosP++;
                        }
                        Pcorr++;

                        if (goldLabels[i].equals(predLabels[i])) {
                            PcorrL++;
                            if (goldInstance.gpos[i].equals(predInstance.ppos[i])) {
                                corrLableAndPosP++;
                            }

                        } else {
                            //		Parser.out.println(numsent+" error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                            PwholeL = false;
                        }
                    } else {
                        //		Parser.out.println(numsent+"error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                        Pwhole = false;
                        PwholeL = false;
                    }

                } else {
                    punc++;
                }


                if (!(goldInstance.gpos[i].toLowerCase().startsWith("pu"))) {
                    if (predHeads[i] == goldHeads[i]) {
                        correctChnWoPunc++;

                        if (goldLabels[i].equals(predLabels[i])) {
                            correctLChnWoPunc++;
                            if (goldInstance.gpos[i].equals(predInstance.ppos[i])) {
                                corrLableAndPosC++;
                            }
                        } else {
                            //		Parser.out.println(numsent+" error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                            //	PwholeL = false;
                        }
                    } else {
                        //		Parser.out.println(numsent+"error gold "+goldLabels[i]+" "+predLabels[i]+" head "+goldHeads[i]+" child "+i);
                        //	Pwhole = false; PwholeL = false; 
                    }

                } else {
                    totalChnWoPunc++;
                }


                if (sig) {
                    if (tlas) {
                        Parser.out.println("1\t");
                    } else {
                        Parser.out.println("0\t");
                    }
                }
            }
            total += ((instanceLength - 1)); // Subtract one to not score fake root token

            Ptotal += ((instanceLength - 1) - punc);
            BPtotal += ((instanceLength - 1) - bpunc);
            CPtotal += ((instanceLength - 1) - totalChnWoPunc);
            if (whole) {
                corrsent++;
            }
            if (wholeL) {
                corrsentL++;
            }
            if (Pwhole) {
                Pcorrsent++;
            }
            if (PwholeL) {
                PcorrsentL++;
            }
            numsent++;

            goldInstance = goldReader.getNext();
            predInstance = predictedReader.getNext();
            correctHead.add((double) ((double) corrLabels / (instanceLength - 1)));
            //	Parser.out.println(""+((double)corrLabels/(instanceLength - 1)));
        }

        Results r = new Results();

        r.correctHead = correctHead;
        int mult = 100000, diff = 1000;

        r.total = total;
        r.corr = corr;
        r.las = (float) Math.round(((double) corrL / total) * mult) / diff;
        r.ula = (float) Math.round(((double) corr / total) * mult) / diff;
        r.lpas = (float) Math.round(((double) corrLableAndPos / total) * mult) / diff;
        r.upla = (float) Math.round(((double) corrHeadAndPos / total) * mult) / diff;
        float tlasp = (float) Math.round(((double) corrLableAndPosP / Ptotal) * mult) / diff;
        float tlasc = (float) Math.round(((double) corrLableAndPosC / Ptotal) * mult) / diff;

        //	Parser.out.print("Total: " + total+" \tCorrect: " + corr+" ");
        Parser.out.print(" LAS/Total/UAS/Total: " + r.las + "/" + (double) Math.round(((double) corrsentL / numsent) * mult) / diff
                + "/" + r.ula + "/" + (double) Math.round(((double) corrsent / numsent) * mult) / diff + " LPAS/UPAS " + r.lpas + "/" + r.upla);

        Parser.out.println("; without . " + (double) Math.round(((double) PcorrL / Ptotal) * mult) / diff + "/"
                + (double) Math.round(((double) PcorrsentL / numsent) * mult) / diff
                + "/" + (double) Math.round(((double) Pcorr / Ptotal) * mult) / diff + "/"
                + (double) Math.round(((double) Pcorrsent / numsent) * mult) / diff + " TLAS " + tlasp
                + " V2 LAS/UAS " + (double) Math.round(((double) BPcorrL / BPtotal) * mult) / diff
                + "/" + (double) Math.round(((double) BPcorr / BPtotal) * mult) / diff
                + " CHN LAS/UAS " + (double) Math.round(((double) correctLChnWoPunc / CPtotal) * mult) / diff
                + "/" + (double) Math.round(((double) correctChnWoPunc / CPtotal) * mult) / diff + " TLAS " + tlasc);

        float precisionNonProj = ((float) nonProjOk) / ((float) nonProjOk + nonProjWrong);
        float recallNonProj = ((float) nonProjOk) / ((float) (nonproj));
        Parser.out.println("proj " + proj + " nonp " + nonproj + "; predicted proj " + pproj + " non " + pnonproj + "; nonp correct "
                + nonProjOk + " nonp wrong " + nonProjWrong
                + " precision=(nonProjOk)/(non-projOk+nonProjWrong): " + precisionNonProj
                + " recall=nonProjOk/nonproj=" + recallNonProj + " F=" + (2 * precisionNonProj * recallNonProj) / (precisionNonProj + recallNonProj));

        if (!printEval) {
            return r;
        }


        HashMap<String, Integer> totalX = new HashMap<>();
        HashMap<String, Integer> totalY = new HashMap<>();

        String A = " "; // &
        Parser.out.println("label\ttp\tcount\trecall\t\ttp\tfp+tp\tprecision\t F-Score ");

        for (Entry<String, Integer> e : labelCount.entrySet()) {

            int tp = labelCorrect.get(e.getKey()) == null ? 0 : labelCorrect.get(e.getKey()).intValue();
            Integer count = labelCount.get(e.getKey());
            int fp = falsePositive.get(e.getKey()) == null ? 0 : falsePositive.get(e.getKey()).intValue();
            Parser.out.println(e.getKey() + "\t" + tp + "\t" + count + "\t" + roundPercent((float) tp / count) + "\t\t" + tp + "\t" + (fp + tp)
                    + "\t" + roundPercent((float) tp / (fp + tp)) + "\t\t" + roundPercent((((float) tp / count)) + (float) tp / (fp + tp)) / 2F); //+totalD
        }
        return r;
    }

    public static float round(double v) {

        return Math.round(v * 10000F) / 10000F;
    }

    public static float roundPercent(double v) {

        return Math.round(v * 10000F) / 100F;
    }
}