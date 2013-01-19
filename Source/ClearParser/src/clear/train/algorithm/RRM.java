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
package clear.train.algorithm;

import clear.train.AbstractTrainer;
import clear.train.kernel.AbstractKernel;
import java.util.Arrays;

/**
 * RRM algorithm.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
public class RRM implements IAlgorithm {

    private int i_K;
    private double d_mu;
    private double d_eta;
    private double d_c;

    public RRM(int K, double mu, double eta, double c) {
        i_K = K;
        d_mu = mu;
        d_eta = eta;
        d_c = c;
    }

    @Override
    public double[] getWeight(AbstractKernel kernel, int currLabel) {
        double[] pWeight = new double[kernel.D];
        Arrays.fill(pWeight, d_mu);
        double[] nWeight = new double[kernel.D];
        Arrays.fill(nWeight, d_mu);
        double[] alpha = new double[kernel.N];
        double[] bWeight = new double[kernel.D];
        double bestAcc = -1;
        int bestK = 0;

        double p, min1, min2, min, delta, delta_y_i, currAcc;
        byte[] aY = new byte[kernel.N];
        byte y_i;
        int i, k;
        int[] x_i;

        for (i = 0; i < kernel.N; i++) {
            aY[i] = (kernel.a_ys.get(i) == currLabel) ? (byte) 1 : (byte) -1;
        }

        for (k = 1; k <= i_K; k++) {
            for (i = 0; i < kernel.N; i++) {
                // retreive x_i, y_i
                x_i = kernel.a_xs.get(i);
                y_i = aY[i];

                // calculate p
                if (kernel.b_binary) {
                    p = getScore(pWeight, nWeight, x_i) * y_i;
                } else {
                    p = getScore(pWeight, nWeight, x_i, kernel.a_vs.get(i)) * y_i;
                }

                // calculate delta
                min1 = 2 * d_c - alpha[i];
                min2 = d_eta * ((d_c - alpha[i]) / d_c - p);
                min = Math.min(min1, min2);
                delta = Math.max(min, -alpha[i]);
                delta_y_i = delta * y_i;

                // update weights
                pWeight[0] *= Math.exp(delta_y_i);
                nWeight[0] *= Math.exp(-delta_y_i);

                for (int idx : x_i) {
                    pWeight[idx] *= Math.exp(delta_y_i);
                    nWeight[idx] *= Math.exp(-delta_y_i);
                }

                // update alpha (boosting factor)
                alpha[i] += delta;
            }

            currAcc = getF1Score(kernel, aY, pWeight, nWeight);

            if (bestAcc < currAcc) {
                setWeight(kernel.D, bWeight, pWeight, nWeight);
                bestAcc = currAcc;
                bestK = k;
            }

            if (currAcc == 1) {
                break;
            }
        }

        AbstractKernel.normalize(bWeight);

        StringBuilder build = new StringBuilder();

        build.append("- label = ");
        build.append(currLabel);
        build.append(": k = ");
        build.append(bestK);
        build.append(", acc = ");
        build.append(bestAcc);

        AbstractTrainer.out.println(build.toString());

        return (bWeight);
    }

    /**
     * bWeight[i] = pWeight[i] - nWeight[i]
     */
    private void setWeight(int D, double[] bWeight, double[] pWeight, double[] nWeight) {
        for (int i = 0; i < D; i++) {
            bWeight[i] = pWeight[i] - nWeight[i];
        }
    }

    /**
     * Returns the score of a training instance
     * <code>x</code> using the balanced weight vectors.
     *
     * @param pWeight positive weight vector
     * @param nWeight negative weight vector
     * @param x training instance (indices start from 1)
     */
    private double getScore(double[] pWeight, double[] nWeight, int[] x) {
        double score = pWeight[0] - nWeight[0];

        for (int idx : x) {
            score += (pWeight[idx] - nWeight[idx]);
        }

        return score;
    }

    private double getScore(double[] pWeight, double[] nWeight, int[] x, double[] v) {
        double score = pWeight[0] - nWeight[0];
        int idx, i;

        for (i = 0; i < x.length; i++) {
            idx = x[i];
            score += (pWeight[idx] - nWeight[idx]) * v[i];
        }

        return score;
    }

    /**
     * Returns F1 score of the balanced weight vectors.
     *
     * @param pWeight positive weight vector
     * @param nWeight negative weight vector
     */
    private double getF1Score(AbstractKernel kernel, byte[] aY, double[] pWeight, double[] nWeight) {
        int correct = 0, pTotal = 0, rTotal = 0, i;
        byte y_i;
        double score;

        for (i = 0; i < kernel.N; i++) {
            y_i = aY[i];

            if (kernel.b_binary) {
                score = getScore(pWeight, nWeight, kernel.a_xs.get(i));
            } else {
                score = getScore(pWeight, nWeight, kernel.a_xs.get(i), kernel.a_vs.get(i));
            }

            if (score > 0) {
                if (y_i == 1) {
                    correct++;
                }
                pTotal++;
            }

            if (y_i == 1) {
                rTotal++;
            }
        }

        double precision = (pTotal == 0) ? 0 : (double) correct / pTotal;
        double recall = (rTotal == 0) ? 0 : (double) correct / rTotal;

        if (precision + recall == 0) {
            return 0;
        }

        return 2 * (precision * recall) / (precision + recall);
    }
}