package is2.data;

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
final public class MFB implements IEncoderPlus {

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
    static private int count = 0;
    /**
     * Stop growing
     */
    public boolean stop = false;
    final public static String NONE = "<None>";

    public MFB() {
    }

    public int size() {
        return count;
    }

    /**
     * Register an attribute class, if it not exists and add a possible value
     *
     * @param type
     * @param type2
     */
    @Override
    final public int register(String a, String v) {

        synchronized (m_featureCounters) {

            HashMap<String, Integer> fs = getFeatureSet().get(a);
            if (fs == null) {
                fs = new HashMap<>();
                getFeatureSet().put(a, fs);
                fs.put(NONE, 0);
                getFeatureCounter().put(a, 1);
            }

            Integer i = fs.get(v);
            if (i == null) {
                Integer c = getFeatureCounter().get(a);
                fs.put(v, c);
                c++;
                getFeatureCounter().put(a, c);
                return c - 1;
            } else {
                return i;
            }
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

    public static short getFeatureBits(String a) {
        if (m_featureBits.get(a) == null) {
            return 0;
        }
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

    public static String printBits(int k) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 31; i++) {
            s.append((k & 0x00000001) == 1 ? '1' : '0');
            k = k >> 1;

        }
        s.reverse();
        return s.toString();
    }
    /**
     * Maps a long to a integer value. This is very useful to save memory for
     * sparse data long values
     *
     * @param l
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
        // DB.println("write"+getFeatureSet().size());
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

        count = size;
        //	stop();
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