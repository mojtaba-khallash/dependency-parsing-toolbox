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
import java.util.Arrays;

/**
 * One-vs-all model.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
public class OneVsAllModel extends AbstractMultiModel {

    public OneVsAllModel(AbstractKernel kernel) {
        super(kernel);
    }

    public OneVsAllModel(String modelFile) {
        super(modelFile);
    }

    public OneVsAllModel(BufferedReader fin) {
        super(fin);
    }

    public OneVsAllModel(int nLabels, int nFeatures, int[] aLabels, double[] dWeights) {
        super(nLabels, nFeatures, aLabels, dWeights);
    }

    @Override
    public void init(AbstractKernel kernel) {
        n_labels = kernel.L;
        n_features = kernel.D;
        a_labels = kernel.a_labels;
        d_weights = new double[n_labels * n_features];
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

    public void loadAux(BufferedReader fin) throws Exception {
        n_labels = Integer.parseInt(fin.readLine());
        n_features = Integer.parseInt(fin.readLine());
        a_labels = new int[n_labels];
        d_weights = new double[n_labels * n_features];

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
        fout.println(n_labels);
        fout.println(n_features);
        printLabels(fout);
        printWeights(fout);
    }

    private int getBeginIndex(int label, int index) {
        return index * n_labels + label;
    }

    @Override
    public void copyWeight(int label, double[] weight) {
        int i;

        for (i = 0; i < n_features; i++) {
            d_weights[getBeginIndex(label, i)] = weight[i];
        }
    }

    @Override
    public double[] getScores(int[] x) {
        double[] scores = Arrays.copyOf(d_weights, n_labels);
        int i, idx, label;

        for (i = 0; i < x.length; i++) {
            for (label = 0; label < n_labels; label++) {
                if ((idx = getBeginIndex(label, x[i])) < d_weights.length) {
                    scores[label] += d_weights[idx];
                }
            }
        }

        return scores;
    }

    @Override
    public double[] getScores(IntArrayList x) {
        double[] scores = Arrays.copyOf(d_weights, n_labels);
        int i, idx, label;

        for (i = 0; i < x.size(); i++) {
            for (label = 0; label < n_labels; label++) {
                if ((idx = getBeginIndex(label, x.get(i))) < d_weights.length) {
                    scores[label] += d_weights[idx];
                }
            }
        }

        return scores;
    }

    public double[] getScores(JIntDoubleTuple[] x) {
        double[] scores = Arrays.copyOf(d_weights, n_labels);
        int idx, label;

        for (JIntDoubleTuple tup : x) {
            for (label = 0; label < n_labels; label++) {
                if ((idx = getBeginIndex(label, tup.i)) < d_weights.length) {
                    scores[label] += (d_weights[idx] * tup.d);
                }
            }
        }

        for (label = 0; label < n_labels; label++) {
            scores[label] = scores[label];
        }

        return scores;
    }

    public double[] getScores(ArrayList<JIntDoubleTuple> x) {
        double[] scores = Arrays.copyOf(d_weights, n_labels);
        int idx, label;

        for (JIntDoubleTuple tup : x) {
            for (label = 0; label < n_labels; label++) {
                if ((idx = getBeginIndex(label, tup.i)) < d_weights.length) {
                    scores[label] += (d_weights[idx] * tup.d);
                }
            }
        }

        for (label = 0; label < n_labels; label++) {
            scores[label] = scores[label];
        }

        return scores;
    }
}