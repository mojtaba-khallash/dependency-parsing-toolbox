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
package clear.engine;

import clear.morph.MorphEnAnalyzer;
import clear.reader.AbstractReader;
import clear.util.IOUtil;
import java.io.BufferedReader;
import java.io.PrintStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Runs a morphological analyzer.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/4/2010
 */
public class MorphAnalyze {

    @Option(name = "-i", usage = "input file", required = true, metaVar = "REQUIRED")
    String inputFile;
    @Option(name = "-o", usage = "output file", required = true, metaVar = "REQUIRED")
    String outputFile;
    @Option(name = "-d", usage = "dictionary jar-file", required = true, metaVar = "REQUIRED")
    String dictFile;

    public MorphAnalyze(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);

            BufferedReader fin = IOUtil.createBufferedFileReader(inputFile);
            PrintStream fout = IOUtil.createPrintFileStream(outputFile);
            MorphEnAnalyzer morph = new MorphEnAnalyzer(dictFile);

            String line, form, pos, lemma;
            String[] tmp;

            while ((line = fin.readLine()) != null) {
                if (line.trim().equals("")) {
                    fout.println();
                    continue;
                }

                tmp = line.split(AbstractReader.FIELD_DELIM);
                form = tmp[0];
                pos = tmp[1];
                lemma = morph.getLemma(form, pos);

                fout.println(line + AbstractReader.FIELD_DELIM + lemma);
            }

            fin.close();
            fout.close();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MorphAnalyze(args);
    }
}