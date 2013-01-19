package is2.mtag;

import is2.data.IEncoderPlus;
import is2.util.DB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Map Features, do not map long to integer
 *
 * @author Bernd Bohnet, 20.09.2009
 */
final public class MFO implements IEncoderPlus {

    /**
     * The features and its values
     */
    static private final HashMap<String, HashMap<String, Integer>> m_featureSets = new HashMap<>();
    /**
     * The feature class and the number of values
     */
    static private final HashMap<String, Integer> m_featureCounters = new HashMap<>();
    /**
     * The number of bits needed to encode a feature
     */
    static final HashMap<String, Integer> m_featureBits = new HashMap<>();
    /**
     * Integer counter for long2int
     */
    //private int count=0;
    /**
     * Stop growing
     */
    public boolean stop = false;
    final public static String NONE = "<None>";

    public static class Data {

        public final String[] a = new String[8];
        public final String[] v = new String[8];
        final short[] s = new short[9];

        public void clear(int i) {
            v[i] = null;
        }
    }

    final public static class Data4 {

        public int shift;
        public short a0, a1, a2, a3, a4, a5, a6, a7, a8, a9;
        public int v0, v1, v2, v3, v4, v5, v6, v7, v8, v9;

        final public long calcs(int b, long v, long l) {
            if (l < 0) {
                return l;
            }
            l |= v << shift;
            shift += b;
            return l;
        }

        final public long calc2() {

            if (v0 < 0 || v1 < 0) {
                return -1;
            }

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;

            return l;
        }

        final public long calc3() {

            if (v0 < 0 || v1 < 0 || v2 < 0) {
                return -1;
            }
            //	if (v1<0||v2<0) return -1;

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;
            l |= (long) v2 << shift;
            shift = (short) (shift + a2);

            //shift=;
            return l;
        }

        final public long calc4() {
            if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0) {
                return -1;
            }

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;
            l |= (long) v2 << shift;
            shift += a2;
            l |= (long) v3 << shift;
            shift = shift + a3;

            return l;
        }

        final public long calc5() {

            if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0) {
                return -1;
            }

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;
            l |= (long) v2 << shift;
            shift += a2;
            l |= (long) v3 << shift;
            shift += a3;
            l |= (long) v4 << shift;
            shift = shift + a4;

