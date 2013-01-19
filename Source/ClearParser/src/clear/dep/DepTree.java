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

import clear.dep.srl.SRLInfo;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.reader.AbstractReader;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Dependency tree.
 *
 * @see DepNode
 * @author Jinho D. Choi <b>Last update:</b> 4/19/2011
 */
@SuppressWarnings("serial")
public class DepTree extends ArrayList<DepNode> implements ITree<DepNode> {

    /**
     * Number of transitions made during parsing
     */
    public int n_trans;
    /**
     * Global score excluding scores from nodes (e.g. SHIFT scores)
     */
    public double d_score;

    /**
     * Initializes the dependency tree. The root node is already inserted.
     */
    public DepTree() {
        DepNode root = new DepNode();
        root.toRoot();

        add(root);
        init(0, 0d);
    }

//	==================================== Construct ====================================
    /**
     * Initializes member variables.
     *
     * @param nTrans {@link DepTree#n_trans}
     * @param score  {@link DepTree#d_score}
     */
    public void init(int nTrans, double score) {
        n_trans = nTrans;
        d_score = score;
    }

    public void copy(DepTree tree) {
        for (int i = 1; i < size(); i++) {
            get(i).copy(tree.get(i));
        }
    }

    @Override
    public DepTree clone() {
        DepTree tree = new DepTree();

        for (int i = 1; i < size(); i++) {
            tree.add(get(i).clone());
        }

        tree.trimToSize();
        return tree;
    }

    /**
     * Each node is separated by a new line ('\n').
     *
     * @return the string representation of the tree.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        for (int i = 1; i < size(); i++) {
            buff.append(get(i));
            buff.append("\n");
        }

        return buff.toString().trim();
    }

//	==================================== Boolean ====================================
    /**
     * @return true if
     * <code>index</code> is in [0, {@link ITree#size()})
     */
    public boolean isRange(int index) {
        return 0 <= index && index < size();
    }

    /**
     * @return true if the
     * <code>node1Id</code>'th node is the ancestor of the
     * <code>node2Id</code>'th node.
     */
    public boolean isAncestor(int nodeId1, int nodeId2) {
        DepNode node2 = get(nodeId2);

        if (!node2.hasHead) {
            return false;
        }
        if (node2.headId == nodeId1) {
            return true;
        }

        return isAncestor(nodeId1, node2.headId);
    }

    /**
     * @return true if the
     * <code>node1</code> is the ancestor of the
     * <code>node2</code>.
     */
    public boolean isAncestor(DepNode node1, DepNode node2) {
        if (!node2.hasHead) {
            return false;
        }
        if (node2.headId == node1.id) {
            return true;
        }

        return isAncestor(node1, get(node2.headId));
    }

    public boolean existsLeftDependent(int currId, String deprel) {
        DepNode node;

        for (int i = currId - 1; i > 0; i--) {
            node = get(i);

            if (node.hasHead && node.headId == currId && node.isDeprel(deprel)) {
                return true;
            }
        }

        return false;
    }

