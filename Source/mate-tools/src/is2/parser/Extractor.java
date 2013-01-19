package is2.parser;

import is2.data.*;
import is2.util.DB;
import is2.util.OptionsSuper;

final public class Extractor {

    static final int _SIB = 85;
    public static int s_rel, s_word, s_type, s_dir, s_dist, s_feat, s_child, s_spath, s_lpath, s_pos, s_rel1;
    public final DX d0, dl1, dl2, dwr, dr, dwwp, dw, dwp, dlf, d3lp, d2lp, d2pw, d2pp;
    public final Long2IntInterface li;
    public boolean s_stack = false;

    public Extractor(Long2IntInterface li, boolean stack, int what) {

        s_stack = stack;

        this.li = li;

        if (what == OptionsSuper.MULTIPLICATIVE) {
            d0 = new D6(li);
            dl1 = new D6(li);
            dl2 = new D6(li);
            dwr = new D6(li);
            dr = new D6(li);
            dwwp = new D6(li);
            dw = new D6(li);
            dwp = new D6(li);
            dlf = new D6(li);
            d3lp = new D6(li);
            d2lp = new D6(li);
            d2pw = new D6(li);
            d2pp = new D6(li);
        } else {
            d0 = new D5(li);
            dl1 = new D5(li);
            dl2 = new D5(li);
            dwr = new D5(li);
            dr = new D5(li);
            dwwp = new D5(li);
            dw = new D5(li);
            dwp = new D5(li);
            dlf = new D5(li);
            d3lp = new D5(li);
            d2lp = new D5(li);
            d2pw = new D5(li);
            d2pp = new D5(li);
        }
    }

    public static void initStat(int what) {
        MFO mf = new MFO();
        if (what == OptionsSuper.MULTIPLICATIVE) {

            DB.println("mult  (d4) ");

            s_rel = mf.getFeatureCounter().get(REL).intValue() * 16;
            s_rel1 = mf.getFeatureCounter().get(REL).intValue() + 1;
            s_pos = mf.getFeatureCounter().get(POS).intValue();
            s_word = mf.getFeatureCounter().get(WORD).intValue();
            s_type = mf.getFeatureCounter().get(TYPE).intValue();
            s_dir = (int) (mf.getFeatureCounter().get(DIR));
            la = (mf.getValue(DIR, LA));
            ra = (mf.getValue(DIR, RA));
            s_dist = (int) (mf.getFeatureCounter().get(DIST));//mf.getFeatureBits(DIST);
            s_feat = (int) (mf.getFeatureCounter().get(Pipe.FEAT));//mf.getFeatureBits(Pipe.FEAT);
            s_spath = (mf.getFeatureCounter().get(Cluster.SPATH) == null ? 0 : mf.getFeatureCounter().get(Cluster.SPATH));//mf.getFeatureBits(Cluster.SPATH);
            s_lpath = (mf.getFeatureCounter().get(Cluster.LPATH) == null ? 0 : mf.getFeatureCounter().get(Cluster.LPATH));//mf.getFeatureBits(Cluster.LPATH);
        } else {

            s_rel = is2.parser.MFO.getFeatureBits(REL);
            s_pos = is2.parser.MFO.getFeatureBits(POS);
            s_word = is2.parser.MFO.getFeatureBits(WORD);
            s_type = is2.parser.MFO.getFeatureBits(TYPE);
            s_dir = is2.parser.MFO.getFeatureBits(DIR);
            la = mf.getValue(DIR, LA);
            ra = mf.getValue(DIR, RA);
            s_dist = is2.parser.MFO.getFeatureBits(DIST);
            s_feat = is2.parser.MFO.getFeatureBits(Pipe.FEAT);
            s_spath = is2.parser.MFO.getFeatureBits(Cluster.SPATH);
            s_lpath = is2.parser.MFO.getFeatureBits(Cluster.LPATH);

            DB.println("shift init (d5) ");
        }
    }

    public void init() {

        d0.a0 = s_type;
        d0.a1 = s_pos;
        d0.a2 = s_pos;
        d0.a3 = s_pos;
        d0.a4 = s_pos;
        d0.a5 = s_pos;
        d0.a6 = s_pos;
        d0.a7 = s_pos;
        d0.fix();
        dl1.a0 = s_type;
        dl1.a1 = s_rel;
        dl1.a2 = s_pos;
        dl1.a3 = s_pos;
        dl1.a4 = s_pos;
        dl1.a5 = s_pos;
        dl1.a6 = s_pos;
        dl1.a7 = s_pos;
        dl1.fix();
        dl2.a0 = s_type;
        dl2.a1 = s_rel;
        dl2.a2 = s_word;
        dl2.a3 = s_pos;
        dl2.a4 = s_pos;
        dl2.a5 = s_pos;
        dl2.a6 = s_pos;
        dl2.a7 = s_pos;
        dl2.fix();
        dwp.a0 = s_type;
        dwp.a1 = s_rel;
        dwp.a2 = s_word;
        dwp.a3 = s_pos;
        dwp.a4 = s_pos;
        dwp.a5 = s_word;
        dwp.fix();
        dwwp.a0 = s_type;
        dwwp.a1 = s_rel;
        dwwp.a2 = s_word;
        dwwp.a3 = s_word;
        dwwp.a4 = s_pos;
        dwwp.a5 = s_word;
        dwwp.fix();
        dlf.a0 = s_type;
        dlf.a1 = s_rel;
        dlf.a2 = s_pos;
        dlf.a3 = s_pos;
        dlf.a4 = s_feat;
        dlf.a5 = s_feat;
        dlf.a6 = s_pos;
        dlf.a7 = s_pos;
        dlf.fix();
        d3lp.a0 = s_type;
        d3lp.a1 = s_rel;
        d3lp.a2 = s_lpath;
        d3lp.a3 = s_lpath;
        d3lp.a4 = s_lpath;
        d3lp.a5 = s_word;
        d3lp.a6 = s_spath;
        d3lp.a7 = s_spath;
        d3lp.fix();
        d2lp.a0 = s_type;
        d2lp.a1 = s_rel;
        d2lp.a2 = s_lpath;
        d2lp.a3 = s_lpath;
        d2lp.a4 = s_word;
        d2lp.a5 = s_word;
        d2lp.fix(); //d3lp.a6 = s_spath; d3lp.a7 = s_spath;	
        d2pw.a0 = s_type;
        d2pw.a1 = s_rel;
        d2pw.a2 = s_lpath;
        d2pw.a3 = s_lpath;
        d2pw.a4 = s_word;
        d2pw.a5 = s_word;
        d2pw.fix(); //d3lp.a6 = s_spath; d3lp.a7 = s_spath;	
        d2pp.a0 = s_type;
        d2pp.a1 = s_rel;
        d2pp.a2 = s_lpath;
        d2pp.a3 = s_lpath;
        d2pp.a4 = s_pos;
        d2pp.a5 = s_pos;
        d2pp.fix(); //d3lp.a6 = s_spath; d3lp.a7 = s_spath;	
    }

