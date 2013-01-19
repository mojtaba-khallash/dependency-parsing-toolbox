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
package clear.ftr.map;

import clear.ftr.xml.AbstractFtrXml;
import clear.util.IOUtil;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Abstract feature map.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2010
 */
abstract public class AbstractFtrMap {

    /**
     * List of labels.
     */
    protected ArrayList<String> a_label;
    /**
     * Takes "label" as a key and its index as a value.
     */
    protected ObjectIntOpenHashMap<String> m_label;
    /**
     * Contains n-gram features.
     */
    protected ArrayList<ObjectIntOpenHashMap<String>> m_ngram;
    /**
     * Size of each n-gram feature.
     */
    public int[] n_ngram;
    /**
     * Takes "feature" as a key and its index as a value.
     */
    protected ArrayList<ObjectIntOpenHashMap<String>> m_extra;
    /**
     * Size of each feature.
     */
    public int[] n_extra;

//	=========================== Init ===========================
    public AbstractFtrMap(AbstractFtrXml xml, int nExtra) {
        init(xml, nExtra);
    }

    protected void init(AbstractFtrXml xml, int nExtra) {
        int i, nNgram = xml.a_ngram_templates.length;

        m_label = new ObjectIntOpenHashMap<>();
        m_ngram = new ArrayList<>(nNgram);
        m_extra = new ArrayList<>(nExtra);

        for (i = 0; i < nNgram; i++) {
            m_ngram.add(new ObjectIntOpenHashMap<String>());
        }

        for (i = 0; i < nExtra; i++) {
            m_extra.add(new ObjectIntOpenHashMap<String>());
        }
    }

    public void addLabel(String label) {
        incrementKey(m_label, label);
    }

    public void addNgram(int index, String ftr) {
        incrementKey(m_ngram.get(index), ftr);
    }

    public void addExtra(int index, String ftr) {
        incrementKey(m_extra.get(index), ftr);
    }

    protected void incrementKey(ObjectIntOpenHashMap<String> map, String key) {
        map.put(key, map.get(key) + 1);
    }

//	=========================== Save ===========================
    public void save(AbstractFtrXml xml, String lexiconFile) {
        try {
            PrintStream fout = IOUtil.createPrintFileStream(lexiconFile);
            saveDefault(xml, fout);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(AbstractFtrXml xml, PrintStream fout) {
        try {
            saveDefault(xml, fout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void saveDefault(AbstractFtrXml xml, PrintStream fout) {
        int i, n;

        // labels
        saveHashMap(fout, m_label, xml.n_cutoff_label);

        // n-grams features
        n = m_ngram.size();
        fout.println(n);

        for (i = 0; i < n; i++) {
            saveHashMap(fout, m_ngram.get(i), xml.n_cutoff_ngram);
        }

        // extra features
        n = m_extra.size();
        fout.println(n);

        for (i = 0; i < n; i++) {
            saveHashMap(fout, m_extra.get(i), xml.n_cutoff_extra);
        }
    }

    protected void saveHashMap(PrintStream fout, ObjectIntOpenHashMap<String> map, int cutoff) {
        String key;
        int value;
        fout.println(countKeys(map, cutoff));

        for (ObjectCursor<String> str : map.keys()) {
            key = str.value;
            value = map.get(key);
            if (value > cutoff) {
                fout.println(key);
            }
        }
    }

    protected void saveFreqMap(PrintStream fout, ObjectIntOpenHashMap<String> map, int cutoff) {
        String key;
        int value;
        fout.println(countKeys(map, cutoff));

        for (ObjectCursor<String> str : map.keys()) {
            key = str.value;
            value = map.get(key);
            if (value > cutoff) {
                fout.println(key + " " + value);
            }
        }
    }

    protected int countKeys(ObjectIntOpenHashMap<String> map, int cutoff) {
        if (cutoff < 1) {
            return map.size();
        }
        int count = 0, value;

        for (ObjectCursor<String> key : map.keys()) {
            value = map.get(key.value);
            if (value > cutoff) {
                count++;
            }
        }

        return count;
    }

//	=========================== Load ===========================
    public AbstractFtrMap(String lexiconFile) {
        load(lexiconFile);
    }

    public AbstractFtrMap(BufferedReader fin) {
        load(fin);
    }

    public void load(String lexiconFile) {
        try {
            BufferedReader fin = IOUtil.createBufferedFileReader(lexiconFile);
            loadDefault(fin);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void load(BufferedReader fin) {
        try {
            loadDefault(fin);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected void loadDefault(BufferedReader fin) throws Exception {
        ObjectIntOpenHashMap<String> map;
        int n, i;
        String key;

        // labels
        n = Integer.parseInt(fin.readLine());
        a_label = new ArrayList<>(n);
        m_label = new ObjectIntOpenHashMap<>(n);

        for (i = 1; i <= n; i++) {
            key = fin.readLine();
            a_label.add(key);
            m_label.put(key, i);
        }

        // n-grams
        n = Integer.parseInt(fin.readLine());
        m_ngram = new ArrayList<>(n);
        n_ngram = new int[n];

        for (i = 0; i < n; i++) {
            map = loadHashMap(fin);
            m_ngram.add(map);
            n_ngram[i] = map.size();
        }

        // extra features
        n = Integer.parseInt(fin.readLine());
        m_extra = new ArrayList<>(n);
        n_extra = new int[n];

        for (i = 0; i < n; i++) {
            map = loadHashMap(fin);
            m_extra.add(map);
            n_extra[i] = map.size();
        }
    }

    protected ObjectIntOpenHashMap<String> loadHashMap(BufferedReader fin) throws Exception {
        int i, n = Integer.parseInt(fin.readLine());
        ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<>(n);

        for (i = 1; i <= n; i++) // 0 is reserved for unseen feature
        {
            map.put(fin.readLine(), i);
        }

        return map;
    }

    protected ObjectIntOpenHashMap<String> loadFreqMap(BufferedReader fin) throws Exception {
        int i, n = Integer.parseInt(fin.readLine());
        ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<>(n);
        String[] tmp;

        for (i = 1; i <= n; i++) {
            tmp = fin.readLine().split(" ");
            map.put(tmp[0], Integer.parseInt(tmp[1]));
        }

        return map;
    }

    public String indexToLabel(int index) {
        return a_label.get(index);
    }

    public int labelToIndex(String label) {
        return m_label.get(label) - 1;
    }

    public int ngramToIndex(int index, String ftr) {
        return m_ngram.get(index).get(ftr) - 1;
    }

    public int extraToIndex(int index, String ftr) {
        return m_extra.get(index).get(ftr) - 1;
    }

    public ObjectIntOpenHashMap<String> getNgramHashMap(int index) {
        return m_ngram.get(index);
    }
}