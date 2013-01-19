package clear.experiment;

import clear.morph.MorphEnAnalyzer;
import clear.treebank.TBNode;
import clear.treebank.TBReader;
import clear.treebank.TBTree;
import clear.util.IOUtil;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ExtractMissPredicates {
    // <treeFile, <treeId, relId>>

    HashMap<String, IntObjectOpenHashMap<IntOpenHashSet>> m_prop;
    MorphEnAnalyzer m_morph;
    PrintStream f_out, f_all;

    public ExtractMissPredicates(String treeDir, String dictFile, String propFile, String outFile) {
        try {
            m_prop = getPropMap(propFile);
            m_morph = new MorphEnAnalyzer(dictFile);
            extractMissPredicates(treeDir, outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public HashMap<String, IntObjectOpenHashMap<IntOpenHashSet>> getPropMap(String propFile) throws Exception {
        HashMap<String, IntObjectOpenHashMap<IntOpenHashSet>> mProp = new HashMap<>();
        BufferedReader reader = IOUtil.createBufferedFileReader(propFile);
        IntObjectOpenHashMap<IntOpenHashSet> mTree;
        IntOpenHashSet sRel;
        String line, treeFile;
        int treeId, relId, n;
        String[] tmp;

        for (n = 0; (line = reader.readLine()) != null; n++) {
            tmp = line.split(" ");
            treeFile = tmp[0];
            treeId = Integer.parseInt(tmp[1]);
            relId = Integer.parseInt(tmp[2]);

            if (mProp.containsKey(treeFile)) {
                mTree = mProp.get(treeFile);
            } else {
                mTree = new IntObjectOpenHashMap<>();
                mProp.put(treeFile, mTree);
            }

            if (mTree.containsKey(treeId)) {
                sRel = mTree.get(treeId);
            } else {
                sRel = new IntOpenHashSet();
                mTree.put(treeId, sRel);
            }

            sRel.add(relId);
        }

        System.out.println("# of PropBank instances: " + n);

        return mProp;
    }

    public void extractMissPredicates(String treeDir, String outFile) {
        ArrayList<String> treeFiles = new ArrayList<>(m_prop.keySet());
        Collections.sort(treeFiles);
        f_out = IOUtil.createPrintFileStream(outFile);
        f_all = IOUtil.createPrintFileStream(outFile + ".ex");

        for (String treeFile : treeFiles) {
            System.out.println("- " + treeFile);
            extractMissPredicatesAux(treeDir, treeFile);
        }

        f_out.close();
        f_all.close();
    }

    private void extractMissPredicatesAux(String treeDir, String treeFile) {
        TBReader reader = new TBReader(treeDir + File.separator + treeFile);
        TBTree tree;
        int relId;
        String lemma, instance;

        for (int treeId = 0; (tree = reader.nextTree()) != null; treeId++) {
            for (TBNode node : tree.getTerminalNodes()) {
                relId = node.terminalId;
                lemma = m_morph.getLemma(node.form, node.pos);

                if (isPredicate(node, lemma, treeFile) && !isIncluded(treeFile, treeId, relId)) {
                    if (lemma.equals("'s")) {
                        lemma = "be";
                    }
                    instance = getPBInstance(lemma, treeFile, treeId, relId);

                    f_out.println(instance);

                    f_all.println(instance);
                    f_all.println(tree.toTree() + "\n");
                }
            }
        }
    }

    public boolean isPredicate(TBNode node, String lemma, String treeFile) {
        if (!node.isPos("VB.*")) {
            return false;
        }

        TBNode parent = node.getParent();
        if (!parent.isPos("VP") || parent.isPos("EDITED")) {
            return false;
        }

        if (parent.isSucceededBy("HYPH")) {
            return false;
        }

        if (node.isFollowedBy("VP|HYPH")) {
            return false;
        }

        if (lemma.equals("do")) {
            return false;
        }

        if (treeFile.startsWith("nw/wsj") && lemma.matches("take|give|make|have")) {
            return false;
        }

        return true;
    }

    private boolean isIncluded(String treeFile, int treeId, int relId) {
        IntObjectOpenHashMap<IntOpenHashSet> mTree = m_prop.get(treeFile);
        if (!mTree.containsKey(treeId)) {
            return false;
        }

        IntOpenHashSet sRel = mTree.get(treeId);
        return sRel.contains(relId);
    }

    public String getPBInstance(String lemma, String treeFile, int treeId, int relId) {
        StringBuilder build = new StringBuilder();

        build.append(treeFile);
        build.append(" ");
        build.append(treeId);
        build.append(" ");
        build.append(relId);
        build.append(" ");
        build.append("userId ");
        build.append(lemma).append("-v ");
        build.append(lemma).append(".XX ");
        build.append("----- ");
        build.append(relId);
        build.append(":");
        build.append("0-rel");

        return build.toString();
    }

    public static void main(String[] args) {
        new ExtractMissPredicates(args[0], args[1], args[2], args[3]);
    }
}