/**
 * Copyright (c) 2009, Regents of the University of Colorado All rights
 * reserved.
 * 
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
* Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the University of Colorado at
 * Boulder nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package clear.parse;

import clear.decode.AbstractDecoder;
import clear.decode.OneVsAllDecoder;
import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLArg;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.ftr.map.SRLFtrMap;
import clear.ftr.xml.SRLFtrXml;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Shift-eager dependency parser.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/6/2010
 */
public class SRLParser extends AbstractSRLParser {

    /**
     * Label of Shift transition
     */
    static public final String LB_SHIFT = "SH";
    /**
     * Label of NoArc transition
     */
    static public final String LB_NO_ARC = "NA";
    /**
     * For {@link SRLParser#FLAG_TRAIN_BOOST} only.
     */
    protected DepTree d_copy = null;

    /**
     * {@link AbstractSRLParser#FLAG_TRAIN_LEXICON}.
     */
    public SRLParser(byte flag, String xmlFile) {
        super(flag, xmlFile);
    }

    /**
     * {@link AbstractSRLParser#FLAG_TRAIN_INSTANCE}.
     */
    public SRLParser(byte flag, SRLFtrXml xml, String[] lexiconFile) {
        super(flag, xml, lexiconFile);
    }

    /**
     * {@link AbstractSRLParser#FLAG_PREDICT} or {@link AbstractSRLParser#FLAG_TRAIN_BOOST}.
     */
    public SRLParser(byte flag, SRLFtrXml xml, SRLFtrMap[] map, AbstractDecoder[] decoder) {
        super(flag, xml, map, decoder);
    }

    /**
     * Initializes member variables.
     */
    private void init(DepTree tree) {
        tree.setSubcat();

        d_tree = tree;
        i_beta = tree.nextPredicateId(0);
        i_lambda = i_beta - 1;
        i_dir = DIR_LEFT;

        ls_args = new ArrayList<>();
        ls_argn = new ArrayList<>();

        if (i_flag == FLAG_PREDICT || i_flag == FLAG_TRAIN_BOOST) {
            d_copy = tree.clone();
            d_tree.clearSRLHeads();
        }
    }

    /**
     * Parses
     * <code>tree</code>.
     */
    @Override
    public void parse(DepTree tree) {
        init(tree);

        while (i_beta < tree.size()) {
            if (i_lambda <= 0 || i_lambda >= tree.size()) {
                shift();
            } else if (i_flag == FLAG_PREDICT) {
                predict();
            } else if (i_flag == FLAG_TRAIN_BOOST) {
                trainConditional();
            } else {
                train();
            }
        }
    }

    /**
     * Trains the dependency tree ({@link SRLParser#d_tree}).
     */
    private void train() {
        String label = getGoldLabel(d_tree);

        if (label.equals(LB_NO_ARC)) {
            noArc(1d);
        } else {
            yesArc(label, 1d);
        }
    }

    /**
     * Predicts dependencies.
     */
    private void predict() {
        predictAux(getFeatureArray());
    }

    private void predictAux(IntArrayList ftr) {
        SRLFtrMap map = getFtrMap();
        OneVsAllDecoder dec = getDecoder();
        JIntDoubleTuple res = dec.predict(ftr);

        String label = (res.i < 0) ? LB_NO_ARC : map.indexToLabel(res.i);
        //	res.d = AbstractModel.logistic(res.d);

        if (label.equals(LB_NO_ARC)) {
            noArc(res.d);
        } else {
            yesArc(label, res.d);
        }
    }

    private void trainConditional() {
        String gLabel = getGoldLabel(d_copy);
        IntArrayList ftr = getFeatureArray();

        saveInstance(gLabel, ftr);
        predictAux(ftr);
    }

    private String getGoldLabel(DepTree tree) {
        DepNode lambda = tree.get(i_lambda);
        String label;

        if ((label = lambda.getLabel(i_beta)) != null) {
            return label;
        } else {
            return LB_NO_ARC;
        }
    }

