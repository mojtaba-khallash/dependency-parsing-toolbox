package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;
import clear.treebank.TBEnLib;

public class CheckNonPorjective {

    int total = 0, totalSen = 0;
    int nproj = 0, nprojSen = 0;

    public CheckNonPorjective(String inputFile) {
        DepReader reader = new DepReader(inputFile, true);
        DepTree tree;
        //	PrintStream fout = IOUtil.createPrintFileStream(inputFile+".np");
        int i;
        for (i = 0; (tree = reader.nextTree()) != null; i++) {
            //		total += tree.size() - 1;
            if (!isProjective(tree)) {
                nprojSen++;
            }
            totalSen++;
            //	fout.println(tree.toStringNonProj()+"\n");
        }

        //	System.out.println((double)total/i);

        System.out.printf("Dependency: %d / %d = %4.2f\n", nproj, total, (double) nproj / total * 100);
        System.out.printf("Sentence  : %d / %d = %4.2f\n", nprojSen, totalSen, (double) nprojSen / totalSen * 100);
    }

    private boolean isProjective(DepTree tree) {
        total += tree.size() - 1;
        boolean isProj = true;

        for (int i = 1; i < tree.size(); i++) {
            DepNode curr = tree.get(i);
            if (TBEnLib.isPunctuation(curr.pos)) {
                continue;
            }
            DepNode head = tree.get(curr.headId);

            int sId = (curr.id < head.id) ? curr.id : head.id;
            int eId = (curr.id < head.id) ? head.id : curr.id;

            for (int j = sId + 1; j < eId; j++) {
                DepNode node = tree.get(j);
                //	if (TBEnLib.isPunctuation(node.pos))	continue;

                if (node.headId < sId || node.headId > eId) {
                    //	curr.nonProj = 1;
                    nproj++;
                    isProj = false;
                    break;
                }
            }
        }

        return isProj;
    }

    static public void main(String[] args) {
        new CheckNonPorjective(args[0]);
    }
}