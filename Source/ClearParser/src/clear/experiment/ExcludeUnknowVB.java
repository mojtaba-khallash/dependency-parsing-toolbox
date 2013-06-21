package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.reader.SRLReader;
import clear.util.IOUtil;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

public class ExcludeUnknowVB {

    public ExcludeUnknowVB(String trainFile, String testFile, String outputFile) {
        removeKnownVerbs(testFile, outputFile, getknownVerbs(trainFile));
    }

    private HashSet<String> getknownVerbs(String trainFile) {
        HashSet<String> set = new HashSet<>();
        SRLReader reader = new SRLReader(trainFile, true);
        DepTree tree;
        DepNode node;

        while ((tree = reader.nextTree()) != null) {
            for (int i = 1; i < tree.size(); i++) {
                node = tree.get(i);
                if (node.isPredicate()) {
                    set.add(node.lemma);
                }
            }
        }

        return set;
    }

    private void removeKnownVerbs(String testFile, String outputFile, HashSet<String> verbs) {
        SRLReader reader = new SRLReader(testFile, true);
        try (PrintStream fout = IOUtil.createPrintFileStream(outputFile)) {
            DepTree tree;
            DepNode node;
            SRLInfo info;
            IntOpenHashSet set;
            ArrayList<SRLHead> list;

            while ((tree = reader.nextTree()) != null) {
                set = new IntOpenHashSet();

                for (int i = 1; i < tree.size(); i++) {
                    node = tree.get(i);

                    if (node.isPredicate() && verbs.contains(node.lemma)) {
                        set.add(node.id);
                    }
                }

                for (int i = 1; i < tree.size(); i++) {
                    node = tree.get(i);
                    info = node.srlInfo;
                    list = new ArrayList<>();

                    for (SRLHead head : info.heads) {
                        if (set.contains(head.headId)) {
                            list.add(head);
                        }
                    }

                    if (!list.isEmpty()) {
                        info.heads.removeAll(list);
                    }
                }

                fout.println(tree + "\n");
            }
        }
    }

    static public void main(String[] args) {
        String trainFile = args[0];
        String testFile = args[1];
        String outputFile = args[2];
        ExcludeUnknowVB excludeUnknowVB = 
                new ExcludeUnknowVB(trainFile, testFile, outputFile);
    }
}