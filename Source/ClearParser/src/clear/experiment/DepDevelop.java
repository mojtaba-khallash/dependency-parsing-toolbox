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
package clear.experiment;

import clear.decode.OneVsAllDecoder;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.engine.AbstractTrain;
import clear.engine.DepEvaluate;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.model.OneVsAllModel;
import clear.parse.AbstractDepParser;
import clear.parse.AbstractParser;
import clear.parse.ShiftEagerParser;
import clear.parse.ShiftPopParser;
import clear.reader.AbstractReader;
import clear.reader.DepReader;
import clear.util.IOUtil;
import java.io.File;
import java.io.PrintStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Trains conditional dependency parser. <b>Last update:</b> 11/19/2010
 *
 * @author Jinho D. Choi
 */
public class DepDevelop extends AbstractTrain {

    private final int MAX_ITER = 5;
    @Option(name = "-t", usage = "feature template file", required = true, metaVar = "REQUIRED")
    private String s_featureXml = null;
    @Option(name = "-i", usage = "training file", required = true, metaVar = "REQUIRED")
    private String s_trainFile = null;
    @Option(name = "-d", usage = "development file", required = true, metaVar = "REQUIRED")
    private String s_devFile = null;
    private StringBuilder s_build = null;
    private DepFtrXml t_xml = null;
    private DepFtrMap t_map = null;
    private OneVsAllModel m_model = null;

    @Override
    public void initElements() {
    }

    protected void train() throws Exception {
        printConfig();

        int i = 0;
        String log = "\n== Bootstrapping: " + i + " ==\n";

        s_build = new StringBuilder();
        s_build.append(log);
        System.out.print(log);

        trainDepParser(AbstractParser.FLAG_TRAIN_LEXICON, null, null);
        trainDepParser(AbstractParser.FLAG_TRAIN_INSTANCE, null, null);
        m_model = (OneVsAllModel) trainModel(0, null);
        a_yx = null;

        double prevAcc = 0, currAcc;

        do {
            currAcc = trainDepParser(AbstractParser.FLAG_PREDICT, s_devFile + ".parse." + i, null);
            if (currAcc <= prevAcc) {
                break;
            }
            //	if (i == 0)	break;
            prevAcc = currAcc;
            trainDepParser(AbstractParser.FLAG_TRAIN_BOOST, null, null);

            log = "\n== Bootstrapping: " + (++i) + " ==\n";
            s_build.append(log);
            System.out.print(log);

            m_model = null;
            m_model = (OneVsAllModel) trainModel(0, null);
            a_yx = null;
        } while (i < MAX_ITER);

        new File(ENTRY_LEXICA).delete();
        System.out.println(s_build.toString());
    }

    /**
     * Trains the dependency parser.
     */
    private double trainDepParser(byte flag, String outputFile, JarArchiveOutputStream zout) throws Exception {
        AbstractDepParser parser = null;
        OneVsAllDecoder decoder;
        PrintStream fout = null;

        if (flag == ShiftPopParser.FLAG_TRAIN_LEXICON) {
            System.out.println("\n* Save lexica");
            switch (s_depParser) {
                case AbstractDepParser.ALG_SHIFT_EAGER:
                    parser = new ShiftEagerParser(flag, s_featureXml);
                    break;
                case AbstractDepParser.ALG_SHIFT_POP:
                    parser = new ShiftPopParser(flag, s_featureXml);
                    break;
            }
        } else if (flag == ShiftPopParser.FLAG_TRAIN_INSTANCE) {
            System.out.println("\n* Print training instances");
            System.out.println("- loading lexica");
            switch (s_depParser) {
                case AbstractDepParser.ALG_SHIFT_EAGER:
                    parser = new ShiftEagerParser(flag, t_xml, ENTRY_LEXICA);
                    break;
                case AbstractDepParser.ALG_SHIFT_POP:
                    parser = new ShiftPopParser(flag, t_xml, ENTRY_LEXICA);
                    break;
            }
        } else if (flag == ShiftPopParser.FLAG_PREDICT || flag == ShiftPopParser.FLAG_TRAIN_BOOST) {
            if (flag == ShiftPopParser.FLAG_PREDICT) {
                System.out.println("\n* Predict");
                fout = IOUtil.createPrintFileStream(outputFile);
            } else {
                System.out.println("\n* Train boost");
            }

            decoder = new OneVsAllDecoder(m_model);
            switch (s_depParser) {
                case AbstractDepParser.ALG_SHIFT_EAGER:
                    parser = new ShiftEagerParser(flag, t_xml, t_map, decoder);
                    break;
                case AbstractDepParser.ALG_SHIFT_POP:
                    parser = new ShiftPopParser(flag, t_xml, t_map, decoder);
                    break;
            }
        }

        String inputFile;
        boolean isTrain;

        if (flag == ShiftPopParser.FLAG_PREDICT) {
            inputFile = s_devFile;
            isTrain = false;
        } else {
            inputFile = s_trainFile;
            isTrain = true;
        }

        AbstractReader<DepNode, DepTree> reader = new DepReader(inputFile, isTrain);
        DepTree tree;
        int n;

        parser.setLanguage(s_language);
        reader.setLanguage(s_language);

        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            parser.parse(tree);

            if (flag == ShiftPopParser.FLAG_PREDICT) {
                fout.println(tree + "\n");
            }
            if (n % 1000 == 0) {
                System.out.printf("\r- parsing: %dK", n / 1000);
            }
        }

        System.out.println("\r- parsing: " + n);

        if (flag == ShiftPopParser.FLAG_TRAIN_LEXICON) {
            System.out.println("- saving");
            parser.saveTags(ENTRY_LEXICA);
            t_xml = parser.getDepFtrXml();
        } else if (flag == ShiftPopParser.FLAG_TRAIN_INSTANCE) {
            t_map = parser.getDepFtrMap();
            a_yx = parser.a_trans;
        } else if (flag == ShiftPopParser.FLAG_PREDICT) {
            fout.close();

            String[] args = {"-g", s_devFile, "-s", outputFile};
            String log = "\n* Development accuracy\n";

            System.out.print(log);
            DepEvaluate eval = new DepEvaluate(args);

            s_build.append(log);
            s_build.append("- LAS: ").append(eval.getLas()).append("\n");
            s_build.append("- UAS: ").append(eval.getUas()).append("\n");
            s_build.append("- LS : ").append(eval.getLs()).append("\n");

            return eval.getLas();
        } else if (flag == AbstractParser.FLAG_TRAIN_BOOST) {
            a_yx = parser.a_trans;
        }

        return 0;
    }

    protected void printConfig() {
        System.out.println("* Configurations");
        System.out.println("- language   : " + s_language);
        System.out.println("- format     : " + s_format);
        System.out.println("- parser     : " + s_depParser);
        System.out.println("- feature_xml: " + s_featureXml);
        System.out.println("- train_file : " + s_trainFile);
        System.out.println("- dev_file   : " + s_devFile);
    }

    static public void main(String[] args) {
        DepDevelop developer = new DepDevelop();
        CmdLineParser cmd = new CmdLineParser(developer);

        try {
            cmd.parseArgument(args);
            developer.init();
            developer.train();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}