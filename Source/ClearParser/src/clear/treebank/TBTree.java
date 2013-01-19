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

import clear.dep.DepLib;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.propbank.PBLib;
import clear.propbank.PBLoc;
import clear.util.tuple.JIntIntTuple;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Tree as in Penn Treebank.
 *
 * @author Jinho D. Choi <b>Last update:</b> 1/27/2011
 */
public class TBTree {

    /**
     * Pointer to the root node
     */
    private TBNode nd_root;
    /**
     * Pointer to terminal nodes
     */
    private ArrayList<TBNode> ls_terminal;

    /**
     * Initializes the tree.
     */
    public TBTree() {
        ls_terminal = new ArrayList<>();
    }

    /**
     * @return node in
     * <code>terminalId:height</code>.
     */
    public TBNode getNode(int terminalId, int height) {
        TBNode node;

        if ((node = getTerminalNode(terminalId)) != null) {
            for (int i = 0; i < height; i++) {
                if (node.getParent() == null) {
                    return null;
                }
                node = node.getParent();
            }
        }

        return node;
    }

    public TBNode getNode(PBLoc pbLoc) {
        return getNode(pbLoc.terminalId, pbLoc.height);
    }

    /**
     * @return the root node.
     */
    public TBNode getRootNode() {
        return nd_root;
    }

    /**
     * Sets the root node of this tree.
     */
    public void setRootNode(TBNode root) {
        nd_root = root;
    }

    /**
     * @return terminal node with the terminal ID. If the terminal ID is not
     * within the range, return null.
     */
    public TBNode getTerminalNode(int terminalId) {
        if (0 <= terminalId && terminalId < ls_terminal.size()) {
            return ls_terminal.get(terminalId);
        }

        return null;
    }

    /**
     * @return list of terminal nodes.
     */
    public ArrayList<TBNode> getTerminalNodes() {
        return ls_terminal;
    }

    /**
     * Adds a terminal node.
     */
    public void addTerminalNode(TBNode node) {
        ls_terminal.add(node);
    }

    /**
     * Assigns the PropBank locations to all nodes.
     */
    public void setPBLocs() {
        TBNode parent;
        int height;

        for (TBNode node : ls_terminal) {
            parent = node;
            height = 0;
            node.pbLoc = new PBLoc(null, node.terminalId, height++);

            while ((parent = parent.getParent()) != null) {
                if (parent.pbLoc != null) {
                    break;
                }
                parent.pbLoc = new PBLoc("", node.terminalId, height++);
            }
        }
    }

    /**
     * @return antecedent node indicated by the co-index. If there is no such
     * node, return null.
     */
    public TBNode getAntecedent(int coIndex) {
        return getAntecedentAux(nd_root, coIndex);
    }

    /**
     * Called from {@link TBTree#getAntecedent(int)}.
     */
    public TBNode getAntecedentAux(TBNode curr, int coIndex) {
        if (curr.coIndex == coIndex) {
            return curr;
        }
        if (!curr.isPhrase()) {
            return null;
        }

        for (TBNode child : curr.getChildren()) {
            TBNode node = getAntecedentAux(child, coIndex);
            if (node != null) {
                return node;
            }
        }

        return null;
    }

    /**
     * Finds antecedents of all empty categories and complementizer.
     */
    public void setAntecedents() {
        setAntecedentsEmptyCategory();
        setAntecedentsComplementizer(nd_root);
    }

    /**
     * Called from {@link TBTree#setAntecedents()}.
     */
    private void setAntecedentsEmptyCategory() {
        TBNode ante;
        int coIndex;

        for (TBNode node : ls_terminal) {
            coIndex = node.getEmptyCategoryCoIndex();
            if (coIndex == -1) {
                continue;
            }

            ante = getAntecedent(coIndex);
            if (ante == null) {
                //	if (!node.isForm("\\*PRO\\*.*") && !node.getParent().isPos("WH.*"))
                System.err.println("Missing antecedent " + coIndex + ": " + node.form);//+"\n"+toTree());
            } else {
                ante.pbLoc.type = PBLib.PROP_OP_ANTE;
                node.antecedent = ante;
            }
        }
    }

