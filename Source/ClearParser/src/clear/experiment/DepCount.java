package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;

public class DepCount {

    public DepCount(String filename) {
        DepReader reader = new DepReader(filename, true);
        DepTree tree;
        DepNode node;
        int i, dist, total = 0;

        int[] count = new int[10];

        while ((tree = reader.nextTree()) != null) {
            for (i = 1; i < tree.size(); i++) {
                node = tree.get(i);
                dist = Math.abs(node.id - node.headId) - 1;
                if (dist > 9) {
                    dist = 9;
                }

                count[dist]++;
                total++;
            }
        }

        for (i = 0; i < count.length; i++) {
            System.out.println(count[i]);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new DepCount(args[0]);
    }
}