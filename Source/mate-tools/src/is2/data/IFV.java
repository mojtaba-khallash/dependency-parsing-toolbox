package is2.data;

public abstract class IFV {

    //  public double score=0;
    public abstract void add(int i);

    public abstract double getScore();

    public abstract void clear();

    @Override
    public abstract IFV clone();

    /**
     * @param gvs
     * @param li
     */
    public void add(long[] gvs, Long2IntInterface li, int l) {
        for (int k = 0; k < gvs.length; k++) {
            if (gvs[k] == Integer.MIN_VALUE) {
                break;
            }
            if (gvs[k] > 0) {
                add(li.l2i(gvs[k] + l));
            }
        }
    }
}