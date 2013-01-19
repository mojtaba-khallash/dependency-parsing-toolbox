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

/**
 * Feature template.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/4/2010
 */
public class FtrTemplate {

    public FtrToken[] tokens;
    /**
     * Type of this feature template (e.g. "p", "mp", "ppp")
     */
    public String type;
    public int cutoff;

    public FtrTemplate(int n, int cutoff) {
        init(n, cutoff);
    }

    public void init(int n, int cutoff) {
        this.tokens = new FtrToken[n];
        this.cutoff = cutoff;
    }

    public void addFtrToken(int index, FtrToken token) {
        tokens[index] = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        toStringAux(build, AbstractFtrXml.N, Integer.toString(tokens.length));

        build.append(" ");
        toStringAux(build, AbstractFtrXml.T, type);

        if (cutoff > 0) {
            build.append(" ");
            toStringAux(build, AbstractFtrXml.C, Integer.toString(cutoff));
        }

        for (int i = 0; i < tokens.length; i++) {
            build.append(" ");
            toStringAux(build, AbstractFtrXml.F + i, tokens[i].toString());
        }

        return build.toString();
    }

    private void toStringAux(StringBuilder build, String field, String value) {
        build.append(field);
        build.append("=\"");
        build.append(value);
        build.append("\"");
    }
}
