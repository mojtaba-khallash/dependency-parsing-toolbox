package clear.experiment;

import clear.dep.DepProj;
import clear.dep.DepTree;
import clear.reader.CoNLLXReader;
import clear.util.IOUtil;
import java.io.PrintStream;

public class DepProjectize {

    public DepProjectize(String[] args) {
        /*
         * DepReader reader = new DepReader(args[0]); DepProj proj = new
         * DepProj(); DepTree tree;	int total;
         *
         * for (total=0; (tree = reader.nextTree()) != null; total++)
         * proj.detectNonProjective(tree);
         *
         * System.out.println(proj.count+" / "+total+" =
         * "+(double)proj.count/total*100);
		reader.close();
         */

        CoNLLXReader reader = new CoNLLXReader(args[0], true);
        DepProj proj = new DepProj();
        DepTree tree;
        int total;
        PrintStream fout = IOUtil.createPrintFileStream(args[1]);

        for (total = 0; (tree = reader.nextTree()) != null; total++) {
            proj.detectNonProjective(tree);
            fout.println(tree + "\n");
        }

        System.out.println(proj.count + " / " + total + " = " + (double) proj.count / total * 100);
        reader.close();
    }

    static public void main(String[] args) {
        new DepProjectize(args);
    }
}