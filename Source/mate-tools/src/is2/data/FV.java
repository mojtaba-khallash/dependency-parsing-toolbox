package is2.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class FV extends IFV {

    private FV subfv1;
    private FV subfv2;
    private boolean negateSecondSubFV = false;
    private int size;
    // content of the nodes NxC
    private int m_index[];

    // type of the nodes NxT
    public FV() {
        this(10);
    }

    public FV(int initialCapacity) {
        m_index = new int[initialCapacity];
    }

    public FV(FV fv1, FV fv2) {
        subfv1 = fv1;
        subfv2 = fv2;
    }

    public FV(FV fv1, FV fv2, boolean negSecond) {
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
    public FV(DataInputStream dos, int capacity) throws IOException {
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
    public FV(DataInputStream dos) throws IOException {
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

            int newCapacity = (m_index.length * 3) / 2 + 1;


            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            m_index = new int[newCapacity];
            System.arraycopy(oldIndex, 0, m_index, 0, oldIndex.length);

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

    final public int createFeature(int i, double v) {

        ensureCapacity(size + 1);
        m_index[size] = i;
        size++;
        return size - 1;
    }

    final public int createFeature(int i) {

        ensureCapacity(size + 1);
        m_index[size] = i;
        size++;
        return size - 1;
    }

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
            size++;
        }
    }

    final public void add(int[] i) {

        for (int k = 0; k < i.length; k++) {
            add(i[k]);
        }

    }

    final public void put(int i, double f) {
        if (i >= 0) {
            createFeature(i, f);
        }
    }

    // fv1 - fv2
    public FV getDistVector(FV fl2) {
        return new FV(this, fl2, true);
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

        // warning changed the the value 

        if (negate) {
            for (int i = 0; i < size; i++) {
                score -= parameters[m_index[i]];//*m_value[i];
            }
        } else {
            for (int i = 0; i < size; i++) {
                score += parameters[m_index[i]];//*m_value[i];
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
                score -= parameters[m_index[i]];//*m_value[i];
            }
        } else {
            for (int i = 0; i < size; i++) {
                score += parameters[m_index[i]];//*m_value[i];
            }
        }
        return score;
    }

    public void update(double[] parameters, double[] total, double alpha_k, double upd) {
        update(parameters, total, alpha_k, upd, false);
    }

    public final void update(double[] parameters, double[] total, double alpha_k, double upd, boolean negate) {

        if (null != subfv1) {
            subfv1.update(parameters, total, alpha_k, upd, negate);

            if (null != subfv2) {
                if (negate) {
                    subfv2.update(parameters, total, alpha_k, upd, !negateSecondSubFV);
                } else {
                    subfv2.update(parameters, total, alpha_k, upd, negateSecondSubFV);
                }
            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                parameters[m_index[i]] -= alpha_k;//*getValue(i);
                total[m_index[i]] -= upd * alpha_k;//*getValue(i);
            }
        } else {
            for (int i = 0; i < size; i++) {
                parameters[m_index[i]] += alpha_k;//*getValue(i);
                total[m_index[i]] += upd * alpha_k;//*getValue(i); 
            }
        }


    }

    public final void update(short[] parameters, short[] total, double alpha_k, double upd, boolean negate) {

        if (null != subfv1) {
            subfv1.update(parameters, total, alpha_k, upd, negate);

            if (null != subfv2) {
                if (negate) {
                    subfv2.update(parameters, total, alpha_k, upd, !negateSecondSubFV);
                } else {
                    subfv2.update(parameters, total, alpha_k, upd, negateSecondSubFV);
                }
            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                parameters[m_index[i]] -= alpha_k;//*getValue(i);
                total[m_index[i]] -= upd * alpha_k;//*getValue(i);
            }
        } else {
            for (int i = 0; i < size; i++) {
                parameters[m_index[i]] += alpha_k;//*getValue(i);
                total[m_index[i]] += upd * alpha_k;//*getValue(i); 
            }
        }


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
                parameters[getIndex(i)] -= alpha_k;
                total[getIndex(i)] -= upd * alpha_k;
            }
        } else {
            for (int i = 0; i < size; i++) {
                parameters[getIndex(i)] += alpha_k;
                total[getIndex(i)] += upd * alpha_k; //
            }
        }


    }

    public final void update(float[] parameters, float[] total, double alpha_k,
            double upd, boolean negate, float[] totalp, Long2IntInterface li) {

        if (null != subfv1) {
            subfv1.update(parameters, total, alpha_k, upd, negate, totalp, li);

            if (null != subfv2 && negate) {
                subfv2.update(parameters, total, alpha_k, upd, !negateSecondSubFV, totalp, li);
            } else {
                subfv2.update(parameters, total, alpha_k, upd, negateSecondSubFV, totalp, li);
            }
        }

        if (negate) {
            for (int i = 0; i < size; i++) {
                parameters[getIndex(i)] -= alpha_k;
                total[getIndex(i)] -= upd * alpha_k;

                totalp[li.l2i(getIndex(i))] -= upd * alpha_k;
                //	totalp[getIndex(i)] -=upd*alpha_k;
            }
        } else {
            for (int i = 0; i < size; i++) {
                parameters[getIndex(i)] += alpha_k;
                total[getIndex(i)] += upd * alpha_k; //

                totalp[li.l2i(getIndex(i))] += upd * alpha_k;
                //	totalp[getIndex(i)] +=upd*alpha_k;
            }
        }
    }
    private static IntIntHash hm1;
    private static IntIntHash hm2;

    public int dotProduct(FV fl2) {

        if (hm1 == null) {
            hm1 = new IntIntHash(size(), 0.4F);
        } else {
            hm1.clear();
        }

        addFeaturesToMap(hm1);

        if (hm2 == null) {
            hm2 = new IntIntHash(fl2.size, 0.4F);
        } else {
            hm2.clear();
        }

        fl2.addFeaturesToMap(hm2);

        int[] keys = hm1.keys();

        int result = 0;
        for (int i = 0; i < keys.length; i++) {
            result += hm1.get(keys[i]) * hm2.get(keys[i]);
        }

        return result;

    }

    public double twoNorm(FV fl2) {

        if (hm1 == null) {
            hm1 = new IntIntHash(size(), 0.4F);
        } else {
            hm1.clear();
        }

        addFeaturesToMap(hm1);

        if (hm2 == null) {
            hm2 = new IntIntHash(fl2.size, 0.4F);
        } else {
            hm2.clear();
        }

        fl2.addFeaturesToMap(hm2);

        int[] keys = hm1.keys();

        int result = 0;
        for (int i = 0; i < keys.length; i++) {
            result += hm1.get(keys[i]) * hm2.get(keys[i]);
        }


        return Math.sqrt((double) result);


    }

    public void addFeaturesToMap(IntIntHash map) {

        if (null != subfv1) {
            subfv1.addFeaturesToMap(map);

            if (null != subfv2) {
                subfv2.addFeaturesToMap(map, negateSecondSubFV);

            }
        }


        for (int i = 0; i < size; i++) {
            if (!map.adjustValue(getIndex(i), 1)) {
                map.put(getIndex(i), 1);
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
            sb.append(getIndex(i)).append(' ');
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

    public void readKeys(DataInputStream dos) throws IOException {

        int keys = dos.readInt();
        for (int i = 0; i < keys; i++) {
            createFeature(dos.readInt(), 1.0);
        }


    }

    public static FV cat(FV f1, FV f2) {
        if (f1 == null) {
            return f2;
        }
        if (f2 == null) {
            return f1;
        }
        return new FV(f1, f2);
    }

    public static FV cat(FV f1, FV f2, FV f3) {
        return FV.cat(f1, FV.cat(f2, f3));
    }

    public static FV cat(FV f1, FV f2, FV f3, FV f4) {
        return FV.cat(f1, FV.cat(f2, FV.cat(f3, f4)));
    }

    public static FV read(DataInputStream dis) throws IOException {
        int cap = dis.readInt();
        if (cap == 0) {
            return null;
        }
        return new FV(dis, cap);

    }

    /*
     * (non-Javadoc) @see is2.IFV#getScore()
     */
    @Override
    public double getScore() {
        //System.out.println("not implemented");
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc) @see is2.IFV#clone()
     */
    @Override
    public IFV clone() {
        FV f = new FV(this.size);
        System.arraycopy(m_index, 0, f.m_index, 0, this.size);
        f.size = this.size;
        return f;
    }
}