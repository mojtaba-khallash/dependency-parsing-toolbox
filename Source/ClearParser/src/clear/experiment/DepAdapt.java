package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;
import clear.util.IOUtil;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class DepAdapt {

    final int MAX_GAP = 5;
    final int MAX_ITER = 20;
    final double STOP = 0.1;
    ObjectIntOpenHashMap<String> m_lexica;
    ArrayList<IntOpenHashSet> a_vectors;

    public DepAdapt(String trainFile, String testFile, String outputFile, double m) {
        initLexica(trainFile);
        initVectors(testFile);
        select(trainFile, outputFile, m);
    }

    private void initLexica(String trainFile) {
        DepReader reader = new DepReader(trainFile, true);
        DepTree tree;
        int d = 1;

        System.out.print("Initializing lexica : ");
        m_lexica = new ObjectIntOpenHashMap<>();

        while ((tree = reader.nextTree()) != null) {
            for (String key : getLexica(tree, true)) {
                if (!m_lexica.containsKey(key)) {
                    m_lexica.put(key, d++);
                }
            }
        }

        reader.close();
        System.out.println(m_lexica.size());
    }

    private void initVectors(String testFile) {
        DepReader reader = new DepReader(testFile, true);
        DepTree tree;
        int n;

        System.out.print("Initializing vectors: ");
        a_vectors = new ArrayList<>();

        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            a_vectors.add(getVectorSet(tree, false));
        }

        reader.close();
        a_vectors.trimToSize();
        System.out.println(n);
    }

    private IntOpenHashSet getVectorSet(DepTree tree, boolean isTrain) {
        IntOpenHashSet set = new IntOpenHashSet();
        int d;

        for (String key : getLexica(tree, isTrain)) {
            if ((d = m_lexica.get(key)) > 0) {
                set.add(d - 1);
            }
        }

        return set;
    }

    private int[] getVectorArray(DepTree tree, boolean isTrain) {
        IntOpenHashSet set = getVectorSet(tree, isTrain);
        int[] vector = set.toArray();

        Arrays.sort(vector);
        return vector;
    }

    private HashSet<String> getLexica(DepTree tree, boolean isTrain) {
        HashSet<String> set = new HashSet<>();

        addNgramLexica(tree, set);

        if (isTrain) {
            addDepTrnLexica(tree, set);
        } else {
            addDepTstLexica(tree, set);
        }

        return set;
    }

    private void addNgramLexica(DepTree tree, HashSet<String> set) {
        DepNode node, prev;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);

            set.add(node.lemma);
            set.add(node.pos);

            if (i > 1) {
                prev = tree.get(i - 1);
                add2gramLexica(set, 0, prev, node);
            }
        }
    }

    private void addDepTrnLexica(DepTree tree, HashSet<String> set) {
        DepNode node, prev, next;
        int dist;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            dist = Math.abs(node.id - node.headId);

            if (dist > MAX_GAP || node.headId == 0) {
                continue;
            }

            if (node.id < node.headId) {
                prev = node;
                next = tree.get(node.headId);
            } else {
                prev = tree.get(node.headId);
                next = node;
            }

            add2gramLexica(set, dist, prev, next);
        }
    }

    private void addDepTstLexica(DepTree tree, HashSet<String> set) {
        DepNode prev, next;
        int i, dist, size = tree.size();

        for (i = 1; i < size; i++) {
            prev = tree.get(i);

            for (dist = 1; dist <= MAX_GAP && i + dist < size; dist++) {
                next = tree.get(i + dist);
                add2gramLexica(set, dist, prev, next);
            }
        }
    }

    private void add2gramLexica(HashSet<String> set, int dist, DepNode prev, DepNode next) {
        String prefix = (dist <= 0) ? "" : dist + "_";

        set.add(prefix + prev.lemma + "_" + next.lemma);
        set.add(prefix + prev.lemma + "_" + next.pos);
        set.add(prefix + prev.pos + "_" + next.lemma);
        set.add(prefix + prev.pos + "_" + next.pos);
    }

    private void select(String trainFile, String outputFile, double m) {
        PrintStream fout = IOUtil.createPrintFileStream(outputFile);
        DepReader reader = new DepReader(trainFile, true);
        DepTree tree;
        int[] vector1;
        int size = a_vectors.size(), count = 0;
        double sim;

        System.out.print("Selecting trees: ");

        for (int i = 1; (tree = reader.nextTree()) != null; i++) {
            vector1 = getVectorArray(tree, true);
            sim = 0;

            for (IntOpenHashSet vector2 : a_vectors) {
                sim += getCosineSimilarity(vector1, vector2);
            }

            if (sim / size > m) {
                fout.println(tree + "\n");
                count++;
            }

            if (i % 1000 == 0) {
                System.out.print(".");
            }
        }

        reader.close();
        fout.close();
        System.out.println();
        System.out.println(count);
    }

    private double getCosineSimilarity(int[] vector1, IntOpenHashSet vector2) {
        double dot = 0;

        for (int idx : vector1) {
            if (vector2.contains(idx)) {
                dot++;
            }
        }

        return dot / (Math.sqrt(vector1.length) * Math.sqrt(vector2.size()));
    }

    static public void main(String[] args) {
        String trainFile = args[0];
        String testFile = args[1];
        String outputFile = args[2];
        double m = Double.parseDouble(args[3]);

        new DepAdapt(trainFile, testFile, outputFile, m);
    }
}