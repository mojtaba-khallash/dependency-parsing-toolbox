package edu.stanford.nlp.parser.ensemble;

import edu.stanford.nlp.parser.ensemble.utils.Now;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;
import org.maltparser.core.helper.SystemLogger;

public class RunnableTrainJob extends BaseModelRunnableJob implements Runnable {

    public RunnableTrainJob(Ensemble ensemble, int index) {
        super(ensemble, index);
    }

    @Override
    public void run() {
        
        try {
            SystemLogger.logger().info("Starting job " + ensemble.baseModels[baseModelIndex] + " at " + new Now() + "...\n");
            createWorkingDirectory();

            // args for malt
            String params = makeMaltEngineParameters();

            // run malt
            Process p = Runtime.getRuntime().exec("java -Xmx2048m -jar lib" + File.separator + "maltParser.jar " + params);

            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));

            String s;
            if (ensemble.multiThreadTrain == true) {
                while ((s = stdError.readLine()) != null) {
                    System.out.println(ensemble.baseModels[baseModelIndex] + ") " + s);
                }
            }
            else {
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }
            }

            // move model file from working directory to model directory
            File origModel = new File(workingDirectory + File.separator + ensemble.modelName + "-" + baseModel + ".mco");
            File savedModel = new File(ensemble.modelDirectory + File.separator + ensemble.modelName + "-" + baseModel + ".mco");
            try {
                FileUtils.copyFile(origModel, savedModel);
                SystemLogger.logger().info("Model file for job " + baseModel + " saved as: " + savedModel.getAbsolutePath() + "\n");
                origModel.delete();
            } catch(Exception ex) {
                SystemLogger.logger().error("ERROR: failed to save model file for job " + baseModel + ". The actual model file might be here: " + origModel.getAbsolutePath() + "\n");
            }

            ensemble.threadFinished();
            SystemLogger.logger().info("Ended job " + baseModel + " at " + new Now() + ".\n");
            SystemLogger.logger().info("-----------------------------------------------------------------------------\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeMaltEngineParameters() {
        StringBuilder pars = new StringBuilder();

        // flowchart: Flow chart
        //  -learn	[Learn a Single MaltParser configuration]
        pars.append("-m ");
        pars.append("learn");

        // name: Configuration name
        pars.append(" -c ");
        pars.append(ensemble.modelName).append("-").append(baseModel);

        // learner:  Learner
        //  - libsvm        [LIBSVM learner]
        //  - liblinear     [LIBLINEAR learner]
        pars.append(" -l ");
        pars.append("liblinear");

        // options: LIBLINEAR options (see liblinear Documentation)
        pars.append(" -lo ");
        pars.append(ensemble.libLinearOptions);

        // verbosity: Verbosity of the liblinear or the libsvm package
        //  - silent	[No output from the liblinear or the libsvm package is logged.]
        //  - error	[Only the error stream of the liblinear or the libsvm package is logged.]
        //  - all	[All output of the liblinear or the libsvm package is logged.]
        pars.append(" -lv ");
        pars.append(ensemble.libLinearLogLevel);

        // verbosity: Verbosity level
        //  - off	[Logging turned off]
        //  - fatal	[Logging of very severe error events]
        //  - error	[Logging of error events]
        //  - warn	[Logging of harmful situations]
        //  - info	[Logging of informational messages]
        //  - debug	[Logging of debugging messages]
        pars.append(" -v ");
        pars.append(ensemble.logLevel);

        // data_split_column:  Data split input column
        pars.append(" -d ");
        pars.append(ensemble.dataSplitColumn);

        // data_split_threshold: Data split threshold [Default = 50]
        pars.append(" -T ");
        pars.append(Integer.toString(ensemble.dataSplitThreshold));

        // data_split_structure: Data split data structure
        pars.append(" -s ");
        if (baseModel.startsWith("nivre")) {
            pars.append("Input[0]");
        } else if (baseModel.startsWith("cov")) {
            pars.append("Right[0]");
        } else {
            throw new RuntimeException("Unknown base model: " + baseModel);
        }

        // parsing_algorithm:  Parsing algorithm 
        //  - nivreeager	[Nivre arc-eager]
        //  - nivrestandard	[Nivre arc-standard]
        //  - covnonproj	[Covington non-projective]
        //  - covproj           [Covington projective]
        //  - stackproj         [Stack projective]
        //  - stackeager	[Stack eager]
        //  - stacklazy         [Stack lazy]
        //  - planar            [Planar eager]
        //  - 2planar           [2-Planar eager]
        pars.append(" -a ");
        // remove (*-ltr) or (*-rtl) from end of algorithm name
        int dashPos = baseModel.lastIndexOf("-");
        assert (dashPos > 0 && dashPos < baseModel.length());
        pars.append(baseModel.substring(0, dashPos));

        // workingdir: Working directory
        pars.append(" -w ");
        pars.append(workingDirectory.getAbsolutePath());

        // infile: Path to input file
        pars.append(" -i ");
        if (leftToRight) {
            if (pseudo_projective) {
                // pseudo-projective trainCorpus
                File origFile = new File(ensemble.trainCorpus);
                File ppFile = new File(ensemble.workingDirectory + File.separator + origFile.getName() + ".pp");
            
                pars.append(ppFile.getAbsolutePath());
            }
            else
                pars.append(ensemble.trainCorpus);
        } else {
            // Reverse trainCorpus
            File origFile = new File(ensemble.trainCorpus);
            
            if (pseudo_projective) {
                File ppReversedFile = new File(ensemble.workingDirectory + File.separator + origFile.getName() + ".pp.reversed");

                pars.append(ppReversedFile.getAbsolutePath());
            }
            else {
                File reversedFile = new File(ensemble.workingDirectory + File.separator + origFile.getName() + ".reversed");

                pars.append(reversedFile.getAbsolutePath());                
            }
        }

        // external: Path to train or svm-train
        if (ensemble.libLinearTrain != null && ensemble.libLinearTrain.length() > 0) {
            pars.append(" -lx ");
            pars.append(ensemble.libLinearTrain);
        }

        // features: Feature model specification
        if (Const.TRAIN_EXTENDED) {
            pars.append(" -F ");
            if (featureModel.equals("<default>")) {
                if (baseModel.startsWith("nivreeager-")) {
                    pars.append("nivreeager");
                } else if (baseModel.startsWith("nivrestandard-")) {
                    pars.append("nivrestandard");
                } else if (baseModel.startsWith("covnonproj-")) {
                    pars.append("covnonproj");
                } else {
                    throw new RuntimeException("Unknown base model: " + baseModel);
                }
            }
            else {
                pars.append(ensemble.workingDirectory).append(File.separator).append(featureModel);
            }
        }

        return pars.toString();
    }
}