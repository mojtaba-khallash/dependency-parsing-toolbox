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
package clear.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Input/output utilities.
 *
 * @author Jinho D. Choi <b>Last update:</b> 6/30/2010
 */
public class IOUtil {

    /**
     * @return new BufferedReader(new InputStreamReader(new
     * FileInputStream(filename), "UTF-8"))
     */
    static public BufferedReader createBufferedFileReader(String filename) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reader;
    }

    /**
     * @return new BufferedReader(new InputStreamReader(new GZIPInputStream(new
     * FileInputStream(filename))))
     */
    static public BufferedReader createBufferedGZipFileReader(String filename) throws Exception {
        return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
    }

    /**
     * @return new Scanner({@link IOUtil#createBufferedFileReader(String)})
     */
    static public Scanner createFileScanner(String filename) {
        return new Scanner(createBufferedFileReader(filename));
    }

    /**
     * @return List containing strings in
     * <code>filename</code>
     */
    static public ArrayList<String> getArrayList(String filename) {
        ArrayList<String> arr = new ArrayList<>();

        try {
            BufferedReader fin = createBufferedFileReader(filename);
            String line;

            while ((line = fin.readLine()) != null) {
                arr.add(line.trim());
            }

            arr.trimToSize();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arr;
    }

    /**
     * @return HashSet containing strings in
     * <code>filename</code>
     */
    static public HashSet<String> getHashSet(String filename) {
        HashSet<String> set = new HashSet<>();

        try {
            BufferedReader fin = createBufferedFileReader(filename);
            String line;

            while ((line = fin.readLine()) != null) {
                set.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return set;
    }

    /**
     * @return HashMap whose keys are strings in
     * <code>filename</code> and values are sequential integers starting at
     * <code>beginId</code>
     */
    static public HashMap<String, Integer> getHashMap(String filename, int beginId) {
        HashMap<String, Integer> map = new HashMap<>();

        try {
            BufferedReader fin = createBufferedFileReader(filename);
            String line;

            for (int i = beginId; (line = fin.readLine()) != null; i++) {
                map.put(line.trim(), i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * @return HashMap whose keys are strings and values are integers in
     * <code>filename</code> delimited by
     * <code>delim</code>
     */
    static public HashMap<String, Integer> getTStringIntHashMap(String filename, String delim) {
        HashMap<String, Integer> map = new HashMap<>();

        try {
            BufferedReader fin = createBufferedFileReader(filename);
            String line;

            while ((line = fin.readLine()) != null) {
                String[] ls = line.split(delim);
                map.put(ls[0], Integer.parseInt(ls[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * @return new PrintStream(filename, "UTF-8")
     */
    static public PrintStream createPrintFileStream(String filename) {
        PrintStream fout = null;

        try {
            //	fout = new PrintStream(filename, "UTF-8");
            fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename), 65536), false, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fout;
    }

    /**
     * @return new PrintStream(new GZIPOutputStream(new
     * FileOutputStream(filename)))
     */
    static public PrintStream createPrintFileGzipStream(String filename) {
        PrintStream fout = null;

        try {
            fout = new PrintStream(new GZIPOutputStream(new FileOutputStream(filename)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fout;
    }

    /**
     * Prints strings in
     * <code>set</code> to
     * <code>outputFile</code>.
     */
    static public void printFile(HashSet<String> set, String outputFile) {
        PrintStream fout = createPrintFileStream(outputFile);
        for (String item : set) {
            fout.println(item);
        }
    }

    /**
     * Prints string keys in
     * <code>map</code> to
     * <code>outputFile</code> whose values are greater than
     * <code>cutoff</code>.
     */
    static public void printFile(HashMap<String, Integer> map, String outputFile, int cutoff) {
        PrintStream fout = createPrintFileStream(outputFile);

        for (String key : map.keySet()) {
            if (map.get(key) > cutoff) {
                fout.println(key);
            }
        }
    }

    static public void mergeFiles(String outputFile, String... mergeFiles) {
        PrintStream fout = IOUtil.createPrintFileStream(outputFile);
        String line;

        for (String filename : mergeFiles) {
            BufferedReader fin = IOUtil.createBufferedFileReader(filename);

            try {
                while ((line = fin.readLine()) != null) {
                    fout.println(line);
                }

                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fout.close();
    }
}