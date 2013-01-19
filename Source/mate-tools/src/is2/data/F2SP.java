package is2.data;

final public class F2SP extends IFV {

    final private float[] parameters;
    public double score = 0;

    public F2SP(float[] p) {
        parameters = p;
    }

    @Override
    final public void add(int i) {
        if (i > 0) {
            score += parameters[i];
        }
    }

    final public void add(int[] i) {
        for (int k = 0; k < i.length; k++) {
            if (i[k] > 0) {
                score += parameters[i[k]];
            }
        }
    }

    final public void sub(float[] px, int i, Long2IntInterface li) {

        if (i > 0) {
            score -= px[li.l2i(i)];
//			score -= px[i];
            //else score -=px[];
        }
    }

    @Override
    public void clear() {
        score = 0;
    }


    /*
     * (non-Javadoc) @see is2.IFV#getScore()
     */
    @Override
    public double getScore() {
        return score;
    }

    public double getScoreF() {
        return score;
    }

    /*
     * (non-Javadoc) @see is2.IFV#clone()
     */
    @Override
    public IFV clone() {
        return new F2SP(this.parameters);
    }

    /**
     * @param l2i
     */
    public void addRel(int i, float f) {
        if (i > 0) {
            score += parameters[i] * f;
        }
    }

    public int length() {
        return this.parameters.length;
    }
}