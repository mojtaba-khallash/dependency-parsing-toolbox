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
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLArg;
import clear.ftr.map.SRLFtrMap;
import clear.ftr.xml.FtrToken;
import clear.ftr.xml.SRLFtrXml;
import clear.util.tuple.JObjectObjectTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Shift-eager dependency parser.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/6/2010
 */
abstract public class AbstractSRLParser extends AbstractParser {

    /**
     * Parse from predicate to the left
     */
    static public final byte DIR_LEFT = -1;
    /**
     * Parse from predicate to the right
     */
    static public final byte DIR_RIGHT = +1;
    /**
     * Feature templates
     */
    protected SRLFtrXml t_xml;
    /**
     * Feature mappings
     */
    protected SRLFtrMap[] t_map;
    /**
     * ML decoder
     */
    protected OneVsAllDecoder[] c_dec;
    /**
     * Current dependency tree
     */
    protected DepTree d_tree;
    /**
     * Index of argument
     */
    protected int i_lambda;
    /**
     * Index of predicate
     */
    protected int i_beta;
    /**
     * {@link AbstractSRLParser#DIR_LEFT} or {@link AbstractSRLParser#DIR_RIGHT}
     */
    protected byte i_dir;
    /**
     * List of all arguments sequence
     */
    protected ArrayList<SRLArg> ls_args;
    /**
     * List of core arguments sequence
     */
    protected ArrayList<String> ls_argn;

//	=============================== Constructors ===============================
    /**
     * {@link AbstractSRLParser#FLAG_TRAIN_LEXICON}.
     */
    public AbstractSRLParser(byte flag, String xmlFile) {
        i_flag = flag;
        t_xml = new SRLFtrXml(xmlFile);
        t_map = new SRLFtrMap[2];

        for (int i = 0; i < t_map.length; i++) {
            t_map[i] = new SRLFtrMap(t_xml);
        }
    }

    /**
     * {@link AbstractSRLParser#FLAG_TRAIN_INSTANCE}.
     */
    public AbstractSRLParser(byte flag, SRLFtrXml xml, String[] lexiconFile) {
        i_flag = flag;
        t_xml = xml;
        t_map = new SRLFtrMap[lexiconFile.length];

        for (int i = 0; i < t_map.length; i++) {
            t_map[i] = new SRLFtrMap(lexiconFile[i]);
        }

        initTrainArrays(t_map.length);
    }

    /**
     * {@link AbstractSRLParser#FLAG_PREDICT} or {@link AbstractSRLParser#FLAG_TRAIN_BOOST}.
     */
    public AbstractSRLParser(byte flag, SRLFtrXml xml, SRLFtrMap[] map, AbstractDecoder[] decoder) {
        i_flag = flag;
        t_xml = xml;
        t_map = map;
        c_dec = new OneVsAllDecoder[decoder.length];

        for (int i = 0; i < decoder.length; i++) {
            c_dec[i] = (OneVsAllDecoder) decoder[i];
        }

        if (flag == FLAG_TRAIN_BOOST) {
            initTrainArrays(decoder.length);
        }
    }

//	=============================== External methods ===============================
    public SRLFtrXml getSRLFtrXml() {
        return t_xml;
    }

    public SRLFtrMap[] getSRLFtrMap() {
        return t_map;
    }

    protected SRLFtrMap getFtrMap() {
        return (i_dir == DIR_LEFT) ? t_map[0] : t_map[1];
    }

    protected OneVsAllDecoder getDecoder() {
        return (i_dir == DIR_LEFT) ? c_dec[0] : c_dec[1];
    }

    protected JObjectObjectTuple<IntArrayList, ArrayList<int[]>> getTrainArray() {
        return (i_dir == DIR_LEFT) ? a_trans.get(0) : a_trans.get(1);
    }

    /**
     * Adds a label and lexica to {@link AbstractSRLParser#t_map}.
     */
    protected void addTags(String label) {
        SRLFtrMap map = getFtrMap();

        addLexica(map);
        map.addLabel(label);
    }

