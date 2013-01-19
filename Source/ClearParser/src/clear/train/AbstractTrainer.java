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

import clear.model.AbstractModel;
import clear.train.algorithm.IAlgorithm;
import clear.train.kernel.AbstractKernel;
import java.io.PrintStream;

/**
 * Abstract trainer.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/8/2010
 */
abstract public class AbstractTrainer {

    static public final byte ST_BINARY = 0;
    static public final byte ST_ONE_VS_ALL = 1;
    protected String s_modelFile;	// output file
    protected PrintStream f_out;
    protected IAlgorithm a_algorithm;
    protected AbstractKernel k_kernel;
    protected int i_numThreads;
    
    public static PrintStream out = System.out;

    public AbstractTrainer(String modelFile, IAlgorithm algorithm, AbstractKernel kernel, int numThreads) {
        init(modelFile, null, algorithm, kernel, numThreads);
    }

    public AbstractTrainer(PrintStream fout, IAlgorithm algorithm, AbstractKernel kernel, int numThreads) {
        init(null, fout, algorithm, kernel, numThreads);
    }

    protected void init(String modelFile, PrintStream fout, IAlgorithm algorithm, AbstractKernel kernel, int numThreads) {
        s_modelFile = modelFile;
        f_out = fout;
        a_algorithm = algorithm;
        k_kernel = kernel;
        i_numThreads = numThreads;

        initModel();
        train();
    }

    abstract protected void initModel();

    abstract protected void train();

    abstract public AbstractModel getModel();
}