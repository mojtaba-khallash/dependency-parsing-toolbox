package is2.util;

import is2.data.Long2IntInterface;

/**
 * @author Bernd Bohnet, 01.09.2009
 *
 * Maps for the Hash Kernel the long values to the int values.
 */
final public class Long2Int implements Long2IntInterface {

    public Long2Int() {
        size = 115911564;
    }

    public Long2Int(int s) {
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

        // this works well LAS 88.138
        //	int r= (int)(( l ^ (l&0xffffffff00000000L) >>> 29 ));//0x811c9dc5 ^ // 29
        //	return Math.abs(r % size);
        // this works a bit better and good with 0x03ffffff 
        //	
		/*
         * long r= l;//26 l = (l>>12)&0xfffffffffffff000L; r ^= l;//38 l =
         * (l>>11)&0xffffffffffffc000L; r ^= l;//49 l = (l>>9)&
         * 0xffffffffffff0000L; //53 r ^= l;//58 l = (l>>7)&0xfffffffffffc0000L;
         * //62 r ^=l;//65 int x = (int)r; x = x % size; //	return x >= 0 ? x :
         * -x ;// Math.abs(r % size);
         *
         */
        //            26 0x03ffffff
        // together with 0x07ffffff 27 88.372
        long r = l;// 27
        l = (l >> 13) & 0xffffffffffffe000L;
        r ^= l;   // 40
        l = (l >> 11) & 0xffffffffffff0000L;
        r ^= l;   // 51
        l = (l >> 9) & 0xfffffffffffc0000L; //53
        r ^= l;  // 60
        l = (l >> 7) & 0xfffffffffff00000L; //62
        r ^= l;    //67
        int x = ((int) r) % size;

        return x >= 0 ? x : -x;
    }
}