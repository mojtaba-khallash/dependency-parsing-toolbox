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
package clear.dep.srl;

import clear.dep.DepNode;
import clear.parse.SRLParser;
import clear.util.cluster.Prob2dMap;
import clear.util.tuple.JObjectObjectTuple;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

public class SRLProb {

    static public final String SYM_PREV = "<";
    static public final String SYM_NEXT = ">";
    static public final String SYM_ACTIVE = "a";
    static public final String SYM_PASSIVE = "p";
    static public final String ARG_NONE = "NONE";
    static public final String ARG_END = "END";
    private Prob2dMap m_prob1a;
    private Prob2dMap m_prob2a;
    private Prob2dMap m_prob2n;

    public SRLProb() {
        m_prob1a = new Prob2dMap();
        m_prob2a = new Prob2dMap();
        m_prob2n = new Prob2dMap();
    }

//	============================= Retrieve Key =============================
    public String getKey(DepNode pred, byte dir) {
        String postfix, feat;

        if (dir == SRLParser.DIR_LEFT) {
            postfix = SYM_PREV;
        } else {
            postfix = SYM_NEXT;
        }

        if ((feat = pred.getFeat("vo")) != null && feat.equals("1")) {
            postfix += SYM_PASSIVE;
        } else {
            postfix += SYM_ACTIVE;
        }

        return pred.lemma + postfix;
    }

    public String getKey(DepNode pred, String prevArg, byte dir) {
        return getKey(pred, dir) + "|" + prevArg;
    }

    public boolean isPrevArg(String label) {
        return (label.startsWith(SYM_PREV));
    }

    public boolean isNextArg(String label) {
        return (label.startsWith(SYM_NEXT));
    }

//	============================= Count 1st-degree =============================
    /**
     * For training.
     */
    public void add1dArgs(DepNode pred, HashSet<String> sArgs) {
        HashSet<String> pSet = new HashSet<>();
        HashSet<String> nSet = new HashSet<>();

        for (String label : sArgs) {
            if (isPrevArg(label)) {
                pSet.add(label);
            } else {
                nSet.add(label);
            }
        }

        String pKey = getKey(pred, SRLParser.DIR_LEFT);
        String nKey = getKey(pred, SRLParser.DIR_RIGHT);

        if (pSet.isEmpty()) {
            m_prob1a.increment(pKey, ARG_END);
        } else {
            m_prob1a.increment(pKey, pSet);
        }

        if (nSet.isEmpty()) {
            m_prob1a.increment(nKey, ARG_END);
        } else {
            m_prob1a.increment(nKey, nSet);
        }
    }

//	============================= Count 2nd-degree =============================
    /**
     * For training.
     */
    public void add2dArgs(DepNode pred, ArrayList<SRLArg> lsArgs) {
        ArrayList<String> pList = new ArrayList<>();
        ArrayList<String> nList = new ArrayList<>();

        for (SRLArg arg : lsArgs) {
            if (isPrevArg(arg.label)) {
                pList.add(arg.label);
            } else {
                nList.add(arg.label);
            }
        }

        JObjectObjectTuple<String, String> prevArgs = new JObjectObjectTuple<>(ARG_NONE, ARG_NONE);

        add2dArgsAux(pList, pred, prevArgs, SRLParser.DIR_LEFT);
        add2dArgsAux(nList, pred, prevArgs, SRLParser.DIR_RIGHT);
    }

    /**
     * Called from {@link SRLProb#add1dArgs(DepNode, HashSet)}.
     */
    private void add2dArgsAux(ArrayList<String> list, DepNode pred, JObjectObjectTuple<String, String> prevArgs, byte dir) {
        for (String currArg : list) {
            m_prob2a.increment(getKey(pred, prevArgs.o1, dir), currArg);
            m_prob2n.increment(getKey(pred, prevArgs.o2, dir), currArg);

            prevArgs.o1 = currArg;
            if (currArg.substring(1).matches("A\\d")) {
                prevArgs.o2 = currArg;
            }
        }

        m_prob2a.increment(getKey(pred, prevArgs.o1, dir), ARG_END);
        m_prob2n.increment(getKey(pred, prevArgs.o2, dir), ARG_END);
    }

//	============================= Print =============================
    public void printAll(String filename) {
        DecimalFormat format = new DecimalFormat("#0.0000");

        m_prob1a.print(filename + ".p1a", format);
        m_prob2a.print(filename + ".p2a", format);
        m_prob2n.print(filename + ".p2n", format);
    }
}