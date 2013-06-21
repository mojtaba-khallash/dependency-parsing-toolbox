package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.SRLReader;
import clear.util.JMap;
import clear.util.tuple.JObjectIntTuple;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;

public class ExtractDeps {

    public ExtractDeps(String inputFile) {
        extractRBs(inputFile);

        /*
         * SRLReader reader = new SRLReader(inputFile, true); DepTree tree;
         *
         * ObjectIntOpenHashMap<String> map = new
         * ObjectIntOpenHashMap<String>();
         *
         * while ((tree = reader.nextTree()) != null) { extractRBs(tree, map); }
         *
         * ArrayList<JObjectIntTuple<String>> list =
         * JMap.<String>getSortedTuples(map);
         *
         * for (JObjectIntTuple<String> tup : list)
			System.out.println(tup.object+"\t"+tup.integer);
         */
    }

    public void extractRBs(String inputFile) {
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;
        ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<>();

        while ((tree = reader.nextTree()) != null) {
            extractCONJPs(tree, map);
        }

        ArrayList<JObjectIntTuple<String>> list = JMap.<String>getSortedTuples(map);

        for (JObjectIntTuple<String> tup : list) {
            System.out.println(tup.object + "\t" + tup.integer);
        }
    }

    void extractCONJPs(DepTree tree, ObjectIntOpenHashMap<String> map) {
        DepNode curr, head;
        String pos;
        int i, j, size = tree.size();

        for (i = 1; i < size; i++) {
            curr = tree.get(i);
            head = tree.get(curr.headId);

            if (curr.isPredicate() && head.isPos("TO") && curr.isDeprel("IM")) {
                for (j = head.id + 1; j < curr.id; j++) {
                    pos = tree.get(j).pos;
                    map.put(pos, map.get(pos) + 1);

                    if (pos.equals("DT")) {
                        System.out.println(curr.id + " " + head.id + " " + tree);
                        try {
                            System.in.read();
                        } catch (IOException e) {
                        }
                    }
                }
            }

        }

        //	try {System.in.read();} catch (IOException e) {}
    }

    public static void main(String[] args) {
        ExtractDeps extractDeps = new ExtractDeps(args[0]);
    }
}