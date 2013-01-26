package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.io.*;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mojtaba Khallash
 */
public class MaltStackSettings extends MaltSettings {
    
    // 0
    public int Level;

    public int AugmentNParts = 5;

    public String AugmentedTrainFile;
    
    public MaltStackSettings() {}
    
    public MaltStackSettings(MaltStackSettings settings) {
        super(settings);
        
        this.Level = settings.Level;
        this.AugmentNParts = settings.AugmentNParts;
        this.AugmentedTrainFile = settings.AugmentedTrainFile;
    }
    
    @Override
    public void preProcess() throws IOException {
        switch (Chart) {
            case Train:
                createAgmentedParts(AugmentNParts, WorkingDirectory, Input);
                break;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Create Agmented Parts">
    private void createAgmentedParts(int parts, String workingDir, String inputFile) 
            throws FileNotFoundException,
                   UnsupportedEncodingException,
                   IOException {
        //==== Create N writer for train and test ====//
        Writer[] trainParts = new Writer[parts];
        Writer[] testParts = new Writer[parts];
        for (int i = 0; i < parts; i++) {
            String train = workingDir + "_train" + i + ".conll";
            trainParts[i] = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(train, true), "UTF-8"));

            String test = workingDir + "_test" + i + ".conll";
            testParts[i] = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(test, true), "UTF-8"));
        }
        
        //==== Read input file ====//
        HashMap<Integer, String> Sentences = readSentemces(inputFile);
        int numInstances = Sentences.size();
        
        // The last partition becomes bigger
        int numInstancesPerPart = numInstances / parts; 
        
        //==== Write data in separate files ====//
        for (int i = 0; i < numInstances; i++) {
            String sen = Sentences.get(i + 1);
            for (int j = 0; j < parts; j++) {
                if ( (i >= j * numInstancesPerPart
                        && i < (j + 1) * numInstancesPerPart) ||
                        (j == parts - 1 && i >= parts * numInstancesPerPart)) {
                    testParts[j].write(sen + "\n");
                } else {
                    trainParts[j].write(sen + "\n");
                }
            }
        }
        
        //==== Close writer stream ====//
        for (int i = 0; i < parts; i++) {
            trainParts[i].close();
            testParts[i].close();
        }
    }
    
    private HashMap<Integer, String> readSentemces(String path) throws IOException {
        HashMap<Integer, String> Sentences = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            StringBuilder tokens = new StringBuilder();
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() != 0) {
                    tokens.append(line).append("\n");
                } else {
                    if (tokens.length() > 0) {
                        count++;
                        Sentences.put(count, tokens.toString());
                        tokens = new StringBuilder();
                    }
                }
            }
        }
        return Sentences;
    }
    // </editor-fold>
    
    @Override
    public void postProcess() throws IOException {
        switch (Chart) {
            case Train:
                String trainFile = WorkingDirectory + "train_pred.conll";

                File trainTarget = new File(trainFile);
                if (trainTarget != null) {
                    try { FileUtils.forceDelete(trainTarget); }
                    catch (Exception e) {}
                }
                
                for (int i = 0; i < AugmentNParts; i++) {
                    // merge to train_pred
                    String test = WorkingDirectory + "_test" + i + ".conll";
                    String parse = WorkingDirectory + "_parse" + i + ".conll";
                    
                    createPredFile(test, parse, trainFile);
                }
                
                copyToDestination(trainFile, AugmentedTrainFile);
                break;
            case Parse:
                // merge to test_pred
                String testFile = WorkingDirectory + "test_pred.conll";

                File testTarget = new File(testFile);
                if (testTarget != null) {
                    try { FileUtils.forceDelete(testTarget); }
                    catch (Exception e) {}
                }
                    
                createPredFile(Gold, Output, testFile);
                copyToDestination(testFile, Output);
                break;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Create Pred File">
    private void createPredFile(String gold, String parse, String destination) 
            throws IOException{
       Writer pred = null;
       BufferedReader reader1 = null, 
                      reader2 = null;
        try {
            pred = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(destination, true), "UTF-8"));
            reader1 = new BufferedReader(new FileReader(gold));
            reader2 = new BufferedReader(new FileReader(parse));
            
            String line1, line2;
            while ((line1 = reader1.readLine()) != null) {
                line2 = reader2.readLine();
                if (line1.trim().length() != 0) {
                    String[] vals = line1.split("\t");
                    line2 = line2.replace("_\t_", vals[6] + "\t" + vals[7] + "\t_\t_");
                }
                pred.write(line2 + "\n");
            }
        } 
        catch(Exception exx) {}
        finally {
            if (pred != null) { pred.close(); }
            if (reader1 != null) { reader1.close(); }
            if (reader2 != null) { reader2.close(); }
        }
    }
    // </editor-fold>
}