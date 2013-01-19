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

/**
 * English Treebank library.
 *
 * @author Jinho D. Choi <b>Last update:</b> 9/1/2010
 */
public class TBEnLib extends TBLib {
    // clausal-level pos-tags

    static final public String POS_S = "S";
    static final public String POS_SBAR = "SBAR";
    static final public String POS_SBARQ = "SBARQ";
    static final public String POS_SINV = "SINV";
    static final public String POS_SQ = "SQ";
    // phrasal-level pos-tags
    static final public String POS_ADJP = "ADJP";
    static final public String POS_ADVP = "ADVP";
    static final public String POS_CAPTION = "CAPTION";
    static final public String POS_CIT = "CIT";
    static final public String POS_CONJP = "CONJP";
    static final public String POS_EDITED = "EDITED";
    static final public String POS_EMBED = "EMBED";
    static final public String POS_FRAG = "FRAG";
    static final public String POS_INTJ = "INTJ";
    static final public String POS_LST = "LST";
    static final public String POS_META = "META";
    static final public String POS_NAC = "NAC";
    static final public String POS_NML = "NML";
    static final public String POS_NP = "NP";
    static final public String POS_NX = "NX";
    static final public String POS_PP = "PP";
    static final public String POS_PRN = "PRN";
    static final public String POS_PRT = "PRT";
    static final public String POS_QP = "QP";
    static final public String POS_RRC = "RRC";
    static final public String POS_UCP = "UCP";
    static final public String POS_UH = "UH";
    static final public String POS_VP = "VP";
    static final public String POS_WHADJP = "WHADJP";
    static final public String POS_WHADVP = "WHADVP";
    static final public String POS_WHNP = "WHNP";
    static final public String POS_WHPP = "WHPP";
    static final public String POS_X = "X";
    // word-level pos-tags
    static final public String POS_ADD = "ADD";
    static final public String POS_AFX = "AFX";
    static final public String POS_CC = "CC";
    static final public String POS_CD = "CD";
    static final public String POS_CODE = "CODE";
    static final public String POS_DT = "DT";
    static final public String POS_DOLLAR = "$";
    static final public String POS_EX = "EX";
    static final public String POS_FW = "FW";
    static final public String POS_IN = "IN";
    static final public String POS_JJ = "JJ";
    static final public String POS_JJR = "JJR";
    static final public String POS_JJS = "JJS";
    static final public String POS_LS = "LS";
    static final public String POS_MD = "MD";
    static final public String POS_NN = "NN";
    static final public String POS_NNS = "NNS";
    static final public String POS_NNP = "NNP";
    static final public String POS_NNPS = "NNPS";
    static final public String POS_PDT = "PDT";
    static final public String POS_POS = "POS";
    static final public String POS_PRP = "PRP";
    static final public String POS_PRP$ = "PRP$";
    static final public String POS_RB = "RB";
    static final public String POS_RBR = "RBR";
    static final public String POS_RBS = "RBS";
    static final public String POS_RP = "RP";
    static final public String POS_TO = "TO";
    static final public String POS_VB = "VB";
    static final public String POS_VBD = "VBD";
    static final public String POS_VBG = "VBG";
    static final public String POS_VBN = "VBN";
    static final public String POS_VBP = "VBP";
    static final public String POS_VBZ = "VBZ";
    static final public String POS_WDT = "WDT";
    static final public String POS_WP = "WP";
    static final public String POS_WP$ = "WP$";
    static final public String POS_WRB = "WRB";
    static final public String POS_XX = "XX";
    // punctuation pos-tags
    static final public String POS_COLON = ":";
    static final public String POS_COMMA = ",";
    static final public String POS_HYPH = "HYPH";
    static final public String POS_LDQ = "``";
    static final public String POS_LRB = "-LRB-";
    static final public String POS_NFP = "NFP";
    static final public String POS_PERIOD = ".";
    static final public String POS_RDQ = "''";
    static final public String POS_RRB = "-RRB-";
    static final public String POS_SYM = "SYM";
    // function tags
    static final public String TAG_ADV = "ADV";
    static final public String TAG_BNF = "BNF";
    static final public String TAG_CLF = "CLF";
    static final public String TAG_CLR = "CLR";
    static final public String TAG_DIR = "DIR";
    static final public String TAG_DTV = "DTV";
    static final public String TAG_ETC = "ETC";
    static final public String TAG_EXT = "EXT";
    static final public String TAG_HLN = "HLN";
    static final public String TAG_IMP = "IMP";
    static final public String TAG_LGS = "LGS";
    static final public String TAG_LOC = "LOC";
    static final public String TAG_MNR = "MNR";
    static final public String TAG_NOM = "NOM";
    static final public String TAG_PRD = "PRD";
    static final public String TAG_PRP = "PRP";
    static final public String TAG_PUT = "PUT";
    static final public String TAG_SBJ = "SBJ";
    static final public String TAG_SEZ = "SEZ";
    static final public String TAG_TMP = "TMP";
    static final public String TAG_TPC = "TPC";
    static final public String TAG_TTL = "TTL";
    static final public String TAG_UNF = "UNF";
    static final public String TAG_VOC = "VOC";
    // empty categories
    static final public String EC_EXP = "*EXP*";
    static final public String EC_ELLIPSE = "*?*";
    static final public String EC_ICH = "*ICH*";
    static final public String EC_NOT = "*NOT*";
    static final public String EC_NULL = "0";
    static final public String EC_PPA = "*PPA*";
    static final public String EC_PRO = "*PRO*";
    static final public String EC_RNR = "*RNR*";
    static final public String EC_STAR = "*";
    static final public String EC_TRACE = "*T*";
    static final public String EC_UNIT = "*U*";

