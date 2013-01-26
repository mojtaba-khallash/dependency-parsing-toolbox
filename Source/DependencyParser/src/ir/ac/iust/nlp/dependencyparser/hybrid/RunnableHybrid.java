package ir.ac.iust.nlp.dependencyparser.hybrid;

import edu.stanford.nlp.parser.ensemble.utils.Eisner;
import edu.stanford.nlp.parser.ensemble.utils.Scorer;
import ir.ac.iust.nlp.dependencyparser.BasePanel;
import ir.ac.iust.nlp.dependencyparser.parsing.RunnableParse;
import ir.ac.iust.nlp.dependencyparser.training.RunnableTrain;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.Flowchart;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.HybridType;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ParserType;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ReparseType;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.MSTStackSettings;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.MaltSettings;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.MaltStackSettings;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.Settings;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.List;
import optimizer.ValidationGenerator;

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

    // <editor-fold defaultstate="collapsed" desc="Run Malt Level0">
    private void runMaltLevel0() {
        MaltStackSettings params = new MaltStackSettings((MaltStackSettings)settings);
        if (params.Level != 0) {
            return;
        }

        // Create temp folder for run stacking
        String tmpFolder = String.valueOf(
                Calendar.getInstance().getTimeInMillis()) + File.separator;
        String currentDir = "tmp" + File.separator + tmpFolder;
        (new File(currentDir)).mkdirs();
        params.WorkingDirectory = currentDir;
        
        try {
            //==============//
            // Train Level0 //
            //==============//
            out.println("Train Level0");
            out.println("---------------------------------------------");
            
            //==== Make Prediction of Train [train_pred.conll] ====//
            out.println("\nAugmenting training data with output predictions...\n");
            // Make N gold file from "train.conll" file
            params.Chart = Flowchart.Train;
            params.preProcess();
            MaltSettings maltSt;
            for (int i = 0; i < params.AugmentNParts; i++) {
                // Run train malt
                out.println("\nTraining classifier for partition " + i);
                maltSt = new MaltSettings((MaltSettings)params);
                maltSt.Model = currentDir + "modelname_level0_part" + i + ".mco";
                maltSt.Input = currentDir + "_train" + i + ".conll";
                RunnableTrain train = new RunnableTrain(null, ParserType.MaltParser, out, maltSt);
                train.run();

                // Run test malt
                out.println("Making predictions for partition " + i);
                maltSt = new MaltSettings((MaltSettings)params);
                maltSt.Model = currentDir + "modelname_level0_part" + i + ".mco";
                maltSt.Input = currentDir + "_test" + i + ".conll";
                maltSt.Output = currentDir + "_parse" + i + ".conll";
                RunnableParse parse = new RunnableParse(null, ParserType.MaltParser, out, maltSt);
                parse.run();
            }
            // Merge N predicted part in "train_pred.conll" file
            params.postProcess();
            
            //==== Train on the whole of "train.conll" ====//
            out.println("\nTraining the base classifier in the whole corpus...");
            maltSt = new MaltSettings((MaltSettings)params);
            maltSt.Chart = Flowchart.Train;
            maltSt.Input = params.Input;
            maltSt.Model = currentDir + "modelname_level0.mco";
            RunnableTrain runTrain = new RunnableTrain(null, ParserType.MaltParser, out, maltSt);
            runTrain.run();

            
            
            
            //==============//
            // Parse Level0 //
            //==============//
            out.println("\nParse Level0");
            out.println("---------------------------------------------");
            params.Chart = Flowchart.Parse;
            params.preProcess();
            maltSt = new MaltSettings((MaltSettings)params);
            maltSt.Model = currentDir + "modelname_level0.mco";
            maltSt.Input = params.Gold;
            maltSt.Output = currentDir + "parse.conll";
            RunnableParse parse = new RunnableParse(null, ParserType.MaltParser, out, maltSt);
            parse.run();
            params.postProcess();

            
            
            
            //=============//
            // Eval Level0 //
            //=============//
            out.println("\nEval Level0");
            out.println("---------------------------------------------");
            mstparser.DependencyParser.out = out;
            params.Chart = Flowchart.Eval;
            params.Output = maltSt.Output;
            mstparser.DependencyParser.main(params.getParameters());
        } catch (Exception ex) {
            out.println("Error: " + ex.getMessage());
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Run MST">
    private void runMST() {
        MSTStackSettings params = new MSTStackSettings((MSTStackSettings)settings);
        
        try {
            mstparser.DependencyParser.out = out;

            // Train Level
            out.println("Train Level" + params.Level);
            out.println("---------------------------------------------");
            params.Chart = Flowchart.Train;
            params.preProcess();
            mstparser.DependencyParser.main(params.getParameters());
            params.postProcess();
            
            // Parse
            out.println("\nParse Level" + params.Level);
            out.println("---------------------------------------------");
            params.Chart = Flowchart.Parse;
            params.preProcess();
            mstparser.DependencyParser.main(params.getParameters());
            params.postProcess();

            // Eval
            out.println("\nEval Level" + params.Level);
            out.println("---------------------------------------------");
            params.Chart = Flowchart.Eval;
            mstparser.DependencyParser.main(params.getParameters());
        } catch (Exception ex) {
            out.println("Error: " + ex.getMessage());
        }
    }
    // </editor-fold>
}