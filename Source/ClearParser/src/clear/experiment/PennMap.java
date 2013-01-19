package clear.experiment;

import clear.treebank.TBReader;
import clear.treebank.TBTree;
import clear.util.IOUtil;
import clear.util.tuple.JIntIntTuple;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class PennMap {

    PrintStream fout;

    public PennMap(String treeDir, String mapFile) {
        String[] filenames = new File(treeDir).list();
        Arrays.sort(filenames);
        fout = IOUtil.createPrintFileStream(mapFile);

        for (String filename : filenames) {
            if (filename.endsWith(".mrg")) {
                printMap(treeDir, filename);
            }
        }

        fout.close();
    }

    void printMap(String treeDir, String filename) {
        TBReader reader = new TBReader(treeDir + File.separator + filename);
        String key = filename + "_";
        JIntIntTuple tup;
        TBTree tree;

        for (int treeId = 0; (tree = reader.nextTree()) != null; treeId++) {
            StringBuilder build = new StringBuilder();

            build.append(key);
            build.append(treeId);
            build.append("\t");
            build.append(tree.formsWithoutSpace());
            build.append("\t");

            ArrayList<JIntIntTuple> map = tree.getCharIdToTerminalIdMap();
            tup = map.get(0);

            build.append(tup.int1);
            build.append(":");
            build.append(tup.int2);

            for (int j = 1; j < map.size(); j++) {
                tup = map.get(j);
                build.append(" ");
                build.append(tup.int1);
                build.append(":");
                build.append(tup.int2);
            }

            fout.println(build.toString());
        }
    }

    static public void main(String[] args) {
        String treeDir = args[0];
        String mapFile = args[1];

        new PennMap(treeDir, mapFile);
    }
}