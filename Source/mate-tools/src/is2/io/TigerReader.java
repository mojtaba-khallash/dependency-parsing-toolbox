package is2.io;

import is2.data.PSTree;
import is2.parser.Parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * @author Dr. Bernd Bohnet, 17.01.2011
 *
 * Reads a sentences in Penn Tree Bank bracket style and return sentences.
 */
public class TigerReader implements PSReader {

    BufferedReader inputReader;
    ArrayList<File> psFiles = new ArrayList<>();
    ArrayList<PSTree> psCache = new ArrayList<>();
    String filter[] = null;
    int startFilter = -1;
    int endFilter = -1;

    public TigerReader() {
    }

    public TigerReader(String file) {

        try {
            inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"), 32768);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param ps
     */
    @Override
    public void startReading(String file, String[] filter) {

        try {
            this.filter = filter;
            startFilter = filter == null ? -1 : 1;
            endFilter = filter == null ? -1 : 1;

            inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"), 32768);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class Line {

        String form;
        String lemma;
        String morph;
        String pos;
        int parent;
        String edge;
    }
    static int stop = 0;

    /**
     * @return
     */
    @Override
    public PSTree getNext() {

        PSTree ps = null;
        String l;
        ArrayList<Line> lines = new ArrayList<>();
        try {
            int state = 1, terminals = 0, nonterminals = 0;
            while ((l = inputReader.readLine()) != null) {

                if (startFilter == 1 && l.startsWith("#BOS " + filter[0])) {
                    Parser.out.println("found start " + l);
                    startFilter = 2;
                }
                if (endFilter == 1 && l.startsWith("#EOS " + filter[1])) {
                    Parser.out.println("found end " + l);

                    endFilter = 2;
                }


                if (startFilter == 1 || endFilter == 2) {
                    continue;
                }

                if (l.startsWith("#BOS")) {

                    state = 2;
                    continue;
                }
                if (l.startsWith("#500")) {
                    state = 3;
                }
                if (l.startsWith("#EOS")) {
                    state = 4;
                }
                if (state < 2) {
                    continue;
                }

                if (state == 4) {

                    ps = new PSTree();
                    ps.create(terminals, nonterminals);
                    //	Parser.out.println("terminals "+terminals);
                    //build ps tree

                    int cnt = 0;
                    //		ps.entries[0] =CONLLReader09.ROOT;
                    //		ps.head[0]=-1;
                    int root = -1;
                    for (Line line : lines) {

                        /*
                         * if (cnt==terminals) { // insert root root =cnt;
                         * cnt++; }
                         */
                        ps.entries[cnt] = line.form;
                        if (cnt < terminals) {
                            ps.pos[cnt] = line.pos;
                        } else {
                            ps.entries[cnt] = line.pos;
                        }
                        ps.lemmas[cnt] = line.lemma;
                        ps.head[cnt] = line.parent == 0 ? lines.size() - 1 : line.parent >= 500 ? line.parent - 500 + terminals : line.parent;
                        //	ps.head[cnt] = line.parent==0?lines.size()-1:line.parent>=500?line.parent-500+terminals:line.parent;
                        ps.morph[cnt] = line.morph;
                        cnt++;

                    }

                    if (root == -1) {
                        root = terminals;
                    }
                    ps.head[cnt - 1] = 0;  // root
                    ps.terminalCount = terminals;
                    lines.clear();
                    state = 1;

                    /*
                     * for(int k=0;k<ps.head.length;k++) { if
                     * (ps.head[k]<terminals && k!=root) { ps.head[k]=root; //
                     * DB.println("error "+k+" "+ps.head[k]); } }
                     */
                    //				Parser.out.println(""+ps.toString());
                    //				if (stop++ == 4)System.exit(0);
                    return ps;
                }



                StringTokenizer t = new StringTokenizer(l, "\t");
                int tc = 0;
                Line line = new Line();
                lines.add(line);
                while (t.hasMoreTokens()) {
                    String token = t.nextToken();
                    if (token.equals("\t")) {
                        continue;
                    }
                    if (tc == 0) {
                        if (token.startsWith("#5") || token.startsWith("#6")) {
                            nonterminals++;

                        } else {
                            terminals++;

                            //change it back to the wrong format since the conll stuff was derived from this. 
                            //	if (token.equals("durchblicken")) token="durchblikken";
                            line.form = token;
                        }

                    } else if (tc == 1) {
                        line.lemma = token;
                    } else if (tc == 2) {
                        line.pos = token;
                    } else if (tc == 3) {
                        line.morph = token;
                    } else if (tc == 4) {
                        line.edge = token;
                    } else if (tc == 5) {
                        line.parent = Integer.parseInt(token);
                    }


                    if (token.length() > 0) {
                        tc++;
                    }
                }

                // read till #EOS
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return ps;
    }

    /**
     * @param tree
     */
    private void removeTraces(ArrayList<Object> tree) {

        Stack<ArrayList<Object>> s = new Stack<>();

        s.push(tree);
        ArrayList<Object> list = null;
        while (!s.isEmpty()) {

            ArrayList<Object> last = list;
            list = s.pop();
            for (int k = 0; k < list.size(); k++) {
                Object o = list.get(k);
                if (o instanceof String) {
                    String t = (String) o;
                    if ((t.endsWith("-1") || t.endsWith("-2") || t.endsWith("-3") || t.endsWith("-4")) && list.size() > (k + 1)) {
                        t = t.substring(0, t.length() - 2);
                        list.set(k, t);
                    }

                    if (t.startsWith("-NONE-")) {

                        // remove the bigger surrounding phrase, e.g. (NP (-NONE- *))
                        if (last.size() == 2 && last.get(0) instanceof String && last.contains(list)) {
                            ArrayList<Object> rest = remove(tree, last);
                            if (rest != null && rest.size() == 1) {
                                rest = remove(tree, rest);
                            }
                        } // remove the phrase only, e.g. (NP (AP nice small) (-NONE- *))
                        else {
                            // there might a phrase with two empty elements (VP (-NONE- *) (-NONE- ...))
//							Parser.out.println("last "+last+" list "+list );
                            ArrayList<Object> rest = remove(tree, list);
                            removeTraces(rest);
                            if (rest.size() == 1) {
                                rest = remove(tree, rest);
                                if (rest != null && rest.size() == 1) {
                                    Parser.out.println("rest " + rest);
                                    System.exit(0);
                                }
                            }
                        }
                        continue;
                    }
                }
                if (o instanceof ArrayList) {
                    s.push((ArrayList<Object>) o);
                }
            }
        }
    }

    /**
     * Remove from tree p
     *
     * @param tree phrase structure tree
     * @param p elment to remove
     */
    private ArrayList<Object> remove(ArrayList<Object> tree, Object p) {
        Stack<ArrayList<Object>> s = new Stack<>();

        s.push(tree);

        while (!s.isEmpty()) {

            ArrayList<Object> list = s.pop();
            for (int k = 0; k < list.size(); k++) {
                Object o = list.get(k);
                if (o == p) {
                    list.remove(p);
                    return list;
                }
                if (o instanceof ArrayList) {
                    s.push((ArrayList<Object>) o);
                }
            }
        }
        return null;
    }

    /**
     * Count the terminals
     *
     * @param current
     * @return
     */
    private int countTerminals(ArrayList<Object> current) {

        int count = 0;
        boolean found = false, all = true;
        for (Object o : current) {
            if (o instanceof String) {
                found = true;
            } else {
                all = false;
                if (o instanceof ArrayList) {
                    count += countTerminals((ArrayList<Object>) o);
                }
            }
        }

        if (found && all) {
            //		Parser.out.println(""+current);
            count++;
        }

        return count;
    }

    /**
     * Count the terminals
     *
     * @param current
     * @return
     */
    private int insert(PSTree ps, ArrayList<Object> current, Integer terminal, Integer xxx, int head) {

        boolean found = false, all = true;
        String term = null;
        String pos = null;
        for (Object o : current) {
            if (o instanceof String) {
                if (found) {
                    term = (String) o;
                }
                if (!found) {
                    pos = (String) o;
                }
                found = true;
            } else {
                all = false;
                //				if (o instanceof ArrayList) count +=countTerminals((ArrayList<Object>)o); 
            }
        }

        if (found && all) {

            if (term.equals("-LRB-")) {
                term = "(";
            }
            if (term.equals("-RRB-")) {
                term = ")";
            }
            if (term.equals("-LCB-")) {
                term = "{";
            }
            if (term.equals("-RCB-")) {
                term = "}";
            }
            if (term.contains("1\\/2-year")) {
                term = term.replace("\\/", "/");
            }
            if (term.contains("1\\/2-foot-tall")) {
                term = term.replace("\\/", "/");
            }


            ps.entries[ps.terminalCount] = term;
            ps.pos[ps.terminalCount] = pos;
            ps.head[ps.terminalCount] = head;
            //	Parser.out.println("terminal "+term+" "+ps.terminal+" head "+head);
            ps.terminalCount++;
        } else if (found && !all) {
            if (pos.startsWith("NP-SBJ")) {
                pos = "NP-SBJ";
            }
            if (pos.startsWith("WHNP")) {
                pos = "WHNP";
            }

            ps.entries[ps.non] = pos;
            ps.head[ps.non] = head;
            //	Parser.out.println("non terminal "+pos+" "+ps.non+" head "+	head);
            int non = ps.non++;

            for (Object o : current) {
                if (o instanceof ArrayList) {
                    insert(ps, (ArrayList<Object>) o, terminal, ps.non, non);
                }
            }
        }
        if (!all && !found) {
            for (Object o : current) {
                if (o instanceof ArrayList) {
                    insert(ps, (ArrayList<Object>) o, terminal, 0, ps.non - 1);
                }
            }
        }
        return terminal;
    }

    /**
     * Count the terminals
     *
     * @param current
     * @return
     */
    private int countNonTerminals(ArrayList<Object> current) {

        int count = 0;
        boolean found = false, all = true;
        for (Object o : current) {
            if (o instanceof String) {
                found = true;
            } else {
                all = false;
                if (o instanceof ArrayList) {
                    count += countNonTerminals((ArrayList<Object>) o);
                }
            }
        }

        if (found && !all) {
            count++;
        }

        return count;
    }
}