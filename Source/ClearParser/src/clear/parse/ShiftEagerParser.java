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

import clear.decode.AbstractMultiDecoder;
import clear.decode.OneVsAllDecoder;
import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.util.tuple.JIntDoubleTuple;
import clear.util.tuple.JObjectDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.util.ArrayList;

/**
 * Shift-eager dependency parser.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2011
 */
public class ShiftEagerParser extends AbstractDepParser {

    /**
     * Label of Shift transition
     */
    static public final String LB_SHIFT = "SH";
    /**
     * Label of No-Arc transition
     */
    static public final String LB_NO_ARC = "NA";
    /**
     * Label of Left-Arc transition
     */
    static public final String LB_LEFT_ARC = "LA";
    /**
     * Label of Right-Arc transition
     */
    static public final String LB_RIGHT_ARC = "RA";
    /**
     * Delimiter between transition and dependency label
     */
    static public final String LB_DELIM = "-";
    /**
     * {@link AbstractDepParser#FLAG_TRAIN_BOOST} only.
     */
    protected DepTree d_copy = null;

    /**
     * {@link ShiftEagerParser#FLAG_PRINT_TRANSITION} or {@link ShiftEagerParser#FLAG_TRAIN_LEXICON}.
     */
    public ShiftEagerParser(byte flag, String filename) {
        super(flag, filename);
    }

    /**
     * {@link ShiftEagerParser#FLAG_TRAIN_INSTANCE}.
     */
    public ShiftEagerParser(byte flag, DepFtrXml xml, String lexiconFile) {
        super(flag, xml, lexiconFile);
    }

    /**
     * {@link ShiftEagerParser#FLAG_PREDICT} or {@link ShiftEagerParser#FLAG_TRAIN_BOOST}.
     */
    public ShiftEagerParser(byte flag, DepFtrXml xml, DepFtrMap map, AbstractMultiDecoder decoder) {
        super(flag, xml, map, decoder);
    }

    /**
     * Initializes all pointers.
     */
    protected void init(DepTree tree) {
        preProcess(tree);

        d_tree = tree;
        i_lambda = 0;
        i_beta = 1;
        prev_trans = new ArrayList<>();

        if (i_flag == FLAG_PRINT_TRANSITION) {
            printTransition("", "");
        } else if (i_flag == FLAG_TRAIN_BOOST) {
            d_copy = tree.clone();
        }
    }

    /**
     * Parses the dependency tree.
     */
    @Override
    public void parse(DepTree tree) {
        init(tree);
        int size = tree.size();

        while (i_beta < size) // beta is not empty
        {
            if (i_lambda == -1) // lambda_1 is empty: deterministic shift
            {
                shift(true);
                continue;
            } else if (i_flag == FLAG_PREDICT) {
                predict();
            } else if (i_flag == FLAG_TRAIN_BOOST) {
                trainBoost();
            } else {
                train();
            }

            d_tree.n_trans++;
        }

        if (i_flag == FLAG_PRINT_TRANSITION) {
            f_out.println();
        } else if (i_flag == FLAG_PREDICT) {
            postProcess(LB_LEFT_ARC, LB_RIGHT_ARC);
        } else if (i_flag == FLAG_TRAIN_BOOST) {
            postProcessBoost();
        }
    }

    /**
     * Trains a dependency tree.
     */
    private void train() {
        DepNode lambda = d_tree.get(i_lambda);
        DepNode beta = d_tree.get(i_beta);

        if (lambda.headId == beta.id) {
            leftArc(lambda, beta, lambda.deprel, 1d);
        } else if (lambda.id == beta.headId) {
            rightArc(lambda, beta, beta.deprel, 1d);
        } else if (isShift(d_tree)) {
            shift(false);
        } else {
            noArc();
        }
    }

