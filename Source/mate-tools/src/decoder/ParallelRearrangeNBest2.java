package decoder;

import extractors.Extractor;
import is2.data.DataF;
import is2.data.Edges;
import is2.data.Parse;
import is2.data.ParseNBest;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * @author Dr. Bernd Bohnet, 30.08.2009
 *
 * This class implements a parallel edge rearrangement for non-projective
 * parsing; The linear method was first suggest by Rayn McDonald et. al. 2005.
 */
final public class ParallelRearrangeNBest2 implements Callable<Object> {

    // new parent child combination to explore
    final static class PA {

        final float p;
        final short ch, pa;
        public short[] heads, types;

        public PA(Parse p, short ch2, short pa2) {
            this.p = (float) p.f1;
            heads = p.heads;
            types = p.labels;
            ch = ch2;
            pa = pa2;

        }
    }
    // list of parent child combinations
    private static ArrayList<PA> parents = new ArrayList<>();
    // some data from the dependency tree
    private short[] pos;
    private DataF x;
    private float lastNBest;
    private float threshold;
    private Extractor extractor;

    /**
     * Initialize the parallel rearrange thread
     *
     * @param pos the part-of-speech
     * @param x the data
     * @param lastNBest
     * @param isChild2 is a child
     * @param edgesC the part-of-speech edge mapping
     * @param s the heads
     * @param ts the types
     */
    public ParallelRearrangeNBest2(short[] pos, DataF x, float lastNBest, Extractor extractor, float threshold) {



        this.lastNBest = lastNBest;
        this.pos = pos;
        this.x = x;

        this.extractor = extractor;
        this.threshold = threshold;
    }
    public ArrayList<ParseNBest> parses = new ArrayList<>();

    @Override
    public Object call() {

        try {

            while (true) {
                PA p = getPA();

                if (p == null) {
                    return parses;
                }

                short oldP = p.heads[p.ch], oldT = p.types[p.ch];
                p.heads[p.ch] = p.pa;

                short[] labels = Edges.get(pos[p.pa], pos[p.ch], p.ch < p.pa);

                for (int l = 0; l < labels.length; l++) {

                    p.types[p.ch] = labels[l];
                    float p_new = extractor.encode3(pos, p.heads, p.types, x);

                    if (p_new < lastNBest || ((p.p + this.threshold) > p_new)) {
                        continue;
                    }

                    ParseNBest x = new ParseNBest();
                    x.signature(p.heads, p.types);
                    x.f1 = p_new;
                    parses.add(x);
                }

                // change back
                p.heads[p.ch] = oldP;
                p.types[p.ch] = oldT;

                // consider changes to labels only
                labels = Edges.get(pos[oldP], pos[p.ch], p.ch < oldP);

                for (int l = 0; l < labels.length; l++) {

                    p.types[p.ch] = labels[l];
                    float p_new = (float) extractor.encode3(pos, p.heads, p.types, x);

                    // optimization: add only if larger than smallest of n-best
                    if (p_new < lastNBest || ((p.p + this.threshold) > p_new)) {
                        continue;
                    }

                    ParseNBest x = new ParseNBest();
                    x.signature(p.heads, p.types);
                    x.f1 = p_new;
                    parses.add(x);
                }

                p.heads[p.ch] = oldP;
                p.types[p.ch] = oldT;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parses;
    }

    /**
     * Add a child-parent combination which are latter explored for
     * rearrangement
     *
     * @param p2
     * @param ch2
     * @param pa
     */
    public static void add(Parse p, short ch2, short pa) {
        parents.add(new PA(p, ch2, pa));
    }

    public static PA getPA() {
        synchronized (parents) {
            if (parents.isEmpty()) {
                return null;
            }
            return parents.remove(parents.size() - 1);
        }
    }
}