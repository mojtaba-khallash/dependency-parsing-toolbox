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
import clear.dep.DepFeat;
import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.ftr.xml.FtrToken;
import clear.morph.MorphKr;
import clear.reader.DepReader;
import clear.util.IOUtil;
import clear.util.tuple.JObjectObjectTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Abstract dependency parser.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2011
 */
abstract public class AbstractDepParser extends AbstractParser {

    /**
     * Shift-eager algorithm
     */
    static final public String ALG_SHIFT_EAGER = "shift-eager";
    /**
     * Shift-pop algorithm
     */
    static final public String ALG_SHIFT_POP = "shift-pop";
    /**
     * Feature templates
     */
    protected DepFtrXml t_xml;
    /**
     * Feature mappings
     */
    protected DepFtrMap t_map;
    /**
     * ML decoder
     */
    protected AbstractMultiDecoder c_dec;
    /**
     * Prints transitions
     */
    protected PrintStream f_out;
    /**
     * Current dependency tree
     */
    protected DepTree d_tree;
    /**
     * Index of lambda_1
     */
    protected int i_lambda;
    /**
     * Index of beta
     */
    protected int i_beta;
    /**
     * Previous transitions
     */
    protected ArrayList<String> prev_trans;
    public int i_trainIndex = 0;

//	=============================== Constructors ===============================
    /**
     * {@link AbstractDepParser#FLAG_PRINT_TRANSITION} or {@link AbstractDepParser#FLAG_TRAIN_LEXICON}.
     */
    public AbstractDepParser(byte flag, String filename) {
        i_flag = flag;

        if (flag == FLAG_PRINT_TRANSITION) {
            f_out = IOUtil.createPrintFileStream(filename);
        } else if (flag == FLAG_TRAIN_LEXICON) {
            t_xml = new DepFtrXml(filename);
            t_map = new DepFtrMap(t_xml);
        }
    }

    /**
     * {@link AbstractDepParser#FLAG_TRAIN_INSTANCE}.
     */
    public AbstractDepParser(byte flag, DepFtrXml xml, String lexiconFile) {
        i_flag = flag;
        t_xml = xml;
        t_map = new DepFtrMap(lexiconFile);

        initTrainArrays(1);
    }

    /**
     * {@link AbstractDepParser#FLAG_PREDICT} or {@link AbstractDepParser#FLAG_TRAIN_BOOST}.
     */
    public AbstractDepParser(byte flag, DepFtrXml xml, DepFtrMap map, AbstractMultiDecoder decoder) {
        i_flag = flag;
        t_xml = xml;
        t_map = map;
        c_dec = decoder;

        if (flag == FLAG_TRAIN_BOOST) {
            initTrainArrays(1);
        }
    }

//	=============================== External methods ===============================
    public DepFtrXml getDepFtrXml() {
        return t_xml;
    }

    public DepFtrMap getDepFtrMap() {
        return t_map;
    }

    /**
     * Saves tags from {@link AbstractDepParser#t_map} to
     * <code>lexiconFile</code>.
     */
    public void saveTags(String lexiconFile) {
        t_map.save(t_xml, lexiconFile);
    }

    public void closeOutputStream() {
        f_out.close();
    }

//	=============================== Pre-processing ===============================
    protected void preProcess(DepTree tree) {
        switch (s_language) {
            case DepReader.LANG_EN:
                preProcessEn(tree);
                break;
            case DepReader.LANG_CZ:
                preProcessCz(tree);
                break;
            case DepReader.LANG_KR:
                preProcessKr(tree);
                break;
        }
    }

    protected void preProcessEn(DepTree tree) {
        int i, j, size = tree.size();
        DepNode head;

        for (i = 1; i < size; i++) {
            head = tree.get(i);

            if (head.isPosx("IN")) {
                for (j = i + 1; j < size; j++) {
                    if (tree.get(j).isPosx("NN.*|CD") && !(j + 1 < size && tree.get(j + 1).isPosx("NN.*|CD|POS"))) {
                        head.rightMostDep = tree.get(j);
                        i = j;
                        break;
                    }
                }
            }
        }
    }

    protected void preProcessCz(DepTree tree) {
        preProcessCzMorph(tree);
        preProcessCzCoord(tree);
    }

    protected void preProcessCzMorph(DepTree tree) {
        DepNode node;
        String feat;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);

