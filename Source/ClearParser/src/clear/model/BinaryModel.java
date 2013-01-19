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
package clear.model;

import clear.train.kernel.AbstractKernel;
import clear.util.IOUtil;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Binary model.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
public class BinaryModel extends AbstractModel {

    public BinaryModel(AbstractKernel kernel) {
        super(kernel);
    }

    public BinaryModel(String modelFile) {
        super(modelFile);
    }

    public BinaryModel(BufferedReader fin) {
        super(fin);
    }

    @Override
    public void init(AbstractKernel kernel) {
        n_features = kernel.D;
        n_labels = 2;
        a_labels = kernel.a_labels;
        d_weights = new double[n_features];
    }

    @Override
    public void load(String modelFile) {
        try {
            BufferedReader fin = IOUtil.createBufferedFileReader(modelFile);

            loadAux(fin);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(BufferedReader fin) {
        try {
            loadAux(fin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAux(BufferedReader fin) throws Exception {
        n_features = Integer.parseInt(fin.readLine());
        n_labels = 2;
        a_labels = new int[2];
        d_weights = new double[n_features];

        readLabels(fin);
        readWeights(fin);
    }

    @Override
    public void save(String modelFile) {
        try {
            PrintStream fout = IOUtil.createPrintFileStream(modelFile);

            saveAux(fout);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(PrintStream fout) {
        try {
            saveAux(fout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAux(PrintStream fout) throws Exception {
        fout.println(n_features);
        printLabels(fout);
        printWeights(fout);
    }

    public void copyWeight(double[] weight) {
        System.arraycopy(weight, 0, d_weights, 0, n_features);
    }

    public double getScore(int[] x) {
        double score = d_weights[0];
        int i, idx;

        for (i = 0; i < x.length; i++) {
            if ((idx = x[i]) < d_weights.length) {
                score += d_weights[idx];
            }
        }

        return score;
    }

    public double getScore(IntArrayList x) {
        double score = d_weights[0];
        int i, idx;

        for (i = 0; i < x.size(); i++) {
            if ((idx = x.get(i)) < d_weights.length) {
                score += d_weights[idx];
            }
        }

        return score;
    }

    public double getScore(ArrayList<JIntDoubleTuple> x) {
        double score = d_weights[0];
        int idx;

        for (JIntDoubleTuple tup : x) {
            if ((idx = tup.i) < d_weights.length) {
                score += (d_weights[idx] * tup.d);
            }
        }

        return score;
    }

    public double getScore(JIntDoubleTuple[] x) {
        double score = d_weights[0];
        int idx;

        for (JIntDoubleTuple tup : x) {
            if ((idx = tup.i) < d_weights.length) {
                score += (d_weights[idx] * tup.d);
            }
        }

        return score;
    }
}