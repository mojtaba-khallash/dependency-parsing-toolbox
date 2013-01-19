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
package clear.morph;

import clear.pos.PosEnLib;
import clear.util.tuple.JObjectObjectTuple;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * English morphological analyzer.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/4/2010
 */
public class MorphEnAnalyzer {

    final public String FIELD_DELIM = "_";
    final public String NOUN_EXC = "noun.exc";
    final public String VERB_EXC = "verb.exc";
    final public String ADJ_EXC = "adj.exc";
    final public String ADV_EXC = "adv.exc";
    final public String NOUN_BASE = "noun.txt";
    final public String VERB_BASE = "verb.txt";
    final public String ADJ_BASE = "adj.txt";
    final public String ORD_BASE = "ordinal.txt";
    final public String NOUN_RULE = "noun.rule";
    final public String VERB_RULE = "verb.rule";
    final public String ADJ_RULE = "adj.rule";
    final public String ABBR_RULE = "abbr.rule";
    /**
     * Noun exceptions
     */
    HashMap<String, String> m_noun_exc;
    /**
     * Verb exceptions
     */
    HashMap<String, String> m_verb_exc;
    /**
     * Adjective exceptions
     */
    HashMap<String, String> m_adj_exc;
    /**
     * Adverb exceptions
     */
    HashMap<String, String> m_adv_exc;
    /**
     * Noun base-forms
     */
    HashSet<String> s_noun_base;
    /**
     * Verb base-forms
     */
    HashSet<String> s_verb_base;
    /**
     * Adjective base-forms
     */
    HashSet<String> s_adj_base;
    /**
     * Ordinal forms
     */
    HashSet<String> s_ord_base;
    /**
     * Noun detachment rules
     */
    ArrayList<JObjectObjectTuple<String, String>> a_noun_rule;
    /**
     * Verb detachment rules
     */
    ArrayList<JObjectObjectTuple<String, String>> a_verb_rule;
    /**
     * Adjective detachment rules
     */
    ArrayList<JObjectObjectTuple<String, String>> a_adj_rule;
    /**
     * Abbreviation replacement rules
     */
    HashMap<String, String> m_abbr_rule;

