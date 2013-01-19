package decoder;

import is2.data.Closed;
import is2.data.DataF;
import is2.data.Edges;
import is2.data.Open;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * @author Bernd Bohnet, 30.08.2009
 *
 * This class implements a parallel feature extractor.
 */
final public class ParallelDecoder implements Callable<Object> {
    // some constants

    private static final float INIT_BEST = (-1.0F / 0.0F);
    private static final boolean[] DIR = {false, true};
    // the data space of the weights for a dependency tree  
    final private DataF x;
    private short[] pos;
    private Open O[][][][];
    private Closed C[][][][];
    private int n;
    boolean done = false;
    public boolean waiting = false;

    /**
     * Initialize the parallel decoder.
     *
     * @param pos part-of-speech
     * @param d data
     * @param edges part-of-speech edge mapping
     * @param o open spans
     * @param c closed spans
     * @param n number of words
     */
    public ParallelDecoder(short[] pos, DataF d, Open o[][][][], Closed c[][][][], int n) {

        this.pos = pos;
        this.x = d;

        this.O = o;
        this.C = c;
        this.n = n;
    }

    private static class DSet {

        short w1, w2;
    }

    @Override
    public Object call() {

        while (true) {

            DSet set = get();
            if (done && set == null) {
                break;
            }

            if (set == null) {
                return null;
            }

            short s = set.w1, t = set.w2;

            for (short dir = 1; dir >= 0; dir--) {

                short[] labs = (dir == 1) ? Edges.get(pos[s], pos[t], false) : Edges.get(pos[t], pos[s], true);

                O[s][t][dir] = new Open[labs.length];
                for (int l = O[s][t][dir].length - 1; l >= 0; l--) {

                    double tRP = INIT_BEST;

                    Closed tL = null, tR = null;

                    for (int r = s; r < t; r++) {

                        if (s == 0 && r != 0) {
                            continue;
                        }

                        double tLPr = INIT_BEST, tRPr = INIT_BEST;
                        Closed tLCld = null, tRCld = null;

                        if (r == s) {
                            tLPr = dir == 1 ? x.sib[s][t][s][0][l] : x.gra[t][s][s][1][l];
                        } else {
                            for (int i = s + 1; i <= r; i++) {
                                if (((dir == 1 ? x.sib[s][t][i][0][l] : x.gra[t][s][i][1][l]) + C[s][r][1][i].p) > tLPr) {
                                    tLPr = ((dir == 1 ? x.sib[s][t][i][0][l] : x.gra[t][s][i][1][l]) + C[s][r][1][i].p);
                                    tLCld = C[s][r][1][i];
                                }
                            }
                        }

                        if (r == t - 1) {
                            tRPr = dir == 1 ? x.gra[s][t][s][0][l] : x.sib[t][s][s][1][l];
                        } else {
                            for (int i = r + 1; i < t; i++) {
                                if (((dir == 1 ? x.gra[s][t][i][0][l] : x.sib[t][s][i][1][l]) + C[r + 1][t][0][i].p) > tRPr) {
                                    tRPr = ((dir == 1 ? x.gra[s][t][i][0][l] : x.sib[t][s][i][1][l]) + C[r + 1][t][0][i].p);
                                    tRCld = C[r + 1][t][0][i];
                                }
                            }
                        }

                        if (tLPr + tRPr > tRP) {
                            tRP = tLPr + tRPr;
                            tL = tLCld;
                            tR = tRCld;
                        }
                    }
                    O[s][t][dir][l] = new Open(s, t, dir, labs[l], tL, tR,
                            (float) (tRP + ((dir == 1) ? x.pl[s][t] : x.pl[t][s]) + ((dir == 1) ? x.lab[s][t][labs[l]][0] : x.lab[t][s][labs[l]][1])));
                }
            }
            C[s][t][1] = new Closed[n];
            C[s][t][0] = new Closed[n];

            for (int m = s; m <= t; m++) {
                for (boolean d : DIR) {
                    if ((d && m != s) || !d && (m != t && s != 0)) {

                        // create closed structure

                        double top = INIT_BEST;

                        Open tU = null;
                        Closed tL = null;
                        int numLabels = O[(d ? s : m)][(d ? m : t)][d ? 1 : 0].length;

                        //for (int l = numLabels-1; l >=0; l--) {
                        for (int l = 0; l < numLabels; l++) {

                            Open hi = O[(d ? s : m)][(d ? m : t)][d ? 1 : 0][l];
                            for (int amb = m + (d ? 1 : -1); amb != (d ? t : s) + (d ? 1 : -1); amb += (d ? 1 : -1)) {

                                if ((hi.p + C[d ? m : s][d ? t : m][d ? 1 : 0][amb].p + x.gra[d ? s : t][m][amb][d ? 0 : 1][l]) > top) {
                                    top = (hi.p + C[d ? m : s][d ? t : m][d ? 1 : 0][amb].p + x.gra[d ? s : t][m][amb][(d ? 0 : 1)][l]);
                                    tU = hi;
                                    tL = C[d ? m : s][d ? t : m][d ? 1 : 0][amb];
                                }
                            }

                            if ((m == (d ? t : s)) && (hi.p + x.gra[d ? s : t][m][d ? s : t][(d ? 0 : 1)][l]) > top) {
                                top = (hi.p + x.gra[(d ? s : t)][m][d ? s : t][d ? 0 : 1][l]);
                                tU = hi;
                                tL = null;
                            }
                        }
                        C[s][t][d ? 1 : 0][m] = new Closed(s, t, m, d ? 1 : 0, tU, tL, (float) top);
                    }
                }
            }
        }
        return null;
    }
    public static ArrayList<DSet> sets = new ArrayList<>();

    static synchronized private DSet get() {
        synchronized (sets) {
            if (sets.isEmpty()) {
                return null;
            }
            return sets.remove(sets.size() - 1);
        }
    }

    public static void add(short w1, short w2) {
        DSet ds = new DSet();
        ds.w1 = w1;
        ds.w2 = w2;
        sets.add(ds);
    }
}