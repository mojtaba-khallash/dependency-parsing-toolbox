package clear.util;

import clear.util.tuple.JObjectIntTuple;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.util.ArrayList;
import java.util.Collections;

public class JMap {

    static public <T> ArrayList<JObjectIntTuple<T>> getSortedTuples(ObjectIntOpenHashMap<T> map) {
        ArrayList<JObjectIntTuple<T>> list = new ArrayList<>();

        for (ObjectCursor<T> cur : map.keys()) {
            list.add(new JObjectIntTuple<>(cur.value, map.get(cur.value)));
        }

        Collections.sort(list);
        return list;
    }
}