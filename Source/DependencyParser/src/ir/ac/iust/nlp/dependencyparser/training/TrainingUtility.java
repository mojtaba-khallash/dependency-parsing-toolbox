package ir.ac.iust.nlp.dependencyparser.training;

import java.io.File;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

/**
 * This example creates two Single Malt configurations files (model0.mco and model1.mco) 
 * by training the models using the small training data file * ../data/talbanken05_train.conll 
 * 
 * @author Johan Hall
 */
public class TrainingUtility {

    public static void TrainCorpus(String workingDir, String modelName, 
            String trainFile) throws MaltChainedException {
        try {
            // Trains the parser model model0.mco and uses the option container 0
            // learn from "trainingDataFile" and write trained model to "model0" file.
            new MaltParserService(0).runExperiment(
                       "-c "  + modelName
                    + " -m learn"
                    + " -i " + workingDir + File.separator + trainFile
                    + " -w " + workingDir);
        } catch (MaltChainedException e) {
            System.err.println("MaltParser exception : " + e.getMessage());
        }
    }
}