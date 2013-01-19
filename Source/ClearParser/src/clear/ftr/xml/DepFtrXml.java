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
package clear.ftr.xml;

import java.io.InputStream;
import java.util.regex.Pattern;
import org.w3c.dom.Document;

/**
 * Dependency feature template.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2011
 */
public class DepFtrXml extends AbstractFtrXml {

    static public final char LAMBDA = 'l';
    static public final char BETA = 'b';
    static public final String R_HD = "hd";		// head
    static public final String R_LM = "lm";		// leftmost  dependent
    static public final String R_RM = "rm";		// rightmost dependent
    static public final String R_LS = "ls";		// left  sibling
    static public final String R_RS = "rs";		// right sibling
    static public final String R_VC = "vc";		// highest verb chain
    static public final String F_FORM = "f";
    static public final String F_LEMMA = "m";
    static public final String F_POS = "p";
    static public final String F_DEPREL = "d";
    static public final Pattern P_REL = Pattern.compile(R_HD + "|" + R_LM + "|" + R_RM + "|" + R_LS + "|" + R_RS + "|" + R_VC);
    static public final Pattern P_FIELD = Pattern.compile(F_FORM + "|" + F_LEMMA + "|" + F_POS + "|" + F_DEPREL);
    static public final Pattern P_FEAT = Pattern.compile("^ft=(.+)$");
    static public final Pattern P_TRANS = Pattern.compile("^tr(\\d+)$");	// transition
    static public final Pattern P_KR = Pattern.compile("^kr(.)(\\d*)$");

    public DepFtrXml(String featureXml) {
        super(featureXml);
    }

    public DepFtrXml(InputStream fin) {
        super(fin);
    }

    @Override
    protected void initFeatures(Document doc) throws Exception {
    }

    @Override
    protected boolean validSource(char token) {
        return token == LAMBDA || token == BETA;
    }

    @Override
    protected boolean validRelation(String relation) {
        return P_REL.matcher(relation).matches();
    }

    @Override
    protected boolean validField(String field) {
        return P_FIELD.matcher(field).matches()
                || P_FEAT.matcher(field).matches()
                || P_TRANS.matcher(field).matches()
                || P_KR.matcher(field).matches();
    }
}