    public int basic(short[] pposs, int p, int d, IFV f) {

        d0.clean();
        dl1.clean();
        dl2.clean();
        dwp.clean();
        dwwp.clean();
        dlf.clean();
        d3lp.clean();

        d3lp.clean();
        d2lp.clean();
        d2pw.clean();
        d2pp.clean();

        int n = 1;
        int dir = (p < d) ? ra : la;
        d0.v0 = n++;
        d0.v1 = pposs[p];
        d0.v2 = pposs[d]; //d0.stop=4;
        int end = (p >= d ? p : d);
        int start = (p >= d ? d : p) + 1;

        for (int i = start; i < end; i++) {
            d0.v3 = pposs[i];
            d0.cz4();
            d0.csa(s_dir, dir, f);
        }
        return n;
    }

    public int firstm(Instances is, int i, int prnt, int dpnt, int label, Cluster cluster, long[] f) {


        for (int k = 0; k < f.length; k++) {
            f[k] = 0;
        }

        short[] pposs = is.pposs[i];
        int[] form = is.forms[i];
        short[][] feats = is.feats[i];


        int pF = form[prnt], dF = form[dpnt];
        int pL = is.plemmas[i][prnt], dL = is.plemmas[i][dpnt];
        int pP = pposs[prnt], dP = pposs[dpnt];

        int prntLS = pF == -1 ? -1 : cluster.getLP(pF), chldLS = dF == -1 ? -1 : cluster.getLP(dF);

        //	final int dir= (prnt < dpnt)? ra:la;

        if (pF > maxForm) {
            pF = -1;
        }
        if (pL > maxForm) {
            pL = -1;
        }

        if (dF > maxForm) {
            dF = -1;
        }
        if (dL > maxForm) {
            dL = -1;
        }


        int n = 3, c = 0;

        dl2.v1 = label;
        dl2.v0 = n++;
        dl2.v2 = pF;
        dl2.v3 = dP;
        dl2.cz4();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v2 = dF;
        dl2.v3 = pP;
        dl2.cz4();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.getVal();


        dwwp.v1 = label;
        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = dF;
        dwwp.cz4();
        f[c++] = dwwp.getVal();

        dl1.v1 = label;
        dl1.v0 = n++;
        dl1.v2 = dP;
        dl1.cz3();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.cz3();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = dP;
        dl1.cz4();
        f[c++] = dl1.getVal();

        int pPm1 = prnt > 0 ? pposs[prnt - 1] : s_str, dPm1 = dpnt > 0 ? pposs[dpnt - 1] : s_str;
        int pPp1 = prnt < pposs.length - 1 ? pposs[prnt + 1] : s_end, dPp1 = dpnt < pposs.length - 1 ? pposs[dpnt + 1] : s_end;

        int pPm2 = prnt > 1 ? pposs[prnt - 2] : s_str, dPm2 = dpnt > 1 ? pposs[dpnt - 2] : s_str;
        int pPp2 = prnt < pposs.length - 2 ? pposs[prnt + 2] : s_end, dPp2 = dpnt < pposs.length - 2 ? pposs[dpnt + 2] : s_end;

        int pFm1 = prnt > 0 ? form[prnt - 1] : s_stwrd, dFm1 = dpnt > 0 ? form[dpnt - 1] : s_stwrd;
        int pFp1 = prnt < form.length - 1 ? form[prnt + 1] : s_stwrd, dFp1 = dpnt < form.length - 1 ? form[dpnt + 1] : s_stwrd;


        if (prnt - 1 == dpnt) {
            pPm1 = -1;
        }
        if (prnt == dpnt - 1) {
            dPm1 = -1;
        }

        if (prnt + 1 == dpnt) {
            pPp1 = -1;
        }
        if (prnt == dpnt + 1) {
            dPp1 = -1;
        }

        if (prnt - 2 == dpnt) {
            pPm2 = -1;
        }
        if (prnt == dpnt - 2) {
            dPm2 = -1;
        }

        if (prnt + 2 == dpnt) {
            pPp2 = -1;
        }
        if (prnt == dpnt + 2) {
            dPp2 = -1;
        }


        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = pPp1;
        dl1.v4 = dP;
        dl1.v5 = dPp1;
        dl1.v6 = (prnt + 1 == dpnt ? 4 : prnt == dpnt + 1 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v5 = dPm1;
        dl1.v6 = (prnt + 1 == dpnt ? 4 : prnt == dpnt - 1 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = pPm1;
        dl1.v6 = (prnt - 1 == dpnt ? 4 : prnt == dpnt - 1 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v5 = dPp1;
        dl1.v6 = (prnt - 1 == dpnt ? 4 : prnt == dpnt + 1 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();


        dl1.v0 = n++;
        dl1.v3 = pPm1;
        dl1.v5 = (prnt - 1 == dpnt ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = dPm1;
        dl1.v5 = (prnt == dpnt - 1 ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = dPp1;
        dl1.v5 = (prnt == dpnt + 1 ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = pPp1;
        dl1.v5 = (prnt + 1 == dpnt ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();

        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = pPp2;
        dl1.v4 = dP;
        dl1.v5 = dPp2;
        dl1.v6 = (prnt + 2 == dpnt ? 4 : prnt == dpnt + 2 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v5 = dPm2;
        dl1.v6 = (prnt + 2 == dpnt ? 4 : prnt == dpnt - 2 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = pPm2;
        dl1.v6 = (prnt - 2 == dpnt ? 4 : prnt == dpnt - 2 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v5 = dPp2;
        dl1.v6 = (prnt - 2 == dpnt ? 4 : prnt == dpnt + 2 ? 5 : 6);
        dl1.cz7();
        f[c++] = dl1.getVal();


        // remove this again
        dl1.v0 = n++;
        dl1.v3 = pPm2;
        dl1.v5 = (prnt - 2 == dpnt ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = dPm2;
        dl1.v5 = (prnt == dpnt - 2 ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = dPp2;
        dl1.v5 = (prnt == dpnt + 2 ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v3 = pPp2;
        dl1.v5 = (prnt + 2 == dpnt ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();



        dl2.v0 = n++;
        dl2.v3 = dFm1;
        dl2.v3 = pPp1;
        dl2.v4 = pP;
        dl2.v5 = (prnt + 1 == dpnt ? 4 : prnt == dpnt - 1 ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = dFp1;
        dl2.v3 = pPm1;
        dl2.v5 = (prnt - 1 == dpnt ? 4 : prnt == dpnt + 1 ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFm1;
        dl2.v3 = dPp1;
        dl2.v4 = dP;
        dl2.v5 = (prnt - 1 == dpnt ? 4 : prnt == dpnt + 1 ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFp1;
        dl2.v3 = dPm1;
        dl2.v5 = (prnt + 1 == dpnt ? 4 : prnt == dpnt - 1 ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();


        // maybe without dir
        dl2.v0 = n++;
        dl2.v3 = dFm1;
        dl2.v3 = dPm2;
        dl2.v4 = pP;
        dl2.v5 = (prnt == dpnt - 1 ? 4 : prnt == dpnt - 2 ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = dFp1;
        dl2.v3 = dPp2;
        dl2.v5 = (prnt == dpnt + 1 ? 4 : prnt == dpnt + 2 ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFm1;
        dl2.v3 = pPm2;
        dl2.v4 = dP;
        dl2.v5 = (prnt - 1 == dpnt ? 4 : prnt - 2 == dpnt ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFp1;
        dl2.v3 = pPp2;
        dl2.v5 = (prnt + 1 == dpnt ? 4 : prnt + 2 == dpnt ? 5 : 6);
        dl2.cz6();
        f[c++] = dl2.getVal();


        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = dF;
        dwwp.v4 = dP;
        dwwp.cz5();
        f[c++] = dwwp.getVal();
        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = dF;
        dwwp.v4 = pP;
        dwwp.cz5();
        f[c++] = dwwp.getVal();
//				dwwp.v0= n++;	dwwp.v2=dF; dwwp.v3=pF; dwwp.v4=pP; dwwp.v4=dP; dwwp.cz6(); f[c++]=dwwp.getVal();  


        // until here


        // lemmas

        dl2.v1 = label;
        dl2.v0 = n++;
        dl2.v2 = pL;
        dl2.v3 = dP;
        dl2.cz4();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v2 = dL;
        dl2.v3 = pP;
        dl2.cz4();
        f[c++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.getVal();


        dwwp.v1 = label;
        dwwp.v0 = n++;
        dwwp.v2 = pL;
        dwwp.v3 = dL;
        dwwp.cz4();
        f[c++] = dwwp.getVal();

        dwp.v1 = label;
        dwp.v0 = n++;
        dwp.v2 = dL;
        dwp.v3 = pP;
        dwp.v4 = dP;
        dwp.v5 = pL; //dwp.cz6(); f[c++]=dwp.getVal();  

        dwp.v0 = n++;
        dwp.v2 = pL;
        dwp.v3 = pP;
        dwp.v4 = dP;
        dwp.v0 = n++;
        dwp.cz5();
        f[c++] = dwp.getVal();



        dwp.v0 = n++;
        dwp.v2 = pL;
        dwp.cz5();
        f[c++] = dwp.getVal();
        dwwp.v0 = n++;
        dwwp.v2 = pL;
        dwwp.v3 = dL;
        dwwp.v4 = dP;
        dwwp.cz5();
        f[c++] = dwwp.getVal();
        dwwp.v0 = n++;
        dwwp.v4 = pP;
        dwwp.cz5();
        f[c++] = dwwp.getVal();


        // cluster
        if (cluster.size() > 10) {
            d2pw.v1 = label;
            d2pw.v0 = n++;
            d2pw.v2 = prntLS;
            d2pw.v3 = chldLS;
            d2pw.cz4();
            f[c++] = d2pw.getVal();
            d2pw.v0 = n++;
            d2pw.v4 = pF;
            d2pw.cz5();
            f[c++] = d2pw.getVal();
            d2pw.v0 = n++;
            d2pw.v4 = dF;
            d2pw.cz5();
            f[c++] = d2pw.getVal();
            //		d2pw.v0=n++;             d2pw.v5=pF;         d2pw.cz6(); f[c++]=d2pw.getVal();


            d2pp.v1 = label;
            d2pp.v0 = n++;
            d2pp.v2 = prntLS;
            d2pp.v3 = chldLS;
            d2pp.cz4();
            f[c++] = d2pp.getVal();
            d2pp.v0 = n++;
            d2pp.v4 = pP;
            d2pp.cz5();
            f[c++] = d2pp.getVal();
            d2pp.v0 = n++;
            d2pp.v4 = dP;
            d2pp.cz5();
            f[c++] = d2pp.getVal();
            d2pp.v0 = n++;
            d2pp.v5 = pP;
            d2pp.cz6();
            f[c++] = d2pp.getVal();
        }

        if (s_stack) {

            short[] prel = is.plabels[i];
            short[] phead = is.pheads[i];

            //take those in for stacking
            dl2.v1 = label;
            dl2.v0 = n++;
            dl2.v2 = prel[dpnt];
            dl2.v3 = pP;
            dl2.v4 = dP;
            dl2.v5 = prnt == phead[dpnt] ? 1 : 2;
            dl2.cz6();
            f[c++] = dl2.getVal();
            dl2.v0 = n++;
            dl2.v2 = pP;
            dl2.v3 = dP;
            dl2.v4 = prnt == phead[dpnt] ? 1 : 2;
            dl2.cz5();
            f[c++] = dl2.getVal();
        }



        if (feats == null) {
            return c;
        }

        short[] featsP = feats[prnt], featsD = feats[dpnt];
        dlf.v0 = n++;
        dlf.v1 = label;
        dlf.v2 = pP;
        dlf.v3 = dP;
        c = extractFeat(f, c, featsP, featsD);


        return c;
    }

    public int second(Instances is, int i, int p, int d, int x, int label, Cluster cluster, long[] f) {

        //for(int k=0;k<f.length;k++) f[k]=0;

        dl1.clean();
        dwp.clean();
        dlf.clean();
        dwwp.clean();

        short[] pos = is.pposs[i];
        int[] forms = is.forms[i], lemmas = is.plemmas[i];


        int pP = pos[p], dP = pos[d];
        int pF = forms[p], dF = forms[d];
        int pL = lemmas[p], cL = lemmas[d];

        int sP = x != -1 ? pos[x] : s_str, sF = x != -1 ? forms[x] : s_stwrd, sL = x != -1 ? lemmas[x] : s_stwrd;

        int n = _SIB;
        if (pF > maxForm) {
            pF = -1;
        }
        if (pL > maxForm) {
            pL = -1;
        }

        if (dF > maxForm) {
            dF = -1;
        }
        if (cL > maxForm) {
            cL = -1;
        }

        if (sF > maxForm) {
            sF = -1;
        }
        if (sL > maxForm) {
            sL = -1;
        }

        int c = 0;

        dl1.v1 = label;
        dwwp.v1 = label;
        dwp.v1 = label;

        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = dP;
        dl1.v4 = sP;
        dl1.cz5();
        f[c++] = dl1.getVal(); // f[c++]=dl1.csa(s_dist,dist);
        dl1.v0 = n++;
        dl1.v3 = sP;
        dl1.cz4();
        f[c++] = dl1.getVal(); //f[c++]=dl1.csa(s_dist,dist);
        dl1.v0 = n++;
        dl1.v2 = dP;
        dl1.cz4();
        f[c++] = dl1.getVal(); //f[c++]=dl1.csa(s_dist,dist); 

        // sibling only could be tried

        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = sF;
        dwwp.cz4();
        f[c++] = dwwp.getVal(); //f[c++]=dwwp.csa(s_dist,dist);
        dwwp.v0 = n++;
        dwwp.v2 = dF;
        dwwp.cz4();
        f[c++] = dwwp.getVal(); //f[c++]=dwwp.csa(s_dist,dist);

        // 154
        dwp.v0 = n++;
        dwp.v2 = sF;
        dwp.v3 = pP;
        dwp.cz4();
        f[c++] = dwp.getVal(); //f[c++]=dwp.csa(s_dist,dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */
        dwp.v3 = dP;
        dwp.cz4();
        f[c++] = dwp.getVal(); //f[c++]=dwp.csa(s_dist,dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */ dwp.v2 = pF;
        dwp.v3 = sP;
        dwp.cz4();
        f[c++] = dwp.getVal(); //f[c++]=dwp.csa(s_dist,dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */ dwp.v2 = dF;
        dwp.cz4();
        f[c++] = dwp.getVal();// f[c++]=dwp.csa(s_dist,dist);

        // 158
        //lemmas

        dwwp.v0 = n++;
        dwwp.v2 = pL;
        dwwp.v3 = sL;
        dwwp.cz4();
        f[c++] = dwwp.getVal();
        dwwp.v0 = n++;
        dwwp.v2 = cL;
        dwwp.cz4();
        f[c++] = dwwp.getVal(); //f[c++]=dwwp.csa(s_dist,dist);  
        dwp.v0 = n++;
        dwp.v2 = sL;
        dwp.v3 = pP;
        dwp.cz4();
        f[c++] = dwp.getVal();// f[c++]=dwp.csa(s_dist,dist);
        dwp.v0 = n++;
        dwp.v3 = dP;
        dwp.cz4();
        f[c++] = dwp.getVal(); //  f[c++]=dwp.csa(s_dist,dist);

        // 162
        dwp.v0 = n++;
        dwp.v2 = pL;
        dwp.v3 = sP;
        dwp.cz4();
        f[c++] = dwp.getVal(); //f[c++]=dwp.csa(s_dist,dist);
        dwp.v0 = n++;
        dwp.v2 = cL;
        dwp.cz4();
        f[c++] = dwp.getVal();// f[c++]=dwp.csa(s_dist,dist);

        // clusters
        if (cluster.size() > 10) {
        }

        int pPm1 = p != 0 ? pos[p - 1] : s_str;
        int chldPm1 = d - 1 >= 0 ? pos[d - 1] : s_str;
        int prntPp1 = p != pos.length - 1 ? pos[p + 1] : s_end;
        int chldPp1 = d != pos.length - 1 ? pos[d + 1] : s_end;

        // sibling part of speech minus and plus 1
        int sPm1 = x > 0 ? pos[x - 1] : s_str;
        int sPp1 = x < pos.length - 1 ? pos[x + 1] : s_end;

        if (x + 1 == x || x + 1 == p || x + 1 == d) {
            sPp1 = -1;
        }
        if (p + 1 == x || p + 1 == p || p + 1 == d) {
            prntPp1 = -1;
        }
        if (d + 1 == x || d + 1 == p || d + 1 == d) {
            chldPp1 = -1;
        }

        if (x - 1 == x || x - 1 == p || x - 1 == d) {
            sPm1 = -1;
        }
        if (d - 1 == x || d - 1 == p || d - 1 == d) {
            chldPm1 = -1;
        }
        if (p - 1 == x || p - 1 == p || p - 1 == d) {
            pPm1 = -1;
        }


        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        // 165
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPm1;
        dl1.v4 = pP;
        dl1.v5 = (x - 1 == p ? 3 : x - 1 == d ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal(); //dl1.getVal();// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = prntPp1;
        dl1.v5 = (x == p + 1 ? 3 : 4);
        dl1.cz6();
        f[c++] = dl1.getVal();// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pPm1;
        dl1.v5 = (x == p - 1 ? 3 : 4);
        dl1.cz6();
        f[c++] = dl1.getVal();// f.add(li.l2i(l));
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPp1;
        dl1.v4 = pPm1;
        dl1.v5 = pP;
        dl1.v6 = (x == p - 1 ? 3 : x == p + 1 ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sPm1;
        dl1.v3 = sP;
        dl1.v4 = pPm1;
        dl1.v5 = pP;
        dl1.v6 = (x == p - 1 ? 3 : x - 1 == p ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPp1;
        dl1.v4 = pP;
        dl1.v5 = prntPp1;
        dl1.v6 = (x + 1 == p ? 3 : x == p + 1 ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sPm1;
        dl1.v3 = sP;
        dl1.v4 = pP;
        dl1.v5 = prntPp1;
        dl1.v6 = (x == p - 1 ? 3 : x == p + 1 ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();// f.add(li.l2i(l)); 

        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPp1;
        dl1.v4 = dP;
        dl1.v5 = (x + 1 == d ? 3 : x + 1 == p ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPm1;
        dl1.v4 = dP;
        dl1.v5 = (x - 1 == d ? 3 : x - 1 == p ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();

        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = chldPp1;
        dl1.v5 = (x == d + 1 ? 3 : d + 1 == p ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = chldPm1;
        dl1.v5 = (x == d - 1 ? 3 : d - 1 == p ? 4 : 5);
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPp1;
        dl1.v4 = chldPm1;
        dl1.v5 = dP;
        dl1.v6 = (x == d - 1 ? 3 : x + 1 == d ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sPm1;
        dl1.v3 = sP;
        dl1.v4 = chldPm1;
        dl1.v5 = dP;
        dl1.v6 = (x - 1 == d ? 3 : d - 1 == x ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sPp1;
        dl1.v4 = dP;
        dl1.v5 = chldPp1;
        dl1.v6 = (x == d + 1 ? 3 : x + 1 == d ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sPm1;
        dl1.v3 = sP;
        dl1.v4 = dP;
        dl1.v5 = chldPp1;
        dl1.v6 = (x - 1 == d ? 3 : d + 1 == x ? 4 : 5);
        dl1.cz7();
        f[c++] = dl1.getVal();

        // c=61;
		/*
         * if (cluster.size()>10) { AtomicInteger N = new AtomicInteger(n); c =
         * addClusterFeatures(d, p, x, pos, forms, cluster, N, c, f,label); n =
         * N.get(); }
         */
        // take those in for stacking

        if (s_stack) {
            short[] prel = is.plabels[i], phead = is.pheads[i];

            int g = p == phead[d] ? 1 : 2;
            if (x >= 0) {
                g += p == phead[x] ? 4 : 8;
            }

            int gr = x == -1 ? s_relend : prel[x];


            dl2.v1 = label;
            dl2.v0 = n++;
            dl2.v2 = prel[d];
            dl2.v3 = g;
            dl2.v4 = sP;
            dl2.v5 = dP;
            dl2.cz6();
            f[c++] = dl2.getVal();
            dl2.v0 = n++;
            dl2.v2 = prel[d];
            dl2.v3 = g;
            dl2.v4 = sP;
            dl2.v5 = pP;
            dl2.cz6();
            f[c++] = dl2.getVal();
            dl2.v0 = n++;
            dl2.v2 = prel[d];
            dl2.v3 = g;
            dl2.v4 = sP;
            dl2.v5 = pP;
            dl2.v6 = dP;
            dl2.cz7();
            f[c++] = dl2.getVal();

            dl2.v0 = n++;
            dl2.v2 = gr;
            dl2.v3 = g;
            dl2.v4 = sP;
            dl2.v5 = dP;
            dl2.cz6();
            f[c++] = dl2.getVal();
            dl2.v0 = n++;
            dl2.v2 = gr;
            dl2.v3 = g;
            dl2.v4 = sP;
            dl2.v5 = pP;
            dl2.cz6();
            f[c++] = dl2.getVal();
            dl2.v0 = n++;
            dl2.v2 = gr;
            dl2.v3 = g;
            dl2.v4 = sP;
            dl2.v5 = pP;
            dl2.v6 = dP;
            dl2.cz7();
            f[c++] = dl2.getVal();
        }

        short[][] feats = is.feats[i];

        if (feats == null) {
            return c;
        }


        short[] featsP = feats[d];
        short[] featsSbl = x != -1 ? feats[x] : null;
        dlf.v1 = label;
        dlf.v0 = n++;
        dlf.v2 = sP;
        dlf.v3 = dP;
        c = extractFeat(f, c, featsP, featsSbl);

        featsP = feats[p];

        dlf.v0 = n++;
        dlf.v1 = label;
        dlf.v2 = sP;
        dlf.v3 = pP;
        c = extractFeat(f, c, featsP, featsSbl);


        return c;
    }

    /**
     * Separated this method to speed up parsing
     *
     * @param d
     * @param p
     * @param x
     * @param pos
     * @param forms
     * @param cluster
     * @param N
     * @param c
     * @param f
     * @return
     */
    int addClusterFeatures(Instances is, int i, int d, int p, int x, Cluster cluster, int c, long[] f, int label) {

        //	int n= N.get();

        short[] pos = is.pposs[i];
        int[] forms = is.forms[i];

        int n = 190;
        int pP = pos[p], dP = pos[d];
        int sP = x != -1 ? pos[x] : s_str;


        int pLSp1 = p != pos.length - 1 ? forms[p + 1] == -1 ? -1 : cluster.getLP(forms[p + 1]) : _cend;
        int cLSp1 = d != pos.length - 1 ? forms[d + 1] == -1 ? -1 : cluster.getLP(forms[d + 1]) : _cend;
        int sLSp1 = x < pos.length - 1 ? forms[x + 1] == -1 ? -1 : cluster.getLP(forms[x + 1]) : _cend;

        int pLSm1 = p != 0 ? forms[p - 1] == -1 ? -1 : cluster.getLP(forms[p - 1]) : _cstr;
        int cLSm1 = d - 1 >= 0 ? forms[d - 1] == -1 ? -1 : cluster.getLP(forms[d - 1]) : _cstr;
        int sLSm1 = x > 0 ? forms[x - 1] == -1 ? -1 : cluster.getLP(forms[x - 1]) : _cstr;

        //int c=61;
        int pF = forms[p], dF = forms[d], sF = x != -1 ? forms[x] : s_stwrd;
        int prntLS = pF == -1 ? -1 : cluster.getLP(pF), chldLS = dF == -1 ? -1 : cluster.getLP(dF);

        int sblLS = (x != -1) && (sF != -1) ? cluster.getLP(sF) : s_stwrd;


        d2lp.v1 = label;
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = sblLS;
        d2lp.cz4();
        f[c++] = d2lp.getVal();
        d2lp.v0 = n++;
        d2lp.v2 = chldLS;
        d2lp.v3 = sblLS;
        d2lp.cz4();
        f[c++] = d2lp.getVal();// f[c++]=d2lp.csa(s_dist,dist);  		

        d3lp.v1 = label;
        d3lp.v0 = n++;
        d3lp.v2 = prntLS;
        d3lp.v3 = chldLS;
        d3lp.v4 = sblLS;
        d3lp.cz5();
        f[c++] = d3lp.getVal();

        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = chldLS;
        d2lp.v4 = sF;
        d2lp.cz5();
        f[c++] = d2lp.getVal(); //f[c++]=d2lp.csa(s_dist,dist);  
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = sblLS;
        d2lp.v4 = dF;
        d2lp.cz5();
        f[c++] = d2lp.getVal(); //f[c++]=d2lp.csa(s_dist,dist);  
        d2lp.v0 = n++;
        d2lp.v2 = chldLS;
        d2lp.v3 = sblLS;
        d2lp.v4 = pF;
        d2lp.cz5();
        f[c++] = d2lp.getVal(); //f[c++]=d2lp.csa(s_dist,dist);  

        d2pp.v1 = label;
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = chldLS;
        d2pp.v4 = sP;
        d2pp.cz5();
        f[c++] = d2pp.getVal(); //f[c++]=d2pp.csa(s_dist,dist);  
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = sblLS;
        d2pp.v4 = dP;
        d2pp.cz5();
        f[c++] = d2pp.getVal(); //f[c++]=d2pp.csa(s_dist,dist);  
        d2pp.v0 = n++;
        d2pp.v2 = chldLS;
        d2pp.v3 = sblLS;
        d2pp.v4 = pP;
        d2pp.cz5();
        f[c++] = d2pp.getVal(); //f[c++]=d2pp.csa(s_dist,dist);  


        if (x + 1 == x || x + 1 == p || x + 1 == d) {
            sLSp1 = -1;
        }
        if (p + 1 == x || p + 1 == p || p + 1 == d) {
            pLSp1 = -1;
        }
        if (d + 1 == x || d + 1 == p || d + 1 == d) {
            cLSp1 = -1;
        }

        if (x - 1 == x || x - 1 == p || x - 1 == d) {
            sLSm1 = -1;
        }
        if (d - 1 == x || d - 1 == p || d - 1 == d) {
            cLSm1 = -1;
        }
        if (p - 1 == x || p - 1 == p || p - 1 == d) {
            pLSm1 = -1;
        }

        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSp1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSm1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSp1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSm1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();



        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSp1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSm1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSp1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSm1;
        dl1.cz5();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.getVal();


        return c;
    }

    private int extractFeat(long[] f, int cnt, short[] featsP, short[] featsD) {
        if (featsP != null && featsD != null) {
            for (short i1 = 0; i1 < featsP.length; i1++) {
                for (short i2 = 0; i2 < featsD.length; i2++) {
                    dlf.v4 = featsP[i1];
                    dlf.v5 = featsD[i2];
                    dlf.cz6();
                    f[cnt++] = dlf.getVal();
                }
            }
        } else if (featsP == null && featsD != null) {

            for (short i2 = 0; i2 < featsD.length; i2++) {
                dlf.v4 = nofeat;
                dlf.v5 = featsD[i2];
                dlf.cz6();
                f[cnt++] = dlf.getVal();

            }
        } else if (featsP != null && featsD == null) {

            for (short i1 = 0; i1 < featsP.length; i1++) {
                dlf.v4 = featsP[i1];
                dlf.v5 = nofeat;
                dlf.cz6();
                f[cnt++] = dlf.getVal();

            }
        }
        return cnt;
    }

    public FV encodeCat(Instances is, int ic, short pposs[], int forms[], int[] lemmas, short[] heads, short[] types, short feats[][], Cluster cluster, FV f) {


        long[] svs = new long[250];

        for (int i = 1; i < heads.length; i++) {


            basic(pposs, heads[i], i, f);

            int w1 = heads[i] < i ? heads[i] : i;
            int w2 = heads[i] < i ? i : heads[i];

            int dir = heads[i] < i ? 0 : s_rel1;
            int label = types[i] + dir;

            int c = firstm(is, ic, w1, w2, label, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            int ch, cmi, cmo;
            if (heads[i] < i) {
                ch = rightmostRight(heads, heads[i], i);
                cmi = leftmostLeft(heads, i, heads[i]);
                cmo = rightmostRight(heads, i, heads.length);

            } else {
                ch = leftmostLeft(heads, heads[i], i);
                cmi = rightmostRight(heads, i, heads[i]);
                cmo = leftmostLeft(heads, i, 0);
            }

            int lx = types[i] + s_rel1 * ((heads[i] < i ? 0 : 1) + 8);
            c = second(is, ic, w1, w2, ch, lx, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }
            c = addClusterFeatures(is, ic, w1, w2, ch, cluster, c, svs, lx);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            lx = types[i] + s_rel1 * ((heads[i] < i ? 0 : 1) + ((cmi < i) ? 0 : 2));
            c = second(is, ic, w1, w2, cmi, lx, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            c = addClusterFeatures(is, ic, w1, w2, cmi, cluster, c, svs, lx);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            lx = types[i] + s_rel1 * ((heads[i] < i ? 0 : 1) + ((cmo < i) ? 0 : 2));
            c = second(is, ic, w1, w2, cmo, lx, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            c = addClusterFeatures(is, ic, w1, w2, cmo, cluster, c, svs, lx);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }
        }

        return f;
    }

    public void compare(Instances is, int ic, short pos[], short[] heads, short[] types, Cluster cluster, F2SF f, DataFES x) {


        long[] svs = new long[250];

        float fx = 0.0F;


        for (int i = 1; i < heads.length; i++) {

            f.clear();
            basic(pos, heads[i], i, f);

            if (x.pl[heads[i]][i] != f.getScore()) {
                DB.println("basic diff " + x.pl[heads[i]][i] + " fg " + f.getScore());
            }

            int w1 = heads[i] < i ? heads[i] : i;
            int w2 = heads[i] < i ? i : heads[i];

            int dir = heads[i] < i ? 0 : s_rel1;
            int label = types[i] + dir;
            f.clear();
            int c = firstm(is, ic, w1, w2, label, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            if (x.lab[heads[i]][i][types[i]] != f.getScore()) {
                DB.println("first diff " + x.lab[heads[i]][i][types[i]] + " fg " + f.getScore());
            }

            short[] labels = Edges.get(pos[heads[i]], pos[i]);
            int lid = -1;
            for (int k = 0; k < labels.length; k++) {
                if (types[i] == labels[k]) {
                    lid = k;
                    break;
                }
            }



            int ch, cmi, cmo;
            if (heads[i] < i) {
                ch = rightmostRight(heads, heads[i], i);
                cmi = leftmostLeft(heads, i, heads[i]);
                cmo = rightmostRight(heads, i, heads.length);

            } else {
                ch = leftmostLeft(heads, heads[i], i);
                cmi = rightmostRight(heads, i, heads[i]);
                cmo = leftmostLeft(heads, i, 0);
            }

            f.clear();


            int lx = types[i] + s_rel1 * ((heads[i] < i ? 0 : 1) + 8);
            c = second(is, ic, w1, w2, ch, lx, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            if (x.sib[heads[i]][i][ch == -1 ? heads[i] : ch][lid] != f.getScore()) {
                DB.println("sib diff " + x.sib[heads[i]][i][ch == -1 ? i : ch][lid] + " fg " + f.getScore());
            }

            f.clear();


            lx = types[i] + s_rel1 * ((heads[i] < i ? 0 : 1) + ((cmi < i) ? 0 : 2));
            c = second(is, ic, w1, w2, cmi, lx, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            if (x.gra[heads[i]][i][cmi == -1 ? i : cmi][lid] != f.getScore()) {
                DB.println("gcm diff " + x.gra[heads[i]][i][cmi == -1 ? i : cmi][lid] + " fg " + f.getScore() + " cmi " + cmi + " i " + i
                        + " head " + heads[i] + " w1 " + w1 + " w2 " + w2 + " label " + lx + " " + ((heads[i] < i ? 0 : 1) + ((cmi < i) ? 0 : 2)));

                Parser.out.println("w1 " + w1 + " w2 " + w2 + " cmi " + cmi + " label " + label + " ");

                for (long k : svs) {
                    Parser.out.print(k + " ");
                }
                Parser.out.println();

            }
            f.clear();
            lx = types[i] + s_rel1 * ((heads[i] < i ? 0 : 1) + ((cmo < i) ? 0 : 2));
            c = second(is, ic, w1, w2, cmo, lx, cluster, svs);
            for (int k = 0; k < c; k++) {
                dl1.map(f, svs[k]);
            }

            if (x.gra[heads[i]][i][cmo == -1 ? i : cmo][lid] != f.getScore()) {
                DB.println("gcm diff " + x.gra[heads[i]][i][cmo == -1 ? i : cmo][lid] + " fg " + f.getScore() + " cmo " + cmo + " i " + i
                        + " head " + heads[i] + " w1 " + w1 + " w2 " + w2 + " label " + lx + " " + ((heads[i] < i ? 0 : 1) + ((cmi < i) ? 0 : 2)));

                Parser.out.println("w1 " + w1 + " w2 " + w2 + " cmi " + cmi + " label " + label + " ");

                for (long k : svs) {
                    Parser.out.print(k + " ");
                }
                Parser.out.println();
            }
        }
    }

    public short[] searchLabel(Instances is, int ic, short pposs[], int forms[], int[] lemmas, short[] heads, short[] types, short feats[][], Cluster cluster, IFV f) {


        long[] svs = new long[250];

        short[] newLabels = new short[types.length];

        for (int i = 1; i < heads.length; i++) {


            //			int n =basic(pposs, forms, heads[i], i, cluster, f); 

            int ch, cmi, cmo;
            if (heads[i] < i) {
                ch = rightmostRight(heads, heads[i], i);
                cmi = leftmostLeft(heads, i, heads[i]);
                cmo = rightmostRight(heads, i, heads.length);

            } else {
                ch = leftmostLeft(heads, heads[i], i);
                cmi = rightmostRight(heads, i, heads[i]);
                cmo = leftmostLeft(heads, i, 0);
            }


            short labels[] = Edges.get(pposs[is.heads[ic][i]], pposs[i]);

            float best = -1000;
            short bestL;
            for (int j = 0; j < labels.length; j++) {

                f.clear();
                firstm(is, ic, heads[i], i, labels[j], cluster, svs);
                for (int k = 0; k < svs.length; k++) {
                    dl1.map(f, svs[k]);
                }

                second(is, ic, heads[i], i, ch, labels[j], cluster, svs);
                for (int k = 0; k < svs.length; k++) {
                    dl1.map(f, svs[k]);
                }

                second(is, ic, heads[i], i, cmi, labels[j], cluster, svs);
                for (int k = 0; k < svs.length; k++) {
                    dl1.map(f, svs[k]);
                }

                second(is, ic, heads[i], i, cmo, labels[j], cluster, svs);
                for (int k = 0; k < svs.length; k++) {
                    dl1.map(f, svs[k]);
                }

                if (best < f.getScore()) {
                    best = (float) f.getScore();
                    bestL = labels[j];
                    newLabels[i] = bestL;
                }
            }
        }
        return newLabels;

        //return f;
    }

    public static float encode3(short[] pos, short heads[], short[] types, DataFES d2) {


        float v = 0F;
        for (int i = 1; i < heads.length; i++) {

            //		int dir= (heads[i] < i)? 0:1;

            v += d2.pl[heads[i]][i];
            v += d2.lab[heads[i]][i][types[i]];

            //	boolean left = i<heads[i]; 
            short[] labels = Edges.get(pos[heads[i]], pos[i]);
            int lid = -1;
            for (int k = 0; k < labels.length; k++) {
                if (types[i] == labels[k]) {
                    lid = k;
                    break;
                }
            }

            int ch, cmi, cmo;
            if (heads[i] < i) {
                ch = rightmostRight(heads, heads[i], i);
                cmi = leftmostLeft(heads, i, heads[i]);
                cmo = rightmostRight(heads, i, heads.length);

                if (ch == -1) {
                    ch = heads[i];
                }
                if (cmi == -1) {
                    cmi = heads[i];
                }
                if (cmo == -1) {
                    cmo = heads[i];
                }

            } else {
                ch = leftmostLeft(heads, heads[i], i);
                cmi = rightmostRight(heads, i, heads[i]);
                cmo = leftmostLeft(heads, i, 0);

                if (ch == -1) {
                    ch = i;
                }
                if (cmi == -1) {
                    cmi = i;
                }
                if (cmo == -1) {
                    cmo = i;
                }
            }
            v += d2.sib[heads[i]][i][ch][lid];
            v += d2.gra[heads[i]][i][cmi][lid];
            v += d2.gra[heads[i]][i][cmo][lid];
        }
        return v;
    }

    public static float encode3(short[] pos, short heads[], short[] types, DataFES d2, float[] scores) {

        float v = 0F;
        for (int i = 1; i < heads.length; i++) {


            scores[i] = d2.pl[heads[i]][i];
            scores[i] += d2.lab[heads[i]][i][types[i]];

            short[] labels = Edges.get(pos[heads[i]], pos[i]);
            int lid = -1;
            for (int k = 0; k < labels.length; k++) {
                if (types[i] == labels[k]) {
                    lid = k;
                    break;
                }
            }

            int ch, cmi, cmo;
            if (heads[i] < i) {
                ch = rightmostRight(heads, heads[i], i);
                cmi = leftmostLeft(heads, i, heads[i]);
                cmo = rightmostRight(heads, i, heads.length);

                if (ch == -1) {
                    ch = heads[i];
                }
                if (cmi == -1) {
                    cmi = heads[i];
                }
                if (cmo == -1) {
                    cmo = heads[i];
                }

            } else {
                ch = leftmostLeft(heads, heads[i], i);
                cmi = rightmostRight(heads, i, heads[i]);
                cmo = leftmostLeft(heads, i, 0);

                if (ch == -1) {
                    ch = i;
                }
                if (cmi == -1) {
                    cmi = i;
                }
                if (cmo == -1) {
                    cmo = i;
                }
            }
            scores[i] += d2.sib[heads[i]][i][ch][lid];
            scores[i] += d2.gra[heads[i]][i][cmi][lid];
            scores[i] += d2.gra[heads[i]][i][cmo][lid];
        }
        return v;
    }

    public static int rightmostRight(short[] heads, int head, int max) {
        int rightmost = -1;
        for (int i = head + 1; i < max; i++) {
            if (heads[i] == head) {
                rightmost = i;
            }
        }

        return rightmost;
    }

    public static int leftmostLeft(short[] heads, int head, int min) {
        int leftmost = -1;
        for (int i = head - 1; i > min; i--) {
            if (heads[i] == head) {
                leftmost = i;
            }
        }
        return leftmost;
    }
    public static final String REL = "REL", END = "END", STR = "STR", LA = "LA", RA = "RA";
    private static int ra, la;
    private static int s_str;
    private static int s_end, _cend, _cstr, s_stwrd, s_relend;
    protected static final String TYPE = "TYPE", DIR = "D";
    public static final String POS = "POS";
    protected static final String DIST = "DIST", MID = "MID";
    private static final String _0 = "0", _4 = "4", _3 = "3", _2 = "2", _1 = "1", _5 = "5", _10 = "10";
    private static int di0, d4, d3, d2, d1, d5, d10;
    private static final String WORD = "WORD", STWRD = "STWRD", STPOS = "STPOS";
    private static int nofeat;
    public static int maxForm;

    /**
     * Initialize the features.
     *
     * @param maxFeatures
     */
    static public void initFeatures() {

        MFO mf = new MFO();
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

        mf.register(TYPE, Pipe.FEAT);
        nofeat = mf.register(Pipe.FEAT, "NOFEAT");

        for (int k = 0; k < 150; k++) {
            mf.register(TYPE, "F" + k);
        }

        di0 = mf.register(DIST, _0);
        d1 = mf.register(DIST, _1);
        d2 = mf.register(DIST, _2);
        d3 = mf.register(DIST, _3);
        d4 = mf.register(DIST, _4);
        d5 = mf.register(DIST, _5);
        //		d5l=mf.register(DIST, _5l);
        d10 = mf.register(DIST, _10);
    }
}