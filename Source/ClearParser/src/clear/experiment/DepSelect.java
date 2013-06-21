package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.SRLReader;
import clear.util.IOUtil;
import clear.util.cluster.Kmeans;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntDoubleOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DepSelect {

    private ObjectIntOpenHashMap<String> m_ftr;
    private JIntDoubleTuple[][] a_unit;
    private int N;

    /**
     * @param cutoff feature cutoff (inclusive).
     */
    public DepSelect(String inputFile, String outputFile, int cutoff, int K, double threshold, float portion) {
        addLexica(inputFile);
        configureMap(cutoff);
        generateUnitClusters(inputFile);
        printSubset(inputFile, outputFile, getSubsetIndices(K, threshold, portion));
    }

    public void addLexica(String inputFile) {
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;
        String vb = "# of trees   : ";

        System.out.print(vb);
        m_ftr = new ObjectIntOpenHashMap<>();

        for (N = 0; (tree = reader.nextTree()) != null; N++) {
            for (String key : getFeatures(tree)) {
                m_ftr.put(key, m_ftr.get(key) + 1);
            }

            if (N % 10000 == 0) {
                System.out.print(".");
            }
        }

        reader.close();
        System.out.println("\r" + vb + N);
    }

    public void configureMap(int cutoff) {
        ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<>();
        int value, count = 1;

        System.out.print("# of features: ");

        for (ObjectCursor<String> cur : m_ftr.keys()) {
            value = m_ftr.get(cur.value);
            if (value >= cutoff) {
                map.put(cur.value, count++);
            }
        }

        m_ftr.clear();
        m_ftr.putAll(map);
        System.out.println(map.size());
    }

    public void generateUnitClusters(String inputFile) {
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;
        int i;

        System.out.print("Generating unit vectors: ");
        a_unit = new JIntDoubleTuple[N][];

        for (i = 0; (tree = reader.nextTree()) != null; i++) {
            a_unit[i] = generateUnitCluster(tree);
            if (i % 10000 == 0) {
                System.out.print(".");
            }
        }

        reader.close();
        System.out.println();
    }

    private JIntDoubleTuple[] generateUnitCluster(DepTree tree) {
        IntDoubleOpenHashMap map = new IntDoubleOpenHashMap();
        ArrayList<String> lsFtr = getFeatures(tree);
        int index;

        for (String key : lsFtr) {
            if ((index = m_ftr.get(key) - 1) >= 0) {
                map.put(index, map.get(index) + 1);
            }
        }

        int[] indices = map.keys().toArray();
        //	int   size    = tree.size();
        JIntDoubleTuple[] tup = new JIntDoubleTuple[map.size()];
        Arrays.sort(indices);
        index = 0;

        for (int i : indices) {
            tup[index++] = new JIntDoubleTuple(i, map.get(i));
        }

        return tup;
    }

    public IntOpenHashSet getSubsetIndices(int K, double threshold, float portion) {
        Kmeans km = new Kmeans(a_unit, m_ftr.size());
        ArrayList<ArrayList<JIntDoubleTuple>> cluster = km.cluster(K, threshold);
        IntOpenHashSet sAll = new IntOpenHashSet(), sSub;
        ArrayList<JIntDoubleTuple> ck;
        Random rand = new Random(0);
        int k, nk, nSub;

        for (k = 0; k < K; k++) {
            ck = cluster.get(k);
            nk = ck.size();
            nSub = Math.round(portion * nk);
            sSub = new IntOpenHashSet(nSub);
            //	Collections.sort(ck);

            while (sSub.size() < nSub) {
                sSub.add(ck.get(rand.nextInt(nk)).i);
            }

            sAll.addAll(sSub);
        }

        return sAll;
    }

    public void printSubset(String inputFile, String outputFile, IntOpenHashSet set) {
        SRLReader reader = new SRLReader(inputFile, true);
        DepTree tree;

        System.out.print("Printing: ");
        PrintStream fout = IOUtil.createPrintFileStream(outputFile);

        for (int i = 0; (tree = reader.nextTree()) != null; i++) {
            if (set.contains(i)) {
                fout.println(tree + "\n");
            }
            if (i % 10000 == 0) {
                System.out.print(".");
            }
        }

        reader.close();
        fout.close();
        System.out.println();
    }

    ArrayList<String> getFeatures(DepTree tree) {
        ArrayList<String> lsFtr = new ArrayList<>();
        int[] iFtr = new int[1];
        DepNode curr;

        tree.setSubcat();

        for (int i = 1; i < tree.size(); i++) {
            iFtr[0] = 0;
            curr = tree.get(i);

            getNgramFeatures(lsFtr, iFtr, tree, curr);
            getDepFeatures(lsFtr, iFtr, tree, curr);
        }

        return lsFtr;
    }

    void getNgramFeatures(ArrayList<String> lsFtr, int[] iFtr, DepTree tree, DepNode curr) {
        // 1-gram
        lsFtr.add(getFeature(iFtr, curr.form));
        lsFtr.add(getFeature(iFtr, curr.pos, curr.lemma));

        // 2-gram
        DepNode prev1 = null;

        if (curr.id - 1 > 0) {
            prev1 = tree.get(curr.id - 1);

            lsFtr.add(getFeature(iFtr, prev1.pos, curr.pos));
            lsFtr.add(getFeature(iFtr, prev1.lemma, curr.pos));
            lsFtr.add(getFeature(iFtr, prev1.pos, curr.lemma));
            lsFtr.add(getFeature(iFtr, prev1.lemma, curr.lemma));
        } else {
            iFtr[0] += 4;
        }

        // 3-gram
        DepNode prev2;

        if (curr.id - 2 > 0) {
            prev2 = tree.get(curr.id - 2);

            lsFtr.add(getFeature(iFtr, prev2.pos, prev1.pos, curr.pos));
        } else {
            iFtr[0] += 1;
        }
    }

    void getDepFeatures(ArrayList<String> lsFtr, int[] iFtr, DepTree tree, DepNode curr) {
        DepNode head = tree.get(curr.headId);
        String dir = (curr.id < head.id) ? "<" : ">";

        lsFtr.add(getFeature(iFtr, dir, curr.pos, head.pos));
        lsFtr.add(getFeature(iFtr, dir, curr.lemma, head.pos));
        lsFtr.add(getFeature(iFtr, dir, curr.pos, head.lemma));
        lsFtr.add(getFeature(iFtr, dir, curr.lemma, head.lemma));

        lsFtr.add(getFeature(iFtr, curr.deprel, curr.pos, head.pos));
        lsFtr.add(getFeature(iFtr, curr.deprel, curr.lemma, head.pos));
        lsFtr.add(getFeature(iFtr, curr.deprel, curr.pos, head.lemma));
        lsFtr.add(getFeature(iFtr, curr.deprel, curr.lemma, head.lemma));
    }

    String getFeature(int[] iFtr, String... ftr) {
        StringBuilder build = new StringBuilder();
        build.append(iFtr[0]++);

        for (String s : ftr) {
            build.append("_");
            build.append(s);
        }

        return build.toString();
    }

    static public void main(String[] args) {
        String inputFile = args[0];
        String outputFile = args[1];
        int cutoff = Integer.parseInt(args[2]);
        int K = Integer.parseInt(args[3]);
        double threshold = Double.parseDouble(args[4]);
        float portion = Float.parseFloat(args[5]);
        DepSelect depSelect = 
                new DepSelect(inputFile, outputFile, cutoff, K, threshold, portion);
    }
}