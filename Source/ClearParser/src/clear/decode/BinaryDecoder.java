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
package clear.decode;

import clear.model.BinaryModel;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Binary decoder.
 *
 * @author Jinho D. Choi <br><b>Last update:</b> 11/8/2010
 */
public class BinaryDecoder extends AbstractDecoder {

    protected BinaryModel m_model;

    public BinaryDecoder(String modelFile) {
        m_model = new BinaryModel(modelFile);
    }

    public BinaryDecoder(BufferedReader fin) {
        m_model = new BinaryModel(fin);
    }

    public BinaryDecoder(BinaryModel model) {
        m_model = model;
    }

    @Override
    public JIntDoubleTuple predict(int[] x) {
        return predictAux(m_model.getScore(x));
    }

    @Override
    public JIntDoubleTuple predict(IntArrayList x) {
        return predictAux(m_model.getScore(x));
    }

    @Override
    public JIntDoubleTuple predict(JIntDoubleTuple[] x) {
        return predictAux(m_model.getScore(x));
    }

    @Override
    public JIntDoubleTuple predict(ArrayList<JIntDoubleTuple> x) {
        return predictAux(m_model.getScore(x));
    }

    private JIntDoubleTuple predictAux(double score) {
        if (score > 0) {
            return new JIntDoubleTuple(m_model.a_labels[0], score);
        } else {
            return new JIntDoubleTuple(m_model.a_labels[1], -score);
        }
    }
}