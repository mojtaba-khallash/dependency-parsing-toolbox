package edu.stanford.nlp.parser.ensemble;

import edu.stanford.nlp.parser.ensemble.utils.DeprojectivizeCorpus;
import edu.stanford.nlp.parser.ensemble.utils.Now;
import edu.stanford.nlp.parser.ensemble.utils.ReverseCorpus;
import edu.stanford.nlp.parser.ensemble.utils.Scorer;
import edu.stanford.nlp.parser.ensemble.utils.Scorer.Score;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;
import org.maltparser.core.helper.SystemLogger;

public class RunnableTestJob extends BaseModelRunnableJob implements Runnable {

    public RunnableTestJob(Ensemble ensemble, int index) {
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

            String str;
            if (ensemble.multiThreadEval == true) {
                while ((str = stdError.readLine()) != null) {
                    System.out.println(ensemble.baseModels[baseModelIndex] + ") " + str);
                }
            }
            else {
                while ((str = stdError.readLine()) != null) {
                    System.out.println(str);
                }
            }

            // reverse output if right-to-left
            if (!leftToRight) {
                File origFile;
                File reversedFile;
                if (pseudo_projective) {
                    origFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel + ".pp.reversed");
                    reversedFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel + ".pp");
                }
                else {
                    origFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel + ".reversed");
                    reversedFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel);
                }
                
                try {
                    ReverseCorpus.reverseCorpus(origFile.getAbsolutePath(), reversedFile.getAbsolutePath());
                    origFile.delete();
                } catch (IOException e) {
                    SystemLogger.logger().error("Failed to reverse file " + origFile.getAbsolutePath() + " to " + reversedFile + "!\n");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                
                if (pseudo_projective) {
                    origFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel + ".pp");
                    reversedFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel);
                    String modelName = "pp-reverse";
                    try {
                        DeprojectivizeCorpus.Deprojectivize(ensemble.workingDirectory, origFile.getName(), reversedFile.getName(), modelName);
                        origFile.delete();
                    } catch (Exception e) {
                        SystemLogger.logger().error("Failed to deprojectivize file " + origFile.getAbsolutePath() + " to " + reversedFile + "!\n");
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
            else if (pseudo_projective) {
                File origFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel + ".pp");
                File destFile = new File(ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel);
                String modelName = "pp";
                try {
                    DeprojectivizeCorpus.Deprojectivize(ensemble.workingDirectory, origFile.getName(), destFile.getName(), modelName);
                    origFile.delete();
                } catch (Exception e) {
                    SystemLogger.logger().error("Failed to deprojectivize file " + origFile.getAbsolutePath() + " to " + destFile + "!\n");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            // score this model
            String sysFile = ensemble.workingDirectory + File.separator + ensemble.outputPrefix + "." + ensemble.modelName + "-" + baseModel;
            Score s = null;
            try {
                s = Scorer.evaluate(ensemble.testCorpus, sysFile);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("WARNING: Cannot open files generated by model " + baseModel);
            }
            if (s != null) {
                SystemLogger.logger().info(String.format(baseModel + " LAS: %.2f %d/%d\n", s.las, s.lcorrect, s.total));
                SystemLogger.logger().info(String.format(baseModel + " UAS: %.2f %d/%d\n", s.uas, s.ucorrect, s.total));
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
        //  -parse	[Parse with a Single MaltParser configuration]
        pars.append("-m ");
        pars.append("parse");

        // name: Configuration name
        pars.append(" -c ");
        pars.append(ensemble.modelName).append("-").append(baseModel);

        // learner:  Learner
        //  - libsvm        [LIBSVM learner]
        //  - liblinear     [LIBLINEAR learner]
        pars.append(" -l ");
        pars.append("liblinear");

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

        // workingdir: Working directory
        pars.append(" -w ");
        pars.append(workingDirectory.getAbsolutePath());

        File from = new File(ensemble.modelDirectory + File.separator + ensemble.modelName + "-" + baseModel + ".mco");
        File to = new File(workingDirectory.getAbsolutePath() + File.separator + ensemble.modelName + "-" + baseModel + ".mco");
        try {
            FileUtils.copyFile(from, to);
        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println("WARNING: Cannot find trained model: " + baseModel);
        }

        // infile: Path to input file
        pars.append(" -i ");
        if (leftToRight) {
            pars.append(ensemble.testCorpus);
        } else {
            File origFile = new File(ensemble.testCorpus);
            
            File reversedFile = new File(ensemble.workingDirectory + File.separator + origFile.getName() + ".reversed");

            pars.append(reversedFile.getAbsolutePath());                
        }

        // outfile: Path to output file
        pars.append(" -o ");
        if (leftToRight) {
            if (pseudo_projective)
                pars.append(ensemble.workingDirectory).append(File.separator).append(ensemble.outputPrefix).append(".").append(ensemble.modelName).append("-").append(baseModel).append(".pp");
            else
                pars.append(ensemble.workingDirectory).append(File.separator).append(ensemble.outputPrefix).append(".").append(ensemble.modelName).append("-").append(baseModel);
        } else {
            if (pseudo_projective)
                pars.append(ensemble.workingDirectory).append(File.separator).append(ensemble.outputPrefix).append(".").append(ensemble.modelName).append("-").append(baseModel).append(".pp.reversed");
            else
                pars.append(ensemble.workingDirectory).append(File.separator).append(ensemble.outputPrefix).append(".").append(ensemble.modelName).append("-").append(baseModel).append(".reversed");
        }

        return pars.toString();
    }
}