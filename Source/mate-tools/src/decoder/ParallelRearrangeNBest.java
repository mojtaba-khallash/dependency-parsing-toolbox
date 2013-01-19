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
final public class ParallelRearrangeNBest implements Callable<Object> {

    // new parent child combination to explore
    final static class PA {

        final float p;
        final short ch, pa;
        float best;

        public PA(float p2, short ch2, short pa2) {
            p = p2;
            ch = ch2;
            pa = pa2;
        }
    }
    // list of parent child combinations
    private static ArrayList<PA> parents = new ArrayList<>();
    // some data from the dependency tree
    private short[] pos;
    private DataF x;
    private boolean[][] isChild;
    public short[] heads, types;
    private float lastNBest;
    private float best; // best so far 
    private float threshold;
    private Extractor extractor;

    /**
     * Initialize the parallel rearrange thread
     *
     * @param isChild2 is a child
     * @param edgesC the part-of-speech edge mapping
     * @param pos the part-of-speech
     * @param x the data
     * @param lastNBest
     * @param s the heads
     * @param ts the types
     */
    public ParallelRearrangeNBest(short[] pos, DataF x, Parse p, float lastNBest, Extractor extractor, float best, float threshold) {


        heads = p.heads;

        types = p.labels;

        isChild = new boolean[heads.length][heads.length];

        for (int i = 1, l1 = 1; i < heads.length; i++, l1 = i) {
            while ((l1 = heads[l1]) != -1) {
                isChild[l1][i] = true;
            }
        }


        this.lastNBest = lastNBest;
        this.pos = pos;
        this.x = x;

        this.extractor = extractor;
        this.best = best;
        this.threshold = threshold;
    }
    public ArrayList<ParseNBest> parses = new ArrayList<>();

    @Override
    public Object call() {

        // check the list of new possible parents and children for a better combination
        for (int ch = 1; ch < heads.length; ch++) {
            for (short pa = 0; pa < heads.length; pa++) {
                if (ch == pa || pa == heads[ch] || isChild[ch][pa]) {
                    continue;
                }

                short oldP = heads[ch], oldT = types[ch];
                heads[ch] = pa;

                short[] labels = Edges.get(pos[pa], pos[ch], ch < pa);

                for (int l = 0; l < labels.length; l++) {

                    types[ch] = labels[l];
                    float p_new = extractor.encode3(pos, heads, types, x);

                    if (p_new < lastNBest || ((best + this.threshold) > p_new)) {
                        continue;
                    }

                    ParseNBest p = new ParseNBest();
                    p.signature(heads, types);
                    p.f1 = p_new;
                    parses.add(p);
                }

                // change back
                heads[ch] = oldP;
                types[ch] = oldT;

                // consider changes to labels only
                labels = Edges.get(pos[oldP], pos[ch], ch < oldP);

                for (int l = 0; l < labels.length; l++) {

                    types[ch] = labels[l];
                    float p_new = (float) extractor.encode3(pos, heads, types, x);

                    // optimization: add only if larger than smallest of n-best
                    if (p_new < lastNBest || ((best + this.threshold) > p_new)) {
                        continue;
                    }

                    ParseNBest p = new ParseNBest();
                    p.signature(heads, types);
                    p.f1 = p_new;
                    parses.add(p);
                }

                heads[ch] = oldP;
                types[ch] = oldT;
            }
        }
        return parses;
    }
}