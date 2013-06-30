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
import java.text.DecimalFormat;
import mstparser.DependencyInstance;

/**
 * A writer to create files in CONLL format.
 *
 * <p> Created: Sat Nov 10 15:25:10 2001 </p>
 *
 * @author Jason Baldridge
 * @version $Id: CONLLWriter.java 94 2007-01-17 17:05:12Z jasonbaldridge $
 * @see mstparser.io.DependencyWriter
 */
public class CONLLWriter extends DependencyWriter {

    public CONLLWriter(boolean labeled) {
        this.labeled = labeled;
    }

    @Override
    public void write(DependencyInstance instance) throws IOException {
        DecimalFormat df = null;
        if (instance.confidenceScores != null) {
            df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
        }
        int numInd = 0;
        String tmp;
        for (int i = 0; i < instance.length(); i++) {
            // Id
            writer.write(Integer.toString(i + 1));
            writer.write('\t');
            
            // word form
            tmp = instance.forms[i];
            if (tmp.equals("<num>") && instance.numbers.size() > 0) {
                tmp = instance.numbers.get(numInd);
                numInd++;
            } 
            writer.write(tmp);
            writer.write('\t');
            
            // lemm
            if (instance.lemmas != null) {
                tmp = instance.lemmas[i+1];
                if (tmp.equals("<num>") && instance.numbers.size() > 0) {
                    tmp = instance.numbers.get(numInd);
                    numInd++;
                } 
            }
            writer.write(tmp);
            writer.write('\t');
            
            // cpostags
            writer.write(instance.cpostags[i]);
            writer.write('\t');
            
            // postags
            if (instance.postags != null) {
                writer.write(instance.postags[i + 1]);
            }
            else {
                writer.write(instance.cpostags[i]);
            }
            writer.write('\t');
            
            // feats
            StringBuilder feats = new StringBuilder();
            if (instance.feats != null) {
                for(int j = 0; j < instance.feats[i+1].length; j++) {
                    if (j!= 0) {
                        feats.append("|");
                    }
                    feats.append(instance.feats[i+1][j]);
                }
                if (feats.length() == 0) {
                    feats = new StringBuilder("_");
                }
            }
            else {
                feats = new StringBuilder("_");
            }
            writer.write(feats.toString());
            writer.write('\t');
            
	    // afm 03-07-08
	    if (instance.stacked)
	    {
                // predicted head
                writer.write(Integer.toString(instance.heads_pred[i]));
                writer.write('\t');
                
                // predicted deprel
                writer.write(instance.deprels_pred[i]);
                writer.write('\t');	    	
	    }

            // head
            writer.write(Integer.toString(instance.heads[i]));
            writer.write('\t');
            
            // deprel
            writer.write(instance.deprels[i]);
            writer.write('\t');
            
            // phead and pdeprel
            writer.write("_\t_");
            
            // confidence scores
            if (instance.confidenceScores != null) {
                writer.write('\t');
                writer.write(df.format(instance.confidenceScores[i]));
            }
            writer.newLine();
        }
        writer.newLine();

    }
}