//
// Ensemble - runs a linear-interpolation of shift-reduce parsers
// Copyright (c) 2009-2010 The Board of Trustees of 
// The Leland Stanford Junior University. All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Mihai Surdeanu
//    mihais AT stanford DOT edu
//
package edu.stanford.nlp.parser.ensemble;

import edu.stanford.cs.ra.arguments.Argument;
import edu.stanford.cs.ra.arguments.Arguments;
import edu.stanford.nlp.parser.ensemble.utils.Eisner;
import edu.stanford.nlp.parser.ensemble.utils.ProjectivizeCorpus;
import edu.stanford.nlp.parser.ensemble.utils.ReverseCorpus;
import edu.stanford.nlp.parser.ensemble.utils.Scorer;
import edu.stanford.nlp.parser.ensemble.utils.Scorer.Score;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import org.maltparser.core.helper.SystemLogger;

public class Ensemble {

    @Argument("Name of the ensemble model.")
    @Argument.Default("ensemble")
    @Argument.Switch("--modelName")
    String modelName;
    @Argument("Comma-separated list of base models to use in the ensemble.")
    @Argument.Default("nivreeager-ltr,nivrestandard-ltr,nivrestandard-rtl")
    @Argument.Switch("--baseModelNames")
    String baseModelNames;
    /**
     * Parsed list of models from baseModelNames
     */
    String[] baseModels;
    @Argument("Feature model specification (use comma to separate feature model of each algorithm).")
    @Argument.Default("<default>")
    @Argument.Switch("--featureModelNames")
    private String featureModelNames;
    /**
     * Parsed list of models from baseModelNames
     */
    String[] featureModels;
    @Argument("Location of the training corpus.")
    @Argument.Switch("--trainCorpus")
    String trainCorpus = null;
    @Argument("Location of the evaluation corpus.")
    @Argument.Switch("--testCorpus")
    String testCorpus = null;
    @Argument("Prefix output files generated during evaluation with this string (all output files are saved in workingDirectory).")
    @Argument.Switch("--outputPrefix")
    String outputPrefix = null;
    @Argument("True if during training the ensemble should create one thread per base model")
    @Argument.Default("false")
    @Argument.Switch("--multiThreadTrain")
    boolean multiThreadTrain;
    @Argument("True if during evaluation the ensemble should create one thread per base model")
    @Argument.Default("false")
    @Argument.Switch("--multiThreadEval")
    boolean multiThreadEval;
    @Argument("The model files will be saved in this directory.")
    @Argument.Default("/tmp")
    @Argument.Switch("--modelDirectory")
    String modelDirectory;
    @Argument("Temporary files created during execution will be stored (and deleted on completion) in this directory.")
    @Argument.Default("/tmp")
    @Argument.Switch("--workingDirectory")
    String workingDirectory;
    @Argument("Training options for liblinear.")
    @Argument.Default("-s_4_-e_0.1_-c_0.2_-B_1.0")
    @Argument.Switch("--libLinearOptions")
    String libLinearOptions;
    @Argument("Log level: off|fatal|error|warn|info|debug")
    @Argument.Default("info")
    @Argument.Switch("--logLevel")
    String logLevel;
    @Argument("Liblinear log level: silent|error|all")
    @Argument.Default("error")
    @Argument.Switch("--libLinearLogLevel")
    String libLinearLogLevel;
    @Argument("Use this external program to train liblinear (should be more robust)")
    @Argument.Default("")
    @Argument.Switch("--libLinearTrain")
    String libLinearTrain;
    @Argument("Split base models based on this column.")
    @Argument.Default("POSTAG")
    @Argument.Switch("--dataSplitColumn")
    String dataSplitColumn;
    //
    // Use this in combination with "-s Input[0]" for non covington models or with "-s Right[0]" for covington models
    //
    @Argument("Data split threshold for base models.")
    @Argument.Default("100")
    @Argument.Switch("--dataSplitThreshold")
    Integer dataSplitThreshold;
    @Argument("train|test")
    @Argument.Default("test")
    @Argument.Switch("--run")
    String run;
    @Argument("Reparsing algorithm: majority|attardi|eisner")
    @Argument.Default("eisner")
    @Argument.Switch("--reparseAlgorithm")
    String reparseAlgorithm;
    @Argument("Size of the thread pool, if multi-threaded processing is enabled.")
    @Argument.Default("4")
    @Argument.Switch("--threadCount")
    private Integer threadCount;
    /**
     * Automatically set to true if any base model requires right-to-left
     * processing
     */
    private boolean rightToLeft;
    /**
     * Automatically set to true if any base model requires pseudo projective
     * processing
     */
    private boolean rtl_pseudo_projective;
    private boolean ltr_pseudo_projective;