    /**
     * Calls {@link MorphEnAnalyzer#init}.
     *
     * @param zipFile "en_dict.jar"
     */
    public MorphEnAnalyzer(String zipFile) {
        try {
            InputStream inputStream = new FileInputStream(zipFile);
            try {
                this.init(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MorphEnAnalyzer(URL zipFileURL) throws IOException {
        InputStream inputStream = zipFileURL.openStream();
        try {
            this.init(inputStream);
        } finally {
            inputStream.close();
        }
    }

    public MorphEnAnalyzer(InputStream inputStream) throws IOException {
        this.init(inputStream);
    }

    /**
     * Initializes a morphological analyzer.
     *
     * @param inputStream "en_dict.jar"
     */
    public void init(InputStream inputStream) throws IOException {
        ZipInputStream zin = new ZipInputStream(inputStream);
        ZipEntry zEntry;
        String filename;

        while ((zEntry = zin.getNextEntry()) != null) {
            filename = zEntry.getName();
            switch (filename) {
                case NOUN_EXC:
                    m_noun_exc = getExcecptionMap(zin);
                    break;
                case VERB_EXC:
                    m_verb_exc = getExcecptionMap(zin);
                    break;
                case ADJ_EXC:
                    m_adj_exc = getExcecptionMap(zin);
                    break;
                case ADV_EXC:
                    m_adv_exc = getExcecptionMap(zin);
                    break;
                case NOUN_BASE:
                    s_noun_base = getBaseSet(zin);
                    break;
                case VERB_BASE:
                    s_verb_base = getBaseSet(zin);
                    break;
                case ADJ_BASE:
                    s_adj_base = getBaseSet(zin);
                    break;
                case ORD_BASE:
                    s_ord_base = getBaseSet(zin);
                    break;
                case NOUN_RULE:
                    a_noun_rule = getRuleList(zin);
                    break;
                case VERB_RULE:
                    a_verb_rule = getRuleList(zin);
                    break;
                case ADJ_RULE:
                    a_adj_rule = getRuleList(zin);
                    break;
                case ABBR_RULE:
                    m_abbr_rule = getAbbreviationMap(zin);
                    break;
            }
        }

        zin.close();
    }

    /**
     * @return HashMap taking exceptions as keys and their base-forms as values.
     */
    private HashMap<String, String> getExcecptionMap(ZipInputStream zin) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader fin = new BufferedReader(new InputStreamReader(zin));

        StringTokenizer tok;
        String line, exc, base;

        while ((line = fin.readLine()) != null) {
            tok = new StringTokenizer(line);
            exc = (tok.hasMoreTokens()) ? tok.nextToken() : null;
            base = (tok.hasMoreTokens()) ? tok.nextToken() : null;

            if (exc != null && base != null) {
                map.put(exc, base);
                while (tok.hasMoreTokens()) {
                    map.put(tok.nextToken(), base);
                }
            }
        }

        return map;
    }

    /**
     * @return HashSet containing base-forms.
     */
    private HashSet<String> getBaseSet(ZipInputStream zin) throws IOException {
        HashSet<String> set = new HashSet<>();
        BufferedReader fin = new BufferedReader(new InputStreamReader(zin));
        String line;

        while ((line = fin.readLine()) != null) {
            set.add(line.trim());
        }

        return set;
    }

    /**
     * @return List containing rules.
     */
    private ArrayList<JObjectObjectTuple<String, String>> getRuleList(ZipInputStream zin) throws IOException {
        ArrayList<JObjectObjectTuple<String, String>> list = new ArrayList<>();
        BufferedReader fin = new BufferedReader(new InputStreamReader(zin));

        StringTokenizer tok;
        String line, str0, str1;

        while ((line = fin.readLine()) != null) {
            tok = new StringTokenizer(line);
            str0 = tok.nextToken();
            str1 = (tok.hasMoreTokens()) ? tok.nextToken() : "";

            list.add(new JObjectObjectTuple<>(str0, str1));
        }

        return list;
    }

    /**
     * @return HashMap taking (abbreviation and pos-tag) as the key and its
     * base-form as the value.
     */
    private HashMap<String, String> getAbbreviationMap(ZipInputStream zin) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader fin = new BufferedReader(new InputStreamReader(zin));

        StringTokenizer tok;
        String line, abbr, pos, key, base;

        while ((line = fin.readLine()) != null) {
            tok = new StringTokenizer(line);
            abbr = tok.nextToken();
            pos = tok.nextToken();
            key = abbr + FIELD_DELIM + pos;
            base = tok.nextToken();

            map.put(key, base);
        }

        return map;
    }

    /**
     * Returns the lemma of the form using the pos-tag.
     *
     * @param form word-form
     * @param pos pos-tag
     */
    public String getLemma(String form, String pos) {
        form = form.toLowerCase();

        // exceptions
        String morphem = getException(form, pos);
        if (morphem != null) {
            return morphem;
        }

        // base-forms
        morphem = getBase(form, pos);
        if (morphem != null) {
            return morphem;
        }

        // abbreviations
        morphem = getAbbreviation(form, pos);
        if (morphem != null) {
            return morphem;
        }

        // numbers
        morphem = getNumber(form, pos);
        if (morphem != null) {
            return morphem;
        }

        return form;
    }

    /**
     * Returns the base form of the form considered to be an exception. If the
     * form is not an exception, returns null.
     *
     * @param form word-form
     * @param pos pos-tag
     */
    private String getException(String form, String pos) {
        if (PosEnLib.isNoun(pos)) {
            return m_noun_exc.get(form);
        } else if (PosEnLib.isVerb(pos)) {
            return m_verb_exc.get(form);
        } else if (PosEnLib.isAdjective(pos)) {
            return m_adj_exc.get(form);
        } else if (PosEnLib.isAdverb(pos)) {
            return m_adv_exc.get(form);
        }

        return null;
    }

    /**
     * Returns the base-form of the form. If there is no base-form, returns
     * null.
     *
     * @param form word-form
     * @param pos pos-tag
     */
    private String getBase(String form, String pos) {
        if (PosEnLib.isNoun(pos)) {
            return getBaseAux(form, s_noun_base, a_noun_rule);
        }
        if (PosEnLib.isVerb(pos)) {
            return getBaseAux(form, s_verb_base, a_verb_rule);
        }
        if (PosEnLib.isAdjective(pos)) {
            return getBaseAux(form, s_adj_base, a_adj_rule);
        }

        return null;
    }

    /**
     * Returns the base-form of the form. If there is no base-form, returns
     * null.
     *
     * @param form word-form
     * @param set set containing base-forms
     * @param rule list containing detachment rules
     */
    private String getBaseAux(String form, HashSet<String> set, ArrayList<JObjectObjectTuple<String, String>> rule) {
        int offset;
        String base;

        for (JObjectObjectTuple<String, String> tup : rule) {
            if (form.endsWith(tup.o1)) {
                offset = form.length() - tup.o1.length();
                base = form.substring(0, offset) + tup.o2;

                if (set.contains(base)) {
                    return base;
                }
            }
        }

        return null;
    }

    /**
     * Returns the base form of the form considered to be an abbreviation. If
     * the form is not an abbreviation, returns null.
     *
     * @param form word-form
     * @param pos pos-tag
     */
    private String getAbbreviation(String form, String pos) {
        String key = form + FIELD_DELIM + pos;

        return m_abbr_rule.get(key);
    }

    /**
     * Returns a simplified form of numbers.
     *
     * @param form word-form
     * @param pos pos-tag
     */
    public String getNumber(String form, String pos) {
        if (s_ord_base.contains(form)) {
            return "$#ORD#$";
        }

        String currStr = getNormalizedNumber(form);

        if (currStr.equals("0st") || currStr.equals("0nd") || currStr.equals("0rd") || currStr.equals("0th")) {
            return "$#ORD#$";
        }

        return (currStr.equals(form)) ? null : currStr;
    }

    static public String getNormalizedNumber(String form) {
        String prevStr = "", currStr = form;

        while (!prevStr.equals(currStr)) {
            prevStr = currStr;

            currStr = currStr.replaceAll("\\d%", "0");
            currStr = currStr.replaceAll("\\$\\d", "0");
            currStr = currStr.replaceAll("\\.\\d", "0");
            currStr = currStr.replaceAll(",\\d", "0");
            currStr = currStr.replaceAll(":\\d", "0");
            //	currStr = currStr.replaceAll("-\\d", "0");
            //	currStr = currStr.replaceAll("\\\\/\\d", "0");
        }

        return currStr.replaceAll("\\d+", "0");
    }

    static public String getAbbrVerb(String form, String pos) {
        if (form.equals("'d") && pos.equals("MD")) {
            return "would";
        }
        if (form.equals("'ll") && pos.equals("MD")) {
            return "will";
        }
        if (form.equals("'m") && pos.equals("VBP")) {
            return "be";
        }
        if (form.equals("'re") && pos.equals("VBP")) {
            return "be";
        }
        if (form.equals("'ve") && pos.equals("VB")) {
            return "have";
        }
        if (form.equals("'ve") && pos.equals("VBP")) {
            return "have";
        }
        if (form.equals("'d") && pos.equals("VBD")) {
            return "have";
        }

        return form;
    }
}