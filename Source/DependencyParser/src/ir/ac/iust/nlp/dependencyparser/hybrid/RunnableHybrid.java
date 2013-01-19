package ir.ac.iust.nlp.dependencyparser.hybrid;

import edu.stanford.nlp.parser.ensemble.utils.Eisner;
import edu.stanford.nlp.parser.ensemble.utils.Scorer;
import ir.ac.iust.nlp.dependencyparser.BasePanel;
import ir.ac.iust.nlp.dependencyparser.parsing.RunnableParse;
import ir.ac.iust.nlp.dependencyparser.training.RunnableTrain;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.HybridType;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ParserType;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ReparseType;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.MSTStackSettings;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.MaltSettings;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.MaltStackSettings;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.Settings;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import optimizer.ValidationGenerator;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Mojtaba Khallash
 */
public class RunnableHybrid implements Runnable {

    private PrintStream out = System.out;
    private HybridType type;
    BasePanel target;
  
    // Stacking setting
    Settings settings;
    private ParserType parser;
    
    // Ensemble settings
    String goldFile;
    List<String> sysFiles;
    String outFile;
    ReparseType reparseAlgorithm;

    public RunnableHybrid(BasePanel target, PrintStream out, 
            ParserType parser, Settings settings) {
        this.target = target;
        if (out != null) {
            this.out = out;
        }
        this.type = HybridType.Stacking;
        this.parser = parser;
        this.settings = settings;
    }
    
    public RunnableHybrid(BasePanel target, PrintStream out, 
            String goldFile, List<String> sysFiles, String outFile, 
            ReparseType reparseAlgorithm) {
        this.target = target;
        if (out != null) {
            this.out = out;
        }
        this.type = HybridType.Ensemble;
        this.goldFile = goldFile;
        this.sysFiles = sysFiles;
        this.outFile = outFile;
        this.reparseAlgorithm = reparseAlgorithm;
    }
    
    @Override
    public void run() {
        try {
            switch (type) {
                case Ensemble:
                    runEnsemble();
                    break;
                case Stacking:
                    switch (parser) {
                        case MSTParser:
                            runMST();
                            break;
                        case MaltParser:
                            runMaltLevel0();
                            break;
                    }
                    break;
            }
        }
        finally {
            if (target != null) {
                target.threadFinished();
            }
        }
    }
    
    private void runEnsemble() {
        String evalName = "eval07.pl";
        boolean exist = true;
        try { 
            exist = new File(evalName).exists();
            if (reparseAlgorithm == ReparseType.chu_liu_edmond) {
                if (!exist) {
                    java.io.BufferedWriter bwValidateFormat;
                    try {
                        bwValidateFormat = new java.io.BufferedWriter(new java.io.FileWriter(evalName));
                        bwValidateFormat.write(ValidationGenerator.generateEval07());
                        bwValidateFormat.close();
                    } catch (java.io.IOException e) {
                    }
                }
            }
            else {
                File outPath = new File(outFile);
                if (!outPath.exists() && outPath.getParentFile() != null) {
                    outPath.getParentFile().mkdirs();
                }
            }
            
            Eisner.ensemble(goldFile, sysFiles, outFile, reparseAlgorithm.toString());
            
            Scorer.Score s = Scorer.evaluate(outFile, goldFile);
            
            out.println(String.format("ensemble LAS: %.2f %d/%d", s.las, s.lcorrect, s.total));
            out.println(String.format("ensemble UAS: %.2f %d/%d", s.uas, s.ucorrect, s.total));
        } catch (IOException ex) {}
        finally {
            if (exist == false) {
                new File(evalName).delete();
            }
        }
    }

