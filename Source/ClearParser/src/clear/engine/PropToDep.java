/**
 * Copyright (c) 2011, Regents of the University of Colorado All rights
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
package clear.engine;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.morph.MorphEnAnalyzer;
import clear.propbank.*;
import clear.treebank.*;
import clear.util.IOUtil;
import clear.util.JSet;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Compare two dependency trees.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/26/2010
 */
public class PropToDep {

    @Option(name = "-i", usage = "name of a file containing PropBank instances", required = true, metaVar = "REQUIRED")
    String s_propFile;
    @Option(name = "-o", usage = "name of a directory for dependency output", required = true, metaVar = "REQUIRED")
    String s_srlDir;
    @Option(name = "-p", usage = "name of a directory containing parse trees", required = true, metaVar = "REQUIRED")
    String s_parseDir;
    @Option(name = "-h", usage = "name of a file containing head-percolation rules", required = true, metaVar = "REQUIRED")
    String s_headruleFile;
    @Option(name = "-m", usage = "name of a file containing dictionaries for morphological analyzer", metaVar = "OPTIONAL")
    String s_dictFile = null;
    @Option(name = "-n", usage = "minimum sentence length (inclusive; default = 1)", metaVar = "OPTIONAL")
    int n_length = 1;
    @Option(name = "-f", usage = "if set, include function tags", metaVar = "OPTIONAL")
    boolean b_funcTag = false;
    @Option(name = "-e", usage = "if set, include empty categories", metaVar = "OPTIONAL")
    boolean b_ec = false;
    @Option(name = "-r", usage = "if set, reverse dependencies of auxiliaries and modals", metaVar = "OPTIONAL")
    boolean b_reverseVC = false;
    @Option(name = "-j", usage = "if set, consider adjectival predicates as verbal predicates", metaVar = "OPTIONAL")
    boolean b_adjPred = false;
//	final String PARSE_EXT = ".parse";
    final String PARSE_EXT = "";
    final String SRL_EXT = ".srl";
    HashMap<String, PBInstance> m_pbInstances;

    public PropToDep(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);