            if ((feat = node.getFeat(DepLib.CZ_FEAT[2])) != null) // degree of Comparison
            {
                node.pos += feat;
            }
            if ((feat = node.getFeat(DepLib.CZ_FEAT[8])) != null) // name
            {
                node.lemma = "$SEM=" + feat + "$";
            } else if ((feat = node.getFeat(DepLib.CZ_FEAT[9])) != null) // number
            {
                if (feat.equals("n")) {
                    node.lemma = "$CRD$";
                }
            }
        }
    }

    protected void preProcessCzCoord(DepTree tree) {
        int coordId, nextId, prevId, size = tree.size(), gap = 10, count, total;
        DepNode coord, prev, next;
        DepFeat prevFeats, nextFeats;
        String nextPos, tmp;
        double score, bestScore;
        JObjectObjectTuple<DepNode, DepNode> bestPair = new JObjectObjectTuple<>(null, null);
        final String SubPOS = DepLib.CZ_FEAT[9];

        for (coordId = 1; coordId < size; coordId++) {
            coord = tree.get(coordId);
            if (!coord.getFeat(SubPOS).equals("^") && !coord.lemma.matches(",|:|&|\\+")) {
                continue;
            }
            bestScore = 0;

            for (nextId = coordId + 1; nextId <= coordId + gap && nextId < size; nextId++) {
                next = tree.get(nextId);
                nextFeats = next.feats;
                nextPos = nextFeats.get(SubPOS);
                total = nextFeats.size();

                for (prevId = coordId - 1; prevId >= coordId - gap && prevId > 0; prevId--) {
                    prev = tree.get(prevId);
                    prevFeats = prev.feats;
                    if (!nextPos.equals(prevFeats.get(SubPOS))) {
                        continue;
                    }

                    count = 0;

                    for (String nextKey : nextFeats.keySet()) {
                        if (nextKey.equals(SubPOS)) {
                            continue;
                        }
                        tmp = prevFeats.get(nextKey);

                        if (tmp != null && tmp.equals(nextFeats.get(nextKey))) {
                            count++;
                        }
                    }

                    score = (double) count / total;

                    if (score > bestScore) {
                        bestScore = score;
                        bestPair.set(prev, next);
                    }

                    if (score >= 0.8) {
                        break;
                    }
                }
            }

            if (bestScore > 0) {
                coord.leftMostDep = bestPair.o1;
                coord.rightMostDep = bestPair.o2;
                bestPair.o1.coordHead = coord;
                bestPair.o2.coordHead = coord;
            }
        }
    }

    protected void preProcessKr(DepTree tree) {
        DepNode node;
        int i, size = tree.size();

        MorphKr root = new MorphKr();
        node = tree.get(DepLib.ROOT_ID);
        node.morphKr = root;

        for (i = 1; i < size; i++) {
            node = tree.get(i);
            node.morphKr = new MorphKr(node.lemma);
        }
    }

//	=============================== Instance ===============================
    protected void trainInstance(String label) {
        if (i_flag == FLAG_TRAIN_LEXICON) {
            addLexica();
            t_map.addLabel(label);
        } else if (i_flag == FLAG_TRAIN_INSTANCE) {
            saveInstance(label, getFeatureArray());
        }
    }

    protected void saveInstance(String label, IntArrayList ftr) {
        //	if (ftr.isEmpty())	System.err.println(d_tree.get(i_lambda).lemma+" "+d_tree.get(i_beta).lemma);
        saveInstance(label, ftr, t_map, i_trainIndex);
    }

    /**
     * Prints the current transition.
     *
     * @param trans transition
     * @param arc lambda_1[0] <- deprel -> beta[0]
     */
    protected void printTransition(String trans, String arc) {
        StringBuilder build = new StringBuilder();

        // operation
        build.append(trans);
        build.append("\t");

        // lambda_1
        build.append("[");
        if (i_lambda >= 0) {
            build.append(0);
        }
        if (i_lambda >= 1) {
            build.append(":").append(i_lambda);
        }
        build.append("]\t");

        // lambda_2
        build.append("[");
        if (getLambda2Count() > 0) {
            build.append(i_lambda + 1);
        }
        if (getLambda2Count() > 1) {
            build.append(":").append((i_beta - 1));
        }
        build.append("]\t");

        // beta
        build.append("[");
        if (i_beta < d_tree.size()) {
            build.append(i_beta);
        }
        if (i_beta <= d_tree.size()) {
            build.append(":").append((d_tree.size() - 1));
        }
        build.append("]\t");

        // transition
        build.append(arc);
        f_out.println(build.toString());
    }

    /**
     * @return number of nodes in lambda_2 (list #2)
     */
    protected int getLambda2Count() {
        return i_beta - (i_lambda + 1);
    }

