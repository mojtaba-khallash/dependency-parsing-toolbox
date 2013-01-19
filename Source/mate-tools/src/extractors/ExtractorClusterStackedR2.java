package extractors;

import is2.data.*;
import java.util.Arrays;

final public class ExtractorClusterStackedR2 implements Extractor {

    public static int s_rel, s_word, s_type, s_dir, s_dist, s_feat, s_child, s_spath, s_lpath, s_pos;
    MFB mf;
    final D4 d0, dl1, dl2, dwr, dr, dwwp, dw, dwp, dlf, d3lp, d2lp, d2pw, d2pp;
    public final Long2IntInterface li;

    public ExtractorClusterStackedR2(Long2IntInterface li) {

        initFeatures();
        this.li = li;
        d0 = new D4(li);
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

    @Override
    public void initStat() {


        mf = new MFB();
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

    @Override
    public void init() {
        //	DB.println("init");
        d0.a0 = s_type;
        d0.a1 = s_pos;
        d0.a2 = s_pos;
        d0.a3 = s_pos;
        d0.a4 = s_pos;
        d0.a5 = s_pos;
        d0.a6 = s_pos;
        d0.a7 = s_pos;
        dl1.a0 = s_type;
        dl1.a1 = s_rel;
        dl1.a2 = s_pos;
        dl1.a3 = s_pos;
        dl1.a4 = s_pos;
        dl1.a5 = s_pos;
        dl1.a6 = s_pos;
        dl1.a7 = s_pos;
        dl2.a0 = s_type;
        dl2.a1 = s_rel;
        dl2.a2 = s_word;
        dl2.a3 = s_pos;
        dl2.a4 = s_pos;
        dl2.a5 = s_pos;
        dl2.a6 = s_pos;
        dl2.a7 = s_pos;
        dwp.a0 = s_type;
        dwp.a1 = s_rel;
        dwp.a2 = s_word;
        dwp.a3 = s_pos;
        dwp.a4 = s_pos;
        dwp.a5 = s_word;
        dwwp.a0 = s_type;
        dwwp.a1 = s_rel;
        dwwp.a2 = s_word;
        dwwp.a3 = s_word;
        dwwp.a4 = s_pos;
        dwwp.a5 = s_word;
        dlf.a0 = s_type;
        dlf.a1 = s_rel;
        dlf.a2 = s_pos;
        dlf.a3 = s_pos;
        dlf.a4 = s_feat;
        dlf.a5 = s_feat;
        dlf.a6 = s_pos;
        dlf.a7 = s_pos;
        d3lp.a0 = s_type;
        d3lp.a1 = s_rel;
        d3lp.a2 = s_lpath;
        d3lp.a3 = s_lpath;
        d3lp.a4 = s_lpath;
        d3lp.a5 = s_word;
        d3lp.a6 = s_spath;
        d3lp.a7 = s_spath;
        d2lp.a0 = s_type;
        d2lp.a1 = s_rel;
        d2lp.a2 = s_lpath;
        d2lp.a3 = s_lpath;
        d2lp.a4 = s_word;
        d2lp.a5 = s_word; //d3lp.a6 = s_spath; d3lp.a7 = s_spath;	
        d2pw.a0 = s_type;
        d2pw.a1 = s_rel;
        d2pw.a2 = s_lpath;
        d2pw.a3 = s_lpath;
        d2pw.a4 = s_word;
        d2pw.a5 = s_word; //d3lp.a6 = s_spath; d3lp.a7 = s_spath;	
        d2pp.a0 = s_type;
        d2pp.a1 = s_rel;
        d2pp.a2 = s_lpath;
        d2pp.a3 = s_lpath;
        d2pp.a4 = s_pos;
        d2pp.a5 = s_pos; //d3lp.a6 = s_spath; d3lp.a7 = s_spath;	
    }

    @Override
    public int basic(short[] pposs, int[] form, int p, int d, Cluster cluster, IFV f) {

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
        //	d0.v0= n; d0.v1=pposs[p]; d0.v2=pposs[d]; //d0.stop=4;
        int end = (p >= d ? p : d);
        int start = (p >= d ? d : p) + 1;

        StringBuilder s = new StringBuilder(end - start);
        int[] x = new int[end - start];
        int c = 0;
        for (int i = start; i < end; i++) {
            //d0.v3=pposs[i];
            //d0.cz4();
            //d0.csa(s_dir,dir,f);
//			s.append((char)pposs[i]);
            x[c++] = pposs[i];
        }

        Arrays.sort(x);
        for (int i = 0; i < x.length; i++) {
            if (i == 0 || x[i] != x[i - 1]) {
                s.append(x[i]);
            }
        }
        int v = mf.register("px", s.toString());

        dwp.v0 = n++;
        dwp.v1 = 1;
        dwp.v2 = v;
        dwp.v3 = pposs[p];
        dwp.v4 = pposs[d];
        dwp.cz5();
        dwp.csa(s_dir, dir, f);

        return n;
    }

    @Override
    public void firstm(Instances is, int i,
            int prnt, int dpnt, int label, Cluster cluster, long[] f) {


        //short[] pposs, int[] form, int[] lemmas, short[][] feats
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

        final int dir = (prnt < dpnt) ? ra : la;

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
        f[c++] = dl2.csa(s_dir, dir);
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.csa(s_dir, dir);
        dl2.v0 = n++;
        dl2.v2 = dF;
        dl2.v3 = pP;
        dl2.cz4();
        f[c++] = dl2.csa(s_dir, dir);
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.csa(s_dir, dir);


        dwwp.v1 = label;
        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = dF;
        dwwp.cz4();
        f[c++] = dwwp.csa(s_dir, dir);

        dl1.v1 = label;
        dl1.v0 = n++;
        dl1.v2 = dP;
        dl1.cz3();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.cz3();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = dP;
        dl1.cz4();
        f[c++] = dl1.csa(s_dir, dir);

        int pPm1 = prnt > 0 ? pposs[prnt - 1] : s_str, dPm1 = dpnt > 0 ? pposs[dpnt - 1] : s_str;
        int pPp1 = prnt < pposs.length - 1 ? pposs[prnt + 1] : s_end, dPp1 = dpnt < pposs.length - 1 ? pposs[dpnt + 1] : s_end;

        int pPm2 = prnt > 1 ? pposs[prnt - 2] : s_str, dPm2 = dpnt > 1 ? pposs[dpnt - 2] : s_str;
        int pPp2 = prnt < pposs.length - 2 ? pposs[prnt + 2] : s_end, dPp2 = dpnt < pposs.length - 2 ? pposs[dpnt + 2] : s_end;

        int pFm1 = prnt > 0 ? form[prnt - 1] : s_stwrd, dFm1 = dpnt > 0 ? form[dpnt - 1] : s_stwrd;
        int pFp1 = prnt < form.length - 1 ? form[prnt + 1] : s_stwrd, dFp1 = dpnt < form.length - 1 ? form[dpnt + 1] : s_stwrd;



        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = pPp1;
        dl1.v4 = dP;
        dl1.v5 = dPp1;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v5 = dPm1;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = pPm1;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v5 = dPp1;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);


        dl1.v0 = n++;
        dl1.v3 = pPm1;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = dPm1;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = dPp1;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = pPp1;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);

        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = pPp2;
        dl1.v4 = dP;
        dl1.v5 = dPp2;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v5 = dPm2;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = pPm2;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v5 = dPp2;
        dl1.cz6();
        f[n++] = dl1.csa(s_dir, dir);

        dl1.v0 = n++;
        dl1.v3 = pPm2;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = dPm2;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = dPp2;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v3 = pPp2;
        dl1.cz5();
        f[n++] = dl1.csa(s_dir, dir);



        dl2.v0 = n++;
        dl2.v3 = dFm1;
        dl2.v3 = pPp1;
        dl2.v4 = pP;
        dl2.cz5();
        f[n++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = dFp1;
        dl2.v3 = pPm1;
        dl2.cz5();
        f[n++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFm1;
        dl2.v3 = dPp1;
        dl2.v4 = dP;
        dl2.cz5();
        f[n++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFp1;
        dl2.v3 = dPm1;
        dl2.cz5();
        f[n++] = dl2.getVal();


        dl2.v0 = n++;
        dl2.v3 = dFm1;
        dl2.v3 = dPm2;
        dl2.v4 = pP;
        dl2.cz5();
        f[n++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = dFp1;
        dl2.v3 = dPp2;
        dl2.cz5();
        f[n++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFm1;
        dl2.v3 = pPm2;
        dl2.v4 = dP;
        dl2.cz5();
        f[n++] = dl2.getVal();
        dl2.v0 = n++;
        dl2.v3 = pFp1;
        dl2.v3 = pPp2;
        dl2.cz5();
        f[n++] = dl2.getVal();


        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = dF;
        dwwp.v4 = dP;
        dwwp.cz5();
        f[n++] = dwwp.csa(s_dir, dir);
        dwwp.v0 = n++;
        dwwp.v2 = pF;
        dwwp.v3 = dF;
        dwwp.v4 = pP;
        dwwp.cz5();
        f[n++] = dwwp.csa(s_dir, dir);
        dwwp.v0 = n++;
        dwwp.v2 = dF;
        dwwp.v3 = pF;
        dwwp.v4 = pP;
        dwwp.v4 = dP;
        dwwp.cz6();
        f[n++] = dwwp.csa(s_dir, dir);



        // lemmas

        dl2.v1 = label;
        dl2.v0 = n++;
        dl2.v2 = pL;
        dl2.v3 = dP;
        dl2.cz4();
        f[c++] = dl2.csa(s_dir, dir);
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.csa(s_dir, dir);
        dl2.v0 = n++;
        dl2.v2 = dL;
        dl2.v3 = pP;
        dl2.cz4();
        f[c++] = dl2.csa(s_dir, dir);
        dl2.v0 = n++;
        dl2.cz3();
        f[c++] = dl2.csa(s_dir, dir);


        dwwp.v1 = label;
        dwwp.v0 = n++;
        dwwp.v2 = pL;
        dwwp.v3 = dL;
        dwwp.cz4();
        f[c++] = dwwp.csa(s_dir, dir);

        dwp.v1 = label;
        dwp.v0 = n++;
        dwp.v2 = dL;
        dwp.v3 = pP;
        dwp.v4 = dP;
        dwp.v5 = pL;
        dwp.cz6();
        f[c++] = dwp.csa(s_dir, dir);
        dwp.v0 = n++;
        dwp.cz5();
        f[c++] = dwp.csa(s_dir, dir);

        dwp.v0 = n++;
        dwp.v2 = pL;
        dwp.cz5();
        f[c++] = dwp.csa(s_dir, dir);
        dwwp.v0 = n++;
        dwwp.v2 = pL;
        dwwp.v3 = dL;
        dwwp.v4 = dP;
        dwwp.cz5();
        f[c++] = dwwp.csa(s_dir, dir);
        dwwp.v0 = n++;
        dwwp.v4 = pP;
        dwwp.cz5();
        f[c++] = dwwp.csa(s_dir, dir);


        // cluster

        d2pw.v1 = label;
        d2pw.v0 = n++;
        d2pw.v2 = prntLS;
        d2pw.v3 = chldLS;
        d2pw.cz4();
        f[c++] = d2pw.csa(s_dir, dir);
        d2pw.v0 = n++;
        d2pw.v4 = pF;
        d2pw.cz5();
        f[c++] = d2pw.csa(s_dir, dir);
        d2pw.v0 = n++;
        d2pw.v4 = dF;
        d2pw.cz5();
        f[c++] = d2pw.csa(s_dir, dir);
        d2pw.v0 = n++;
        d2pw.v5 = pF;
        d2pw.cz6();
        f[c++] = d2pw.csa(s_dir, dir);


        d2pp.v1 = label;
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = chldLS;
        d2pp.cz4();
        f[c++] = d2pp.csa(s_dir, dir);
        d2pp.v0 = n++;
        d2pp.v4 = pP;
        d2pp.cz5();
        f[c++] = d2pp.csa(s_dir, dir);
        d2pp.v0 = n++;
        d2pp.v4 = dP;
        d2pp.cz5();
        f[c++] = d2pp.csa(s_dir, dir);
        d2pp.v0 = n++;
        d2pp.v5 = pP;
        d2pp.cz6();
        f[c++] = d2pp.csa(s_dir, dir);


        short[] prel = is.plabels[i];
        short[] phead = is.pheads[i];


        //take those in for stacking
        //	dl2.v1=label;
        //	dl2.v0= n++;dl2.v2=prel[dpnt];dl2.v3=pP;dl2.v4=dP; dl2.v5=prnt==phead[dpnt]?1:2; dl2.cz6(); f[c++]=dl2.csa(s_dir,dir); 
        //	dl2.v0= n++;dl2.v2=pP;dl2.v3=dP; dl2.v4=prnt==phead[dpnt]?1:2; dl2.cz5(); f[c++]=dl2.csa(s_dir,dir); 



        if (feats == null) {
            return;
        }

        short[] featsP = feats[prnt], featsD = feats[dpnt];
        dlf.v0 = n++;
        dlf.v1 = label;
        dlf.v2 = pP;
        dlf.v3 = dP;
        extractFeat(f, c, dir, featsP, featsD);
    }

    @Override
    public void gcm(Instances is, int i, int p, int d, int gc, int label, Cluster cluster, long[] f) {

        for (int k = 0; k < f.length; k++) {
            f[k] = 0;
        }

        short[] pos = is.pposs[i];
        int[] forms = is.forms[i];
        int[] lemmas = is.plemmas[i];
        short[][] feats = is.feats[i];

        int pP = pos[p], dP = pos[d];
        int prntF = forms[p], chldF = forms[d];
        int prntL = lemmas[p], chldL = lemmas[d];
        int prntLS = prntF == -1 ? -1 : cluster.getLP(prntF), chldLS = chldF == -1 ? -1 : cluster.getLP(chldF);

        int gP = gc != -1 ? pos[gc] : s_str;
        int gcF = gc != -1 ? forms[gc] : s_stwrd;
        int gcL = gc != -1 ? lemmas[gc] : s_stwrd;
        int gcLS = (gc != -1) && (gcF != -1) ? cluster.getLP(gcF) : s_stwrd;

        if (prntF > maxForm) {
            prntF = -1;
        }
        if (prntL > maxForm) {
            prntL = -1;
        }

        if (chldF > maxForm) {
            chldF = -1;
        }
        if (chldL > maxForm) {
            chldL = -1;
        }

        if (gcF > maxForm) {
            gcF = -1;
        }
        if (gcL > maxForm) {
            gcL = -1;
        }


        int dir = (p < d) ? ra : la, dir_gra = (d < gc) ? ra : la;

        int n = 84, c = 0;

        //dl1.v023();
        dl1.v1 = label;
        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = dP;
        dl1.v4 = gP;
        dl1.cz5();
        dl1.cs(s_dir, dir);
        f[c++] = dl1.csa(s_dir, dir_gra);
        dl1.v0 = n++;
        dl1.v2 = pP;
        dl1.v3 = gP;
        dl1.cz4();
        dl1.cs(s_dir, dir);
        f[c++] = dl1.csa(s_dir, dir_gra);
        dl1.v0 = n++;
        dl1.v2 = dP;
        dl1.cz4();
        dl1.cs(s_dir, dir);
        f[c++] = dl1.csa(s_dir, dir_gra);

        dwwp.v1 = label;
        dwwp.v0 = n++;
        dwwp.v2 = prntF;
        dwwp.v3 = gcF;
        dwwp.cz4();
        dwwp.cs(s_dir, dir);
        f[c++] = dwwp.csa(s_dir, dir_gra);

        dwwp.v0 = n++;
        dwwp.v2 = chldF;
        dwwp.v3 = gcF;
        dwwp.cz4();
        dwwp.cs(s_dir, dir);
        f[c++] = dwwp.csa(s_dir, dir_gra);

        dwp.v1 = label;
        dwp.v0 = n++;
        dwp.v2 = gcF;
        dwp.v3 = pP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = gcF;
        dwp.v3 = dP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = prntF;
        dwp.v3 = gP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = chldF;
        dwp.v3 = gP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);


        // lemma

        dwwp.v0 = n++;
        dwwp.v2 = prntL;
        dwwp.v3 = gcL;
        dwwp.cz4();
        dwwp.cs(s_dir, dir);
        f[c++] = dwwp.csa(s_dir, dir_gra);

        dwwp.v0 = n++;
        dwwp.v2 = chldL;
        dwwp.v3 = gcL;
        dwwp.cz4();
        dwwp.cs(s_dir, dir);
        f[c++] = dwwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = gcL;
        dwp.v3 = pP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = gcL;
        dwp.v3 = dP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = prntL;
        dwp.v3 = gP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);

        dwp.v0 = n++;
        dwp.v2 = chldL;
        dwp.v3 = gP;
        dwp.cz4();
        dwp.cs(s_dir, dir);
        f[c++] = dwp.csa(s_dir, dir_gra);


        // clusters 			   	

        d2lp.v1 = label;
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = gcLS;
        d2lp.cz4();
        d2lp.cs(s_dir, dir);
        f[c++] = d2lp.csa(s_dir, dir_gra);// f.add(li.l2i(l)); 		  
        d2lp.v0 = n++;
        d2lp.v2 = chldLS;
        d2lp.v3 = gcLS;
        d2lp.cz4();
        d2lp.cs(s_dir, dir);
        f[c++] = d2lp.csa(s_dir, dir_gra);
        d3lp.v0 = n++;
        d3lp.v1 = label;
        d3lp.v2 = prntLS;
        d3lp.v3 = chldLS;
        d3lp.v4 = gcLS;
        d3lp.cz5();
        d3lp.cs(s_dir, dir);
        f[c++] = d3lp.csa(s_dir, dir_gra);

        //_f83;		
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = chldLS;
        d2lp.v4 = gcF;
        d2lp.cz5();
        f[c++] = d2lp.csa(s_dir, dir);
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = gcLS;
        d2lp.v4 = chldF;
        d2lp.cz5();
        f[c++] = d2lp.csa(s_dir, dir);
        d2lp.v0 = n++;
        d2lp.v2 = chldLS;
        d2lp.v3 = gcLS;
        d2lp.v4 = prntF;
        d2lp.cz5();
        f[c++] = d2lp.csa(s_dir, dir);

        d2pp.v1 = label;
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = chldLS;
        d2pp.v4 = gP;
        d2pp.cz5();
        f[c++] = d2pp.csa(s_dir, dir);
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = gcLS;
        d2pp.v4 = dP;
        d2pp.cz5();
        f[c++] = d2pp.csa(s_dir, dir);
        d2pp.v0 = n++;
        d2pp.v2 = chldLS;
        d2pp.v3 = gcLS;
        d2pp.v4 = pP;
        d2pp.cz5();
        f[c++] = d2pp.csa(s_dir, dir);



        // linear features

        int prntPm1 = p != 0 ? pos[p - 1] : s_str; // parent-pos-minus1
        int chldPm1 = d - 1 >= 0 ? pos[d - 1] : s_str;  // child-pos-minus1
        int prntPp1 = p != pos.length - 1 ? pos[p + 1] : s_end;
        int chldPp1 = d != pos.length - 1 ? pos[d + 1] : s_end;

        int gcPm1 = gc > 0 ? pos[gc - 1] : s_str;
        int gcPp1 = gc < pos.length - 1 ? pos[gc + 1] : s_end;

        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = dP;
        dl1.v4 = chldPp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = dP;
        dl1.v4 = chldPm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPp1;
        dl1.v4 = chldPm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcPm1;
        dl1.v3 = gP;
        dl1.v4 = chldPm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPp1;
        dl1.v4 = dP;
        dl1.v5 = chldPp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcPm1;
        dl1.v3 = gP;
        dl1.v4 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = pP;
        dl1.v4 = prntPp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = pP;
        dl1.v4 = prntPm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPp1;
        dl1.v4 = prntPm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcPm1;
        dl1.v3 = gP;
        dl1.v4 = prntPm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcPp1;
        dl1.v4 = pP;
        dl1.v5 = prntPp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcPm1;
        dl1.v3 = gP;
        dl1.v4 = pP;
        dl1.v5 = prntPp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);


        int pLSp1 = p != pos.length - 1 ? forms[p + 1] == -1 ? -1 : cluster.getLP(forms[p + 1]) : _cend;
        int cLSp1 = d != pos.length - 1 ? forms[d + 1] == -1 ? -1 : cluster.getLP(forms[d + 1]) : _cend;
        int gcLSp1 = gc < pos.length - 1 ? forms[gc + 1] == -1 ? -1 : cluster.getLP(forms[gc + 1]) : s_end;

        int pLSm1 = p != 0 ? lemmas[p - 1] == -1 ? -1 : cluster.getLP(lemmas[p - 1]) : _cstr;
        int cLSm1 = d - 1 >= 0 ? lemmas[d - 1] == -1 ? -1 : cluster.getLP(lemmas[d - 1]) : _cstr;
        int gcLSm1 = gc > 0 ? lemmas[gc - 1] == -1 ? -1 : cluster.getLP(lemmas[gc - 1]) : _cstr;


        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = dP;
        dl1.v4 = cLSp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = dP;
        dl1.v4 = cLSm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSp1;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcLSm1;
        dl1.v3 = gP;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSp1;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = cLSm1;
        dl1.v3 = gP;
        dl1.v4 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = pP;
        dl1.v4 = pLSp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = pP;
        dl1.v4 = pLSm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSp1;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcLSm1;
        dl1.v3 = gP;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gP;
        dl1.v3 = gcLSp1;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = gcLSm1;
        dl1.v3 = gP;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);



        short[] prel = is.plabels[i], phead = is.pheads[i];

        int g = p == phead[d] ? 1 : 2;
        if (gc >= 0) {
            g += d == phead[gc] ? 4 : 8;
        }

        int gr = gc == -1 ? s_relend : prel[gc];

        // take those in for stacking
	/*
         * dl2.v1=label; dl2.v0=
         * n++;dl2.v2=prel[d];dl2.v3=g;dl2.v4=gP;dl2.v5=dP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=prel[d];dl2.v3=g;dl2.v4=gP;dl2.v5=pP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=prel[d];dl2.v3=g;dl2.v4=gP;dl2.v5=pP;dl2.v6=dP;dl2.cz7();f[c++]=dl2.csa(s_dir,dir);          *
         * dl2.v0=
         * n++;dl2.v2=gr;dl2.v3=g;dl2.v4=gP;dl2.v5=dP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=gr;dl2.v3=g;dl2.v4=gP;dl2.v5=pP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=gr;dl2.v3=g;dl2.v4=gP;dl2.v5=pP;dl2.v6=dP;dl2.cz7();f[c++]=dl2.csa(s_dir,dir);          *
         */
        if (feats == null) {
            return;
        }

        short[] featsP = feats[d];
        short[] featsD = gc != -1 ? feats[gc] : null;

        dlf.v0 = n++;
        dlf.v1 = label;
        dlf.v2 = gP;
        dlf.v3 = dP;
        extractFeat(f, c, dir, featsP, featsD);
    }

    @Override
    public void siblingm(Instances is, int i, short pos[], int forms[], int[] lemmas, short[][] feats, int prnt, int d, int sblng, int label, Cluster cluster, long[] f, int v) {

        for (int k = 0; k < f.length; k++) {
            f[k] = 0;
        }

        int pP = pos[prnt], dP = pos[d];
        int prntF = forms[prnt], chldF = forms[d];
        int prntL = lemmas[prnt], chldL = lemmas[d];
        int prntLS = prntF == -1 ? -1 : cluster.getLP(prntF), chldLS = chldF == -1 ? -1 : cluster.getLP(chldF);

        int sP = sblng != -1 ? pos[sblng] : s_str, sblF = sblng != -1 ? forms[sblng] : s_stwrd, sblL = sblng != -1 ? lemmas[sblng] : s_stwrd;

        int sblLS = (sblng != -1) && (sblF != -1) ? cluster.getLP(sblF) : s_stwrd;


        int dir = (prnt < d) ? ra : la;

        int abs = Math.abs(prnt - d);

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
            dist = di0;
        }

        int n = 147;

        if (prntF > maxForm) {
            prntF = -1;
        }
        if (prntL > maxForm) {
            prntL = -1;
        }

        if (chldF > maxForm) {
            chldF = -1;
        }
        if (chldL > maxForm) {
            chldL = -1;
        }

        if (sblF > maxForm) {
            sblF = -1;
        }
        if (sblL > maxForm) {
            sblL = -1;
        }


        dl1.v0 = n++;
        dl1.v1 = label;
        dl1.v2 = pP;
        dl1.v3 = dP;
        dl1.v4 = sP;
        dl1.cz5();
        f[0] = dl1.csa(s_dir, dir);
        f[1] = dl1.csa(s_dist, dist);
        dl1.v0 = n++;
        dl1.v3 = sP;
        dl1.cz4();
        f[2] = dl1.csa(s_dir, dir);
        f[3] = dl1.csa(s_dist, dist);
        dl1.v0 = n++;
        dl1.v2 = dP;
        dl1.cz4();
        f[4] = dl1.csa(s_dir, dir);
        f[5] = dl1.csa(s_dist, dist);

        // sibling only could be tried
        dwwp.v1 = label;
        dwwp.v0 = n++;
        dwwp.v2 = prntF;
        dwwp.v3 = sblF;
        dwwp.cz4();
        f[6] = dwwp.csa(s_dir, dir);
        f[7] = dwwp.csa(s_dist, dist);
        dwwp.v0 = n++;
        dwwp.v2 = chldF;
        dwwp.cz4();
        f[8] = dwwp.csa(s_dir, dir);
        f[9] = dwwp.csa(s_dist, dist);
        dwp.v0 = n++;
        dwp.v1 = label;
        dwp.v2 = sblF;
        dwp.v3 = pP;
        dwp.cz4();
        f[10] = dwp.csa(s_dir, dir);
        f[11] = dwp.csa(s_dist, dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */
        dwp.v3 = dP;
        dwp.cz4();
        f[12] = dwp.csa(s_dir, dir);
        f[13] = dwp.csa(s_dist, dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */ dwp.v2 = prntF;
        dwp.v3 = sP;
        dwp.cz4();
        f[14] = dwp.csa(s_dir, dir);
        f[15] = dwp.csa(s_dist, dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */ dwp.v2 = chldF;
        dwp.cz4();
        f[16] = dwp.csa(s_dir, dir);
        f[17] = dwp.csa(s_dist, dist);

        //lemmas
        dwwp.v0 = n++;
        dwwp.v2 = prntL;
        dwwp.v3 = sblL;
        dwwp.cz4();
        f[18] = dwwp.csa(s_dir, dir);
        dwwp.v0 = n++;
        dwwp.v2 = chldL;
        dwwp.cz4();
        f[19] = dwwp.csa(s_dir, dir);
        f[20] = dwwp.csa(s_dist, dist);
        dwp.v0 = n++;	/*
         * dwp.v1=label;
         */ dwp.v2 = sblL;
        dwp.v3 = pP;
        dwp.cz4();
        f[21] = dwp.csa(s_dir, dir);
        f[22] = dwp.csa(s_dist, dist);
        dwp.v0 = n++; /*
         * dwp.v1=label;
         */ dwp.v3 = dP;
        dwp.cz4();
        f[23] = dwp.csa(s_dir, dir);
        f[24] = dwp.csa(s_dist, dist);
        dwp.v0 = n++; /*
         * dwp.v1=label;
         */ dwp.v2 = prntL;
        dwp.v3 = sP;
        dwp.cz4();
        f[25] = dwp.csa(s_dir, dir);
        f[26] = dwp.csa(s_dist, dist);
        dwp.v0 = n++; /*
         * dwp.v1=label;
         */ dwp.v2 = chldL;
        dwp.cz4();
        f[27] = dwp.csa(s_dir, dir);
        f[28] = dwp.csa(s_dist, dist);


        // clusters

        d2lp.v1 = label;
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = sblLS;
        d2lp.cz4();
        f[29] = d2lp.csa(s_dir, dir);
        d2lp.v0 = n++;
        d2lp.v2 = chldLS;
        d2lp.v3 = sblLS;
        d2lp.cz4();
        f[30] = d2lp.csa(s_dir, dir);
        f[31] = d2lp.csa(s_dist, dist);

        d3lp.v1 = label;
        d3lp.v0 = n++;
        d3lp.v2 = prntLS;
        d3lp.v3 = chldLS;
        d3lp.v4 = sblLS;
        d3lp.cz5();
        f[32] = d3lp.csa(s_dir, dir);

        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = chldLS;
        d2lp.v4 = sblF;
        d2lp.cz5();
        f[33] = d2lp.csa(s_dir, dir);
        f[34] = d2lp.csa(s_dist, dist);
        d2lp.v0 = n++;
        d2lp.v2 = prntLS;
        d2lp.v3 = sblLS;
        d2lp.v4 = chldF;
        d2lp.cz5();
        f[35] = d2lp.csa(s_dir, dir);
        f[36] = d2lp.csa(s_dist, dist);
        d2lp.v0 = n++;
        d2lp.v2 = chldLS;
        d2lp.v3 = sblLS;
        d2lp.v4 = prntF;
        d2lp.cz5();
        f[37] = d2lp.csa(s_dir, dir);
        f[38] = d2lp.csa(s_dist, dist);

        d2pp.v1 = label;
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = chldLS;
        d2pp.v4 = sP;
        d2pp.cz5();
        f[39] = d2pp.csa(s_dir, dir);
        f[40] = d2pp.csa(s_dist, dist);
        d2pp.v0 = n++;
        d2pp.v2 = prntLS;
        d2pp.v3 = sblLS;
        d2pp.v4 = dP;
        d2pp.cz5();
        f[41] = d2pp.csa(s_dir, dir);
        f[42] = d2pp.csa(s_dist, dist);
        d2pp.v0 = n++;
        d2pp.v2 = chldLS;
        d2pp.v3 = sblLS;
        d2pp.v4 = pP;
        d2pp.cz5();
        f[43] = d2pp.csa(s_dir, dir);
        f[44] = d2pp.csa(s_dist, dist);


        int prntPm1 = prnt != 0 ? pos[prnt - 1] : s_str;
        int chldPm1 = d - 1 >= 0 ? pos[d - 1] : s_str;
        int prntPp1 = prnt != pos.length - 1 ? pos[prnt + 1] : s_end;
        int chldPp1 = d != pos.length - 1 ? pos[d + 1] : s_end;

        // sibling part of speech minus and plus 1
        int sblPm1 = sblng > 0 ? pos[sblng - 1] : s_str;
        int sblPp1 = sblng < pos.length - 1 ? pos[sblng + 1] : s_end;

        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[45] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[46] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = prntPp1;
        dl1.cz5();
        f[47] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = prntPm1;
        dl1.cz5();
        f[48] = dl1.csa(s_dir, dir);// f.add(li.l2i(l));
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPp1;
        dl1.v4 = prntPm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[49] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sblPm1;
        dl1.v3 = sP;
        dl1.v4 = prntPm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[50] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPp1;
        dl1.v4 = pP;
        dl1.v5 = prntPp1;
        dl1.cz6();
        f[51] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sblPm1;
        dl1.v3 = sP;
        dl1.v4 = pP;
        dl1.v5 = prntPp1;
        dl1.cz6();
        f[52] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[53] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[54] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = chldPp1;
        dl1.cz5();
        f[55] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = chldPm1;
        dl1.cz5();
        f[56] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPp1;
        dl1.v4 = chldPm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[57] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sblPm1;
        dl1.v3 = sP;
        dl1.v4 = chldPm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[58] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sblPp1;
        dl1.v4 = dP;
        dl1.v5 = chldPp1;
        dl1.cz6();
        f[59] = dl1.csa(s_dir, dir);// f.add(li.l2i(l)); 
        dl1.v0 = n++;
        dl1.v2 = sblPm1;
        dl1.v3 = sP;
        dl1.v4 = dP;
        dl1.v5 = chldPp1;
        dl1.cz6();
        f[60] = dl1.csa(s_dir, dir);

        int c = 61;

        int pLSp1 = prnt != pos.length - 1 ? forms[prnt + 1] == -1 ? -1 : cluster.getLP(forms[prnt + 1]) : _cend;
        int cLSp1 = d != pos.length - 1 ? forms[d + 1] == -1 ? -1 : cluster.getLP(forms[d + 1]) : _cend;
        int sLSp1 = sblng < pos.length - 1 ? forms[sblng + 1] == -1 ? -1 : cluster.getLP(forms[sblng + 1]) : _cend;

        int pLSm1 = prnt != 0 ? forms[prnt - 1] == -1 ? -1 : cluster.getLP(forms[prnt - 1]) : _cstr;
        int cLSm1 = d - 1 >= 0 ? forms[d - 1] == -1 ? -1 : cluster.getLP(forms[d - 1]) : _cstr;
        int sLSm1 = sblng > 0 ? forms[sblng - 1] == -1 ? -1 : cluster.getLP(forms[sblng - 1]) : _cstr;

        //int c=61;

        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);



        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = pP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = pP;
        dl1.v4 = pLSm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pLSm1;
        dl1.v5 = pP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = pP;
        dl1.v5 = pLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = dP;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSp1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = dP;
        dl1.v4 = cLSm1;
        dl1.cz5();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSm1;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = cLSm1;
        dl1.v5 = dP;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sP;
        dl1.v3 = sLSp1;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);
        dl1.v0 = n++;
        dl1.v2 = sLSm1;
        dl1.v3 = sP;
        dl1.v4 = dP;
        dl1.v5 = cLSp1;
        dl1.cz6();
        f[c++] = dl1.csa(s_dir, dir);

        // take those in for stacking

        /*
         * short[] prel = is.plabels[i],phead=is.pheads[i];
         *
         * int g = prnt==phead[d]?1:2 ; if (sblng>=0) g +=
         * prnt==phead[sblng]?4:8;
         *
         * int gr = sblng==-1?s_relend:prel[sblng];
         *
         *
         * dl2.v0=
         * n++;dl2.v2=prel[d];dl2.v3=g;dl2.v4=sP;dl2.v5=dP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=prel[d];dl2.v3=g;dl2.v4=sP;dl2.v5=pP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=prel[d];dl2.v3=g;dl2.v4=sP;dl2.v5=pP;dl2.v6=dP;dl2.cz7();f[c++]=dl2.csa(s_dir,dir);          *
         * dl2.v0=
         * n++;dl2.v2=gr;dl2.v3=g;dl2.v4=sP;dl2.v5=dP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=gr;dl2.v3=g;dl2.v4=sP;dl2.v5=pP;dl2.cz6();f[c++]=dl2.csa(s_dir,dir);
         * dl2.v0=
         * n++;dl2.v2=gr;dl2.v3=g;dl2.v4=sP;dl2.v5=pP;dl2.v6=dP;dl2.cz7();f[c++]=dl2.csa(s_dir,dir);
         */

        if (feats == null) {
            return;
        }

        int cnt = c;

        short[] featsP = feats[d];
        short[] featsSbl = sblng != -1 ? feats[sblng] : null;

        dlf.v0 = n++;
        dlf.v1 = label;
        dlf.v2 = sP;
        dlf.v3 = dP;


        cnt = extractFeat(f, cnt, dir, featsP, featsSbl);

        featsP = feats[prnt];
        featsSbl = sblng != -1 ? feats[sblng] : null;

        dlf.v0 = n++;
        dlf.v1 = label;
        dlf.v2 = pP;
        dlf.v3 = sP;
        if (featsP != null && featsSbl != null) {
            for (short i1 = 0; i1 < featsP.length; i1++) {
                for (short i2 = 0; i2 < featsSbl.length; i2++) {
                    dlf.v4 = featsP[i1];
                    dlf.v5 = featsSbl[i2];
                    dlf.cz6();
                    f[cnt++] = dlf.csa(s_dir, prnt < sblng ? 1 : 2);
                }
            }
        } else if (featsP == null && featsSbl != null) {

            for (short i2 = 0; i2 < featsSbl.length; i2++) {
                dlf.v4 = nofeat;
                dlf.v5 = featsSbl[i2];
                dlf.cz6();
                f[cnt++] = dlf.csa(s_dir, dir);
            }

        } else if (featsP != null && featsSbl == null) {

            for (short i1 = 0; i1 < featsP.length; i1++) {
                dlf.v4 = featsP[i1];
                dlf.v5 = nofeat;
                dlf.cz6();
                f[cnt++] = dlf.csa(s_dir, dir);
            }
        }
    }

    private int extractFeat(long[] f, int cnt, int dir, short[] featsP, short[] featsD) {
        if (featsP != null && featsD != null) {
            for (short i1 = 0; i1 < featsP.length; i1++) {
                for (short i2 = 0; i2 < featsD.length; i2++) {
                    dlf.v4 = featsP[i1];
                    dlf.v5 = featsD[i2];
                    dlf.cz6();
                    f[cnt++] = dlf.csa(s_dir, dir);
                }
            }
        } else if (featsP == null && featsD != null) {

            for (short i2 = 0; i2 < featsD.length; i2++) {
                dlf.v4 = nofeat;
                dlf.v5 = featsD[i2];
                dlf.cz6();
                f[cnt++] = dlf.csa(s_dir, dir);

            }
        } else if (featsP != null && featsD == null) {

            for (short i1 = 0; i1 < featsP.length; i1++) {
                dlf.v4 = featsP[i1];
                dlf.v5 = nofeat;
                dlf.cz6();
                f[cnt++] = dlf.csa(s_dir, dir);

            }
        }
        return cnt;
    }

    @Override
    public FV encodeCat(Instances is, int ic, short pposs[], int forms[], int[] lemmas, short[] heads, short[] types, short feats[][], Cluster cluster, FV f) {


        long[] svs = new long[250];

        for (int i = 1; i < heads.length; i++) {


            int n = basic(pposs, forms, heads[i], i, cluster, f);

            firstm(is, ic, heads[i], i, types[i], cluster, svs);
            for (int k = 0; k < svs.length; k++) {
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

            siblingm(is, ic, pposs, forms, lemmas, feats, heads[i], i, ch, types[i], cluster, svs, n);
            for (int k = 0; k < svs.length; k++) {
                dl1.map(f, svs[k]);
            }


            gcm(is, ic, heads[i], i, cmi, types[i], cluster, svs);
            for (int k = 0; k < svs.length; k++) {
                dl1.map(f, svs[k]);
            }

            gcm(is, ic, heads[i], i, cmo, types[i], cluster, svs);
            for (int k = 0; k < svs.length; k++) {
                dl1.map(f, svs[k]);
            }
        }

        return f;
    }

    @Override
    public float encode3(short[] pos, short heads[], short[] types, DataF d2) {

        double v = 0;
        for (int i = 1; i < heads.length; i++) {

            int dir = (heads[i] < i) ? 0 : 1;

            v += d2.pl[heads[i]][i];
            v += d2.lab[heads[i]][i][types[i]][dir];

            boolean left = i < heads[i];
            short[] labels = Edges.get(pos[heads[i]], pos[i], left);
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
            v += d2.sib[heads[i]][i][ch][dir][lid];
            v += d2.gra[heads[i]][i][cmi][dir][lid];
            v += d2.gra[heads[i]][i][cmo][dir][lid];
        }
        return (float) v;
    }

    /**
     * Provide the scores of the edges
     *
     * @param pos
     * @param heads
     * @param types
     * @param edgesScores
     * @param d2
     * @return
     */
    public static float encode3(short[] pos, short heads[], short[] types, float[] edgesScores, DataF d2) {

        double v = 0;
        for (int i = 1; i < heads.length; i++) {

            int dir = (heads[i] < i) ? 0 : 1;

            edgesScores[i] = d2.pl[heads[i]][i];
            edgesScores[i] += d2.lab[heads[i]][i][types[i]][dir];

            boolean left = i < heads[i];
            short[] labels = Edges.get(pos[heads[i]], pos[i], left);
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
            edgesScores[i] += d2.sib[heads[i]][i][ch][dir][lid];
            edgesScores[i] += d2.gra[heads[i]][i][cmi][dir][lid];
            edgesScores[i] += d2.gra[heads[i]][i][cmo][dir][lid];
            v += edgesScores[i];
        }
        return (float) v;
    }

    private static int rightmostRight(short[] heads, int head, int max) {
        int rightmost = -1;
        for (int i = head + 1; i < max; i++) {
            if (heads[i] == head) {
                rightmost = i;
            }
        }

        return rightmost;
    }

    private static int leftmostLeft(short[] heads, int head, int min) {
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
    protected static final String TYPE = "TYPE", DIR = "D", FEAT = "F";
    public static final String POS = "POS";
    protected static final String DIST = "DIST", MID = "MID";
    private static final String _0 = "0", _4 = "4", _3 = "3", _2 = "2", _1 = "1", _5 = "5", _10 = "10";
    private static int di0, d4, d3, d2, d1, d5, d10;
    private static final String WORD = "WORD", STWRD = "STWRD", STPOS = "STPOS";
    private static int nofeat;
    private static int maxForm;

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

        for (int k = 0; k < 215; k++) {
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

    /*
     * (non-Javadoc) @see extractors.Extractor#getType()
     */
    @Override
    public int getType() {
        return s_type;
    }

    /*
     * (non-Javadoc) @see extractors.Extractor#setMaxForm(java.lang.Integer)
     */
    @Override
    public void setMaxForm(int max) {
        maxForm = max;
    }

    /*
     * (non-Javadoc) @see extractors.Extractor#getMaxForm()
     */
    @Override
    public int getMaxForm() {
        return maxForm;
    }
}