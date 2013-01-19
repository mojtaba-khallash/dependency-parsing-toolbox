package clear.util.cluster;

import clear.util.tuple.JObjectDoubleTuple;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Prob1dMap extends ObjectIntOpenHashMap<String> {

    public int n_total;

    public Prob1dMap() {
        n_total = 0;
    }

    public void increment(String key) {
        put(key, get(key) + 1);
        n_total++;
    }

    public double getProb(String key) {
        return (double) get(key) / n_total;
    }

    public ObjectDoubleOpenHashMap<String> getProbMap() {
        ObjectDoubleOpenHashMap<String> map = new ObjectDoubleOpenHashMap<>(size());

        for (ObjectCursor<String> cur : keys()) {
            map.put(cur.value, getProb(cur.value));
        }

        return map;
    }

    /**
     * @return sorted list generated from a map: P(1D)
     */
    public ArrayList<JObjectDoubleTuple<String>> getProbList() {
        ArrayList<JObjectDoubleTuple<String>> list = new ArrayList<>(size());

        for (ObjectCursor<String> cur : keys()) {
            list.add(new JObjectDoubleTuple<>(cur.value, getProb(cur.value)));
        }

        Collections.sort(list);
        return list;
    }

    public String toStringProb() {
        StringBuilder build = new StringBuilder();
        DecimalFormat format = new DecimalFormat("#0.0000");

        for (JObjectDoubleTuple<String> tup : getProbList()) {
            build.append(tup.object);
            build.append("\t");
            build.append(format.format(tup.value));
            build.append("\n");
        }

        return build.toString().trim();
    }
}