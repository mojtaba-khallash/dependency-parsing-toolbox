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

import clear.util.JArrays;
import com.carrotsearch.hppc.IntArrayList;

/**
 * Semantic role labeling argument	.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/19/2011
 */
public class SRLSpan {

    static public final String ID_DELIM = ",";
    public String label;
    public IntArrayList argIds;

    public SRLSpan(String span) {
        String[] tmp = span.split(SRLHead.DELIM);
        label = tmp[0];
        argIds = new IntArrayList();

        for (String id : tmp[1].split(ID_DELIM)) {
            argIds.add(Integer.parseInt(id));
        }
    }

    public SRLSpan(IntArrayList argIds, String label) {
        set(argIds, label);
    }

    public void set(IntArrayList argIds, String label) {
        this.argIds = argIds;
        this.label = label;
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        build.append(label);
        build.append(SRLHead.DELIM);
        build.append(JArrays.join(argIds, ID_DELIM));

        return build.toString();
    }
}