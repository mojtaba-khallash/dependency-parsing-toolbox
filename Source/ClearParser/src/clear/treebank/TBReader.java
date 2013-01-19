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

import clear.util.JFileTokenizer;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Treebank reader
 *
 * @author Jinho D. Choi <b>Last update:</b> 8/30/2010
 */
public class TBReader {

    /**
     * Left round bracket "("
     */
    static final public String LRB = "(";
    /**
     * Right round bracket "("
     */
    static final public String RRB = ")";
    /**
     * FileTokenizer to read the Treebank file
     */
    private JFileTokenizer f_tree;

    /**
     * Initializes the Treebank reader.
     *
     * @param treeFile name of the Treebank file
     */
    public TBReader(String treeFile) {
        String delim = LRB + RRB + JFileTokenizer.WHITE;
        f_tree = new JFileTokenizer(treeFile, delim, true);
    }

    /**
     * Returns the next tree in the Treebank. If there is none, returns null.
     */
    public TBTree nextTree() {
        String str;

        do // find the first '('
        {
            str = nextToken();
            if (str == null) {
                f_tree.close();
                return null;
            }
        } while (!str.equals(LRB));

        int numBracket = 1;
        int terminalIndex = 0;
        int tokenIndex = 0;
        TBTree tree = new TBTree();
        TBNode head = new TBNode(null, TBEnLib.POS_TOP);	// dummy-head
        TBNode curr = head;								// pointer to the current node

        while (true) {
            if ((str = nextToken()) == null) {
                errorMsg("more token needed");
            }

            if (numBracket == 1 && str.equals(TBEnLib.POS_TOP)) {
                TBNode node = new TBNode(curr, str);	// add a child to 'curr'
                curr.addChild(node);
                curr = node;							// move to child
            } else if (str.equals(LRB)) {
                numBracket++;
                if ((str = nextToken()) == null) // str = pos-tag
                {
                    errorMsg("POS-tag is missing");
                }

                TBNode node = new TBNode(curr, str);	// add a child to 'curr'
                curr.addChild(node);
                curr = node;							// move to child
            } else if (str.equals(RRB)) {
                numBracket--;
                curr = curr.getParent();				// move to parent
                if (numBracket == 0) {
                    break;
                }
            } else {
                curr.setForm(str);						// str = word
                curr.terminalId = curr.headId = terminalIndex++;

                if (!curr.isEmptyCategory()) {
                    curr.tokenId = tokenIndex++;
                }

                tree.addTerminalNode(curr);					// add 'curr' as a leaf
            }
        }

        TBNode top = head.getChildren().get(0);
        //	normalizeEC(top);

        if (top.isPos(TBEnLib.POS_TOP)) {
            top.setParent(null);
            tree.setRootNode(top);
        } else {
            tree.setRootNode(head);
        }

        return tree;
    }

    public void normalizeEC(TBNode root) {
        IntObjectOpenHashMap<ArrayList<TBNode>> map = new IntObjectOpenHashMap<>();
        retrieveCoIndexMap(root, map);
        if (map.isEmpty()) {
            return;
        }

        int[] keys = map.keys().toArray();
        ArrayList<TBNode> list;
        Arrays.sort(keys);

        int coIndex = 1, i;
        TBNode curr, ec;
        boolean isFirst;

        for (int key : keys) {
            list = map.get(key);
            isFirst = true;

            for (i = list.size() - 1; i >= 0; i--) {
                curr = list.get(i);

                if (curr.isEmptyCategoryRec() && i > 0) {
                    ec = curr.getSubTerminalNodes().get(0);

                    if (isFirst || ec.isForm("\\*ICH\\*|\\*RNR\\*")) {
                        curr.coIndex = -1;
                        ec.form += "-" + coIndex;
                        isFirst = false;
                    } else {
                        curr.coIndex = coIndex++;
                        ec.form += "-" + coIndex;
                    }
                } else {
                    curr.coIndex = coIndex;
                }
            }

            coIndex++;
        }
    }

    private void retrieveCoIndexMap(TBNode node, IntObjectOpenHashMap<ArrayList<TBNode>> map) {
        if (node.isPhrase()) {
            if (node.coIndex != -1) {
                int key = node.coIndex;
                ArrayList<TBNode> list;

                if (map.containsKey(key)) {
                    list = map.get(key);
                } else {
                    list = new ArrayList<>();
                    map.put(key, list);
                }

                list.add(node);
            }

            for (TBNode child : node.getChildren()) {
                retrieveCoIndexMap(child, map);
            }
        }
    }

    /**
     * Skips all white-spaces and returns the next token. If there is no such
     * token, returns null.
     */
    private String nextToken() {
        while (f_tree.hasMoreTokens()) {
            String str = f_tree.nextToken();

            if (JFileTokenizer.WHITE.indexOf(str) == -1) {
                return str;
            }
        }

        return null;
    }

    /**
     * Prints an error-message and exits.
     */
    private void errorMsg(String msg) {
        System.err.println("error: " + msg + " (line: " + f_tree.getLineNumber() + ")");
        System.exit(1);
    }
}