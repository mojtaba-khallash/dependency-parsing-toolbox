package ir.ac.iust.nlp.dependencyparser;

/*
* Copyright (C) 2013 Iran University of Science and Technology
*
* This file is part of "Dependency Parsing Toolbox" Project, as available 
* from http://nlp.iust.ac.ir This file is free software;
* you can redistribute it and/or modify it under the terms of the GNU General 
* Public License (GPL) as published by the Free Software Foundation, in 
* version 2 as it comes in the "COPYING" file of the VirtualBox OSE 
* distribution. VirtualBox OSE is distributed in the hope that it will be 
* useful, but WITHOUT ANY WARRANTY of any kind.
*
* You may elect to license modified versions of this file under the terms 
* and conditions of either the GPL.
*/

import ir.ac.iust.nlp.dependencyparser.evaluation.EvalSettings;
import ir.ac.iust.nlp.dependencyparser.hybrid.RunnableHybrid;
import ir.ac.iust.nlp.dependencyparser.inputoutput.ReadCorpus;
import ir.ac.iust.nlp.dependencyparser.optomization.RunnableOptimizer;
import ir.ac.iust.nlp.dependencyparser.parsing.RunnableParse;
import ir.ac.iust.nlp.dependencyparser.projection.RunnableProjectivize;
import ir.ac.iust.nlp.dependencyparser.training.RunnableTrain;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.Flowchart;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ParserType;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.ReparseType;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.TransformType;
import ir.ac.iust.nlp.dependencyparser.utility.parsing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.vxu.msi.malteval.MaltEvalConsole;

/**
 *
 * @author Mojtaba Khallash
 */
public class DependencyParser {

    public static String maxRam = "";
    public static String minRam = "";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean visisble = true;
        boolean exception = false;
        Flowchart mode = Flowchart.None;
        Flowchart helpChart = Flowchart.None;
        String workingDir = System.getProperty("user.dir");
        File tmp = new File(workingDir + File.separator + "tmp");
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        String input = "";
        String output = "";
        String gold = "";
        String model = "";
        boolean crossVal = false;
        // majority|attardi|eisner|chu_liu_edmond
        String method = "majority";
        // none|baseline|head|path|head+path
        String markingStrategy = "head";
        // none|ignore|left|right|head
        String coveredRoot = "head";
        // shortest|deepest
        String LiftingOrder = "shortest";
        // 1|2|3|all
        String phase = "all";
        // settings
        
        // - eval.metric
        // LAS|LA|UAS|AnyRight|AnyWrong|BothRight|BothWrong|HeadRight|HeadWrong
        // LabelRight|LabelWrong|DirectionRight|GroupedHeadToChildDistanceRight
        // HeadToChildDistanceRight
        // => NOTE: for selecting multiple metrics, separate them by comma.
        // - eval.groupByVal
        // Token|Wordform|Lemma|Cpostag|Postag|Feats|Deprel|Sentence|RelationLength
        // GroupedRelationLength|SentenceLength|StartWordPosition|EndWordPosition
        // ArcDirection|ArcDepth|BranchingFactor|ArcProjectivity|Frame
        EvalSettings eval = new EvalSettings();
        MaltSettings malt = new MaltSettings();
        ClearSettings clear = new ClearSettings();
        MSTSettings mst = new MSTSettings();
        MateSettings mate = new MateSettings();
        ParserType parser = ParserType.MaltParser;
        String level = "*";
        ParserType parserL0 = ParserType.MSTParser;
        ParserType parserL1 = ParserType.MSTParser;
        MaltStackSettings maltL0 = new MaltStackSettings();
        maltL0.Level = 0;
        MSTStackSettings mstL0 = new MSTStackSettings();
        MSTStackSettings mstL1 = new MSTStackSettings();
        mstL0.Level = 0;
        mstL1.Level = 1;

        showIntroduction();
                
