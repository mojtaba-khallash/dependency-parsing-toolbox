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

import clear.decode.AbstractDecoder;
import clear.decode.OneVsAllDecoder;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.ftr.map.SRLFtrMap;
import clear.ftr.xml.SRLFtrXml;
import clear.model.AbstractModel;
import clear.model.OneVsAllModel;
import clear.parse.AbstractParser;
import clear.parse.AbstractSRLParser;
import clear.parse.SRLParser;
import clear.reader.AbstractReader;
import clear.reader.SRLReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class SRLTrain extends AbstractTrain {

    @Option(name = "-t", usage = "feature template file", required = true, metaVar = "REQUIRED")
    private String s_featureXml = null;
    @Option(name = "-i", usage = "training file", required = true, metaVar = "REQUIRED")
    private String s_trainFile = null;
    @Option(name = "-n", usage = "bootstrapping level (default = 1)", required = false, metaVar = "OPTIONAL")
    private int n_boot = 1;
    private SRLFtrXml t_xml = null;
    private SRLFtrMap[] t_map = null;
    private AbstractModel[] m_model = null;
    private String[] s_lexiconFiles = {ENTRY_LEXICA + ".0", ENTRY_LEXICA + ".1"};

    @Override
    public void initElements() {
    }

    protected void train() throws Exception {
        printConfig();

        String modelFile = s_modelFile;
        JarArchiveOutputStream zout = new JarArchiveOutputStream(new FileOutputStream(modelFile));

        t_map = new SRLFtrMap[s_lexiconFiles.length];
        m_model = new AbstractModel[s_lexiconFiles.length];

        trainSRLParser(AbstractParser.FLAG_TRAIN_LEXICON, null);
        trainSRLParser(AbstractParser.FLAG_TRAIN_INSTANCE, zout);

        for (int j = 0; j < m_model.length; j++) {
            m_model[j] = trainModel(j, zout);
        }

        a_yx = null;
        zout.flush();
        zout.close();

        for (int i = 1; i <= n_boot; i++) {
            modelFile = s_modelFile + ".boot" + i;
            System.out.print("\n== Bootstrapping: " + i + " ==\n");

            zout = new JarArchiveOutputStream(new FileOutputStream(modelFile));
            trainSRLParser(AbstractParser.FLAG_TRAIN_BOOST, zout);

            m_model = new AbstractModel[m_model.length];
            for (int j = 0; j < m_model.length; j++) {
                m_model[j] = trainModel(j, zout);
            }

            a_yx = null;
            zout.flush();
            zout.close();
        }

        for (String filename : s_lexiconFiles) {
            new File(filename).delete();
        }
    }

    /**
     * Trains the dependency parser.
     */
    private void trainSRLParser(byte flag, JarArchiveOutputStream zout) throws Exception {
        AbstractSRLParser labeler = null;
        AbstractDecoder[] decoder;

        if (flag == SRLParser.FLAG_TRAIN_LEXICON) {
            System.out.println("\n* Save lexica");
            labeler = new SRLParser(flag, s_featureXml);
        } else if (flag == SRLParser.FLAG_TRAIN_INSTANCE) {
            System.out.println("\n* Print training instances");
            System.out.println("- loading lexica");

            labeler = new SRLParser(flag, t_xml, s_lexiconFiles);
        } else if (flag == SRLParser.FLAG_TRAIN_BOOST) {
            System.out.println("\n* Train boost");

            decoder = new AbstractDecoder[m_model.length];
            for (int i = 0; i < decoder.length; i++) {
                decoder[i] = new OneVsAllDecoder((OneVsAllModel) m_model[i]);
            }

            labeler = new SRLParser(flag, t_xml, t_map, decoder);
        }

        AbstractReader<DepNode, DepTree> reader = new SRLReader(s_trainFile, true);
        DepTree tree;
        int n;

        labeler.setLanguage(s_language);
        reader.setLanguage(s_language);

        for (n = 0; (tree = reader.nextTree()) != null; n++) {
            labeler.parse(tree);

            if (n % 1000 == 0) {
                System.out.printf("\r- parsing: %dK", n / 1000);
            }
        }

        System.out.println("\r- labeling: " + n);

        if (flag == SRLParser.FLAG_TRAIN_LEXICON) {
            System.out.println("- labeling");
            labeler.saveTags(s_lexiconFiles);
            t_xml = labeler.getSRLFtrXml();
        } else if (flag == SRLParser.FLAG_TRAIN_INSTANCE || flag == SRLParser.FLAG_TRAIN_BOOST) {
            a_yx = labeler.a_trans;

            zout.putArchiveEntry(new JarArchiveEntry(ENTRY_FEATURE));
            IOUtils.copy(new FileInputStream(s_featureXml), zout);
            zout.closeArchiveEntry();

            for (String lexicaFile : s_lexiconFiles) {
                zout.putArchiveEntry(new JarArchiveEntry(lexicaFile));
                IOUtils.copy(new FileInputStream(lexicaFile), zout);
                zout.closeArchiveEntry();
            }

            if (flag == SRLParser.FLAG_TRAIN_INSTANCE) {
                t_map = labeler.getSRLFtrMap();
            }
        }
    }

    protected void printConfig() {
        System.out.println("* Configurations");
        System.out.println("- language   : " + s_language);
        System.out.println("- format     : " + s_format);
        System.out.println("- parser     : " + s_depParser);
        System.out.println("- feature_xml: " + s_featureXml);
        System.out.println("- train_file : " + s_trainFile);
        System.out.println("- model_file : " + s_modelFile);
        System.out.println("- n_boots    : " + n_boot);
    }

    static public void main(String[] args) {
        SRLTrain train = new SRLTrain();
        CmdLineParser cmd = new CmdLineParser(train);

        try {
            cmd.parseArgument(args);
            train.init();
            train.train();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}