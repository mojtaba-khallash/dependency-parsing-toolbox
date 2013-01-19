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

/**
 * Semantic role labeling feature template.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2010
 */
public class SRLFtrXml extends DepFtrXml {

    static public final Pattern P_SUBCAT = Pattern.compile("^sc([" + F_POS + F_DEPREL + "])(\\d+)$");
    static public final Pattern P_PATH = Pattern.compile("^pt([" + F_POS + F_DEPREL + "])(\\d+)$");
    static public final Pattern P_ARGN = Pattern.compile("^argn(\\d+)$");

    public SRLFtrXml(String featureXml) {
        super(featureXml);
    }

    public SRLFtrXml(InputStream fin) {
        super(fin);
    }

    @Override
    protected boolean validField(String field) {
        return super.validField(field)
                || P_SUBCAT.matcher(field).matches()
                || P_PATH.matcher(field).matches()
                || P_ARGN.matcher(field).matches();
    }
}