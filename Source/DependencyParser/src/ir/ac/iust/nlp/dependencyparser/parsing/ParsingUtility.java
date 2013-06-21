package ir.ac.iust.nlp.dependencyparser.parsing;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class ParsingUtility {

    /*
     * To run this example requires that you have ran TrainingExperiment that
     * creates model0.mco.
     * Sample sentenceTokens 
     *      // Creates an array of tokens, which contains the Swedish sentence 
     *      // 'Grundavdraget upphör alltså vid en taxerad inkomst på 52500 kr.' 
     *      // in the CoNLL data format. String[]
     *      tokens = new String[11]; tokens[0] = "1\tGrundavdraget\t_\tN\tNN\tDD|SS";
     *      tokens[1] = "2\tupphör\t_\tV\tVV\tPS|SM"; 
     *      tokens[2] = "3\talltså\t_\tAB\tAB\tKS"; 
     *      tokens[3] = "4\tvid\t_\tPR\tPR\t_"; 
     *      tokens[4] = "5\ten\t_\tN\tEN\t_"; 
     *      tokens[5] = "6\ttaxerad\t_\tP\tTP\tPA"; 
     *      tokens[6] = "7\tinkomst\t_\tN\tNN\t_"; 
     *      tokens[7] = "8\tpå\t_\tPR\tPR\t_"; 
     *      tokens[8] = "9\t52500\t_\tR\tRO\t_"; 
     *      tokens[9] = "10\tkr\t_\tN\tNN\t_"; 
     *      tokens[10] = "11\t.\t_\tP\tIP\t_";
     *
     * @author Johan Hall
     */
    public static DependencyStructure ParseSentence(
            String[] sentenceTokens, String workingDir,
            String modelPath, String logFile) throws MaltChainedException {
        return (DependencyStructure) ParseFromInputToken(1, sentenceTokens, workingDir, 
                modelPath, logFile);
    }
    
    public static String[] ParseSentence1(
            String[] sentenceTokens, String workingDir,
            String modelPath, String logFile) throws MaltChainedException {
        return (String[]) ParseFromInputToken(2, sentenceTokens, workingDir, 
                modelPath, logFile);
    }
    
    private static Object ParseFromInputToken(int version, String[] tokens,
            String workingDir, String modelName, String logName) 
            throws MaltChainedException {
        Object result = null;
        MaltParserService service = null;
        try {
            service = new MaltParserService();

            // Inititalize:
            //      1. the parser model         modelPath
            //      2. the working directory    workingDir
            //      3. the logging file         logName
            service.initializeParserModel(
                       "-c " + modelName
                    + " -m parse"
                    + " -w " + workingDir
                    + " -lfi " + logName);
            
            switch (version) {
                /*
                 * This example shows how to parse a sentence with MaltParser by
                 * first initialize a parser model.
                 */
                case 1:
                    // Parses the Swedish sentence above
                    DependencyStructure graph = service.parse(tokens);

                    // Outputs the dependency graph created by MaltParser.
                    result = graph;
                    break;

                /*
                 * This example shows how to parse a sentence with MaltParser by
                 * first initialize a parser model. This example is the same as
                 * case 1 except that we use the parseTokens method in
                 * MaltParserService that returns an array of tokens with
                 * information about it head index and dependency type.
                 */
                case 2:
                    // Parses the Swedish sentence above
                    result = service.parseTokens(tokens);
                    break;
            }
        }
        finally {
            if (service != null) {
                // Terminates the parser model
                service.terminateParserModel();
            }
        }
        
        return result;
    }

    /*
     * This example shows how to parse sentences from file. The only difference
     * between example case 1 is that the input is read from file
     * 'data/talbanken05_test.conll' and written to 'out.conll' in the CoNLL
     * data format.
     *
     * To run this example requires that you have ran TrainingExperiment that
     * creates model0.mco
     *
     * @author Johan Hall
     */
    public static List<DependencyStructure> ParseFromFile(String workingDir, 
            String modelName, String inputPath, String outputPath, 
            String logName) throws MaltChainedException {
        List<DependencyStructure> dss  = new LinkedList<>();
        BufferedReader reader = null;
        BufferedWriter writer = null;
        MaltParserService service = null;
        try {
            service = new MaltParserService();

            // Inititalize:
            //      1. the parser model         modelName
            //      2. the working directory    workingDir
            //      3. the logging file         logName
            service.initializeParserModel(
                       "-c " + modelName 
                    + " -m parse" 
                    + " -w " + workingDir
                    + " -lfi " + logName);
                        
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(workingDir + File.separator + inputPath), "UTF-8"));
            
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(workingDir + File.separator + outputPath), "UTF-8"));
            
            String line;
            ArrayList<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    DependencyStructure graph = service.parse(lines.toArray(new String[lines.size()]));
                    dss.add(graph);
                    for (int i = 1; i <= graph.getHighestDependencyNodeIndex(); i++) {
                        DependencyNode node = graph.getDependencyNode(i);
                        if (node != null) {
                            for (SymbolTable table : node.getLabelTypes()) {
                                writer.write(node.getLabelSymbol(table) + "\t");
                            }
                            if (node.hasHead()) {
                                Edge e = node.getHeadEdge();
                                writer.write(e.getSource().getIndex() + "\t");
                                if (e.isLabeled()) {
                                    for (SymbolTable table : e.getLabelTypes()) {
                                        writer.write(e.getLabelSymbol(table) + "\t");
                                    }
                                } else {
                                    for (SymbolTable table : graph.getDefaultRootEdgeLabels().keySet()) {
                                        writer.write(graph.getDefaultRootEdgeLabelSymbol(table) + "\t");
                                    }
                                }
                            }
                            writer.write('\n');
                            writer.flush();
                        }
                    }
                    writer.write('\n');
                    writer.flush();
                    lines.clear();
                } else {
                    lines.add(line);
                }
            }
        } finally {
            if (reader != null) {
                try { reader.close(); } 
                catch(Exception ex){}
            }
            if (writer != null) {
                try { 
                    writer.flush();
                    writer.close();
                } catch(Exception ex){}
            }
            if (service != null) {
                service.terminateParserModel();
            }

            return dss;
        }
    }
    
    /**
    * 
    * To run this example requires that you have ran TrainingExperiment that creates model0.mco and model1.mco
    * 
    * @author Johan Hall
    */
    public static void ParseFromFile1(String workingDir, String modelName, 
            String inputPath, String outputPath, String logName) 
            throws MaltChainedException {
        // Inititalize:
        //      1. the parser model         modelName
        //      2. the working directory    workingDir
        //      3. the logging file         logName
        new MaltParserService(0).runExperiment(
                   "-c " + modelName
                + " -m parse" 
                + " -i "+ workingDir + File.separator + inputPath
                + " -o " + workingDir + File.separator + outputPath
                + " -w " + workingDir
                + " -lfi " + logName);
    }
}