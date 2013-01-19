package extractors;

import is2.data.*;
import is2.util.DB;
import java.util.Arrays;

final public class ExtractorReranker {

    public static int s_rel, s_word, s_type, s_dir, s_dist, s_feat, s_child, s_spath, s_lpath, s_pos;
    public static int d0, d1, d2, d3, d4, d5, d10;
    MFB mf;
    final D4 dl1, dl2, dwr, dr, dwwp, dw, dwp, dlf, d3lp, d2lp, d2pw, d2pp;
    public final Long2IntInterface li;

    public ExtractorReranker(Long2IntInterface li) {
        this.li = li;
        dl1 = new D4(li);
        dl2 = new D4(li);
        dwr = new D4(li);
        dr = new D4(li);
        dwwp = new D4(li);

        dw = new D4(li);
        dwp = new D4(li);

        dlf = new D4(li);
        d3lp = new D4(li);
        d2lp = new D4(li);
        d2pw = new D4(li);
        d2pp = new D4(li);

    }

    public static void initStat() {
        DB.println("init called ");
        MFB mf = new MFB();
        s_rel = mf.getFeatureCounter().get(REL).intValue();
        s_pos = mf.getFeatureCounter().get(POS).intValue();
        s_word = mf.getFeatureCounter().get(WORD).intValue();
        s_type = mf.getFeatureCounter().get(TYPE).intValue();//mf.getFeatureBits();
        s_dir = mf.getFeatureCounter().get(DIR);
        la = mf.getValue(DIR, LA);
        ra = mf.getValue(DIR, RA);
        s_dist = mf.getFeatureCounter().get(DIST);//mf.getFeatureBits(DIST);
        s_feat = mf.getFeatureCounter().get(FEAT);//mf.getFeatureBits(Pipe.FEAT);
        s_spath = mf.getFeatureCounter().get(Cluster.SPATH) == null ? 0 : mf.getFeatureCounter().get(Cluster.SPATH);//mf.getFeatureBits(Cluster.SPATH);
        s_lpath = mf.getFeatureCounter().get(Cluster.LPATH) == null ? 0 : mf.getFeatureCounter().get(Cluster.LPATH);//mf.getFeatureBits(Cluster.LPATH);
    }

    public void init() {
        mf = new MFB();

        dl1.a0 = s_type;
        dl1.a1 = 3;
        dl1.a2 = s_pos;
        dl1.a3 = s_pos;
        dl1.a4 = s_pos;
        dl1.a5 = s_pos;
        dl1.a6 = s_pos;
        dl1.a7 = s_pos;
        dl2.a0 = s_type;
        dl2.a1 = 3;
        dl2.a2 = s_rel;
        dl2.a3 = s_rel;
        dl2.a4 = s_rel;
        dl2.a5 = s_rel;
        dl2.a6 = s_rel;
        dl2.a7 = s_rel;
        dl2.a8 = s_rel;
        dl2.a9 = s_rel;
        dwp.a0 = s_type;
        dwp.a1 = 3;
        dwp.a2 = s_word;
        dwp.a3 = s_rel;
        dwp.a4 = s_rel;
        dwp.a5 = s_rel;
        dwp.a6 = s_rel;
        dwp.a7 = s_rel;
        dwwp.a0 = s_type;
        dwwp.a1 = 3;
        dwwp.a2 = s_word;
        dwwp.a3 = s_word;
        dwwp.a4 = s_pos;
        dwwp.a5 = s_word;
        dwwp.a6 = s_pos;
        dwwp.a7 = s_pos;
    }
    public static final String REL = "REL", END = "END", STR = "STR", LA = "LA", RA = "RA", FEAT = "F";
    private static int ra, la;
    private static int s_str;
    private static int s_end, _cend, _cstr, s_stwrd, s_relend;
    protected static final String TYPE = "TYPE", DIR = "D";
    public static final String POS = "POS";
    protected static final String DIST = "DIST", MID = "MID";
    private static final String _0 = "0", _4 = "4", _3 = "3", _2 = "2", _1 = "1", _5 = "5", _10 = "10";
    private static final String WORD = "WORD", STWRD = "STWRD", STPOS = "STPOS";
    private static int nofeat;
    public static int maxForm;
    final public static int _FC = 60;

