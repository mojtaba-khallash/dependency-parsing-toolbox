package is2.data;

/**
 * @author Bernd Bohnet, 01.09.2009
 *
 * Maps for the Hash Kernel the long values to the int values.
 */
final public class Long2IntExact implements Long2IntInterface {

    static gnu.trove.TLongIntHashMap mapt = new gnu.trove.TLongIntHashMap();
    static int cnt = 0;

    public Long2IntExact() {
        size = 115911564;
    }

    public Long2IntExact(int s) {
        size = s;
    }
    /**
     * Integer counter for long2int
     */
    final private int size; //0x03ffffff //0x07ffffff

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
    final public void start() {
    }


    /*
     * (non-Javadoc) @see is2.sp09k9992.Long2IntIterface#l2i(long)
     */
    @Override
    final public int l2i(long l) {
        if (l < 0) {
            return -1;
        }

        int i = mapt.get(l);
        if (i != 0) {
            return i;
        }

        if (i == 0 && cnt < size - 1) {
            cnt++;
            mapt.put(l, cnt);
            return cnt;
        }
        return -1;
    }
}