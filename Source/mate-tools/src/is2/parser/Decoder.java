package is2.parser;

import is2.data.DataFES;
import is2.data.Parse;
import java.util.ArrayList;
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
    /**
     * Threshold for rearrange edges non-projective
     */
    public static float NON_PROJECTIVITY_THRESHOLD = 0.3F;
    static ExecutorService executerService = java.util.concurrent.Executors.newFixedThreadPool(Parser.THREADS);

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
    public static Parse decode(short[] pos, DataFES x, boolean projective, boolean training) throws InterruptedException {

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

        float bestSpanScore = (-1.0F / 0.0F);
        Closed bestSpan = null;
        for (int m = 1; m < n; m++) {
            if (C[0][n - 1][1][m].p > bestSpanScore) {
                bestSpanScore = C[0][n - 1][1][m].p;
                bestSpan = C[0][n - 1][1][m];
            }
        }

        // build the dependency tree from the chart 
        Parse out = new Parse(pos.length);

        bestSpan.create(out);

        out.heads[0] = -1;
        out.labels[0] = 0;

        timeDecotder += (System.nanoTime() - ts);

        ts = System.nanoTime();

        if (!projective) {
            rearrange(pos, out.heads, out.labels, x, training);
        }

        timeRearrange += (System.nanoTime() - ts);

        return out;
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
    public static void rearrange(short[] pos, short[] heads, short[] labs, DataFES x, boolean training) throws InterruptedException {

        int threads = (pos.length > Parser.THREADS) ? Parser.THREADS : pos.length;



        // wh  what to change, nPar - new parent, nType - new type
        short wh = -1, nPar = -1, nType = -1;
        ArrayList<ParallelRearrange> pe = new ArrayList<>();

        while (true) {
            boolean[][] isChild = new boolean[heads.length][heads.length];
            for (int i = 1, l1 = 1; i < heads.length; i++, l1 = i) {
                while ((l1 = heads[l1]) != -1) {
                    isChild[l1][i] = true;
                }
            }

            float max = Float.NEGATIVE_INFINITY;
            float p = Extractor.encode3(pos, heads, labs, x);

            pe.clear();
            for (int i = 0; i < threads; i++) {
                pe.add(new ParallelRearrange(isChild, pos, x, heads, labs));
            }

            for (int ch = 1; ch < heads.length; ch++) {

                for (short pa = 0; pa < heads.length; pa++) {
                    if (ch == pa || pa == heads[ch] || isChild[ch][pa]) {
                        continue;
                    }

                    ParallelRearrange.add(p, (short) ch, pa);
                }
            }
            executerService.invokeAll(pe);

            for (ParallelRearrange.PA rp : ParallelRearrange.order) {
                if (max < rp.max) {
                    max = rp.max;
                    wh = rp.wh;
                    nPar = rp.nPar;
                    nType = rp.nType;
                }
            }
            ParallelRearrange.order.clear();

            if (max <= NON_PROJECTIVITY_THRESHOLD) {
                break; // bb: changed from 0.0
            }
            heads[wh] = nPar;
            labs[wh] = nType;
        }
    }

    public static String getInfo() {
        return "Decoder non-projectivity threshold: " + NON_PROJECTIVITY_THRESHOLD;
    }
}