package clear.experiment;

import clear.dep.DepTree;
import clear.reader.SRLReader;
import clear.util.IOUtil;
import java.io.File;
import java.io.PrintStream;

public class generateCV {

    static public void main(String[] args) {
        String inputFile = args[0];
        String outputDir = args[1];
        int n = Integer.parseInt(args[2]);
        int cv = Integer.parseInt(args[3]);

        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;

        PrintStream[] ftrn = new PrintStream[cv];
        PrintStream[] ftst = new PrintStream[cv];

        for (int i = 0; i < cv; i++) {
            ftrn[i] = IOUtil.createPrintFileStream(outputDir + File.separator + "cv" + i + ".trn");
            ftst[i] = IOUtil.createPrintFileStream(outputDir + File.separator + "cv" + i + ".tst");
        }

        int stop = (int) Math.ceil((float) n / cv);

        for (int treeId = 0; (tree = reader.nextTree()) != null; treeId++) {
            int idx = treeId / stop;

            ftst[idx].println(tree + "\n");

            for (int i = 0; i < cv; i++) {
                if (i != idx) {
                    ftrn[i].println(tree + "\n");
                }
            }

            if (treeId % 1000 == 0) {
                System.out.print("\r" + treeId);
            }
        }

        for (int i = 0; i < cv; i++) {
            ftrn[i].close();
            ftst[i].close();
        }
    }
}