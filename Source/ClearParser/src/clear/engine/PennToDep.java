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

import clear.dep.DepTree;
import clear.morph.MorphEnAnalyzer;
import clear.reader.AbstractReader;
import clear.treebank.*;
import clear.util.IOUtil;
import java.io.File;
import java.io.PrintStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class PennToDep {

    @Option(name = "-i", usage = "name of a file containing phrase structure tree", required = true, metaVar = "REQUIRED")
    String s_inputFile;
    @Option(name = "-o", usage = "name of a file containing dependency trees", required = true, metaVar = "REQUIRED")
    String s_outputFile;
    @Option(name = "-h", usage = "name of a file containing head-percolation rules", required = true, metaVar = "REQUIRED")
    String s_headruleFile;
    @Option(name = "-m", usage = "name of a file containing dictionaries for morphological analyzer", metaVar = "OPTIONAL")
    String s_dictFile = null;
    @Option(name = "-l", usage = "language ::= " + AbstractReader.LANG_EN + " (default) | " + AbstractReader.LANG_KR, metaVar = "OPTIONAL")
    String s_language = AbstractReader.LANG_EN;
    @Option(name = "-n", usage = "minimum sentence length (inclusive; default = 1)", metaVar = "OPTIONAL")
    int n_length = 1;
    @Option(name = "-f", usage = "if set, include function tags", metaVar = "OPTIONAL")
    boolean b_funcTag = false;
    @Option(name = "-e", usage = "if set, include empty categories", metaVar = "OPTIONAL")
    boolean b_ec = false;
    @Option(name = "-r", usage = "if set, reverse dependencies of auxiliaries and modals", metaVar = "OPTIONAL")
    boolean b_reverseVC = false;

    public PennToDep(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);
            convert();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        }
    }

    public void convert() {
        TBReader reader = new TBReader(s_inputFile);
        TBHeadRules headrules = new TBHeadRules(s_headruleFile);
        MorphEnAnalyzer morph = (s_dictFile != null) ? new MorphEnAnalyzer(s_dictFile) : null;
        PrintStream fout = IOUtil.createPrintFileStream(s_outputFile);
        TBTree tree;
        AbstractTBConvert converter;

        if (s_language.equals(AbstractReader.LANG_KR)) {
            converter = new TBKrConvert(headrules);
        } else {
            converter = new TBEnConvert(headrules, morph, b_funcTag, b_ec, b_reverseVC);
        }

        String filename = s_inputFile.substring(s_inputFile.lastIndexOf(File.separator) + 1);
        int i = 0;

        System.out.print("\r" + filename + ": 0");

        while ((tree = reader.nextTree()) != null) {
            DepTree dTree = converter.toDepTree(tree);
            if (dTree.size() >= n_length) {
                fout.println(dTree + "\n");
                i++;
            }
            if (i % 1000 == 0) {
                System.out.print("\r" + filename + ": " + i);
            }
        }

        fout.flush();
        fout.close();
        System.out.println("\r" + filename + ": " + i);
    }

    static public void main(String[] args) {
        PennToDep ptd = new PennToDep(args);
    }
}