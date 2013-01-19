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

import clear.dep.DepLib;
import clear.reader.AbstractReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Compare two dependency trees.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/19/2011
 */
public class SRLInfo {

    static final public String DELIM_ARG = ";";
    public String rolesetId;
    public ArrayList<SRLHead> heads;
    public ArrayList<SRLSpan> spans;

    public SRLInfo() {
        rolesetId = DepLib.FIELD_BLANK;
        heads = new ArrayList<>();
    }

    /**
     * @param args "headId:label(;headId:label)*" or "_"
     */
    public SRLInfo(String rolesetId, String args) {
        this.rolesetId = rolesetId;
        heads = new ArrayList<>();

        if (!args.equals(DepLib.FIELD_BLANK)) {
            for (String arg : args.split(DELIM_ARG)) {
                heads.add(new SRLHead(arg));
            }
        }
    }

//	============================ Getter ============================
    /**
     * @return label of the headId; if not exists, returns null.
     */
    public String getLabel(int headId) {
        for (SRLHead head : heads) {
            if (head.equals(headId)) {
                return head.label;
            }
        }

        return null;
    }

//	============================ Setter ============================
    public void setRolesetId(String rolesetId) {
        this.rolesetId = rolesetId;
    }

    public void addHead(int headId, String label) {
        heads.add(new SRLHead(headId, label));
    }

    public void addHead(int headId, String label, double score) {
        heads.add(new SRLHead(headId, label, score));
    }

    public void addHeads(ArrayList<SRLHead> heads) {
        this.heads.addAll(heads);
    }

    public void setSpan(String spans) {
        this.spans = new ArrayList<>();

        for (String span : spans.split(DELIM_ARG)) {
            this.spans.add(new SRLSpan(span));
        }
    }

//	============================ Boolean ============================
    public boolean isPredicate() {
        return !rolesetId.equals(DepLib.FIELD_BLANK);
    }

    public boolean isEmptyHead() {
        return heads.isEmpty();
    }

    public boolean isArgOf(int headId) {
        for (SRLHead head : heads) {
            if (head.equals(headId)) {
                return true;
            }
        }

        return false;
    }

    public SRLHead getHead(int headId, String label) {
        for (SRLHead head : heads) {
            if (head.equals(headId, label)) {
                return head;
            }
        }

        return null;
    }

    public SRLHead getHead(int headId) {
        for (SRLHead head : heads) {
            if (head.equals(headId)) {
                return head;
            }
        }

        return null;
    }

    public boolean labelMatches(String regex) {
        for (SRLHead head : heads) {
            if (head.labelMatches(regex)) {
                return true;
            }
        }

        return false;
    }

//	============================ Global ============================
    public void copy(SRLInfo info) {
        rolesetId = info.rolesetId;
        heads.clear();

        for (SRLHead head : info.heads) {
            heads.add(head.clone());
        }
    }

    @Override
    public SRLInfo clone() {
        SRLInfo info = new SRLInfo();

        info.copy(this);
        return info;
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        build.append(rolesetId);
        build.append(AbstractReader.FIELD_DELIM);

        if (heads.isEmpty()) {
            build.append(DepLib.FIELD_BLANK);
        } else {
            Collections.sort(heads);
            build.append(heads.get(0).toString());

            for (int i = 1; i < heads.size(); i++) {
                build.append(DELIM_ARG);
                build.append(heads.get(i).toString());
            }
        }

        return build.toString();
    }
}