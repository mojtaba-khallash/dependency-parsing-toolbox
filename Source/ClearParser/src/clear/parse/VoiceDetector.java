package clear.parse;

import clear.treebank.TBEnLib;
import clear.treebank.TBNode;
import java.util.ArrayList;

public class VoiceDetector {

    static public int getPassive(TBNode pred) {
        if (!pred.pos.matches("VBN")) {
            return 0;
        }

        // Ordinary passive:
        // 1. Parent is VP, closest verb sibling of any VP ancestor is passive auxiliary (be verb)
        {
            TBNode parent = pred.getParent();

            while (parent != null && parent.isPos("VP")) {
                if (findAux(parent)) {
                    return 1;
                }
                parent = parent.getParent();
            }
        }

        // 2. ancestor path is (ADVP->)*VP, closest verb sibling of the VP is passive auxiliary (be verb)
        {
            TBNode curr = pred;

            while (curr.getParent() != null && curr.getParent().isPos("ADJP|UCP")) {
                curr = curr.getParent();
            }

            if (curr != pred && curr.getParent().isPos("VP")) {
                if (findAux(curr)) {
                    return 2;
                }
            }
        }

        //Reduced Passive:
        //1. Parent and nested ancestors are VP,
        //   none of VP ancestor's preceding siblings is verb
        //   parent of oldest VP ancestor is NP
        outer:
        {
            TBNode curr = pred;

            while (curr.getParent() != null && curr.getParent().isPos("VP")) {
                curr = curr.getParent();
                ArrayList<TBNode> siblings = curr.getParent().getChildren();

                for (int i = curr.childId - 1; i >= 0; --i) {
                    if (siblings.get(i).isPos("VB.*|AUX|MD")) {
                        break outer;
                    }
                }
            }

            if (curr != pred && curr.getParent() != null && curr.getParent().isPos("NP")) {
                return 3;
            }
        }

        //2. Parent is PP
        {
            TBNode parent = pred.getParent();

            if (parent != null && parent.isPos("PP")) {
                return 4;
            }
        }

        //3. Parent is VP, grandparent is clause, and great grandparent is clause, NP, VP or PP
        {
            TBNode parent = pred.getParent();
            TBNode grandParent = (parent != null) ? parent.getParent() : null;
            TBNode greatParent = (grandParent != null) ? grandParent.getParent() : null;

            if (parent != null && parent.isPos("VP")
                    && grandParent != null && grandParent.isPos("S.*")
                    && greatParent != null && greatParent.isPos("S.*|NP|VP|PP")) {
                return 5;
            }
        }

        //4. ancestors are ADVP, no preceding siblings of oldest ancestor is DET, no following siblings is a noun or NP
        outer:
        {
            TBNode curr = pred;

            while (curr.getParent() != null && curr.getParent().isPos("ADJP")) {
                curr = curr.getParent();
            }

            if (curr != pred) {
                ArrayList<TBNode> siblings = curr.getParent().getChildren();

                for (int i = curr.childId - 1; i >= 0; i--) {
                    if (siblings.get(i).isPos("DT")) {
                        break outer;
                    }
                }

                for (int i = curr.childId + 1; i < siblings.size(); ++i) {
                    if (siblings.get(i).isPos("N.*")) {
                        break outer;
                    }
                }

                return 6;
            }
        }

        return 0;
    }

    static private boolean findAux(TBNode curr) {
        ArrayList<TBNode> siblings = curr.getParent().getChildren();
        TBNode node;

        for (int i = curr.childId - 1; i >= 0; --i) {
            node = siblings.get(i);

            // find auxiliary verb if verb, if not, stop
            if (node.isPos("VB.*|AUX")) {
                String form = node.form.toLowerCase();
                return TBEnLib.isBe(form) || TBEnLib.isGet(form) || TBEnLib.isBecome(form);
            }
        }

        return false;
    }
}