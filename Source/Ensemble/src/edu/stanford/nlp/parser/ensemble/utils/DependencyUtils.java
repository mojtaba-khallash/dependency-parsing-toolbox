package edu.stanford.nlp.parser.ensemble.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DependencyUtils {

    private static class IdComparator implements Comparator<Dependency> {

        @Override
        public int compare(Dependency o1, Dependency o2) {
            if (o1.mod() < o2.mod()) {
                return -1;
            } else if (o1.mod() == o2.mod()) {
                return 0;
            }
            return 1;
        }
    }

    public static <T extends Dependency> void sortById(List<T> tree) {
        Collections.sort(tree, new IdComparator());
    }
}