    /**
     * Called from {@link TBTree#setAntecedents()}.
     */
    private void setAntecedentsComplementizer(TBNode curr) {
        if (!curr.isPhrase()) {
            return;
        }

        for (TBNode child : curr.getChildren()) {
            if (curr.isPos(TBEnLib.POS_SBAR) && child.isPos("WH.*")) {
                TBNode parent = curr.getParent();

                if (parent != null && parent.isPos("NP|VP")) {
                    ArrayList<TBNode> siblings = parent.getChildren();
                    TBNode ante;

                    for (int i = curr.childId - 1; i >= 0; i--) {
                        ante = siblings.get(i);

                        if ((parent.isPos("NP") && ante.isPos("NP")) || (parent.isPos("VP") && ante.isPos("PP"))) {
                            TBNode comp = child.getComplementizer();

                            if (comp != null) {
                                ante.pbLoc.type = PBLib.PROP_OP_ANTE;
                                comp.pbLoc.type = PBLib.PROP_OP_COMP;
                                comp.antecedent = ante;
                            }

                            break;
                        }
                    }
                }
            }

            setAntecedentsComplementizer(child);
        }
    }

    /**
     * @return tree representation in Penn Treebank style.
     */
    public String toTree() {
        return "(" + toTreeAux(nd_root, "") + ")";
    }

    /**
     * Called from {@link TBTree#toTreeAux(TBNode, String).}
     */
    private String toTreeAux(TBNode node, String indent) {
        String str = indent + "(" + node.getTags();
        if (!node.isPhrase()) {
            return str += " " + node.form + ")";
        }

        for (TBNode child : node.getChildren()) {
            str += "\n" + toTreeAux(child, indent + "  ");
        }

        return str + ")";
    }

    public boolean isUnder(int terminalId, String phrase) {
        TBNode curr;

        for (int i = 1; i < 100; i++) {
            curr = getNode(terminalId, i);
            if (curr == null) {
                return false;
            }
            if (curr.isPos(phrase)) {
                return true;
            }
        }

        return false;
    }

    public boolean isLastRNR(TBNode node) {
        for (int i = node.terminalId + 1; i < ls_terminal.size(); i++) {
            if (ls_terminal.get(i).form.equals((node.form))) {
                return false;
            }
        }

        return true;
    }

    public void mapSRLTree(DepTree tree) {
        mapSRLTreeAux(tree, getRootNode());
        mapSRLTreeAuxClean(tree);
    }

    private void mapSRLTreeAux(DepTree tree, TBNode tNode) {
        if (tNode.headId >= 0) {
            DepNode dNode = tree.get(tNode.headId + 1);

            if (tNode.pb_heads != null) {
                dNode.addSRLHeads(tNode.pb_heads);
                Collections.sort(dNode.srlInfo.heads);
            } else if (tNode.rolesetId != null) {
                dNode.setRolesetId(tNode.rolesetId);
            }

            if (tNode.antecedent != null) {
                TBNode ante = getNode(tNode.antecedent.pbLoc);
                dNode.antecedent = tree.get(ante.headId + 1);
            }
        }

        if (tNode.isPhrase()) {
            for (TBNode child : tNode.getChildren()) {
                mapSRLTreeAux(tree, child);
            }
        }
    }

    private void mapSRLTreeAuxClean(DepTree tree) {
        ArrayList<SRLHead> list;
        DepNode node, head;

        for (int i = 1; i < tree.size(); i++) {
            node = tree.get(i);
            head = tree.get(node.headId);
            list = new ArrayList<>();

            while (!head.isRoot()) {
                for (SRLHead tmp : head.srlInfo.heads) {
                    if (!tree.isAncestor(head.id, tmp.headId)) {
                        list.add(tmp);
                    } else if (tree.get(tmp.headId).isDeprel(DepLib.DEPREL_PRN)) {
                        list.add(tmp);
                    }
                }

                head = tree.get(head.headId);
            }

            node.removeSRLHeads(list);
        }
    }

    public String formsWithoutSpace() {
        StringBuilder build = new StringBuilder();

        for (TBNode node : ls_terminal) {
            if (node.isEmptyCategory()) {
                continue;
            }
            node.form = node.form.replaceAll("\\\\/", "/");
            build.append(node.form);
        }

        return build.toString();
    }

    public ArrayList<JIntIntTuple> getCharIdToTerminalIdMap() {
        ArrayList<JIntIntTuple> map = new ArrayList<>();
        int length = 0;

        for (TBNode node : ls_terminal) {
            if (node.isEmptyCategory()) {
                continue;
            }

            map.add(new JIntIntTuple(length, node.terminalId));
            length += node.form.length();
        }

        return map;
    }
}