    /**
     * Performs a shift transition.
     *
     * @param isDeterministic true if this is called for a deterministic-shift.
     */
    private void shift() {
        if (i_dir == DIR_RIGHT) {
            shiftRight();
            i_beta = d_tree.nextPredicateId(i_beta);
        }

        i_dir *= -1;
        i_lambda = i_beta + i_dir;
    }

    /**
     * Called from {@link SRLParser#shift()} for {@link AbstractSRLParser#DIR_RIGHT}.
     */
    private void shiftRight() {
        if (i_flag == FLAG_PREDICT || i_flag == FLAG_TRAIN_BOOST) {
            addArgs(ls_args);
        }

        ls_args.clear();
        ls_argn.clear();
    }

    private void addArgs(ArrayList<SRLArg> seq) {
        for (SRLArg arg : seq) {
            d_tree.get(arg.argId).addSRLHead(i_beta, arg.label, arg.score);
        }
    }

    /**
     * Performs a no-arc transition.
     */
    private void noArc(double score) {
        trainInstance(LB_NO_ARC);
        i_lambda += i_dir;
    }

    private String yesArc(String label, double score) {
        trainInstance(label);

        SRLArg arg = new SRLArg(i_lambda, label, score);

        ls_args.add(arg);
        if (label.matches("A\\d")) {
            ls_argn.add(label);
        }

        i_lambda += i_dir;
        return null;
    }

    private void trainInstance(String label) {
        if (i_flag == FLAG_TRAIN_LEXICON) {
            addTags(label);
        } else if (i_flag == FLAG_TRAIN_INSTANCE) {
            saveInstance(label, getFeatureArray());
        }
    }

    // ---------------------------- getFtr*() ----------------------------
    @Override
    protected void addLexica(SRLFtrMap map) {
        addNgramLexica(map);
        addSetLexica(map, 0, d_tree.getDeprelDepSet(i_beta));
        addStrLexica(map, 1, getPredArg());
    }

    protected void addSetLexica(SRLFtrMap map, int ftrId, AbstractCollection<String> ftrs) {
        for (String ftr : ftrs) {
            map.addExtra(ftrId, ftr);
        }
    }

    protected void addStrLexica(SRLFtrMap map, int ftrId, String ftr) {
        if (ftr != null) {
            map.addExtra(ftrId, ftr);
        }
    }

    protected String getPredArg() {
        if (i_dir == DIR_RIGHT) {
            return null;
        }
        SRLInfo info = d_tree.get(i_lambda).srlInfo;

        if (!info.heads.isEmpty()) {
            for (int i = info.heads.size() - 1; i >= 0; i--) {
                SRLHead head = info.heads.get(i);

                if (head.headId < i_beta) {
                    DepNode pred = d_tree.get(head.headId);
                    return pred.lemma + "_" + head.label;
                }
            }
        }

        return null;
    }

    protected IntArrayList getFeatureArray() {
        // add features
        IntArrayList arr = new IntArrayList();
        int idx[] = {1};
        SRLFtrMap map = getFtrMap();

        addNgramFeatures(arr, idx, map);
        addBinaryFeatures(arr, idx);
        addDistanceFeature(arr, idx);
        addSetFeatures(arr, idx, map, 0, d_tree.getDeprelDepSet(i_beta));
        addStrFeatures(arr, idx, map, 1, getPredArg());

        return arr;
    }

    protected void addBinaryFeatures(IntArrayList arr, int[] idx) {
        DepNode lambda = d_tree.get(i_lambda);
        DepNode beta = d_tree.get(i_beta);

        if (lambda.headId == i_beta) {
            arr.add(idx[0]);
        } else if (beta.headId == i_lambda) {
            arr.add(idx[0] + 1);
        } else if (d_tree.isAncestor(beta, lambda)) {
            arr.add(idx[0] + 2);	// for out-of-domain
        }
        while (DepLib.M_VC.matcher(beta.deprel).matches()) {
            beta = d_tree.get(beta.headId);

            if (d_tree.getDeprelDepSet(beta.id).contains(DepLib.DEPREL_SBJ)) {
                arr.add(idx[0] + 3);
                break;
            }
        }

        idx[0] += 4;
    }

