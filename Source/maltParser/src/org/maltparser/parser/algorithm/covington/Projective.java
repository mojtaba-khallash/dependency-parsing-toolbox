package org.maltparser.parser.algorithm.covington;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.History;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.transition.TransitionTable;

/**
 * @author Johan Hall
 *
 */
public class Projective extends TransitionSystem {

    protected static final int SHIFT = 1;
    protected static final int NOARC = 2;
    protected static final int RIGHTARC = 3;
    protected static final int LEFTARC = 4;

    public Projective() throws MaltChainedException {
        super();
    }

    public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        CovingtonConfig covingtonConfig = (CovingtonConfig) config;
        currentAction.getAction(actionContainers);

        Edge e;
        switch (transActionContainer.getActionCode()) {
            case LEFTARC:
                e = covingtonConfig.getDependencyGraph().addDependencyEdge(covingtonConfig.getRightTarget().getIndex(), covingtonConfig.getLeftTarget().getIndex());
                addEdgeLabels(e);
//			config.setArcParent(covingtonConfig.getRightTarget());
//			config.setArcChild(covingtonConfig.getLeftTarget());
                break;
            case RIGHTARC:
                e = covingtonConfig.getDependencyGraph().addDependencyEdge(covingtonConfig.getLeftTarget().getIndex(), covingtonConfig.getRightTarget().getIndex());
                addEdgeLabels(e);
//			config.setArcParent(covingtonConfig.getLeftTarget());
//			config.setArcChild(covingtonConfig.getRightTarget());
                break;
            default:
//			config.setArcParent(null);
//			config.setArcChild(null);
                break;
        }
        update(covingtonConfig, transActionContainer.getActionCode());
    }

    private void update(CovingtonConfig covingtonConfig, int trans) throws MaltChainedException {
        if (trans == SHIFT || trans == RIGHTARC) {
            covingtonConfig.setRight(covingtonConfig.getRight() + 1);
            covingtonConfig.setLeft(covingtonConfig.getRight() - 1);
        } else {
            int leftstop = covingtonConfig.getLeftstop();
            int left = covingtonConfig.getLeft();
            if (trans == NOARC) {
                DependencyStructure dg = covingtonConfig.getDependencyStructure();
                DependencyNode leftNode = covingtonConfig.getInput().get(covingtonConfig.getLeft());
                if (dg.getTokenNode(leftNode.getIndex()) != null && dg.getTokenNode(leftNode.getIndex()).hasHead()) {
                    left = dg.getTokenNode(leftNode.getIndex()).getHead().getIndex();
                } else {
                    left = leftstop - 1;
                }
            } else {
                DependencyNode rightNode = covingtonConfig.getRightTarget();
                left--;
                DependencyNode leftNode;
                while (left >= leftstop) {
                    leftNode = covingtonConfig.getInput().get(left);
                    if (rightNode.findComponent().getIndex() != leftNode.findComponent().getIndex()) {
                        break;
                    }
                    left--;
                }
            }

            if (left < leftstop) {
                covingtonConfig.setRight(covingtonConfig.getRight() + 1);
                covingtonConfig.setLeft(covingtonConfig.getRight() - 1);
            } else {
                covingtonConfig.setLeft(left);
            }
        }
    }

    public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
        final CovingtonConfig covingtonConfig = (CovingtonConfig) config;
        if (!covingtonConfig.isAllowRoot() && covingtonConfig.getLeftTarget().isRoot()) {
            return updateActionContainers(history, Projective.NOARC, null);
        }
        return null;
    }

    protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
        ttable.addTransition(SHIFT, "SH", false, null);
        ttable.addTransition(NOARC, "NA", false, null);
        ttable.addTransition(RIGHTARC, "RA", true, null);
        ttable.addTransition(LEFTARC, "LA", true, null);
    }

    protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
        GuideUserAction currentAction = new ComplexDecisionAction((History) history);

        transActionContainer.setAction(SHIFT);
        transActionContainer.setAction(NOARC);
        for (int i = 0; i < arcLabelActionContainers.length; i++) {
            arcLabelActionContainers[i].setAction(-1);
        }
        currentAction.addAction(actionContainers);
    }

    public String getName() {
        return "covproj";
    }

    public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        CovingtonConfig covingtonConfig = (CovingtonConfig) config;
        DependencyNode leftTarget = covingtonConfig.getLeftTarget();
        DependencyNode rightTarget = covingtonConfig.getRightTarget();
        DependencyStructure dg = covingtonConfig.getDependencyGraph();
        currentAction.getAction(actionContainers);
        int trans = transActionContainer.getActionCode();

        if (trans == SHIFT && covingtonConfig.isAllowShift() == false) {
            return false;
        }
        if ((trans == LEFTARC || trans == RIGHTARC) && !isActionContainersLabeled()) {
            return false;
        }
        if (trans == LEFTARC && leftTarget.isRoot()) {
            return false;
        }
        if (trans == LEFTARC && dg.hasLabeledDependency(leftTarget.getIndex())) {
            return false;
        }
        if (trans == RIGHTARC && dg.hasLabeledDependency(rightTarget.getIndex())) {
            return false;
        }
        return true;
    }

    public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
        return updateActionContainers(history, Projective.NOARC, null);
    }
}