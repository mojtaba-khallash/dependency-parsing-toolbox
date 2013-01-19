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
import com.carrotsearch.hppc.IntArrayList;
import java.io.BufferedReader;

/**
 * Abstract model for multi-classification.
 *
 * @author Jinho D. Choi <b>Last update:</b> 11/5/2010
 */
abstract public class AbstractMultiModel extends AbstractModel {

    public AbstractMultiModel(AbstractKernel kernel) {
        super(kernel);
    }

    public AbstractMultiModel(String modelFile) {
        super(modelFile);
    }

    public AbstractMultiModel(BufferedReader fin) {
        super(fin);
    }

    public AbstractMultiModel(int nLabels, int nFeatures, int[] aLabels, double[] dWeights) {
        super(nLabels, nFeatures, aLabels, dWeights);
    }

    abstract public void copyWeight(int label, double[] weight);

    abstract public double[] getScores(int[] x);

    abstract public double[] getScores(IntArrayList x);
}