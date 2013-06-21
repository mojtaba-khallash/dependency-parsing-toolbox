package clear.util.cluster;

import clear.util.IOUtil;
import clear.util.tuple.JObjectDoubleTuple;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Prob2dMap extends HashMap<String, Prob1dMap> {

    private int n_total;

    public Prob2dMap() {
        n_total = 0;
    }

    public double get2dProb(String key2d) {
        Prob1dMap map = get(key2d);
        return (double) map.n_total / n_total;
    }

    public double get2dProb(String key2d, String key1d) {
        Prob1dMap map = get(key2d);
        double prob2d = (double) map.n_total / n_total;

        return prob2d * map.getProb(key1d);
    }

    public double get1dProb(String key2d, String key1d) {
        Prob1dMap map = get(key2d);
        return map.getProb(key1d);
    }

    public ObjectDoubleOpenHashMap<String> getProb1dMap(String key2d) {
        Prob1dMap map1d = get(key2d);
        if (map1d == null) {
            return null;
        }

        return map1d.getProbMap();
    }

    /**
     * @return probabilistic map: P(1D|2D) * P(2D).
     */
    public ObjectDoubleOpenHashMap<String> getProb2dMap(String key2d) {
        Prob1dMap map1d = get(key2d);
        if (map1d == null) {
            return null;
        }

        ObjectDoubleOpenHashMap<String> map = new ObjectDoubleOpenHashMap<>(map1d.size());
        double prob2d = (double) map1d.n_total / n_total;

        for (ObjectCursor<String> cur : map1d.keys()) {
            map.put(cur.value, map1d.getProb(cur.value) * prob2d);
        }

        return map;
    }

    /**
     * @return sorted list generated from a 1st-degree map: P(1D|2D)
     */
    public ArrayList<JObjectDoubleTuple<String>> getProb1dList(String key2d) {
        return map2list(getProb1dMap(key2d));
    }

    /**
     * @return sorted list generated from a 1st-degree map: P(1D|2D) * P(2D)
     */
    public ArrayList<JObjectDoubleTuple<String>> getProb2dList(String key2d) {
        return map2list(getProb2dMap(key2d));
    }

    private ArrayList<JObjectDoubleTuple<String>> map2list(ObjectDoubleOpenHashMap<String> map) {
        if (map == null) {
            return null;
        }

        ArrayList<JObjectDoubleTuple<String>> list = new ArrayList<>(map.size());

        for (ObjectCursor<String> cur : map.keys()) {
            list.add(new JObjectDoubleTuple<>(cur.value, map.get(cur.value)));
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Increments both 1st and 2nd-degree maps.
     */
    public void increment(String key2d, String key1d) {
        Prob1dMap map = get1dMap(key2d);
        n_total++;

        map.increment(key1d);
    }

    /**
     * Increments a top map once, and all sub maps
     */
    public void increment(String key2d, Collection<String> keys1d) {
        Prob1dMap map = get1dMap(key2d);
        n_total += keys1d.size();

        for (String key1d : keys1d) {
            map.increment(key1d);
        }
    }

    /**
     * Increments a 2nd-degree map.
     */
    public Prob1dMap get1dMap(String key2d) {
        Prob1dMap map1d;

        if (containsKey(key2d)) {
            map1d = get(key2d);
        } else {
            map1d = new Prob1dMap();
            put(key2d, map1d);
        }

        return map1d;
    }

    public void print(String filename, DecimalFormat format) {
        try (PrintStream fout = IOUtil.createPrintFileStream(filename)) {
            fout.print(toString(format));
        }
    }

    @Override
    public String toString() {
        return toString(new DecimalFormat("#0.0000"));
    }

    public String toString(DecimalFormat format) {
        ArrayList<String> keys2d = new ArrayList<>(keySet());
        StringBuilder build = new StringBuilder();
        Collections.sort(keys2d);

        for (String key2d : keys2d) {
            build.append(toString(key2d, format));
            build.append("\n");
        }

        return build.toString();
    }

    public String toString(String key2d, DecimalFormat format) {
        ArrayList<JObjectDoubleTuple<String>> list = getProb2dList(key2d);
        StringBuilder build = new StringBuilder();

        build.append(key2d);

        for (JObjectDoubleTuple<String> tup : list) {
            build.append(" ");
            build.append(tup.object);
            build.append(":");
            build.append(format.format(tup.value));
        }

        return build.toString();
    }
}