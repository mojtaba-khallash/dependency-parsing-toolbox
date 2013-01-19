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
import java.util.HashSet;

/**
 * Propbank argument.
 *
 * @author Jinho D. Choi <b>Last update:</b> 1/25/2011
 */
public class PBArg {

    /**
     * Propbank argument-label
     */
    public String label;
    /**
     * Predicate ID of this argument
     */
    public int predicateId;
    /**
     * Propbank locations
     */
    private ArrayList<PBLoc> pb_locs = null;

    /**
     * Initializes the Propbank argument.
     */
    public PBArg(String label, int predicateId) {
        init(label, predicateId);
    }

    public void init(String label, int predicateId) {
        this.label = label;
        this.predicateId = predicateId;
        this.pb_locs = new ArrayList<>();
    }

    /**
     * @return true if this argument contains
     * <code>terminalId:height</code>.
     */
    public boolean containsLoc(int terminalId, int height) {
        for (PBLoc loc : pb_locs) {
            if (loc.equals(terminalId, height)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if this argument contains
     * <code>pbLoc</code>.
     */
    public boolean containsLoc(PBLoc pbLoc) {
        for (PBLoc loc : pb_locs) {
            if (loc.equals(pbLoc)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if this argument has overlap with
     * <code>pbArg</code>.
     */
    public boolean overlapsLocs(PBArg pbArg) {
        for (PBLoc cLoc : pb_locs) {
            for (PBLoc pLoc : pbArg.getLocs()) {
                if (cLoc.equals(pLoc)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean overlapsMildLocs(PBArg pbArg) {
        for (PBLoc cLoc : pb_locs) {
            for (PBLoc pLoc : pbArg.getLocs()) {
                if (cLoc.terminalId == pLoc.terminalId) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds a location.
     */
    public boolean addLoc(PBLoc pbLoc) {
        return pb_locs.add(pbLoc);
    }

    /**
     * Adds all locations in
     * <code>pbLocs</code> that are not already in this argument.
     */
    public void addLocs(ArrayList<PBLoc> pbLocs) {
        for (PBLoc pbLoc : pbLocs) {
            if (!containsLoc(pbLoc)) {
                pb_locs.add(pbLoc);
            }
        }
    }

    /**
     * Inserts the location to the argument; if already exists, overwrite.
     */
    public boolean putLoc(PBLoc pbLoc) {
        for (PBLoc loc : pb_locs) {
            if (loc.contains(pbLoc)) {
                loc.type = pbLoc.type;
                return true;
            } else if (pbLoc.contains(loc)) {
                loc.copy(pbLoc);
                return true;
            }
        }

        return pb_locs.add(pbLoc);
    }

    public void putLocs(ArrayList<PBLoc> pbLocs) {
        for (PBLoc pbLoc : pbLocs) {
            putLoc(pbLoc);
        }
    }

    public void removeAllLocs() {
        pb_locs = new ArrayList<>();
    }

    /**
     * Removes
     * <code>pbLoc</code> (the object) from this argument.
     */
    public boolean removeLoc(PBLoc pbLoc) {
        return pb_locs.remove(pbLoc);
    }

    /**
     * Removes all occurrences of
     * <code>pbLoc</code> (the location) from this argument.
     */
    public boolean removeLocs(PBLoc pbLoc) {
        HashSet<PBLoc> set = new HashSet<>();

        for (PBLoc loc : pb_locs) {
            if (pbLoc.contains(loc)) {
                set.add(loc);
            }
        }

        return pb_locs.removeAll(set);
    }

    public ArrayList<PBLoc> getLocs() {
        return pb_locs;
    }

    /**
     * @return if the label is a pattern of
     * <code>regex</code>.
     */
    public boolean isLabel(String regex) {
        return label.matches(regex);
    }

    /**
     * @return string representation of the argument.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        for (PBLoc loc : pb_locs) {
            buff.append(loc.toString());
        }

        buff.append(PBLib.PROP_LABEL_DELIM);
        buff.append(label);

        return buff.toString();
    }
}