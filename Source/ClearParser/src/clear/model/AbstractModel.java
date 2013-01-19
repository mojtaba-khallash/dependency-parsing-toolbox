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
import java.io.BufferedReader;
import java.io.PrintStream;

/**
 * Abstract model.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
abstract public class AbstractModel {

    public int n_features;
    public int n_labels;
    public int[] a_labels;
    public double[] d_weights;

    /**
     * For training.
     */
    public AbstractModel(AbstractKernel kernel) {
        init(kernel);
    }

    /**
     * For decoding.
     */
    public AbstractModel(String modelFile) {
        load(modelFile);
    }

    /**
     * For decoding.
     */
    public AbstractModel(BufferedReader fin) {
        load(fin);
    }

    public AbstractModel(int nLabels, int nFeatures, int[] aLabels, double[] dWeights) {
        n_labels = nLabels;
        n_features = nFeatures;
        a_labels = aLabels;
        d_weights = dWeights;
    }

    protected void readLabels(BufferedReader fin) throws Exception {
        String[] tmp = fin.readLine().split(" ");
        int i;

        for (i = 0; i < tmp.length; i++) {
            a_labels[i] = Integer.parseInt(tmp[i]);
        }
    }

    protected void readWeights(BufferedReader fin) throws Exception {
        readVector(fin, d_weights);
    }

    protected void readVector(BufferedReader fin, double[] vector) throws Exception {
        int[] buffer = new int[128];
        int i, b;

        for (i = 0; i < vector.length; i++) {
            b = 0;

            while (true) {
                int ch = fin.read();

                if (ch == ' ') {
                    break;
                } else {
                    buffer[b++] = ch;
                }
            }

            vector[i] = Double.parseDouble((new String(buffer, 0, b)));
        }
    }

    protected void printLabels(PrintStream fout) throws Exception {
        StringBuilder build = new StringBuilder();
        int i;

        build.append(a_labels[0]);
        for (i = 1; i < a_labels.length; i++) {
            build.append(" ");
            build.append(a_labels[i]);
        }

        fout.println(build.toString());
    }

    protected void printWeights(PrintStream fout) throws Exception {
        printVector(fout, d_weights);
    }

    protected void printVector(PrintStream fout, double[] vector) throws Exception {
        StringBuilder build;
        int i = 0, j;

        while (i < vector.length) {
            build = new StringBuilder();

            for (j = 0; j < n_features; j++) {
                build.append(vector[i++]);
                build.append(' ');
            }

            fout.println(build.toString());
        }
    }

    static public double logistic(double score) {
        return 1 / (1 + Math.exp(-score));
    }

    abstract public void init(AbstractKernel kernel);

    abstract public void load(String modelFile);

    abstract public void load(BufferedReader fin);

    abstract public void save(String modelFile);

    abstract public void save(PrintStream fout);
}