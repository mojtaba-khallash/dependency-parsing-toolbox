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
package clear.dep;

import java.util.regex.Pattern;

/**
 * This class contains constant variables for dependency parsing.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/26/2010
 */
public class DepLib {

    static final public String FIELD_BLANK = "_";
    /**
     * ID of the root node
     */
    static public final int ROOT_ID = 0;
    /**
     * ID of a null node
     */
    static public final int NULL_ID = -1;
    /**
     * Head ID of a null/root node
     */
    static public final int NULL_HEAD_ID = -2;
    /**
     * Feature tag for root nodes
     */
    static public final String ROOT_TAG = "#$ROOT$#";
    static public final String DEPREL_ADV = "ADV";
    static public final String DEPREL_AMOD = "AMOD";
    static public final String DEPREL_APPO = "APPO";
    static public final String DEPREL_AUX = "AUX";
    static public final String DEPREL_BNF = "BNF";
    static public final String DEPREL_CAP = "CAP";
    static public final String DEPREL_CIT = "CIT";
    static public final String DEPREL_COORD = "COORD";
    static public final String DEPREL_CONJ = "CONJ";
    static public final String DEPREL_CLF = "CLF";
    static public final String DEPREL_DEP = "DEP";
    static public final String DEPREL_DIR = "DIR";
    static public final String DEPREL_DTV = "DTV";
    static public final String DEPREL_EDIT = "EDIT";
    static public final String DEPREL_EXT = "EXT";
    static public final String DEPREL_EXTR = "EXTR";
    static public final String DEPREL_GAP = "GAP";
    static public final String DEPREL_IM = "IM";
    static public final String DEPREL_INTJ = "INTJ";
    static public final String DEPREL_LGS = "LGS";
    static public final String DEPREL_LOC = "LOC";
    static public final String DEPREL_META = "META";
    static public final String DEPREL_MNR = "MNR";
    static public final String DEPREL_MOD = "MOD";
    static public final String DEPREL_NMOD = "NMOD";
    static public final String DEPREL_OBJ = "OBJ";
    static public final String DEPREL_OPRD = "OPRD";
    static public final String DEPREL_P = "P";
    static public final String DEPREL_PMOD = "PMOD";
    static public final String DEPREL_PRD = "PRD";
    static public final String DEPREL_PRN = "PRN";
    static public final String DEPREL_PRP = "PRP";
    static public final String DEPREL_PRT = "PRT";
    static public final String DEPREL_QMOD = "QMOD";
    static public final String DEPREL_ROOT = "ROOT";
    static public final String DEPREL_SEZ = "SEZ";
    static public final String DEPREL_SBJ = "SBJ";
    static public final String DEPREL_SUB = "SUB";
    static public final String DEPREL_TMP = "TMP";
    static public final String DEPREL_VC = "VC";
    static public final String DEPREL_VOC = "VOC";
    static public final Pattern M_VC = Pattern.compile("CONJ|COORD|IM|OPRD|VC");
    static public final String[] CZ_FEAT = {"Cas", "Gen", "Gra", "Neg", "Num", "PGe", "PNu", "Per", "Sem", "SubPOS", "Ten", "Var", "Voi"};
}