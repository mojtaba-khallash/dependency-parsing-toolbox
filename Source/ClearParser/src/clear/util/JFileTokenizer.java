/**
 * Copyright (c) 2007, Regents of the University of Colorado All rights
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
package clear.util;

import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * FileTokenizer applies jdk standard string-tokenizer to the entire string
 * within a file. To create a file-tokenizer, you need to pass the name of a
 * file you want to tokenize. For example, if 'test.txt' file contains the text
 * below,
 * <pre>
 * Hi I'm Jinho!
 * File-tokenizer works great!
 *
 * here is how the file-tokenizer works.
 *
 * Code:
 * FileTokenizer tok = new FileTokenizer("test.txt");
 * while (tok.hasMoreTokens())
 *     System.out.println(tok.nextToken());
 *
 * Output:
 * Hi
 * I'm
 * Jinho!
 * File-tokenizer
 * works
 * great!
 * </pre>
 *
 * @see StringTokenizer
 * @author Jinho D. Choi <b>Last update:</b> 02/05/2010
 */
public class JFileTokenizer {

    /**
     * White spaces such as " \t\n\r\f"
     */
    static final public String WHITE = " \t\n\r\f";
    private Scanner mb_scan;
    private StringTokenizer mb_tok;
    private String mb_delim;
    private boolean mb_returnDelims;
    private int mb_numLines;

    /**
     * Initializes the file-tokenizer for a file
     * <code>filename</code>. The default delimiters are {@value
     * JFileTokenizer#WHITE}.
     *
     * @param filename name of the file to parse
     */
    public JFileTokenizer(String filename) {
        init(filename, WHITE, false);
    }

    /**
     * Initializes the file-tokenizer for a file
     * <code>filename</code>. The delimiters are specified as
     * <code>delim</code>.
     *
     * @param filename name of the file to parse
     * @param delim delimiters.
     */
    public JFileTokenizer(String filename, String delim) {
        init(filename, delim, false);
    }

    /**
     * Initializes the file-tokenizer for a file
     * <code>filename</code>. The delimiters are specified as
     * <code>delim</code>. If
     * <code>returnDelims</code> is true, it will return the delimiters as
     * tokens.
     *
     * @param filename name of the file to parse
     * @param delim delimiters
     * @param returnDelims flag indicating whether to return the delimiters as
     * tokens
     */
    public JFileTokenizer(String filename, String delim, boolean returnDelims) {
        init(filename, delim, returnDelims);
    }

    /**
     * initializes member variables.
     */
    private void init(String filename, String delim, boolean returnDelims) {
        mb_scan = IOUtil.createFileScanner(filename);
        mb_delim = delim;
        mb_returnDelims = returnDelims;
        mb_numLines = 0;

        initTokenizer();
    }

    /**
     * Returns true if there are more lines.
     */
    private boolean initTokenizer() {
        if (mb_scan.hasNextLine()) {
            mb_tok = new StringTokenizer(mb_scan.nextLine(), mb_delim, mb_returnDelims);
            mb_numLines++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if there are more tokens.
     */
    public boolean hasMoreTokens() {
        if (mb_tok.hasMoreTokens()) // more tokens exist
        {
            return true;
        } else if (initTokenizer()) // get more tokens from the next line
        {
            return hasMoreTokens();
        }

        return false;					// no token exists
    }

    /**
     * Returns the next token in line. If there is none, returns null.
     */
    public String nextToken() {
        if (hasMoreTokens()) {
            return mb_tok.nextToken();
        } else {
            return null;
        }
    }

    /**
     * Returns the line number that is being parsed.
     */
    public int getLineNumber() {
        return mb_numLines;
    }

    /**
     * Flushes all tokens in the current line.
     */
    public void flushLine() {
        while (mb_tok.hasMoreTokens()) {
            mb_tok.nextToken();
        }
    }

    /**
     * Closes the file-tokenizer.
     */
    public void close() {
        mb_scan.close();
    }
}