package is2.data;

final public class Closed {

    public double p;
    short b, e, m;
    byte dir;
    Closed d;
    Open u;

    public Closed(short s, short t, int m, int dir, Open u, Closed d, float score) {
        this.b = s;
        this.e = t;
        this.m = (short) m;
        this.dir = (byte) dir;
        this.u = u;
        this.d = d;
        p = score;
    }

    public void create(Parse parse) {
        if (u != null) {
            u.create(parse);
        }
        if (d != null) {
            d.create(parse);
        }
    }
}