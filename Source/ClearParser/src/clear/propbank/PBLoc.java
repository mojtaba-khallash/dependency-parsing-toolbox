/**
 * Copyright (c) 2010, Regents of the University of Colorado All rights
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

/**
 * Propbank location.
 *
 * @author Jinho D. Choi <b>Last update:</b> 1/25/2011
 */
public class PBLoc implements Comparable<PBLoc> {

    public String type;			// "" | "*" | "," | ";"
    public int terminalId;
    public int height;

    public PBLoc(String type, int terminalId, int height) {
        set(type, terminalId, height);
    }

    public void set(String type, int terminalId, int height) {
        this.type = type;
        this.terminalId = terminalId;
        this.height = height;
    }

    public void copy(PBLoc pbLoc) {
        set(pbLoc.type, pbLoc.terminalId, pbLoc.height);
    }

    /**
     * @return true if this location is
     * <code>terminalId:height</code>.
     */
    public boolean equals(int terminalId, int height) {
        return this.terminalId == terminalId && this.height == height;
    }

    /**
     * @return true if this location is the same as
     * <code>loc</code> (regardless of the type).
     */
    public boolean equals(PBLoc loc) {
        return terminalId == loc.terminalId && height == loc.height;
    }

    /**
     * @return true if the type of this location is
     * <code>type</code>.
     */
    public boolean isType(String type) {
        return this.type.equals(type);
    }

    /**
     * @return true if the current location is superset of
     * <code>loc</code> (regardless of the type).
     */
    public boolean contains(PBLoc loc) {
        return terminalId == loc.terminalId && height >= loc.height;
    }

    public String getLoc() {
        StringBuilder build = new StringBuilder();

        build.append(terminalId);
        build.append(PBLib.PROP_LOC_DELIM);
        build.append(height);

        return build.toString();
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        build.append(type);
        build.append(terminalId);
        build.append(PBLib.PROP_LOC_DELIM);
        build.append(height);

        return build.toString();
    }

    @Override
    public int compareTo(PBLoc pbLoc) {
        int diffTerminal = terminalId - pbLoc.terminalId;

        if (diffTerminal != 0) {
            return diffTerminal;
        }
        return height - pbLoc.height;
    }
}