    /**
     * Initialize the features.
     *
     * @param maxFeatures
     */
    static public void initFeatures() {


        MFB mf = new MFB();
        mf.register(POS, MID);
        s_str = mf.register(POS, STR);
        s_end = mf.register(POS, END);

        s_relend = mf.register(REL, END);

        _cstr = mf.register(Cluster.SPATH, STR);
        _cend = mf.register(Cluster.SPATH, END);


        mf.register(TYPE, POS);

        s_stwrd = mf.register(WORD, STWRD);
        mf.register(POS, STPOS);

        la = mf.register(DIR, LA);
        ra = mf.register(DIR, RA);

        //		mf.register(TYPE, CHAR);

        mf.register(TYPE, FEAT);
        nofeat = mf.register(FEAT, "NOFEAT");

        for (int k = 0; k < 60; k++) {
            mf.register(TYPE, "F" + k);
        }


        d0 = mf.register(DIST, _0);
        d1 = mf.register(DIST, _1);
        d2 = mf.register(DIST, _2);
        d3 = mf.register(DIST, _3);
        d4 = mf.register(DIST, _4);
        d5 = mf.register(DIST, _5);
        //		d5l=mf.register(DIST, _5l);
        d10 = mf.register(DIST, _10);


    }

    /**
     * @param is
     * @param n
     * @param parseNBest
     * @param vs
     */
    public void extractFeatures3(Instances is, int i, ParseNBest parse, int rank, long[] v) {

        int f = 1, n = 0;

        for (short k = 0; k < is.length(i) - 1; k++) {

            short[] chld = children(parse.heads, k);

            f = 2;

            int fm = is.forms[i][k];
            int hh = k != 0 ? is.pposs[i][parse.heads[k]] : s_end;
            int h = is.pposs[i][k];
            int hrel = parse.labels[k];
            int hhrel = k != 0 ? parse.labels[parse.heads[k]] : s_relend;
            int hhf = k != 0 ? is.forms[i][parse.heads[k]] : s_stwrd;



            int rlast = chld.length > 0 ? parse.labels[chld[chld.length - 1]] : s_relend;

            int[] rels = new int[chld.length];
            int[] pss = new int[chld.length];
            for (int j = 0; j < chld.length; j++) {
                rels[j] = parse.labels[chld[j]];
                pss[j] = is.pposs[i][chld[j]];
            }

            StringBuilder rl = new StringBuilder(chld.length);
            StringBuilder psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }

            int rli = mf.register("rli", rl.toString());
            int pli = mf.register("pli", psl.toString());

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();



            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwp.v0 = f++;
            dwp.v2 = rli;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();

            Arrays.sort(rels);
            Arrays.sort(pss);

            rl = new StringBuilder(chld.length);
            psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }
            rli = mf.register("rli", rl.toString());
            pli = mf.register("pli", psl.toString());


            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dl1.v0 = f++;
            dl1.v2 = h;
            dl1.v3 = hrel;
            dl1.v4 = hhrel;
            dl1.v5 = hh;
            dl1.v6 = rlast;
            dl1.cz6();
            v[n++] = dl1.getVal();
            dwp.v0 = f++;
            dwp.v2 = fm;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.cz5();
            v[n++] = dwp.getVal();
            dwp.v0 = f++;
            dwp.v2 = hhf;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = hhf;
            dwwp.v4 = hrel;
            dwwp.v5 = hhrel;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = h;
            dwwp.v3 = hhf;
            dwwp.v4 = hrel;
            dwwp.v5 = hhrel;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = hh;
            dwwp.v4 = hrel;
            dwwp.v5 = hhrel;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = hhf;
            dwwp.v4 = h;
            dwwp.v5 = hh;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = h;
            dwwp.v3 = hhf;
            dwwp.v4 = hrel;
            dwwp.v5 = hh;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.v5 = hrel;
            dwwp.cz6();
            v[n++] = dwwp.getVal();


