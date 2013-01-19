package ir.ac.iust.nlp.dependencyparser.evaluation;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mojtaba Khallash
 */
public class EvalSettings {
    public String goldFile;
    public String parseFile;
    public String outputFile;
    public String metrics = "LAS";
    public String groupByVal = "Token";
    public int minSentenceLength = -1; // all length
    public int maxSentenceLength = -1; // all length
    public String[] ExcludeKeys = null;
    public String[] ExcludeVals = null;
    public boolean useConfusionMatrix = false;
    public boolean showDetails = false;
    public boolean showHeaderInfo = true;
    public boolean showRowInfo = true;
    public boolean mergeTables = true;
    public boolean useTabSeparate = false;
    public String pattern = "0.000";
    
    public String[] getParameters() {
        List<String> pars = new LinkedList<>();
         
        // gold file
        pars.add("-g");
        pars.add(goldFile);
        
        // parse file
        pars.add("-s");
        pars.add(parseFile);
        
        // output
        pars.add("--output");
        pars.add(outputFile);
        
        // evaluation metrics
        pars.add("--Metric");
        pars.add(metrics);

        // GroupBy
        pars.add("--GroupBy");
        pars.add(groupByVal);
        
        // Sentence Length
        if (minSentenceLength != -1) {
            pars.add("--MinSentenceLength");
            pars.add(String.valueOf(minSentenceLength));
        }
        if (maxSentenceLength != -1) {
            pars.add("--MaxSentenceLength");
            pars.add(String.valueOf(maxSentenceLength));
        }
        
        // Exclude
        if (ExcludeKeys != null) {
            for (int i = 0; i < ExcludeKeys.length; i++) {
                pars.add("--Exclude" + ExcludeKeys[i]);
                pars.add(ExcludeVals[i]);
            }
        }
        
        // Confusion Matrix
        pars.add("--confusion-matrix");
        if (useConfusionMatrix == true) {
            pars.add("1");
        } else {
            pars.add("0");
        }

        // details
        pars.add("--details");
        if (showDetails == true) {
            pars.add("1");
        } else {
            pars.add("0");
        }
        
        // header info
        pars.add("--header-info");
        if (showHeaderInfo == true) {
            pars.add("1");
        } else {
            pars.add("0");
        }
        
        // row header
        pars.add("--row-header");
        if (showRowInfo == true) {
            pars.add("1");
        } else {
            pars.add("0");
        }
        
        // merge tables
        pars.add("--merge-tables");
        if (mergeTables == true) {
            pars.add("1");
        } else {
            pars.add("0");
        }
 
        // tab separate
        pars.add("--tab");
        if (useTabSeparate == true) {
            pars.add("1");
        } else {
            pars.add("0");
        }

        // pattern
        pars.add("--pattern");
        pars.add(pattern);
                
        return pars.toArray(new String[0]);
    }
}