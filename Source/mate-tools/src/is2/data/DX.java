/**
 *
 */
package is2.data;

/**
 * @author Dr. Bernd Bohnet, 30.08.2011
 *
 *
 */
public abstract class DX {

    public long a0, a1, a2, a3, a4, a5, a6, a7, a8, a9;
    public long v0, v1, v2, v3, v4, v5, v6, v7, v8, v9;

    public abstract void cz3();

    public abstract void cz4();

    public abstract void cz5();

    public abstract void cz6();

    public abstract void cz7();

    public abstract void cz8();

    public abstract void clean();

    public abstract long cs(int b, int v);

    public abstract long csa(int b, int v);

    public abstract void csa(int b, int v, IFV f);

    /**
     * @return
     */
    public abstract long getVal();

    /**
     * @param f
     * @param l
     */
    public abstract void map(IFV f, long l);

    /**
     * @param label
     * @param s_type
     * @return
     */
    public abstract int computeLabeValue(int label, int s_type);

    public abstract void fix();
}