            return l;
        }

        final public long calc6() {

            if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0 || v5 < 0) {
                return -1;
            }

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;
            l |= (long) v2 << shift;
            shift += a2;
            l |= (long) v3 << shift;
            shift += a3;
            l |= (long) v4 << shift;
            shift += a4;
            l |= (long) v5 << shift;
            shift = shift + a5;

            return l;
        }

        final public long calc7() {

            if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0 || v5 < 0 || v6 < 0) {
                return -1;
            }

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;
            l |= (long) v2 << shift;
            shift += a2;
            l |= (long) v3 << shift;
            shift += a3;
            l |= (long) v4 << shift;
            shift += a4;
            l |= (long) v5 << shift;
            shift += a5;
            l |= (long) v6 << shift;
            shift = shift + a6;

            return l;
        }

        final public long calc8() {

            if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0 || v5 < 0 || v6 < 0 || v7 < 0) {
                return -1;
            }

            long l = v0;
            shift = a0;
            l |= (long) v1 << shift;
            shift += a1;
            l |= (long) v2 << shift;
            shift += a2;
            l |= (long) v3 << shift;
            shift += a3;
            l |= (long) v4 << shift;
            shift += a4;
            l |= (long) v5 << shift;
            shift += a5;
            l |= (long) v6 << shift;
            shift += a6;
            l |= (long) v7 << shift;
            shift = shift + a7;

            return l;
        }
    }

    public MFO() {
    }

    //	public  int size() {return count;}
    /**
     * Register an attribute class, if it not exists and add a possible value
     *
     * @param type
     * @param type2
     */
    @Override
    final public int register(String a, String v) {

        HashMap<String, Integer> fs = getFeatureSet().get(a);
        if (fs == null) {
            fs = new HashMap<>();
            getFeatureSet().put(a, fs);
            fs.put(NONE, 0);
            getFeatureCounter().put(a, 1);
        }
        Integer c = getFeatureCounter().get(a);

        Integer i = fs.get(v);
        if (i == null) {
            fs.put(v, c);
            c++;
            getFeatureCounter().put(a, c);
            return c - 1;
        } else {
            return i;
        }
    }

    /**
     * Calculates the number of bits needed to encode a feature
     */
    public void calculateBits() {

        int total = 0;
        for (Entry<String, Integer> e : getFeatureCounter().entrySet()) {
            int bits = (int) Math.ceil((Math.log(e.getValue() + 1) / Math.log(2)));
            m_featureBits.put(e.getKey(), bits);
            total += bits;
            //	System.out.println(" "+e.getKey()+" bits "+bits+" number "+(e.getValue()+1));
        }

        //		System.out.println("total number of needed bits "+total);
    }

    @Override
    public String toString() {

        StringBuilder content = new StringBuilder();
        for (Entry<String, Integer> e : getFeatureCounter().entrySet()) {
            content.append(e.getKey()).append(" ").append(e.getValue());
            content.append(':');
            //		 HashMap<String,Integer> vs = getFeatureSet().get(e.getKey());
            content.append(getFeatureBits(e.getKey()));

            /*
             * if (vs.size()<120) for(Entry<String,Integer> e2 : vs.entrySet())
             * { content.append(e2.getKey()+" ("+e2.getValue()+") ");
			 }
             */
            content.append('\n');

        }
        return content.toString();
    }

    public static long calcs(Data4 d, int b, long v, long l) {
        if (l < 0) {
            return l;
        }
        l |= v << d.shift;
        d.shift += b;
        return l;
    }

    public static short getFeatureBits(String a) {
        return (short) m_featureBits.get(a).intValue();
    }

    /**
     * Get the integer place holder of the string value v of the type a
     *
     * @param t the type
     * @param v the value
     * @return the integer place holder of v
     */
    @Override
    final public int getValue(String t, String v) {

        if (m_featureSets.get(t) == null) {
            return -1;
        }
        Integer vi = m_featureSets.get(t).get(v);
        if (vi == null) {
            return -1; //stop && 
        }
        return vi.intValue();
    }

    /**
     * Static version of getValue
     *
     * @see getValue
     */
    public static int getValueS(String a, String v) {

        if (m_featureSets.get(a) == null) {
            return -1;
        }
        Integer vi = m_featureSets.get(a).get(v);
        if (vi == null) {
            return -1; //stop && 
        }
        return vi.intValue();
    }

    public int hasValue(String a, String v) {

        Integer vi = m_featureSets.get(a).get(v);
        if (vi == null) {
            return -1;
        }
        return vi.intValue();
    }

    final public long calc2(Data4 d) {

        if (d.v0 < 0 || d.v1 < 0) {
            return -1;
        }
        //	if (d.v1<0||d.v2<0) return -1;

        long l = d.v0;
        short shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        //	l |= (long)d.v2<<shift;
        d.shift = shift;

        //d.shift=;
        return l;
    }

    final public long calc3(Data4 d) {

        if (d.v0 < 0 || d.v1 < 0 || d.v2 < 0) {
            return -1;
        }
        //	if (d.v1<0||d.v2<0) return -1;

        long l = d.v0;
        short shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        l |= (long) d.v2 << shift;
        d.shift = shift + d.a2;

        //d.shift=;
        return l;
    }

    final public long calc4(Data4 d) {
        if (d.v0 < 0 || d.v1 < 0 || d.v2 < 0 || d.v3 < 0) {
            return -1;
        }

        long l = d.v0;
        int shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        l |= (long) d.v2 << shift;
        shift += d.a2;
        l |= (long) d.v3 << shift;
        d.shift = shift + d.a3;

        return l;
    }

    final public long calc5(Data4 d) {

        if (d.v0 < 0 || d.v1 < 0 || d.v2 < 0 || d.v3 < 0 || d.v4 < 0) {
            return -1;
        }

        long l = d.v0;
        int shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        l |= (long) d.v2 << shift;
        shift += d.a2;
        l |= (long) d.v3 << shift;
        shift += d.a3;
        l |= (long) d.v4 << shift;
        d.shift = shift + d.a4;

        return l;
    }

    final public long calc6(Data4 d) {

        if (d.v0 < 0 || d.v1 < 0 || d.v2 < 0 || d.v3 < 0 || d.v4 < 0 || d.v5 < 0) {
            return -1;
        }

        long l = d.v0;
        int shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        l |= (long) d.v2 << shift;
        shift += d.a2;
        l |= (long) d.v3 << shift;
        shift += d.a3;
        l |= (long) d.v4 << shift;
        shift += d.a4;
        l |= (long) d.v5 << shift;
        d.shift = shift + d.a5;

        return l;
    }

    final public long calc7(Data4 d) {

        if (d.v0 < 0 || d.v1 < 0 || d.v2 < 0 || d.v3 < 0 || d.v4 < 0 || d.v5 < 0 || d.v6 < 0) {
            return -1;
        }

        long l = d.v0;
        int shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        l |= (long) d.v2 << shift;
        shift += d.a2;
        l |= (long) d.v3 << shift;
        shift += d.a3;
        l |= (long) d.v4 << shift;
        shift += d.a4;
        l |= (long) d.v5 << shift;
        shift += d.a5;
        l |= (long) d.v6 << shift;
        d.shift = shift + d.a6;

        return l;
    }

    final public long calc8(Data4 d) {

        if (d.v0 < 0 || d.v1 < 0 || d.v2 < 0 || d.v3 < 0 || d.v4 < 0 || d.v5 < 0 || d.v6 < 0 || d.v7 < 0) {
            return -1;
        }

        long l = d.v0;
        int shift = d.a0;
        l |= (long) d.v1 << shift;
        shift += d.a1;
        l |= (long) d.v2 << shift;
        shift += d.a2;
        l |= (long) d.v3 << shift;
        shift += d.a3;
        l |= (long) d.v4 << shift;
        shift += d.a4;
        l |= (long) d.v5 << shift;
        shift += d.a5;
        l |= (long) d.v6 << shift;
        shift += d.a6;
        l |= (long) d.v7 << shift;
        d.shift = shift + d.a7;

        return l;
    }
    /**
     * Maps a long to a integer value. This is very useful to save memory for
     * sparse data long values
     *
     * @param node
     * @return the integer
     */
    static public int misses = 0;
    static public int good = 0;

    /**
     * Write the data
     *
     * @param dos
     * @throws IOException
     */
    static public void writeData(DataOutputStream dos) throws IOException {

        dos.writeInt(getFeatureSet().size());
        for (Entry<String, HashMap<String, Integer>> e : getFeatureSet().entrySet()) {
            dos.writeUTF(e.getKey());
            dos.writeInt(e.getValue().size());

            for (Entry<String, Integer> e2 : e.getValue().entrySet()) {

                if (e2.getKey() == null) {
                    DB.println("key " + e2.getKey() + " value " + e2.getValue() + " e -key " + e.getKey());
                }
                dos.writeUTF(e2.getKey());
                dos.writeInt(e2.getValue());
            }
        }
    }

    public void read(DataInputStream din) throws IOException {

        int size = din.readInt();
        for (int i = 0; i < size; i++) {
            String k = din.readUTF();
            int size2 = din.readInt();

            HashMap<String, Integer> h = new HashMap<>();
            getFeatureSet().put(k, h);
            for (int j = 0; j < size2; j++) {
                h.put(din.readUTF(), din.readInt());
            }
            getFeatureCounter().put(k, size2);
        }

        calculateBits();
    }

    /**
     * Clear the data
     */
    static public void clearData() {
        getFeatureSet().clear();
        m_featureBits.clear();
        getFeatureSet().clear();
    }

    @Override
    public HashMap<String, Integer> getFeatureCounter() {
        return m_featureCounters;
    }

    static public HashMap<String, HashMap<String, Integer>> getFeatureSet() {
        return m_featureSets;
    }

    static public String[] reverse(HashMap<String, Integer> v) {
        String[] set = new String[v.size()];
        for (Entry<String, Integer> e : v.entrySet()) {
            set[e.getValue()] = e.getKey();
        }
        return set;
    }
}