//	=============================== Lexica ===============================
    protected void addLexica() {
        addNgramLexica(t_xml, t_map);
        addLanguageSpecificLexica();
    }

    protected void addLanguageSpecificLexica() {
        switch (s_language) {
            case DepReader.LANG_EN:
                addEnPunctuationLexica();
                break;
            case DepReader.LANG_CZ:
                addCzPunctuationLexica();
                break;
        }
    }

    protected void addEnPunctuationLexica() {
        DepNode b0 = d_tree.get(i_beta);

        if (b0.isDeprel(DepLib.DEPREL_P)) {
            t_map.addExtra(0, b0.form);
        }
    }

    protected void addCzPunctuationLexica() {
        DepNode b0 = d_tree.get(i_beta);

        if (b0.isPos("Z")) {
            t_map.addExtra(0, b0.form);
        }
    }

//	=============================== Feature ===============================
    protected IntArrayList getFeatureArray() {
        // add features
        IntArrayList arr = new IntArrayList();
        int idx[] = {1};

        addNgramFeatures(arr, idx, t_xml, t_map);
        addLanguageSpecificFeatures(arr, idx);

        return arr;
    }

    protected void addLanguageSpecificFeatures(IntArrayList arr, int[] beginIndex) {
        switch (s_language) {
            case DepReader.LANG_EN:
                addEnPunctuationFeatures(arr, beginIndex);
                break;
            case DepReader.LANG_CZ:
                addCzPunctuationFeatures(arr, beginIndex);
                addCzCoordFeatures(arr, beginIndex);
                //	addCzCaseFeatures       (arr, beginIndex);
                break;
            case DepReader.LANG_KR:
                addKrCaseFeatures(arr, beginIndex);
                break;
        }
    }

    /**
     * Adds punctuation features. This method is called from {@link ShiftPopParser#getFeatureArray()}.
     */
    protected void addEnPunctuationFeatures(IntArrayList arr, int[] beginIndex) {
        int index, n = t_map.n_extra[0];

        index = d_tree.getRightNearestPunctuation(i_lambda, i_beta - 1, t_map);
        if (index != -1) {
            arr.add(beginIndex[0] + index);
        }
        beginIndex[0] += n;

        index = d_tree.getRightNearestPunctuation(i_beta, d_tree.size() - 1, t_map);
        if (index != -1) {
            arr.add(beginIndex[0] + index);
        }
        beginIndex[0] += n;

        index = d_tree.getLeftNearestPunctuation(i_beta, i_lambda + 1, t_map);
        if (index != -1) {
            arr.add(beginIndex[0] + index);
        }
        beginIndex[0] += n;

        /*
         * index = d_tree.getLeftNearestPunctuation(i_lambda, 1, t_map); if
         * (index != -1)	arr.add(beginIndex[0] + index); beginIndex[0] += n;
         */
    }

    protected void addCzPunctuationFeatures(IntArrayList arr, int[] beginIndex) {
        int index, n = t_map.n_extra[0];

        index = d_tree.getRightNearestPunctuation(i_lambda, i_beta - 1, t_map);
        if (index != -1) {
            arr.add(beginIndex[0] + index);
        }
        beginIndex[0] += n;

        /*
         * index = d_tree.getRightNearestPunctuation(i_beta, d_tree.size()-1,
         * t_map); if (index != -1)	arr.add(beginIndex[0] + index);
         * beginIndex[0] += n;
         *
         * index = d_tree.getLeftNearestPunctuation(i_beta, i_lambda+1, t_map);
         * if (index != -1)	arr.add(beginIndex[0] + index); beginIndex[0] += n;
         *
         * index = d_tree.getLeftNearestPunctuation(i_lambda, 1, t_map); if
         * (index != -1)	arr.add(beginIndex[0] + index); beginIndex[0] += n;
         */
    }

    protected void addCzCoordFeatures(IntArrayList arr, int[] beginIndex) {
        DepNode lambda = d_tree.get(i_lambda);
        DepNode beta = d_tree.get(i_beta);

        if (lambda.coordHead != null) {
            if (lambda.coordHead.id == i_beta) {
                arr.add(beginIndex[0]);
            } else if (lambda.coordHead.id > 0) {
                arr.add(beginIndex[0] + 1);
            }
        }

        if (beta.coordHead != null) {
            if (beta.coordHead.id == i_lambda) {
                arr.add(beginIndex[0] + 2);
            }
        }

        beginIndex[0] += 3;
    }

    protected void addCzCaseFeatures(IntArrayList arr, int[] beginIndex) {
        if (d_tree.get(i_lambda).isPos("V")) {
            if (!d_tree.existsLeftDependent(i_lambda, "Sb")) {
                arr.add(beginIndex[0]);
            }
            if (!d_tree.existsLeftDependent(i_lambda, "Obj")) {
                arr.add(beginIndex[0] + 1);
            }
            if (!d_tree.existsRightDependent(i_lambda, "Sb")) {
                arr.add(beginIndex[0] + 2);
            }
            if (!d_tree.existsRightDependent(i_lambda, "Obj")) {
                arr.add(beginIndex[0] + 3);
            }
        }

        if (d_tree.get(i_beta).isPos("V")) {
            if (!d_tree.existsLeftDependent(i_beta, "Sb")) {
                arr.add(beginIndex[0] + 4);
            }
            if (!d_tree.existsLeftDependent(i_beta, "Obj")) {
                arr.add(beginIndex[0] + 5);
            }
        }

        beginIndex[0] += 6;
    }

    private void addKrCaseFeatures(IntArrayList arr, int[] beginIndex) {
        DepNode lambda = d_tree.get(i_lambda);
        DepNode beta = d_tree.get(i_beta);
        MorphKr lMorph = lambda.morphKr;
        MorphKr bMorph = beta.morphKr;

        if (lMorph.isX) {
            arr.add(beginIndex[0]);
        }
        if (bMorph.isX) {
            arr.add(beginIndex[0] + 1);
        }

        beginIndex[0] += 2;

        /*
         * if (!lambda.isRoot() && i_beta - i_lambda == 1) { if
         * (lMorph.getLastMorphem().pos.matches("NN.*") &&
         * bMorph.getFirstMorphem().pos.matches("NN.*")) arr.add(beginIndex[0]);
         * }
         *
         * beginIndex[0] += 1;
         */
    }

    /**
     * @return field retrieved from
     * <code>token</code>
     */
    @Override
    protected String getField(FtrToken token) {
        int index = (token.source == DepFtrXml.LAMBDA) ? i_lambda : i_beta;
        index += token.offset;

        if (!d_tree.isRange(index) || (token.source == DepFtrXml.LAMBDA && index == i_beta) || (token.source == DepFtrXml.BETA && index == i_lambda)) {
            return null;
        }

        DepNode node = null;

        if (token.relation == null) {
            node = d_tree.get(index);
        } else if (token.isRelation(DepFtrXml.R_HD)) {
            node = d_tree.getHead(index);
        } else if (token.isRelation(DepFtrXml.R_LM)) {
            node = d_tree.getLeftMostDependent(index);
        } else if (token.isRelation(DepFtrXml.R_RM)) {
            node = d_tree.getRightMostDependent(index);
        }

        if (node == null) {
            return null;
        }
        Matcher m;

        if (token.isField(DepFtrXml.F_FORM)) {
            return node.form;
        } else if (token.isField(DepFtrXml.F_LEMMA)) {
            return node.lemma;
        } else if (token.isField(DepFtrXml.F_POS)) {
            return node.pos;
        } else if (token.isField(DepFtrXml.F_DEPREL)) {
            return node.getDeprel();
        } else if ((m = DepFtrXml.P_FEAT.matcher(token.field)).find()) {
            return node.getFeat(m.group(1));
        } else if ((m = DepFtrXml.P_TRANS.matcher(token.field)).find()) {
            int idx = prev_trans.size() - Integer.parseInt(m.group(1)) - 1;
            return (idx >= 0) ? prev_trans.get(idx) : null;
        } else if ((m = DepFtrXml.P_KR.matcher(token.field)).find()) {
            String type = m.group(1);
            int loc = Integer.parseInt(m.group(2));

            return node.morphKr.getMorphem(loc, type);
        }

        //	System.err.println("Error: unspecified feature '"+token.field+"'");
        return null;
    }

    /**
     * Parses
     * <code>tree</code>.
     */
    abstract public void parse(DepTree tree);
}