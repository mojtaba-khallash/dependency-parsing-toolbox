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
package clear.ftr.map;

import clear.ftr.xml.SRLFtrXml;
import java.io.BufferedReader;

/**
 * Semantic role labeling feature map.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/12/2010
 */
public class SRLFtrMap extends AbstractFtrMap {

    static private final int N_EXTRA = 2;

    public SRLFtrMap(SRLFtrXml xml) {
        super(xml, N_EXTRA);
    }

    public SRLFtrMap(String lexiconFile) {
        super(lexiconFile);
    }

    public SRLFtrMap(BufferedReader fin) {
        super(fin);
    }
}