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
package clear.dep;

import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.ftr.FtrLib;
import clear.morph.MorphKr;
import clear.propbank.PBLoc;
import clear.reader.AbstractReader;
import java.util.ArrayList;

/**
 * Dependency node.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/19/2011
 */
public class DepNode {

    /**
     * Index of the current node
     */
    public int id;
    /**
     * Word form
     */
    public String form;
    /**
     * Lemma
     */
    public String lemma;
    /**
     * Part-of-speech tag
     */
    public String pos;
    /**
     * Fine-grained Part-of-speech tag
     */
    public String fpos;
    /**
     * Feats
     */
    public DepFeat feats;
    /**
     * Index of the head node
     */
    public int headId;
    /**
     * Dependency label
     */
    public String deprel;
    /**
     * Score of the headId'th node being the head of the node
     */
    public double score;
    /**
     * True if the node already found its head
     */
    public boolean hasHead;
    /**
     * Leftmost dependent
     */
    public DepNode leftMostDep;
    /**
     * Rightmost dependent
     */
    public DepNode rightMostDep;
    /**
     * Left sibling
     */
    public DepNode leftSibling;
    /**
     * Right sibling
     */
    public DepNode rightSibling;
    /**
     * For Czech: conjunction head
     */
    public DepNode coordHead;
    /**
     * Antecedent of this node
     */
    public DepNode antecedent;
    /**
     * Skip this node if it is true
     */
    public boolean isSkip;
    /**
     * SRL information
     */
    public SRLInfo srlInfo;
    /**
     * For PropToDep only
     */
    public PBLoc[] pbLoc = null;
    /**
     * For Korean: morphems
     */
    public MorphKr morphKr = null;

    /**
     * Initializes the node as a null node.
     */
    public DepNode() {
        init(DepLib.NULL_ID, FtrLib.TAG_NULL, FtrLib.TAG_NULL, FtrLib.TAG_NULL, FtrLib.TAG_NULL, null, DepLib.NULL_HEAD_ID, FtrLib.TAG_NULL, 0, false, null, null, null, null, null, null, false, null, null);
    }

//	==================================== Construct ====================================
    /**
     * Converts the node as a root node.
     */
    public void toRoot() {
        id = DepLib.ROOT_ID;
        form = DepLib.ROOT_TAG;
        lemma = DepLib.ROOT_TAG;
        pos = DepLib.ROOT_TAG;
        deprel = DepLib.ROOT_TAG;
    }

    private void init(int id, String form, String lemma, String pos, String fpos, DepFeat feats, int headId, String deprel, double score, boolean hasHead, DepNode leftMostDep, DepNode rightMostDep, DepNode leftSibling, DepNode rightSibling, DepNode coordHead, DepNode antecedent, boolean isSkip, SRLInfo srlInfo, MorphKr morphKr) {
        this.id = id;
        this.form = form;
        this.lemma = lemma;
        this.pos = pos;
        this.fpos = fpos;
        this.feats = feats;
        this.headId = headId;
        this.deprel = deprel;

        this.score = score;
        this.hasHead = hasHead;
        this.leftMostDep = leftMostDep;
        this.rightMostDep = rightMostDep;
        this.leftSibling = leftSibling;
        this.rightSibling = rightSibling;
        this.coordHead = coordHead;
        this.antecedent = antecedent;
        this.isSkip = isSkip;

        this.srlInfo = (srlInfo != null) ? srlInfo.clone() : null;
        this.morphKr = morphKr;
    }

    public void copy(DepNode node) {
        init(node.id, node.form, node.lemma, node.pos, node.fpos, node.feats, node.headId, node.deprel, node.score, node.hasHead, node.leftMostDep, node.rightMostDep, node.leftSibling, node.rightSibling, node.coordHead, node.antecedent, node.isSkip, node.srlInfo, node.morphKr);
    }

    @Override
    public DepNode clone() {
        DepNode node = new DepNode();

        node.copy(this);
        return node;
    }

    /**
     * @return the string representation of the node. Each field is separated by {@link DepNode#FIELD_DELIM}.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        organizeFeats();

        buff.append(id);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(form);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(lemma);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(pos);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(fpos);
        buff.append(AbstractReader.FIELD_DELIM);
        if (feats == null) {
            buff.append(DepLib.FIELD_BLANK);
        } else {
            buff.append(feats.toString());
        }
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(headId);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(deprel);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(AbstractReader.EMPTY_FIELD);
        buff.append(AbstractReader.FIELD_DELIM);
        buff.append(AbstractReader.EMPTY_FIELD);

        if (srlInfo != null) {
            buff.append(AbstractReader.FIELD_DELIM);
            buff.append(srlInfo.toString());
        }

        /*
         * if (pbLoc != null) { buff.append(AbstractReader.FIELD_DELIM);
         * buff.append(pbLoc[0].terminalId); buff.append(";");
         * buff.append(pbLoc[1].toString());
		}
         */

