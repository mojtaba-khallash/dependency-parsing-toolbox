package is2.util;

import is2.data.SentenceData09;
import is2.io.*;
import is2.parser.Parser;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author Dr. Bernd Bohnet, 01.03.2010
 *
 *
 */
public class Convert {

    public static void main(String args[]) throws Exception {


        if (args.length < 2) {

            Parser.out.println("Usage");
            Parser.out.println(" java is2.util.Convert <in> <out> [-w06|-w0809|-yue]  [-wordsonly]");
        }

        int todo = 9;
        boolean wordsOnly = false;
        for (String a : args) {
            if (a != null && a.equals("-w06")) {
                todo = 6;
            } else if (a != null && a.equals("-w0809")) {
                todo = 89;
            } else if (a != null && a.equals("-yue")) {
                todo = 99;
            } else if (a != null && a.equals("-utf8")) {
                todo = 8;
            }

            if (a != null && a.equals("-wordsonly")) {
                wordsOnly = true;
            }
        }

        if (todo == 9) {
            convert(args[0], args[1]);
        } else if (todo == 6) {
            convert0906(args[0], args[1]);
        } else if (todo == 8) {
            convert8(args[0], args[1], args[2]);
        } else if (todo == 89) {
            convert0809(args[0], args[1]);
        } else if (todo == 99) {
            convertChnYue(args[0], args[1], wordsOnly);
        }
    }

    private static void convert8(String infile, String outfile, String format) {
        try {

            Parser.out.println("availableCharsets: " + Charset.availableCharsets());

            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(infile), format));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));

            int ch;

            int count = 0, wcount = 0;
            while ((ch = in.read()) > -1) {
                count++;

                if (Character.isDefined(ch)) {

                    out.write(ch);
                    wcount++;
                }
            }
            in.close();
            out.close();
            Parser.out.println("read " + count + " chars and wrote " + wcount + " utf8 chars");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convert(String source, String target) throws Exception {

        CONLLReader06 reader = new CONLLReader06(source);
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

            SentenceData09 i09 = new SentenceData09(formsNoRoot, formsNoRoot, formsNoRoot, pposs, pposs, labels, heads, fillp, of, pf);

            //public SentenceData09(String[] forms, String[] lemmas, String[] olemmas,String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
            //SentenceData09
            //	SentenceData09 i2 = new SentenceData09(i.forms, i.lemmas,i.org_lemmas,);

            writer.write(i09);
        }
        writer.finishWriting();
    }

    public static void convertChnYue(String source, String target, boolean wordsOnly) throws Exception {


        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"), 32768);

        CONLLWriter09 writer = new CONLLWriter09(target);

        int str = 0;
        while (true) {

            ArrayList<String[]> lines = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.length() < 2) {
                    break;
                }
                String split[] = line.split("\t");
                lines.add(split);
            }
            if (line == null) {
                break;
            }

            str++;


            String[] formsNoRoot = new String[lines.size()];
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
                formsNoRoot[j] = lines.get(j)[0];
                if (formsNoRoot[j].length() == 0 || formsNoRoot[j].equals("")) {
                    Parser.out.println("error forms " + str);
                    //		System.exit(0);
                    formsNoRoot[j] = "_";
                }

                posNoRoot[j] = lines.get(j)[1];
                if (posNoRoot[j].length() == 0 || posNoRoot[j].equals(" ")) {
                    Parser.out.println("error pos " + str);
                    //	System.exit(0);
                }
                pposs[j] = "_";

                labels[j] = lines.get(j)[3];
                if (labels[j].length() == 0 || labels[j].equals(" ")) {
                    Parser.out.println("error lab " + str);
                    labels[j] = "_";
                    //	System.exit(0);
                }
                heads[j] = Integer.parseInt(lines.get(j)[2]) + 1;
                if (heads[j] > posNoRoot.length) {
                    Parser.out.println("head out of range " + heads[j] + " " + heads.length + " " + str);
                    heads[j] = posNoRoot.length;
                }

                // 0 is root and not -1  
                if (heads[j] == -1) {
                    heads[j] = 0;
                }

                lemmas[j] = "_";

                org_lemmas[j] = "_";

                of[j] = "_";
                pf[j] = "_";

                if (wordsOnly) {
                    posNoRoot[j] = "_";
                    heads[j] = 0;
                    labels[j] = "_";
                }

                // (instance.fillp!=null) fillp[j] = instance.fillp[j+1];
            }

            SentenceData09 i09 = new SentenceData09(formsNoRoot, lemmas, org_lemmas, posNoRoot, posNoRoot, labels, heads, fillp, of, pf);

            //public SentenceData09(String[] forms, String[] lemmas, String[] olemmas,String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
            //SentenceData09
            //	SentenceData09 i2 = new SentenceData09(i.forms, i.lemmas,i.org_lemmas,);

            writer.write(i09);
        }
        writer.finishWriting();
    }

    /**
     * Convert the 0
     *
     * @param source
     * @param target
     * @throws Exception
     */
    public static void convert0809(String source, String target) throws Exception {

        CONLLReader08 reader = new CONLLReader08(source);
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
                //	if (org_lemmas[j].length()==0 ||org_lemmas[j].equals(" ")) {
                //		Parser.out.println("error lab "+str);
                //	//	System.exit(0);
                //	}
//				of[j] = i.ofeats[j+1];
//				pf[j] = i.pfeats[j+1];
                if (str == 6099) {
                    //		Parser.out.println(formsNoRoot[j]+"\t"+posNoRoot[j]+"\t"+pposs[j]+"\t"+labels[j]+"\t"+heads[j]);
                }

                // (instance.fillp!=null) fillp[j] = instance.fillp[j+1];
            }

            SentenceData09 i09 = new SentenceData09(formsNoRoot, org_lemmas, lemmas, pposs, pposs, labels, heads, fillp, of, pf);

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
                pposs[j] = i.gpos[j + 1];

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