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

import clear.train.AbstractTrainer;
import clear.train.BinaryTrainer;
import clear.train.OneVsAllTrainer;
import clear.train.algorithm.IAlgorithm;
import clear.train.algorithm.LibLinearL2;
import clear.train.algorithm.RRM;
import clear.train.kernel.NoneKernel;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Trains a classifier.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/8/2010
 */
public class MLTrain {

    @Option(name = "-i", usage = "instance file", required = true, metaVar = "REQUIRED")
    String s_instanceFile;
    @Option(name = "-m", usage = "model file", required = true, metaVar = "REQUIRED")
    String s_modelFile;
    @Option(name = "-a", usage = "algorithm ::= " + IAlgorithm.LIBLINEAR_L2 + " (LibLinear L2-SVM; default) |\n              " + IAlgorithm.RRM + " (Robust Risk Minimization)", metaVar = "OPTIONAL")
    String s_algorithm = IAlgorithm.LIBLINEAR_L2;
    @Option(name = "-s", usage = "strategy ::= " + AbstractTrainer.ST_BINARY + " (binary) | " + AbstractTrainer.ST_ONE_VS_ALL + " (one-vs-all; default)", metaVar = "OPTIONAL")
    byte i_strategy = AbstractTrainer.ST_ONE_VS_ALL;
    @Option(name = "-n", usage = "# of threads to train with (default = 2)", metaVar = "OPTIONAL")
    int i_numThreads = 2;
    @Option(name = "-L", usage = "LIB: loss type ::= 1 (L1-loss; default) | 2 (L2-loss)", metaVar = "OPTIONAL")
    byte i_lossType = 1;
    @Option(name = "-E", usage = "LIB: termination criterion (default = 0.1)\nRRM: learning rate (default = 0.001)", metaVar = "OPTIONAL")
    double d_e = 0.1;
    @Option(name = "-B", usage = "LIB: bias (default = -1)", metaVar = "OPTIONAL")
    double d_bias = -1;
    @Option(name = "-C", usage = "LIB: penalty (default = 0.1)\nRRM: regularization (default = 0.1)", metaVar = "OPTIONAL")
    double d_c = 0.1;
    @Option(name = "-K", usage = "RRM: max # of iterations (default = 40)", metaVar = "OPTIONAL")
    int i_K = 40;
    @Option(name = "-M", usage = "RRM: initial weights (default = 1.0)", metaVar = "OPTIONAL")
    double d_mu = 1.0;

    public MLTrain(String[] args) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);

            long st = System.currentTimeMillis();
            IAlgorithm algorithm;

            if (s_algorithm.equals(IAlgorithm.LIBLINEAR_L2)) {
                algorithm = new LibLinearL2(i_lossType, d_c, d_e, d_bias);
            } else // RRM
            {
                d_e = 0.001;
                algorithm = new RRM(i_K, d_mu, d_e, d_c);
            }

            if (i_strategy == AbstractTrainer.ST_BINARY) {
                new BinaryTrainer(s_modelFile, algorithm, new NoneKernel(s_instanceFile));
            } else // One-vs-all
            {
                new OneVsAllTrainer(s_modelFile, algorithm, new NoneKernel(s_instanceFile), i_numThreads);
            }

            long time = System.currentTimeMillis() - st;
            System.out.printf("\n* Training time: %d hours, %d minutes\n", time / (1000 * 3600), time / (1000 * 60));
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        }
    }

    static public void main(String[] args) {
        new MLTrain(args);
    }
}