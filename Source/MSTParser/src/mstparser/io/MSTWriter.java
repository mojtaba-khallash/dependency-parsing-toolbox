///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
// 
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////
package mstparser.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import mstparser.DependencyInstance;
import mstparser.Util;

/**
 * A writer to create files in MST format.
 *
 * <p> Created: Sat Nov 10 15:25:10 2001 </p>
 *
 * @author Jason Baldridge
 * @version $Id: MSTWriter.java 94 2007-01-17 17:05:12Z jasonbaldridge $
 * @see mstparser.io.DependencyWriter
 */
public class MSTWriter extends DependencyWriter {

    public MSTWriter(boolean labeled) {
        this.labeled = labeled;
    }

    @Override
    public void write(DependencyInstance instance) throws IOException {
        writer.write(Util.join(instance.forms, '\t') + "\n");
        StringBuilder sb = new StringBuilder();
        sb.append(instance.postags[1]);
        for (int i = 2; i < instance.postags.length; i++) {
            sb.append("\t").append(instance.postags[i]);
        }
        writer.write(sb.toString() + "\n");
        if (labeled) {
            writer.write(Util.join(instance.deprels, '\t') + "\n");
        }
        writer.write(Util.join(instance.heads, '\t') + "\n");
        if (instance.confidenceScores != null) {
            writer.write(Util.join(instance.confidenceScores, '\t', 3) + "\n");
        }
        writer.write("\n");
    }
}