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
package clear.engine;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.helper.POSTagger;
import clear.helper.Tokenizer;
import clear.parse.AbstractDepParser;
import clear.parse.Lemmatizer;
import clear.reader.*;
import clear.util.IOUtil;
import java.io.File;
import java.io.PrintStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

/**
 * Predicts dependency trees.
 *
 * @author Jinho D. Choi <b>Last update:</b> 6/29/2010
 */
public class DepPredict extends AbstractCommon {

    @Option(name = "-i", usage = "input file", required = true, metaVar = "REQUIRED")
    private String s_inputPath = null;
    @Option(name = "-o", usage = "output file", required = true, metaVar = "REQUIRED")
    private String s_outputFile = null;
    @Option(name = "-m", usage = "model file", required = true, metaVar = "REQUIRED")
    private String s_modelFile = null;
    /**
     * Tokenizing modelFile
     */
    private Tokenizer g_tokenizer = null;
    /**
     * Part-of-speech tagging modelFile
     */
    private POSTagger g_postagger = null;
    /**
     * Morphological dictionary directory
     */
    private Lemmatizer g_lemmatizer = null;
    /**
     * Dependency parser
     */
    private AbstractDepParser g_parser = null;
    private int[] n_size_total = new int[10];
    private double[] d_time = new double[10];
    private double d_time_total;
    private int n_total;

    public DepPredict(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);
            init();
            initTime();
            printConfig();

            g_parser = getDepParser(s_modelFile);
            g_parser.setLanguage(s_language);

            File file = new File(s_inputPath);

            if (file.isFile()) {
                predict(s_inputPath, s_outputFile);
            } else {
                for (String inputFile : file.list()) {
                    inputFile = s_inputPath + File.separator + inputFile;
                    predict(inputFile, inputFile + ".parse");
                }
            }

            printTime();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void predict(String inputFile, String outputFile) throws Exception {
        AbstractReader<DepNode, DepTree> reader = null;
        switch (s_format) {
            case AbstractReader.FORMAT_RAW:
                reader = new RawReader(inputFile, g_tokenizer);
                break;
            case AbstractReader.FORMAT_POS:
                reader = new PosReader(inputFile);
                break;
            case AbstractReader.FORMAT_DEP:
                reader = new DepReader(inputFile, false);
                break;
            case AbstractReader.FORMAT_CONLLX:
                reader = new CoNLLXReader(inputFile, false);
                break;
        }

        reader.setLanguage(s_language);

        PrintStream fout = IOUtil.createPrintFileStream(outputFile);
        //	PrintStream fplot = IOUtil.createPrintFileStream("plot.txt");
        DepTree tree;

        long st, et;
        int n = 0;

        out.println("\n* Predict: " + inputFile);

        while (true) {
            st = System.currentTimeMillis();
            tree = reader.nextTree();
            if (tree == null) {
                break;
            }
            switch (s_format) {
                case AbstractReader.FORMAT_RAW:
                    g_postagger.postag(tree);
                    g_lemmatizer.lemmatize(tree);
                    break;
                case AbstractReader.FORMAT_POS:
                    g_lemmatizer.lemmatize(tree);
                    break;
            }

            g_parser.parse(tree);
            n++;
            et = System.currentTimeMillis();
            fout.println(tree + "\n");
            if (n % 100 == 0) {
                out.print("\r- parsing: " + n);
            }

            int index = (tree.size() >= 101) ? 9 : (tree.size() - 1) / 10;
            d_time[index] += (et - st);
            d_time_total += (et - st);
            n_size_total[index]++;
            //		fplot.println(tree.size()+"\t"+tree.n_trans);
        }
        out.println("\r- parsing: " + n);

        n_total += n;
        fout.close();
        reader.close();
        //	fplot.flush();	fplot.close();
    }

    @Override
    protected void initElements() {
        if (s_format.equals(AbstractReader.FORMAT_RAW) || s_format.equals(AbstractReader.FORMAT_POS)) {
            Element ePredict = getElement(e_config, TAG_PREDICT);
            Element element;

            if ((element = getElement(ePredict, TAG_PREDICT_TOK_MODEL)) != null) {
                g_tokenizer = new Tokenizer(element.getTextContent().trim());
            }

            if ((element = getElement(ePredict, TAG_PREDICT_POS_MODEL)) != null) {
                g_postagger = new POSTagger(element.getTextContent().trim());
            }

            if ((element = getElement(ePredict, TAG_PREDICT_MORPH_DICT)) != null) {
                g_lemmatizer = new Lemmatizer(element.getTextContent().trim());
            }
        }
    }

    protected final void printConfig() {
        out.println("* Configurations");
        out.println("- language   : " + s_language);
        out.println("- format     : " + s_format);
        out.println("- parser     : " + s_depParser);
        out.println("- model_file : " + s_modelFile);
        out.println("- input_file : " + s_inputPath);
        out.println("- output_file: " + s_outputFile);
    }

    protected final void initTime() {
        n_size_total = new int[10];
        d_time = new double[10];
        d_time_total = 0;
        n_total = 0;
    }

    protected final void printTime() {
        out.println("\n* Parsing time per sentence length");

        for (int i = 0; i < d_time.length; i++) {
            out.printf("<= %3d: %4.2f (%f/%d)\n", (i + 1) * 10, d_time[i] / n_size_total[i], d_time[i], n_size_total[i]);
        }

        out.printf("\nAverage parsing time: %4.2f (ms) (%f/%d)\n", d_time_total / n_total, d_time_total, n_total);
    }

    static public void main(String[] args) {
        DepPredict depPredict = new DepPredict(args);
    }
}