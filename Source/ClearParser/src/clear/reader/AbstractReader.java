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
package clear.reader;

import clear.dep.ITree;
import clear.util.IOUtil;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Abstract reader.
 *
 * @author Jinho D. Choi <b>Last update:</b> 6/26/2010
 */
abstract public class AbstractReader<NodeType, TreeType> {

    /**
     * Flag for Chinese
     */
    static public final String LANG_CH = "ch";
    /**
     * Flag for English
     */
    static public final String LANG_EN = "en";
    /**
     * Flag for Hindi
     */
    static public final String LANG_HI = "hi";
    /**
     * Flag for Czech
     */
    static public final String LANG_CZ = "cz";
    /**
     * Flag for Korean
     */
    static public final String LANG_KR = "kr";
    /**
     * Flag for raw format
     */
    static public final String FORMAT_RAW = "raw";
    /**
     * Flag for tok format
     */
    static public final String FORMAT_TOK = "tok";
    /**
     * Flag for part-of-speech format
     */
    static public final String FORMAT_POS = "pos";
    /**
     * Flag for dependency format
     */
    static public final String FORMAT_DEP = "dep";
    /**
     * Flag for semantic-role labeling format
     */
    static public final String FORMAT_SRL = "srl";
    /**
     * Flag for CoNLL-X format
     */
    static public final String FORMAT_CONLLX = "conllx";
    /**
     * Flag for dependency-verbnet format
     */
    static public final String FORMAT_DEPV = "depv";
    /**
     * Delimiter between fields
     */
    static public final String FIELD_DELIM = "\t";
    /**
     * Value for empty fields
     */
    static public final String EMPTY_FIELD = "_";
    /**
     * File reader
     */
    protected BufferedReader f_in;
    /**
     * Language code
     */
    protected String s_language = LANG_EN;

    /**
     * The constructor calls {@link AbstractReader#open(String)}.
     *
     * @param filename name of the file to read
     */
    public AbstractReader(String filename) {
        open(filename);
    }

    /**
     * Opens the abstract reader for
     * <code>filename</code>.
     *
     * @param filename name of the file to read
     */
    public void open(String filename) {
        f_in = IOUtil.createBufferedFileReader(filename);
    }

    /**
     * Closes the abstract reader.
     */
    public void close() {
        try {
            f_in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLanguage(String language) {
        s_language = language;
    }

    /**
     * @return true if
     * <code>line</code> is empty
     */
    protected boolean isSkip(String line) {
        //	return line.startsWith(";") || line.trim().isEmpty();
        return line.trim().isEmpty();
    }

    /**
     * Appends the next tree to
     * <code>tree</code>.
     * <code>tree</code> may or may not already contain nodes.
     *
     * @return true if the next tree exists
     * @throws IOException
     */
    protected boolean appendNextTree(ITree<NodeType> tree) throws IOException {
        // skip empty lines
        String line;
        while ((line = f_in.readLine()) != null) {
            if (!isSkip(line)) {
                break;
            }
        }

        // the end of the line
        if (line == null) {
            close();
            return false;
        }

        // add nodes
        int id = tree.size();
        tree.add(toNode(line, id++));

        while ((line = f_in.readLine()) != null) {
            if (isSkip(line)) {
                return true;
            } else {
                tree.add(toNode(line, id++));
            }
        }

        return true;
    }

    abstract public TreeType nextTree();

    /**
     * @param line string of values
     * @param id token ID of the node
     * @return node containing values from
     * <code>line</code>.
     */
    abstract protected NodeType toNode(String line, int id);
}