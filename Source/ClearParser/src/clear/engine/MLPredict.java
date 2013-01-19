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

import clear.decode.AbstractDecoder;
import clear.decode.BinaryDecoder;
import clear.decode.OneVsAllDecoder;
import clear.train.AbstractTrainer;
import clear.train.kernel.AbstractKernel;
import clear.util.DSUtil;
import clear.util.IOUtil;
import clear.util.tuple.JIntDoubleTuple;
import java.io.BufferedReader;
import java.io.PrintStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Predicts using a classifier.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/8/2010
 */
public class MLPredict {

    @Option(name = "-i", usage = "input file", required = true, metaVar = "REQUIRED")
    String s_inputFile;
    @Option(name = "-o", usage = "output file", required = true, metaVar = "REQUIRED")
    String s_outputFile;
    @Option(name = "-m", usage = "model file", required = true, metaVar = "REQUIRED")
    String s_modelFile;
    @Option(name = "-s", usage = "strategy ::= " + AbstractTrainer.ST_BINARY + " (binary) | " + AbstractTrainer.ST_ONE_VS_ALL + " (one-vs-all; default)", metaVar = "OPTIONAL")
    byte i_strategy = AbstractTrainer.ST_ONE_VS_ALL;

    public MLPredict(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);

            long st = System.currentTimeMillis();

            System.out.println("* Loading model: " + s_modelFile);
            AbstractDecoder decode;

            if (i_strategy == AbstractTrainer.ST_BINARY) {
                decode = new BinaryDecoder(s_modelFile);
            } else // One-vs-all
            {
                decode = new OneVsAllDecoder(s_modelFile);
            }

            BufferedReader fin = IOUtil.createBufferedFileReader(s_inputFile);
            PrintStream fout = IOUtil.createPrintFileStream(s_outputFile);
            String line;
            String[] tok;
            int y;
            JIntDoubleTuple res;
            int correct = 0, total;

            System.out.println("* Predicting   : " + s_inputFile);
            for (total = 0; (line = fin.readLine()) != null; total++) {
                tok = line.split(AbstractKernel.COL_DELIM);
                y = Integer.parseInt(tok[0]);

                if (!line.contains(":")) {
                    res = decode.predict(DSUtil.toIntArray(tok, 1));
                } else {
                    res = decode.predict(DSUtil.toJIntDoubleArray(tok, 1));
                }

                fout.println(res.i + " " + res.d);
                if (res.i == y) {
                    correct++;
                }
            }

            fout.close();

            long time = System.currentTimeMillis() - st;
            System.out.printf("* Accuracy     : %f (%d/%d)\n", (double) correct / total, correct, total);
            System.out.printf("* Decoding time: %d hours, %d minutes, %d seconds\n", time / (1000 * 3600), time / (1000 * 60), time / 1000);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void main(String[] args) {
        MLPredict mlp = new MLPredict(args);
    }
}