    /**
     * Saves tags from {@link AbstractSRLParser#t_map} to
     * <code>lexiconFile</code>.
     */
    public void saveTags(String[] lexiconFile) {
        for (int i = 0; i < t_map.length; i++) {
            t_map[i].save(t_xml, lexiconFile[i]);
        }
    }

    /**
     * Saves a training instance for argument classification.
     */
    protected void saveInstance(String label, IntArrayList arr) {
        int index = getFtrMap().labelToIndex(label);
        if (index < 0) {
            return;
        }

        JObjectObjectTuple<IntArrayList, ArrayList<int[]>> yx;
        yx = getTrainArray();

        yx.o1.add(index);
        yx.o2.add(arr.toArray());
    }

//	=============================== Lexica ===============================
    /**
     * Add n-gram lexica to the feature map.
     */
    protected void addNgramLexica(SRLFtrMap map) {
        addNgramLexica(t_xml, map);
    }

    /**
     * Adds n-gram features.
     */
    protected void addNgramFeatures(IntArrayList arr, int[] idx, SRLFtrMap tmap) {
        addNgramFeatures(arr, idx, t_xml, tmap);
    }

    /**
     * @return field retrieved from
     * <code>token</code>
     */
    @Override
    protected String getField(FtrToken token) {
        int index = (token.source == SRLFtrXml.LAMBDA) ? i_lambda : i_beta;
        index += token.offset;

        if (!d_tree.isRange(index) || (token.source == SRLFtrXml.LAMBDA && index == i_beta) || (token.source == SRLFtrXml.BETA && index == i_lambda)) {
            return null;
        }

        DepNode node = null;

        if (token.relation == null) {
            node = d_tree.get(index);
        } else if (token.isRelation(SRLFtrXml.R_HD)) {
            node = d_tree.getHead(index);
        } else if (token.isRelation(SRLFtrXml.R_LM)) {
            node = d_tree.getLeftMostDependent(index);
        } else if (token.isRelation(SRLFtrXml.R_RM)) {
            node = d_tree.getRightMostDependent(index);
        } else if (token.isRelation(SRLFtrXml.R_LS)) {
            node = d_tree.getLeftSibling(index);
        } else if (token.isRelation(SRLFtrXml.R_RS)) {
            node = d_tree.getRightSibling(index);
        } else if (token.isRelation(SRLFtrXml.R_VC)) {
            node = d_tree.getHighestVC(index);
        }

        if (node == null) {
            return null;
        }
        Matcher m;

        if (token.isField(SRLFtrXml.F_FORM)) {
            return node.form;
        } else if (token.isField(SRLFtrXml.F_LEMMA)) {
            return node.lemma;
        } else if (token.isField(SRLFtrXml.F_POS)) {
            return node.pos;
        } else if (token.isField(SRLFtrXml.F_DEPREL)) {
            return node.getDeprel();
        } else if ((m = SRLFtrXml.P_FEAT.matcher(token.field)).find()) {
            return node.getFeat(m.group(1));
        } else if ((m = SRLFtrXml.P_SUBCAT.matcher(token.field)).find()) {
            byte idx = Byte.parseByte(m.group(2));
            return d_tree.getSubcat(m.group(1), node.id, idx);
        } else if ((m = SRLFtrXml.P_PATH.matcher(token.field)).find()) {
            byte idx = Byte.parseByte(m.group(2));
            if (node.id > d_tree.size()) {
                System.out.println(node.toString());
                System.out.println(d_tree.toString());
            }
            return d_tree.getPath(m.group(1), node.id, i_beta, idx);
        } else if ((m = SRLFtrXml.P_ARGN.matcher(token.field)).find()) {
            int idx = ls_argn.size() - Integer.parseInt(m.group(1)) - 1;
            return (idx < 0) ? null : ls_argn.get(idx);
        }

        //	System.err.println("Error: unspecified feature '"+token.field+"'");
        return null;
    }

    abstract public void parse(DepTree tree);

    abstract protected void addLexica(SRLFtrMap map);
}