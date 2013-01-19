package clear.util.cluster;

import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Kmeans {

    private final int RAND_SEED = 0;
    private int N, D;
    private JIntDoubleTuple[][] v_unit;
    private double[] d_centroid;
    private double[] d_scala;

    public Kmeans(JIntDoubleTuple[][] unit, int dimension) {
        v_unit = unit;
        N = unit.length;
        D = dimension;
    }

    /**
     * K-means clustering.
     *
     * @param threshold minimum RSS.
     * @return each row represents a cluster, and each column represents a tuple
     * of (index of a unit vector, similarity to the centroid).
     */
    public ArrayList<ArrayList<JIntDoubleTuple>> cluster(int K, double threshold) {
        ArrayList<ArrayList<JIntDoubleTuple>> currCluster = null;
        ArrayList<ArrayList<JIntDoubleTuple>> prevCluster = null;
        double prevRss = -1, currRss;

        initCentroids(K);
        int max = N / K;

        for (int iter = 0; iter < max; iter++) {
            System.out.println("\n===== Iteration: " + iter + " =====\n");

            currCluster = getClusters(K);
            updateCentroids(K, currCluster);
            currRss = getRSS(K, currCluster);

            if (prevRss >= currRss) {
                return prevCluster;
            }
            if (currRss >= threshold) {
                break;
            }

            prevRss = currRss;
            prevCluster = currCluster;
        }

        return currCluster;
    }

    /**
     * Initializes random centroids.
     */
    private void initCentroids(int K) {
        IntOpenHashSet set = new IntOpenHashSet();
        Random rand = new Random(RAND_SEED);
        d_centroid = new double[K * D];
        d_scala = new double[K];

        while (set.size() < K) {
            set.add(rand.nextInt(N));
        }

        int k = 0;
        double scala;

        for (IntCursor cur : set) {
            scala = 0;

            for (JIntDoubleTuple tup : v_unit[cur.value]) {
                d_centroid[getCentroidIndex(k, tup.i)] = tup.d;
                scala += tup.d * tup.d;
            }

            d_scala[k++] = Math.sqrt(scala);
        }
    }

    /**
     * @return centroid of each cluster.
     */
    private void updateCentroids(int K, ArrayList<ArrayList<JIntDoubleTuple>> cluster) {
        ArrayList<JIntDoubleTuple> ck;
        int i, k, size;
        double scala;

        Arrays.fill(d_centroid, 0);
        Arrays.fill(d_scala, 0);

        System.out.print("Updating centroids: ");

        for (k = 0; k < K; k++) {
            ck = cluster.get(k);

            for (JIntDoubleTuple tup1 : ck) {
                for (JIntDoubleTuple tup2 : v_unit[tup1.i]) {
                    d_centroid[getCentroidIndex(k, tup2.i)] += tup2.d;
                }
            }

            size = ck.size();
            scala = 0;

            for (i = k * D; i < (k + 1) * D; i++) {
                if (d_centroid[i] > 0) {
                    d_centroid[i] /= size;
                    scala += d_centroid[i] * d_centroid[i];
                }
            }

            d_scala[k] = Math.sqrt(scala);
            System.out.print(".");
        }

        System.out.println();
    }

    /**
     * Each cluster contains indices of {@link Kmeans#v_unit}.
     */
    private ArrayList<ArrayList<JIntDoubleTuple>> getClusters(int K) {
        ArrayList<ArrayList<JIntDoubleTuple>> cluster = new ArrayList<>(K);
        JIntDoubleTuple max = new JIntDoubleTuple(-1, -1);
        JIntDoubleTuple[] unit;
        int i, k;
        double sim;

        for (k = 0; k < K; k++) {
            cluster.add(new ArrayList<JIntDoubleTuple>());
        }

        System.out.print("Clustering: ");

        for (i = 0; i < N; i++) {
            unit = v_unit[i];
            max.set(-1, -1);

            for (k = 0; k < K; k++) {
                if ((sim = cosine(unit, k)) > max.d) {
                    max.set(k, sim);
                }
            }

            cluster.get(max.i).add(new JIntDoubleTuple(i, max.d));
            if (i % 10000 == 0) {
                System.out.print(".");
            }
        }

        System.out.println();

        for (k = 0; k < K; k++) {
            System.out.printf("%4d: %d\n", k, cluster.get(k).size());
        }

        return cluster;
    }

    /**
     * @param k [0, K-1].
     * @param index [0, D-1].
     */
    private int getCentroidIndex(int k, int index) {
        return k * D + index;
    }

    private double getRSS(int K, ArrayList<ArrayList<JIntDoubleTuple>> cluster) {
        double sim = 0;
        System.out.print("Calulating RSS: ");

        for (int k = 0; k < K; k++) {
            for (JIntDoubleTuple tup : cluster.get(k)) {
                sim += cosine(v_unit[tup.i], k);
            }

            System.out.print(".");
        }

        System.out.println();
        sim /= N;

        System.out.println("RSS = " + sim);
        return sim / N;
    }

    private double cosine(JIntDoubleTuple[] unit, int k) {
        double scala = 0, dot = 0;

        for (JIntDoubleTuple tup : unit) {
            dot += tup.d * d_centroid[getCentroidIndex(k, tup.i)];
            scala += tup.d * tup.d;
        }

        scala = Math.sqrt(scala);

        return dot / (scala * d_scala[k]);
    }
}