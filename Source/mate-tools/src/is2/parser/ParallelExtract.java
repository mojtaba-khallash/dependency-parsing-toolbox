package is2.parser;

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

    final DataFES d;
    // the data extractor does the actual work
    final Extractor extractor;
    private Instances is;
    private int i;
    private F2SF para;
    private Cluster cluster;

    public ParallelExtract(Extractor e, Instances is, int i, DataFES d, F2SF para, Cluster cluster) {

        this.is = is;
        extractor = e;
        this.d = d;
        this.i = i;
        this.para = para;
        this.cluster = cluster;
    }

    public static class DSet {

        int w1, w2;
    }

    @Override
    public Object call() {

        try {

            F2SF f = para;


            short[] pos = is.pposs[i];
            int length = pos.length;

            long[] gvs = new long[50];
            long[] svs = new long[220];

            while (true) {

                DSet set = get();
                if (set == null) {
                    break;
                }

                int w1 = set.w1;
                int w2 = set.w2;


                f.clear();
                extractor.basic(pos, w1, w2, f);
                d.pl[w1][w2] = f.getScoreF();


                f.clear();

                extractor.basic(pos, w2, w1, f);
                d.pl[w2][w1] = f.getScoreF();

                short[] labels = Edges.get(pos[w1], pos[w2]);
                float[] lab = d.lab[w1][w2];

                final Long2IntInterface li = extractor.li;

                int c = extractor.firstm(is, i, w1, w2, 0, cluster, svs);

                for (int l = 0; l < lab.length; l++) {
                    lab[l] = -100;
                }

                for (int l = 0; l < labels.length; l++) {
                    short label = labels[l];

                    f.clear();
                    int lv = extractor.d0.computeLabeValue(label, Extractor.s_type);
                    for (int k = 0; k < c; k++) {
                        if (svs[k] > 0) {
                            f.add(li.l2i(svs[k] + lv));
                        }
                    }


                    lab[label] = f.getScoreF();
                }

                labels = Edges.get(pos[w2], pos[w1]);
                lab = d.lab[w2][w1];

                for (int l = 0; l < lab.length; l++) {
                    lab[l] = -100;
                }


                for (int l = 0; l < labels.length; l++) {
                    int label = labels[l];

                    f.clear();
                    int lv = extractor.d0.computeLabeValue(label + Extractor.s_rel1, Extractor.s_type);
                    for (int k = 0; k < c; k++) {
                        if (svs[k] > 0) {
                            f.add(li.l2i(svs[k] + lv));
                        }
                    }

                    lab[label] = f.getScoreF();
                }

                int s = w1 < w2 ? w1 : w2;
                int e = w1 < w2 ? w2 : w1;


                for (int m = 0; m < length; m++) {

                    int g = (m == s || e == m) ? -1 : m;

                    int cn = extractor.second(is, i, w1, w2, g, 0, cluster, svs);
                    int cc = extractor.addClusterFeatures(is, i, w1, w2, g, cluster, 0, gvs, 0);
                    //for(int k=0;k<c;k++) dl1.map(f,svs[k]);


                    if (m >= w1) {
                        labels = Edges.get(pos[w1], pos[w2]);
                        float[] lab2 = new float[labels.length];
                        for (int l = 0; l < labels.length; l++) {

                            short label = labels[l];

                            int lx = label + Extractor.s_rel1 * (g < w2 ? 0 : 2);

                            f.clear();
                            int lv = extractor.d0.computeLabeValue(lx, Extractor.s_type);
                            for (int k = 0; k < cn; k++) {
                                if (svs[k] > 0) {
                                    f.add(li.l2i(svs[k] + lv));
                                }
                            }
                            for (int k = 0; k < cc; k++) {
                                if (gvs[k] > 0) {
                                    f.add(li.l2i(gvs[k] + lv));
                                }
                            }

                            lab2[l] = f.getScoreF();
                        }
                        d.gra[w1][w2][m] = lab2;
                    }


                    if (m <= w2) {
                        labels = Edges.get(pos[w2], pos[w1]);
                        float lab2[];
                        d.gra[w2][w1][m] = lab2 = new float[labels.length];
                        for (int l = 0; l < labels.length; l++) {

                            int label = labels[l];
                            int lx = label + Extractor.s_rel1 * (1 + (g < w1 ? 0 : 2));

                            f.clear();
                            int lv = extractor.d0.computeLabeValue(lx, Extractor.s_type);
                            for (int k = 0; k < cn; k++) {
                                if (svs[k] > 0) {
                                    f.add(li.l2i(svs[k] + lv));
                                }
                            }
                            for (int k = 0; k < cc; k++) {
                                if (gvs[k] > 0) {
                                    f.add(li.l2i(gvs[k] + lv));
                                }
                            }

                            lab2[l] = f.getScoreF();
                        }
                    }


                    g = (m == s || e == m) ? -1 : m;

                    //	int cn = extractor.second(is,i,w1,w2,g,0, cluster, svs,Extractor._SIB);
                    if (m >= w1 && m <= w2) {
                        labels = Edges.get(pos[w1], pos[w2]);
                        float lab2[] = new float[labels.length];
                        d.sib[w1][w2][m] = lab2;

                        for (int l = 0; l < labels.length; l++) {

                            short label = labels[l];

                            int lx = label + Extractor.s_rel1 * (8);
                            f.clear();
                            int lv = extractor.d0.computeLabeValue(lx, Extractor.s_type);
                            for (int k = 0; k < cn; k++) {
                                if (svs[k] > 0) {
                                    f.add(li.l2i(svs[k] + lv));
                                }
                            }
                            for (int k = 0; k < cc; k++) {
                                if (gvs[k] > 0) {
                                    f.add(li.l2i(gvs[k] + lv));
                                }
                            }


                            lab2[l] = (float) f.score;//f.getScoreF();
                        }
                    }
                    if (m >= w1 && m <= w2) {
                        labels = Edges.get(pos[w2], pos[w1]);
                        float[] lab2 = new float[labels.length];
                        d.sib[w2][w1][m] = lab2;
                        for (int l = 0; l < labels.length; l++) {

                            int label = labels[l];

                            int lx = label + Extractor.s_rel1 * (9);

                            f.clear();
                            int lv = extractor.d0.computeLabeValue(lx, Extractor.s_type);
                            for (int k = 0; k < cn; k++) {
                                if (svs[k] > 0) {
                                    f.add(li.l2i(svs[k] + lv));
                                }
                            }
                            for (int k = 0; k < cc; k++) {
                                if (gvs[k] > 0) {
                                    f.add(li.l2i(gvs[k] + lv));
                                }
                            }

                            lab2[l] = f.score;//f.getScoreF();
                        }
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