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

import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Abstract kernel.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
abstract public class AbstractKernel {

    static public final byte KERNEL_NONE = 0;
    static public final byte KERNEL_POLYNOMIAL = 1;
    static public final byte KERNEL_DAEM = 2;
    /**
     * Delimiter between index and value (e.g. 3:0.12)
     */
    static public final String FTR_DELIM = ":";
    /**
     * Delimiter between columns (e.g. 0:0.12 3:0.45)
     */
    static public final String COL_DELIM = " ";
    /**
     * Total number of training instances
     */
    public int N;
    /**
     * Total number of features
     */
    public int D;
    /**
     * Total number of labels
     */
    public int L;
    /**
     * List of labels
     */
    public int[] a_labels;
    /**
     * Training labels
     */
    public IntArrayList a_ys;
    /**
     * Training feature indices
     */
    public ArrayList<int[]> a_xs;
    /**
     * Training feature values
     */
    public ArrayList<double[]> a_vs;
    /**
     * Kernel type
     */
    public byte kernel_type;
    /**
     * true if binary features only
     */
    public boolean b_binary;
    
    public PrintStream out = System.out;

    public AbstractKernel(byte kernelType) {
        kernel_type = kernelType;
        b_binary = true;
    }

    /**
     * Calls {@link AbstractKernel#init(String)}
     *
     * @param instanceFile name of a file containing training instances
     */
    public AbstractKernel(byte kernelType, String instanceFile) {
        this(kernelType);

        try {
            init(instanceFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public int getScala(int[] xi, int[] xj) {
        int scala = 0, i;

        for (i = 0; i < xi.length; i++) {
            if (Arrays.binarySearch(xj, xi[i]) >= 0) {
                scala++;
            }
        }

        return scala;
    }

    static public int getScala(IntArrayList xi, int[] xj) {
        int scala = 0, i;

        for (i = 0; i < xi.size(); i++) {
            if (Arrays.binarySearch(xj, xi.get(i)) >= 0) {
                scala++;
            }
        }

        return scala;
    }

    static public double getScala(int[] xi, int[] xj, double[] vi, double[] vj) {
        double scala = 0;
        int i, j;

        for (i = 0; i < xi.length; i++) {
            if ((j = Arrays.binarySearch(xj, xi[i])) >= 0) {
                scala += vi[i] * vj[j];
            }
        }

        return scala;
    }

    static public double getScala(ArrayList<JIntDoubleTuple> xvi, int[] xj, double[] vj) {
        double scala = 0;
        int j;

        for (JIntDoubleTuple tup : xvi) {
            if ((j = Arrays.binarySearch(xj, tup.i)) >= 0) {
                scala += tup.d * vj[j];
            }
        }

        return scala;
    }

    /**
     * Normalizes a weight vector.
     */
    static public void normalize(double[] weight) {
        double norm = 0;

        for (int i = 0; i < weight.length; i++) {
            norm += (weight[i] * weight[i]);
        }

        norm = Math.sqrt(norm);

        for (int i = 0; i < weight.length; i++) {
            weight[i] /= norm;
        }
    }

    /**
     * Kernelizes this feature space
     */
    abstract protected void init(String instanceFile) throws Exception;
}