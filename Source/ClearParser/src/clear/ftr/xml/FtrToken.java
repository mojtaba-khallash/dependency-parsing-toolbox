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
 * Feature token.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/4/2010
 */
public class FtrToken {

    /**
     * Source of this feature (e.g., l: lambda, b: beta)
     */
    public char source;
    /**
     * Offset from {@link FtrToken#source}[0] (e.g., -1, 0, 1)
     */
    public int offset;
    /**
     * Relation to {@link FtrToken#source}[0] (e.g., hd, lm, rm)
     */
    public String relation;
    /**
     * Field of this feature (e.g., f, m, p, d)
     */
    public String field;

    public FtrToken(char source, int offset, String relation, String field) {
        set(source, offset, relation, field);
    }

    public void set(char source, int offset, String relation, String field) {
        this.source = source;
        this.offset = offset;
        this.relation = relation;
        this.field = field;
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();

        build.append(source);

        if (offset != 0) {
            build.append(offset);
        }

        if (relation != null) {
            build.append(AbstractFtrXml.DELIM_R);
            build.append(relation);
        }

        build.append(AbstractFtrXml.DELIM_F);
        build.append(field);

        return build.toString();
    }

    public boolean isField(String str) {
        return field.equals(str);
    }

    public boolean isRelation(String str) {
        return relation.equals(str);
    }
}