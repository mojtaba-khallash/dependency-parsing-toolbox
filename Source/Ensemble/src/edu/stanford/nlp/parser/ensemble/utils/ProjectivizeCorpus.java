/**
 * Changes the non-projective a corpus to pseudo-projective.
 */
package edu.stanford.nlp.parser.ensemble.utils;

import java.io.*;
import org.maltparser.core.exception.MaltChainedException;

/**
 *
 * @author Mojtaba Khallash
 */
public class ProjectivizeCorpus {
    
    public static PrintStream out = System.out;
    
    public static void Projectivize(String workingDirectory, String input, 
            String Output, String model) throws MaltChainedException,
            IOException {
        Projectivize(workingDirectory, input, Output, model, "head");
    }

    public static void Projectivize(String workingDirectory, String input, 
            String Output, String model, String markingStrategy) 
            throws MaltChainedException, IOException {
        Projectivize(workingDirectory, input, Output, model, markingStrategy, "head");
    }

    public static void Projectivize(String workingDirectory, String input, 
            String Output, String model, String markingStrategy, 
            String coveredRoot) throws MaltChainedException, IOException {
        Projectivize(workingDirectory, input, Output, model, markingStrategy, coveredRoot, "shortest");
    }

    public static void Projectivize(String workingDirectory, String input, 
            String Output, String model, String markingStrategy, 
            String coveredRoot, String liftingOrder) 
            throws MaltChainedException, IOException {
        
        // args for malt
        String params = makeMaltEngineParameters(workingDirectory, input, 
            Output, model, markingStrategy, coveredRoot, liftingOrder);

        // run malt
        Process p = Runtime.getRuntime().exec("java -Xmx2048m -jar lib" + File.separator + "maltParser.jar " + params);
        
        BufferedReader stdError = new BufferedReader(new InputStreamReader(
                p.getErrorStream()));
        
        String s;
        while ((s = stdError.readLine()) != null) {
            out.println(s);
        }
    }
    
    private static String makeMaltEngineParameters(String workingDirectory, String input, 
            String Output, String model, String markingStrategy, 
            String coveredRoot, String liftingOrder) {
        
        StringBuilder pars = new StringBuilder();
        
        // Config Name = Pseudo-Projectivity
        pars.append("-c ");
        pars.append(model);

        // Processing Mode = Projectivize
        pars.append(" -m ");
        pars.append("proj");

        // Input Config [file path - format]
        pars.append(" -i ");
        pars.append(workingDirectory).append(File.separator).append(input);

        // Output Config [file path - format]
        pars.append(" -o ");
        pars.append(workingDirectory).append(File.separator).append(Output);

        // Marking Strategy [none, baseline, head, path, head+path]
        pars.append(" -pp ");
        pars.append(markingStrategy);

        // Covered Root [none - left - right - head - ignore]
        pars.append(" -pcr ");
        pars.append(coveredRoot);

        // Lifting Order [shortest - deepest]
        pars.append(" -plo ");
        pars.append(liftingOrder);

        // Working Directory Path
        pars.append(" -w ");
        pars.append(workingDirectory);
        
        return pars.toString();
    }
}
