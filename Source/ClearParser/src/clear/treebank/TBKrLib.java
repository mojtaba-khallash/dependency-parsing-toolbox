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
package clear.treebank;

import clear.util.tuple.JObjectObjectTuple;
import java.util.ArrayList;

/**
 * English Treebank library.
 *
 * @author Jinho D. Choi <b>Last update:</b> 9/1/2010
 */
public class TBKrLib extends TBLib {
    // clausal-level pos-tags

    static final public String POS_S = "S";
    static final public String POS_AP = "AP";
    static final public String POS_DP = "DP";
    static final public String POS_NP = "NP";
    static final public String POS_IP = "IP";
    static final public String POS_VP = "VP";
    static final public String POS_VNP = "VNP";
    static final public String POS_Q = "Q";
    static final public String POS_X = "X";
    static final public String POS_L = "L";
    static final public String POS_R = "R";
    // function tags
    static final public String TAG_AJT = "AJT";
    static final public String TAG_CMP = "CMP";
    static final public String TAG_CNJ = "CNJ";
    static final public String TAG_INT = "INT";
    static final public String TAG_MOD = "MOD";
    static final public String TAG_OBJ = "OBJ";
    static final public String TAG_PRN = "PRN";
    static final public String TAG_SBJ = "SBJ";
    static final public String DEP_P = "P";
    static final public String DEP_ADV = "ADV";
    static final public String DEP_MOD = "MOD";
    static final public String DEP_AMOD = "AMOD";
    static final public String DEP_DMOD = "DMOD";
    static final public String DEP_NMOD = "NMOD";
    static final public String DEP_VMOD = "VMOD";
    static final public String DEP_QMOD = "QMOD";

    static public ArrayList<JObjectObjectTuple<String, String>> splitMorphem(String morphem) {
        if (morphem.startsWith("+")) {
            morphem = morphem.substring(1);
        }
        morphem = morphem.replaceAll("//", "-FS-/");
        morphem = morphem.replaceAll("\\+/", "-PS-/");
        String[] tmp = morphem.split("\\+|/");

        ArrayList<JObjectObjectTuple<String, String>> list = new ArrayList<>(tmp.length / 2);
        if (tmp.length % 2 != 0) {
            return list;
        }

        for (int i = 0; i < tmp.length; i += 2) {
            switch (tmp[i]) {
                case "-FS-":
                    tmp[i] = "/";
                    break;
                case "-PS-":
                    tmp[i] = "+";
                    break;
            }

            list.add(new JObjectObjectTuple<>("_" + tmp[i], tmp[i + 1]));
        }

        return list;
    }

    static public boolean isPunctuation(String morphem) {
        for (JObjectObjectTuple<String, String> tup : splitMorphem(morphem)) {
            if (!tup.o2.matches("SF|SP|SS|SE|SO|SW")) {
                return false;
            }
        }

        return true;
    }
}