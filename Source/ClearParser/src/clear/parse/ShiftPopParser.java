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
import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;

/**
 * Shift-pop dependency parser.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2010
 */
public class ShiftPopParser extends ShiftEagerParser {

    /**
     * Label of Left-Pop transition
     */
    static public final String LB_LEFT_POP = "LP";
    private final String LB_LEFT_ARCPOP = LB_LEFT_ARC + "|" + LB_LEFT_POP;

    /**
     * {@link ShiftPopParser#FLAG_PRINT_TRANSITION} or {@link ShiftPopParser#FLAG_TRAIN_LEXICON}.
     */
    public ShiftPopParser(byte flag, String filename) {
        super(flag, filename);
    }

    /**
     * {@link ShiftPopParser#FLAG_TRAIN_INSTANCE}.
     */
    public ShiftPopParser(byte flag, DepFtrXml xml, String lexiconFile) {
        super(flag, xml, lexiconFile);
    }

    /**
     * {@link ShiftPopParser#FLAG_PREDICT} or {@link ShiftPopParser#FLAG_TRAIN_BOOST}.
     */
    public ShiftPopParser(byte flag, DepFtrXml xml, DepFtrMap map, AbstractMultiDecoder decoder) {
        super(flag, xml, map, decoder);
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
            } else if (tree.get(i_lambda).isSkip) {
                i_lambda--;
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
            postProcess(LB_LEFT_ARCPOP, LB_RIGHT_ARC);
        } else if (i_flag == FLAG_TRAIN_BOOST) {
            postProcessBoost();
        }
    }

    /**
     * Trains a dependency tree .
     */
    private void train() {
        DepNode lambda = d_tree.get(i_lambda);
        DepNode beta = d_tree.get(i_beta);

        if (lambda.headId == beta.id) {
            if (isPop(d_tree)) {
                leftPop(lambda, beta, lambda.deprel, 1d);
            } else {
                leftArc(lambda, beta, lambda.deprel, 1d);
            }
        } else if (lambda.id == beta.headId) {
            rightArc(lambda, beta, beta.deprel, 1d);
        } else if (isShift(d_tree)) {
            shift(false);
        } else {
            noArc();
        }
    }

    protected boolean isPop(DepTree tree) {
        int i, size = tree.size();

        for (i = i_beta + 1; i < size; i++) {
            if (tree.get(i).headId == i_lambda) {
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

        if (trans.equals(LB_LEFT_POP) && !d_tree.isAncestor(lambda, beta) && lambda.id != DepLib.ROOT_ID) {
            leftPop(lambda, beta, deprel, res.d);
        } else if (trans.equals(LB_LEFT_ARC) && !d_tree.isAncestor(lambda, beta) && lambda.id != DepLib.ROOT_ID) {
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

    private String getGoldLabel(DepTree tree) {
        DepNode lambda = tree.get(i_lambda);
        DepNode beta = tree.get(i_beta);

        if (lambda.headId == beta.id) {
            if (isPop(tree)) {
                return LB_LEFT_POP + LB_DELIM + lambda.deprel;
            } else {
                return LB_LEFT_ARC + LB_DELIM + lambda.deprel;
            }
        } else if (lambda.id == beta.headId) {
            return LB_RIGHT_ARC + LB_DELIM + beta.deprel;
        } else if (isShift(tree)) {
            return LB_SHIFT;
        } else {
            return LB_NO_ARC;
        }
    }

    protected void leftPop(DepNode lambda, DepNode beta, String deprel, double score) {
        String label = LB_LEFT_POP + LB_DELIM + deprel;
        trainInstance(label);

        lambda.setHead(beta.id, deprel, score);
        lambda.isSkip = true;
        if (beta.leftMostDep == null || lambda.id < beta.leftMostDep.id) {
            beta.leftMostDep = lambda;
        }
        i_lambda--;
        prev_trans.add(label);

        if (i_flag == FLAG_PRINT_TRANSITION) {
            printTransition("LEFT-POP", lambda.id + " <-" + deprel + "- " + beta.id);
        }
    }
}