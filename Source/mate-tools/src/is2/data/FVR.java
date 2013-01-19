package is2.data;

import gnu.trove.TIntDoubleHashMap;
import is2.parser.Parser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class FVR extends IFV {

    private FVR subfv1;
    private FVR subfv2;
    private boolean negateSecondSubFV = false;
    private int size;
    // content of the nodes NxC
    private int m_index[];
    private float m_value[];

    // type of the nodes NxT
    public FVR() {
        this(10);
    }

    public FVR(int initialCapacity) {
        m_index = new int[initialCapacity];
        m_value = new float[initialCapacity];
    }

    /*
     * public FVR (FVR fv1, FVR fv2) { subfv1 = fv1; subfv2 = fv2; }
     */
    public FVR(FVR fv1, FVR fv2, boolean negSecond) {
        this(0);
        subfv1 = fv1;
        subfv2 = fv2;
        negateSecondSubFV = negSecond;
    }

    /**
     * Read a feature vector
     *
     * @param index
     * @param value
     */
    public FVR(DataInputStream dos, int capacity) throws IOException {
        this(capacity);
        size = m_index.length;

        for (int i = 0; i < size; i++) {
            m_index[i] = dos.readInt();
        }
    }

    /**
     * Read a feature vector
     *
     * @param index
     * @param value
     */
    public FVR(DataInputStream dos) throws IOException {
        this(dos.readInt());
        size = m_index.length;

        for (int i = 0; i < size; i++) {
            m_index[i] = dos.readInt();
        }
    }

    /**
     * Increases the capacity of this <tt>Graph</tt> instance, if necessary, to
     * ensure that it can hold at least the number of nodes specified by the
     * minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity.
     */
    private void ensureCapacity(int minCapacity) {


        if (minCapacity > m_index.length) {

            int oldIndex[] = m_index;
            float oldValue[] = m_value;

            int newCapacity = (m_index.length * 3) / 2 + 1;


            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            m_index = new int[newCapacity];
            m_value = new float[newCapacity];

            System.arraycopy(oldIndex, 0, m_index, 0, oldIndex.length);
            System.arraycopy(oldValue, 0, m_value, 0, oldValue.length);

        }
    }

    final public int size() {
        return size;
    }

    final public boolean isEmpty() {
        return size == 0;
    }

    @Override
    final public void clear() {
        size = 0;
    }

    final public int createFeature(int i, float v) {

        ensureCapacity(size + 1);
        m_index[size] = i;
        m_value[size] = v;
        size++;
        return size - 1;
    }
    /*
     * final public int createFeature(int i) {
     *
     * ensureCapacity(size+1); m_index[size] =i; size++; return size-1; }
     */

    final public int getIndex(int i) {
        return m_index[i];
    }

    public void setIndex(int p, int i) {
        m_index[p] = i;
    }

    /**
     * Trims the capacity of this <tt>Graph</tt> instance to true size. An
     * application can use this operation to minimize the storage of an
     * <tt>Graph</tt> instance.
     */
    public void trimToSize() {

        if (size < m_index.length) {


            int oldIndex[] = m_index;

            m_index = new int[size];
            System.arraycopy(oldIndex, 0, m_index, 0, size);
        }
    }

    @Override
    final public void add(int i) {
        if (i >= 0) {
            ensureCapacity(size + 1);
            m_index[size] = i;
            m_value[size] = 1.0f;
            size++;
        }
    }

    final public void add(int i, float f) {
        if (i >= 0) {
            createFeature(i, f);
        }
    }

    // fv1 - fv2
    public FVR getDistVector(FVR fl2) {
        return new FVR(this, fl2, true);
    }

    public double getScore(double[] parameters, boolean negate) {
        double score = 0.0;

        if (null != subfv1) {
            score += subfv1.getScore(parameters, negate);

            if (null != subfv2) {
                if (negate) {
                    score += subfv2.getScore(parameters, !negateSecondSubFV);
                } else {
                    score += subfv2.getScore(parameters, negateSecondSubFV);
                }
            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                score -= parameters[m_index[i]];
            }
        } else {
            for (int i = 0; i < size; i++) {
                score += parameters[m_index[i]];
            }
        }

        return score;
    }

    final public float getScore(float[] parameters, boolean negate) {
        float score = 0.0F;

        if (null != subfv1) {
            score += subfv1.getScore(parameters, negate);

            if (null != subfv2) {
                if (negate) {
                    score += subfv2.getScore(parameters, !negateSecondSubFV);
                } else {
                    score += subfv2.getScore(parameters, negateSecondSubFV);
                }
            }
        }

        // warning changed the  value 

        if (negate) {
            for (int i = 0; i < size; i++) {
                score -= parameters[m_index[i]] * m_value[i];
            }
        } else {
            for (int i = 0; i < size; i++) {
                score += parameters[m_index[i]] * m_value[i];
            }
        }

        return score;
    }

    final public int getScore(short[] parameters, boolean negate) {
        int score = 0;

        if (null != subfv1) {
            score += subfv1.getScore(parameters, negate);

            if (null != subfv2) {
                if (negate) {
                    score += subfv2.getScore(parameters, !negateSecondSubFV);
                } else {
                    score += subfv2.getScore(parameters, negateSecondSubFV);
                }
            }
        }

        // warning changed the value 

        if (negate) {
            for (int i = 0; i < size; i++) {
                score -= parameters[m_index[i]] * m_value[i];
            }
        } else {
            for (int i = 0; i < size; i++) {
                score += parameters[m_index[i]] * m_value[i];
            }
        }

        return score;
    }

    public final void update(float[] parameters, float[] total, double alpha_k, double upd, boolean negate) {

        if (null != subfv1) {
            subfv1.update(parameters, total, alpha_k, upd, negate);

            if (null != subfv2 && negate) {
                subfv2.update(parameters, total, alpha_k, upd, !negateSecondSubFV);
            } else {
                subfv2.update(parameters, total, alpha_k, upd, negateSecondSubFV);
            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                parameters[getIndex(i)] -= alpha_k * m_value[i];
                total[getIndex(i)] -= upd * alpha_k * m_value[i];
            }
        } else {
            for (int i = 0; i < size; i++) {
                parameters[getIndex(i)] += alpha_k * m_value[i];
                total[getIndex(i)] += upd * alpha_k * m_value[i]; //
            }
        }
    }
//	private static IntIntHash hm1;
//	private static IntIntHash hm2;
    private static TIntDoubleHashMap hd1;
    private static TIntDoubleHashMap hd2;

    public int dotProduct(FVR fl2) {

        if (hd1 == null) {
            hd1 = new TIntDoubleHashMap(size(), 0.4F);
        } else {
            hd1.clear();
        }

        addFeaturesToMap(hd1);

        if (hd2 == null) {
            hd2 = new TIntDoubleHashMap(fl2.size, 0.4F);
        } else {
            hd2.clear();
        }

        fl2.addFeaturesToMap(hd2);

        int[] keys = hd1.keys();

        int result = 0;
        for (int i = 0; i < keys.length; i++) {
            result += hd1.get(keys[i]) * hd2.get(keys[i]);
        }

        return result;
    }

    private void addFeaturesToMap(TIntDoubleHashMap map) {

        if (null != subfv1) {
            subfv1.addFeaturesToMap(map);

            if (null != subfv2) {
                subfv2.addFeaturesToMap(map, negateSecondSubFV);

            }
        }


        for (int i = 0; i < size; i++) {
            if (!map.adjustValue(getIndex(i), m_value[i])) {
                map.put(getIndex(i), m_value[i]);
            }
        }
    }

    private void addFeaturesToMap(IntIntHash map, boolean negate) {

        if (null != subfv1) {
            subfv1.addFeaturesToMap(map, negate);

            if (null != subfv2) {
                if (negate) {
                    subfv2.addFeaturesToMap(map, !negateSecondSubFV);
                } else {
                    subfv2.addFeaturesToMap(map, negateSecondSubFV);
                }
            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                if (!map.adjustValue(getIndex(i), -1)) {
                    map.put(getIndex(i), -1);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (!map.adjustValue(getIndex(i), 1)) {
                    map.put(getIndex(i), 1);
                }
            }
        }
    }

    private void addFeaturesToMap(TIntDoubleHashMap map, boolean negate) {

        if (null != subfv1) {
            subfv1.addFeaturesToMap(map, negate);

            if (null != subfv2) {
                if (negate) {
                    subfv2.addFeaturesToMap(map, !negateSecondSubFV);
                } else {
                    subfv2.addFeaturesToMap(map, negateSecondSubFV);
                }

            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                if (!map.adjustValue(getIndex(i), -m_value[i])) {
                    map.put(getIndex(i), -m_value[i]);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (!map.adjustValue(getIndex(i), m_value[i])) {
                    map.put(getIndex(i), m_value[i]);
                }
            }
        }


    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    private void toString(StringBuilder sb) {
        if (null != subfv1) {
            subfv1.toString(sb);

            if (null != subfv2) {
                subfv2.toString(sb);
            }
        }
        for (int i = 0; i < size; i++) {
            sb.append(getIndex(i)).append('=').append(m_value[i]).append(' ');
        }
    }

    public void writeKeys(DataOutputStream dos) throws IOException {

        //	int keys[] = keys();
        //	dos.writeInt(keys.length);
        //	for(int i=0;i<keys.length;i++) {
        //		dos.writeInt(keys[i]);
        //	}


        //int keys[] = keys();
        dos.writeInt(size);
        for (int i = 0; i < size; i++) {
            dos.writeInt(m_index[i]);
        }

    }

    /*
     *
     * final public static FVR cat(FVR f1,FVR f2) { if (f1==null) return f2; if
     * (f2==null) return f1; return new FVR(f1, f2); }
     *
     * final public static FVR cat(FVR f1,FVR f2, FVR f3) { return FVR.cat(f1,
     * FVR.cat(f2, f3)); } final public static FVR cat(FVR f1,FVR f2, FVR f3,
     * FVR f4) { return FVR.cat(f1, FVR.cat(f2, FVR.cat(f3, f4))); }
     */
    public static FVR read(DataInputStream dis) throws IOException {
        int cap = dis.readInt();
        if (cap == 0) {
            return null;
        }
        return new FVR(dis, cap);

    }

    /*
     * (non-Javadoc) @see is2.IFV#getScore()
     */
    @Override
    public double getScore() {
        Parser.out.println("not implemented");
        new Exception().printStackTrace();
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc) @see is2.IFV#clone()
     */
    @Override
    public IFV clone() {
        FVR f = new FVR(this.size);
        for (int i = 0; i < this.size; i++) {
            f.m_index[i] = m_index[i];
            f.m_value[i] = m_value[i];
        }
        f.size = this.size;
        return f;
    }
}