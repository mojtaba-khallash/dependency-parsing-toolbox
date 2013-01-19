import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.SRLReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Tmp {

    HashSet<String> set;

    public Tmp(String inputFile) {
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;

        set = new HashSet<>();

        while ((tree = reader.nextTree()) != null) {
            tree.setHasHeads();
            mod(tree);
        }

        ArrayList<String> keys = new ArrayList<>(set);
        Collections.sort(keys);

        for (String key : keys) {
            System.out.println(key);
        }
    }

    void rb(DepTree tree) {
        DepNode curr, next;
        int count;

        for (int i = 1; i < tree.size(); i++) {
            curr = tree.get(i);
            if (!curr.isPos("IN")) {
                continue;
            }
            count = 0;

            for (DepNode node : tree.getRightDependents(curr.id)) {
                //	count++;
                if (!node.isPosx("RB.*|IN") && !node.isDeprel("P") && !node.isDeprel("COORD")) {
                    count++;
                }
            }

            if (count > 1) {
                System.out.println(tree.getRightDependencies(curr.id));
                System.out.println(curr.id + " " + tree + "\n");
                try {
                    System.in.read();
                } catch (IOException e) {
                }
            }
        }
    }

    final void mod(DepTree tree) {
        DepNode curr;

        for (int i = 1; i < tree.size(); i++) {
            curr = tree.get(i);

            if (curr.isPos("MD") && curr.srlInfo.labelMatches("AM-MOD")) {
                set.add(curr.lemma);
            }
        }
    }

    void neg(DepTree tree) {
        DepNode prev, curr;

        for (int i = 2; i < tree.size(); i++) {
            prev = tree.get(i - 1);
            curr = tree.get(i);

            if (curr.lemma.equals("not") && prev.isPosx("VB.*")) {
                if (!curr.isDeprel("ADV") || curr.headId != prev.id) {
                    System.out.println(curr.id + " " + tree + "\n");
                }
            }
        }
    }

    static public void main(String[] args) {
        Tmp tmp = new Tmp(args[0]);
    }
}