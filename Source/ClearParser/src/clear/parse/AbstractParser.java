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
package clear.parse;

import clear.ftr.FtrLib;
import clear.ftr.map.AbstractFtrMap;
import clear.ftr.xml.AbstractFtrXml;
import clear.ftr.xml.FtrTemplate;
import clear.ftr.xml.FtrToken;
import clear.reader.AbstractReader;
import clear.util.tuple.JObjectObjectTuple;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import java.util.ArrayList;

/**
 * Abstract parser.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2011
 */
public abstract class AbstractParser {

    /**
     * Print transitions
     */
    static public final byte FLAG_PRINT_TRANSITION = 0;
    /**
     * Train lexica
     */
    static public final byte FLAG_TRAIN_LEXICON = 1;
    /**
     * Train instances
     */
    static public final byte FLAG_TRAIN_INSTANCE = 2;
    /**
     * Train using boosting
     */
    static public final byte FLAG_TRAIN_BOOST = 3;
    /**
     * Predict
     */
    static public final byte FLAG_PREDICT = 4;
    /**
     * Predict using k-best ranking
     */
    static public final byte FLAG_PREDICT_BEST = 5;
    /**
     * {@link AbstractParser#FLAG_*}
     */
    protected byte i_flag;
    /**
     * Language
     */
    protected String s_language = AbstractReader.LANG_EN;
    /**
     * Training instances
     */
    public ArrayList<JObjectObjectTuple<IntArrayList, ArrayList<int[]>>> a_trans;

    /**
     * @param language {@link AbstractReader#LANG_*}
     */
    public void setLanguage(String language) {
        s_language = language;
    }

    /**
     * Initializes arrays to save training instances.
     */
    protected void initTrainArrays(int size) {
        a_trans = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            addTrainArrays();
        }
    }

    public void addTrainArrays() {
        a_trans.add(new JObjectObjectTuple<>(new IntArrayList(), new ArrayList<int[]>()));
    }

    /**
     * Saves a training instance.
     */
    protected void saveInstance(String label, IntArrayList ftr, AbstractFtrMap tmap, int trainIndex) {
        int index = tmap.labelToIndex(label);
        if (index < 0) {
            return;
        }

        JObjectObjectTuple<IntArrayList, ArrayList<int[]>> yx = a_trans.get(trainIndex);
        int[] ftrArr = ftr.toArray();

        //	System.err.println(label+" "+index+" "+ftr);
        yx.o1.add(index);
        yx.o2.add(ftrArr);
    }

    protected boolean existInstance(JObjectObjectTuple<IntArrayList, ArrayList<int[]>> yx, int index, int[] ftrArr) {
        int i, size = yx.o1.size();

        for (i = 0; i < size; i++) {
            if (yx.o1.get(i) == index && equals(yx.o2.get(i), ftrArr)) {
                return true;
            }
        }

        return false;
    }

    protected boolean equals(int[] x1, int[] x2) {
        if (x1.length != x2.length) {
            return false;
        }

        for (int i = 0; i < x1.length; i++) {
            if (x1[i] != x2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add n-gram lexica.
     */
    protected void addNgramLexica(AbstractFtrXml txml, AbstractFtrMap tmap) {
        FtrTemplate[][] templates = txml.a_ngram_templates;
        FtrTemplate[] template;
        int i, j, n, m = templates.length;
        String ftr;

        for (j = 0; j < m; j++) {
            template = templates[j];
            n = template.length;

            for (i = 0; i < n; i++) {
                if ((ftr = getFeature(template[i])) != null) {
                    tmap.addNgram(j, ftr);
                }
            }
        }
    }

    /**
     * Adds n-gram features.
     */
    protected void addNgramFeatures(IntArrayList arr, int[] idx, AbstractFtrXml txml, AbstractFtrMap tmap) {
        FtrTemplate[][] templates = txml.a_ngram_templates;
        FtrTemplate[] template;
        int i, j, n, m = templates.length, size, value;
        ObjectIntOpenHashMap<String> map;
        String ftr;

        for (j = 0; j < m; j++) {
            map = tmap.getNgramHashMap(j);
            size = tmap.n_ngram[j];

            template = templates[j];
            n = template.length;

            for (i = 0; i < n; i++) {
                if ((ftr = getFeature(template[i])) != null) {
                    value = map.get(ftr);
                    if (value > 0) {
                        arr.add(idx[0] + value - 1);
                    }
                }

                idx[0] += size;
            }
        }
    }

    /**
     * @return feature value.
     */
    protected String getFeature(FtrTemplate ftr) {
        StringBuilder build = new StringBuilder();
        int i, n = ftr.tokens.length;
        String field;

        for (i = 0; i < n; i++) {
            field = getField(ftr.tokens[i]);
            if (field == null) {
                return null;
            }

            if (i > 0) {
                build.append(FtrLib.TAG_DELIM);
            }
            build.append(field);
        }

        return build.toString();
    }

    abstract protected String getField(FtrToken token);
}