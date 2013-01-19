package is2.data;

/**
 * @author Bernd Bohnet, 01.09.2009
 *
 * Maps for the Hash Kernel the long values to the int values.
 */
final public class Long2IntQuick implements Long2IntInterface {

    /**
     * Integer counter for long2int
     */
    final private int size;

    public Long2IntQuick() {
        size = 0x07ffffff;
    }

    public Long2IntQuick(int s) {
        size = s;
    }

    /*
     * (non-Javadoc) @see is2.sp09k9992.Long2IntIterface#size()
     */
    @Override
    public int size() {
        return size;
    }

    /*
     * (non-Javadoc) @see is2.sp09k9992.Long2IntIterface#start() has no meaning
     * for this implementation
     */
    /*
     * (non-Javadoc) @see is2.sp09k9992.Long2IntIterface#l2i(long)
     */
    @Override
    final public int l2i(long r) {
        long l = (r >> 16) & 0xfffffffffffff000L;
        r ^= l;
        r ^= l = (l >> 12) & 0xffffffffffff0000L;
        r ^= l = (l >> 8) & 0xfffffffffffc0000L;
        return (int) (r % size);
    }
}