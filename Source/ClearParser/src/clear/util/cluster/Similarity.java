package clear.util.cluster;

import com.carrotsearch.hppc.IntDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;

public class Similarity {

    static public double cosine(IntDoubleOpenHashMap map1, IntDoubleOpenHashMap map2) {
        double scala1 = 0, scala2 = 0, dot = 0, val;

        for (int key : map1.keys().toArray()) {
            val = map1.get(key);

            if (map2.containsKey(key)) {
                dot += (val * map2.get(key));
            }

            scala1 += (val * val);
        }

        for (int key : map2.keys().toArray()) {
            val = map2.get(key);
            scala2 += (val * val);
        }

        scala1 = Math.sqrt(scala1);
        scala2 = Math.sqrt(scala2);

        return dot / (scala1 * scala2);
    }

    static public double cosine(ObjectDoubleOpenHashMap<String> map1, ObjectDoubleOpenHashMap<String> map2) {
        double scala1 = 0, scala2 = 0, dot = 0, val;
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

    static public double cosine(ObjectIntOpenHashMap<String> map1, ObjectIntOpenHashMap<String> map2) {
        double scala1 = 0, scala2 = 0, dot = 0, val;
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
}