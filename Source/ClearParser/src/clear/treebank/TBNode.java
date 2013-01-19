/**
 * Copyright (c) 2007, Regents of the University of Colorado All rights
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
package clear.treebank;

import clear.dep.srl.SRLHead;
import clear.propbank.PBLoc;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Treebank node.
 *
 * @author Jinho D. Choi <b>Last update:</b> 9/1/2010
 */
public class TBNode {

    /**
     * Word-form of the node
     */
    public String form;
    /**
     * Part-of-speech tag of the node
     */
    public String pos;
    /**
     * Function tags of the node
     */
    public HashSet<String> tags;
    /**
     * Co-index (e.g., NP-1)
     */
    public int coIndex;
    /**
     * Gapping index (e.g., NP=1)
     */
    public int gapIndex;
    /**
     * Terminal index (counting ECs), starting from 0
     */
    public int terminalId;
    /**
     * Token index (not counting ECs), starting from 0
     */
    public int tokenId;
    /**
     * Head index, derived by headrules
     */
    public int headId;
    /**
     * Child index among its siblings
     */
    public int childId;
    /**
     * If empty category, store its antecedent.
     */
    public TBNode antecedent;
    /**
     * PropBank location of this node
     */
    public PBLoc pbLoc;
    /**
     * Roleset ID if exist
     */
    public String rolesetId;
    /**
     * Parent node
     */
    protected TBNode nd_parent;
    /**
     * List of children nodes
     */
    protected ArrayList<TBNode> ls_children;
    /**
     * PropBank heads
     */
    protected ArrayList<SRLHead> pb_heads;

    /**
     * PropBank arguments (if this node is a predicate)
     */
//	protected ArrayList<SRLArg>	pb_args;
    /**
     * Initializes the node with its parent node and pos-tag.
     */
    public TBNode(TBNode parent, String postag) {
        form = null;
        tags = null;
        coIndex = -1;
        gapIndex = -1;
        terminalId = -1;
        tokenId = -1;
        headId = -1;
        childId = -1;
        antecedent = null;
        pbLoc = null;
        rolesetId = null;
        nd_parent = parent;
        ls_children = null;
        pb_heads = null;
        //	pb_args     = null;
        init(postag);
    }

    /**
     * Initializes pos-tags, function-tags, co-index, and gap-index.
     */
    private void init(String postag) {
        if (postag.matches("-([A-Z])+-")) {
            pos = postag;
            return;
        }

        StringTokenizer tok = new StringTokenizer(postag, "-=~", true);
        pos = tok.nextToken();
        OUTER:
        while (tok.hasMoreTokens()) {
            String op = tok.nextToken();
            switch (op) {
                case "=":
                    if (tok.hasMoreTokens()) {
                        gapIndex = Integer.parseInt(tok.nextToken());
                    } else {
                        break OUTER;
                    }
                    break;
                case "-":
                    if (tok.hasMoreTokens()) {
                        String tag = tok.nextToken();

                        if (tag.matches("\\d*")) {
                            coIndex = Integer.parseInt(tag);
                        } else {
                            if (tags == null) {
                                tags = new HashSet<>();
                            }
                            tags.add(tag);
                        }
                    } else {
                        break OUTER;
                    }
                    break;
            }
        }
    }

    /**
     * Returns true if the word-form of this node is
     * <code>form</code>. If the node is a phrase, returns false.
     */
    public boolean isForm(String regex) {
        return this.form != null && this.form.matches(regex);
    }

    /**
     * Returns true if the rule applies to this node. If
     * <code>rule</code> starts with '-', it compares the function tag;
     * otherwise, compares the pos-tag.
     */
    public boolean isRule(String rule) {
        if (rule.charAt(0) == '-') {
            return isTag(rule.substring(1));
        } else {
            return isPos(rule);
        }
    }

    /**
     * @return true if the pos-tag of this node is
     * <code>pos</code> in regular expression (e.g., NN.*|VB).
     */
    public boolean isPos(String regex) {
        return pos.matches(regex);
    }

