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

import clear.model.BinaryModel;
import clear.train.algorithm.IAlgorithm;
import clear.train.kernel.AbstractKernel;
import java.io.PrintStream;

/**
 * Binary trainer.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/8/2010
 */
public class BinaryTrainer extends AbstractTrainer {

    protected BinaryModel m_model;

    public BinaryTrainer(String modelFile, IAlgorithm algorithm, AbstractKernel kernel) {
        super(modelFile, algorithm, kernel, 1);
    }

    public BinaryTrainer(PrintStream fout, IAlgorithm algorithm, AbstractKernel kernel, int numThreads) {
        super(fout, algorithm, kernel, numThreads);
    }

    @Override
    public BinaryModel getModel() {
        return m_model;
    }

    @Override
    protected void initModel() {
        m_model = new BinaryModel(k_kernel);
    }

    @Override
    protected void train() {
        int curr_label = k_kernel.a_labels[0];

        out.println("\n* Training");
        m_model.copyWeight(a_algorithm.getWeight(k_kernel, curr_label));

        out.println("\n* Saving");

        if (s_modelFile != null) {
            m_model.save(s_modelFile);
        } else if (f_out != null) {
            m_model.save(f_out);
        }
    }
}