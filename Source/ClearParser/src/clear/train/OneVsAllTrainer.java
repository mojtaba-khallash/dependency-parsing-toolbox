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
package clear.train;

import clear.model.OneVsAllModel;
import clear.train.algorithm.IAlgorithm;
import clear.train.kernel.AbstractKernel;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * One-vs-all trainer.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/8/2010
 */
public class OneVsAllTrainer extends AbstractTrainer {

    volatile protected OneVsAllModel m_model;

    public OneVsAllTrainer(String modelFile, IAlgorithm algorithm, AbstractKernel kernel, int numThreads) {
        super(modelFile, algorithm, kernel, numThreads);
    }

    public OneVsAllTrainer(PrintStream fout, IAlgorithm algorithm, AbstractKernel kernel, int numThreads) {
        super(fout, algorithm, kernel, numThreads);
    }

    @Override
    public OneVsAllModel getModel() {
        return m_model;
    }

    @Override
    protected void initModel() {
        m_model = new OneVsAllModel(k_kernel);
    }

    @Override
    protected void train() {
        ExecutorService executor = Executors.newFixedThreadPool(i_numThreads);
        out.println("\n* Training");

        for (int currLabel = 0; currLabel < k_kernel.L; currLabel++) {
            executor.execute(new TrainTask(currLabel));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            out.println("\n* Saving");

            if (s_modelFile != null) {
                m_model.save(s_modelFile);
            } else if (f_out != null) {
                m_model.save(f_out);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class TrainTask implements Runnable {

        /**
         * Current label to train
         */
        int curr_label;

        /**
         * Trains a one-vs-all model using {@link AbstractTrainer#a_xs} and {@link AbstractTrainer#a_ys}
         * with respect to
         * <code>currLabel</code>.
         *
         * @param currLabel current label to train ({@link this#curr_label})
         */
        public TrainTask(int currLabel) {
            curr_label = currLabel;
        }

        @Override
        public void run() {
            m_model.copyWeight(curr_label, a_algorithm.getWeight(k_kernel, k_kernel.a_labels[curr_label]));
        }
    }
}