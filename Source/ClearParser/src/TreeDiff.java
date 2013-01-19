import clear.treebank.TBNode;
import clear.treebank.TBReader;
import clear.treebank.TBTree;
import java.util.ArrayList;

public final class TreeDiff {

    public TreeDiff(String treeFile1, String treeFile2) {
        isDifferent(treeFile1, treeFile2);
    }

    boolean isDifferent(String treeFile1, String treeFile2) {
        TBReader reader1 = new TBReader(treeFile1);
        TBReader reader2 = new TBReader(treeFile2);
        TBTree tree1, tree2;

        for (int treeId = 0; (tree1 = reader1.nextTree()) != null; treeId++) {
            tree2 = reader2.nextTree();

            if (tree2 == null) {
                System.err.println("Different # of trees");
                return true;
            }

            if (isDifferent(tree1, tree2, treeId)) {
                return true;
            }
        }

        if (reader2.nextTree() != null) {
            System.err.println("Different # of trees");
            return true;
        }

        return false;
    }

    boolean isDifferent(TBTree tree1, TBTree tree2, int treeId) {
        ArrayList<TBNode> list1 = tree1.getTerminalNodes();
        ArrayList<TBNode> list2 = tree2.getTerminalNodes();

        if (list1.size() != list2.size()) {
            System.err.println("Different # of terminal nodes: " + treeId);
            return true;
        }

        for (int termId = 0; termId < list1.size(); termId++) {
            if (list1.get(termId).getMaxHeight() != list2.get(termId).getMaxHeight()) {
                System.out.println("Different terminal nodes: " + treeId + " " + termId);
                return true;
            }
        }

        return false;
    }

    static public void main(String[] args) {
        TreeDiff treeDiff = new TreeDiff(args[0], args[1]);
    }
}