        return buff.toString();
    }

    private void organizeFeats() {
        if (antecedent != null) {
            if (feats == null) {
                feats = new DepFeat();
            }
            feats.put("at", Integer.toString(antecedent.id));
        }
    }

//	==================================== Boolean ====================================
    /**
     * @return true if the node is a null node.
     */
    public boolean isNull() {
        return id == DepLib.NULL_ID;
    }

    /**
     * @return true if the node is the root.
     */
    public boolean isRoot() {
        return id == DepLib.ROOT_ID;
    }

    public boolean isPredicate() {
        return srlInfo != null && srlInfo.isPredicate();
    }

    /**
     * @return true if the form of the node is
     * <code>form</code>.
     */
    public boolean isForm(String form) {
        return this.form.equals(form);
    }

    /**
     * @return true if the lemma of the node is
     * <code>lemma</code>.
     */
    public boolean isLemma(String lemma) {
        return this.lemma.equals(lemma);
    }

    /**
     * @return true if the part-of-speech tag of the node is
     * <code>pos</code>.
     */
    public boolean isPos(String pos) {
        return this.pos.equals(pos);
    }

    /**
     * @return true if the part-of-speech tag of the node starts with
     * <code>posx</code>.
     */
    public boolean isPosx(String regex) {
        return this.pos.matches(regex);
    }

    /**
     * @return true if the dependency label of the node is
     * <code>deprel</code>.
     */
    public boolean isDeprel(String deprel) {
        return this.deprel.equals(deprel);
    }

    /**
     * @return true if this node is a SRL argument of the headId.
     */
    public boolean isArgOf(int headId) {
        return srlInfo.isArgOf(headId);
    }

    public boolean isLabel(String regex) {
        return srlInfo.labelMatches(regex);
    }

//	==================================== Getter ====================================
    /**
     * @return ({@link DepNode#hasHead}) ? {@link DepNode#headId} : {@link DepLib#NULL_HEAD_ID}.
     */
    public int getHeadId() {
        return hasHead ? headId : DepLib.NULL_HEAD_ID;
    }

    /**
     * @return ({@link DepNode#hasHead}) ? {@link DepNode#deprel} : {@link FtrLib#TAG_NULL}.
     */
    public String getDeprel() {
        return hasHead ? deprel : null;
    }

    public String getLabel(int headId) {
        return srlInfo.getLabel(headId);
    }

    /**
     * @return null if the key doesn't exist.
     */
    public String getFeat(String key) {
        return (feats != null) ? feats.get(key) : null;
    }

//	==================================== Setter ====================================
    /**
     * Sets the
     * <code>headId</code>'th node as the head of the node.
     *
     * @param headId index of the head node
     * @param deprel dependency label between the current and the head nodes
     * @param score score of the headId'th node being the head of the node
     */
    public void setHead(int headId, String deprel, double score) {
        this.headId = headId;
        this.deprel = deprel;
        this.score = score;
        this.hasHead = true;
    }

    public void clearDepHead() {
        headId = DepLib.NULL_HEAD_ID;
        deprel = FtrLib.TAG_NULL;
        score = 0d;
        hasHead = false;
    }

    public void setRolesetId(String rolesetId) {
        srlInfo.setRolesetId(rolesetId);
    }

    public void addSRLHead(int headId, String label) {
        srlInfo.addHead(headId, label);
    }

    public void addSRLHead(int headId, String label, double score) {
        srlInfo.addHead(headId, label, score);
    }

    public void addSRLHeads(ArrayList<SRLHead> heads) {
        srlInfo.addHeads(heads);
    }

    public void clearSRLHeads() {
        srlInfo.heads.clear();
    }

    public void removeSRLHeads(ArrayList<SRLHead> sHeads) {
        ArrayList<SRLHead> delList = new ArrayList<>();

        for (SRLHead tHead : this.srlInfo.heads) {
            for (SRLHead pHead : sHeads) {
                if (tHead.equals(pHead)) {
                    delList.add(tHead);
                    break;
                }
            }
        }

        srlInfo.heads.removeAll(delList);
    }
}