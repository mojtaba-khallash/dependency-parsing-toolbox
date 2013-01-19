package is2.data;

import is2.parser.Parser;
import is2.util.DB;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Dr. Bernd Bohnet, 17.01.2011
 *
 *
 */
public class PSTree {

    int wordCount = 0;
    public String entries[];
    public String lemmas[];
    public int head[];
    public String pos[];
    public int[] ok;
    public int non;
    public int terminalCount;
    public String[] morph;
    public int[] forms;
    public int[] phrases;
    public int[][] psfeats;
    public int[] ppos;

    /**
     * @param d
     */
    public PSTree(SentenceData09 d) {
        create(d.length() - 1, d.length() * 20);
        for (int i = 1; i < d.length(); i++) {
            entries[i - 1] = d.forms[i];
            pos[i - 1] = d.ppos[i];
        }
    }

    /**
     * Create an undefined phrase tree
     */
    public PSTree() {
    }

    /**
     * @param terminals
     * @param nonTerminals
     */
    public void create(int terminals, int nonTerminals) {
        entries = new String[terminals + nonTerminals];
        pos = new String[terminals + nonTerminals];
        head = new int[terminals + nonTerminals];
        lemmas = new String[terminals + nonTerminals];
        morph = new String[terminals + nonTerminals];
        non = terminals;
        wordCount = terminals;

        for (int i = terminals + 1; i < head.length; i++) {
            head[i] = -1;
        }
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();

        for (int i = 0; i < entries.length; i++) {
            if (head[i] == -1 && entries[i] == null) {
                break;
            }

            s.append(i).append("\t").append(pos[i]).append("\t").append(entries[i]).append("\t").append(head[i]).append((ok == null ? "" : ("\t" + (ok[i] == 1)))).append(" \n");

        }
        //		DB.println("entries "+entries.length);
        return s.toString();
    }

    /**
     * @return
     */
    public boolean containsNull() {
        for (int k = 0; k < wordCount - 1; k++) {
            if (entries[k] == null) {
                return true;
            }
        }
        return false;
    }

    public int equals(SentenceData09 s) {

        int j = 1; // starts with root
        for (int i = 0; i < terminalCount - 1; i++) {

            //	if (s.forms[j].equals("erschrekkend")) s.forms[j]="erschreckend"; 

            if (s.forms.length < j) {
                DB.println("" + s + " " + this.toString());
                return i;

            }

            if (!entries[i].equals(s.forms[j])) {
                //	Parser.out.println("ps "+entries[i]+" != ds "+s.forms[j]);
                // Rolls-Royce
                if (entries[i].startsWith(s.forms[j]) && s.forms.length > i + 2 && s.forms[j + 1].equals("-")) {
                    j += 2;
                    if (entries[i].contains(s.forms[j - 1]) && s.forms.length > i + 3 && s.forms[j + 1].equals("-")) {
                        j += 2; // &&
                        //   				Parser.out.println("s.forms[j] "+s.forms[j]+" s.forms[j-1] "+s.forms[j-1]+" "+entries[i]);
                        if (entries[i].contains(s.forms[j - 1]) && s.forms.length > i + 3 && s.forms[j + 1].equals("-")) {
                            j += 2; // &&
                            //	   				Parser.out.println("s.forms[j] "+s.forms[j]+" s.forms[j-1] "+s.forms[j-1]+" "+entries[i]);
                        }
                    }
                    //Interstate\/Johnson
                } else if (entries[i].startsWith(s.forms[j]) && s.forms.length > i + 2 && s.forms[j + 1].equals("/")) {
                    j += 2;
                    if (entries[i].contains(s.forms[j - 1]) && s.forms.length > i + 3 && s.forms[j + 1].equals("/")) {
                        j += 2; // &&
                        //  				Parser.out.println("s.forms[j] "+s.forms[j]+" s.forms[j-1] "+s.forms[j-1]+" "+entries[i]);
                    }

                    // U.S.-Japan -> U  .  S . - Japan
                } else if (entries[i].startsWith(s.forms[j]) && s.forms.length > i + 2 && s.forms[j + 1].equals(".")) {
                    j += 2;
                    if (entries[i].contains(s.forms[j - 1]) && s.forms.length > i + 3 && s.forms[j + 1].equals(".")) {
                        j += 2; // &&
                        //				Parser.out.println("s.forms[j] "+s.forms[j]+" s.forms[j-1] "+s.forms[j-1]+" "+entries[i]);
                    }
                } else if (entries[i].startsWith(s.forms[j]) && s.forms.length > i + 1 && s.forms[j + 1].equals("'S")) {
                    j += 1;

                } else {

                    // chech those !!!
                    //		Parser.out.print("entry "+entries[i]+" form "+s.forms[j]+" ");
                    return j;
                }
            }
            j++;
        }

        // without root
        return s.length();
        //return j;

    }