        try {
            for (int i = 0; i< args.length; i++) {
                switch (args[i]) {
                    case "-Xmx":
                        i++;
                        maxRam = args[i];
                        break;
                    case "-Xms":
                        i++;
                        minRam = args[i];
                        break;
                    case "-v":
                        i++;
                        String val = args[i];
                        if (!(val.equals("0") || val.equals("1"))) {
                            throw new Exception("Only 0 or 1 is valid for -v parameter.");
                        }
                        visisble = val.equals("1");
                        break;
                    case "-mode":
                        i++;
                        mode = getChart(args[i].toLowerCase());
                        break;
                    case "-help":
                        i++;
                        try {
                            helpChart = getChart(args[i].toLowerCase());
                        } catch (Exception e) {
                            i--;
                        }
                        break;

                    // -- proj, deproj, optimizer, train, parse, eval, ensemble, stack param --//
                    case "-i":
                        i++;
                        input = args[i];
                        break;
                        
                    // -- proj, deproj, parse, eval, ensemble param --//
                    case "-o":
                        i++;
                        output = args[i];
                        break;
                        
                    // -- eval, ensemble param --//
                    case "-g":
                        i++;
                        gold = args[i];
                        break;
                        
                    // -- eval param --//
                    case "-metric":
                        i++;
                        eval.metrics = args[i];
                        break;
                    case "-group":
                        i++;
                        eval.groupByVal = args[i];
                        break;
                        
                    // -- ensemble param --//
                    case "-method":
                        i++;
                        method = args[i].toLowerCase();
                        break;
                        
                    // -- optimizer param --//
                    case "-cross_val":
                        i++;
                        crossVal = args[i].equals("1");
                        break;
                        
                    // -- proj, deproj, train, parse param --//
                    case "-m":
                        i++;
                        model = args[i];
                        break;
                        
                    // -- proj params -- //
                    case "-mark":
                        i++;
                        markingStrategy = args[i].toLowerCase();
                        break;
                    case "-covered":
                        i++;
                        coveredRoot = args[i].toLowerCase();
                        break;
                    case "-lift_order":
                        i++;
                        LiftingOrder = args[i].toLowerCase();
                        break;
                        
                    // -- optimizer and train and parse param --//
                    case "-parser":
                        i++;
                        parser = getParserType(args[i]);
                        break;
                    case "-phase":
                        i++;
                        phase = args[i];
                        break;
                        
                    // -- malt, clear parameters --//
                    case "-option":
                        i++;
                        clear.OptionsFile = malt.OptionsFile = args[i];
                        break;
                    case "-guide":
                        i++;
                        clear.GuidesFile = malt.GuidesFile = args[i];
                        break;

                    // -- clear parameters --//
                    case "-bootstrap":
                        i++;
                        clear.BootstrappingLevel = Integer.parseInt(args[i]);
                        break;

                    // -- mst, mate parameters --//
                    case "-iter":
                        i++;
                        mate.Iteration = mst.Iteration = Integer.parseInt(args[i]);
                        break;
                    case "-decode":
                        i++;
                        mate.DecodeType = mst.DecodeType = args[i].toLowerCase();
                        break;
                        
                    // -- mst parameters --//
                    case "-k":
                        i++;
                        mst.TrainingK = Integer.parseInt(args[i]);
                        break;
                    case "-order":
                        i++;
                        mst.Order = Integer.parseInt(args[i]);
                        break;
                    case "-loss":
                        i++;
                        mst.LossType = args[i];
                        break;
                        
                    // -- mate parameters --//
                    case "-threshold":
                        i++;
                        mate.NonProjectivityThreshold = Integer.parseInt(args[i]);
                        break;
                    case "-creation":
                        i++;
                        mate.FeatureCreation = args[i];
                        break;
                    case "-core":
                        i++;
                        mate.Cores = Integer.parseInt(args[i]);
                        break;
                        
                    // -- stack param --//
                    case "-t":
                        i++;
                        maltL0.Gold = mstL0.Gold = args[i];
                        break;
                    case "-l":
                        i++;
                        level = args[i];
                        break;
                    case "-l0_part":
                        i++;
                        maltL0.AugmentNParts = mstL0.AugmentNParts = Integer.parseInt(args[i]);
                        break;
                    case "-l0_out_train":
                        i++;
                        mstL1.Input = maltL0.AugmentedTrainFile = mstL0.AugmentedTrainFile = args[i];
                        break;
                    case "-l0_parser":
                        i++;
                        parserL0 = getParserType(args[i]);
                        break;
                    // -- malt stack parameters --//
                    case "-l0_option":
                        i++;
                        maltL0.OptionsFile = args[i];
                        break;
                    case "-l0_guide":
                        i++;
                        maltL0.GuidesFile = args[i];
                        break;
                    // -- mst stack parameters --//
                    case "-l0_iter":
                        i++;
                        mstL0.Iteration = Integer.parseInt(args[i]);
                        break;
                    case "-l0_decode":
                        i++;
                        mstL0.DecodeType = args[i].toLowerCase();
                        break;
                    case "-l0_k":
                        i++;
                        mstL0.TrainingK = Integer.parseInt(args[i]);
                        break;
                    case "-l0_order":
                        i++;
                        mstL0.Order = Integer.parseInt(args[i]);
                        break;
                    case "-l0_loss":
                        i++;
                        mstL0.LossType = args[i];
                        break;
                    case "-l0_out_parse":
                        i++;
                        mstL1.Gold = maltL0.Output = mstL0.Output = args[i];
                        break;
                    case "-l1_parser":
                        i++;
                        parserL1 = getParserType(args[i]);
                        break;
                    case "-l1_pe":
                        i++;
                        mstL1.UsePredEdge = args[i].equals("1");
                        break;
                    case "-l1_ps":
                        i++;
                        mstL1.UsePrevSibling = args[i].equals("1");
                        break;
                    case "-l1_ns":
                        i++;
                        mstL1.UseNextSibling = args[i].equals("1");
                        break;
                    case "-l1_gp":
                        i++;
                        mstL1.UseGrandParents = args[i].equals("1");
                        break;
                    case "-l1_ac":
                        i++;
                        mstL1.UseAllchildren = args[i].equals("1");
                        break;
                    case "-l1_ph":
                        i++;
                        mstL1.UsePredHead = args[i].equals("1");
                        break;
                    case "-l1_v":
                        i++;
                        mstL1.UseValency = args[i].equals("1");
                        break;
                    // -- mst stack parameters --//
                    case "-l1_iter":
                        i++;
                        mstL1.Iteration = Integer.parseInt(args[i]);
                        break;
                    case "-l1_decode":
                        i++;
                        mstL1.DecodeType = args[i].toLowerCase();
                        break;
                    case "-l1_k":
                        i++;
                        mstL1.TrainingK = Integer.parseInt(args[i]);
                        break;
                    case "-l1_order":
                        i++;
                        mstL1.Order = Integer.parseInt(args[i]);
                        break;
                    case "-l1_loss":
                        i++;
                        mstL1.LossType = args[i];
                        break;
                    case "-l1_output":
                        i++;
                        mstL1.Output = args[i];
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            exception = true;
            visisble = false;
        }
        finally {
            Settings settings = null;
            switch (parser) {
                case MaltParser:
                    malt.WorkingDirectory = "tmp" + File.separator;
                    settings = malt;
                    break;
                case ClearParser:
                    settings = clear;
                    break;
                case MSTParser:
                    settings = mst;
                    break;
                case MateTools:
                    settings = mate;
                    break;
            }
            settings.Input = input;
            settings.Model = model;
            settings.Output = output;
            
            Settings settingsL0 = null;
            switch (parserL0) {
                case MaltParser:
                    settingsL0 = maltL0;
                    break;
                case MSTParser:
                    settingsL0 = mstL0;
                    break;
            }
            settingsL0.Input = input;
            
            if (visisble == false) {
                if (exception == true) {
                    showHelp(helpChart);
                    System.exit(1);
                }
                else {
                    File in;
                    File md;
                    Runnable run = null;
                    switch(mode) {
                        case Read:
                            if (input.length() == 0) {
                                System.out.println("input file not entered.");
                                System.exit(1);
                            }
                            ReadCorpus.getStatistics(input);
                            break;
                        case Proj:
                            if (input.length() == 0 || 
                                output.length() == 0 ||
                                model.length() == 0) {
                                System.out.println("some parameter not entered.");
                                System.exit(1);
                            }
                            in = new File(workingDir + File.separator + input);
                            if (!in.exists()) {
                                System.out.println("cannot find input file.");
                                System.exit(1);
                            }
                            run = new RunnableProjectivize(
                                    null,                       // target
                                    TransformType.Projectivize, // Transform type
                                    workingDir,                 // Working directory
                                    input, output, model, markingStrategy, 
                                    coveredRoot, LiftingOrder);
                            break;
                        case Deproj:
                            if (input.length() == 0 || 
                                output.length() == 0 ||
                                model.length() == 0) {
                                System.out.println("some parameter not entered.");
                                System.exit(1);
                            }
                            in = new File(workingDir + File.separator + input);
                            if (!in.exists()) {
                                System.out.println("cannot find input file.");
                                System.exit(1);
                            }
                            md = new File(workingDir + File.separator + model + ".mco");
                            if (!md.exists()) {
                                System.out.println("cannot find model file.");
                                System.exit(1);
                            }
                            run = new RunnableProjectivize(
                                    null,                           // target
                                    TransformType.Deprojectivize,   // Transform type
                                    workingDir,                     // Working directory
                                    input, output, model, null, null, null);
                            break;
                        case Optimizer:
                            if (phase.equals("all")) {
                                run = new RunnableOptimizer(null, 1, input, crossVal);
                                run.run();
                                run = new RunnableOptimizer(null, 2, input, crossVal);
                                run.run();
                                run = new RunnableOptimizer(null, 3, input, crossVal);
                            }
                            else {
                                int p = Integer.parseInt(phase);
                                run = new RunnableOptimizer(null, p, input, crossVal);
                            }
                            break;
                        case Train:
                            run = new RunnableTrain(null, parser, null, settings);
                            break;
                        case Parse:
                            run = new RunnableParse(null, parser, null, settings);
                            break;
                        case Eval:
                            if (input.length() == 0 || 
                                output.length() == 0 ||
                                gold.length() == 0) {
                                System.out.println("some parameter not entered.");
                                System.exit(1);
                            }
                            in = new File(workingDir + File.separator + input);
                            if (!in.exists()) {
                                System.out.println("cannot find input file.");
                                System.exit(1);
                            }
                            in = new File(workingDir + File.separator + gold);
                            if (!in.exists()) {
                                System.out.println("cannot find gold file.");
                                System.exit(1);
                            }
                            eval.parseFile = input;
                            eval.goldFile = gold;
                            eval.outputFile = output;
                            MaltEvalConsole.main(eval.getParameters());
                            try {
                                BufferedReader reader = new BufferedReader(new FileReader(output));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                }
                            }
                            catch (Exception e) {}
                            break;
                        case Ensemble:
                            if (input.length() == 0 || 
                                output.length() == 0 ||
                                gold.length() == 0) {
                                System.out.println("some parameter not entered.");
                                System.exit(1);
                            }
                            String[] ins = input.split(",");
                            for (int i = 0; i < ins.length; i++) {
                                in = new File(workingDir + File.separator + ins[i]);
                                if (!in.exists()) {
                                    System.out.println("cannot find input file '" + ins[i] + "'.");
                                    System.exit(1);
                                }
                            }
                            in = new File(workingDir + File.separator + gold);
                            if (!in.exists()) {
                                System.out.println("cannot find gold file.");
                                System.exit(1);
                            }
                            List<String> sysFiles = new LinkedList<>();
                            Collections.addAll(sysFiles, input.split(","));
                            ReparseType type = ReparseType.valueOf(method);
                            run = new RunnableHybrid(null, null, 
                                    gold, sysFiles, output, type);
                            break;
                        case Stack:
                            maltL0.Model = mstL0.Model = "ModelL0.mco";
                            mstL1.Model = "ModelL1.mco";
                            switch (level) {
                                case "all":
                                    run = new RunnableHybrid(null, null, 
                                            parserL0, 
                                            settingsL0);
                                    run.run();
                                    run = new RunnableHybrid(null, null, 
                                            parserL1, 
                                            mstL1);
                                    break;
                                case "0":
                                    run = new RunnableHybrid(null, null, 
                                            parserL0, settingsL0);
                                    break;
                                case "1":
                                    run = new RunnableHybrid(null, null, 
                                            parserL1, mstL1);
                                    break;
                            }
                            break;
                        case None:
                            showHelp(helpChart);
                            break;
                    }
                    if (run != null) {
                        run.run();
                    }
                }
                System.exit(0);
            }
            else {
                showHelp(helpChart);
            }
        }
        
        DependencyParserApp application = new DependencyParserApp();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(application);
            application.pack();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
        }
        application.setVisible(visisble);
    }
    
    private static ParserType getParserType(String parser) {
        switch(parser.toLowerCase()) {
            default:
            case "malt":
                return ParserType.MaltParser;
            case "clear":
                return ParserType.ClearParser;
            case "mst":
                return ParserType.MSTParser;
            case "mate":
                return ParserType.MateTools;
        }
    }
    
    private static void showIntroduction() {
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("                      Dependency Parser Toolbox 1.0");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("                            Mojtaba Khallash");
        System.out.println();
        System.out.println("             Iran University of Science and Technology (IUST)");
        System.out.println("                                 Iran");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println();        
    }
    
    private static void showHelp(Flowchart chart) {
        System.out.println("Required Arguments:");
        System.out.println("        -v <visibility (0|1)>");
        System.out.println("        -mode <operation-mode(read|proj|deproj|optimizer|");
        System.out.println("                              train|parse|eval|ensemble|stack)>\n");
        switch(chart) {
            case None:
                System.out.println("        Use (-help <mode-value>) for more parameters.");
                break;
            case Read:
                System.out.println("        >> read: reading corpus and get statistical info");
                System.out.println("        -i <input conll file>");
                break;
            case Proj:
                System.out.println("        >> proj: Projectivizing treebank");
                System.out.println("        -i <input conll file>");
                System.out.println("        -o <projectivized output>");
                System.out.println("        -m <projectivizing model name>");
                System.out.println("        -mark <marking-strategy (None|Baseline|Head|Path|Head+Path)");
                System.out.println("                [default: Head]>");
                System.out.println("        -covered <covered-root (None|Ignore|Left|Right|Head) [default: Head]>");
                System.out.println("        -lift_order <lifting-order (Shortest|Deepest) [default: Shortest]>");
                break;
            case Deproj:
                System.out.println("        >> deproj: Deprojectivize treebank");
                System.out.println("        -i <input conll file>");
                System.out.println("        -m <existing projectivizing model name>");
                System.out.println("        -o <deprojectivizing output>");
                break;
            case Optimizer:
                System.out.println("        >> optimizer: Optimizing algorithm parameters and feature model");
                System.out.println("        -i <training-corput>");
                System.out.println("        -parser <parser-type (malt)>");
                System.out.println("        -phase <optimizing phase (1|2|3|all) [default: all for running all phases]>");
                System.out.println("        -cross_val <using 5-fold cross-validation (0|1) [default: 0]>");
                break;
            case Train:
                System.out.println("        >> train: Training from annotated data");
                System.out.println("        -i <input training corpus>");
                System.out.println("        -m <name of training model>");
                System.out.println("        -parser <parser-type (malt|clear|mst|mate) [default: malt]>");
                System.out.println("            >> malt parameters:");
                System.out.println("                -option <option-file>");
                System.out.println("                -guide <guide-file>");
                System.out.println("            >> clear parameters:");
                System.out.println("                -option <option-file>");
                System.out.println("                -guide <guide-file>");
                System.out.println("                -bootstrap <bootstrapping-level [default: 2]>");
                System.out.println("            >> mst parameters:");
                System.out.println("                -decode <decode-type (proj|non-proj) [default: non-proj]>");
                System.out.println("                -loss <loss-type (punc|nopunc) [default: punc]>");
                System.out.println("                -order <order (1|2) [default: 2]>");
                System.out.println("                -k <training k-best [default: 1]>");
                System.out.println("                -iter <training iterations [default: 10]>");
                System.out.println("            >> mate parameters:");
                System.out.println("                -decode <decode-type (proj|non-proj) [default: non-proj]>");
                System.out.println("                -threshold <nonprojective-threshold (0-1) [default: 0.3]>");
                System.out.println("                -creation <feature-creation (multiplicative|shift) [default: multiplicative]>");
                System.out.println("                -core <number-of-core [default: max-exiting-cores]>");
                System.out.println("                -iter <training iterations [default: 10]>");
                break;
            case Parse:
                System.out.println("        >> parse: Parsing with trained model");
                System.out.println("        -i <input parsing file>");
                System.out.println("        -m <name of trined model>");
                System.out.println("        -o <output parsed name>");
                System.out.println("        -parser <parser-type (malt|clear|mst|mate) [default: malt]>");
                System.out.println("            >> malt parameters: [None]");
                System.out.println("            >> clear parameters:");
                System.out.println("                -option <option-file>");
                System.out.println("            >> mst parameters:");
                System.out.println("                -decode <decode-type (proj|non-proj) [default: non-proj]>");
                System.out.println("                -order <order (1|2) [default: 2]>");
                System.out.println("            >> mate parameters:");
                System.out.println("                -decode <decode-type (proj|non-proj) [default: non-proj]>");
                System.out.println("                -threshold <nonprojective-threshold (0-1) [default: 0.3]>");
                System.out.println("                -creation <feature-creation (multiplicative|shift) [default: multiplicative]>");
                System.out.println("                -core <number-of-core [default: max-exiting-cores]>");
                break;
            case Eval:
                System.out.println("        >> eval: Evaluating parsed file with gold data");
                System.out.println("        -i <input parsed file>");
                System.out.println("        -g <gold file>");
                System.out.println("        -o <output eval log>");
                System.out.println("        -metric <metric (LAS|LA|UAS|AnyRight|AnyWrong|BothRight|BothWrong|HeadRight|HeadWrong|");
                System.out.println("                         LabelRight|LabelWrong|DirectionRight|GroupedHeadToChildDistanceRight|");
                System.out.println("                         HeadToChildDistanceRight) [default: LAS]");
                System.out.println("            NOTE: for selecting multiple metrics, separate them by comma.>");
                System.out.println("        -group <group-by (Token|Wordform|Lemma|Cpostag|Postag|Feats|Deprel|Sentence|RelationLength|");
                System.out.println("                          GroupedRelationLength|SentenceLength|StartWordPosition|EndWordPosition|");
                System.out.println("                          ArcDirection|ArcDepth|BranchingFactor|ArcProjectivity|Frame) [default: Token]>");
                // and ...
                break;
            case Ensemble:
                System.out.println("        >> ensemble: Ensemble for combining base parsers in parse time");
                System.out.println("        -i <input baseline parsers file (separate by comma)>");
                System.out.println("        -g <gold file>");
                System.out.println("        -o <output file>");
                System.out.println("        -method <method (majority|attardi|eisner|chu_liu_edmond) [default: majority]>");
                break;
            case Stack:
                System.out.println("        >> stack: Stacking for combining base parsers in train time");
                System.out.println("        -i <input train file>");
                System.out.println("        -t <input test file>");
                System.out.println("        -l <level (0|1|all) [default: all for running both level]>");
                System.out.println("        -l0_part <level0 augmented parts [default: 5]>");
                System.out.println("        -l0_out_train <level0 output augmented train>");
                System.out.println("        -l0_out_parse <level0 output ougmented parse>");
                System.out.println("        -l0_parser <level0 parser-type (malt|mst) [default: mst]>");
                System.out.println("            >> malt parameters:");
                System.out.println("                -l0_option <level0 option-file>");
                System.out.println("                -l0_guide <level0 guide-file>");
                System.out.println("            >> mst parameters:");
                System.out.println("                -l0_decode <level0 decode-type (proj|non-proj) [default: non-proj]>");
                System.out.println("                -l0_loss <level0 loss-type (punc|nopunc) [default: punc]>");
                System.out.println("                -l0_order <level0 order (1|2) [default: 2]>");
                System.out.println("                -l0_k <level0 training k-best [default: 1]>");
                System.out.println("                -l0_iter <level0 training iterations [default: 10]>");
                System.out.println("        -l1_pe <level1 use predicted edge (0|1) [default: 1]>");
                System.out.println("        -l1_ps <level1 use previous sibling (0|1) [default: 1]>");
                System.out.println("        -l1_ns <level1 use next sibling (0|1) [default: 1]>");
                System.out.println("        -l1_gp <level1 use grandparent (0|1) [default: 1]>");
                System.out.println("        -l1_ac <level1 use all childs (0|1) [default: 1]>");
                System.out.println("        -l1_ph <level1 use predicted head (0|1) [default: 1]>");
                System.out.println("        -l1_v <level1 use valency (0|1) [default: 1]>");
                System.out.println("        -l1_parser <level1 parser-type (mst) [default: mst]>");
                System.out.println("            >> mst parameters:");
                System.out.println("                -l1_decode <level1 decode-type (proj|non-proj) [default: non-proj]>");
                System.out.println("                -l1_loss <level1 loss-type (punc|nopunc) [default: punc]>");
                System.out.println("                -l1_order <level1 order (1|2) [default: 2]>");
                System.out.println("                -l1_k <level1 training k-best [default: 1]>");
                System.out.println("                -l1_iter <level1 training iterations [default: 10]>");
                System.out.println("        -l1_output <level1 parsed output>");
                break;
        }
    }
    
    private static Flowchart getChart(String mode) {
        switch(mode) {
            case "read":
                return Flowchart.Read;
            case "proj":
                return Flowchart.Proj;
            case "deproj":
                return Flowchart.Deproj;
            case "optimizer":
                return Flowchart.Optimizer;
            case "train":
                return Flowchart.Train;
            case "parse":
                return Flowchart.Parse;
            case "eval":
                return Flowchart.Eval;
            case "ensemble":
                return Flowchart.Ensemble;
            case "stack":
                return Flowchart.Stack;
        }
        
        return Flowchart.None;
    }
}