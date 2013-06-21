package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.DepReader;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class DepGMCluster {

    final int MAX_ITER = 20;
    final double SQRT_2PI = Math.sqrt(2 * Math.PI);
    final double STOP = 0.01;
    ObjectIntOpenHashMap<String> m_lexica;
    int[][] a_vectors;	// i_N x *
    double[][] d_m;			// i_D x i_K
    double[] d_s;			// i_K
    double[] p_k;			// i_K
    double[][] p_kn;		// i_N x i_K
    double[] d_mScalars;	// i_K	
    int i_K, i_D, i_N;

    public DepGMCluster(String trainFile, int K) {
        init(trainFile, K);
        cluster();
    }

    public void init(String trainFile, int K) {
        i_K = K;

        initLexica(trainFile);
        initVectors(trainFile);
        initDistributions();
    }

    private void initLexica(String trainFile) {
        DepReader reader = new DepReader(trainFile, true);
        DepTree tree;
        int d = 1;

        System.out.print("Initializing lexica : ");
        m_lexica = new ObjectIntOpenHashMap<>();

        for (i_N = 0; (tree = reader.nextTree()) != null; i_N++) {
            for (String key : getLexica(tree)) {
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
            a_vectors[n] = getVector(tree);
        }

        reader.close();
        System.out.println(i_N);
    }

    private void initDistributions() {
        System.out.print("Initializing priors : ");
        initPriors();
        System.out.println(i_K + " x " + i_N);

        System.out.print("Initializing means  : ");
        initMeans();
        System.out.println(i_K);

        System.out.print("Initializing stdevs : ");
        initStdevs();
        System.out.println(i_K);
    }

    private void initPriors() {
        double p = 1d / i_K;

        p_k = new double[i_K];
        p_kn = new double[i_N][i_K];

        Arrays.fill(p_k, p);
    }

    private void initMeans() {
        IntOpenHashSet set = new IntOpenHashSet();
        Random rand = new Random(0);
        int[] vector;
        int n, k = 0;

        d_m = new double[i_D][i_K];
        d_mScalars = new double[i_K];

        while (set.size() < i_K) {
            n = rand.nextInt(i_N);

            if (!set.contains(n)) {
                vector = a_vectors[n];
                set.add(n);

                for (int d : vector) {
                    d_m[d][k] = 1;
                }

                d_mScalars[k++] = Math.sqrt(vector.length);
            }
        }
    }

    private void initStdevs() {
        double[] dist;
        int n, k;
        d_s = new double[i_K];

        for (n = 0; n < i_N; n++) {
            dist = getCosineDistances(a_vectors[n]);

            for (k = 0; k < i_K; k++) {
                d_s[k] += dist[k];
            }
        }

        for (k = 0; k < i_K; k++) {
            d_s[k] = Math.sqrt(d_s[k] / i_N);
        }
    }

    private HashSet<String> getLexica(DepTree tree) {
        HashSet<String> set = new HashSet<>();

        addDepLexica(tree, set);
        return set;
    }

    protected void addDepLexica(DepTree tree, HashSet<String> set) {
        DepNode node, head;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            if (node.headId < 0) {
                continue;
            }

            addDep1gramLexica(set, node, "<", node.deprel);
            if (node.headId == 0) {
                continue;
            }
            head = tree.get(node.headId);
            addDep1gramLexica(set, head, ">", node.deprel);

            if (node.id < head.id) {
                addDep2gramLexica(set, node, head, "<", "");
                addDep2gramLexica(set, node, head, "<", node.deprel);
            } else {
                addDep2gramLexica(set, head, node, ">", "");
                addDep2gramLexica(set, head, node, ">", node.deprel);
            }

            if (head.headId < 0) {
                continue;
            }
            addDep3gramLexica(set, node, head, tree.get(head.headId));
        }
    }

    private void addDep1gramLexica(HashSet<String> set, DepNode node, String dir, String deprel) {
        set.add(node.lemma + dir + deprel);
        set.add(node.pos + dir + deprel);
    }

    private void addDep2gramLexica(HashSet<String> set, DepNode prev, DepNode next, String dir, String deprel) {
        String label = dir + deprel;

        set.add(prev.lemma + "_" + next.lemma + label);
        set.add(prev.lemma + "_" + next.pos + label);
        set.add(prev.pos + "_" + next.lemma + label);
        set.add(prev.pos + "_" + next.pos + label);
    }

    private void addDep3gramLexica(HashSet<String> set, DepNode node, DepNode head, DepNode grandHead) {
        set.add(node.pos + "_" + head.pos + "_" + grandHead.pos);
    }

    private int[] getVector(DepTree tree) {
        IntOpenHashSet set = new IntOpenHashSet();
        int d;

        for (String key : getLexica(tree)) {
            if ((d = m_lexica.get(key)) > 0) {
                set.add(d - 1);
            }
        }

        int[] vector = set.toArray();
        Arrays.sort(vector);

        return vector;
    }

    public void cluster() {
        double prevScore = 0, currScore = expectation();
        System.out.println("- Score : " + currScore);

        for (int i = 0; i < MAX_ITER && Math.abs(currScore - prevScore) > STOP; i++) {
            System.out.println("\nIteration: " + i);
            prevScore = currScore;

            System.out.println("- M-step");
            maximization();

            System.out.println("- E-step");
            currScore = expectation();

            System.out.println("- Score : " + currScore);
        }

        Random rand = new Random();

        for (int i = 0; i < 10; i++) {
            System.out.println(Arrays.toString(p_kn[rand.nextInt(i_N)]));
        }
    }

    private double expectation() {
        double[] memberships;
        double sum, score = 0;
        int k, n;

        for (n = 0; n < i_N; n++) {
            memberships = getMemberships(a_vectors[n]);
            sum = 0;

            for (k = 0; k < i_K; k++) {
                sum += memberships[k];
            }

            for (k = 0; k < i_K; k++) {
                p_kn[n][k] = memberships[k] / sum;
            }

            score += Math.log(sum);
        }

        return score;
    }

    private double[] getMemberships(int[] vector) {
        double[] memberships = new double[i_K];
        double[] dist = getCosineDistances(vector);
        double stdev;

        for (int k = 0; k < i_K; k++) {
            stdev = d_s[k];
            memberships[k] = p_k[k] * (1 / (SQRT_2PI * stdev)) * Math.exp(-0.5 * dist[k] * (stdev * stdev));
        }

        return memberships;
    }

    private void maximization() {
        maximizePriors();
        maximizeMeans();
        maximizeStdDevs();

        for (int k = 0; k < i_K; k++) {
            p_k[k] /= i_N;
        }
    }

    private void maximizePriors() {
        int n, k;

        Arrays.fill(p_k, 0);

        for (n = 0; n < i_N; n++) {
            for (k = 0; k < i_K; k++) {
                p_k[k] += p_kn[n][k];
            }
        }
    }

    private void maximizeMeans() {
        int n, k, d;
        double m;

        for (d = 0; d < i_D; d++) {
            Arrays.fill(d_m[d], 0);
        }

        for (n = 0; n < i_N; n++) {
            for (int idx : a_vectors[n]) {
                for (k = 0; k < i_K; k++) {
                    d_m[idx][k] += p_kn[n][k];
                }
            }
        }

        Arrays.fill(d_mScalars, 0);

        for (d = 0; d < i_D; d++) {
            for (k = 0; k < i_K; k++) {
                m = d_m[d][k] / p_k[k];
                d_m[d][k] = m;
                d_mScalars[k] += m * m;
            }
        }

        for (k = 0; k < i_K; k++) {
            d_mScalars[k] = Math.sqrt(d_mScalars[k]);
        }
    }

    private void maximizeStdDevs() {
        double[] dist;
        int n, k;

        Arrays.fill(d_s, 0);

        for (n = 0; n < i_N; n++) {
            dist = getCosineDistances(a_vectors[n]);

            for (k = 0; k < i_K; k++) {
                d_s[k] += p_kn[n][k] * dist[k];
            }
        }

        for (k = 0; k < i_K; k++) {
            d_s[k] = Math.sqrt(d_s[k] / p_k[k]);
        }
    }

    private double[] getCosineDistances(int[] vector) {
        double[] dots = new double[i_K];
        double[] mean;
        double scalar = Math.sqrt(vector.length);
        int k;

        for (int d : vector) {
            mean = d_m[d];

            for (k = 0; k < i_K; k++) {
                dots[k] += mean[k];
            }
        }

        for (k = 0; k < i_K; k++) {
            dots[k] = 1 - (dots[k] / (d_mScalars[k] * scalar));
        }

        return dots;
    }

    static public void main(String[] args) {
        String trainFile = args[0];
        int K = Integer.parseInt(args[1]);
        DepGMCluster depGMCluster = new DepGMCluster(trainFile, K);
    }
}