    /**
     * @param dn
     * @return
     */
    public int getPS(int dn) {

        return this.head[dn - 1];
    }

    /**
     * @param dn
     * @param n
     * @param commonHead the common head in the phrase structure
     * @return
     */
    public String getChain(int dn, int n, int commonHead) {

        int pdn = dn - 1, pdh = n - 1;
        //	int phraseHead =head[pdh];

        //		Parser.out.println("phrase head "+phraseHead+" common head "+commonHead);

        int[] ch = new int[20];
        int head = this.head[pdn];
        int i = 0;
        ch[i++] = head;
        while (commonHead != head && head != 0) {

            head = this.head[head];
            ch[i++] = head;
        }
        StringBuilder chain = new StringBuilder();

        for (int k = 0; k < i; k++) {
            chain.append(entries[ch[k]]).append(" ");
        }
        return chain.toString();
    }

    /**
     * @param dn
     * @param n
     * @return
     */
    public int getCommonHead(int d, int dh) {
        int pdh = this.getPS(dh), pd = this.getPS(d);


        ArrayList<Integer> path2root = getPath2Root(pdh);

        //Parser.out.println("path 2 root "+path2root+" pdh "+pdh);

        for (int n : path2root) {
            int candidateHead = pd;
            while (candidateHead != 0 && candidateHead != -1) {
                if (n == candidateHead) {
                    return n;
                }
                candidateHead = this.head[candidateHead];
            }
        }
        return -1;
    }

    /**
     * @param pdh
     */
    private ArrayList<Integer> getPath2Root(int pdh) {
        ArrayList<Integer> path = new ArrayList<>();


        // restrict the number in case its a cycle which should never be
        for (int k = 0; k < 100; k++) {
            if (pdh == -1) {
                break;
            }
            path.add(pdh);
            pdh = this.head[pdh];
            if (pdh == 0) {
                break;
            }
        }
        return path;
    }

    /**
     * Get operations to create root see operation in method getOperation
     *
     * @param pr
     */
    public String getOperationRoot(int pr) {

        StringBuilder o = new StringBuilder();
        int h = pr;
        int[] path = new int[10];
        //	Parser.out.println(" start node "+pr);
        int k = 0;
        for (; k < 10; k++) {
            h = head[h];
            if (h == -1) {
                break;
            }
            path[k] = h;
            if (h == 0) {
                break;
            }
        }
        k -= 2;

        boolean first = true;
        for (; k >= 0; k--) {

            // create phrase
            if (first) {
                o.append("c:").append(entries[path[k]]);
                first = false;
            } // insert and create phrase
            else {
                o.append(":ci:").append(entries[path[k]]);
            }
        }


        // insert dependent node
        //if (o.length()>0) 
        o.append(":in:d");
        //else o.append("in:d");  // insert root into nothing
        return o.toString();
    }

    /**
     * Create operation to include dependency edges in phrase structure
     * Operations: c - create ; i - insert ; in - insert (dependent) node ; up:X
     * go the (phrase) X up ci create and insert ...
     *
     * @param dn
     * @param n
     * @param commonHead
     * @return
     */
    public String getOperation(int dn, int n, int commonHead) {

        StringBuilder o = new StringBuilder();

        // from n move up to common head, if needed
        int ph = n - 1, pd = dn - 1;

        int[] path = new int[20];
        int i = 0;

        int h = ph;

        boolean nth = false;
        for (int k = 0; k < 10; k++) {
            h = head[h];
            path[k] = h;
            if (nth) {
                o.append(':');
            }
            o.append("up:").append(entries[h]);
            nth = true;
            if (h == commonHead) {
                break;
            }
        }

        // from common head to the node
        int k = 0;
        h = pd;
        for (; k < 10; k++) {
            h = head[h];
            path[k] = h;
            if (h == commonHead) {
                break;
            }

        }
        k -= 1;

        //		boolean first=true;
        for (; k >= 0; k--) {

            // create phrase
            if (!nth) {
                o.append("ci:").append(entries[path[k]]);
                nth = true;
            } // insert and create phrase
            else {
                o.append(":ci:").append(entries[path[k]]);
            }
        }


        // insert dependent node
        o.append(":in:d");



        return o.toString();
    }

