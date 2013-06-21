package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;
import clear.util.IOUtil;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class DepFuzzyCluster {

    final int MAX_GAP = 5;
    final int MAX_ITER = 20;
    final double STOP = 0.1;
    ObjectIntOpenHashMap<String> m_lexica;
    int[][] a_vectors;		// i_N x *
    double[][] d_centroids;	// i_D x i_K
    double[] d_scalars;		// i_K	
    double[][] d_u;			// i_N x i_K
    double d_m, d_a;
    int i_K, i_D, i_N;

    public DepFuzzyCluster(String trainFile, String testFile, int K, double m) {
        init(trainFile, K, m);
        train();
        split(trainFile, testFile);
    }

    private void init(String trainFile, int K, double m) {
        i_K = K;
        d_m = m;
        d_a = 1d / i_K;

        initLexica(trainFile);
        initVectors(trainFile);
        initPriors();
    }

    private void initLexica(String trainFile) {
        DepReader reader = new DepReader(trainFile, true);
        DepTree tree;
        int d = 1;

        System.out.print("Initializing lexica : ");
        m_lexica = new ObjectIntOpenHashMap<>();

        for (i_N = 0; (tree = reader.nextTree()) != null; i_N++) {
            for (String key : getLexica(tree, true)) {
                if (!m_lexica.containsKey(key)) {
                    m_lexica.put(key, d++);
                }
            }
        }

        reader.close();
        System.out.println((i_D = m_lexica.size()));
    }

    private void initVectors(String filename) {
        DepReader reader = new DepReader(filename, true);
        DepTree tree;

        System.out.print("Initializing vectors: ");
        a_vectors = new int[i_N][];

        for (int n = 0; (tree = reader.nextTree()) != null; n++) {
            a_vectors[n] = getVector(tree, true);
        }

        reader.close();
        System.out.println(i_N);
    }

    private void initPriors() {
        double u = 1d / i_K;
        d_u = new double[i_N][i_K];

        System.out.print("Initializing priors : ");

        for (int n = 0; n < i_N; n++) {
            Arrays.fill(d_u[n], u);
        }

        System.out.println(i_N + " x " + i_K);
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

    private int[] getVector(DepTree tree, boolean isTrain) {
        IntOpenHashSet set = new IntOpenHashSet();
        int d;

        for (String key : getLexica(tree, isTrain)) {
            if ((d = m_lexica.get(key)) > 0) {
                set.add(d - 1);
            }
        }

        int[] vector = set.toArray();
        Arrays.sort(vector);

        return vector;
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

    private void train() {
        double prevScore, currScore = 0;

        for (int i = 0; i < MAX_ITER; i++) {
            System.out.println("\nIteration: " + i);
            prevScore = currScore;

            System.out.println("- E-step");
            expectation();

            System.out.println("- M-step");
            currScore = maximization();

            System.out.println("- Score : " + currScore);

            if (Math.abs(currScore - prevScore) < STOP) {
                break;
            }
        }
    }

    private void expectation() {
        if (d_centroids == null) {
            initCentroids();
            return;
        }

        double[] den = new double[i_K];
        double u;
        int[] vector;
        int k, n;

        for (n = 0; n < i_N; n++) {
            vector = a_vectors[n];

            for (k = 0; k < i_K; k++) {
                u = getPrior(n, k);
                den[k] += u;

                for (int d : vector) {
                    d_centroids[d][k] += u;
                }
            }
        }

        Arrays.fill(d_scalars, 0);

        for (n = 0; n < i_D; n++) {
            for (k = 0; k < i_K; k++) {
                u = d_centroids[n][k] / den[k];
                d_centroids[n][k] = u;
                d_scalars[k] += u * u;
            }
        }

        for (k = 0; k < i_K; k++) {
            d_scalars[k] = Math.sqrt(d_scalars[k]);
        }
    }

    private void initCentroids() {
        IntOpenHashSet set = new IntOpenHashSet();
        Random rand = new Random(0);
        int[] vector;
        int n, k = 0;

        d_centroids = new double[i_D][i_K];
        d_scalars = new double[i_K];

        while (set.size() < i_K) {
            n = rand.nextInt(i_N);

            if (!set.contains(n)) {
                vector = a_vectors[n];
                set.add(n);

                for (int d : vector) {
                    d_centroids[d][k] = 1;
                }

                d_scalars[k++] = Math.sqrt(vector.length);
            }
        }
    }

    private double getPrior(int n, int k) {
        return Math.pow(d_u[n][k], d_m);
    }

    private double maximization() {
        double[] dist;
        double num, den, sum, m = 1d / (d_m - 1), score = 0;
        int[] vector;
        int n, k, i;

        for (n = 0; n < i_N; n++) {
            vector = a_vectors[n];
            dist = getCosineDistances(vector);

            for (k = 0; k < i_K; k++) {
                num = dist[k];
                sum = 0;

                for (i = 0; i < i_K; i++) {
                    if (i == k) {
                        sum += 1;
                    } else {
                        den = dist[i];
                        sum += Math.pow(num / den, m);
                    }
                }

                d_u[n][k] = 1d / sum;
                score += Math.pow(d_u[n][k], d_m) * dist[k];
            }
        }

        return score;
    }

    private double[] getCosineDistances(int[] vector) {
        double[] dots = new double[i_K];
        double[] centroid;
        double scalar = Math.sqrt(vector.length);
        int k;

        for (int d : vector) {
            centroid = d_centroids[d];

            for (k = 0; k < i_K; k++) {
                dots[k] += centroid[k];
            }
        }

        for (k = 0; k < i_K; k++) {
            dots[k] = 1 - (dots[k] / (d_scalars[k] * scalar));
        }

        return dots;
    }

    private void split(String trainFile, String testFile) {
        splitTrainFile(trainFile);
        splitTestFile(testFile);
    }

    private void splitTrainFile(String trainFile) {
        PrintStream[] fout = getPrintStreams(trainFile);
        DepReader reader = new DepReader(trainFile, true);
        DepTree tree;
        int[] count = new int[i_K];
        int n, k;

        System.out.println("\nSplitting: " + trainFile);

        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            for (k = 0; k < i_K; k++) {
                if (d_u[n][k] >= d_a) {
                    fout[k].println(tree + "\n");
                    count[k]++;
                }
            }
        }

        for (k = 0; k < i_K; k++) {
            System.out.println(k + ": " + count[k]);
        }

        closePrintStreams(fout);
    }

    private void splitTestFile(String testFile) {
        PrintStream[] fout = getPrintStreams(testFile);
        DepReader reader = new DepReader(testFile, true);
        DepTree tree;
        int[] count = new int[i_K];
        int n, k;
        double[] dist;
        JIntDoubleTuple max = new JIntDoubleTuple(-1, -1);

        System.out.println("\nSplitting: " + testFile);

        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            dist = getCosineDistances(getVector(tree, false));
            max.set(-1, Double.MAX_VALUE);

            for (k = 0; k < i_K; k++) {
                if (dist[k] < max.d) {
                    max.set(k, dist[k]);
                }
            }

            fout[max.i].println(tree + "\n");
            count[max.i]++;
        }

        for (k = 0; k < i_K; k++) {
            System.out.println(k + ": " + count[k]);
        }

        closePrintStreams(fout);
    }

    private PrintStream[] getPrintStreams(String filename) {
        PrintStream[] fout = new PrintStream[i_K];

        for (int k = 0; k < i_K; k++) {
            fout[k] = IOUtil.createPrintFileStream(filename + ".k" + i_K + ".m" + d_m + "." + k);
        }

        return fout;
    }

    private void closePrintStreams(PrintStream[] fout) {
        for (PrintStream f : fout) {
            f.close();
        }
    }

    static public void main(String[] args) {
        String trainFile = args[0];
        String testFile = args[1];
        int K = Integer.parseInt(args[2]);
        double m = Double.parseDouble(args[3]);
        DepFuzzyCluster depFuzzyCluster = new DepFuzzyCluster(trainFile, testFile, K, m);
    }
}