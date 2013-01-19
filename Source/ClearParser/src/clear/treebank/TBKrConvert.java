/**
 * Copyright (c) 2010, Regents of the University of Colorado All rights
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

import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepTree;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import java.util.ArrayList;

/**
 * This class provides APIs to convert phrase structure trees to dependency
 * trees in English.
 *
 * @author Jinho D. Choi <b>Last update:</b> 9/1/2010
 */
public class TBKrConvert extends AbstractTBConvert {

    public TBKrConvert(TBHeadRules headrules) {
        g_headrules = headrules;
    }

    /**
     * @return a dependency tree converted from
     * <code>pTree</cdoe>.
     */
    @Override
    public DepTree toDepTree(TBTree pTree) {
        p_tree = pTree;
        d_tree = new DepTree();

        initDepTree(pTree.getRootNode());
        setDepHeads(pTree.getRootNode());
        configureConjunctions();
        setDepRoot();

        d_tree.checkTree();
        return d_tree;
    }

    /**
     * Initializes
     * <code>tree</code> using the subtree of
     * <code>curr</code>.
     */
    private void initDepTree(TBNode curr) {
        curr.pos = curr.pos.split(";")[0];

        if (curr.isPhrase()) {
            for (TBNode child : curr.getChildren()) {
                initDepTree(child);
            }
        } else {
            DepNode node = new DepNode();

            node.id = curr.terminalId + 1;
            node.lemma = curr.form;
            node.pos = curr.pos;

            d_tree.add(node);
        }
    }

    /**
     * Finds heads for all phrases.
     */
    private void setDepHeads(TBNode curr) {
        if (!curr.isPhrase()) {
            return;
        }

        // traverse all subtrees
        for (TBNode child : curr.getChildren()) {
            setDepHeads(child);
        }

        // top-level constituent
        if (curr.isPos(TBLib.POS_TOP)) {
            return;
        }

        // find heads of all subtrees
        findHeads(curr);
        setDepHeadsAux(curr);
    }

