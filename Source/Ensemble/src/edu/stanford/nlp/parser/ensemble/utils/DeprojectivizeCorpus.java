/**
 * Retrieves the non-projective corpus from pseudo-projective one.
 */
package edu.stanford.nlp.parser.ensemble.utils;

import java.io.*;
import org.maltparser.core.exception.MaltChainedException;

/**
 *
 * @author Mojtaba Khallash
 */
public class DeprojectivizeCorpus {
    
    public static PrintStream out = System.out;
    
    public static void Deprojectivize(String workingDirectory, String input, 
            String Output, String model) 
            throws MaltChainedException, IOException {
        
        // args for malt
        String params = makeMaltEngineParameters(workingDirectory, input, 
            Output, model);

        // run malt
        Process p = Runtime.getRuntime().exec("java -Xmx2048m -jar lib" + File.separator + "maltParser.jar " + params);
        
        BufferedReader stdError = new BufferedReader(new InputStreamReader(
                p.getErrorStream()));
        
        String s;
        while ((s = stdError.readLine()) != null) {
            out.println(s);
        }
    }
    
    private static String makeMaltEngineParameters(String workingDirectory, 
            String input, String Output, String model) {
        
        StringBuilder pars = new StringBuilder();

        // Config Name = Pseudo-Projectivity
        pars.append("-c ");
        pars.append(model);

        // Processing Mode = Deprojectivize
        pars.append(" -m ");
        pars.append("deproj");

        // Input Config [file path - format]
        pars.append(" -i ");
        pars.append(workingDirectory).append(File.separator).append(input);

        // Output Config [file path - format]
        pars.append(" -o ");
        pars.append(workingDirectory).append(File.separator).append(Output);

        // Working Directory Path
        pars.append(" -w ");
        pars.append(workingDirectory);

        return pars.toString();
    }
}