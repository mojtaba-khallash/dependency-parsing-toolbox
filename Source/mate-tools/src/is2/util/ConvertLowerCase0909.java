package is2.util;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.io.CONLLWriter09;

/**
 * @author Dr. Bernd Bohnet, 01.03.2010
 *
 *
 */
public class ConvertLowerCase0909 {

    public static void main(String args[]) throws Exception {

        CONLLReader09 reader = new CONLLReader09(args[0]);
        CONLLWriter09 writer = new CONLLWriter09(args[1]);

        int str = 0;
        while (true) {
            SentenceData09 i = reader.getNext();
            str++;
            if (i == null) {
                break;
            }

            SentenceData09 i09 = new SentenceData09(i);
            i09.createSemantic(i);

            for (int k = 0; k < i09.length(); k++) {
                i09.lemmas[k] = i09.lemmas[k].toLowerCase();
                i09.plemmas[k] = i09.plemmas[k].toLowerCase();

            }
            writer.write(i09);
        }
        writer.finishWriting();
    }

    public static void convert(String source, String target) throws Exception {

        CONLLReader09 reader = new CONLLReader09(source);
        CONLLWriter09 writer = new CONLLWriter09(target);

        int str = 0;
        while (true) {
            SentenceData09 i = reader.getNext();
            str++;
            if (i == null) {
                break;
            }

            SentenceData09 i09 = new SentenceData09(i);
            i09.createSemantic(i);

            for (int k = 0; k < i09.length(); k++) {
                i09.lemmas[k] = i09.lemmas[k].toLowerCase();
                i09.plemmas[k] = i09.plemmas[k].toLowerCase();

            }

            //public SentenceData09(String[] forms, String[] lemmas, String[] olemmas,String[] gpos, String[] ppos, String[] labs, int[] heads, String[] fillpred) {
            //SentenceData09
            //	SentenceData09 i2 = new SentenceData09(i.forms, i.lemmas,i.org_lemmas,);

            writer.write(i09);
        }
        writer.finishWriting();
    }
}