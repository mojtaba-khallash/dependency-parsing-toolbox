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
package clear.propbank;

import java.util.ArrayList;

/**
 * Propbank instance.
 *
 * @author Jinho D. Choi <b>Last update:</b> 2/15/2010
 */
public class PBInstance {

    static final public String KEY_DELIM = "*";
    /**
     * Name of the treebank file
     */
    public String treePath;
    /**
     * Tree index (starting from 0)
     */
    public int treeIndex;
    /**
     * Predicate ID
     */
    public int predicateId;
    /**
     * Annotator ID
     */
    public String annotator;
    /**
     * Instance type (e.g., lemma-v, lemma-n)
     */
    public String type;
    /**
     * Roleset (or frameset) ID
     */
    public String rolesetId;
    /**
     * List of arguments
     */
    private ArrayList<PBArg> a_arg = null;

    /**
     * Initializes the Propbank instance.
     */
    public PBInstance() {
        a_arg = new ArrayList<>();
    }

    public String getKey() {
        return treePath + KEY_DELIM + treeIndex + KEY_DELIM + predicateId;
    }

    /**
     * Adds an argument to the instance.
     *
     * @param arg Propbank argument
     */
    public void addArg(PBArg arg) {
        PBArg tmp;

        if ((tmp = getArgByLabel(arg.label)) != null) {
            tmp.addLocs(arg.getLocs());
        } else {
            a_arg.add(arg);
        }
    }

    /**
     * Adds a list of Propbank arguments. Discard arguments that are already in
     * the instance.
     */
    public void addArgs(ArrayList<PBArg> args) {
        for (PBArg arg : args) {
            if (!contains(arg)) {
                addArg(arg);
            }
        }
    }

    public PBArg getArg(String label) {
        for (PBArg pbArg : a_arg) {
            if (pbArg.isLabel(label)) {
                return pbArg;
            }
        }

        return null;
    }

    /**
     * @return the list of Propbank arguments
     */
    public ArrayList<PBArg> getArgs() {
        return a_arg;
    }

    /**
     * @return true if the instance contains
     * <code>arg</code>
     */
    public boolean contains(PBArg arg) {
        for (PBArg a : a_arg) {
            if (a.equals(arg)) {
                return true;
            }
        }

        return false;
    }

    public PBArg getArgByLabel(String label) {
        for (PBArg arg : a_arg) {
            if (arg.isLabel(label)) {
                return arg;
            }
        }

        return null;
    }

    /**
     * Returns the string representation of the Propbank instance.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(treePath);
        buff.append(PBLib.FIELD_DELIM);
        buff.append(treeIndex);
        buff.append(PBLib.FIELD_DELIM);
        buff.append(predicateId);
        buff.append(PBLib.FIELD_DELIM);
        buff.append(rolesetId);

        for (PBArg arg : a_arg) {
            buff.append(PBLib.FIELD_DELIM);
            buff.append(arg.toString());
        }

        return buff.toString();
    }
}