    /**
     * This method is called from {@link ShiftEagerParser#train()}.
     *
     * @return true if non-deterministic shift needs to be performed
     */
    protected boolean isShift(DepTree tree) {
        DepNode beta = tree.get(i_beta);

        for (int i = i_lambda; i >= 0; i--) {
            DepNode lambda = tree.get(i);

            if (lambda.headId == beta.id || lambda.id == beta.headId) {
                return false;
            }
        }

        return true;
    }

    /**
     * Predicts dependencies.
     */
    private void predict() {
        predictAux(getFeatureArray());
    }

    private void trainBoost() {
        String gLabel = getGoldLabel(d_copy);
        IntArrayList ftr = getFeatureArray();

        saveInstance(gLabel, ftr);
        predictAux(ftr);
    }

    private String predictAux(IntArrayList ftr) {
        JIntDoubleTuple res;

        res = c_dec.predict(ftr);

        String label = (res.i < 0) ? LB_NO_ARC : t_map.indexToLabel(res.i);
        int index = label.indexOf(LB_DELIM);
        String trans = (index > 0) ? label.substring(0, index) : label;
        String deprel = (index > 0) ? label.substring(index + 1) : "";
        DepNode lambda = d_tree.get(i_lambda);
        DepNode beta = d_tree.get(i_beta);

        if (trans.equals(LB_LEFT_ARC) && !d_tree.isAncestor(lambda, beta) && lambda.id != DepLib.ROOT_ID) {
            leftArc(lambda, beta, deprel, res.d);
        } else if (trans.equals(LB_RIGHT_ARC) && !d_tree.isAncestor(beta, lambda)) {
            rightArc(lambda, beta, deprel, res.d);
        } else if (trans.equals(LB_SHIFT)) {
            shift(false);
        } else {
            noArc();
        }

        return label;
    }

    private String getGoldLabel(DepTree tree) {
        DepNode lambda = tree.get(i_lambda);
        DepNode beta = tree.get(i_beta);

        if (lambda.headId == beta.id) {
            return LB_LEFT_ARC + LB_DELIM + lambda.deprel;
        } else if (lambda.id == beta.headId) {
            return LB_RIGHT_ARC + LB_DELIM + beta.deprel;
        } else if (isShift(tree)) {
            return LB_SHIFT;
        } else {
            return LB_NO_ARC;
        }
    }

    /**
     * Predicts dependencies for tokens that have not found their heads during
     * parsing.
     */
    protected void postProcess(String leftLabels, String rightLabels) {
        int currId, maxId, i, n = d_tree.size();
        JObjectDoubleTuple<String> max;
        DepNode curr, node;

        for (currId = 1; currId < n; currId++) {
            curr = d_tree.get(currId);
            if (curr.hasHead) {
                continue;
            }

            max = new JObjectDoubleTuple<>(null, -1000);
            maxId = -1;

            for (i = currId - 1; i >= 0; i--) {
                node = d_tree.get(i);
                if (d_tree.isAncestor(curr, node)) {
                    continue;
                }
                maxId = getMaxHeadId(curr, node, maxId, max, rightLabels);
            }

            for (i = currId + 1; i < d_tree.size(); i++) {
                node = d_tree.get(i);
                if (d_tree.isAncestor(curr, node)) {
                    continue;
                }
                maxId = getMaxHeadId(curr, node, maxId, max, leftLabels);
            }

            if (maxId != -1) {
                curr.setHead(maxId, max.object, max.value);
            }
        }
    }

    /**
     * This method is called from {@link ShiftEagerParser#postProcess()}.
     */
    protected int getMaxHeadId(DepNode curr, DepNode head, int maxId, JObjectDoubleTuple<String> max, String sTrans) {
        if (curr.id < head.id) {
            i_lambda = curr.id;
            i_beta = head.id;
        } else {
            i_lambda = head.id;
            i_beta = curr.id;
        }

        JIntDoubleTuple[] aRes;
        JIntDoubleTuple res;
        String label, trans;
        int index;

        aRes = ((OneVsAllDecoder) c_dec).predictAll(getFeatureArray());

        if (curr.id < head.id && t_map.indexToLabel(aRes[0].i).equals(LB_SHIFT)) {
            return maxId;
        }

        for (int i = 0; i < aRes.length; i++) {
            res = aRes[i];

            label = t_map.indexToLabel(res.i);
            index = label.indexOf(LB_DELIM);
            if (index == -1) {
                continue;
            }
            trans = label.substring(0, index);

            if (trans.matches(sTrans)) {
                if (max.value < res.d) {
                    String deprel = label.substring(index + 1);
                    max.set(deprel, res.d);
                    maxId = head.id;
                }
                break;
            }
        }

        return maxId;
    }

