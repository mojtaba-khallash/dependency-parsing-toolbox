package clear.experiment;

import clear.morph.MorphEnAnalyzer;
import clear.util.IOUtil;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Tab2Dep {

    static public void main(String[] args) {
        String inputFile = args[0];
        String outputFile = args[1];
        String dictDir = args[2];

        Scanner scan = IOUtil.createFileScanner(inputFile);
        PrintStream fout = IOUtil.createPrintFileStream(outputFile);
        MorphEnAnalyzer morph = new MorphEnAnalyzer(dictDir);

        while (scan.hasNextLine()) {
            StringTokenizer tok = new StringTokenizer(scan.nextLine());

            if (!tok.hasMoreTokens()) {
                fout.println();
            } else {
                String id = tok.nextToken();
                String form = tok.nextToken();
                String pos = tok.nextToken();
                String lemma = morph.getLemma(form, pos);
                String[] dep = tok.nextToken().split("/");

                fout.println(id + "\t" + form + "\t" + lemma + "\t" + pos + "\t" + dep[0] + "\t" + dep[1]);
            }
        }
    }
}