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
package clear.train.algorithm;

import clear.train.AbstractTrainer;
import clear.train.kernel.AbstractKernel;

/**
 * LibLinear L2-SVM algorithm.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/4/2010
 */
public class LibLinearL2 implements IAlgorithm {

    private byte i_lossType;
    private double d_c;
    private double d_eps;
    private double d_bias;

    public LibLinearL2(byte lossType, double c, double eps, double bias) {
        i_lossType = lossType;
        d_c = c;
        d_eps = eps;
        d_bias = bias;
    }

    @Override
    public double[] getWeight(AbstractKernel kernel, int currLabel) {
        final int MAX_ITER = 1000;

        //	Random   rand   = new Random(0);
        double[] QD = new double[kernel.N];
        double[] alpha = new double[kernel.N];
        double[] weight = new double[kernel.D];
        double U, G, d, alpha_old;

        int[] index = new int[kernel.N];
        byte[] aY = new byte[kernel.N];

        int active_size = kernel.N;
        int i, j, s, iter;
        byte yi;
        int[] xi;
        double[] vi = null;

        // PG: projected gradient, for shrinking and stopping
        double PG;
        double PGmax_old = Double.POSITIVE_INFINITY;
        double PGmin_old = Double.NEGATIVE_INFINITY;
        double PGmax_new, PGmin_new;

        // for loss function
        double[] diag = {0, 0, 0};
        double[] upper_bound = {d_c, 0, d_c};

        if (i_lossType == 2) {
            diag[0] = 0.5 / d_c;
            diag[2] = 0.5 / d_c;
            upper_bound[0] = Double.POSITIVE_INFINITY;
            upper_bound[2] = Double.POSITIVE_INFINITY;
        }

        for (i = 0; i < kernel.N; i++) {
            index[i] = i;
            aY[i] = (kernel.a_ys.get(i) == currLabel) ? (byte) 1 : (byte) -1;
            QD[i] = diag[GETI(aY, i)];

            if (kernel.b_binary) {
                QD[i] += kernel.a_xs.get(i).length;
            } else {
                for (double value : kernel.a_vs.get(i)) {
                    QD[i] += (value * value);
                }
            }

            if (d_bias > 0) {
                QD[i] += (d_bias * d_bias);
            }
        }

        for (iter = 0; iter < MAX_ITER; iter++) {
            PGmax_new = Double.NEGATIVE_INFINITY;
            PGmin_new = Double.POSITIVE_INFINITY;

            /*
             * for (i=0; i<active_size; i++) { int j = i +
             * r_rand.nextInt(active_size - i); swap(index, i, j);
			}
             */

            for (s = 0; s < active_size; s++) {
                i = index[s];
                yi = aY[i];
                xi = kernel.a_xs.get(i);
                if (!kernel.b_binary) {
                    vi = kernel.a_vs.get(i);
                }

                G = (d_bias > 0) ? weight[0] * d_bias : 0;
                for (j = 0; j < xi.length; j++) {
                    if (kernel.b_binary) {
                        G += weight[xi[j]];
                    } else {
                        G += (weight[xi[j]] * vi[j]);
                    }
                }

                G = G * yi - 1;
                G += alpha[i] * diag[GETI(aY, i)];
                U = upper_bound[GETI(aY, i)];

                if (alpha[i] == 0) {
                    if (G > PGmax_old) {
                        active_size--;
                        swap(index, s, active_size);
                        s--;
                        continue;
                    }

                    PG = Math.min(G, 0);
                } else if (alpha[i] == U) {
                    if (G < PGmin_old) {
                        active_size--;
                        swap(index, s, active_size);
                        s--;
                        continue;
                    }

                    PG = Math.max(G, 0);
                } else {
                    PG = G;
                }

                PGmax_new = Math.max(PGmax_new, PG);
                PGmin_new = Math.min(PGmin_new, PG);

                if (Math.abs(PG) > 1.0e-12) {
                    alpha_old = alpha[i];
                    alpha[i] = Math.min(Math.max(alpha[i] - G / QD[i], 0.0), U);
                    d = (alpha[i] - alpha_old) * yi;

                    if (d_bias > 0) {
                        weight[0] += d * d_bias;
                    }

                    for (j = 0; j < xi.length; j++) {
                        if (kernel.b_binary) {
                            weight[xi[j]] += d;
                        } else {
                            weight[xi[j]] += (d * vi[j]);
                        }
                    }
                }
            }

            if (PGmax_new - PGmin_new <= d_eps) {
                if (active_size == kernel.N) {
                    break;
                } else {
                    active_size = kernel.N;
                    PGmax_old = Double.POSITIVE_INFINITY;
                    PGmin_old = Double.NEGATIVE_INFINITY;
                    continue;
                }
            }

            PGmax_old = PGmax_new;
            PGmin_old = PGmin_new;
            if (PGmax_old <= 0) {
                PGmax_old = Double.POSITIVE_INFINITY;
            }
            if (PGmin_old >= 0) {
                PGmin_old = Double.NEGATIVE_INFINITY;
            }
        }

        int nSV = 0;

        for (i = 0; i < kernel.N; i++) {
            if (alpha[i] > 0) {
                ++nSV;
            }
        }

        StringBuilder build = new StringBuilder();

        build.append("- label = ");
        build.append(currLabel);
        build.append(": iter = ");
        build.append(iter);
        build.append(", nSV = ");
        build.append(nSV);

        AbstractTrainer.out.println(build.toString());

        return weight;
    }

    private int GETI(byte[] y, int i) {
        return y[i] + 1;
    }

    private void swap(int[] array, int idxA, int idxB) {
        int temp = array[idxA];
        array[idxA] = array[idxB];
        array[idxB] = temp;
    }
}