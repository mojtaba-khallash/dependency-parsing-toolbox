package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.dep.srl.SRLInfo;
import clear.reader.SRLReader;
import clear.util.IOUtil;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MergeDepSRL {

    final Pattern P_MOD_1 = Pattern.compile("^can$|^could$|^may$|^might$|^must$|^shall$|^should$|^will$|^'ll$|^would$");
    final Pattern P_MOD_2 = Pattern.compile("^'d$|^'ma$|^mighta$|^dare$|^need$");
    final Pattern P_MOD_3 = Pattern.compile("^c$|^ca$|^mus$|^sha$|^wo$|^ai$");
    final int N_TOTAL = 0;
    final int N_SAME_HEAD = 1;
    int[] n_fix;
    int[] n_count;

    public MergeDepSRL(String inputFile, String outputFile) {
        SRLReader reader = new SRLReader(inputFile, true);
        try (PrintStream fout = IOUtil.createPrintFileStream(outputFile)) {
            DepTree tree;
            DepNode curr;
            SRLInfo info;
            int i;

            n_fix = new int[2];
            n_count = new int[2];

            while ((tree = reader.nextTree()) != null) {
                tree.setHasHeads();
                relocatePPs(tree);
                relabelModals(tree);
                checkTOs(tree);

                /*
                 * for (i=1; i<tree.size(); i++) { curr = tree.get(i); info =
                 * curr.srlInfo;
                 *
                 * if (info.heads.isEmpty()) continue; else if (info.heads.size() ==
                 * 1) mergeSingle(tree, curr, info.heads.get(0));
                 *
                 * n_count[N_TOTAL]++;
                            }
                 */

                //	fout.println(tree+"\n");
            }
        }
    }

    private void relocatePPs(DepTree tree) {
        DepNode curr, head, prev;
        SRLInfo sInfo;
        ArrayList<SRLHead> remove;

        for (int i = 1; i < tree.size(); i++) {
            curr = tree.get(i);
            sInfo = curr.srlInfo;
            remove = new ArrayList<>();

            for (SRLHead sHead : sInfo.heads) {
                prev = null;
                head = tree.get(curr.headId);

                while (true) {
                    if (head.srlInfo != null && !head.srlInfo.isArgOf(sHead.headId) && tree.isAncestor(sHead.headId, head.id)) {
                        if (head.isPos("IN") && head.headId == sHead.headId) {
                            prev = head;
                            break;
                        }

                        head = tree.get(head.headId);
                    } else {
                        break;
                    }
                }


                if (prev != null) {
                    prev.addSRLHead(sHead.headId, sHead.label);
                    remove.add(sHead);
                    n_fix[0]++;
                }
            }

            sInfo.heads.removeAll(remove);
        }
    }

    private void relabelModals(DepTree tree) {
        DepNode curr;
        SRLInfo sInfo;
        int modal;
        ArrayList<SRLHead> remove;

        for (int i = 1; i < tree.size(); i++) {
            curr = tree.get(i);
            sInfo = curr.srlInfo;
            modal = getModalType(tree, curr);
            remove = new ArrayList<>();

            for (SRLHead sHead : sInfo.heads) {
                if (sHead.equals("AM-MOD") && modal == 0) {
                    remove.add(sHead);
                }
            }

            sInfo.heads.removeAll(remove);

            /*
             * if (modal > 0) { ArrayList<DepNode> list = new
             * ArrayList<DepNode>(); getModalHeads(tree, curr, list);
             *
             * for (DepNode node : list) { SRLHead tmp;
             *
             * if (node.isPredicate()) { if ((tmp = sInfo.getHead(node.id)) !=
             * null) tmp.label = "AM-MOD"; else sInfo.heads.add(new
             * SRLHead(node.id, "AM-MOD")); } }
			}
             */
        }
    }

    public void checkTOs(DepTree tree) {
        DepNode curr, head, node;
        SRLHead sHead;
        int i, j, size = tree.size();

        for (i = 1; i < size; i++) {
            curr = tree.get(i);
            head = tree.get(curr.headId);

            if (curr.isPredicate() && head.isPos("TO")) {
                for (j = 1; j < i - 1; j++) {
                    node = tree.get(j);
                    sHead = node.srlInfo.getHead(curr.id);

                    if (sHead != null && node.headId != head.id && !tree.isAncestor(node.id, head.id) && !sHead.labelMatches("A.") && !sHead.labelMatches("R-A.")) {
                        System.out.println(curr.id + " " + node.id + " " + tree);
                        try {
                            System.in.read();
                        } catch (IOException e) {
                        }
                    }
                }
            }

        }
    }

    public void getModalHeads(DepTree tree, DepNode curr, ArrayList<DepNode> list) {
        ArrayList<DepNode> tmp = tree.getVCList(curr.id);
        if (tmp.isEmpty()) {
            return;
        }
        DepNode vc;

        for (DepNode node : tmp) {
            if (node.isPredicate()) {
                list.add(node);
            }
            vc = tree.getRightNearestCoord(node.id, "VB.*");

            if (vc != null) {
                getModalHeads(tree, vc, list);
            }
        }
    }

    public int getModalType(DepTree tree, DepNode curr) {
        String cForm = curr.form.toLowerCase();

        if (P_MOD_1.matcher(cForm).find()) {
            return 1;
        } else if (P_MOD_2.matcher(cForm).find()) {
            DepNode vc = tree.getFirstVC(curr.id);
            if (vc != null && vc.isPos("VB")) {
                return 2;
            }
        } else if (P_MOD_3.matcher(cForm).find()) {
            if (curr.id + 1 < tree.size() && tree.get(curr.id + 1).form.toLowerCase().equals("n't")) {
                return 3;
            }
        }

        return 0;
    }

    private void mergeSingle(DepTree tree, DepNode curr, SRLHead sHead) {
        DepNode head = tree.get(curr.headId);

        if (sHead.headId == head.id) {
            curr.deprel = sHead.label;
            n_count[N_SAME_HEAD]++;
        } else if (sHead.equals("AM-MOD")) {
        } else if (curr.deprel.matches("CONJ")) {
            System.out.println(curr.id + " " + tree);
            try {
                System.in.read();
            } catch (IOException e) {
            }
        }
        /*
         * else if (tree.isAncestor(sHead.headId, head.id)) { curr.headId =
         * sHead.headId; curr.deprel = sHead.label;
		}
         */
    }

    static public void main(String[] args) {
        MergeDepSRL mergeDepSRL = new MergeDepSRL(args[0], args[1]);
    }
}