    public static void main(String[] args) throws Exception {
        Ensemble ensemble = new Ensemble(args);
        ensemble.run();
    }

    public Ensemble(String[] args) {
        Arguments.parse(args, this);
        SystemLogger.instance().setSystemVerbosityLevel(logLevel);

        //
        // sanity checks
        //
        File md = new File(modelDirectory);
        if (!md.exists()) {
            throw new RuntimeException("ERROR: Model directory " + md.getAbsolutePath() + " does not exist!");
        }
        if (!md.isDirectory()) {
            throw new RuntimeException("ERROR: Model directory " + md.getAbsolutePath() + " is not a directory!");
        }
        if (!md.canWrite()) {
            throw new RuntimeException("ERROR: Must have write permission to model directory " + md.getAbsolutePath() + "!");
        }

        File wd = new File(workingDirectory);
        if (!wd.exists()) {
            throw new RuntimeException("ERROR: Working directory " + wd.getAbsolutePath() + " does not exist!");
        }
        if (!wd.isDirectory()) {
            throw new RuntimeException("ERROR: Working directory " + wd.getAbsolutePath() + " is not a directory!");
        }
        if (!wd.canWrite()) {
            throw new RuntimeException("ERROR: Must have write permission to working directory " + wd.getAbsolutePath() + "!");
        }

        baseModels = baseModelNames.split(",");

        int len = baseModels.length;
        featureModels = new String[len];
        String[] models = featureModelNames.split(",");
        for (int i = 0; i < len; i++) {
            if (i < models.length) {
                featureModels[i] = models[i];
            } else {
                featureModels[i] = "<default>";
            }
        }

        rightToLeft = false;
        rtl_pseudo_projective = false;
        ltr_pseudo_projective = false;
        for (String bm : baseModels) {
            if (!BASE_MODELS.contains(bm)) {
                throw new RuntimeException("Unknown base model: " + bm);
            }
            if (bm.lastIndexOf("rtl") != -1) {
                rightToLeft = true;

                if (bm.endsWith("+PP")) {
                    rtl_pseudo_projective = true;
                }
            } else if (bm.endsWith("+PP")) {
                ltr_pseudo_projective = true;
            }
        }

        if (!run.equals(Const.RUN_TEST) && !run.equals(Const.RUN_TRAIN)) {
            throw new RuntimeException("Unknown run mode: " + run);
        }

        if (run.equalsIgnoreCase(Const.RUN_TRAIN)) {
            if (trainCorpus == null) {
                throw new RuntimeException("Training corpus must be specified if --run train!");
            }
            File f = new File(trainCorpus);
            if (!f.exists()) {
                throw new RuntimeException("ERROR: Training corpus " + f.getAbsolutePath() + " does not exist!");
            }
            if (!f.isFile()) {
                throw new RuntimeException("ERROR: Training corpus " + f.getAbsolutePath() + " is not a file!");
            }
            if (!f.canRead()) {
                throw new RuntimeException("ERROR: Must have read permission to training corpus " + f.getAbsolutePath() + "!");
            }
            SystemLogger.logger().info("Will run in TRAIN mode.\n");
        }
        if (run.equalsIgnoreCase(Const.RUN_TEST)) {
            if (testCorpus == null) {
                throw new RuntimeException("Test corpus must be specified if --run test!");
            }
            File f = new File(testCorpus);
            if (!f.exists()) {
                throw new RuntimeException("ERROR: Test corpus " + f.getAbsolutePath() + " does not exist!");
            }
            if (!f.isFile()) {
                throw new RuntimeException("ERROR: Test corpus " + f.getAbsolutePath() + " is not a file!");
            }
            if (!f.canRead()) {
                throw new RuntimeException("ERROR: Must have read permission to test corpus " + f.getAbsolutePath() + "!");
            }
            SystemLogger.logger().info("Will run in TEST mode.\n");
        }
    }
    private static final Set<String> BASE_MODELS = new HashSet<String>(Arrays.asList(
            "nivreeager-ltr",    "nivrestandard-ltr",   "covnonproj-ltr",
            "nivreeager-ltr+PP", "nivrestandard-ltr+PP", 
            "nivreeager-rtl",    "nivrestandard-rtl",   "covnonproj-rtl",
            "nivreeager-rtl+PP", "nivrestandard-rtl+PP"));

