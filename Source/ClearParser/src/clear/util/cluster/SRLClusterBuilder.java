package clear.util.cluster;

import clear.experiment.SRLVerbCluster;
import clear.util.tuple.JIntDoubleTuple;
import clear.util.tuple.JIntIntTuple;
import clear.util.tuple.JObjectDoubleTuple;
import clear.util.tuple.JObjectObjectTuple;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SRLClusterBuilder {
    // 0.78, 0.76

    double d_hm_lower = 0.78;	// 0.80 same
    double d_km_lower = 0.78;
    ObjectDoubleOpenHashMap<String> d_similarities;
    ArrayList<ProbCluster> k_clusters;

    public SRLClusterBuilder() {
    }

    public SRLClusterBuilder(double threshold) {
        d_km_lower = threshold;
    }

    public ArrayList<ProbCluster> getInitClusters(HashMap<String, ObjectDoubleOpenHashMap<String>> map) {
        ArrayList<ProbCluster> clusters = new ArrayList<>();
        ProbCluster cluster;

        for (String key : map.keySet()) {
            cluster = new ProbCluster(key);
            cluster.add(map.get(key));
            clusters.add(cluster);
        }

        return clusters;
    }

    /**
     * @return average cosine similarity between two clusters.
     */
    public double getCtrSimilarity(ProbCluster cluster1, ProbCluster cluster2, boolean useDynamic) {
        String key = getJoinedKey(cluster1, cluster2);
        if (useDynamic && d_similarities.containsKey(key)) {
            return d_similarities.get(key);
        }

        ObjectDoubleOpenHashMap<String> ctr1 = getCentroid(cluster1);
        ObjectDoubleOpenHashMap<String> ctr2 = getCentroid(cluster2);

        double sim = getCosineSimilarity(ctr1, ctr2);
        if (useDynamic) {
            d_similarities.put(key, sim);
        }

        return sim;
    }

    public ObjectDoubleOpenHashMap<String> getCentroid(ProbCluster cluster) {
        ObjectDoubleOpenHashMap<String> centroid = new ObjectDoubleOpenHashMap<>();
        String key;

        for (ObjectDoubleOpenHashMap<String> map : cluster) {
            for (ObjectCursor<String> cur : map.keys()) {
                key = cur.value;
                centroid.put(key, centroid.get(key) + map.get(key));
            }
        }

        int size = cluster.size();

        for (ObjectCursor<String> cur : centroid.keys()) {
            key = cur.value;
            centroid.put(key, centroid.get(key) / size);
        }

        return centroid;
    }

    /**
     * @return average cosine similarity between two clusters.
     */
    public double getAvgSimilarity(ProbCluster cluster1, ProbCluster cluster2, boolean useDynamic) {
        String key = getJoinedKey(cluster1, cluster2);
        if (useDynamic && d_similarities.containsKey(key)) {
            return d_similarities.get(key);
        }

        double avg = 0;

        for (ObjectDoubleOpenHashMap<String> map1 : cluster1) {
            for (ObjectDoubleOpenHashMap<String> map2 : cluster2) {
                avg += getCosineSimilarity(map1, map2);
            }
        }

        avg /= (cluster1.size() * cluster2.size());
        if (useDynamic) {
            d_similarities.put(key, avg);
        }

        return avg;
    }

    /**
     * @return cosine similarity of two maps.
     */
    public double getCosineSimilarity(ObjectDoubleOpenHashMap<String> map1, ObjectDoubleOpenHashMap<String> map2) {
        double dot = 0, scala1 = 0, scala2 = 0, val;
        String key;

        for (ObjectCursor<String> cur : map1.keys()) {
            key = cur.value;
            val = map1.get(key);

            if (map2.containsKey(key)) {
                dot += (val * map2.get(key));
            }

            scala1 += (val * val);
        }

        for (ObjectCursor<String> cur : map2.keys()) {
            val = map2.get(cur.value);
            scala2 += (val * val);
        }

        scala1 = Math.sqrt(scala1);
        scala2 = Math.sqrt(scala2);

        return dot / (scala1 * scala2);
    }

    /**
     * @return joined key of two clusters.
     */
    public String getJoinedKey(ProbCluster cluster1, ProbCluster cluster2) {
        StringBuilder build = new StringBuilder();

        /*
         * build.append("["); build.append(cluster1.key); build.append(",");
         * build.append(cluster2.key);
		build.append("]");
         */

        build.append(cluster1.key);
        build.append(",");
        build.append(cluster2.key);

        return build.toString();
    }

    public void printCluster() {
        Collections.sort(k_clusters);
        int count = 0;

        for (ProbCluster cluster : k_clusters) {
            if (cluster.size() == 1) {
                break;
            }
            System.out.println(cluster.key + " " + cluster.score);
            count++;
        }

        System.out.println("# of clusters: " + count);
    }

// ======================== Hierarchical agglomerative clustering ========================	
    public void hmCluster(HashMap<String, ObjectDoubleOpenHashMap<String>> map) {
        d_similarities = new ObjectDoubleOpenHashMap<>();
        k_clusters = getInitClusters(map);

        hmClusterRec();
        hmClusterTrim();
    }

    private void hmClusterRec() {
        boolean cont = true;

        for (int i = 0; cont; i++) {
            System.out.println("== Iteration: " + i + " ==");
            cont = hmClusterAux();
            //	if (cont)	printCluster();
        }
    }

    protected void hmClusterTrim() {
        ArrayList<ProbCluster> remove = new ArrayList<>();

        for (ProbCluster cluster : k_clusters) {
            if (cluster.size() == 1) {
                remove.add(cluster);
            }
        }

        k_clusters.removeAll(remove);
        d_similarities.clear();
        printCluster();
    }

    /**
     * @return true if clustering is performed.
     */
    private boolean hmClusterAux() {
        ArrayList<JObjectDoubleTuple<JIntIntTuple>> list = new ArrayList<>();
        ProbCluster cluster1, cluster2;
        double score;

        for (int i = 0; i < k_clusters.size() - 1; i++) {
            cluster1 = k_clusters.get(i);

            for (int j = i + 1; j < k_clusters.size(); j++) {
                cluster2 = k_clusters.get(j);
                score = getAvgSimilarity(cluster1, cluster2, true);
                //	score    = getCtrSimilarity(cluster1,cluster2,true);
                list.add(new JObjectDoubleTuple<>(new JIntIntTuple(i, j), score));
            }
        }

        IntOpenHashSet sClustered = new IntOpenHashSet();
        ArrayList<ProbCluster> sRemove = new ArrayList<>();
        JIntIntTuple idx;
        Collections.sort(list);

        for (int i = 0; i < list.size(); i++) {
            JObjectDoubleTuple<JIntIntTuple> tup = list.get(i);

            if (tup.value < d_hm_lower) {
                break;
            }

            idx = tup.object;
            if (sClustered.contains(idx.int1) || sClustered.contains(idx.int2)) {
                continue;
            }
            sClustered.add(idx.int1);
            sClustered.add(idx.int2);

            cluster1 = k_clusters.get(idx.int1);
            cluster2 = k_clusters.get(idx.int2);
            cluster1.addAll(cluster2);
            cluster1.set(getJoinedKey(cluster1, cluster2), tup.value);
            sRemove.add(cluster2);
        }

        k_clusters.removeAll(sRemove);
        return !sClustered.isEmpty();
    }

// ======================== K-mean clustering ========================
    public void kmCluster(HashMap<String, ObjectDoubleOpenHashMap<String>> map) {
        ArrayList<ProbCluster> nClusters = getInitClusters(map);
        ArrayList<JIntIntTuple> list = new ArrayList<>();
        ProbCluster kCluster, nCluster;
        JIntDoubleTuple max;
        int i, j, k;
        double sim;
        IntOpenHashSet skip = new IntOpenHashSet();

        for (k = 0; k < 3; k++) {
            System.out.println("== Iteration: " + k + " ==");
            list.clear();

            for (i = 0; i < nClusters.size(); i++) {
                if (skip.contains(i)) {
                    continue;
                }
                nCluster = nClusters.get(i);
                max = new JIntDoubleTuple(-1, -1);

                for (j = 0; j < k_clusters.size(); j++) {
                    sim = getAvgSimilarity(nCluster, k_clusters.get(j), false);
                    if (max.d < sim) {
                        max.set(j, sim);
                    }
                }

                if (max.d >= d_km_lower) {
                    list.add(new JIntIntTuple(max.i, i));
                }
            }

            for (JIntIntTuple tup : list) {
                kCluster = k_clusters.get(tup.int1);
                nCluster = nClusters.get(tup.int2);
                kCluster.addAll(nCluster);
                kCluster.set(getJoinedKey(kCluster, nCluster), 1);
                skip.add(tup.int2);
            }

            if (list.isEmpty()) {
                break;
            }
        }

        printCluster();
    }

    public JObjectObjectTuple<IntIntOpenHashMap, IntIntOpenHashMap> getClusterMaps() {
        IntIntOpenHashMap lMap = new IntIntOpenHashMap();
        IntIntOpenHashMap gMap = new IntIntOpenHashMap();
        ProbCluster cluster;
        String[] ids, key;

        for (int i = 0; i < k_clusters.size(); i++) {
            cluster = k_clusters.get(i);
            ids = cluster.key.split(",");

            for (String id : ids) {
                key = id.split(":");

                if (key[0].equals(SRLVerbCluster.FLAG_LOCAL)) {
                    lMap.put(Integer.parseInt(key[1]), i + 1);
                } else {
                    gMap.put(Integer.parseInt(key[1]), i + 1);
                }
            }
        }

        return new JObjectObjectTuple<>(lMap, gMap);
    }

    public void cluster(Prob2dMap map, ObjectDoubleOpenHashMap<String> mWeights) {
        d_similarities = new ObjectDoubleOpenHashMap<>();
        k_clusters = getInitClusters(map, mWeights);

        hmClusterRec();
    }

    /**
     * Called from {@link SRLClusterBuilder#MapCluster(ProbMap, int)}.
     */
    private ArrayList<ProbCluster> getInitClusters(Prob2dMap map, ObjectDoubleOpenHashMap<String> mWeights) {
        ArrayList<ProbCluster> clusters = new ArrayList<>();
        ObjectDoubleOpenHashMap<String> lmap;
        ProbCluster cluster;
        double weight;

        for (String key : map.keySet()) {
            cluster = new ProbCluster(key);
            lmap = map.getProb2dMap(key);

            for (ObjectCursor<String> cur : lmap.keys()) {
                if ((weight = mWeights.get(cur.value)) > 0) {
                    lmap.put(cur.value, lmap.get(cur.value) * weight);
                }
            }

            cluster.add(lmap);
            clusters.add(cluster);
        }

        return clusters;
    }
}