    static public boolean isConjunction(String pos) {
        return isWordConjunction(pos) || isPuncConjunction(pos);
    }

    static public boolean isWordConjunction(String pos) {
        return pos.equals(POS_CC) || pos.equals(POS_CONJP);
    }

    static public boolean isPuncConjunction(String pos) {
        return pos.equals(POS_COMMA) || pos.equals(POS_COLON);
    }

    static public boolean isCorrelativeConjunction(String words) {
        words = words.toLowerCase();
        return words.equals("either") || words.equals("neither") || words.equals("whether") || words.equals("both") || words.equals("not only");
    }

    static public boolean isNounLike(String pos) {
        return isNoun(pos) || pos.equals(POS_NP) || pos.equals(POS_NML) || pos.equals(POS_WHNP) || pos.contains(TAG_NOM);
    }

    static public boolean isNoun(String pos) {
        return pos.startsWith(POS_NN) || pos.equals(POS_PRP);
    }

    static public boolean isVerb(String pos) {
        return pos.startsWith(POS_VB) || pos.equals(POS_MD);
    }

    static public boolean isAdjectiveLike(String pos) {
        return isAdjective(pos) || pos.equals(POS_ADJP);
    }

    static public boolean isAdjective(String pos) {
        return pos.startsWith(POS_JJ);
    }

    static public boolean isAdverb(String pos) {
        return pos.startsWith(POS_RB);
    }

    static public boolean isWhAdverbLike(String pos) {
        return pos.equals(POS_WHADVP) || pos.equals(POS_WRB) || pos.equals(POS_WHPP) || pos.equals(POS_IN);
    }

    static public boolean isPunctuation(String pos) {
        return pos.equals(POS_COLON) || pos.equals(POS_COMMA) || pos.equals(POS_PERIOD) || pos.equals(POS_NFP) || pos.equals(POS_HYPH) || pos.equals(POS_SYM)
                || pos.equals(POS_LDQ) || pos.equals(POS_RDQ) || pos.equals(POS_LRB) || pos.equals(POS_RRB);
    }

    static public boolean isSentence(String pos) {
        return pos.equals(POS_S) || pos.equals(POS_SBAR) || pos.equals(POS_SBARQ) || pos.equals(POS_SINV) || pos.equals(POS_SQ);
    }

    static public boolean isBe(String form) {
        return form.matches("be|been|being|am|is|was|are|were|'m|'s|'re");
    }

    static public boolean isHave(String form) {
        return form.matches("have|has|had|having|'ve|'d");
    }

    static public boolean isDo(String form) {
        return form.matches("do|does|did|done|doing");
    }

    static public boolean isGet(String form) {
        return form.equals("get") || form.equals("gets") || form.equals("got") || form.equals("gotten") || form.equals("getting");
    }

    static public boolean isBecome(String form) {
        return form.matches("become|becomes|became");
    }

    static public boolean isAux(String form) {
        return isBe(form) || isHave(form) || isDo(form);
    }

    static public boolean isLightVerb(String form) {
        return form.equals("take") || form.equals("takes") || form.equals("took") || form.equals("taken") || form.equals("taking")
                || form.equals("give") || form.equals("gives") || form.equals("gave") || form.equals("given") || form.equals("giving")
                || form.equals("make") || form.equals("makes") || form.equals("made") || form.equals("making")
                || form.equals("do") || form.equals("does") || form.equals("did") || form.equals("done") || form.equals("doing")
                || form.equals("have") || form.equals("has") || form.equals("had") || form.equals("having");
    }

    static public boolean isComplementizer(String form) {
        form = form.toLowerCase();

        return form.matches("what|when|where|which|who|whom|whose|why|how|that");
    }
}