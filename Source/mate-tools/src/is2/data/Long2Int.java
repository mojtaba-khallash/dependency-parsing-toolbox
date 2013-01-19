package is2.data;

import is2.parser.Parser;

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

    public static void main(String args[]) {

        long l = 123456;
        long l2 = 1010119;
        Parser.out.println("l \t" + l + "\t" + printBits(l));

        long x = 100000000;
        Parser.out.println("1m\t" + l2 + "\t" + printBits(x) + "\t" + x);

        Parser.out.println("l2\t" + l2 + "\t" + printBits(l));

        Parser.out.println("l2*l\t" + l2 + "\t" + printBits(l * l2) + " \t " + l * l2);

        Parser.out.println("l2*l*l2\t" + l2 + "\t" + printBits(l * l2 * l2) + " \t " + l * l2 * l2);

        Parser.out.println("l2*l*l2\t" + l2 + "\t" + printBits(l * l2 * l2 * l2) + " \t " + l * l2 * l2 * l2);


        Parser.out.println("l2*l*l2\t" + l2 + "\t" + printBits((l * l2) % 0xfffff) + " \t " + l * l2 * l2 * l2 + "\t " + 0xfffff);
        Parser.out.println("l2*l*l2\t" + l2 + "\t" + printBits((l * l2) & 0xfffffff) + " \t " + l * l2 * l2 * l2);
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

    static public StringBuffer printBits(long out) {
        StringBuffer s = new StringBuffer();

        for (int k = 0; k < 65; k++) {
            s.append((out & 1) == 1 ? "1" : "0");
            out >>= 1;
        }
        s.reverse();
        return s;
    }
}