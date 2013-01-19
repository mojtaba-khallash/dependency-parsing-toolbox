package is2.data;

public interface Long2IntInterface {

    public abstract int size();

    /**
     * Maps a long to a integer value. This is very useful to save memory for
     * sparse data long values
     *
     * @param l
     * @return the integer
     */
    public abstract int l2i(long l);
}