package is2.parser;

import is2.data.DX;
import is2.data.IFV;
import is2.data.Long2IntInterface;

/**
 * @author Dr. Bernd Bohnet, 30.10.2010
 *
 *
 */
final public class D5 extends DX {

    public long shift;
    private long h;

    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz2()
     */
    final public void cz2() {

        if (v0 < 0 || v1 < 0) {
            shift = 0;
            h = -1;
            return;
        }

        h = v0 | v1 << (shift = a0);
        shift += a1;
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz3()
     */
    @Override
    final public void cz3() {

        if (v0 < 0 || v1 < 0 || v2 < 0) {
            shift = 0;
            h = -1;
            return;

        }

        h = v0 | v1 << (shift = a0) | v2 << (shift += a1);
        shift = shift + a2;

    }


    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz4()
     */
    @Override
    final public void cz4() {
        if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0) {
            shift = 0;
            h = -1;
            return;
        }

        h = v0 | v1 << (shift = a0) | v2 << (shift += a1) | v3 << (shift += a2);
        shift = shift + a3;
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz5()
     */
    @Override
    final public void cz5() {

        if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0) {
            shift = 0;
            h = -1;
            return;
        }

        h = v0 | v1 << (shift = a0) | v2 << (shift += a1) | v3 << (shift += a2) | v4 << (shift += a3);
        shift = shift + a4;
    }


    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz6()
     */
    @Override
    final public void cz6() {

        if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0 || v5 < 0) {
            shift = 0;
            h = -1;
            return;
        }

        h = v0 | v1 << (shift = a0) | v2 << (shift += a1) | v3 << (shift += a2) | v4 << (shift += a3) | v5 << (shift += a4);
        shift = shift + a5;
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz7()
     */
    @Override
    final public void cz7() {

        if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0 || v5 < 0 || v6 < 0) {
            shift = 0;
            h = -1;
            return;
        }

        h = v0 | v1 << (shift = a0) | v2 << (shift += a1) | v3 << (shift += a2) | v4 << (shift += a3) | v5 << (shift += a4) | v6 << (shift += a5);
        shift = shift + a6;
    }


    /*
     * (non-Javadoc) @see is2.parser52L.DX#cz8()
     */
    @Override
    final public void cz8() {

        if (v0 < 0 || v1 < 0 || v2 < 0 || v3 < 0 || v4 < 0 || v5 < 0 || v6 < 0 || v7 < 0) {
            h = -1;
            shift = 0;
            return;
        }

        h = v0 | v1 << (shift = a0) | v2 << (shift += a1) | v3 << (shift += a2) | v4 << (shift += a3) | v5 << (shift += a4) | v6 << (shift += a5) | v7 << (shift += a6);
        shift = shift + a7;
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#clean()
     */
    @Override
    final public void clean() {
        v0 = 0;
        v1 = 0;
        v2 = 0;
        v3 = 0;
        v4 = 0;
        v5 = 0;
        v6 = 0;
        v7 = 0;
        v8 = 0;
        shift = 0;
        h = 0;
    }
    public final Long2IntInterface _li;

    public D5(Long2IntInterface li) {
        _li = li;
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#cs(int, int)
     */
    @Override
    final public long cs(int b, int v) {
        if (h < 0) {
            h = -1;
            shift = 0;
            return -1;
        }

        h |= (long) v << shift;
        shift += b;
        if (shift > 64) {
            Parser.out.println("shift too large " + shift);
            new Exception().printStackTrace();
        }

        return h;
    }
    /*
     * (non-Javadoc) @see is2.parser52L.DX#csa(int, int)
     */

    @Override
    final public long csa(int b, int v) {
        if (h < 0) {
            h = -1;
            shift = 0;
            return -1;
        }

        h |= (long) v << shift;
        shift += b;
        if (shift > 64) {
            Parser.out.println("shift too large " + shift);
            new Exception().printStackTrace();
        }

        return h;

    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#csa(int, int, is2.data.IFV)
     */
    @Override
    final public void csa(int b, int v, IFV f) {
        if (h < 0) {
            h = -1;
            shift = 0;
            return;
        }

        h |= (long) v << shift;
        shift += b;
        if (shift > 64) {
            Parser.out.println("shift too large " + shift);
            new Exception().printStackTrace();
        }

        f.add((int) _li.l2i(h));
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#getVal()
     */
    @Override
    public long getVal() {
        if (h < 0) {
            h = -1;
            shift = 0;
            return h;
        }
        return h;
    }

    /*
     * (non-Javadoc) @see is2.parser52L.DX#map(is2.data.IFV, long)
     */
    @Override
    public void map(IFV f, long l) {
        if (l > 0) {
            f.add(_li.l2i(l));
        }
    }

    /*
     * (non-Javadoc) @see is2.data.DX#computeLabeValue(short, short)
     */
    @Override
    public int computeLabeValue(int label, int shift) {
        return label << shift;
    }

    @Override
    public void fix() {
    }
}