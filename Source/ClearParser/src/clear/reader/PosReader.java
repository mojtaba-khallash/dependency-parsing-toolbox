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
package clear.reader;

import clear.dep.DepNode;
import clear.dep.DepTree;
import java.io.IOException;

/**
 * Part-of-speech dependency reader.
 *
 * @author Jinho D. Choi <b>Last update:</b> 6/26/2010
 */
public class PosReader extends AbstractReader<DepNode, DepTree> {

    /**
     * Initializes the dependency reader for
     * <code>filename</code>.
     *
     * @param filename name of the file containing dependency trees
     */
    public PosReader(String filename) {
        super(filename);
    }

    /**
     * Returns the next dependency tree. If there is no more tree, returns null.
     */
    @Override
    public DepTree nextTree() {
        DepTree tree = new DepTree();
        boolean isNext = false;

        try {
            isNext = appendNextTree(tree);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isNext ? tree : null;
    }

    @Override
    protected DepNode toNode(String line, int id) {
        DepNode node = new DepNode();
        String[] str = line.split(FIELD_DELIM);
        node.id = id;
        node.form = str[0];
        node.pos = str[1];

        return node;
    }
}