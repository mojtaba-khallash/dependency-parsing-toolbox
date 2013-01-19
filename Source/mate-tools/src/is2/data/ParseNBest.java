package is2.data;

final public class ParseNBest extends Parse {

    private String signature = null;

    //public float[] scores;
    public ParseNBest() {
    }

    public ParseNBest(short[] heads2, short[] types2, float p_new) {
        super(heads2, types2, p_new);
    }

    public ParseNBest(int i) {
        super(i);
    }

    /**
     * @param sig
     * @param readFloat
     */
    public ParseNBest(String sig, float score) {
        super(sig, score);
    }

    /**
     * create a total order to provide replicable deterministic results
     *
     * @param o
     * @return
     */
    public int compareTo(ParseNBest o) {
        if (f1 < o.f1) {
            return 1;
        }
        if (f1 == o.f1) {
            if (signature == null) {
                signature = signature();
            }
            if (o.signature == null) {
                o.signature = o.signature();
            }
            return o.signature.compareTo(signature);

        }
        return -1;
    }

    /**
     * @return the signature of a parse
     */
    @Override
    public String signature() {
        if (signature != null) {
            return signature;
        }
        signature = super.signature();
        return signature;
    }

    /**
     * @return the signature of a parse
     */
    public String signature(short[] heads, short[] labels) {
        StringBuilder b = new StringBuilder(heads.length * 2);
        for (int k = 0; k < heads.length; k++) {
            b.append((char) heads[k]).
                    append((char) labels[k]);
        }
        signature = b.toString();
        return signature;
    }

    /**
     * @param heads
     * @param types
     * @param oldP
     * @param ch
     * @param s
     */
    public String signature(short[] heads, short[] types, short p, short ch, short l) {
        StringBuilder b = new StringBuilder(heads.length * 2);
        for (int k = 0; k < heads.length; k++) {


            b.append(k == ch ? (char) p
                    : (char) heads[k]).
                    append(k == ch ? (char) l : (char) types[k]);
        }
        signature = b.toString();
        return signature;
    }

    @Override
    public Parse clone() {
        ParseNBest p = new ParseNBest();
        p.heads = new short[heads.length];
        p.labels = new short[labels.length];

        System.arraycopy(heads, 0, p.heads, 0, heads.length);
        System.arraycopy(labels, 0, p.labels, 0, labels.length);

        p.f1 = f1;

        return p;
    }
}