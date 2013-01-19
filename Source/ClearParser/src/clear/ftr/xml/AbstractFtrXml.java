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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Abstract feature template.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2011
 */
abstract public class AbstractFtrXml {

    static protected final String TEMPLATE = "feature_template";
    static protected final String CUTOFF = "cutoff";
    static protected final String LABEL = "label";
    /**
     * N-gram feature
     */
    static protected final String NGRAM = "ngram";
    /**
     * Extra feature
     */
    static protected final String EXTRA = "extra";
    /**
     * Number of tokens
     */
    static protected final String N = "n";
    /**
     * Cutoff (>= 0)
     */
    static protected final String C = "c";
    /**
     * Type (e.g., "pp", "ump")
     */
    static protected final String T = "t";
    /**
     * Discrete field (e.g., "f", "m", "p", "d")
     */
    static protected final String F = "f";
    /**
     * "true" | "false"
     */
    static protected final String VISIBLE = "visible";
    /**
     * Field delimiter (e.g., l+1.f)
     */
    static protected final String DELIM_F = ":";
    /**
     * Relation delimiter (e.g., l_hd)
     */
    static protected final String DELIM_R = "_";
    /**
     * N-gram feature [type][templates]
     */
    public FtrTemplate[][] a_ngram_templates;
    public int n_cutoff_label;
    public int n_cutoff_ngram;
    public int n_cutoff_extra;

    public AbstractFtrXml(String featureXml) {
        try {
            init(new FileInputStream(featureXml));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AbstractFtrXml(InputStream fin) {
        init(fin);
    }

    public void init(InputStream fin) {
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = dFactory.newDocumentBuilder();
            Document doc = builder.parse(fin);

            initCutoffs(doc);
            initNgrams(doc);
            initFeatures(doc);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Initializes cutoffs.
     */
    protected void initCutoffs(Document doc) throws Exception {
        NodeList eList = doc.getElementsByTagName(CUTOFF);
        if (eList.getLength() <= 0) {
            return;
        }
        Element eCutoff = (Element) eList.item(0);

        n_cutoff_label = (eCutoff.hasAttribute(LABEL)) ? Integer.parseInt(eCutoff.getAttribute(LABEL)) : 0;
        n_cutoff_ngram = (eCutoff.hasAttribute(NGRAM)) ? Integer.parseInt(eCutoff.getAttribute(NGRAM)) : 0;
        n_cutoff_extra = (eCutoff.hasAttribute(EXTRA)) ? Integer.parseInt(eCutoff.getAttribute(EXTRA)) : 0;
    }

    protected void initNgrams(Document doc) throws Exception {
        NodeList eList = doc.getElementsByTagName(NGRAM);
        HashMap<String, ArrayList<FtrTemplate>> map = new HashMap<>();

        int i, n = eList.getLength();
        Element eFeature;

        for (i = 0; i < n; i++) {
            eFeature = (Element) eList.item(i);
            if (eFeature.getAttribute(VISIBLE).trim().equals("false")) {
                continue;
            }

            FtrTemplate ftr = getFtrTemplate(eFeature);

            if (map.containsKey(ftr.type)) {
                map.get(ftr.type).add(ftr);
            } else {
                ArrayList<FtrTemplate> list = new ArrayList<>();
                map.put(ftr.type, list);
                list.add(ftr);
            }
        }

        n = map.size();
        a_ngram_templates = new FtrTemplate[n][];
        ArrayList<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);

        for (i = 0; i < n; i++) {
            ArrayList<FtrTemplate> list = map.get(keys.get(i));
            FtrTemplate[] arr = new FtrTemplate[list.size()];
            list.toArray(arr);
            a_ngram_templates[i] = arr;
        }
    }

    /**
     * Convert the element to {@link FtrTemplate}.
     */
    protected FtrTemplate getFtrTemplate(Element eFeature) {
        int nToken = Integer.parseInt(eFeature.getAttribute(N));
        int cutoff = (eFeature.hasAttribute(C)) ? Integer.parseInt(eFeature.getAttribute(C)) : 0;

        FtrTemplate ftr = new FtrTemplate(nToken, cutoff);
        StringBuilder build = new StringBuilder();

        int i;
        String type;

        for (i = 0; i < nToken; i++) {
            FtrToken tok = getFtrToken(eFeature.getAttribute(F + i));
            ftr.addFtrToken(i, tok);
            build.append(tok.field);
        }

        if (eFeature.hasAttribute(T)) {
            type = eFeature.getAttribute(T).trim();
        } else {
            type = build.toString();
        }

        ftr.setType(type);
        return ftr;
    }

    /**
     * @param ftr (e.g., "l.f", "l+1.m", "l-1.p", "l0_hd.d")
     */
    protected FtrToken getFtrToken(String ftr) {
        String[] aField = ftr.split(DELIM_F);	// {"l-1_hd", "p"}
        String[] aRelation = aField[0].split(DELIM_R);	// {"l-1", "hd"} 

        char source = aRelation[0].charAt(0);
        if (!validSource(source)) {
            xmlError(ftr);
        }

        int offset = 0;
        if (aRelation[0].length() >= 2) {
            if (aRelation[0].charAt(1) == '+') {
                offset = Integer.parseInt(aRelation[0].substring(2));
            } else {
                offset = Integer.parseInt(aRelation[0].substring(1));
            }
        }

        String relation = null;
        if (aRelation.length > 1) {
            relation = aRelation[1];
            if (!validRelation(relation)) {
                xmlError(ftr);
            }
        }

        String field = aField[1];
        if (!validField(field)) {
            xmlError(ftr);
        }

        return new FtrToken(source, offset, relation, field);
    }

    /**
     * Prints system error and exits.
     */
    protected void xmlError(String error) {
        System.err.println("Invalid feature: " + error);
        System.exit(1);
    }

    /**
     * Initializes other kinds of features.
     */
    abstract protected void initFeatures(Document doc) throws Exception;

    abstract protected boolean validSource(char source);

    abstract protected boolean validRelation(String relation);

    abstract protected boolean validField(String filed);

    protected void toStringAux(StringBuilder build, String type, FtrTemplate ftr) {
        build.append("    <");
        build.append(type);
        build.append(" ");
        build.append(ftr.toString());
        build.append("/>\n");
    }
}