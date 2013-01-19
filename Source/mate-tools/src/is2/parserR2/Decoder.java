package is2.parserR2;

import decoder.ParallelDecoder;
import decoder.ParallelRearrangeNBest;
import decoder.ParallelRearrangeNBest2;
import extractors.Extractor;
import is2.data.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Bernd Bohnet, 01.09.2009
 *
 * This methods do the actual work and they build the dependency trees.
 */
final public class Decoder {

    public static final boolean TRAINING = true;
    public static long timeDecotder;
    public static long timeRearrange;
    public static final boolean LAS = true;
    /**
     * Threshold for rearrange edges non-projective
     */
    public static float NON_PROJECTIVITY_THRESHOLD = 0.3F;
    public static ExecutorService executerService = java.util.concurrent.Executors.newFixedThreadPool(Parser.THREADS);

    // do not initialize
    private Decoder() {
    }

    /**
     * Build a dependency tree based on the data
     *
     * @param pos part-of-speech tags
     * @param x the data
     * @param projective projective or non-projective
     * @param edges the edges
     * @return a parse tree
     * @throws InterruptedException
     */
    public static List<ParseNBest> decode(short[] pos, DataF x, boolean projective, Extractor extractor) throws InterruptedException {

        long ts = System.nanoTime();

        if (executerService.isShutdown()) {
            executerService = java.util.concurrent.Executors.newCachedThreadPool();
        }
        final int n = pos.length;

        final Open O[][][][] = new Open[n][n][2][];
        final Closed C[][][][] = new Closed[n][n][2][];

        ArrayList<ParallelDecoder> pe = new ArrayList<>();

        for (int i = 0; i < Parser.THREADS; i++) {
            pe.add(new ParallelDecoder(pos, x, O, C, n));
        }

        for (short k = 1; k < n; k++) {

            // provide the threads the data
            for (short s = 0; s < n; s++) {
                short t = (short) (s + k);
                if (t >= n) {
                    break;
                }

                ParallelDecoder.add(s, t);
            }

            executerService.invokeAll(pe);
        }

        double bestSpanScore = (-1.0F / 0.0F);
        Closed bestSpan = null;
        for (int m = 1; m < n; m++) {
            if (C[0][n - 1][1][m].p > bestSpanScore) {
                bestSpanScore = C[0][n - 1][1][m].p;
                bestSpan = C[0][n - 1][1][m];
            }
        }

        // build the dependency tree from the chart 
        ParseNBest out = new ParseNBest(pos.length);

        bestSpan.create(out);

        out.heads[0] = -1;
        out.labels[0] = 0;
        bestProj = out;

        timeDecotder += (System.nanoTime() - ts);
        //	DB.println(""+out);

        ts = System.nanoTime();
        List<ParseNBest> parses;

        if (!projective) {

            //	if (training) 
            //		rearrange(pos, out.heads, out.types,x,training);
            //else { 
            //	DB.println("bestSpan score "+(float)bestSpan.p+" comp score "+Extractor.encode3(pos, out.heads, out.types, x));
            //	System.out.println();
            //	Parse best = new Parse(out.heads,out.types,Extractor.encode3(pos, out.heads, out.types, x));
            parses = rearrangeNBest(pos, out.heads, out.labels, x, extractor);
            //		DB.println("1best "+parses.get(0).f1);
            //		DB.println(""+parses.get(0).toString());


            //	for(ParseNBest p :parses) if (p.heads==null) p.signature2parse(p.signature());

            ///		if (parses.get(0).f1>(best.f1+NON_PROJECTIVITY_THRESHOLD)) out = parses.get(0);
            //		else out =best;

            //	}
        } else {
            parses = new ArrayList<>();
            parses.add(out);
        }
        timeRearrange += (System.nanoTime() - ts);

        return parses;
    }
    static Parse bestProj = null;

    /**
     * This is the parallel non-projective edge re-arranger
     *
     * @param pos part-of-speech tags
     * @param heads parent child relation
     * @param labs edge labels
     * @param x the data
     * @param edges the existing edges defined by part-of-speech tags
     * @throws InterruptedException
     */
    public static List<ParseNBest> rearrangeNBestP(short[] pos, short[] heads, short[] labs, DataF x, Extractor extractor) throws InterruptedException {

        ArrayList<ParallelRearrangeNBest2> pe = new ArrayList<>();

        int round = 0;
        ArrayList<ParseNBest> parses = new ArrayList<>();
        ParseNBest px = new ParseNBest();
        px.signature(heads, labs);
        //Object extractor;
        px.f1 = extractor.encode3(pos, heads, labs, x);
        parses.add(px);

        float lastNBest = Float.NEGATIVE_INFINITY;

        HashSet<Parse> done = new HashSet<>();
        gnu.trove.THashSet<CharSequence> contained = new gnu.trove.THashSet<>();

        while (true) {

            pe.clear();

            // used the first three parses
            int ic = 0, considered = 0;
            while (true) {

                if (parses.size() <= ic || considered > 11) {
                    break;
                }

                ParseNBest parse = parses.get(ic);

                ic++;
                // parse already extended
                if (done.contains(parse)) {
                    continue;
                }
                considered++;

                parse.signature2parse(parse.signature());

                done.add(parse);


                boolean[][] isChild = new boolean[heads.length][heads.length];

                for (int i = 1, l1 = 1; i < heads.length; i++, l1 = i) {
                    while ((l1 = heads[l1]) != -1) {
                        isChild[l1][i] = true;
                    }
                }


                // check the list of new possible parents and children for a better combination
                for (short ch = 1; ch < heads.length; ch++) {
                    for (short pa = 0; pa < heads.length; pa++) {
                        if (ch == pa || pa == heads[ch] || isChild[ch][pa]) {
                            continue;
                        }
                        ParallelRearrangeNBest2.add(parse.clone(), ch, pa);
                    }
                }

            }

            for (int t = 0; t < Parser.THREADS; t++) {
                pe.add(new ParallelRearrangeNBest2(pos, x, lastNBest, extractor, NON_PROJECTIVITY_THRESHOLD));
            }


            executerService.invokeAll(pe);

            // avoid to add parses several times
            for (ParallelRearrangeNBest2 rp : pe) {
                for (int k = rp.parses.size() - 1; k >= 0; k--) {
                    if (lastNBest > rp.parses.get(k).f1) {
                        continue;
                    }
                    CharSequence sig = rp.parses.get(k).signature();
                    if (!contained.contains(sig)) {
                        parses.add(rp.parses.get(k));
                        contained.add(sig);
                    }
                }
            }

            Collections.sort(parses);

            if (round >= 2) {
                break;
            }
            round++;

            // do not use to much memory
            if (parses.size() > Parser.NBest) {
                //			if (parses.get(Parser.NBest).f1>lastNBest) lastNBest = (float)parses.get(Parser.NBest).f1;
                parses.subList(Parser.NBest, parses.size() - 1).clear();
            }
        }
        return parses;
    }

