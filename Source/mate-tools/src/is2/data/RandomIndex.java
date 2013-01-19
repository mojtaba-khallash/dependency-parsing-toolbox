/**
 *
 */
package is2.data;

import is2.util.DB;

/**
 * @author Dr. Bernd Bohnet, 20.05.2011
 *
 *
 */
public class RandomIndex implements Long2IntInterface {

    final int[] prims = {52349171, 199951347, 89990, 5001, 32891, 17, 19, 23, 29, 31, 37, 47, 53, 59, 61, 67, 71};
//	final int[] prims = {1,3,5,7,11,17,19,23,29,31,37,47,53,59,61,67,71};
    final long hashFunctionModifiers[];
    final int kbit, lbit;
    final int hsize; // maximal size of hash
    final int bits; // available bits
    final int moves; // needed moves to put a number into  

    /**
     * Creates the random functions.
     *
     * @param kbit The bits to be mapped
     * @param lbit The left shift of the bits
     * @param hsize The size of the featurs space (not included in the original
     * algorithm)
     * @param numberFunctions The number of the hash functions
     */
    public RandomIndex(int kbit, int lbit, int hsize, int numberFunctions) {


        this.kbit = kbit;
        this.lbit = lbit;


        if (hsize <= 0) {
            this.hsize = 67000001; // default value 
        } else {
            this.hsize = hsize;
        }

        bits = (int) Math.ceil(Math.log(this.hsize) / Math.log(2));

        moves = (int) Math.ceil(64f / (float) bits);



        DB.println("moves " + moves + " bits " + bits + " hsize " + hsize);

        hashFunctionModifiers = new long[numberFunctions];

        for (int f = 0; f < numberFunctions; f++) {
            hashFunctionModifiers[f] = prims[f];
        }
    }

    public int[] hash(long x) {
        int[] hvals = new int[hashFunctionModifiers.length];

        for (int k = 0; k < hashFunctionModifiers.length; k++) {

            // the original function: value = ((x+1) * hashFunctionModifiers[k] & m ) >> n;

            // the first part of the original function
            long value = (x + 1) * hashFunctionModifiers[k];

            // do the above >> n with a maximal size of the available hash values
            // Shift all bits until they have been each xor-ed (^) in the range of the hash
            // in order the have all information potentially represented there.

            for (int j = 1; j <= moves; j++) {
                value = value ^ (value >> (bits * j));
            }

            // Map the value to the range of the available space should be the same as (value & m) . 
            hvals[k] = Math.abs((int) value % hsize);
        }
        return hvals;
    }

    public int[] hashU(long x) {
        int[] hvals = new int[hashFunctionModifiers.length];

        long y = Long.reverse(x);
        for (int k = 0; k < hashFunctionModifiers.length; k++) {

            // the original function: value = ((x+1) * hashFunctionModifiers[k] & m ) >> n;

            // the first part of the original function
            long value1 = (((y + 1) * hashFunctionModifiers[k]) /*
                     * % 2 pow 64
                     */) >> (kbit - lbit);

            // I get probably only the first part lets get the second part too
            //	long value2 = (((y+1>>20) * hashFunctionModifiers[k]) /* % 2 pow 64 */  ) >> (kbit-lbit);


            // the modulo (%)  2 pow 64 is done since the long number can not be larger than 2 pow 64.  
            //	System.out.println("value "+value+" shift "+(lbit-kbit));
            hvals[k] = Math.abs((int) value1);
        }
        return hvals;
    }

    /*
     * (defun generate-hash-fn (&key (k-bit 32) (l-bit 8) verbosep constants
     * (count 4))
     *
     * (labels ((random-constant () (let ((a (+ (random (- (expt 2 k-bit) 1))
     * 1))) (logior a 1)))) ;; inclusive OR ensures odd number. (let ((pdiff (-
     * (- k-bit l-bit)));; neg. sign to do a rightshift, see ash() (sub1 (-
     * (expt 2 k-bit) 1)) (constants (copy-list constants))) (unless constants
     * (loop ;; a = odd number a where 0 < a < u. until (= count (length
     * constants)) do (pushnew (random-constant) constants))) (when verbosep
     * (format t "~&generate-hash-fn(): using random constants: ~a~%"
     * constants)) (values #'(lambda (x) (loop for a in constants ;;; always add
     * 1 to x to avoid f(0)=0. collect (ash (logand (* (+ 1 x) a) sub1) pdiff)))
     * constants))))
     *
     */
    /*
     * (non-Javadoc) @see is2.data.Long2IntInterface#l2i(long)
     */
    @Override
    public int l2i(long l) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc) @see is2.data.Long2IntInterface#size()
     */
    @Override
    public int size() {
        return hsize;
    }
}