    public void run() throws IOException {
        List<Runnable> jobs = createJobs();

        boolean multiThreaded = false;
        if ((run.equalsIgnoreCase(Const.RUN_TRAIN) && multiThreadTrain)
                || (run.equalsIgnoreCase(Const.RUN_TEST) && multiThreadEval)) {
            multiThreaded = true;
        }
        
        String file_name;
        String phase_name;

        // reverse the training corpus
        if (run.equals(Const.RUN_TRAIN)) {
            file_name = trainCorpus;
            phase_name = "training";
        } // reverse the testing corpus
        else if (run.equals(Const.RUN_TEST)) {
            file_name = testCorpus;
            phase_name = "testing";
        } else {
            throw new RuntimeException("Unknown run mode: " + run);
        }

        if (rightToLeft) {
            File f = new File(file_name);
            File f1 = new File(workingDirectory + File.separator + f.getName());
            f1.deleteOnExit();
            FileUtils.copyFile(f, f1);
            
            if (rtl_pseudo_projective && run.equals(Const.RUN_TRAIN)) {
                String ppReversedFileName = workingDirectory + File.separator + f.getName() + ".pp";
                try {
                    SystemLogger.logger().debug("Projectivise reversing " + phase_name + " corpus to " + ppReversedFileName + "\n");
                    String input = f.getName();
                    f = new File(ppReversedFileName);
                    String output = f.getName();
                    ProjectivizeCorpus.Projectivize(workingDirectory, input, output, "pp-reverse");
                    f.deleteOnExit();
                    f = new File("pp-reverse.mco");
                    f.deleteOnExit();
                    
                    f = new File(ppReversedFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error: cannot projectivize corpus");
                }
            }
            
            String reversedFileName = workingDirectory + File.separator + f.getName() + ".reversed";
            SystemLogger.logger().debug("Reversing " + phase_name + " corpus to " + reversedFileName + "\n");
            ReverseCorpus.reverseCorpus(f.getAbsolutePath(), reversedFileName);
            f = new File(reversedFileName);
            f.deleteOnExit();
        }

        if (ltr_pseudo_projective && run.equals(Const.RUN_TRAIN)) {
            File f = new File(file_name);
            File f1 = new File(workingDirectory + File.separator + f.getName());
            f1.deleteOnExit();
            FileUtils.copyFile(f, f1);
            String ppFileName = workingDirectory + File.separator + f.getName() + ".pp";
            try {
                SystemLogger.logger().debug("Projectivise " + phase_name + " corpus to " + ppFileName + "\n");
                String input = f.getName();
                f = new File(ppFileName);
                String output = f.getName();
                ProjectivizeCorpus.Projectivize(workingDirectory, input, output, "pp");
                f.deleteOnExit();
                f = new File("pp.mco");
                f.deleteOnExit();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error: cannot projectivize corpus");
            }
        }

        if (multiThreaded) {
            ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
            for (Runnable job : jobs) {
                threadPool.execute(job);
            }
            threadPool.shutdown();
            this.waitForThreads(jobs.size());
        } else {
            for (Runnable job : jobs) {
                job.run();
            }
        }

        // run the actual ensemble model
        if (run.equalsIgnoreCase(Const.RUN_TEST)) {
            String outFile = workingDirectory + File.separator + outputPrefix + "." + modelName + "-ensemble";
            List<String> sysFiles = new ArrayList<String>();
            for (String baseModel : baseModels) {
                sysFiles.add((workingDirectory + File.separator + outputPrefix + "." + modelName + "-" + baseModel));
            }
            // generate the ensemble
            Eisner.ensemble(testCorpus, sysFiles, outFile, reparseAlgorithm);
            // score the ensemble
            Score s = Scorer.evaluate(testCorpus, outFile);
            if (s != null) {
                SystemLogger.logger().info(String.format("ensemble LAS: %.2f %d/%d\n", s.las, s.lcorrect, s.total));
                SystemLogger.logger().info(String.format("ensemble UAS: %.2f %d/%d\n", s.uas, s.ucorrect, s.total));
            }

            SystemLogger.logger().info("Ensemble output saved as: " + outFile + "\n");
        }

        SystemLogger.logger().info("DONE.\n");
    }

    private synchronized void waitForThreads(int count) {
        while (count > 0) {
            try {
                this.wait();
                count--;
                SystemLogger.logger().info("One thread finished. " + count + " still going.\n");
            } catch (InterruptedException e) {
                SystemLogger.logger().info("Main thread interrupted!\n");
                break;
            }
        }
        SystemLogger.logger().info("All threads finished.\n");
    }

    public synchronized void threadFinished() {
        this.notify();
    }

    private List<Runnable> createJobs() {
        List<Runnable> jobs = new ArrayList<Runnable>();
        if (run.equalsIgnoreCase(Const.RUN_TRAIN)) {
            for (int i = 0; i < baseModels.length; i++) {
                jobs.add(new RunnableTrainJob(this, i));
            }
        } else if (run.equalsIgnoreCase(Const.RUN_TEST)) {
            for (int i = 0; i < baseModels.length; i++) {
                jobs.add(new RunnableTestJob(this, i));
            }
        }
        return jobs;
    }
}