    /**
     * This is the parallel non-projective edge re-arranger
     *
     * @param pos part-of-speech tags
     * @param heads parent child relation
     * @param labs edge labels
     * @param x the data
     * @param edges the existing edges defined by part-of-speech tags
     * @throws InterruptedException
     */
    public static List<ParseNBest> rearrangeNBest(short[] pos, short[] heads, short[] labs, DataF x, Extractor extractor) throws InterruptedException {

        ArrayList<ParallelRearrangeNBest> pe = new ArrayList<>();

        int round = 0;
        ArrayList<ParseNBest> parses = new ArrayList<>();
        ParseNBest px = new ParseNBest();
        px.signature(heads, labs);
        //Object extractor;
        px.f1 = extractor.encode3(pos, heads, labs, x);
        parses.add(px);

        float lastNBest = Float.NEGATIVE_INFINITY;

        HashSet<Parse> done = new HashSet<>();
        gnu.trove.THashSet<CharSequence> contained = new gnu.trove.THashSet<>();
        while (true) {

            pe.clear();

            // used the first three parses
            int i = 0;
            while (true) {

                if (parses.size() <= i || pe.size() > 12) {
                    break;
                }

                ParseNBest parse = parses.get(i);

                i++;

                // parse already extended
                if (done.contains(parse)) {
                    continue;
                }

//				DB.println("err "+parse.heads);

                parse.signature2parse(parse.signature());

                done.add(parse);
                pe.add(new ParallelRearrangeNBest(pos, x, parse, lastNBest, extractor, (float) parse.f1, NON_PROJECTIVITY_THRESHOLD));
            }

            executerService.invokeAll(pe);

            // avoid to add parses several times
            for (ParallelRearrangeNBest rp : pe) {
                for (int k = rp.parses.size() - 1; k >= 0; k--) {
                    if (lastNBest > rp.parses.get(k).f1) {
                        continue;
                    }
                    CharSequence sig = rp.parses.get(k).signature();
                    if (!contained.contains(sig)) {
                        parses.add(rp.parses.get(k));
                        contained.add(sig);
                    }
                }
            }

            Collections.sort(parses);

            if (round >= 2) {
                break;
            }
            round++;

            // do not use to much memory
            if (parses.size() > Parser.NBest) {
                if (parses.get(Parser.NBest).f1 > lastNBest) {
                    lastNBest = (float) parses.get(Parser.NBest).f1;
                }
                parses.subList(Parser.NBest, parses.size() - 1).clear();
            }
        }
        return parses;
    }

    public static String getInfo() {

        return "Decoder non-projectivity threshold: " + NON_PROJECTIVITY_THRESHOLD;
    }

    /**
     * @param parses
     * @param is
     * @param i
     * @return
     */
    public static int getGoldRank(List<ParseNBest> parses, Instances is, int i, boolean las) {

        for (int p = 0; p < parses.size(); p++) {

            if (parses.get(p).heads == null) {
                parses.get(p).signature2parse(parses.get(p).signature());
            }

            boolean eq = true;
            for (int w = 1; w < is.length(0); w++) {
                if (is.heads[i][w] != parses.get(p).heads[w] || (is.labels[i][w] != parses.get(p).labels[w] && las)) {
                    eq = false;
                    break;
                }
            }
            if (eq) {
                return p;
            }
        }
        return -1;
    }

    public static int getSmallestError(List<ParseNBest> parses, Instances is, int i, boolean las) {

        int smallest = -1;
        for (int p = 0; p < parses.size(); p++) {

            int err = 0;
            for (int w = 1; w < is.length(0); w++) {
                if (is.heads[i][w] != parses.get(p).heads[w] || (is.labels[i][w] != parses.get(p).labels[w] && las)) {
                    err++;
                }
            }
            if (smallest == -1 || smallest > err) {
                smallest = err;
            }
            if (smallest == 0) {
                return 0;
            }
        }
        return smallest;
    }

    public static int getError(ParseNBest parse, Instances is, int i, boolean las) {

        int err = 0;
        for (int w = 1; w < is.length(i); w++) {
            if (is.heads[i][w] != parse.heads[w] || (is.labels[i][w] != parse.labels[w] && las)) {
                err++;
            }
        }
        return err;
    }
}