    public boolean existsRightDependent(int currId, String deprel) {
        DepNode node;

        for (int i = currId + 1; i < size(); i++) {
            node = get(i);

            if (node.hasHead && node.headId == currId && node.isDeprel(deprel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Prints errors if not unique-root, single-headed, connected, acyclic.
     *
     * @return true if there is no error.
     */
    public boolean checkTree() {
        int countRoot = 0;

        for (int i = 1; i < size(); i++) {
            DepNode node = get(i);

            if (node.headId == DepLib.ROOT_ID) {
                countRoot++;
            }

            if (!isRange(node.headId)) {
                System.err.println("Not connected: " + node.id + " <- " + node.headId);
                return false;
            }

            if (isAncestor(node.id, node.headId)) {
                System.err.println("Cycle exists: " + node.id + " <-*-> " + node.headId);
                return false;
            }
        }

        if (countRoot != 1) {
            System.err.println("Not single-rooted: " + countRoot);
            //	System.err.println(this.toString());
            return false;
        }

        return true;
    }

//	==================================== Getter ====================================
    /**
     * Returns the head of the
     * <code>currId</code>'th node. If there is no such head, returns a null
     * node.
     *
     * @param currId index of the node to find the head for
     */
    public DepNode getHead(int currId) {
        DepNode curr = get(currId);

        return curr.hasHead ? get(curr.headId) : null;
    }

    /**
     * @return last node in the tree.
     */
    public DepNode getLastNode() {
        return get(size() - 1);
    }

    /**
     * @return the leftmost dependent of the
     * <code>currId</code>'th node.
     */
    public DepNode getLeftMostDependent(int currId) {
        return get(currId).leftMostDep;
    }

    /**
     * @return the rightmost dependent of the
     * <code>currId</code>'th node.
     */
    public DepNode getRightMostDependent(int currId) {
        return get(currId).rightMostDep;
    }

    public DepNode getLeftSibling(int currId) {
        return get(currId).leftSibling;
    }

    public DepNode getRightSibling(int currId) {
        return get(currId).rightSibling;
    }

    /**
     * Returns the index of the left-nearest punctuation of the
     * <code>currId</code>'th node. Punctuation is defined in
     * <code>lib</code> and the index can be retrieved from it. It stops
     * searching when it meets
     * <code>leftBoundId</code>'th node.
     *
     * @param currId index of the current node
     * @param leftBoundId index of the left bound
     * @param map feature mapping containing indices of punctuation
     */
    public int getLeftNearestPunctuation(int currId, int leftBoundId, DepFtrMap map) {
        int i, puncIndex;

        for (i = currId - 1; i >= leftBoundId; i--) {
            puncIndex = map.extraToIndex(0, get(i).form);
            if (puncIndex >= 0) {
                return puncIndex;
            }
        }

        return -1;
    }

    /**
     * Returns the index of the right-nearest punctuation of the
     * <code>currId</code>'th node. Punctuation is defined in
     * <code>lib</code> and the index can be retrieved from it. It searches upto
     * the
     * <code>rightBoundId</code>'th node (inclusive).
     *
     * @param currId index of the current node
     * @param rightBoundId index of the right bound
     * @param map feature mapping containing indices of punctuation
     */
    public int getRightNearestPunctuation(int currId, int rightBoundId, DepFtrMap map) {
        int i, puncIndex;

        for (i = currId + 1; i <= rightBoundId; i++) {
            puncIndex = map.extraToIndex(0, get(i).form);
            if (puncIndex >= 0) {
                return puncIndex;
            }
        }

        return -1;
    }

    public ArrayList<DepNode> getLeftDependents(int currId) {
        ArrayList<DepNode> list = new ArrayList<>();

        for (int i = 1; i < currId; i++) {
            DepNode node = get(i);
            if (node.hasHead && node.headId == currId) {
                list.add(node);
            }
        }

        return list;
    }

    public ArrayList<DepNode> getRightDependents(int currId) {
        ArrayList<DepNode> list = new ArrayList<>();

        for (int i = size() - 1; i > currId; i--) {
            DepNode node = get(i);
            if (node.hasHead && node.headId == currId) {
                list.add(node);
            }
        }

        return list;
    }

    public ArrayList<DepNode> getRightDependentsLR(int currId) {
        ArrayList<DepNode> list = new ArrayList<>();

        for (int i = currId + 1; i < size(); i++) {
            DepNode node = get(i);
            if (node.hasHead && node.headId == currId) {
                list.add(node);
            }
        }

        return list;
    }

    public HashSet<String> getLeftDependencies(int currId) {
        HashSet<String> set = new HashSet<>();

        for (int i = 1; i < currId; i++) {
            DepNode node = get(i);
            if (node.hasHead && node.headId == currId) {
                set.add(node.deprel);
            }
        }

        return set;
    }

    public HashSet<String> getRightDependencies(int currId) {
        HashSet<String> set = new HashSet<>();

        for (int i = size() - 1; i > currId; i--) {
            DepNode node = get(i);
            if (node.hasHead && node.headId == currId) {
                set.add(node.deprel);
            }
        }

        return set;
    }

    /**
     * Experimental
     */
    public DepNode getRightNearestDependent(int currId) {
        DepNode node;

        for (int i = currId + 1; i < size(); i++) {
            node = get(i);
            if (node.headId == currId) {
                return node;
            }
        }

        return null;
    }

    /**
     * Assumes all nodes already have heads.
     *
     * @return all dependents of
     * <code>currId</code>'th node
     */
    public ArrayList<DepNode> getDependents(int currId) {
        ArrayList<DepNode> list = new ArrayList<>();

        for (int i = 1; i < size(); i++) {
            DepNode node = get(i);
            if (node.headId == currId) {
                list.add(node);
            }
        }

        return list;
    }

    public HashSet<String> getDeprelDepSet(int currId) {
        HashSet<String> set = new HashSet<>();

        for (int i = 1; i < size(); i++) {
            DepNode node = get(i);
            if (node.headId == currId) {
                set.add(node.deprel);
            }
        }

        return set;
    }

    public String getPath(String field, int fromId, int toId, byte flag) {
        DepNode fNode = get(fromId);
        DepNode tNode = get(toId);

        if (isAncestor(fNode, tNode)) {
            return getPathDown(field, fNode, tNode);
        } else if (isAncestor(tNode, fNode)) {
            return getPathUp(field, fNode, tNode);
        }

        DepNode head = tNode;

        while (head.headId >= 0) {
            head = get(head.headId);

            if (isAncestor(head, fNode)) {
                StringBuilder build = new StringBuilder();

                if (flag == 0) {
                    build.append(getPathUp(field, fNode, head));
                    build.append(getPathDown(field, head, tNode));
                } else if (flag == 1) {
                    build.append(getPathUp(field, fNode, head));
                } else if (flag == 2) {
                    build.append(getPathDown(field, head, tNode));
                } else if (flag == 10) {
                    String up = getPathUp(field, fNode, head);
                    String down = getPathDown(field, head, tNode);

                    build.append("u");
                    build.append(up.split("^").length);
                    build.append("d");
                    build.append(down.split("|").length);
                }

                return build.toString();
            }
        }

        return "NO_PATH";
    }

    private String getPathDown(String field, DepNode fNode, DepNode tNode) {
        ArrayDeque<String> deq = new ArrayDeque<>();
        String curr;

        while (tNode != fNode) {
            curr = field.equals(DepFtrXml.F_DEPREL) ? tNode.deprel : tNode.pos;

            deq.push(curr);
            tNode = get(tNode.headId);
        }

        StringBuilder build = new StringBuilder();

        while (!deq.isEmpty()) {
            build.append("|");
            build.append(deq.pop());
        }

        return build.toString();
    }

    private String getPathUp(String field, DepNode fNode, DepNode tNode) {
        StringBuilder build = new StringBuilder();
        String curr;

        while (fNode != tNode) {
            curr = field.equals(DepFtrXml.F_DEPREL) ? fNode.deprel : fNode.pos;

            build.append("^");
            build.append(curr);

            fNode = get(fNode.headId);
        }

        return build.toString();
    }

    // flag: 0 - whole, 1 - left-side, 2 - right-side 
    public String getSubcat(String field, int currId, byte flag) {
        StringBuilder build = new StringBuilder();
        String curr;

        for (DepNode node : getDependents(currId)) {
            if (!node.isDeprel(DepLib.DEPREL_P)) {
                curr = field.equals(DepFtrXml.F_DEPREL) ? node.deprel : node.pos;

                if ((flag == 0) || (flag == 1 && node.id < currId) || (flag == 2 && node.id > currId)) {
                    build.append(curr);
                    build.append("_");
                }
            }
        }

        return build.toString();
    }

    public DepNode getHighestVC(int currId) {
        DepNode node = get(currId);

        while (DepLib.M_VC.matcher(node.deprel).matches()) {
            node = get(node.headId);
        }

        return (node.id == currId) ? null : node;
    }

    public DepNode getFirstVC(int currId) {
        DepNode node;

        for (int i = 1; i < size(); i++) {
            node = get(i);

            if (node.headId == currId && node.isDeprel(DepLib.DEPREL_VC)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Including the current node.
     */
    public ArrayList<DepNode> getVCList(int currId) {
        ArrayList<DepNode> list = new ArrayList<>();
        DepNode curr = get(currId);
        if (curr.isPosx("VB.*")) {
            list.add(curr);
        }

        getVCListAux(currId, list);
        return list;
    }

    private void getVCListAux(int currId, ArrayList<DepNode> list) {
        DepNode vc = getFirstVC(currId);

        if (vc != null) {
            list.add(vc);
            getVCListAux(vc.id, list);
        }
    }

    public DepNode getRightNearestCoord(int currId, String regex) {
        int i, size = size();
        DepNode node;

        for (i = currId + 1; i < size; i++) {
            node = get(i);

            if (node.headId == currId && node.deprel.matches("COORD|CONJ")) {
                if (node.isPosx(regex)) {
                    return node;
                }
                return getRightNearestCoord(node.id, regex);
            }
        }

        return null;
    }

    public boolean isReference(int currId) {
        if (get(currId).isPosx("W.*")) {
            return true;
        }
        if (currId - 1 > 0 && get(currId + 1).isPosx("W.*")) {
            return true;
        }

        /*
         * for (DepNode node : getDependents(currId)) {
		}
         */

        return false;
    }

    public IntOpenHashSet getVCIdSet(int currId) {
        IntArrayList list = new IntArrayList();
        DepNode node = get(currId);

        while (node.id > 0) {
            if (DepLib.M_VC.matcher(node.deprel).matches()) {
                list.add(node.headId);
            }

            node = get(node.headId);
            if (node.isPosx("VB.*")) {
                list.add(node.id);
            }
        }

        if (!list.isEmpty()) {
            list.add(get(list.get(list.size() - 1)).headId);
        }

        return new IntOpenHashSet(list);
    }

    public String getPRT(int predId) {
        if (!get(predId).isPredicate()) {
            return null;
        }
        DepNode node;

        for (int i = predId + 1; i < size(); i++) {
            node = get(i);
            if (node.headId == predId && node.isDeprel(DepLib.DEPREL_PRT)) {
                return node.form;
            }
        }

        return null;
    }

    public IntOpenHashSet getSubIdSet(int currId) {
        IntOpenHashSet set = new IntOpenHashSet();

        getSubIdSetAux(currId, set);
        return set;
    }

    private void getSubIdSetAux(int currId, IntOpenHashSet set) {
        set.add(currId);

        for (DepNode node : getDependents(currId)) {
            set.add(node.id);
            getSubIdSetAux(node.id, set);
        }
    }

    public String getSubstring(int currId) {
        int[] arr = getSubIdSet(currId).toArray();
        Arrays.sort(arr);

        StringBuilder build = new StringBuilder();

        for (int idx : arr) {
            build.append(get(idx).lemma);
            build.append(" ");
        }

        return build.toString().trim();
    }

    /**
     * @return the score of the tree.
     */
    public double getScore() {
        double score = d_score;
        for (int i = 1; i < size(); i++) {
            score += get(i).score;
        }

        return score;
    }

    public int prevPredicateId(int currId) {
        for (int id = currId - 1; id > 0; id--) {
            if (get(id).isPredicate()) {
                return id;
            }
        }

        return -1;
    }

    public int nextPredicateId(int currId) {
        for (int id = currId + 1; id < size(); id++) {
            if (get(id).isPredicate()) {
                return id;
            }
        }

        return size();
    }

//	==================================== Setter ====================================
    /**
     * Sets the
     * <code>headId</code>'th node as the head of the
     * <code>currId</code>'th node.
     *
     * @see DepNode#setHead(int, String, double)
     * @param currId index of the current node
     * @param headId index of the head node
     * @param deprel dependency label between the current and the head nodes
     * @param score score of the headId'th node being the head of the node
     */
    public void setHead(int currId, int headId, String deprel, double score) {
        DepNode curr = get(currId);
        curr.setHead(headId, deprel, score);
    }

    public void clearDepHeads() {
        for (int i = 1; i < size(); i++) {
            get(i).clearDepHead();
        }
    }

    public void clearSRLHeads() {
        for (int i = 1; i < size(); i++) {
            get(i).clearSRLHeads();
        }
    }

    public void setSubcat() {
        DepNode node, head, prev, next;
        int i, j;

        for (i = 1; i < size(); i++) {
            node = get(i);

            if (node.headId >= 0) {
                head = get(node.headId);

                if (node.id < head.id) {
                    if (head.leftMostDep == null || head.leftMostDep.id > node.id) {
                        head.leftMostDep = node;
                    }
                } else {
                    if (head.rightMostDep == null || head.rightMostDep.id < node.id) {
                        head.rightMostDep = node;
                    }
                }
            }

            ArrayList<DepNode> children = getDependents(node.id);

            for (j = 1; j < children.size(); j++) {
                prev = children.get(j - 1);
                next = children.get(j);

                next.leftSibling = prev;
            }

            for (j = 0; j < children.size() - 1; j++) {
                prev = children.get(j);
                next = children.get(j + 1);

                prev.rightSibling = next;
            }
        }
    }

    /**
     * Make non-projective dependencies on punctuation as projective.
     */
    public void projectizePunc() {
        for (int i = 1; i < size(); i++) {
            DepNode curr = get(i);
            if (curr.isDeprel(DepLib.DEPREL_P)) {
                continue;
            }
            DepNode head = get(curr.headId);

            int sId, eId;
            if (curr.id < head.id) {
                sId = curr.id;
                eId = head.id;
            } else {
                sId = head.id;
                eId = curr.id;
            }

            for (int j = sId + 1; j < eId; j++) {
                DepNode node = get(j);

                if (node.isDeprel(DepLib.DEPREL_P) && (sId > node.headId || node.headId > eId)) {
                    if (curr.headId != DepLib.ROOT_ID) {
                        node.headId = curr.headId;
                    } else {
                        node.headId = curr.id;
                    }
                }
            }
        }
    }

    public void swap(int id1, int id2) {
        DepNode node1 = get(id1);
        DepNode node2 = get(id2);
        DepNode tNode = node1.clone();

        node1.copy(node2);
        node2.copy(tNode);
    }

    public void setPredicates(String language) {
        DepNode node, head;
        String roleset;

        for (int i = 1; i < size(); i++) {
            node = get(i);

            if (node.headId >= 0) {
                node.hasHead = true;
                head = get(node.headId);
            } else {
                head = null;
            }

            if (language.equals(AbstractReader.LANG_EN)) {
                roleset = isEnPredicate(node, head) ? node.lemma + ".XX" : DepLib.FIELD_BLANK;
                node.srlInfo = new SRLInfo(roleset, DepLib.FIELD_BLANK);
            }
        }
    }

    private boolean isEnPredicate(DepNode node, DepNode head) {
        if (!node.isPosx("VB.*")) {
            return false;
        }

        if (node.headId > node.id && head != null && head.pos.startsWith("NN")) {
            return false;
        }

        if (node.deprel.startsWith("aux")) {
            return false;
        }

        DepNode dep;

        for (int i = node.id + 1; i < size(); i++) {
            dep = get(i);
            if (dep.headId == node.id && dep.isDeprel("VC")) {
                return false;
            }
        }

        return true;
    }

    public void setHasHeads() {
        DepNode node;

        for (int i = 1; i < size(); i++) {
            node = get(i);
            if (node.headId >= 0) {
                node.hasHead = true;
            }
        }
    }
}