    /**
     * @return true is the function tag of this node is
     * <code>tag</code>.
     */
    public boolean isTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    /**
     * @return true if the node is an empty category.
     */
    public boolean isEmptyCategory() {
        return pos.equals(TBLib.POS_NONE);
    }

    /**
     * @return true if the node contains only empty category, recursively.
     */
    public boolean isEmptyCategoryRec() {
        return isEmptyCategoryRecAux(this);
    }

    /**
     * This method is called from {@link TBNode#isEmptyCategoryRec()}.
     */
    private boolean isEmptyCategoryRecAux(TBNode curr) {
        if (!curr.isPhrase()) {
            return curr.isEmptyCategory();
        }

        for (TBNode child : curr.getChildren()) {
            if (!isEmptyCategoryRecAux(child)) {
                return false;
            }
        }

        return true;
    }

    public boolean isToken() {
        return tokenId != -1;
    }

    /**
     * @return true if the node is a phrase.
     */
    public boolean isPhrase() {
        return ls_children != null;
    }

    /**
     * @return true if this node is a phrase containing
     * <code>pos</code> as a pos-tag of its children.
     */
    public boolean containsPos(String pos) {
        if (!isPhrase()) {
            return false;
        }

        for (TBNode child : ls_children) {
            if (child.isPos(pos)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if this node is a phrase containing
     * <code>tag</code> as a function-tag of its children.
     */
    public boolean containsTag(String tag) {
        if (!isPhrase()) {
            return false;
        }

        for (TBNode child : ls_children) {
            if (child.isTag(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param regex word-form
     * @return true if this node contains the word-form.
     */
    public boolean containsForm(String regex) {
        return containsFormAux(this, regex);
    }

    private boolean containsFormAux(TBNode node, String regex) {
        if (!node.isPhrase()) {
            return node.isForm(regex);
        }

        for (TBNode child : node.getChildren()) {
            if (containsFormAux(child, regex)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if the node contains a gap-node.
     */
    public boolean containsGap() {
        if (!isPhrase()) {
            return false;
        }

        for (TBNode child : ls_children) {
            if (child.gapIndex != -1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a child with a gap-index of
     * <code>index</code>. If there is no such child, returns null.
     */
    public TBNode getGapNode(int index) {
        return getGapNodeAux(index, this);
    }

    /**
     * This method is called from {@link TBNode#getGapNode(int)}.
     */
    private TBNode getGapNodeAux(int index, TBNode curr) {
        if (!curr.isPhrase()) {
            return null;
        }

        for (TBNode child : curr.getChildren()) {
            if (child.coIndex == index || child.gapIndex == index) {
                return curr;
            }

            TBNode node = getGapNodeAux(index, child);
            if (node != null) {
                return node;
            }
        }

        return null;
    }

    /**
     * @return the number of children whose pos-tag is
     * <code>pos</code>.
     */
    public int countsPos(String pos) {
        if (!isPhrase()) {
            return 0;
        }
        int count = 0;

        for (TBNode child : ls_children) {
            if (child.isPos(pos)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the parent node. If there is none, returns null.
     */
    public TBNode getParent() {
        return nd_parent;
    }

    /**
     * Returns the list of children nodes. If there is no child, returns null.
     */
    public ArrayList<TBNode> getChildren() {
        return ls_children;
    }

    /**
     * Assigns
     * <code>form</code> to {@link TBNode#form}. If
     * <code>form</code> is a bracket-tag, convert it to its actual word-form
     * (e.g., "-LRB-" to "(").
     */
    public void setForm(String form) {
        form = form.replaceAll("-LRB-", "(");
        form = form.replaceAll("-LSB-", "[");
        form = form.replaceAll("-LCB-", "{");
        form = form.replaceAll("-RRB-", ")");
        form = form.replaceAll("-RSB-", "]");
        form = form.replaceAll("-RCB-", "}");

        this.form = form;
    }

    /**
     * Sets the parent node to
     * <code>parent</code>.
     */
    public void setParent(TBNode parent) {
        nd_parent = parent;
    }

    /**
     * Adds a child node.
     */
    public void addChild(TBNode child) {
        if (ls_children == null) {
            ls_children = new ArrayList<>();
        }

        child.childId = ls_children.size();
        ls_children.add(child);
    }

    /**
     * Returns word-forms of the node's subtree.
     */
    public String toWords() {
        return toWordsAux(this);
    }

    /**
     * This method is called from {@link TBNode#toWords()}.
     */
    private String toWordsAux(TBNode curr) {
        if (curr.isPhrase()) {
            StringBuilder build = new StringBuilder();

            for (TBNode child : curr.getChildren()) {
                build.append(toWordsAux(child));
                build.append(" ");
            }

            return build.toString().trim();
        } else {
            return curr.form;
        }
    }

    /**
     * @return pos-tags of the node's children.
     */
    public String toPosTags() {
        StringBuilder build = new StringBuilder();

        for (TBNode child : ls_children) {
            build.append(child.pos).append("-").append(child.tags);
            build.append(" ");
        }

        return build.toString();
    }

    /**
     * @return pos-tags of the node's children.
     */
    public String toPosWords() {
        StringBuilder build = new StringBuilder();

        for (TBNode child : ls_children) {
            build.append("(");
            build.append(child.pos);
            build.append(" ");
            build.append(child.toWords());
            build.append(")");
            build.append(" ");
        }

        return build.toString();
    }

    public ArrayList<TBNode> getSubTerminalNodes() {
        ArrayList<TBNode> list = new ArrayList<>();
        getSubTerminalNodesAux(this, list);

        return list;
    }

    private void getSubTerminalNodesAux(TBNode node, ArrayList<TBNode> list) {
        if (node.isPhrase()) {
            for (TBNode child : node.getChildren()) {
                getSubTerminalNodesAux(child, list);
            }
        } else {
            list.add(node);
        }
    }

    public IntOpenHashSet getSubTermainlIDs() {
        IntOpenHashSet set = new IntOpenHashSet();
        getSubTerminalIDsAux(this, set);

        return set;
    }

    private void getSubTerminalIDsAux(TBNode node, IntOpenHashSet set) {
        if (node.isPhrase()) {
            for (TBNode child : node.getChildren()) {
                getSubTerminalIDsAux(child, set);
            }
        } else {
            set.add(node.terminalId);
        }
    }

    /**
     * @return the bitset of terminal indices of the subtree of this node.
     */
    public BitSet getSubTerminalBitSet() {
        BitSet set = new BitSet();
        getSubTerminalBitSetAux(this, set);

        return set;
    }

    /**
     * This method is called from {@link TBTree#getSubTerminalBitSet()}.
     */
    private void getSubTerminalBitSetAux(TBNode node, BitSet set) {
        if (node.getChildren() == null) {
            set.set(node.terminalId);
        } else {
            for (TBNode child : node.getChildren()) {
                getSubTerminalBitSetAux(child, set);
            }
        }
    }

    /**
     * Returns the bitset of token indices of the subtree of the current node.
     * Each index gets added by
     * <code>offset</code> (e.g., if
     * <code>offset</code> is 1, [0,1,2] becomes [1,2,3]).
     */
    public BitSet getSubTokenBitSet(int offset) {
        BitSet set = new BitSet();
        getSubTokenBitSetAux(this, set, offset);

        return set;
    }

    /**
     * This method is called from {@link TBNode#getSubTokenBitSet(int)}.
     */
    private void getSubTokenBitSetAux(TBNode node, BitSet set, int offset) {
        if (node.getChildren() == null) {
            int tokenIndex = node.tokenId + offset;
            if (tokenIndex >= offset) {
                set.set(tokenIndex);
            }
        } else {
            for (TBNode child : node.getChildren()) {
                getSubTokenBitSetAux(child, set, offset);
            }
        }
    }

    /**
     * If this node is an empty category, return the coIndex of its antecedent.
     */
    public int getEmptyCategoryCoIndex() {
        if (isEmptyCategory()) {
            int idx = form.lastIndexOf('-');
            if (idx >= 0) {
                return Integer.parseInt(form.substring(idx + 1));
            }
        }

        return -1;
    }

    public String getTags() {
        StringBuilder build = new StringBuilder();

        build.append(pos);

        if (coIndex != -1) {
            build.append("-");
            build.append(coIndex);
        }

        if (gapIndex != -1) {
            build.append("=");
            build.append(gapIndex);
        }

        if (tags != null) {
            for (String tag : tags) {
                build.append("-");
                build.append(tag);
            }
        }

        return build.toString();
    }

    public boolean addPBHead(SRLHead sHead) {
        if (pb_heads == null) {
            pb_heads = new ArrayList<>();
        }

        for (SRLHead head : pb_heads) {
            if (head.equals(sHead)) {
                return false;
            }
        }

        return pb_heads.add(sHead);
    }

    public ArrayList<SRLHead> getPBHeads() {
        return pb_heads;
    }

    /*
     * public boolean addPBArg(SRLArg sArg) { if (pb_args == null) pb_args = new
     * ArrayList<SRLArg>();
     *
     * for (SRLArg arg : pb_args) { if (arg.isLabel(sArg.label)) return false; }
     *
     * return pb_args.add(sArg);
	}
     */
    public String getSentenceGroup() {
        return getSentenceGroupAux(this);
    }

    private String getSentenceGroupAux(TBNode node) {
        if (node.isPos("S.*")) {
            return node.pos;
        }
        if (node.getParent() == null) {
            return null;
        }

        return getSentenceGroupAux(node.getParent());
    }

    public boolean isPrior(String pos) {
        if (nd_parent == null) {
            return false;
        }

        TBNode parent = getParent();
        ArrayList<TBNode> siblings = parent.getChildren();

        for (int i = childId - 1; i >= 0; i--) {
            if (siblings.get(i).isPos(pos)) {
                return true;
            }
        }

        return false;
    }

    public boolean isSucceededBy(String pos) {
        if (nd_parent == null) {
            return false;
        }

        TBNode parent = getParent();
        ArrayList<TBNode> siblings = parent.getChildren();

        if (childId - 1 >= 0 && siblings.get(childId - 1).isPos(pos)) {
            return true;
        }

        return false;
    }

    public boolean isFollowedBy(String pos) {
        if (nd_parent == null) {
            return false;
        }

        TBNode parent = getParent();
        ArrayList<TBNode> siblings = parent.getChildren();

        for (int i = childId + 1; i < siblings.size(); i++) {
            if (siblings.get(i).isPos(pos)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return complementizer terminal node. If there is no such node, return
     * null.
     */
    public TBNode getComplementizer() {

        for (TBNode node : getSubTerminalNodes()) {
            if (node.isPos("W.*|-NONE-") || TBEnLib.isComplementizer(node.form)) {
                return node;
            }
        }

        return isPos("WH.*") ? this : null;
    }

    public TBNode getIncludedEmptyCategory(String regex) {
        for (TBNode node : getSubTerminalNodes()) {
            if (node.isForm(regex)) {
                return node;
            }
        }

        return null;
    }

    public boolean hasAntecedent() {
        return antecedent != null;
    }

    public TBNode getIncludedEmptyCategory() {
        for (TBNode node : getSubTerminalNodes()) {
            if (node.isEmptyCategory()) {
                return node;
            }
        }

        return null;
    }

    public int getMaxHeight() {
        TBNode curr = nd_parent;
        int height = 0;

        while (curr != null) {
            height++;
            curr = curr.getParent();
        }

        return height;
    }
}