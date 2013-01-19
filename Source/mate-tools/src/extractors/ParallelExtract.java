package extractors;

import is2.data.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * @author Bernd Bohnet, 30.08.2009
 *
 * This class implements a parallel feature extractor.
 */
final public class ParallelExtract implements Callable<Object> {
    // the data space of the weights for a dependency tree  

    final DataF d;
    // the data extractor does the actual work
    final Extractor extractor;
    private Instances is;
    private int i;
    private F2SF para;
    private Cluster cluster;
    private Long2IntInterface li;

    public ParallelExtract(Extractor e, Instances is, int i, DataF d, F2SF para, Cluster cluster, Long2IntInterface li) {

        this.is = is;
        extractor = e;
        this.d = d;
        this.i = i;
        this.para = para;
        this.cluster = cluster;
        this.li = li;
    }

    public static class DSet {

        int w1, w2;
    }

    @Override
    public Object call() {

        try {

            F2SF f = para;


            short[] pos = is.pposs[i];
            int[] forms = is.forms[i];
            int[] lemmas = is.plemmas[i];
            short[][] feats = is.feats[i];
            int length = pos.length;

            long[] svs = new long[250];

            int type = extractor.getType();

            while (true) {

                DSet set = get();
                if (set == null) {
                    break;
                }

                int w1 = set.w1;
                int w2 = set.w2;

                f.clear();
                int n = extractor.basic(pos, forms, w1, w2, cluster, f);
                d.pl[w1][w2] = f.getScoreF();

                short[] labels = Edges.get(pos[w1], pos[w2], false);
                float[][] lab = d.lab[w1][w2];

                extractor.firstm(is, i, w1, w2, 0, cluster, svs);

                if (labels != null) {


                    for (int l = labels.length - 1; l >= 0; l--) {

                        short label = labels[l];

                        f.clear();
                        for (int k = svs.length - 1; k >= 0; k--) {
                            if (svs[k] > 0) {
                                f.add(li.l2i(svs[k] + label * type));
                            }
                        }
                        lab[label][0] = f.getScoreF();
                    }
                }

                labels = Edges.get(pos[w1], pos[w2], true);

                if (labels != null) {

                    for (int l = labels.length - 1; l >= 0; l--) {

                        int label = labels[l];
                        f.clear();
                        for (int k = svs.length - 1; k >= 0; k--) {
                            if (svs[k] > 0) {
                                f.add(li.l2i(svs[k] + label * type));
                            }
                        }
                        lab[label][1] = f.getScoreF();
                    }
                }

                int s = w1 < w2 ? w1 : w2;
                int e = w1 < w2 ? w2 : w1;

                int sg = w1 < w2 ? w1 : 0;
                int eg = w1 < w2 ? length : w1 + 1;


                for (int m = s; m < e; m++) {
                    for (int dir = 0; dir < 2; dir++) {
                        labels = Edges.get(pos[w1], pos[w2], dir == 1);
                        float lab2[] = new float[labels.length];

                        int g = (m == s || e == m) ? -1 : m;


                        extractor.siblingm(is, i, pos, forms, lemmas, feats, w1, w2, g, 0, cluster, svs, n);

                        for (int l = labels.length - 1; l >= 0; l--) {

                            int label = labels[l];
                            f.clear();

                            for (int k = svs.length - 1; k >= 0; k--) {
                                if (svs[k] > 0) {
                                    f.add(li.l2i(svs[k] + label * type));
                                }
                            }
                            lab2[l] = (float) f.score;//f.getScoreF();
                        }
                        d.sib[w1][w2][m][dir] = lab2;
                    }
                }

                for (int m = sg; m < eg; m++) {
                    for (int dir = 0; dir < 2; dir++) {
                        labels = Edges.get(pos[w1], pos[w2], dir == 1);
                        float[] lab2 = new float[labels.length];

                        int g = (m == s || e == m) ? -1 : m;

                        extractor.gcm(is, i, w1, w2, g, 0, cluster, svs);

                        for (int l = labels.length - 1; l >= 0; l--) {

                            int label = labels[l];

                            f.clear();
                            for (int k = svs.length - 1; k >= 0; k--) {
                                if (svs[k] > 0) {
                                    f.add(li.l2i(svs[k] + label * type));
                                }
                            }
                            lab2[l] = f.getScoreF();
                        }
                        d.gra[w1][w2][m][dir] = lab2;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    static ArrayList<DSet> sets = new ArrayList<>();

    private DSet get() {

        synchronized (sets) {
            if (sets.isEmpty()) {
                return null;
            }
            return sets.remove(sets.size() - 1);
        }
    }

    static public void add(int w1, int w2) {
        DSet ds = new DSet();
        ds.w1 = w1;
        ds.w2 = w2;
        sets.add(ds);
    }
}