            readPBInstances();
            merge();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        }
    }

    /**
     * Reads all PropBank instances and stores them to {@link PropToDep#m_pbInstances}.
     */
    public void readPBInstances() {
        PBReader reader = new PBReader(s_propFile);
        PBInstance instance;

        m_pbInstances = new HashMap<>();
        System.out.println("Initialize: " + s_propFile);

        while ((instance = reader.nextInstance()) != null) {
            if (instance.rolesetId.endsWith(".LV")) {
                continue;
            }
            if (!instance.type.endsWith("-v")) {
                continue;
            }

            m_pbInstances.put(instance.getKey(), instance);
        }
    }

    /**
     * @return list of tree paths as in PropBank instance.
     */
    private ArrayList<String> getTreePaths() {
        HashSet<String> set = new HashSet<>();

        for (String key : m_pbInstances.keySet()) {
            set.add(key.substring(0, key.indexOf(PBInstance.KEY_DELIM)));
        }

        ArrayList<String> list = new ArrayList<>(set);
        Collections.sort(list);

        return list;
    }

    /**
     * @return list of PropBank instances for the tree.
     */
    private ArrayList<PBInstance> getPBInstances(String treePath, int treeIndex) {
        ArrayList<PBInstance> list = new ArrayList<>();
        String prefix = treePath + PBInstance.KEY_DELIM + treeIndex + PBInstance.KEY_DELIM;

        for (String key : m_pbInstances.keySet()) {
            if (key.startsWith(prefix)) {
                list.add(m_pbInstances.get(key));
            }
        }

        return list;
    }

    public void merge() {
        TBReader reader;
        TBTree tree;
        int treeIndex;
        String mergeFile;
        PrintStream fout;
        DepTree dTree;
        DepNode dNode;

        TBHeadRules headrules = new TBHeadRules(s_headruleFile);
        MorphEnAnalyzer morph = (s_dictFile != null) ? new MorphEnAnalyzer(s_dictFile) : null;
        TBEnConvert convert = new TBEnConvert(headrules, morph, b_funcTag, b_ec, b_reverseVC);

        s_parseDir += File.separator;
        s_srlDir += File.separator;

        ArrayList<PBInstance> list;

        for (String treePath : getTreePaths()) {
            mergeFile = s_srlDir + treePath.substring(treePath.lastIndexOf(File.separator) + 1) + SRL_EXT;
            reader = new TBReader(s_parseDir + treePath + PARSE_EXT);
            fout = IOUtil.createPrintFileStream(mergeFile);

            System.out.println(mergeFile);

            for (treeIndex = 0; (tree = reader.nextTree()) != null; treeIndex++) {
                list = getPBInstances(treePath, treeIndex);
                if (!b_adjPred) {
                    removeAdjectivalPredicates(tree, list);
                }

                if (list.isEmpty()) {
                    dTree = convert.toDepTree(tree);

                    for (int i = 1; i < dTree.size(); i++) {
                        dNode = dTree.get(i);
                        dNode.srlInfo = new SRLInfo();
                    }
                } else {
                    tree.setPBLocs();
                    tree.setAntecedents();

                    for (PBInstance instance : list) {
                        mergeAux(instance, tree);
                    }

                    dTree = convert.toSRLTree(tree);
                }

                //	fout.println(";"+treePath+" "+treeIndex);
                if (dTree.size() > n_length) {
                    fout.println(dTree + "\n");
                }
            }

            fout.close();
        }
    }

    protected void removeAdjectivalPredicates(TBTree tree, ArrayList<PBInstance> instances) {
        ArrayList<PBInstance> remove = new ArrayList<>();
        TBNode node;

        for (PBInstance instance : instances) {
            node = tree.getNode(instance.predicateId, 1);
            if (node.isPos(TBEnLib.POS_NP) || (node.isPos(TBEnLib.POS_ADJP) && node.getParent().isPos(TBEnLib.POS_NP) && !node.isPrior(TBEnLib.POS_NP))) {
                remove.add(instance);
            }
        }

        instances.removeAll(remove);
    }

    private void mergeAux(PBInstance instance, TBTree tree) {
        TBNode pred = tree.getNode(instance.predicateId, 0);
        if (pred == null) {
            System.err.println("Wrong location of predicate: " + instance.treePath + " " + instance.treeIndex + " " + instance.predicateId);
            return;
        }

        ArrayList<PBArg> pbArgs = instance.getArgs();
        ArrayList<PBArg> delArgs = new ArrayList<>();

        for (PBArg pbArg : pbArgs) {
            if (!processEmtpyCategories(pbArg, tree, pred)) {
                System.err.println("Wrong location in " + pbArg.label + ": " + instance.toString());
            }
        }

        for (PBArg pbArg : pbArgs) {
            if (pbArg.isLabel("rel.*")) {
                if (processRels(pbArg, instance.predicateId)) {
                    delArgs.add(pbArg);
                }

                continue;
            }

            if (pbArg.isLabel("LINK.*")) {
                if (!processLink(pbArgs, pbArg, tree)) {
                    System.err.println("No-achor in " + pbArg.label + ": " + instance.toString());
                }

                delArgs.add(pbArg);
                continue;
            }
        }

        pbArgs.removeAll(delArgs);
        if (pbArgs.isEmpty()) {
            return;
        }

        if (!instance.rolesetId.endsWith(".DP")) {
            pred.rolesetId = instance.rolesetId;
        }

        for (PBArg pbArg : pbArgs) {
            processEmtpyCategoriesSub(pbArg, tree, pred);

            if (!addPBArgToTBTree(pbArg, tree)) {
                System.err.println("Wrong location in " + pbArg.label + ": " + instance.toString());
            }
        }
    }

    /**
     * Removes
     * <code>predId:0</code> from
     * <code>pbArg</code>.
     */
    private boolean processRels(PBArg pbArg, int predId) {
        for (PBLoc loc : pbArg.getLocs()) {
            if (loc.equals(predId, 0)) {
                pbArg.removeLoc(loc);
                break;
            }
        }

        return pbArg.getLocs().isEmpty();
    }

    /**
     * Merges LINK-argument with its anchor-argument.
     */
    private boolean processLink(ArrayList<PBArg> pbArgs, PBArg linkArg, TBTree tree) {
        PBLoc anchor = new PBLoc(null, -1, -1);
        TBNode node;

        if (linkArg.isLabel("LINK-SLC")) {
            // find antecedent
            for (PBLoc pbLoc : linkArg.getLocs()) {
                node = tree.getNode(pbLoc.terminalId, pbLoc.height);
                if (node == null) {
                    return false;
                }

                if (node.isPos("WH.*")) {
                    anchor = pbLoc;
                    break;
                }
            }

            // find antecedent in height 1
            if (anchor.terminalId == -1) {
                for (PBLoc pbLoc : linkArg.getLocs()) {
                    node = tree.getNode(pbLoc.terminalId, 1);

                    if (node != null && node.isPos("WH.*")) {
                        pbLoc.height = 1;
                        anchor = pbLoc;
                        break;
                    }
                }
            }
        } else if (anchor.terminalId == -1) // normalize empty categories
        {
            for (PBLoc pbLoc : linkArg.getLocs()) {
                node = tree.getNode(pbLoc.terminalId, 0);
                if (node == null) {
                    return false;
                }

                if (node.isEmptyCategory()) {
                    pbLoc.height = 0;
                }
            }
        }

        for (PBArg pbArg : pbArgs) {
            if (!pbArg.isLabel("LINK.*") && pbArg.overlapsLocs(linkArg)) {
                processLinkAux(linkArg, pbArg, anchor, tree);
                return true;
            }
        }

        for (PBArg pbArg : pbArgs) {
            if (!pbArg.isLabel("LINK.*") && pbArg.overlapsMildLocs(linkArg)) {
                processLinkAux(linkArg, pbArg, anchor, tree);
                return true;
            }
        }

        return false;
    }

    private void processLinkAux(PBArg linkArg, PBArg pbArg, PBLoc anchor, TBTree tree) {
        TBNode node, comp;
        pbArg.putLocs(linkArg.getLocs());

        // find antecedents of complementizer
        if (anchor.terminalId != -1) {
            node = tree.getNode(anchor.terminalId, anchor.height);
            comp = node.getComplementizer();

            if (!comp.hasAntecedent()) {
                Collections.sort(pbArg.getLocs());
                PBLoc anteLoc = pbArg.getLocs().get(0);

                comp.pbLoc.type = PBLib.PROP_OP_COMP;
                comp.antecedent = tree.getNode(anteLoc.terminalId, anteLoc.height);
            } else {
                pbArg.putLoc(comp.antecedent.pbLoc);
            }
        }
    }

    /**
     * Finds empty categories' antecedents.
     */
    private boolean processEmtpyCategories(PBArg pbArg, TBTree tree, TBNode pred) {
        ArrayList<PBLoc> addLocs = new ArrayList<>();
        //	ArrayList<PBLoc> delLocs = new ArrayList<PBLoc>();
        TBNode curr, node;

        for (PBLoc pbLoc : pbArg.getLocs()) {
            curr = tree.getNode(pbLoc.terminalId, pbLoc.height);
            if (curr == null) {
                return false;
            }

            if ((node = curr.getIncludedEmptyCategory("\\*ICH\\*.*")) != null && node.hasAntecedent()) {
                node.antecedent.pbLoc.type = PBLib.PROP_OP_SKIP;
                addLocs.add(node.antecedent.pbLoc);
            } else if ((node = curr.getIncludedEmptyCategory("\\*RNR\\*.*")) != null && node.hasAntecedent()) {
                node.antecedent.pbLoc.type = PBLib.PROP_OP_SKIP;
                addLocs.add(node.antecedent.pbLoc);
            } else if (curr.isEmptyCategoryRec()) {
                do {
                    pbLoc.height = 0;

                    if (curr.isPhrase()) {
                        curr = tree.getNode(pbLoc.terminalId, 0);
                    }

                    if (curr.hasAntecedent()) {
                        pbLoc = curr.antecedent.pbLoc;
                        addLocs.add(pbLoc);
                    } else {
                        break;
                    }

                    curr = tree.getNode(pbLoc.terminalId, pbLoc.height);
                } while (curr.isEmptyCategoryRec());

                /*
                 * if (curr.isForm("\\*T\\*.*")) { //	delLocs.add(pbLoc); if
                 * (curr.hasAntecedent())	addLocs.add(curr.antecedent.pbLoc); }
                 * else if (curr.isForm("\\*PRO\\*.*|\\*|\\*-\\d")) { if
                 * (curr.getParent().isFollowedBy("VP")) { if
                 * (curr.hasAntecedent()) addLocs.add(curr.antecedent.pbLoc); }
                 * //	else //	delLocs.add(pbLoc);
				}
                 */
            }
        }

        for (PBLoc pbLoc : addLocs) {
            pbArg.putLoc(pbLoc);
        }

        //	for (PBLoc pbLoc: delLocs)
        //		pbArg.removeLocs(pbLoc);

        trimEmptyCategories(pbArg, tree, pred);

        return true;
    }

    private void processEmtpyCategoriesSub(PBArg pbArg, TBTree tree, TBNode pred) {
        ArrayList<SRLHead> heads;
        TBNode curr, node, term;

        for (PBLoc pbLoc : pbArg.getLocs()) {
            curr = tree.getNode(pbLoc.terminalId, pbLoc.height);

            if (curr.isPos("S")) {
                for (TBNode child : curr.getChildren()) {
                    if (child.isEmptyCategoryRec() && (node = child.getIncludedEmptyCategory("\\*|\\*-\\d")) != null && node.hasAntecedent() && node.terminalId > pred.terminalId) {
                        node = node.antecedent;
                        term = tree.getTerminalNode(node.pbLoc.terminalId);
                        if (term.isPos("EX") || term.form.equalsIgnoreCase("it")) {
                            return;
                        }

                        if ((heads = node.getPBHeads()) != null) {
                            for (SRLHead head : heads) {
                                if (head.equals(pred.terminalId + 1)) {
                                    return;
                                }
                            }
                        }

                        node.pbLoc.type = "";
                        pbArg.putLoc(node.pbLoc);

                        return;
                    }
                }
            }
        }
    }

    void trimEmptyCategories(PBArg pbArg, TBTree tree, TBNode pred) {
        ArrayList<PBLoc> pbLocs = pbArg.getLocs();
        ArrayList<PBLoc> delLocs = new ArrayList<>();
        Collections.sort(pbLocs);

        TBNode ante = tree.getNode(pbLocs.get(0));

        if (ante.isEmptyCategoryRec()) {
            ante.antecedent = null;
            ante = null;
        } else {
            ante.pbLoc.type = PBLib.PROP_OP_ANTE;
        }

        boolean isFound = false;
        PBLoc pbLoc;
        TBNode curr;

        for (int i = pbLocs.size() - 1; i >= 0; i--) {
            pbLoc = pbLocs.get(i);
            curr = tree.getNode(pbLoc);
            if (!curr.isEmptyCategoryRec()) {
                continue;
            }

            if (curr.isPhrase()) {
                curr = tree.getNode(pbLoc.terminalId, 0);
            }

            if (curr.isForm("\\*PRO\\*.*|\\*|\\*-\\d")) {
                if (!isFound && pbLoc.terminalId < pred.terminalId) {
                    isFound = true;
                    curr.antecedent = ante;
                } else {
                    delLocs.add(pbLoc);
                }
            }
        }

        if (isFound) {
            pbLocs.removeAll(delLocs);
        }
    }

    protected boolean isCyclic(ArrayList<PBInstance> pbInstances, TBTree tree) {
        ArrayList<IntOpenHashSet> list = new ArrayList<>();
        TBNode node;

        for (PBInstance instance : pbInstances) {
            IntOpenHashSet set = new IntOpenHashSet();

            for (PBArg pbArg : instance.getArgs()) {
                for (PBLoc pbLoc : pbArg.getLocs()) {
                    node = tree.getNode(pbLoc);
                    set.addAll(node.getSubTermainlIDs());
                }
            }

            list.add(set);
        }

        int i, j, size = pbInstances.size();

        for (i = 0; i < size; i++) {
            PBInstance iInstance = pbInstances.get(i);
            IntOpenHashSet iSet = list.get(i);

            for (j = i + 1; j < size; j++) {
                PBInstance jInstance = pbInstances.get(j);
                IntOpenHashSet jSet = list.get(j);

                if (iSet.contains(jInstance.predicateId) && jSet.contains(iInstance.predicateId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean addPBArgToTBTree(PBArg pbArg, TBTree tree) {
        ArrayList<PBLoc> pbLocs = pbArg.getLocs();
        IntOpenHashSet addIDs = new IntOpenHashSet();
        IntOpenHashSet delIDs = new IntOpenHashSet();
        PBLoc rLoc = null;
        TBNode node, tmp;
        String label;

        if (pbArg.label.matches("rel.*")) {
            label = "C-V";
        } else {
            label = "A" + pbArg.label.substring(3);
        }

        // retrieve all terminal IDs
        for (PBLoc pbLoc : pbLocs) {
            if ((node = tree.getNode(pbLoc)) == null) {
                return false;
            }

            tmp = tree.getTerminalNode(pbLoc.terminalId);

            if (pbLoc.isType(PBLib.PROP_OP_SKIP)) {
                delIDs.addAll(node.getSubTermainlIDs());
            }

            if (node.isPos("WH.*")) {
                rLoc = pbLoc;
            } else if (node.isEmptyCategoryRec() && tmp.form.startsWith(TBEnLib.EC_PRO) && pbLoc.terminalId > pbArg.predicateId) {
                continue;
            } else {
                addIDs.addAll(node.getSubTermainlIDs());
            }
        }

        if (addIDs.isEmpty()) {
            return true;
        }

        // add terminal IDs
	/*
         * int[] ids = addIDs.toArray(); Arrays.sort(ids);
         *
         * TBNode pred = tree.getTerminalNode(pbArg.predicateId);
         * pred.addPBArg(new SRLArg(label, ids));
         */

        // add each argument
        addIDs.removeAll(delIDs);
        int terminalId, height;
        String prefix = "";
        int[] ids;

        while (!addIDs.isEmpty()) {
            height = 0;
            ids = addIDs.toArray();
            Arrays.sort(ids);
            terminalId = ids[0];

            while (true) {
                node = tree.getNode(terminalId, height + 1);

                if (node == null || !JSet.isSubset(addIDs, node.getSubTermainlIDs())) {
                    node = tree.getNode(terminalId, height);

                    if (node.isEmptyCategoryRec()) {
                        node.addPBHead(new SRLHead(pbArg.predicateId + 1, label));
                    } else if (!TBEnLib.isPunctuation(node.pos)) {
                        node.addPBHead(new SRLHead(pbArg.predicateId + 1, prefix + label));
                    }

                    addIDs.removeAll(node.getSubTermainlIDs());
                    if (!node.isEmptyCategoryRec() && !TBEnLib.isPunctuation(node.pos) && !label.startsWith("AM")) {
                        prefix = "C-";
                    }

                    break;
                }

                height++;
            }
        }

        prefix = "R-";

        if (rLoc != null) {
            node = tree.getNode(rLoc);
            //	ids  = node.getSubTermainlIDs().toArray();
            //	Arrays.sort(ids);
            //	pred.addPBArg(new SRLArg(prefix+label, ids));
            node.addPBHead(new SRLHead(pbArg.predicateId + 1, prefix + label));
        }

        return true;
    }

    static public void main(String[] args) {
        PropToDep ptd = new PropToDep(args);
    }
}