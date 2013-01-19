package is2.lemmatizer;

import is2.data.SentenceData09;
import is2.io.*;
import is2.parser.Parser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class Evaluator {

    public static void evaluate(String act_file, String pred_file, int formatTask) throws Exception {

        CONLLReader goldReader = null;
        CONLLReader predictedReader = null;
        if (formatTask == 4) {
            goldReader = new CONLLReader04(act_file, CONLLReader09.NO_NORMALIZE);
            predictedReader = new CONLLReader04(act_file, CONLLReader09.NO_NORMALIZE);
        }
        else if (formatTask == 6) {
            goldReader = new CONLLReader06(act_file, CONLLReader09.NO_NORMALIZE);
            predictedReader = new CONLLReader06(act_file, CONLLReader09.NO_NORMALIZE);
        }
        else if (formatTask == 8) {
            goldReader = new CONLLReader08(act_file, CONLLReader09.NO_NORMALIZE);
            predictedReader = new CONLLReader08(act_file, CONLLReader09.NO_NORMALIZE);
        }
        else if (formatTask == 9) {
            goldReader = new CONLLReader09(act_file, CONLLReader09.NO_NORMALIZE);
            predictedReader = new CONLLReader09(act_file, CONLLReader09.NO_NORMALIZE);
        }
        //	predictedReader.startReading(pred_file);


        HashMap<String, Integer> errors = new HashMap<>();


        int total = 0, corr = 0, corrL = 0, corrT = 0;
        int numsent = 0, corrsent = 0, corrsentL = 0;
        SentenceData09 goldInstance = goldReader.getNext();
        SentenceData09 predInstance = predictedReader.getNext();

        while (goldInstance != null) {

            int instanceLength = goldInstance.length();

            if (instanceLength != predInstance.length()) {
                Parser.out.println("Lengths do not match on sentence " + numsent);
            }


            String gold[] = goldInstance.lemmas;
            String pred[] = predInstance.plemmas;


            boolean whole = true;
            boolean wholeL = true;

            // NOTE: the first item is the root info added during nextInstance(), so we skip it.

            for (int i = 1; i < instanceLength; i++) {
                if (gold[i].toLowerCase().equals(pred[i].toLowerCase())) {
                    corrT++;
                }

                if (gold[i].equals(pred[i])) {
                    corrL++;
                } else {

                    //	Parser.out.println("error gold:"+goldPos[i]+" pred:"+predPos[i]+" "+goldInstance.forms[i]+" snt "+numsent+" i:"+i);
                    String key = "gold: '" + gold[i] + "' pred: '" + pred[i] + "'";
                    Integer cnt = errors.get(key);
                    if (cnt == null) {
                        errors.put(key, 1);
                    } else {
                        errors.put(key, cnt + 1);
                    }
                }

            }
            total += instanceLength - 1; // Subtract one to not score fake root token

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
        ArrayList<Entry<String, Integer>> opsl = new ArrayList<>();
        for (Entry<String, Integer> e : errors.entrySet()) {
            opsl.add(e);
        }

        Collections.sort(opsl, new Comparator<Entry<String, Integer>>() {

            @Override
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {

                return o1.getValue() == o2.getValue() ? 0 : o1.getValue() > o2.getValue() ? 1 : -1;
            }
        });

        for (Entry<String, Integer> e : opsl) {
            //	Parser.out.println(e.getKey()+"  "+e.getValue());
        }

        Parser.out.println("Tokens: " + total + " Correct: " + corrT + " " + (float) corrT / total + " correct uppercase " + (float) corrL / total);
    }

    public static void main(String[] args) throws Exception {
        int format = 9;
        if (args.length > 2) {
            try {
                format = Integer.parseInt(args[2]);
            }
            catch(Exception e) {}
        }

        evaluate(args[0], args[1], format);
    }
}