    protected void addDistanceFeature(IntArrayList arr, int[] idx) {
        int dist = Math.abs(i_beta - i_lambda);

        if (dist <= 5) {
            dist = 0;
        } else if (dist <= 10) {
            dist = 1;
        } else {
            dist = 2;
        }

        arr.add(idx[0] + dist);
        idx[0] += 3;
    }

    protected void addSetFeatures(IntArrayList arr, int[] idx, SRLFtrMap map, int ftrId, AbstractCollection<String> ftrs) {
        IntArrayList list = new IntArrayList();
        int i;

        for (String ftr : ftrs) {
            if ((i = map.extraToIndex(ftrId, ftr)) >= 0) {
                list.add(idx[0] + i);
            }
        }

        int[] tmp = list.toArray();
        Arrays.sort(tmp);
        arr.add(tmp, 0, tmp.length);
        idx[0] += map.n_extra[ftrId];
    }

    protected void addStrFeatures(IntArrayList arr, int[] idx, SRLFtrMap map, int ftrId, String ftr) {
        if (ftr != null) {
            int index = map.extraToIndex(ftrId, ftr);
            if (index >= 0) {
                arr.add(idx[0] + index);
            }
        }

        idx[0] += map.n_extra[ftrId];
    }
//	==================================== SHIFT ====================================
    /*
     * private void predictBest() { if (i_lambda >= d_tree.size())	//
     * right-shift { dynamicPopulate(); return; } else if (i_lambda <= 0)	//
     * left-shift { shift(); predictBest(); return; }
     *
     * int iBeta = i_beta; int iLambda = i_lambda; byte iDir = i_dir;
     * ArrayList<SRLArg> lsArgs = new ArrayList<SRLArg>(ls_args);
     * ArrayList<String> lsArgn = new ArrayList<String>(ls_argn);
     *
     * SRLFtrMap map = getFtrMap(); OneVsAllDecoder dec = getDecoder();
     * JIntDoubleTuple[] res = dec.predictAll(getFeatureArray()); String label;
     * double score;
     *
     * for (int i=0; i<K; i++) { if (i != 0) { if (res[i].d < THRESHOLD)	break;
     *
     * i_beta = iBeta; i_lambda = iLambda; i_dir = iDir; ls_args = new
     * ArrayList<SRLArg>(lsArgs); ls_argn = new ArrayList<String>(lsArgn); }
     *
     * label = map.indexToLabel(res[i].i); score =
     * AbstractModel.logistic(res[i].d);
     *
     * if (label.equals(LB_NO_ARC)) noArc(score); else if ((label =
     * yesArc(label, score)) != null) { dynamicAttach(label); continue; }
     *
     * predictBest(); } }
     *
     * private void dynamicPick() { ArrayList<ArrayList<SRLArg>> list =
     * m_dynamic.get(KEY_REL); JObjectDoubleTuple<ArrayList<SRLArg>> max = new
     * JObjectDoubleTuple<ArrayList<SRLArg>>(null, -1); DepNode beta =
     * d_tree.get(i_beta); double score;
     *
     * for (ArrayList<SRLArg> seq : list) { score = getScore(beta, seq); if
     * (score > max.value)	max.set(seq, score); }
     *
     * addArgs(max.object); }
     *
     * private double getScore(DepNode pred, ArrayList<SRLArg> lsArgs) { double
     * score = 1;
     *
     * for (SRLArg arg : lsArgs) score *= arg.score;
     *
     * return score; }
     *
     * private void dynamicPopulate() { addDynamicList(KEY_REL, ls_args); int
     * size = ls_args.size();
     *
     * for (int i=0; i<size; i++) { SRLArg arg = ls_args.get(i);
     * addDynamicList(arg.toString(), new ArrayList<SRLArg>(ls_args.subList(i,
     * size))); } }
     *
     * private void dynamicAttach(String key) { ArrayList<ArrayList<SRLArg>>
     * list = m_dynamic.get(key);
     *
     * for (ArrayList<SRLArg> ls : list) { ArrayList<SRLArg> tmp = new
     * ArrayList<SRLArg>(ls_args);
     *
     * tmp.addAll(ls); addDynamicList(KEY_REL, tmp); } }
     *
     * private void addDynamicList(String key, ArrayList<SRLArg> seq) {
     * ArrayList<ArrayList<SRLArg>> list;
     *
     * if (m_dynamic.containsKey(key)) { list = m_dynamic.get(key); } else {
     * list = new ArrayList<ArrayList<SRLArg>>(); m_dynamic.put(key, list); }
     *
     * list.add(seq); }
     *
     * protected String seqToString(ArrayList<SRLArg> seq) { StringBuilder build
     * = new StringBuilder(); build.append(d_tree.get(i_beta).form);
     * NumberFormat formatter = new DecimalFormat("#0.0000");
     *
     * for (SRLArg arg : seq) { build.append(" "); build.append(arg.toString());
     * build.append(":"); build.append(formatter.format(arg.score)); }
     *
     * return build.toString(); }
     *
     * protected boolean isShift(double score) { if (b_checkShift)	b_checkShift
     * = false; else	return false;
     *
     * double[] arr = getShiftArray(score); if (arr == null)	return false;
     *
     * double sum = 0; double[] weight = {0.23017389186528628,
     * -1.4706302173489318, -0.1499877207617069, -1.180854067164068,
     * -0.38980112947136575, 0.10158032395092477, -2.073205316928537}; for (int
     * i=0; i<weight.length; i++) sum += (weight[i] * arr[i]); sum *= -1; return
     * (sum >= 1.5); }
     *
     * protected double[] getShiftArray(double score) { DepNode beta =
     * d_tree.get(i_beta);
     *
     * ObjectDoubleOpenHashMap<String> mProb1a = p_prob.get1aProbMap(beta,
     * i_dir); ObjectDoubleOpenHashMap<String> mProb2a =
     * p_prob.get2aProbMap(beta, s_prevArgA, i_dir);
     * ObjectDoubleOpenHashMap<String> mProb2n = p_prob.get2aProbMap(beta,
     * s_prevArgN, i_dir);
     *
     * JDoubleDoubleTuple prob1a = getEndArgProb(mProb1a); JDoubleDoubleTuple
     * prob2a = getEndArgProb(mProb2a); JDoubleDoubleTuple prob2n =
     * getEndArgProb(mProb2n);
     *
     * double[] arr = {prob1a.d1, prob2a.d1, prob2n.d1, -prob1a.d2, -prob2a.d2,
     * -prob2n.d2, -score}; double prob = 0; for (double d : arr)	prob += d;
     *
     * return (prob == 0) ? null : arr; }
     *
     * private JDoubleDoubleTuple getEndArgProb(ObjectDoubleOpenHashMap<String>
     * mPred) { JDoubleDoubleTuple max = new JDoubleDoubleTuple(0, 0); if (mPred
     * == null)	return max;
     *
     * String label;	double prob;
     *
     * for (ObjectCursor<String> cur : mPred.keySet()) { if ((label =
     * cur.value).equals(SRLProb.ARG_END)) { max.d1 = mPred.get(label); } else
     * if (!s_args.contains(label)) { if ((prob = mPred.get(label)) > max.d2)
     * max.d2 = prob; } }
     *
     * return max; }
     *
     * protected String getShiftEquation(double[] arr, boolean isShift) { //
     * DecimalFormat format = new DecimalFormat("#0.0000"); StringBuilder build
     * = new StringBuilder();
     *
     * build.append(d_tree.get(i_beta).form); build.append(" ");
     * build.append(d_tree.get(i_lambda).form); build.append(" ");
     *
     * if (isShift) { build.append( "1"); } else { build.append("-1"); }
     *
     * for (int i=0; i<arr.length; i++) { build.append(" ");
     * build.append((i+1)); build.append(":"); build.append(arr[i]); }
     *
     * return build.toString(); }
     *
     * protected boolean isShift(DepTree tree) { for (int i=i_lambda; 0<i &&
     * i<tree.size(); i+=i_dir) { if (tree.get(i).isSRLHead(i_beta)) return
     * false; }
     *
     * return true; }
     */
}