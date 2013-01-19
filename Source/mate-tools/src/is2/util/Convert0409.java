package is2.util;

import is2.data.SentenceData09;
import is2.io.CONLLReader04;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter06;
import is2.io.CONLLWriter09;
import is2.parser.Parser;

/**
 * @author Dr. Bernd Bohnet, 01.03.2010
 *
 *
 */
public class Convert0409 {

    public static void main(String args[]) throws Exception {

        convert(args[0], args[1]);
    }

    public static void convert(String source, String target) throws Exception {

        CONLLReader04 reader = new CONLLReader04(source);
        CONLLWriter09 writer = new CONLLWriter09(target);

        int str = 0;
        while (true) {
            SentenceData09 i = reader.getNext();
            str++;
            if (i == null) {
                break;
            }

            String[] formsNoRoot = new String[i.length() - 1];
            String[] posNoRoot = new String[formsNoRoot.length];
            String[] lemmas = new String[formsNoRoot.length];

            String[] org_lemmas = new String[formsNoRoot.length];

            String[] of = new String[formsNoRoot.length];
            String[] pf = new String[formsNoRoot.length];

            String[] pposs = new String[formsNoRoot.length];
            String[] labels = new String[formsNoRoot.length];
            String[] fillp = new String[formsNoRoot.length];

            int[] heads = new int[formsNoRoot.length];

            for (int j = 0; j < formsNoRoot.length; j++) {
                formsNoRoot[j] = i.forms[j + 1];
                if (formsNoRoot[j].length() == 0 || formsNoRoot[j].equals("")) {
                    Parser.out.println("error forms " + str);
                    //		System.exit(0);
                    formsNoRoot[j] = " ";
                }
                posNoRoot[j] = i.gpos[j + 1];
                if (posNoRoot[j].length() == 0 || posNoRoot[j].equals(" ")) {
                    Parser.out.println("error pos " + str);
                    //	System.exit(0);
                }
                pposs[j] = i.ppos[j + 1];
                if (pposs[j].length() == 0 || pposs[j].equals(" ")) {
                    Parser.out.println("error pos " + str);
                    //System.exit(0);
                }

                labels[j] = i.labels[j + 1];
                if (labels[j].length() == 0 || labels[j].equals(" ")) {
                    Parser.out.println("error lab " + str);
                    //	System.exit(0);
                }
                heads[j] = i.heads[j + 1];
                if (heads[j] > posNoRoot.length) {
                    Parser.out.println("head out of range " + heads[j] + " " + heads.length + " " + str);
                    heads[j] = posNoRoot.length;
                }

                lemmas[j] = i.plemmas[j + 1];
                if (lemmas[j].length() == 0 || lemmas[j].equals(" ")) {
                    Parser.out.println("error lab " + str);
                    //	System.exit(0);
                }
                org_lemmas[j] = i.lemmas[j + 1];
                if (org_lemmas[j].length() == 0 || org_lemmas[j].equals(" ")) {
                    Parser.out.println("error lab " + str);
                    //	System.exit(0);
                }
                of[j] = i.ofeats[j + 1];
                pf[j] = i.pfeats[j + 1];
                if (str == 6099) {
                    //		Parser.out.println(formsNoRoot[j]+"\t"+posNoRoot[j]+"\t"+pposs[j]+"\t"+labels[j]+"\t"+heads[j]);
                }

                // (instance.fillp!=null) fillp[j] = instance.fillp[j+1];
            }

            SentenceData09 i09 = new SentenceData09(formsNoRoot, lemmas, org_lemmas, pposs, pposs, labels, heads, fillp, of, pf);

            //public SentenceData09(String[] forms, String[] lemmas, String[] olemmas,String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
            //SentenceData09
            //	SentenceData09 i2 = new SentenceData09(i.forms, i.lemmas,i.org_lemmas,);

            writer.write(i09);
        }
        writer.finishWriting();
    }

    public static void convert0906(String source, String target) throws Exception {

        CONLLReader09 reader = new CONLLReader09(source);
        CONLLWriter06 writer = new CONLLWriter06(target);


        while (true) {
            SentenceData09 i = reader.getNext();

            if (i == null) {
                break;
            }


            String[] formsNoRoot = new String[i.length() - 1];
            String[] posNoRoot = new String[formsNoRoot.length];
            String[] lemmas = new String[formsNoRoot.length];

            String[] org_lemmas = new String[formsNoRoot.length];

            String[] of = new String[formsNoRoot.length];
            String[] pf = new String[formsNoRoot.length];

            String[] pposs = new String[formsNoRoot.length];
            String[] labels = new String[formsNoRoot.length];
            String[] fillp = new String[formsNoRoot.length];

            int[] heads = new int[formsNoRoot.length];

            for (int j = 0; j < formsNoRoot.length; j++) {
                formsNoRoot[j] = i.forms[j + 1];
                posNoRoot[j] = i.gpos[j + 1];
                pposs[j] = i.ppos[j + 1];

                labels[j] = i.labels[j + 1];
                heads[j] = i.heads[j + 1];
                lemmas[j] = i.plemmas[j + 1];

                org_lemmas[j] = i.lemmas[j + 1];
                of[j] = i.ofeats[j + 1];
                pf[j] = i.pfeats[j + 1];

                // (instance.fillp!=null) fillp[j] = instance.fillp[j+1];
            }

            SentenceData09 i09 = new SentenceData09(formsNoRoot, lemmas, org_lemmas, posNoRoot, pposs, labels, heads, fillp, of, pf);

            //public SentenceData09(String[] forms, String[] lemmas, String[] olemmas,String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
            //SentenceData09
            //	SentenceData09 i2 = new SentenceData09(i.forms, i.lemmas,i.org_lemmas,);

            writer.write(i09);
        }
        writer.finishWriting();
    }
}