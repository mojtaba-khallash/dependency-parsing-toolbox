/**
 * Copyright (c) 2007, Regents of the University of Colorado All rights
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

import clear.util.IOUtil;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Propbank reader.
 *
 * @author Jinho D. Choi <b>Last update:</b> 02/15/2010
 */
public class PBReader {

    /**
     * Scanner to read Propbank files
     */
    private Scanner f_prop;

    /**
     * Initializes the Propbank reader.
     *
     * @param propFile name of the Propbank file
     */
    public PBReader(String propFile) {
        f_prop = IOUtil.createFileScanner(propFile);
    }

    /**
     * Returns the next Propbank instance. If there is none, return null.
     */
    public PBInstance nextInstance() {
        String line;

        if (!f_prop.hasNextLine()) {
            f_prop.close();
            return null;
        }

        if ((line = f_prop.nextLine().trim()).isEmpty()) {
            f_prop.close();
            return null;
        }

        String[] str = line.split(PBLib.FIELD_DELIM);

        PBInstance instance = new PBInstance();
        instance.treePath = str[0];
        instance.treeIndex = Integer.parseInt(str[1]);
        instance.predicateId = Integer.parseInt(str[2]);
        instance.annotator = str[3];
        instance.type = str[4];
        instance.rolesetId = str[5];

        for (int i = 7; i < str.length; i++) {
            String sarg = str[i];
            int idx = sarg.indexOf(PBLib.PROP_LABEL_DELIM);
            String label = sarg.substring(idx + 1);
            String locs = sarg.substring(0, idx);
            PBArg pbArg = new PBArg(label, instance.predicateId);

            StringTokenizer tok = new StringTokenizer(locs, PBLib.PROP_ARG_OP, true);
            String argType = "";

            while (tok.hasMoreTokens()) {
                String next = tok.nextToken();

                if (next.length() == 1) {
                    argType = next;
                } else {
                    String[] loc = next.split(PBLib.PROP_LOC_DELIM);
                    int terminalId = Integer.parseInt(loc[0]);
                    int height = Integer.parseInt(loc[1]);

                    if (!pbArg.containsLoc(terminalId, height)) {
                        pbArg.addLoc(new PBLoc(argType, terminalId, height));
                    }
                    //	else
                    //		System.err.println("Duplicated location "+next+": "+line);
                }
            }

            instance.addArg(pbArg);
        }

        return instance;
    }
}