    private void runMaltLevel0() {
        MaltStackSettings params = (MaltStackSettings)settings;
        if (params.Level != 0) {
            return;
        }
        
        String trainFile = params.Input;
        List<File> files = new LinkedList<>();
        Writer trainPred = null;
        Writer testPred = null;
        try {
            HashMap<Integer, String> Sentences;
            int count;
            try (BufferedReader reader = new BufferedReader(new FileReader(trainFile))) {
                String line;
                Sentences = new HashMap<>();
                StringBuilder tokens = new StringBuilder();
                count = 0;
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

            int numInstances = count;

            int i, j;
            int numInstancesPerPart = numInstances / params.AugmentNParts; // The last partition becomes bigger

            Writer[] trainParts = new Writer[numInstances];
            Writer[] testParts = new Writer[numInstances];
            for (i = 0; i < params.AugmentNParts; i++) {
                String train = params.WorkingDirectory + "_train" + i + ".conll";
                files.add(new File(train));
                trainParts[i] = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(train, true), "UTF-8"));

                String test = params.WorkingDirectory + "_test" + i + ".conll";
                files.add(new File(test));
                testParts[i] = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(test, true), "UTF-8"));
            }

            out.println("Train Level0");
            out.println("---------------------------------------------");
            for (i = 0; i < numInstances; i++) {
                String sen = Sentences.get(i + 1);
                for (j = 0; j < params.AugmentNParts; j++) {
                    if ( (i >= j * numInstancesPerPart
                            && i < (j + 1) * numInstancesPerPart) ||
                         (j == params.AugmentNParts - 1 && i >= params.AugmentNParts * numInstancesPerPart)) {
                        testParts[j].write(sen + "\n");
                    } else {
                        trainParts[j].write(sen + "\n");
                    }
                }
            }

            File optionFile = new File(params.OptionsFile).getAbsoluteFile();
            String options = optionFile.getName();
            File optionFileTo = new File(params.WorkingDirectory + options);
            if (!optionFile.equals(optionFileTo)) {
                FileUtils.copyFile(optionFile, optionFileTo);
                files.add(optionFileTo);
            }
            params.OptionsFile = options;

            File guideFile = new File(params.GuidesFile).getAbsoluteFile();
            String guide = guideFile.getName();
            File guideFileTo = new File(params.WorkingDirectory + guide);
            if (!guideFile.equals(guideFileTo)) {
                FileUtils.copyFile(guideFile, guideFileTo);
                files.add(guideFileTo);
            }
            params.GuidesFile = guide;

            out.println("\nAugmenting training data with output predictions...\n");

            File trainTarget = new File("tmp" + File.separator + "train_pred.conll");
            try {
                if (trainTarget != null) {
                    FileUtils.forceDelete(trainTarget);
                }
            } catch (Exception e) {
            } finally {
                trainPred = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(trainTarget, true), "UTF-8"));
            }

            MaltSettings maltSt;
            
            for (i = 0; i < params.AugmentNParts; i++) {
                trainParts[i].close();
                testParts[i].close();

                // run train malt
                out.println("\nTraining classifier for partition " + i);
                maltSt = new MaltSettings((MaltSettings)params);
                maltSt.Model = "modelname_level0_part" + i + ".model";
                maltSt.Input = "_train" + i + ".conll";
                RunnableTrain train = new RunnableTrain(null, ParserType.MaltParser, out, maltSt);
                train.run();

                // run test malt
                out.println("Making predictions for partition " + i);
                maltSt = new MaltSettings((MaltSettings)params);
                maltSt.Model = "modelname_level0_part" + i + ".model";
                maltSt.Input = "_test" + i + ".conll";
                maltSt.Output = "_parse" + i + ".conll";
                RunnableParse parse = new RunnableParse(null, ParserType.MaltParser, out, maltSt);
                parse.run();
                files.add(new File(params.WorkingDirectory + "_parse" + i + ".conll"));
                files.add(new File(params.WorkingDirectory + "modelname_level0_part" + i + ".model"));

                // merge to train_pred
                BufferedReader reader1 = new BufferedReader(new FileReader(params.WorkingDirectory + "_test" + i + ".conll"));
                BufferedReader reader2 = new BufferedReader(new FileReader(params.WorkingDirectory + "_parse" + i + ".conll"));
                try {
                    String line1, line2;
                    while ((line1 = reader1.readLine()) != null) {
                        line2 = reader2.readLine();
                        if (line1.trim().length() != 0) {
                            String[] vals = line1.split("\t");
                            line2 = line2.replace("_\t_", vals[6] + "\t" + vals[7] + "\t_\t_");
                        }
                        trainPred.write(line2 + "\n");
                    }
                } 
                catch(Exception exx) {}
                finally {
                    reader1.close();
                    reader2.close();
                }
            }
            
            // run train malt
            File trainFileFrom = new File(params.Input).getAbsoluteFile();
            String train = trainFileFrom.getName();
            File trainFileTo = new File(params.WorkingDirectory + train);
            if (!trainFileFrom.equals(trainFileTo)) {
                FileUtils.copyFile(trainFileFrom, trainFileTo);
                files.add(trainFileTo);
            }
            params.Input = train;
            
            out.println("\nTraining the base classifier in the whole corpus...");
            maltSt = new MaltSettings((MaltSettings)params);
            maltSt.Model = "modelname_level0.model.model";
            RunnableTrain runTrain = new RunnableTrain(null, ParserType.MaltParser, out, maltSt);
            runTrain.run();

            // run test malt
            File testFileFrom = new File(params.Gold).getAbsoluteFile();
            String test = testFileFrom.getName();
            File testFileTo = new File(params.WorkingDirectory + test);
            if (!testFileFrom.equals(testFileTo)) {
                FileUtils.copyFile(testFileFrom, testFileTo);
                files.add(testFileTo);
            }
            params.Gold = test;
            
            out.println("\nParse Level0");
            out.println("---------------------------------------------");
            maltSt = new MaltSettings((MaltSettings)params);
            maltSt.Model = "modelname_level0.model";
            maltSt.Input = params.Gold;
            maltSt.Output = "parse.conll";
            RunnableParse parse = new RunnableParse(null, ParserType.MaltParser, out, maltSt);
            parse.run();
            files.add(new File(params.WorkingDirectory + "parse.conll"));
            files.add(new File(params.WorkingDirectory + "modelname_level0.model"));

            // merge to test_pred
            File testTarget = new File("tmp" + File.separator + "test_pred.conll");
            try {
                if (testTarget != null) {
                    FileUtils.forceDelete(testTarget);
                }
            } catch (Exception e) {
            } finally {
                testPred = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(testTarget, true), "UTF-8"));
            }
            
            BufferedReader reader1 = new BufferedReader(new FileReader(params.WorkingDirectory + test));
            BufferedReader reader2 = new BufferedReader(new FileReader(params.WorkingDirectory + "parse.conll"));
            try {
                String line1, line2;
                while ((line1 = reader1.readLine()) != null) {
                    line2 = reader2.readLine();
                    if (line1.trim().length() != 0) {
                        String[] vals = line1.split("\t");
                        line2 = line2.replace("_\t_", vals[6] + "\t" + vals[7] + "\t_\t_");
                    }
                    testPred.write(line2 + "\n");
                }
            } 
            catch(Exception exx) {}
            finally {
                reader1.close();
                reader2.close();
            }
            
            out.println("\nEval Level0");
            out.println("---------------------------------------------");
            mstparser.DependencyParser.out = out;
            mstparser.DependencyParser.main(new String[]{
                        "eval",
                        "gold-file:" + params.Gold,
                        "output-file:" + params.WorkingDirectory + "parse.conll",
                        "format:CONLL"
                    });
        } catch (Exception ex) {
        } finally {
            if (trainPred != null) {
                try {
                    trainPred.close();
                } catch (IOException ex) {
                }
            }
            if (testPred != null) {
                try {
                    testPred.close();
                } catch (IOException ex) {
                }
            }
            File trainPredFrom = new File(params.WorkingDirectory + "train_pred.conll").getAbsoluteFile();
            File trainPredTo = new File(params.AugmentedTrainFile);
            if (!trainPredFrom.equals(trainPredTo)) {
                try {
                    FileUtils.copyFile(trainPredFrom, trainPredTo);
                } catch (IOException ex) {
                }
                files.add(trainPredFrom);
            }
            
            
            File testPredFrom = new File(params.WorkingDirectory + "test_pred.conll").getAbsoluteFile();
            File testPredTo = new File(params.Output);
            if (!testPredFrom.equals(testPredTo)) {
                try {
                    FileUtils.copyFile(testPredFrom, testPredTo);
                } catch (IOException ex) {
                }
                files.add(testPredFrom);
            }
            File f;
            for (int i = 0; i < files.size(); i++) {
                f = files.get(i);
                try {
                    if (f != null) {
                        FileUtils.forceDelete(f);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void runMST() {
        MSTStackSettings params = (MSTStackSettings)settings;
        
        try {
            mstparser.DependencyParser.out = out;

            // Train Level
            out.println("Train Level" + params.Level);
            out.println("---------------------------------------------");
            mstparser.DependencyParser.main(params.getTrainParameters());
            
            // Parse
            out.println("\nParse Level" + params.Level);
            out.println("---------------------------------------------");
            MSTStackSettings ps = new MSTStackSettings(params);
            ps.Input = params.Gold;
            mstparser.DependencyParser.main(ps.getTestParameters());

            // Eval
            out.println("\nEval Level" + params.Level);
            out.println("---------------------------------------------");
            mstparser.DependencyParser.main(new String[]{
                        "eval",
                        "gold-file:" + params.Gold,
                        "output-file:" + params.Output,
                        "format:CONLL"
                    });
        } catch (Exception ex) {
        } finally {
            File model = new File(params.Model);
            if (model != null) {
                try {
                    FileUtils.forceDelete(model);
                } catch (Exception ex) {
                }
            }
        }
    }
}