    private void postProcessBoost() {
        int currId, n = d_tree.size();
        DepNode curr;

        for (currId = 1; currId < n; currId++) {
            if (d_tree.get(currId).hasHead) {
                continue;
            }
            curr = d_copy.get(currId);

            i_lambda = currId - 1;
            i_beta = currId;

            if (isShift(d_copy)) {
                saveInstance(LB_SHIFT, getFeatureArray());
            }

            if (currId < curr.headId) {
                i_lambda = currId;
                i_beta = curr.headId;
            } else {
                i_lambda = curr.headId;
                i_beta = currId;
            }

            saveInstance(getGoldLabel(d_copy), getFeatureArray());
        }
    }

    /**
     * Performs a shift transition.
     *
     * @param isDeterministic true if this is called for a deterministic-shift.
     */
    protected void shift(boolean isDeterministic) {
        if (!isDeterministic) {
            trainInstance(LB_SHIFT);
        }

        i_lambda = i_beta++;
        prev_trans.clear();

        if (i_flag == FLAG_PRINT_TRANSITION) {
            if (isDeterministic) {
                printTransition("DT-SHIFT", "");
            } else {
                printTransition("NT-SHIFT", "");
            }
        }
    }

    /**
     * Performs a no-arc transition.
     */
    protected void noArc() {
        trainInstance(LB_NO_ARC);

        i_lambda--;
        prev_trans.add(LB_NO_ARC);

        if (i_flag == FLAG_PRINT_TRANSITION) {
            printTransition("NO-ARC", "");
        }
    }

    /**
     * Performs a left-arc transition.
     *
     * @param lambda lambda_1[0]
     * @param beta beta[0]
     * @param deprel dependency label between
     * <code>lambda</code> and
     * <code>beta</code>
     * @param score dependency score between
     * <code>lambda</code> and
     * <code>beta</code>
     */
    protected void leftArc(DepNode lambda, DepNode beta, String deprel, double score) {
        String label = LB_LEFT_ARC + LB_DELIM + deprel;
        trainInstance(label);

        lambda.setHead(beta.id, deprel, score);
        if (beta.leftMostDep == null || lambda.id < beta.leftMostDep.id) {
            beta.leftMostDep = lambda;
        }
        i_lambda--;
        prev_trans.add(label);

        if (i_flag == FLAG_PRINT_TRANSITION) {
            printTransition("LEFT-ARC", lambda.id + " <-" + deprel + "- " + beta.id);
        }
    }

    /**
     * Performs a right-arc transition.
     *
     * @param lambda lambda_1[0]
     * @param beta beta[0]
     * @param deprel dependency label between lambda_1[0] and beta[0]
     * @param score dependency score between lambda_1[0] and beta[0]
     */
    protected void rightArc(DepNode lambda, DepNode beta, String deprel, double score) {
        String label = LB_RIGHT_ARC + LB_DELIM + deprel;
        trainInstance(label);

        beta.setHead(lambda.id, deprel, score);
        if (lambda.rightMostDep == null || lambda.rightMostDep.id < beta.id) {
            lambda.rightMostDep = beta;
        }
        i_lambda--;
        prev_trans.add(label);

        if (i_flag == FLAG_PRINT_TRANSITION) {
            printTransition("RIGHT-ARC", lambda.id + " -" + deprel + "-> " + beta.id);
        }
    }
}