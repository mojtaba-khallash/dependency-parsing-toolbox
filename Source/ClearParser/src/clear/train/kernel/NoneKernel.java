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

import clear.util.DSUtil;
import clear.util.IOUtil;
import clear.util.tuple.JObjectObjectTuple;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntOpenHashSet;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Linear kernel.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
public class NoneKernel extends AbstractKernel {

    public NoneKernel() {
        super(KERNEL_NONE);
    }

    public NoneKernel(String instanceFile) {
        super(KERNEL_NONE, instanceFile);
    }

    /**
     * Reads training instances from
     * <code>instanceFile</code> and stores to
     * {@link AbstractKernel#a_ys} and {@link AbstractKernel#a_xs}.
     *
     * @param instanceFile name of a file containing training instances
     */
    @Override
    protected void init(String instanceFile) throws Exception {
        final int NUM = 1000000;

        BufferedReader fin = IOUtil.createBufferedFileReader(instanceFile);

        a_ys = new IntArrayList(NUM);
        a_xs = new ArrayList<>(NUM);

        IntOpenHashSet sLabels = new IntOpenHashSet();
        String line;
        String[] tok, tmp;
        int y, i;
        int[] x;
        double[] v;

        for (N = 0; (line = fin.readLine()) != null; N++) {
            if (N == 0 && line.contains(AbstractKernel.FTR_DELIM)) {
                b_binary = false;
                a_vs = new ArrayList<>(NUM);
            }

            tok = line.split(COL_DELIM);
            y = Integer.parseInt(tok[0]);
            a_ys.add(y);

            if (b_binary) {
                x = DSUtil.toIntArray(tok, 1);
                a_xs.add(x);
            } else {
                x = new int[tok.length - 1];
                v = new double[tok.length - 1];

                for (i = 1; i < tok.length; i++) {
                    tmp = tok[i].split(FTR_DELIM);
                    x[i - 1] = Integer.parseInt(tmp[0]);
                    v[i - 1] = Double.parseDouble(tmp[1]);
                }

                a_xs.add(x);
                a_vs.add(v);
            }

            // indices in feature are in ascending order
            D = Math.max(D, x[x.length - 1]);
            sLabels.add(y);

            if (N % 100000 == 0) {
                out.print("\r* Initializing  : " + (N / 1000) + "K");
            }
        }
        out.println("\r* Initializing  : " + instanceFile);

        fin.close();
        a_ys.trimToSize();
        a_xs.trimToSize();

        // feature dimension = last feature-index + 1
        D++;

        // sort labels;
        a_labels = sLabels.toArray();
        Arrays.sort(a_labels);
        L = a_labels.length;

        out.println("- # of instances: " + N);
        out.println("- # of labels   : " + L);
        out.println("- # of features : " + D);
    }

    public void add(JObjectObjectTuple<IntArrayList, ArrayList<int[]>> yx) throws Exception {
        a_ys = yx.o1;
        a_xs = yx.o2;
        N = a_ys.size();

        IntOpenHashSet sLabels = new IntOpenHashSet();
        int y, i;
        int[] x;

        out.println("* Initializing  : " + N);

        for (i = 0; i < N; i++) {
            y = a_ys.get(i);
            x = a_xs.get(i);
            D = Math.max(D, x[x.length - 1]);
            sLabels.add(y);
        }

        a_ys.trimToSize();
        a_xs.trimToSize();

        // feature dimension = last feature-index + 1
        D++;

        // sort labels;
        a_labels = sLabels.toArray();
        Arrays.sort(a_labels);
        L = a_labels.length;

        out.println("- # of instances: " + N);
        out.println("- # of labels   : " + L);
        out.println("- # of features : " + D);
    }

    public void addValueArray(JObjectObjectTuple<IntArrayList, ArrayList<double[]>> yx) throws Exception {
        a_ys = yx.o1;
        a_vs = yx.o2;
        a_xs = new ArrayList<>();

        N = a_ys.size();
        D = a_vs.get(0).length;

        IntOpenHashSet sLabels = new IntOpenHashSet();
        int y, i;
        int[] x = new int[D];

        for (i = 0; i < D; i++) {
            x[i] = i + 1;
        }

        out.println("* Initializing  : " + N);

        for (i = 0; i < N; i++) {
            a_xs.add(x);
            y = a_ys.get(i);
            sLabels.add(y);
        }

        a_ys.trimToSize();
        a_xs.trimToSize();
        a_vs.trimToSize();

        // feature dimension = last feature-index + 1
        D++;

        // sort labels;
        a_labels = sLabels.toArray();
        Arrays.sort(a_labels);
        L = a_labels.length;

        out.println("- # of instances: " + N);
        out.println("- # of labels   : " + L);
        out.println("- # of features : " + D);
    }
}