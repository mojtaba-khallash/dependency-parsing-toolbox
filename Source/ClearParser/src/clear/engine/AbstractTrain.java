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

import clear.model.AbstractModel;
import clear.train.AbstractTrainer;
import clear.train.BinaryTrainer;
import clear.train.OneVsAllTrainer;
import clear.train.algorithm.IAlgorithm;
import clear.train.algorithm.LibLinearL2;
import clear.train.algorithm.RRM;
import clear.train.kernel.AbstractKernel;
import clear.train.kernel.NoneKernel;
import clear.util.tuple.JIntObjectTuple;
import clear.util.tuple.JObjectObjectTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.io.PrintStream;
import java.util.ArrayList;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

/**
 * Trains dependency parser. <b>Last update:</b> 11/16/2010
 *
 * @author Jinho D. Choi
 */
abstract public class AbstractTrain extends AbstractCommon {

    @Option(name = "-m", usage = "model file", required = false, metaVar = "OPTIONAL")
    protected String s_modelFile = null;
    protected final String TAG_CLASSIFY = "classify";
    protected final String TAG_CLASSIFY_ALGORITHM = "algorithm";
    protected byte kernel_type = AbstractKernel.KERNEL_NONE;
    protected byte trainer_type = AbstractTrainer.ST_ONE_VS_ALL;
    protected ArrayList<JObjectObjectTuple<IntArrayList, ArrayList<int[]>>> a_yx;
    
    protected AbstractModel trainModel(int index, JarArchiveOutputStream zout) throws Exception {
        JIntObjectTuple<IAlgorithm> tup = getAlgorithm();

        if (tup.object == null) {
            System.err.println("Learning algorithm is not specified in the feature template");
            return null;
        }

        IAlgorithm algorithm = tup.object;
        int numThreads = tup.index;

        PrintStream fout = null;

        if (zout != null) {
            if (a_yx.size() == 1) {
                zout.putArchiveEntry(new JarArchiveEntry(ENTRY_MODEL));
            } else {
                zout.putArchiveEntry(new JarArchiveEntry(ENTRY_MODEL + "." + index));
            }

            fout = new PrintStream(zout);
        }

        long st = System.currentTimeMillis();

        NoneKernel kernel = new NoneKernel();
        kernel.out = out;
        kernel.add(a_yx.get(index));
        AbstractTrainer.out = out;
        AbstractTrainer trainer = (trainer_type == AbstractTrainer.ST_BINARY) ? new BinaryTrainer(fout, algorithm, kernel, numThreads) : new OneVsAllTrainer(fout, algorithm, kernel, numThreads);

        long time = System.currentTimeMillis() - st;
        out.printf("- duration: %d h, %d m\n", time / (1000 * 3600), time / (1000 * 60));

        if (zout != null) {
            zout.closeArchiveEntry();
        }
        if (fout != null) {
            if (index == a_yx.size() - 1) {
                fout.close();
            } else {
                fout.flush();
            }
        }

        return trainer.getModel();
    }

    private JIntObjectTuple<IAlgorithm> getAlgorithm() {
        Element eTrain = getElement(e_config, TAG_CLASSIFY);
        Element element = getElement(eTrain, TAG_CLASSIFY_ALGORITHM);
        String name = element.getAttribute("name").trim();

        StringBuilder options = new StringBuilder();
        IAlgorithm algorithm = null;
        String tmp;
        switch (name) {
            case IAlgorithm.LIBLINEAR_L2:
                {
                    byte lossType = 1;
                    double c = 0.1, eps = 0.1, bias = -1;
                    if ((tmp = element.getAttribute("l").trim()).length() > 0) {
                        lossType = Byte.parseByte(tmp);
                    }
                    if ((tmp = element.getAttribute("c").trim()).length() > 0) {
                        c = Double.parseDouble(tmp);
                    }
                    if ((tmp = element.getAttribute("e").trim()).length() > 0) {
                        eps = Double.parseDouble(tmp);
                    }
                    if ((tmp = element.getAttribute("b").trim()).length() > 0) {
                        bias = Double.parseDouble(tmp);
                    }
                    algorithm = new LibLinearL2(lossType, c, eps, bias);
                    options.append("loss_type = ");
                    options.append(lossType);
                    options.append(", c = ");
                    options.append(c);
                    options.append(", eps = ");
                    options.append(eps);
                    options.append(", bias = ");
                    options.append(bias);
                    break;
                }
            case IAlgorithm.RRM:
                {
                    int k = 40;
                    double mu = 1.0, eta = 0.001, c = 0.1;
                    if ((tmp = element.getAttribute("k").trim()).length() > 0) {
                        k = Integer.parseInt(tmp);
                    }
                    if ((tmp = element.getAttribute("m").trim()).length() > 0) {
                        mu = Double.parseDouble(tmp);
                    }
                    if ((tmp = element.getAttribute("e").trim()).length() > 0) {
                        eta = Double.parseDouble(tmp);
                    }
                    if ((tmp = element.getAttribute("c").trim()).length() > 0) {
                        c = Double.parseDouble(tmp);
                    }
                    algorithm = new RRM(k, mu, eta, c);
                    options.append("K = ");
                    options.append(k);
                    options.append(", mu = ");
                    options.append(mu);
                    options.append(", eta = ");
                    options.append(eta);
                    options.append(", c = ");
                    options.append(c);
                    break;
                }
        }

        int numThreads = 1;

        element = getElement(eTrain, "threads");
        if (element != null) {
            numThreads = Integer.parseInt(element.getTextContent().trim());
        }

        out.println("\n* Train model");
        out.println("- algorithm: " + name);
        out.println("- options  : " + options.toString());
        out.println("- threads  : " + numThreads);
        out.println();

        return new JIntObjectTuple<>(numThreads, algorithm);
    }
}