    /**
     * @param ph node in the phrase structure corresponding to the head in the
     * dependency structure
     * @param pt node in the prhase structure corresponding to the dependent in
     * the ds.
     * @param check
     * @return rules was applicable
     */
    public boolean exec(String r, int ph, int pt, boolean check) {

        String o[] = r.split(":");

        int last = -1, headP = -1;

        // create root node

        //	Parser.out.println("operation "+r+" "+ph+" "+pt);
        boolean done = true;
        for (int i = 0; i < o.length; i++) {

            if (o[i].equals("c")) {
                if (check) {
                    return true;
                }

                if (ph < 0) {
                    last = non++;
                }

                entries[non] = o[++i]; // create
                head[pt] = non;
                head[non] = last; // insert into root
                last = non++;
            } else if (o[i].equals("ci")) {
                if (check) {
                    return true;
                }
                entries[non] = o[++i]; // create
                head[non] = last; // insert
                last = non;
                non++;
            } else if (o[i].equals("in") && o[i + 1].equals("d")) {
                if (check) {
                    return true;
                }
                head[pt] = last; // insert
                i++; // move forward because of 'd'
            } else if (o[i].equals("up")) {

                if (ph == -1) {
                    //			Parser.out.println("ph is -1 please check this "+ph+" there is a bug ");
                    return false;
                }

                if (headP == -1) {
                    headP = head[ph];
                } else {
                    headP = head[headP];
                }

                try {
                    if (headP == -1 || entries[headP] == null || !entries[headP].equals(o[i + 1])) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Parser.out.println("" + entries[headP] + " o[i+1] " + o[i + 1] + " " + headP + " " + this.terminalCount);
                    //		Parser.out.println(""+	this.toString());
                    System.exit(0);
                }

                i++;
                last = headP;
            } else {
                done = false;
            }
        }

        return done;
    }

    /**
     * More tolerant mapping
     *
     * @param ph node in the phrase structure corresponding to the head in the
     * dependency structure
     * @param pt node in the prhase structure corresponding to the dependent in
     * the ds.
     * @param check
     * @return rules was applicable
     */
    public boolean execT(String r, int ph, int pt, boolean check) {

        String o[] = r.split(":");

        int last = -1, headP = -1;

        int up = 0;

        boolean done = true;
        for (int i = 0; i < o.length; i++) {

            if (o[i].equals("c")) {
                if (check) {
                    return true;
                }


                // create root node
                if (ph < 0) {
                    last = non++;
                }

                entries[non] = o[++i]; // create
                head[pt] = non;
                head[non] = last; // insert into root
                last = non++;
            } else if (o[i].equals("ci")) {

                if (check) {
                    return true;
                }
                entries[non] = o[++i]; // create
                head[non] = last; // insert
                last = non;
                non++;
            } else if (o[i].equals("in") && o[i + 1].equals("d")) {
                if (check) {
                    return true;
                }

                //			DB.println("hallo");

                if (last != -1) {
                    head[pt] = last; // insert
                }

                // i am not sure if this does much good?

                //	if (last ==-1)				

                //	done=true;



                i++; // move forward because of 'd'

            } else if (o[i].equals("up")) {
                up++;
                if (ph == -1) {
                    return false;
                }

                if (headP == -1) {
                    headP = head[ph];
                } else {
                    headP = head[headP];
                }

                try {

                    // tolerant mapping
                    if (headP == -1 || entries[headP] == null
                            || ((!entries[headP].equals(o[i + 1])) && up > 1)) {
                        return false; //>1
                    }// && entries[headP].charAt(0)!=o[i+1].charAt(0)
                } catch (Exception e) {
                    e.printStackTrace();
                    Parser.out.println("" + entries[headP] + " o[i+1] " + o[i + 1] + " " + headP + " " + this.terminalCount);
                }

                i++;
                last = headP;
            } else {
                done = false;
            }

        }


        return done;
    }
    public final static boolean INSERT_NEWLINE = true;

