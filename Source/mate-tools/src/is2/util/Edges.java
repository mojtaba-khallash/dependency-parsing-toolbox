package is2.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Dr. Bernd Bohnet, 13.05.2009;
 *
 *
 */
public final class Edges {

    private static short[][][] edges;
    private static HashMap<Short, Integer> labelCount = new HashMap<>();
    private static HashMap<String, Integer> slabelCount = new HashMap<>();
    static short[] def = new short[1];

    private Edges() {
    }

    /**
     * @param length
     */
    public static void init(int length) {
        edges = new short[length][length][];
    }

    public static void findDefault() {

        int best = 0;

        for (Entry<Short, Integer> e : labelCount.entrySet()) {

            if (best < e.getValue()) {
                best = e.getValue();
                def[0] = e.getKey();
            }
        }

        //	labelCount=null;
        //	String[] types = new String[mf.getFeatureCounter().get(PipeGen.REL)];
        //	for (Entry<String, Integer> e : MFO.getFeatureSet().get(PipeGen.REL).entrySet())  	types[e.getValue()] = e.getKey();

        is2.util.DB.println("set default label to " + def[0] + " ");

        //	System.out.println("found default "+def[0]);

    }

    public static void put(int pos1, int pos2, short label) {
        putD(pos1, pos2, label);
        //	putD(pos2, pos1,!dir, label);		
    }

    public static void putD(int pos1, int pos2, short label) {

        Integer lc = labelCount.get(label);
        if (lc == null) {
            labelCount.put(label, 1);
        } else {
            labelCount.put(label, lc + 1);
        }

        String key = pos1 + "-" + pos2 + label;
        Integer lcs = slabelCount.get(key);
        if (lcs == null) {
            slabelCount.put(key, 1);
        } else {
            slabelCount.put(key, lcs + 1);
        }

        if (edges[pos1][pos2] == null) {
            edges[pos1][pos2] = new short[1];
            edges[pos1][pos2][0] = label;

//			edgesh[pos1][pos2][dir?0:1] = new TIntHashSet(2);
//			edgesh[pos1][pos2][dir?0:1].add(label);
        } else {
            short labels[] = edges[pos1][pos2];
            for (short l : labels) {
                //contains label already?
                if (l == label) {
                    return;
                }
            }

            short[] nlabels = new short[labels.length + 1];
            System.arraycopy(labels, 0, nlabels, 0, labels.length);
            nlabels[labels.length] = label;
            edges[pos1][pos2] = nlabels;

            //		edgesh[pos1][pos2][dir?0:1].add(label);
        }
    }

    public static short[] get(int pos1, int pos2) {

        if (pos1 < 0 || pos2 < 0 || edges[pos1][pos2] == null) {
            return def;
        }
        return edges[pos1][pos2];
    }

    /**
     * @param dis
     */
    static public void write(DataOutputStream d) throws IOException {

        int len = edges.length;
        d.writeShort(len);

        for (int p1 = 0; p1 < len; p1++) {
            for (int p2 = 0; p2 < len; p2++) {
                if (edges[p1][p2] == null) {
                    d.writeShort(0);
                } else {
                    d.writeShort(edges[p1][p2].length);
                    for (int l = 0; l < edges[p1][p2].length; l++) {
                        d.writeShort(edges[p1][p2][l]);
                    }
                }
            }
        }
        d.writeShort(def[0]);
    }

    /**
     * @param dis
     */
    public static void read(DataInputStream d) throws IOException {
        int len = d.readShort();

        edges = new short[len][len][];
        for (int p1 = 0; p1 < len; p1++) {
            for (int p2 = 0; p2 < len; p2++) {
                int ll = d.readShort();
                if (ll == 0) {
                    edges[p1][p2] = null;
                } else {
                    edges[p1][p2] = new short[ll];
                    for (int l = 0; l < ll; l++) {
                        edges[p1][p2][l] = d.readShort();
                    }
                }
            }
        }
        def[0] = d.readShort();
    }

    public static class C implements Comparator<Short> {

        public C() {
            super();
        }
        String _key;

        public C(String key) {
            super();
            _key = key;
        }

        /*
         * (non-Javadoc) @see java.util.Comparator#compare(java.lang.Object,
         * java.lang.Object)
         */
        @Override
        public int compare(Short l1, Short l2) {

            //	int c1 = labelCount.get(l1);
            //	int c2 = labelCount.get(l2);
            //	if (true) return c1==c2?0:c1>c2?-1:1;

            int x1 = slabelCount.get(_key + l1.shortValue());
            int x2 = slabelCount.get(_key + l2.shortValue());
            //	System.out.println(x1+" "+x2);

            return x1 == x2 ? 0 : x1 > x2 ? -1 : 1;
        }
    }
}