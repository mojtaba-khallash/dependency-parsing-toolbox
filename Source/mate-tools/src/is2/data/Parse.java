package is2.data;

import java.util.BitSet;

public class Parse implements Comparable<Parse> {

    public short[] heads;
    public short[] labels;
    public double f1;

    public Parse() {
    }

    public Parse(int i) {
        heads = new short[i];
        labels = new short[i];
    }

    /**
     * @param heads2
     * @param types2
     * @param p_new
     */
    public Parse(short[] heads2, short[] types2, float p_new) {
        this.heads = new short[heads2.length];
        this.labels = new short[types2.length];
        //	this.heads=heads2;
        //	this.labels=types2;
        System.arraycopy(heads2, 0, heads, 0, heads.length);
        System.arraycopy(types2, 0, labels, 0, labels.length);
        f1 = p_new;
    }

    /**
     * @param heads2
     * @param types2
     * @param p_new
     */
    public Parse(String parse, float p_new) {

        //	this(parse.length()/2);		

        signature2parse(parse);

        f1 = p_new;

    }

    public void signature2parse(String parse) {
        int p = 0;
        heads = new short[parse.length() / 2];
        labels = new short[heads.length];
        //	DB.println("pl "+parse.length());
        for (int k = 0; k < heads.length; k++) {
            heads[k] = (short) parse.charAt(p++);
            labels[k] = (short) parse.charAt(p++);
        }
    }

    @Override
    public Parse clone() {
        Parse p = new Parse();
        p.heads = new short[heads.length];
        p.labels = new short[labels.length];

        System.arraycopy(heads, 0, p.heads, 0, heads.length);
        System.arraycopy(labels, 0, p.labels, 0, labels.length);

        p.f1 = f1;

        return p;
    }

    /**
     * Check if it is a tree
     *
     * @return
     */
    public boolean checkTree() {

        BitSet set = new BitSet(heads.length);
        set.set(0);
        return checkTree(set, 0);
    }

    /**
     * @param set
     * @return
     */
    private boolean checkTree(BitSet set, int h) {
        //System.out.print(" h "+h);

        for (int i = 0; i < heads.length; i++) {
            if (heads[i] == h) {
                //	System.out.print(" "+i);
                if (!set.get(i)) {
                    checkTree(set, i);
                }
                set.set(i);
            }
        }

        for (int i = 0; i < heads.length; i++) {
            if (!set.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int k = 0; k < this.heads.length; k++) {
            b.append(k).append(" ").append(heads[k]).append(" ").append(this.labels[k]).append("\n");
        }
        return b.toString();
    }

    /*
     * (non-Javadoc) @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Parse o) {

        if (f1 == o.f1) {
            return this.signature().compareTo(o.signature());
        }
        return f1 < o.f1 ? 1 : f1 == o.f1 ? 0 : -1;
    }

    /**
     * @return the signature of a parse
     */
    public String signature() {
        StringBuilder b = new StringBuilder(heads.length * 2);
        for (int k = 0; k < heads.length; k++) {
            b.append((char) heads[k]).append((char) labels[k]);
        }
        return b.toString();
    }

    /**
     * @return the signature of a parse
     */
    public StringBuilder signatureSB() {
        StringBuilder b = new StringBuilder(heads.length * 2);
        for (int k = 0; k < heads.length; k++) {
            b.append((char) heads[k]).append((char) labels[k]);
        }
        return b;
    }
}