    /**
     * Convert to bracket format
     *
     * @param newLine
     * @return
     */
    public String toPennBracket(boolean newLine) {


        StringBuilder b = new StringBuilder();
        ArrayList<Integer> current = null;// = new ArrayList<Integer>();  
        int open = 0;
        for (int i = 0; i < terminalCount; i++) {
            ArrayList<Integer> path = getPathToRoot(i);

            ArrayList<Integer> diff = getDiffPath(path, current);

            boolean spaces = false;

            ArrayList<Integer> common = this.getDiffCommon(path, current);

            if (current != null && (current.size() > common.size())) {

                // close brackets 
                for (int bc = 0; bc < current.size() - common.size(); bc++) {
                    b.append(")");
                    open--;
                }
                if (diff.isEmpty() && newLine) {
                    b.append("\n");
                }
                spaces = true;
            }

            if (i != 0 && diff.size() > 0 && newLine) {
                b.append("\n").append(createSpaces(open));
            }

            for (int k = diff.size() - 1; k >= 0; k--) {
                open++;
                b.append("(").append((entries[path.get(k)] == null ? " " : entries[path.get(k)]));
                if (k != 0 && path.size() - 1 != k && newLine) {
                    b.append("\n").append(createSpaces(open));
                }
                spaces = false;
            }
            if (spaces) {
                b.append(createSpaces(open));
            } else {
                b.append(" ");
            }

            String term = entries[i];
            if (term.equals("(")) {
                term = "-LRB-";
            }
            if (term.equals(")")) {
                term = "-RRB-";
            }
            if (term.equals("{")) {
                term = "-LCB-";
            }
            if (term.equals("}")) {
                term = "-RCB-";
            }

            String ps = pos[i];
            if (ps.equals("(")) {
                ps = "-LRB-";
            }
            if (ps.equals("$(")) {
                ps = "-LRB-";
            }

            if (ps.equals(")")) {
                ps = "-RRB-";
            }
            if (ps.equals("{")) {
                ps = "-LCB-";
            }
            if (ps.equals("}")) {
                ps = "-RCB-";
            }


            b.append("(").append(ps).append(" ").append(term).append(')');
            current = path;
            //			break;
        }
        for (; open > 0; open--) {
            b.append(")");
        }
        //	b.append("\n");

        return b.toString();
    }
    static int cnt = 0;

    /**
     * @param path
     * @param current
     * @return
     */
    private ArrayList<Integer> getDiffPath(ArrayList<Integer> path, ArrayList<Integer> current) {
        if (current == null) {
            return path;
        }

        ArrayList<Integer> common = new ArrayList<>();

        int pindex = path.size() - 1;
        int cindex = current.size() - 1;

        while (cindex >= 0 && pindex >= 0) {

            if (path.get(pindex) == current.get(cindex)) {
                cindex--;
                pindex--;
            } else {
                break;
            }
        }

        for (int k = 0; k <= pindex; k++) {
            common.add(path.get(k));
        }

        return common;
    }

    private ArrayList<Integer> getDiffCommon(ArrayList<Integer> path, ArrayList<Integer> current) {
        if (current == null) {
            return path;
        }

        ArrayList<Integer> common = new ArrayList<>();

        int pindex = path.size() - 1;
        int cindex = current.size() - 1;

        while (cindex >= 0 && pindex >= 0) {

            if (path.get(pindex) == current.get(cindex)) {
                common.add(path.get(pindex));
                cindex--;
                pindex--;
            } else {
                break;
            }
        }

        Collections.reverse(common);
        //	Parser.out.println("common "+pindex+" "+common);

        return common;
    }

    /**
     * @param i
     * @return
     */
    private StringBuffer createSpaces(int i) {
        StringBuffer s = new StringBuffer();
        for (int k = 0; k < i; k++) {
            s.append("  ");
        }
        return s;
    }

    /**
     * @param i
     * @return
     */
    private ArrayList<Integer> getPathToRoot(int i) {

        ArrayList<Integer> path = new ArrayList<>();

        int h = i;
        while (true) {
            h = this.head[h];
            if (h < this.terminalCount || path.contains(h)) {
                break;
            }
            path.add(h);
        }

        //	Collections.reverse(list)

        return path;
    }

    public String conll09() {

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < this.terminalCount; i++) {
            if (head[i] == -1 && entries[i] == null) {
                break;
            }

            s.append((i + 1)).append('\t').append(entries[i]).append("\t_\t_\t").append(pos[i]).append("\t_\t_\t_\t_\t_\t_\t_\t_\n");
        }

        return s.toString();
    }

    /**
     * @param phead
     * @return
     */
    public int[] getChilds(int head) {

        int count = 0;
        for (int i = 0; i < this.entries.length; i++) {
            if (this.head[i] == head) {
                count++;
            }
        }

        int[] clds = new int[count];
        count = 0;
        for (int i = 0; i < this.entries.length; i++) {
            if (this.head[i] == head) {
                clds[count++] = i;
            }
        }

        return clds;
    }
}