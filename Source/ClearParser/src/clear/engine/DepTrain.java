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

import clear.decode.OneVsAllDecoder;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.model.OneVsAllModel;
import clear.parse.AbstractDepParser;
import clear.parse.AbstractParser;
import clear.parse.ShiftEagerParser;
import clear.parse.ShiftPopParser;
import clear.reader.AbstractReader;
import clear.reader.CoNLLXReader;
import clear.reader.DepReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Trains conditional dependency parser. <b>Last update:</b> 11/19/2010
 *
 * @author Jinho D. Choi
 */
public class DepTrain extends AbstractTrain {

    @Option(name = "-t", usage = "feature template file", required = true, metaVar = "REQUIRED")
    private String s_featureXml = null;
    @Option(name = "-i", usage = "training file", required = true, metaVar = "REQUIRED")
    private String s_trainFile = null;
    @Option(name = "-n", usage = "bootstrapping level (default = 2)", required = false, metaVar = "OPTIONAL")
    private int n_boot = 2;
    private DepFtrXml t_xml = null;
    private DepFtrMap t_map = null;
    private OneVsAllModel m_model = null;
    
    @Override
    public void initElements() {
    }

    public DepTrain(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);
            init();
            train();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DepTrain(String configFile, String featureXml, String trainFile, String modelFile, int nBoot) {
        s_configFile = configFile;
        s_featureXml = featureXml;
        s_trainFile = trainFile;
        s_modelFile = modelFile;
        n_boot = nBoot;

        try {
            init();
            train();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void train() throws Exception {
        printConfig();

        String modelFile = s_modelFile;
        JarArchiveOutputStream zout = new JarArchiveOutputStream(new FileOutputStream(modelFile));

        trainDepParser(AbstractParser.FLAG_TRAIN_LEXICON, null);
        trainDepParser(AbstractParser.FLAG_TRAIN_INSTANCE, zout);
        m_model = (OneVsAllModel) trainModel(0, zout);
        a_yx = null;
        zout.flush();
        zout.close();

        for (int i = 1; i <= n_boot; i++) {
            modelFile = s_modelFile + ".boot" + i;
            out.print("\n== Bootstrapping: " + i + " ==\n");

            zout = new JarArchiveOutputStream(new FileOutputStream(modelFile));
            trainDepParser(AbstractParser.FLAG_TRAIN_BOOST, zout);
            m_model = null;
            m_model = (OneVsAllModel) trainModel(0, zout);
            a_yx = null;
            zout.flush();
            zout.close();
        }

        new File(ENTRY_LEXICA).delete();
    }

    /**
     * Trains the dependency parser.
     */
    private void trainDepParser(byte flag, JarArchiveOutputStream zout) throws Exception {
        AbstractDepParser parser = null;
        OneVsAllDecoder decoder;

        if (flag == ShiftPopParser.FLAG_TRAIN_LEXICON) {
            out.println("\n* Save lexica");
            switch (s_depParser) {
                case AbstractDepParser.ALG_SHIFT_EAGER:
                    parser = new ShiftEagerParser(flag, s_featureXml);
                    break;
                case AbstractDepParser.ALG_SHIFT_POP:
                    parser = new ShiftPopParser(flag, s_featureXml);
                    break;
            }
        } else if (flag == ShiftPopParser.FLAG_TRAIN_INSTANCE) {
            out.println("\n* Print training instances");
            out.println("- loading lexica");
            switch (s_depParser) {
                case AbstractDepParser.ALG_SHIFT_EAGER:
                    parser = new ShiftEagerParser(flag, t_xml, ENTRY_LEXICA);
                    break;
                case AbstractDepParser.ALG_SHIFT_POP:
                    parser = new ShiftPopParser(flag, t_xml, ENTRY_LEXICA);
                    break;
            }
        } else if (flag == ShiftPopParser.FLAG_TRAIN_BOOST) {
            out.println("\n* Train conditional");
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

        AbstractReader<DepNode, DepTree> reader = null;
        DepTree tree;
        int n;
        switch (s_format) {
            case AbstractReader.FORMAT_DEP:
                reader = new DepReader(s_trainFile, true);
                break;
            case AbstractReader.FORMAT_CONLLX:
                reader = new CoNLLXReader(s_trainFile, true);
                break;
        }

        parser.setLanguage(s_language);
        reader.setLanguage(s_language);

        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            parser.parse(tree);

            if (n % 1000 == 0) {
                out.printf("\r- parsing: %dK", n / 1000);
            }
        }

        out.println("\r- parsing: " + n);

        if (flag == ShiftPopParser.FLAG_TRAIN_LEXICON) {
            out.println("- saving");
            parser.saveTags(ENTRY_LEXICA);
            t_xml = parser.getDepFtrXml();
        } else if (flag == ShiftPopParser.FLAG_TRAIN_INSTANCE || flag == ShiftPopParser.FLAG_TRAIN_BOOST) {
            a_yx = parser.a_trans;

            zout.putArchiveEntry(new JarArchiveEntry(ENTRY_PARSER));
            PrintStream fout = new PrintStream(zout);
            fout.print(s_depParser);
            fout.flush();
            zout.closeArchiveEntry();

            zout.putArchiveEntry(new JarArchiveEntry(ENTRY_FEATURE));
            IOUtils.copy(new FileInputStream(s_featureXml), zout);
            zout.closeArchiveEntry();

            zout.putArchiveEntry(new JarArchiveEntry(ENTRY_LEXICA));
            IOUtils.copy(new FileInputStream(ENTRY_LEXICA), zout);
            zout.closeArchiveEntry();

            if (flag == ShiftPopParser.FLAG_TRAIN_INSTANCE) {
                t_map = parser.getDepFtrMap();
            }
        }
    }

    protected void printConfig() {
        out.println("* Configurations");
        out.println("- language   : " + s_language);
        out.println("- format     : " + s_format);
        out.println("- parser     : " + s_depParser);
        out.println("- feature_xml: " + s_featureXml);
        out.println("- train_file : " + s_trainFile);
        out.println("- model_file : " + s_modelFile);
        out.println("- n_boots    : " + n_boot);
    }

    static public void main(String[] args) {
        DepTrain depTrain = new DepTrain(args);
    }
}