            //		dl1.v0= f++; dl1.v2=h;dl1.v3=hrel; dl1.v4=hhrel;dl1.v5=hh; dl1.v6=hhhrel;dl1.v7=hhh; dl1.v8=rlast; dl1.cz9(); v[n++]=dl1.getVal(); 
//			dl1.v0= f++; dl1.v2=h;dl1.v3=hrel; dl1.v4=hhrel;dl1.v5=hh; dl1.v6=hhhrel;dl1.v7=hhh; dl1.v8=rlast; dl1.cz9(); v[n++]=dl1.getVal(); 
            //	dl1.v0= f++; dl1.v2=h;dl1.v3=hrel; dl1.v4=dir;dl1.v5=hh; dl1.v6=hhh;dl1.v7=rlast; dl1.v8=r1; dl1.cz9(); v[n++]=dl1.getVal();
            //	dl1.v0= f++; dl1.v2=h;dl1.v3=hh; dl1.v4=hhh;dl1.v5=hrel; dl1.cz6(); v[n++]=dl1.getVal();


            short hp = parse.heads[k];
            short[] hchld = hp == -1 ? new short[0] : children(parse.heads, hp);

            int[] hrels = new int[hchld.length];
            int[] hpss = new int[hchld.length];
            for (int j = 0; j < hchld.length; j++) {
                hrels[j] = parse.labels[hchld[j]];
                hpss[j] = is.pposs[i][hchld[j]];
            }


            StringBuilder hrl = new StringBuilder(hchld.length);
            StringBuilder hpsl = new StringBuilder(hchld.length);
            for (int j = 0; j < hchld.length; j++) {
                hrl.append((char) hrels[j]);
                hpsl.append((char) hpss[j]);
            }
            int hrli = mf.register("rli", hrl.toString());
            int hpli = mf.register("pli", hpsl.toString());

