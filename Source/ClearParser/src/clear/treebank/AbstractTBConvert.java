package clear.treebank;

import clear.dep.DepTree;

abstract public class AbstractTBConvert {

    protected TBTree p_tree;
    protected DepTree d_tree;
    protected TBHeadRules g_headrules;

    abstract public DepTree toDepTree(TBTree pTree);
}