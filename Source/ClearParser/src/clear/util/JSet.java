package clear.util;

import com.carrotsearch.hppc.IntOpenHashSet;

public class JSet {

    /**
     * @return true if set2 is the subset of set1.
     */
    static public boolean isSubset(IntOpenHashSet set1, IntOpenHashSet set2) {
        IntOpenHashSet set = new IntOpenHashSet(set2);

        set.removeAll(set1);
        return set.isEmpty();
    }

    static public int min(IntOpenHashSet set) {
        int[] array = set.toArray();
        int min = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }
}