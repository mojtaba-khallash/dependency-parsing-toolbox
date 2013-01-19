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
package clear.propbank;

/**
 * Propbank library.
 *
 * @author Jinho D. Choi <b>Last update:</b> 2/5/2010
 */
public class PBLib {

    /**
     * Delimiter between label and locations as in Propbank (e.g., 0:1-ARG0)
     */
    static public final String PROP_LABEL_DELIM = "-";
    /**
     * Delimiter between terminalIndex and height as in Propbank (e.g., 0:1)
     */
    static public final String PROP_LOC_DELIM = ":";
    /**
     * Annotation operators as in Propbank (e.g., *)
     */
    static public final String PROP_ARG_OP = "*,;&";
    static public final String PROP_OP_ANTE = "@";
    static public final String PROP_OP_COMP = "r";
    static public final String PROP_OP_SKIP = ";";
    /**
     * Delimiter between label and IDs (e.g., ARG0:1,2)
     */
    static public String LABEL_DELIM = ":";
    /**
     * Delimiter between IDs (e.g., 1,2)
     */
    static public String ID_DELIM = ",";
    /**
     * Delimiter between fields (e.g., <treeFile> <treeIndex> ..)
     */
    static public String FIELD_DELIM = " ";
}