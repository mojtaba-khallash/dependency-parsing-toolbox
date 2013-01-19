package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.reader.SRLReader;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.io.IOException;
import java.util.Arrays;

public class DepAnalyze {

    public DepAnalyze(String inputFile) {
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;

        while ((tree = reader.nextTree()) != null) {
            analyze(tree);
        }
    }

    public void analyze(DepTree tree) {
        DepNode pred, node, lm;
        IntOpenHashSet sVC;
        int predId = 0, lmId, i, j;

        tree.setSubcat();

        for (i = 0; (predId = tree.nextPredicateId(predId)) < tree.size(); i++) {
            pred = tree.get(predId);
            sVC = tree.getVCIdSet(predId);
            lm = pred.leftMostDep;

            if (lm != null) {
                lmId = (lm.id < pred.headId) ? lm.id : pred.headId;
            } else if (pred.headId < pred.id) {
                lmId = pred.headId;
            } else {
                continue;
            }

            lm = tree.get(lmId);

            outer:
            for (j = lm.id - 1; j > 0; j--) {
                node = tree.get(j);

                if (!node.isArgOf(predId)) {
                    continue;
                }
                if (sVC.contains(node.headId)) {
                    continue;
                }

                for (int id : sVC.toArray()) {
                    if (tree.get(id).headId == node.id) {
                        continue outer;
                    }
                }

                for (SRLHead head : node.srlInfo.heads) {
                    if (head.headId < predId) {
                        continue outer;
                    }
                }

                System.out.println(node.id + " " + predId + " " + Arrays.toString(sVC.toArray()));
                System.out.println(tree.toString());

                try {
                    System.in.read();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        new DepAnalyze(args[0]);
    }
}