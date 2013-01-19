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
package clear.train.kernel;

/**
 * Linear kernel.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
public class PolynomialKernel extends NoneKernel {

    public int i_degree;
    public double d_gamma;
    public double d_coef;
    public double[][] d_kernel;

    public PolynomialKernel(String instanceFile, int degree, double gamma, double coef) {
        super(instanceFile);
        initKernel(degree, gamma, coef);
    }

    private void initKernel(int degree, double gamma, double coef) {
        kernel_type = KERNEL_POLYNOMIAL;
        i_degree = degree;
        d_gamma = gamma;
        d_coef = coef;
        d_kernel = new double[N][N];

        int i, j;
        double scala;
        int[] xi, xj;
        double[] vi = null, vj;

        for (i = 0; i < N; i++) {
            xi = a_xs.get(i);
            if (!b_binary) {
                vi = a_vs.get(i);
            }

            for (j = 0; j < N; j++) {
                xj = a_xs.get(j);

                if (b_binary) {
                    scala = getScala(xi, xj);
                } else {
                    vj = a_vs.get(j);
                    scala = getScala(xi, xj, vi, vj);
                }

                d_kernel[i][j] = getPolyValue(scala, gamma, coef, degree);
            }
        }
    }

    static public double getPolyValue(double scala, double gamma, double coef, int degree) {
        return Math.pow(scala * gamma + coef, degree);
    }
}