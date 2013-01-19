package is2.util;

final public class BloomFilter {

    int storage[];
    int size;
    int hits;

    public BloomFilter(int s, int h) {
        storage = new int[s];
        this.size = s * 32;
        this.hits = h;
    }

    public int computeHashCode(long value) {
        return (int) ((value ^ (value & 0xffffffff00000000L) >>> 27) * 5);//0x811c9dc5 ^ // 29
    }

    public boolean lookup(long h) {


        int x = Math.abs(computeHashCode(h) % size);
        int y = Math.abs(((int) h) % size);

        if ((storage[x >> 7] & (1 << (x & 31))) == 0) {
            return false;
        }

        for (int i = 0; i <= hits; i++) {
            x = Math.abs(x + y) % size;
            if ((storage[x >> 7] & (1 << (x & 31))) == 0) {
                return false;
            }
            y = (y + i) % size;
        }
        return true;
    }
    /*
     * public boolean lookup(int hash1, int hash2) { int x = hash1 % size; int
     * pos = x >> 7; int value = storage[pos]; if((value & (1 << (x & 31))) ==
     * 0) return false; int y = hash2 % size; for(int i = 0; i <= hits ; i++) {
     * x = (x + y) % size; if((value & (1 << (x & 31))) == 0) return false; y =
     * (y + i) % size; } return true; }
     *
     * public void put(int hash1, int hash2) { int x = hash1 % size; int pos = x
     * >> 7; int value = storage[pos ] | (1 << (x & 31));
     *
     * int y = hash2 % size; for(int i = 0; i <= hits ; i++) { x = (x + y) %
     * size; y = (y + i) % size; value |= (1 << (x & 31)); } storage[pos]=value;
     * }
     */

    public void put(long h) {

        int x = Math.abs(computeHashCode(h) % size);
        int y = Math.abs(((int) h) % size);

        storage[x >> 7] = storage[x >> 7] | (1 << (x & 31));

        for (int i = 0; i <= hits; i++) {
            x = (x + y) % size;
            y = (y + i) % size;
            storage[x >> 7] = storage[x >> 7] | (1 << (x & 31));
        }
    }
}