            dwwp.v0 = f++;
            dwwp.v2 = hpli;
            dwwp.v3 = hrli;
            dwwp.cz4();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = hrli;
            dwwp.cz4();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = hpli;
            dwwp.v3 = fm;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = hpli;
            dwwp.v3 = rli;
            dwwp.v4 = hrel;
            dwwp.v5 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = hrli;
            dwwp.v4 = hrel;
            dwwp.v5 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = hpli;
            dwwp.v3 = hpli;
            dwwp.v4 = hrel;
            dwwp.v5 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();



        }

        v[n] = Integer.MIN_VALUE;
    }

    /**
     * This works seem works well with n-best n=8 (88.858074) , n=10
     * (88.836884), n=12 (88.858) n=14 (88.913417) n=16 (88.79546) n=20
     * (88.80621) n 50 (88.729364) 1-best: 88.749605
     *
     * @param is
     * @param i
     * @param parse
     * @param rank
     * @param v
     * @param cluster
     */
    public void extractFeatures(Instances is, int i, ParseNBest parse, int rank, long[] v, Cluster cluster) {

        // mf.getValue(REL, "SB");

        int f = 1, n = 0;

        for (short k = 0; k < is.length(i) - 1; k++) {

            short[] chld = children(parse.heads, k);

            int abs = Math.abs(parse.heads[k] - k);
            final int dist;
            if (abs > 10) {
                dist = d10;
            } else if (abs > 5) {
                dist = d5;
            } else if (abs == 5) {
                dist = d4;
            } else if (abs == 4) {
                dist = d3;
            } else if (abs == 3) {
                dist = d2;
            } else if (abs == 2) {
                dist = d1;
            } else {
                dist = d0;
            }


            f = 2;

            int fm = is.forms[i][k];
            int hh = k != 0 ? is.pposs[i][parse.heads[k]] : s_end;
            int h = is.pposs[i][k];
            int hrel = parse.labels[k];//is.labels[i][k];
            int hhrel = k != 0 ? parse.labels[parse.heads[k]] : s_relend;
            int hhf = k != 0 ? is.forms[i][parse.heads[k]] : s_stwrd;

            int r1 = chld.length > 0 ? parse.labels[chld[0]] : s_relend;
            int rlast = chld.length > 0 ? parse.labels[chld[chld.length - 1]] : s_relend;

            int[] rels = new int[chld.length];
            int[] pss = new int[chld.length];
            int[] cls = new int[chld.length];

            int[] rc = new int[30]; // 20 was a good length

            for (int j = 0; j < chld.length; j++) {
                rels[j] = parse.labels[chld[j]];
                if (rels[j] < rc.length) {
                    rc[rels[j]]++;
                }
                pss[j] = is.pposs[i][chld[j]];
//				cls[j] = is.forms[i][chld[j]]==-1?0:cluster.getLP(is.forms[i][chld[j]]);
//				cls[j] = cls[j]==-1?0:cls[j];
            }

            StringBuilder rl = new StringBuilder(chld.length);
            StringBuilder psl = new StringBuilder(chld.length);
            StringBuilder csl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
//				csl.append((char)cls[j]);
            }

            int rli = mf.register("rli", rl.toString());
            int pli = mf.register("pli", psl.toString());
//			int cli = mf.register("cli", csl.toString());


            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            //	dwwp.v0=f++; dwwp.v2=cli; dwwp.v3=fm;  dwwp.v4=h; dwwp.cz5(); v[n++]=dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();
            //dwwp.v0=f++; dwwp.v2=cli; dwwp.cz3(); v[n++]=dwwp.getVal();

            //	dwwp.v0=f++; dwwp.v2=cli;dwwp.v3=h; dwwp.cz4(); v[n++]=dwwp.getVal();

            for (int j = 1; j < rc.length; j++) {
                dwwp.v0 = f++;
                dwwp.v2 = rc[j] == 0 ? 1 : rc[j] == 1 ? 2 : 3;
                dwwp.v3 = j;
                dwwp.cz4();
                v[n++] = dwwp.getVal();//
            }

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwp.v0 = f++;
            dwp.v2 = rli;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();

            //dwwp.v0=f++; dwwp.v2=h; dwwp.v3=hh; dwwp.v4=dist; dwwp.cz5(); v[n++]=dwwp.getVal();

            Arrays.sort(rels);
            Arrays.sort(pss);

            rl = new StringBuilder(chld.length);
            psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }
            rli = mf.register("rli", rl.toString());
            pli = mf.register("pli", psl.toString());

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dl1.v0 = f++;
            dl1.v2 = h;
            dl1.v3 = hrel;
            dl1.v4 = hhrel;
            dl1.v5 = hh;
            dl1.v6 = rlast;
            dl1.cz6();
            v[n++] = dl1.getVal();
            dwp.v0 = f++;
            dwp.v2 = fm;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.cz5();
            v[n++] = dwp.getVal();
            dwp.v0 = f++;
            dwp.v2 = hhf;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();
        }

        v[n] = Integer.MIN_VALUE;
    }

    /**
     *
     * Works well!
     *
     * @param is
     * @param i
     * @param parse
     * @param rank
     * @param v
     */
    public void extractFeatures6(Instances is, int i, ParseNBest parse, int rank, long[] v) {

        // mf.getValue(REL, "SB");

        int f = 1, n = 0;

        for (short k = 0; k < is.length(i) - 1; k++) {

            short[] chld = children(parse.heads, k);

            f = 2;

            int fm = is.forms[i][k];
            int hh = k != 0 ? is.pposs[i][parse.heads[k]] : s_end;
            int h = is.pposs[i][k];
            int hrel = parse.labels[k];//is.labels[i][k];
            int hhrel = k != 0 ? parse.labels[parse.heads[k]] : s_relend;
            int hhf = k != 0 ? is.forms[i][parse.heads[k]] : s_stwrd;

            int r1 = chld.length > 0 ? parse.labels[chld[0]] : s_relend;
            int rlast = chld.length > 0 ? parse.labels[chld[chld.length - 1]] : s_relend;

            int[] rels = new int[chld.length];
            int[] pss = new int[chld.length];

            int[] rc = new int[30]; // 20 was a good length

            for (int j = 0; j < chld.length; j++) {
                rels[j] = parse.labels[chld[j]];
                if (rels[j] < rc.length) {
                    rc[rels[j]]++;
                }
                //	if (rels[j]==sb) numSB++;
                pss[j] = is.pposs[i][chld[j]];
            }

            StringBuilder rl = new StringBuilder(chld.length);
            StringBuilder psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }

            int rli = mf.register("rli", rl.toString());
            int pli = mf.register("pli", psl.toString());


            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();

            for (int j = 1; j < rc.length; j++) {
                dwwp.v0 = f++;
                dwwp.v2 = rc[j] == 0 ? 1 : rc[j] == 1 ? 2 : 3;
                dwwp.v3 = j;
                dwwp.cz4();
                v[n++] = dwwp.getVal();//
            }

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwp.v0 = f++;
            dwp.v2 = rli;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();


            Arrays.sort(rels);
            Arrays.sort(pss);

            rl = new StringBuilder(chld.length);
            psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }
            rli = mf.register("rli", rl.toString());
            pli = mf.register("pli", psl.toString());


            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dl1.v0 = f++;
            dl1.v2 = h;
            dl1.v3 = hrel;
            dl1.v4 = hhrel;
            dl1.v5 = hh;
            dl1.v6 = rlast;
            dl1.cz6();
            v[n++] = dl1.getVal();
            dwp.v0 = f++;
            dwp.v2 = fm;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.cz5();
            v[n++] = dwp.getVal();
            dwp.v0 = f++;
            dwp.v2 = hhf;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();

        }

        v[n] = Integer.MIN_VALUE;
    }

    public void extractFeatures2(Instances is, int i, ParseNBest parse, int rank, long[] v) {



        int f = 1, n = 0;

        for (short k = 0; k < is.length(i) - 1; k++) {

            short[] chld = children(parse.heads, k);

            f = 2;

            int fm = is.forms[i][k];
            int hh = k != 0 ? is.pposs[i][parse.heads[k]] : s_end;
            int h = is.pposs[i][k];
            int hrel = parse.labels[k];//is.labels[i][k];
            int hhrel = k != 0 ? parse.labels[parse.heads[k]] : s_relend;
            int hhf = k != 0 ? is.forms[i][parse.heads[k]] : s_stwrd;

            int r1 = chld.length > 0 ? parse.labels[chld[0]] : s_relend;
            int rlast = chld.length > 0 ? parse.labels[chld[chld.length - 1]] : s_relend;

            int[] rels = new int[chld.length];
            int[] pss = new int[chld.length];



            for (int j = 0; j < chld.length; j++) {
                rels[j] = parse.labels[chld[j]];
                pss[j] = is.pposs[i][chld[j]];
            }

            StringBuilder rl = new StringBuilder(chld.length);
            StringBuilder psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }

            int rli = mf.register("rli", rl.toString());
            int pli = mf.register("pli", psl.toString());


            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.cz3();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = hh;
            dwwp.v4 = h;
            dwwp.cz5();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.cz5();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = hh;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwp.v0 = f++;
            dwp.v2 = rli;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();


            Arrays.sort(rels);
            Arrays.sort(pss);

            rl = new StringBuilder(chld.length);
            psl = new StringBuilder(chld.length);
            for (int j = 0; j < chld.length; j++) {
                rl.append((char) rels[j]);
                psl.append((char) pss[j]);
            }
            rli = mf.register("rli", rl.toString());
            pli = mf.register("pli", psl.toString());


            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = rli;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = fm;
            dwwp.v3 = rli;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();
            dwwp.v0 = f++;
            dwwp.v2 = pli;
            dwwp.v3 = fm;
            dwwp.v4 = 1;
            dwwp.v5 = h;
            dwwp.cz6();
            v[n++] = dwwp.getVal();

            dwwp.v0 = f++;
            dwwp.v2 = rli;
            dwwp.v3 = h;
            dwwp.cz4();
            v[n++] = dwwp.getVal();

            dl1.v0 = f++;
            dl1.v2 = h;
            dl1.v3 = hrel;
            dl1.v4 = hhrel;
            dl1.v5 = hh;
            dl1.v6 = rlast;
            dl1.cz6();
            v[n++] = dl1.getVal();
            dwp.v0 = f++;
            dwp.v2 = fm;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.cz5();
            v[n++] = dwp.getVal();
            dwp.v0 = f++;
            dwp.v2 = hhf;
            dwp.v3 = hrel;
            dwp.v4 = hh;
            dwp.v5 = h;
            dwp.cz6();
            v[n++] = dwp.getVal();

        }

        v[n] = Integer.MIN_VALUE;
    }

    /**
     * @param parse
     * @param k
     * @return
     */
    private short[] children(short[] heads, short h) {

        int c = 0;
        for (int k = 0; k < heads.length; k++) {
            if (heads[k] == h) {
                c++;
            }
        }

        short[] clds = new short[c];
        c = 0;
        for (int k = 0; k < heads.length; k++) {
            if (heads[k] == h) {
                clds[c++] = (short) k;
            }
        }
        return clds;
    }
}