    /**
     * Finds heads of all phrases under
     * <code>curr</code> using
     * <code>headrules</code>.
     * <code>beginId</code> inclusive,
     * <code>endId</code> exclusive.
     */
    private void findHeads(TBNode curr) {
        TBHeadRule headrule = g_headrules.getHeadRule(curr.pos);
        ArrayList<TBNode> children = curr.getChildren();

        if (children.size() == 1) {
            curr.headId = children.get(0).headId;
            return;
        }

        if (findHeadByTag(curr, headrule)) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (findHeadByRule(curr, headrule, i)) {
                break;
            }
        }
    }

    private boolean findHeadByTag(TBNode curr, TBHeadRule headrule) {
        if (curr.tags == null) {
            return false;
        }

        ArrayList<TBNode> children = curr.getChildren();
        ArrayList<TBNode> tNodes = new ArrayList<>();
        String tag = (String) curr.tags.toArray()[0];
        TBNode child;
        int i, size = children.size();

        if (size == 2) {
            child = children.get(1);

            if (child.isPos(TBKrLib.POS_X + "|" + TBKrLib.POS_S) && child.isTag(tag) && !child.isPhrase()) {
                curr.headId = children.get(0).headId;
                return true;
            }
        }

        // curr.tag == child.tag
        for (i = size - 1; i >= 0; i--) {
            child = children.get(i);

            if (child.isTag(tag)) {
                if (child.isPos(curr.pos)) {
                    curr.headId = child.headId;
                    return true;
                }

                if (!child.isPos(TBKrLib.POS_X + "|" + TBKrLib.POS_L + "|" + TBKrLib.POS_R)) {
                    tNodes.add(child);
                }
            }
        }

        // only one child with the same tag
        if ((size = tNodes.size()) == 1) {
            curr.headId = tNodes.get(0).headId;
            return true;
        }

        if (size > 1) {
            for (String rule : headrule.rules) {
                if (headrule.dir == -1) {
                    for (i = size - 1; i >= 0; i--) {
                        if (findHeadAux(curr, tNodes.get(i), rule, 3)) {
                            return true;
                        }
                    }
                } else {
                    for (i = 0; i < size; i++) {
                        if (findHeadAux(curr, tNodes.get(i), rule, 3)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean findHeadByRule(TBNode curr, TBHeadRule headrule, int isSkip) {
        ArrayList<TBNode> children = curr.getChildren();
        int i, size = children.size();

        for (String rule : headrule.rules) {
            if (headrule.dir == -1) {
                for (i = 0; i < size; i++) {
                    if (findHeadAux(curr, children.get(i), rule, isSkip)) {
                        return true;
                    }
                }
            } else {
                for (i = size - 1; i >= 0; i--) {
                    if (findHeadAux(curr, children.get(i), rule, isSkip)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * This method is called by {@link TBKrConvert#findHeads(TBNode, TBHeadRules)}
     * and {@link TBKrConvert#findGapHeads(TBNode, TBHeadRules)}.
     */
    private boolean findHeadAux(TBNode curr, TBNode child, String rule, int isSkip) {
        if (isSkip == 0) {
            if (child.isTag(TBKrLib.TAG_CNJ + "|" + TBKrLib.TAG_MOD)
                    || child.isTag(TBKrLib.TAG_PRN)
                    || child.isPos(TBKrLib.POS_L + "|" + TBKrLib.POS_R)
                    || (child.isPos(TBKrLib.POS_X + "|" + TBKrLib.POS_S) && !child.isPhrase())) {
                return false;
            }
        } else if (isSkip == 1) {
            if (child.isTag(TBKrLib.TAG_PRN)
                    || child.isPos(TBKrLib.POS_L + "|" + TBKrLib.POS_R)
                    || (child.isPos(TBKrLib.POS_X + "|" + TBKrLib.POS_S) && !child.isPhrase())) {
                return false;
            }
        } else if (isSkip == 2) {
            if (child.isPos(TBKrLib.POS_L + "|" + TBKrLib.POS_R)
                    || (child.isPos(TBKrLib.POS_X + "|" + TBKrLib.POS_S) && !child.isPhrase())) {
                return false;
            }
        }

        if (child.isRule(rule)) {
            curr.headId = child.headId;
            return true;
        }

        return false;
    }

    private void configureConjunctions() {
        IntObjectOpenHashMap<ArrayList<DepNode>> map = new IntObjectOpenHashMap<>();
        ArrayList<DepNode> list;
        DepNode curr;

        for (int i = 1; i < d_tree.size(); i++) {
            curr = d_tree.get(i);

            if (curr.isDeprel(TBKrLib.TAG_CNJ)) {
                if (map.containsKey(curr.headId)) {
                    list = map.get(curr.headId);
                } else {
                    list = new ArrayList<>();
                    map.put(curr.headId, list);
                }

                list.add(curr);
            }
        }

        for (int key : map.keys().toArray()) {
            list = map.get(key);

            for (int i = 0; i < list.size() - 1; i++) {
                list.get(i).headId = list.get(i + 1).id;
            }
        }
    }

    /**
     * Assigns the root of the dependency tree.
     */
    private void setDepRoot() {
        for (int i = 1; i < d_tree.size(); i++) {
            DepNode node = d_tree.get(i);

            if (node.headId == DepLib.NULL_HEAD_ID) {
                node.setHead(DepLib.ROOT_ID, DepLib.DEPREL_ROOT, 0);
            }
        }
    }

    private void setDepHeadsAux(TBNode curr) {
        ArrayList<TBNode> children = curr.getChildren();
        TBNode child;

        for (int i = 0; i < children.size(); i++) {
            child = children.get(i);

            if (child.headId == curr.headId) {
                continue;
            }
            //	if (hasHead(child.headId))			continue;

            String deprel = getDeprel(curr, child);
            setDependency(child.headId, curr.headId, deprel);
        }
    }

    private String getDeprel(TBNode parent, TBNode child) {
        String deprel;

        TBNode p = p_tree.getTerminalNodes().get(parent.headId);
        TBNode c = p_tree.getTerminalNodes().get(child.headId);

        if (child.isPos(TBKrLib.POS_L + "|" + TBKrLib.POS_R) || TBKrLib.isPunctuation(c.form)) {
            return TBKrLib.DEP_P;
        }
        if ((deprel = getXDeprel(child)) != null) {
            return deprel;
        }
        if ((deprel = getTagDeprel(child)) != null) {
            return deprel;
        }
        if ((deprel = getInferredDeprel(p, c)) != null) {
            return deprel;
        }

        return DepLib.DEPREL_DEP;
    }

    private String getXDeprel(TBNode child) {
        if (child.isPos(TBKrLib.POS_X + "|" + TBKrLib.POS_S) && !child.isPhrase()) {
            String tag = getTagDeprel(child);

            if (tag != null) {
                return TBKrLib.POS_X + "_" + tag;
            } else {
                return TBKrLib.POS_X;
            }
        }

        return null;
    }

    private String getTagDeprel(TBNode child) {
        if (child.tags != null) {
            String deprel = (String) child.tags.toArray()[0];
            return deprel;
        }

        return null;
    }

    private String getInferredDeprel(TBNode p, TBNode c) {
        if (c.isPos(TBKrLib.POS_AP)) {
            return TBKrLib.DEP_ADV;
        }
        if (p.isPos(TBKrLib.POS_AP)) {
            return TBKrLib.DEP_AMOD;
        }
        if (p.isPos(TBKrLib.POS_DP)) {
            return TBKrLib.DEP_DMOD;
        }
        if (p.isPos(TBKrLib.POS_NP)) {
            return TBKrLib.DEP_NMOD;
        }
        if (p.isPos(TBKrLib.POS_VP + "|" + TBKrLib.POS_VNP + "|" + TBKrLib.POS_IP)) {
            return TBKrLib.DEP_VMOD;
        }
        //	if (p.isPos(TBKrLib.POS_Q))
        //		return TBKrLib.DEP_QMOD;

        return null;
    }

    /**
     * Assigns the dependency head of the current node.
     */
    private void setDependency(int currId, int headId, String deprel) {
        d_tree.setHead(currId + 1, headId + 1, deprel, 1);
    }

    TBNode getTagNode(TBNode root, TBNode c, String tag) {
        if (c.isTag(tag)) {
            return c;
        }

        TBNode parent = c.getParent();

        while (parent != null && !parent.equals(root)) {
            if (parent.isTag(tag)) {
                return parent;
            }
            parent = parent.getParent();
        }

        return null;
    }

    /**
     * @return true if the current node already has its dependency head.
     */
    boolean hasHead(int currId) {
        return d_tree.get(currId + 1).hasHead;
    }
}