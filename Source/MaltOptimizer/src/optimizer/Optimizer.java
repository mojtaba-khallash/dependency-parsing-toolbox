package optimizer;

import algorithmTester.AlgorithmTester;
import java.io.*;
import java.util.*;

/**
 *
 * @author Miguel Ballesteros
 *
 */
public class Optimizer {

    public static boolean ShowMaltLog = false;
    public static boolean ExitInEnd = true;
    public static boolean pseudoRandomizeSelection = false;
    public static boolean chooseMajority = false;
    public static boolean chooseAverage = false;
    public static boolean chooseAllOfThem = false;
    public static boolean crossValidation = false;
    public static String maltPath = "malt.jar"; //malt path  
    private String trainingCorpus;
    public static String testCorpus = "";
    private String language = "lang";
    private boolean projective;
    private boolean strictRoot;
    private boolean coveredRoots;
    private boolean coveredRootsWithoutChildren;
    public static int numbTokens;
    public static int numbSentences;
    private double percentage;
    private int numbDanglingCases = 0;
    public static double defaultBaseline = 0.0;
    public static boolean allow_rootNiv = true;
    public static boolean allow_reduceNiv = false;
    public final static String pattern = "%.2f";
    
    private Writer writer = null;
    public static PrintStream out = System.out;

    public int getNumbDanglingCases() {
        return numbDanglingCases;
    }

    public void setNumbDanglingCases(int numbDanglingCases) {
        this.numbDanglingCases = numbDanglingCases;
    }

    public int getNumbTokens() {
        return numbTokens;
    }

    public void setNumbTokens(int numbTokens) {
        Optimizer.numbTokens = numbTokens;
    }

    public int getNumbSentences() {
        return numbSentences;
    }

    public void setNumbSentences(int numbSentences) {
        Optimizer.numbSentences = numbSentences;
    }
    public static String bestAlgorithm;
    private boolean rootGRL;
    private boolean danglingPunctuation;
    public static String pcrOption = "none";
    private boolean pcr;
    public static int numRootLabels = 1;
    public static String optionGRL = "ROOT";
    public static String optionMenosR = "normal";
    public static String ppOption = "head";
    public static boolean usePPOption = false;
    public static boolean allow_shift = false;
    public static boolean allow_root = true;
    public static Double bestResult = 0.0;
    public static String javaHeapValue = "";
    private boolean smallCaseBothThings = false;
    private boolean noNonProjective = false;
    private boolean substantialNonProjective = false;
    public static int nMaxTokens = Integer.MAX_VALUE;
    public static String featureModel = "NivreEager.xml";
    public static String featureModelBruteForce = "bruteForce1.xml";
    public static String InputLookAhead = "Input";
    public static boolean cposEqPos = false;
    public static boolean lemmaBlank = true;
    public static boolean featsBlank = true;
    public static double threshold = 0.05;
    public static double bestResultBruteForce = 0.0;
    public static String libraryValue = "-s_4_-c_0.1";
    public static boolean includePunctuation = true;
    public static int order = 0;
    public static String evaluationMeasure = "LAS";
    public static String featureAlgorithm = "Greedy";

    public boolean isSmallCaseBothThings() {
        return smallCaseBothThings;
    }

    public void setSmallCaseBothThings(boolean smallCaseBothThings) {
        this.smallCaseBothThings = smallCaseBothThings;
    }

    public boolean isNoNonProjective() {
        return noNonProjective;
    }

    public void setNoNonProjective(boolean noNonProjective) {
        this.noNonProjective = noNonProjective;
    }

    public boolean isSubstantialNonProjective() {
        return substantialNonProjective;
    }

    public void setSubstantialNonProjective(boolean substantialNonProjective) {
        this.substantialNonProjective = substantialNonProjective;
    }

    public Optimizer() {
    }

    public void setCorpus(String c) {
        trainingCorpus = c;
    }

    public void runPhase1() {
        smallCaseBothThings = false;

        ShowIntroduction();
        
        println("PHASE 1: DATA ANALYSIS");
        println("In order to optimize MaltParser for your training set, MaltOptimizer will " + "\n"
                + "first analyze the data and set some basic parameters.");

        println("-----------------------------------------------------------------------------");

        String validationName = "validateFormat.py";
        String SharedTaskName = "SharedTaskCommon.py";
        String s;
        Process p = null;
        try {
            println("DATA VALIDATION");
            println("Validating the CoNLL data format ... ");
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            File val = new File(validationName);
            if (!val.exists()) {
                BufferedWriter bwValidateFormat;
                try {
                    bwValidateFormat = new BufferedWriter(new FileWriter(validationName));
                    bwValidateFormat.write(ValidationGenerator.generateValidateFormat());
                    bwValidateFormat.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File task = new File(SharedTaskName);
            if (!task.exists()) {
                BufferedWriter bwSharedTaskCommon;
                try {
                    bwSharedTaskCommon = new BufferedWriter(new FileWriter(SharedTaskName));
                    bwSharedTaskCommon.write(ValidationGenerator.generateSharedTaskCommon());
                    bwSharedTaskCommon.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            println(" (may take a few minutes)");
            p = Runtime.getRuntime().exec("java -Dfile.encoding=UTF8 -jar lib" + File.separator + "jython.jar validateFormat.py " + trainingCorpus);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            /*
             * BufferedReader stdOutput = new BufferedReader(new
             * InputStreamReader(
	                p.getOutputStream()));
             */
            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            boolean right;
            int warnings;
            try (BufferedWriter bwLog = new BufferedWriter(new FileWriter("logValidationFile.txt"))) {
                right = false;
                warnings = 0;
                while ((s = stdError.readLine()) != null) {
                    bwLog.write(s + "\n");
                    warnings++;
                    if (s.equals("Exit status =  0")) {
                        right = true;
                    }
                }
            }
            warnings--;
            if (right) {
                if (warnings == 0) {
                    println("Your training set is in valid CoNLL format.");
                }
                if (warnings > 0) {
                    println("Your training set is in valid CoNLL format, but the validation script");
                    println("gave some warnings, so you may want to consult the logfile\n " + System.getProperty("user.dir") + File.separator + "logValidationFile.txt .");
                }
            } else {
                println("Your training set is not in valid CoNLL format. MaltOptimizer will");
                println("terminate. Please consult the logfile " + System.getProperty("user.dir") + File.separator + "logFile.txt");
                println("to find out what needs to be fixed.");
                
                Exit();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (p != null)
                p.destroy();
            
            File val = new File(validationName);
            if (val.exists()) {
               val.delete();
            }

            File task = new File(SharedTaskName);
            if (task.exists()) {
               task.delete();
            }

            File taskClass = new File("SharedTaskCommon$py.class");
            if (taskClass.exists()) {
               taskClass.delete();
            }
        }
        println("-----------------------------------------------------------------------------");
        println("DATA CHARACTERISTICS");


        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);


        println(ch.extraDataCharacteristics());
        int numToks = ch.getNumbTokens();



        percentage = ch.projectiveOrNonProjective();
        String cad = String.valueOf(percentage);
        if (cad.length() > 5) {
            cad = cad.substring(0, 5);
        }
        //println("There are "+ch.getNumberOfSentences()+" sentences in the corpus.");
        if (percentage > 15) { //non-negligible argument
            println("Your training set contains a substantial amount of non-projective trees (" + cad + " %).");
            //println("The system is going to test only non-projective algorithms.");
            projective = false;
        } else {
            if (percentage == 0) {
                println("Your training set contains no non-projective trees.");
                //println("The system is going to test only projective algorithms.");
                projective = true;
            } else {
                println("Your training set contains a small amount of non-projective trees (" + cad + " %).");
                //println("The system is going to test both kind of algorithms: projective and non-projective.");
                smallCaseBothThings = true;
            }
        }



        //Dangling Punctuation
		/*
         * coveredRoots=ch.coveredRoots(); if (coveredRoots) {
         * println("There are signs of covered roots."); } else {
         * println("There are no signs of covered roots."); }
         *
         * coveredRootsWithoutChildren=ch.coveredRootsWithoutChildren(); if
         * (coveredRootsWithoutChildren) { println("There are signs
         * of covered roots without children."); } else {
         * println("There are no signs of covered roots without
         * children.");
		}
         */

        danglingPunctuation = ch.danglingPunctuation();
        String danglingFreq = ch.getDanglingFreq();
        Integer di = Integer.parseInt(danglingFreq);
        this.numbDanglingCases = di;
        if (danglingPunctuation) {
            println("Your training set contains unattached internal punctuation (" + danglingFreq + " instances).");
        } else {
            println("Your training set does not contain unattached internal punctuation.");
        }




        //ROOT GRL?
        String realRoot = "";
        rootGRL = ch.rootLabels();
        HashMap<String, Double> rootLabels = ch.getRootlabels();
        //ArrayList<String> getThreeFrequent(rootLabels);
        if (rootGRL) {
            Set<String> set = rootLabels.keySet();
            Iterator<String> it = set.iterator();
            println("Your training set has multiple DEPREL labels for tokens where HEAD=0:");
            //println("Frequency of labels used for tokens with HEAD = 0 .");
            while (it.hasNext()) {
                String r = it.next();
                Double d = rootLabels.get(r) * 100;
                String val = d.toString();
                if (val.length() < 3) {
                    val = val.substring(0, 2);
                } else if (val.length() > 5) {
                    val = val.substring(0, 5);
                }
                println(r + ": " + val + "% ");
                numRootLabels++;
            }
            if (numRootLabels > 1) {
                numRootLabels--;
            }
            /*
             * println(""); println("The system is going
             * to test which root label configuration is better.");
             * println("it may take a few minutes...");
             */

            optionMenosR = "normal";
        } else {
            Set<String> set = rootLabels.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String r = it.next();
                realRoot = r;
            }
            println("Your training set has a unique DEPREL label for tokens where HEAD=0:" + realRoot + ".");
            if (realRoot.equals("ROOT")) {
                //println("There is no need to test which ROOT label configuration is better.");
                pcr = false;
                //optionMenosR="strict";
            } else {
                pcr = true;
                optionGRL = realRoot;
                //optionMenosR="strict";
                //println("There is no need to test which ROOT label configuration is better.");
            }
        }

        println("-----------------------------------------------------------------------------");









        println("BASIC OPTIMIZATION SETUP ");
        print("Generating training and test files for optimization");
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            print(".");
        }
        println(".");

        println(ch.getMessageDivision());

        if (ch.getNumbTokens() > 100000) {
            println("Given that your data set is relatively large, we recommend using a single \ndevelopment set during subsequent optimization phases. If you prefer to use 5-fold cross-validation, you can specify this instead (-v cv).");
        } else {
            println("Given that your data set is relatively small, we recommend using 5-fold \ncross-validation during subsequent optimization phases (-v cv).");
        }

        //Language Detection
				/*
         * String frase=ch.getSamplePlainText(); /*LanguageDetector ld=new
         * LanguageDetector(frase); language=ld.getLanguage();
         * println("The system has detected your corpus is written in:"+language+".");
         */
        language = "lang";

        //String realRoot=ch.getHead0();
        //Head 0 to root?:"+rootGRL+"(Root:"+realRoot+")");

        print("Testing the default settings ");
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            print(".");
        }

        println(". (may take a few seconds)");
        AlgorithmTester atdefault = new AlgorithmTester(language, ch, trainingCorpus, writer);

        Double bestDefaultResult = atdefault.executeDefault();
        String sBestDefLabelResult = String.format(pattern, bestDefaultResult);
        println("LAS with default settings: " + sBestDefLabelResult + "%");
        defaultBaseline = bestDefaultResult;
        bestResult = bestDefaultResult;


        //println("-------------------");	


        if (rootGRL) {
            print("Testing root labels ");
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                print(".");
            }
            println(". ");
            //
            ArrayList<String> threeLabels = getThreeFrequent(rootLabels);
            //println(threeLabels);
            AlgorithmTester atroot = new AlgorithmTester(language, ch, trainingCorpus, writer);

            String bestLabel = atroot.executeLabelTest(threeLabels);
            println("Default root label reset: -grl " + bestLabel);
            Double bestLabelResult = atroot.getBestLabelLASResult();
            String sBestLabelResult = String.format(pattern, bestLabelResult);
            Double difference;
            println(String.format(pattern, bestResult));
            println(sBestLabelResult);
            if (bestResult < bestLabelResult) {
                difference = bestLabelResult - bestResult;
                bestResult = bestLabelResult;

                String sDifferenceLabel = String.format(pattern, difference);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + sBestLabelResult + "%)");
                optionGRL = bestLabel;
                optionMenosR = "normal";
            }
        }


        if (danglingPunctuation) {
            print("Testing preprocessing of unattached punctuation ");
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                print(".");
            }
            println(". ");
            AlgorithmTester atpcr = new AlgorithmTester(language, ch, trainingCorpus, writer);

            String bestOption = atpcr.executePCRTest();
            pcrOption = bestOption;
            println("Treatment of covered roots reset: -pcr " + bestOption);
            Double bestLabelResult = atpcr.getBestLabelLASResult();
            String sBestLabelResult = String.format(pattern, bestLabelResult);
            Double difference;
            if (bestResult < bestLabelResult) {
                difference = bestLabelResult - bestResult;
                bestResult = bestLabelResult;

                String sDifferenceLabel = String.format(pattern, difference);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + sBestLabelResult + "%)");
            }


            /*
             * Double bestLabelResult=atroot.getBestLabelLASResult(); String
             * sBestLabelResult=""+bestLabelResult; if
             * (sBestLabelResult.length()>5)
             * sBestLabelResult=sBestLabelResult.substring(0, 5); Double
             * difference=0.0; if (bestResult<bestLabelResult)
             * difference=bestLabelResult-bestResult; String
             * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
             * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
             * println("Incremental "+evaluationMeasure+"
             * improvement: +"+sDifferenceLabel+"% ("+sBestLabelResult+"%)");
             */
        }



        OptionsGenerator ogen = new OptionsGenerator();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel) 

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsFile = ogen.generateIncOptionsPhase1(language, "nivreeager", AlgorithmTester.training80, optionMenosR, los.getLibraryOptions(), optionGRL, pcrOption);

        BufferedWriter bwOptionsNivreEager;

        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter("incr_optionFile.xml"));
            bwOptionsNivreEager.write(optionsFile);
            bwOptionsNivreEager.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        numbTokens = ch.getNumbTokens();
        numbSentences = ch.getNumbSentences();
        numbDanglingCases = Integer.parseInt(danglingFreq);
        createLogFile(1);
        createOptionFile(1);

        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the analysis of your training set and saved the");
        println("results for future use in /phase1_logFile.txt. Updated MaltParser options can be found");
        println("in /phase1_optFile.txt. If you want to change any of these options, you should");
        println("edit /phase1_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 2 (Parsing Algorithm) run the following command:");
        println("java -jar MaltOptimizer.jar -p 2 -m <malt_path> -c <trainingCorpus>");

        Exit();

        //*******
        //HAY QUE ENCAJAR AHORA LA PHASE 1: Data characteristics con la Phase 2: Algorithm Testing
        //Es decir: cosas que faltarían, option GRL!, option PCR!, etc

        //Testing the ALGORITHMS!
		/*
         * println("-----------------------------------------------------------------------------");
         * println("ALGORITHM TESTING"); println("it may
         * take a few minutes...");
         *
         * AlgorithmTester at=new AlgorithmTester(language,
         * percentage,ch,trainingCorpus,optionMenosR); bestAlgorithm=""; if
         * (projective) { bestAlgorithm=at.executeProjectivity();
         * println("The System has inferred that the best algorithm
         * for your corpus is: "+bestAlgorithm+"."); try { Process
         * p2=Runtime.getRuntime().exec("rm "+language+"Model.mco"); Process
         * p3=Runtime.getRuntime().exec("rm "+language+"ModelStack.mco");
         * BufferedReader stdInput = new BufferedReader(new InputStreamReader(
         * p2.getInputStream())); /*BufferedReader stdOutput = new
         * BufferedReader(new InputStreamReader( p.getOutputStream()));
         * BufferedReader stdError = new BufferedReader(new InputStreamReader(
         * p2.getErrorStream()));
         *
         * BufferedReader stdInput3 = new BufferedReader(new InputStreamReader(
         * p3.getInputStream())); /*BufferedReader stdOutput = new
         * BufferedReader(new InputStreamReader( p.getOutputStream()));
         * BufferedReader stdError3 = new BufferedReader(new InputStreamReader(
         * p3.getErrorStream()));
         *
         * // Leemos la salida del comando //println("Ésta es la
         * salida standard del comando:\n"); while ((s = stdInput.readLine()) !=
         * null) {} while ((s = stdInput3.readLine()) != null) {} } catch
         * (IOException e) { 
         * e.printStackTrace(); } } else {
         * bestAlgorithm=at.executeNonProjectivity(); println("The
         * System has inferred that the best non-projective algorithm for your
         * corpus is: "+bestAlgorithm+"."); //NON-PROJECTIVE CODE //....
         *
         * if (smallCaseBothThings) { bestAlgorithm=at.executeProjectivity();
         * println("The System has inferred that the best projective
         * algorithm for your corpus is: "+bestAlgorithm+"."); try { Process
         * p2=Runtime.getRuntime().exec("rm "+language+"Model.mco"); Process
         * p3=Runtime.getRuntime().exec("rm "+language+"ModelStack.mco");
         * BufferedReader stdInput = new BufferedReader(new InputStreamReader(
         * p2.getInputStream())); /*BufferedReader stdOutput = new
         * BufferedReader(new InputStreamReader( p.getOutputStream()));
         * BufferedReader stdError = new BufferedReader(new InputStreamReader(
         * p2.getErrorStream()));
         *
         * BufferedReader stdInput3 = new BufferedReader(new InputStreamReader(
         * p3.getInputStream())); /*BufferedReader stdOutput = new
         * BufferedReader(new InputStreamReader( p.getOutputStream()));
         * BufferedReader stdError3 = new BufferedReader(new InputStreamReader(
         * p3.getErrorStream()));
         *
         * // Leemos la salida del comando //println("Ésta es la
         * salida standard del comando:\n"); while ((s = stdInput.readLine()) !=
         * null) {} while ((s = stdInput3.readLine()) != null) {} } catch
         * (IOException e) { 
         * e.printStackTrace(); } } } //}
         *
         * //Cleaning files
         *
         *
         * println("-----------------------------------------------------------------------------");
         * println("FEATURE TESTING"); println("it may
         * take a few minutes..."); double bestResult=at.getBestResult(); String
         * bestFeature=""; boolean nivreeager=false; if
         * (bestAlgorithm.equals("nivreeager")){ nivreeager=true; }
         *
         * FeatureGenerator fg=new FeatureGenerator(language, writer, out); String
         * featureBaseline="StackSwap.xml"; if (nivreeager){
         * featureBaseline="NivreEager.xml"; } // Decision tree if (nivreeager)
         * { fg.removeInputNivreEager(featureBaseline, "1"+featureBaseline);
         * //Run the experiment double
         * result1=at.executeNivreEagerDefault("1"+featureBaseline); if
         * (result1>bestResult) { bestResult=result1;
         * fg.removeStack("1"+featureBaseline, "2"+featureBaseline); double
         * result2=at.executeNivreEagerDefault("2"+featureBaseline); if
         * (result2>bestResult) { bestFeature="2"+featureBaseline; } else {
         * bestFeature="1"+featureBaseline; } } else{
         * fg.addInputNivreEager(featureBaseline, "3"+featureBaseline); double
         * result3=at.executeNivreEagerDefault("3"+featureBaseline); if
         * (result3>bestResult) { bestResult=result3;
         * bestFeature="3"+featureBaseline; } else {
         * fg.removeStack(featureBaseline, "4"+featureBaseline); double
         * result4=at.executeNivreEagerDefault("4"+featureBaseline); if
         * (result4>bestResult) { bestResult=result4;
         * bestFeature="4"+featureBaseline; } else {
         * bestFeature=featureBaseline; } } } } else { //STACKLAZY
         *
         *
         * fg.removeLookAheadStackLazy(featureBaseline, "1"+featureBaseline);
         * println("Trying with the first modification of the feature
         * model."); double result1=at.executeStackLazy("1"+featureBaseline);
         * println(result1); if (result1>bestResult) {
         * bestResult=result1; fg.removeStack("1"+featureBaseline,
         * "2"+featureBaseline); println("Trying with the another
         * modification of the feature model."); double
         * result2=at.executeStackLazy("2"+featureBaseline);
         * println(result2); if (result2>bestResult) {
         * bestFeature="2"+featureBaseline; } else {
         * bestFeature="1"+featureBaseline; } } else{
         * fg.addLookAheadStackLazy(featureBaseline, "3"+featureBaseline);
         * println("Trying with another modification of the feature
         * model."); double result3=at.executeStackLazy("3"+featureBaseline);
         * println(result3); if (result3>bestResult) {
         * bestResult=result3; bestFeature="3"+featureBaseline; } else {
         * fg.removeStack(featureBaseline, "4"+featureBaseline);
         * println("Trying with another modification of the feature
         * model."); double result4=at.executeStackLazy("4"+featureBaseline);
         * println(result4); if (result4>bestResult) {
         * bestResult=result4; bestFeature="4"+featureBaseline; } else {
         * bestFeature=featureBaseline; } } } }
         *
         * println("The system has inferred that the best results
         * possible is:"+bestResult); println("It is obtained with
         * this feature model:"+ bestFeature); fg.printFeature(bestFeature);
         *
         * //
         *
         * try { Process p2=Runtime.getRuntime().exec("rm
         * optionsNivreEager.xml"); Process p3=Runtime.getRuntime().exec("rm
         * optionsStackLazy.xml"); BufferedReader stdInput = new
         * BufferedReader(new InputStreamReader( p2.getInputStream()));
         * /*BufferedReader stdOutput = new BufferedReader(new
         * InputStreamReader( p.getOutputStream())); BufferedReader stdError =
         * new BufferedReader(new InputStreamReader( p2.getErrorStream()));
         *
         * BufferedReader stdInput3 = new BufferedReader(new InputStreamReader(
         * p3.getInputStream())); /*BufferedReader stdOutput = new
         * BufferedReader(new InputStreamReader( p.getOutputStream()));
         * BufferedReader stdError3 = new BufferedReader(new InputStreamReader(
         * p3.getErrorStream()));
         *
         * // Leemos la salida del comando //println("Ésta es la
         * salida standard del comando:\n"); while ((s = stdInput.readLine()) !=
         * null) {} while ((s = stdInput3.readLine()) != null) {} } catch
         * (IOException e) { 
         * e.printStackTrace(); }
         *
         * println("-----------------------------------------------------------------------------");
         * println("LIBRARY OPTIONS TESTING"); println("it
         * may take a few minutes...");
         *
         * String
         * bestLibraryOptions="-s_0_-t_1_-d_2_-g_0.2_-c_1.0_-r_0.4_-e_0.1";
         *
         * LibraryOptionsSetter lo=LibraryOptionsSetter.getSingleton();
         *
         * println("First Experiment. C=0.5.");
         *
         * lo.incrementC(-0.5); double result; if (nivreeager) {
         * result=at.executeNivreEagerDefault(bestFeature); } else { //STACKLAZY
         * result=at.executeStackLazy(bestFeature);
         *
         * }
         * if (result>bestResult){ bestLibraryOptions=lo.getLibraryOptions();
         * println("This new set of library options is better."); }
         * else { lo.incrementC(-0.4); println("First Experiment.
         * C=0.1"); double result2; if (nivreeager) {
         * result2=at.executeNivreEagerDefault(bestFeature); } else {
         * //STACKLAZY result2=at.executeStackLazy(bestFeature);
         *
         * }
         * if (result2>bestResult){ bestLibraryOptions=lo.getLibraryOptions();
         * println("This new set of library options is better."); } }
         *
         * println("The best results the systems can provide
         * is:"+bestResult+"."); println("The best library options
         * are "+ bestLibraryOptions+".");
         *
         * println("-----------------------------------------------------------------------------");
         * println("LIBRARY OPTIONS TESTING");
         *
         * OptionsGenerator og=new
         * OptionsGenerator(bestAlgorithm,language,trainingCorpus,bestLibraryOptions+".");
         * println("Therefore, this is the <OptionsFile>.xml that the
         * system suggests:"); println(og.generateOptionsFile());
         */
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    private void loadPhase1Results(String pathTrainingSet) {
        //phase1_optFile.txt
        //phase1_logFile.txt
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("phase1_logFile.txt"));
            try {
                int contador = 0;
                while (br.ready()) {
                    String line;
                    try {
                        line = br.readLine();
                        StringTokenizer st = new StringTokenizer(line, ":");
                        String tok = "";
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        }
                        contador++;
                        if (contador == 1) {
                            if (pathTrainingSet.equals(tok)) {
                                this.setTrainingCorpus(tok);
                            } else {
                                try {
                                    throw new PathNotFoundException(writer);
                                } catch (PathNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (contador == 2) {
                            Integer nt = Integer.parseInt(tok);
                            this.setNumbTokens(nt);
                        }
                        if (contador == 3) {
                            Integer nt = Integer.parseInt(tok);
                            this.setNumbSentences(nt);
                        }
                        if (contador == 4) {
                            Double nt = Double.parseDouble(tok);
                            this.setPercentage(nt);
                            if (nt == 0.0) {
                                this.setNoNonProjective(true);
                            } else {
                                if (nt > 15) {
                                    this.setSubstantialNonProjective(true);
                                } else {
                                    this.setSmallCaseBothThings(true);
                                }
                            }
                        }
                        if (contador == 5) {
                            Integer it = Integer.parseInt(tok);
                            if (it > 0) {
                                this.setDanglingPunctuation(true);
                            }
                            this.setNumbDanglingCases(it);
                        }
                        if (contador == 6) {
                            Double nt = Double.parseDouble(tok);
                            Optimizer.setBestResult(nt);
                        }
                        if (contador == 7) {
                            Double nt = Double.parseDouble(tok);
                            Optimizer.setDefaultBaseline(nt);
                        }
                        if (contador == 8) {
                            Integer nt = Integer.parseInt(tok);
                            Optimizer.numRootLabels = nt;
                        }
                        if (contador == 9) {
                            javaHeapValue = tok;
                        }
                        if (contador == 10) {
                            Integer nt = Integer.parseInt(tok);
                            nMaxTokens = nt;
                        }

                        if (contador == 11) {
                            if (tok.equals("true")) {
                                cposEqPos = true;
                            } else {
                                cposEqPos = false;
                            }
                        }
                        if (contador == 12) {
                            if (tok.equals("true")) {
                                lemmaBlank = true;
                            } else {
                                lemmaBlank = false;
                            }
                        }
                        if (contador == 13) {
                            if (tok.equals("true")) {
                                featsBlank = true;
                            } else {
                                featsBlank = false;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        BufferedReader brOpt;
        try {
            brOpt = new BufferedReader(new FileReader("phase1_optFile.txt"));
            try {
                int contador = 0;
                boolean grl;
                boolean pcr;
                while (brOpt.ready()) {
                    String line;
                    try {
                        line = brOpt.readLine();
                        StringTokenizer st = new StringTokenizer(line, ":");
                        grl = false;
                        pcr = false;
                        if (line.contains("grl")) {
                            grl = true;
                        }
                        if (line.contains("pcr")) {
                            pcr = true;
                        }
                        String tok = "";
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        }
                        contador++;
                        if (grl) {
                            Optimizer.setOptionGRL(tok);
                            grl = false;
                        }
                        if (pcr) {
                            Optimizer.setPcrOption(tok);
                            pcr = false;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    public static double getDefaultBaseline() {
        return defaultBaseline;
    }

    public static void setDefaultBaseline(double defaultBaseline) {
        Optimizer.defaultBaseline = defaultBaseline;
    }

    public static int getNumRootLabels() {
        return numRootLabels;
    }

    public static void setNumRootLabels(int numRootLabels) {
        Optimizer.numRootLabels = numRootLabels;
    }

    public static String getPpOption() {
        return ppOption;
    }

    public static void setPpOption(String ppOption) {
        Optimizer.ppOption = ppOption;
    }

    public static boolean isUsePPOption() {
        return usePPOption;
    }

    public static void setUsePPOption(boolean usePPOption) {
        Optimizer.usePPOption = usePPOption;
    }

    public static boolean isAllow_shift() {
        return allow_shift;
    }

    public static void setAllow_shift(boolean allow_shift) {
        Optimizer.allow_shift = allow_shift;
    }

    public static boolean isAllow_root() {
        return allow_root;
    }

    public static void setAllow_root(boolean allow_root) {
        Optimizer.allow_root = allow_root;
    }

    public void runPhase2() {
        
        ShowIntroduction();

        println("PHASE 2: PARSING ALGORITHM SELECTION\n");
        
        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        for (int i = 1; i < 6; i++) {
            new File ("fold_train_" + i + ".conll").delete();
        }
        ch.generateDivision8020();
        
        Optimizer.bestAlgorithm = "nivreeager";

        if (this.noNonProjective) { //case: Strictly projective case (non projectivities==0)
            println("MaltOptimizer found in Phase 1 that your training set contains");
            println("no non-projective trees and will therefore only try projective parsing algorithms.\n");
            //println("Testing the no non projective algorithms ...");
            runStrictlyProjective();
        } else {
            if (this.substantialNonProjective) { //case non projectivities>15
                println("MaltOptimizer found in Phase 1 that your training set contains");
                println("a substantial amount of non-projective trees and will therefore \nonly try non-projective algorithms.\n");
                //println("Testing the non-projective algorithms ...");
                runLargeAmountNonProjective();
            } else if (this.smallCaseBothThings) { //case non projectivities <15
                println("MaltOptimizer found in Phase 1 that your training set contains");
                println("a small amount of non-projective trees and will therefore \ntry both projective and non-projective algorithms.\n");
                runStrictlyProjective();
                runLargeAmountNonProjective();
            }
        }
        println("-----------------------------------------------------------------------------");
        String bestAlgoPrintOut = bestAlgorithm;
        if (bestAlgorithm.equals("nivreeager")) {
            bestAlgoPrintOut = "NivreEager";
        }

        if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            bestAlgoPrintOut = "NivreStandard";
        }

        if (bestAlgorithm.equals("covnonproj")) {
            bestAlgoPrintOut = "CovingtonNonProjective";
        }

        if (bestAlgorithm.equals("covproj")) {
            bestAlgoPrintOut = "CovingtonProjective";
        }

        if (bestAlgorithm.equals("stackproj")) {
            bestAlgoPrintOut = "StackProjective";
        }

        if (bestAlgorithm.equals("stackeager")) {
            bestAlgoPrintOut = "StackEager";
        }

        if (bestAlgorithm.equals("stacklazy")) {
            bestAlgoPrintOut = "StackLazy";
        }
        if (Optimizer.usePPOption) {
            println("MaltOptimizer found that the best parsing algorithm is: " + bestAlgorithm + "+ pp option");
        } else {
            println("MaltOptimizer found that the best parsing algorithm is: " + bestAlgorithm);
        }

        //after this: Run algorithm specific parameters with the best one.
        //nivre*----> rootHandling (normal|strict|relaxed)
        //cov*-----> allowshift (true|false) and allowroot(true|false)
        //stack*---> don't have any
        //if -pp is better, test (baseline | head | path | head+path)
        AlgorithmTester at = new AlgorithmTester(this.language, ch, this.trainingCorpus, writer);
        Double difference;


        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////

        if (bestAlgorithm.contains("cov")) {
            //allow_shift test2
            if (bestAlgorithm.equals("covproj")) {
                println("Testing the Covington--Projective algorithm ...");
                
                double covprojLAS;
                if (Optimizer.usePPOption) {
                    covprojLAS = at.executeCovingtonProjectivePPAllowShiftAllowRoot("CovingtonProjective", "head", true, allow_root);
                } else {
                    covprojLAS = at.executeCovingtonProjectiveAllowShiftAllowRoot("CovingtonProjective", true, allow_root);
                }
                
                if (covprojLAS > (Optimizer.bestResult + threshold)) {
                    Optimizer.bestAlgorithm = "covproj";
                    difference = covprojLAS - bestResult;
                    Optimizer.bestResult = covprojLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    Optimizer.allow_shift = true;
                    println("New allow_shift option: true");
                    Optimizer.bestAlgorithm = "covproj";
                    String bestRes = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                }
                if (Optimizer.usePPOption) {
                    covprojLAS = at.executeCovingtonProjectivePPAllowShiftAllowRoot("CovingtonProjective", "head", allow_shift, false);
                } else {
                    covprojLAS = at.executeCovingtonProjectiveAllowShiftAllowRoot("CovingtonProjective", allow_shift, false);
                }
                if (covprojLAS > (Optimizer.bestResult + threshold)) {
                    Optimizer.bestAlgorithm = "covproj";
                    difference = covprojLAS - bestResult;
                    Optimizer.bestResult = covprojLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    Optimizer.allow_root = false;
                    println("New allow_root option: false");
                    Optimizer.bestAlgorithm = "covproj";
                    String bestRes = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                }
            } else {
                if (bestAlgorithm.equals("covnonproj")) {
                    println("Testing the Covington-Non-Projective algorithm ...");
                    
                    double covnonprojLAS;
                    if (Optimizer.usePPOption) {
                        covnonprojLAS = at.executeCovingtonNonProjectivePPAllowShiftAllowRoot("CovingtonNonProjective", "head", true, allow_root);
                    } else {
                        covnonprojLAS = at.executeCovingtonNonProjectiveAllowShiftAllowRoot("CovingtonNonProjective", true, allow_root);
                    }
                    
                    if (covnonprojLAS > (Optimizer.bestResult + threshold)) {
                        Optimizer.bestAlgorithm = "covnonproj";
                        difference = covnonprojLAS - bestResult;
                        Optimizer.bestResult = covnonprojLAS;
                        String sDifferenceLabel = String.format(pattern, difference);
                        Optimizer.allow_shift = true;
                        println("New allow_shift option: true");
                        Optimizer.bestAlgorithm = "covnonproj";
                        String bestRes = String.format(pattern, bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                    }
                    if (Optimizer.usePPOption) {
                        covnonprojLAS = at.executeCovingtonNonProjectivePPAllowShiftAllowRoot("CovingtonNonProjective", "head", allow_shift, false);
                    } else {
                        covnonprojLAS = at.executeCovingtonNonProjectiveAllowShiftAllowRoot("CovingtonNonProjective", allow_shift, false);
                    }
                    if (covnonprojLAS > (Optimizer.bestResult + threshold)) {
                        Optimizer.bestAlgorithm = "covnonproj";
                        difference = covnonprojLAS - bestResult;
                        Optimizer.bestResult = covnonprojLAS;
                        String sDifferenceLabel = String.format(pattern, difference);
                        Optimizer.allow_root = false;
                        println("New allow_root option: false");
                        Optimizer.bestAlgorithm = "covnonproj";
                        String bestRes = String.format(pattern, bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                    }
                }
            }
        }

        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////

        //println(bestAlgorithm);

        if (bestAlgorithm.contains("nivre")) {// && numRootLabels==1) {

            /*
             * println("Root handling testing ...");
             * optionMenosR=at.executeRootHandlingTest(bestAlgorithm); if
             * (!optionMenosR.equals("normal")){ println("Default
             * root handling strategy reset: "+optionMenosR);
			}
             */

            /////////////////////////NEW FOR VERSION 1.7 //////////
            ///allow_root and allow_reduce

            //allow_root=true  --- allow_reduce=false  (DEFAULT-> No need to run)
            //allow_root=false --- allow_reduce=false
            //allow_root=true  --- allow_reduce=true
            //allow_root=False --- allow_reduce=true

            println("Root Handling testing ...");
            at.executeRootHandlingTestNivre17(bestAlgorithm);
            println("Root handling selected strategy:");
            println("\t" + "allow_root: " + allow_rootNiv);
            println("\t" + "allow_reduce: " + allow_reduceNiv);

            ////////////////////////////////////////////////////////
        }

        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        if (usePPOption) {
            println("Testing pseudo-projective (PP) options ...");
            ppOption = at.executePPTest(bestAlgorithm);
            if (!ppOption.equals("head")) {
                println("Default marking strategy reset: " + ppOption);
            }
        }

        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String bestRes = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 2: +" + sDifferenceLabel + "% (" + bestRes + "%) ");


        OptionsGenerator ogen = new OptionsGenerator();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel) 

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr, String pp, boolean allowShift, boolean allowROOT) {
        String optionsFile = ogen.generateIncOptionsTestingsEndPhase2(language, "nivreeager", AlgorithmTester.training80, optionMenosR, los.getLibraryOptions(), optionGRL, pcrOption, ppOption, allow_shift, allow_root);

        BufferedWriter bwOptionsNivreEager;

        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter("incr_optionFile.xml"));
            bwOptionsNivreEager.write(optionsFile);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(2);
        createOptionFile(2);

        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the parsing algorithm selection phase for your");
        println("training set and saved the results for future use in phase2_logFile.txt. ");
        println("Updated MaltParser options can be found in phase2_optFile.txt. If you want");
        println("to change any of these options, you should edit phase2_optFile.txt before.");
        println("you start the next optimization phase.\n");
        println("To proceed with Phase 3 (Feature Selection) run the following command:");
        println("java -jar MaltOptimizer.jar -p 3 -m <malt_path> -c <trainingCorpus>");

        Exit();
    }

    public void runPhase25Fold() {
        
        ShowIntroduction();

        println("PHASE 2: PARSING ALGORITHM SELECTION - 5-FOLD CROSS-VALIDATION\n");
        //println("Cross-validation\n");
        
        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        ch.generateDivision8020();
        ch.generate5FoldCrossCorporaPseudo();
            
        Optimizer.bestAlgorithm = "nivreeager";

        if (this.noNonProjective) { //case: Strictly projective case (non projectivities==0)
            println("MaltOptimizer found in Phase 1 that your training set contains");
            println("no non-projective trees and will therefore only try projective parsing algorithms.\n");
            //println("Testing the no non projective algorithms ...");
            runStrictlyProjective5Fold();
        } else {
            if (this.substantialNonProjective) { //case non projectivities>15
                println("MaltOptimizer found in Phase 1 that your training set contains");
                println("a substantial amount of non-projective trees and will therefore \nonly try non-projective algorithms.\n");
                //println("Testing the non-projective algorithms ...");
                runLargeAmountNonProjective5Fold();
            } else if (this.smallCaseBothThings) { //case non projectivities <15
                println("MaltOptimizer found in Phase 1 that your training set contains");
                println("a small amount of non-projective trees and will therefore \ntry both projective and non-projective algorithms.\n");
                runStrictlyProjective5Fold();
                runLargeAmountNonProjective5Fold();
            }
        }
        println("-----------------------------------------------------------------------------");
        String bestAlgoPrintOut = bestAlgorithm;
        if (bestAlgorithm.equals("nivreeager")) {
            bestAlgoPrintOut = "NivreEager";
        }

        if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            bestAlgoPrintOut = "NivreStandard";
        }

        if (bestAlgorithm.equals("covnonproj")) {
            bestAlgoPrintOut = "CovingtonNonProjective";
        }

        if (bestAlgorithm.equals("covproj")) {
            bestAlgoPrintOut = "CovingtonProjective";
        }

        if (bestAlgorithm.equals("stackproj")) {
            bestAlgoPrintOut = "StackProjective";
        }

        if (bestAlgorithm.equals("stackeager")) {
            bestAlgoPrintOut = "StackEager";
        }

        if (bestAlgorithm.equals("stacklazy")) {
            bestAlgoPrintOut = "StackLazy";
        }
        if (Optimizer.usePPOption) {
            println("MaltOptimizer found that the best parsing algorithm is: " + bestAlgorithm + "+ pp option");
        } else {
            println("MaltOptimizer found that the best parsing algorithm is: " + bestAlgorithm);
        }

        //after this: Run algorithm specific parameters with the best one.
        //nivre*----> rootHandling (normal|strict|relaxed)
        //cov*-----> allowshift (true|false) and allowroot(true|false)
        //stack*---> don't have any
        //if -pp is better, test (baseline | head | path | head+path)
        AlgorithmTester at = new AlgorithmTester(this.language, ch, this.trainingCorpus, writer);
        Double difference;


        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////

        if (bestAlgorithm.contains("cov")) {
            //allow_shift test2
            if (bestAlgorithm.equals("covproj")) {
                println("Testing the Covington--Projective algorithm ...");
                
                Double covprojLAS;
                if (Optimizer.usePPOption) {
                    covprojLAS = runCovingtonProjectivePPAllowShiftAllowRoot5Fold("CovingtonProjective.xml", "head", true, allow_root);
                } else {
                    covprojLAS = runCovingtonProjectiveAllowShiftAllowRoot5Fold("CovingtonProjective.xml", true, allow_root);
                }
                
                if (covprojLAS > (Optimizer.bestResult + threshold)) {
                    Optimizer.bestAlgorithm = "covproj";
                    difference = covprojLAS - bestResult;
                    Optimizer.bestResult = covprojLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    Optimizer.allow_shift = true;
                    println("New allow_shift option: true");
                    Optimizer.bestAlgorithm = "covproj";
                    String bestRes = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                }
                if (Optimizer.usePPOption) {
                    covprojLAS = runCovingtonProjectivePPAllowShiftAllowRoot5Fold("CovingtonProjective.xml", "head", allow_shift, false);
                } else {
                    covprojLAS = runCovingtonProjectiveAllowShiftAllowRoot5Fold("CovingtonProjective.xml", allow_shift, false);
                }
                if (covprojLAS > (Optimizer.bestResult + threshold)) {
                    Optimizer.bestAlgorithm = "covproj";
                    difference = covprojLAS - bestResult;
                    Optimizer.bestResult = covprojLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    Optimizer.allow_root = false;
                    println("New allow_root option: false");
                    Optimizer.bestAlgorithm = "covproj";
                    String bestRes = String.format(Optimizer.pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                }

            } else {
                if (bestAlgorithm.equals("covnonproj")) {
                    println("Testing the Covington-Non-Projective algorithm ...");
                    
                    double covnonprojLAS;
                    if (Optimizer.usePPOption) {
                        covnonprojLAS = runCovingtonNonProjectivePPAllowShiftAllowRoot5Fold("CovingtonNonProjective.xml", "head", true, allow_root);
                    } else {
                        covnonprojLAS = runCovingtonNonProjectiveAllowShiftAllowRoot5Fold("CovingtonNonProjective.xml", true, allow_root);
                    }
                    
                    if (covnonprojLAS > (Optimizer.bestResult + threshold)) {
                        Optimizer.bestAlgorithm = "covnonproj";
                        difference = covnonprojLAS - bestResult;
                        Optimizer.bestResult = covnonprojLAS;
                        String sDifferenceLabel = String.format(pattern, difference);
                        Optimizer.allow_shift = true;
                        println("New allow_shift option: true");
                        Optimizer.bestAlgorithm = "covnonproj";
                        String bestRes = String.format(pattern, bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");

                    }
                    if (Optimizer.usePPOption) {
                        covnonprojLAS = runCovingtonNonProjectivePPAllowShiftAllowRoot5Fold("CovingtonNonProjective.xml", "head", allow_shift, false);
                    } else {
                        covnonprojLAS = runCovingtonNonProjectiveAllowShiftAllowRoot5Fold("CovingtonNonProjective.xml", allow_shift, false);
                    }
                    if (covnonprojLAS > (Optimizer.bestResult + threshold)) {
                        Optimizer.bestAlgorithm = "covnonproj";
                        difference = covnonprojLAS - bestResult;
                        Optimizer.bestResult = covnonprojLAS;
                        String sDifferenceLabel = String.format(pattern, difference);
                        Optimizer.allow_root = false;
                        println("New allow_root option: false");
                        Optimizer.bestAlgorithm = "covnonproj";
                        String bestRes = String.format(pattern, bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + bestRes + "%)");
                    }
                }
            }
        }

        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////

        /*
         * if (bestAlgorithm.contains("nivre") && numRootLabels==1) {
         * println("Root handling testing ...");
         * optionMenosR=at.executeRootHandlingTest(bestAlgorithm); if
         * (!optionMenosR.equals("normal")){ println("Default root
         * handling strategy reset: "+optionMenosR); }
         *
         * }
         */
        
        if (bestAlgorithm.contains("nivre")) {// && numRootLabels==1) {

            /*
             * println("Root handling testing ...");
             * optionMenosR=at.executeRootHandlingTest(bestAlgorithm); if
             * (!optionMenosR.equals("normal")){ println("Default
             * root handling strategy reset: "+optionMenosR);
			}
             */

            /////////////////////////NEW FOR VERSION 1.7 //////////
            ///allow_root and allow_reduce

            //allow_root=true  --- allow_reduce=false  (DEFAULT-> No need to run)
            //allow_root=false --- allow_reduce=false
            //allow_root=true  --- allow_reduce=true
            //allow_root=False --- allow_reduce=true

            println("Root Handling testing ...");
            at.executeRootHandlingTestNivre17(bestAlgorithm);
            println("Root handling selected strategy:");
            println("\t" + "allow_root: " + allow_rootNiv);
            println("\t" + "allow_reduce: " + allow_reduceNiv);
        }

        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        if (usePPOption) {
            println("Testing pseudo-projective (PP) options ...");
            ppOption = at.executePPTest(bestAlgorithm);
            if (!ppOption.equals("head")) {
                println("Default marking strategy reset: " + ppOption);
            }
        }

        double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String bestRes = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 2: +" + sDifferenceLabel + "% (" + bestRes + "%) ");


        OptionsGenerator ogen = new OptionsGenerator();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel) 

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr, String pp, boolean allowShift, boolean allowROOT) {
        String optionsFile = ogen.generateIncOptionsTestingsEndPhase2(language, "nivreeager", AlgorithmTester.training80, optionMenosR, los.getLibraryOptions(), optionGRL, pcrOption, ppOption, allow_shift, allow_root);

        BufferedWriter bwOptionsNivreEager;

        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter("incr_optionFile.xml"));
            bwOptionsNivreEager.write(optionsFile);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(2);
        createOptionFile(2);

        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the parsing algorithm selection phase for your");
        println("training set and saved the results for future use in phase2_logFile.txt. ");
        println("Updated MaltParser options can be found in phase2_optFile.txt. If you want");
        println("to change any of these options, you should edit phase2_optFile.txt before.");
        println("you start the next optimization phase.\n");
        println("To proceed with Phase 3 (Feature Selection) run the following command:");
        println("java -jar MaltOptimizer.jar -p 3 -m <malt_path> -c <trainingCorpus>");

        Exit();
    }

    /*
     * public ArrayList<String> getThreeFrequent(HashMap<String,Double>()) {
     *
     * return null;
	}
     */
    private void runLargeAmountNonProjective() {

        Double bestNonProjective;
        String bestAlgNonProjective;
        println("Testing the non-projective algorithms ...");
        println("");
        println("               CovingtonNonProjective --vs-- StackLazy");
        println("                          /                     \\");
        println("                         /                       \\");
        println("                        /                         \\");
        println("                       /                           \\");
        println("                      /                             \\");
        println("                     /                               \\");
        println("                    /                                 \\");
        println("               NivreEager+PP             StackEager --vs-- StackProjective+PP");
        println("                    |                                  |");
        println("                    |                                  |");
        println("         CovingtonProjective+PP                 NivreStandard+PP");
        println("");
        println("");


        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester(this.language, ch, this.trainingCorpus, writer);
        Double difference;

        println("Testing the Covington-Non-Projective algorithm ...");
        double covnonprojLAS = at.executeCovingtonNonProjective("CovingtonNonProjective");
        bestNonProjective = covnonprojLAS;
        bestAlgNonProjective = "covnonproj";
        if (covnonprojLAS > (bestResult)) {
            difference = covnonprojLAS - bestResult;
            bestResult = covnonprojLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: covnonproj");
            Optimizer.bestAlgorithm = "covnonproj";

            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }

        println("Testing the StackLazy algorithm ...");
        double stacklazyLAS = at.executeStackLazy("StackSwap");
        if (stacklazyLAS > bestNonProjective) {
            bestNonProjective = stacklazyLAS;
            bestAlgNonProjective = "stacklazy";
        }
        if (stacklazyLAS > (Optimizer.bestResult)) {
            difference = stacklazyLAS - bestResult;
            Optimizer.bestResult = stacklazyLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: stacklazy");
            Optimizer.bestAlgorithm = "stacklazy";
            String s = String.format(pattern, Optimizer.bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }
        switch (bestAlgNonProjective) {
            case "covnonproj":
                //if (bestAlgNonProjective.equals("nivreeager")) {
                println("Testing the NivreEager algorithm with pseudo-projective parsing (PP) ...");
                double nivreEagerLAS = at.executeNivreEagerPPOption("NivreEager", Optimizer.ppOption);
                if (nivreEagerLAS > (bestResult)) {
                    difference = nivreEagerLAS - bestResult;
                    bestResult = nivreEagerLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    println("New best algorithm: nivreeager");
                    Optimizer.bestAlgorithm = "nivreeager";
                    Optimizer.usePPOption = true;
                    String s = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");

                    println("Testing the Covington-Projective algorithm with pseudo-projective parsing (PP) ...");
                    double covprojLAS = at.executecovprojPPOption("CovingtonProjective", Optimizer.ppOption);
                    if (covprojLAS > (bestResult)) {
                        bestAlgorithm = "covproj";
                        difference = covprojLAS - bestResult;
                        bestResult = covprojLAS;
                        String sDifferenceLabel2 = String.format(pattern, difference);
                        println("New best algorithm: covproj + pp option");
                        Optimizer.bestAlgorithm = "covproj";
                        Optimizer.usePPOption = true;
                        s = String.format(pattern, Optimizer.bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel2 + "% (" + s + "%)");
                    }
                }
                break;
            case "stacklazy":
                println("Testing the StackEager algorithm ...");
                double stackeagerLAS = at.executestackEager("StackSwap");
                if (stackeagerLAS > (bestResult)) {
                    difference = stackeagerLAS - bestResult;
                    bestResult = stackeagerLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    println("New best algorithm: stackeager");
                    Optimizer.bestAlgorithm = "stackeager";
                    String s = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
                }
                println("Testing the StackProjective algorithm with pseudo-projective parsing (PP) ...");
                double stackprojLAS = at.executestackprojPPOption("StackProjective", Optimizer.ppOption);
                if (stackprojLAS > (bestResult)) {
                    bestAlgorithm = "stackproj";
                    difference = stackprojLAS - bestResult;
                    bestResult = stackprojLAS;
                    String sDifferenceLabel2 = String.format(pattern, difference);
                    println("New best algorithm: stackproj + pp option");
                    Optimizer.bestAlgorithm = "stackproj";
                    Optimizer.usePPOption = true;
                    String s = String.format(pattern, Optimizer.bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel2 + "% (" + s + "%)");
                }
                if (Optimizer.bestAlgorithm.equals("stackproj") && (Optimizer.usePPOption == true)) {

                    println("Testing the NivreStandard algorithm with pseudo-projective parsing (PP) ...");
                    double nivrestandardLAS = at.executenivrestandardPPOption("NivreStandard", Optimizer.ppOption);
                    if (nivrestandardLAS > bestResult) {
                        bestAlgorithm = "nivrestandard";
                        difference = nivrestandardLAS - bestResult;
                        bestResult = nivrestandardLAS;
                        String sDifferenceLabel2 = String.format(pattern, difference);
                        println("New best algorithm: nivrestandard + pp option");
                        Optimizer.bestAlgorithm = "nivrestandard";
                        Optimizer.usePPOption = true;
                        String s = String.format(pattern, bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel2 + "% (" + s + "%)");
                    }
                }
                break;
        }

        String bestAlgoPrintOut = bestAlgNonProjective;
        if (bestAlgNonProjective.equals("nivreeager")) {
            bestAlgoPrintOut = "NivreEager";
        }

        if (bestAlgNonProjective.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            bestAlgoPrintOut = "NivreStandard";
        }

        if (bestAlgNonProjective.equals("covnonproj")) {
            bestAlgoPrintOut = "CovingtonNonProjective";
        }

        if (bestAlgNonProjective.equals("covproj")) {
            bestAlgoPrintOut = "CovingtonProjective";
        }

        if (bestAlgNonProjective.equals("stackproj")) {
            bestAlgoPrintOut = "StackProjective";
        }

        if (bestAlgNonProjective.equals("stackeager")) {
            bestAlgoPrintOut = "StackEager";
        }

        if (bestAlgNonProjective.equals("stacklazy")) {
            bestAlgoPrintOut = "StackLazy";
        }
        println("Best Non-Projective algorithm: " + bestAlgoPrintOut + "\n");
    }

    private void runLargeAmountNonProjective5Fold() {

        println("Testing the non-projective algorithms ...");
        println("");
        println("               CovingtonNonProjective --vs-- StackLazy");
        println("                          /                     \\");
        println("                         /                       \\");
        println("                        /                         \\");
        println("                       /                           \\");
        println("                      /                             \\");
        println("                     /                               \\");
        println("                    /                                 \\");
        println("               NivreEager+PP             StackEager --vs-- StackProjective+PP");
        println("                    |                                  |");
        println("                    |                                  |");
        println("         CovingtonProjective+PP                 NivreStandard+PP");
        println("");
        println("");


        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester(this.language, ch, this.trainingCorpus, writer);
        Double difference;

        println("Testing the Covington-Non-Projective algorithm ...");
        Double covnonprojLAS = this.runAlgorithm5Fold("CovingtonNonProjective.xml", "covnonproj");
        Double bestNonProjective = covnonprojLAS;
        String bestAlgNonProjective = "covnonproj";
        if (covnonprojLAS > bestResult) {
            difference = covnonprojLAS - bestResult;
            bestResult = covnonprojLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: covnonproj");
            bestAlgorithm = "covnonproj";
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }

        println("Testing the StackLazy algorithm ...");
        Double stacklazyLAS = runAlgorithm5Fold("StackSwap.xml", "stacklazy");
        if (stacklazyLAS > bestNonProjective) {
            bestNonProjective = stacklazyLAS;
            bestAlgNonProjective = "stacklazy";
        }
        if (stacklazyLAS > bestResult) {
            difference = stacklazyLAS - bestResult;
            bestResult = stacklazyLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: stacklazy");
            Optimizer.bestAlgorithm = "stacklazy";
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }
        switch (bestAlgNonProjective) {
            case "covnonproj":
                //if (bestAlgNonProjective.equals("nivreeager")) {
                println("Testing the NivreEager algorithm with pseudo-projective parsing (PP) ...");
                //Double nivreEagerLAS=at.executeNivreEagerPPOption5Fold("NivreEager.xml",this.ppOption);
                Double nivreEagerLAS = this.runAlgorithm5FoldPPOption("NivreEager.xml", "nivreeager");
                if (nivreEagerLAS > bestResult) {
                    difference = nivreEagerLAS - bestResult;
                    bestResult = nivreEagerLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    println("New best algorithm: nivreeager");
                    bestAlgorithm = "nivreeager";
                    usePPOption = true;
                    String s = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");

                    println("Testing the Covington-Projective algorithm with pseudo-projective parsing (PP) ...");
                    Double covprojLAS = this.runAlgorithm5FoldPPOption("CovingtonProjective.xml", "covproj");
                    if (covprojLAS > bestResult) {
                        bestAlgorithm = "covproj";
                        difference = covprojLAS - bestResult;
                        bestResult = covprojLAS;
                        String sDifferenceLabel2 = String.format(pattern, difference);
                        println("New best algorithm: covproj + pp option");
                        Optimizer.bestAlgorithm = "covproj";
                        Optimizer.usePPOption = true;
                        s = String.format(pattern, Optimizer.bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel2 + "% (" + s + "%)");
                    }
                }
                break;
            case "stacklazy":
                println("Testing the StackEager algorithm ...");
                //Double stackeagerLAS=at.executestackEager5Fold("StackSwap.xml");
                Double stackeagerLAS = runAlgorithm5Fold("StackSwap.xml", "stackeager");
                if (stackeagerLAS > bestResult) {
                    difference = stackeagerLAS - bestResult;
                    bestResult = stackeagerLAS;
                    String sDifferenceLabel = String.format(pattern, difference);
                    println("New best algorithm: stackeager");
                    bestAlgorithm = "stackeager";
                    String s = String.format(pattern, bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
                }
                println("Testing the StackProjective algorithm with pseudo-projective parsing (PP) ...");
                Double stackprojLAS = runAlgorithm5FoldPPOption("StackProjective.xml", "stackproj");
                if (stackprojLAS > bestResult) {
                    bestAlgorithm = "stackproj";
                    difference = stackprojLAS - bestResult;
                    bestResult = stackprojLAS;
                    String sDifferenceLabel2 = String.format(pattern, difference);
                    println("New best algorithm: stackproj + pp option");
                    bestAlgorithm = "stackproj";
                    usePPOption = true;
                    String s = String.format(pattern, Optimizer.bestResult);
                    println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel2 + "% (" + s + "%)");
                }
                if (Optimizer.bestAlgorithm.equals("stackproj") && (Optimizer.usePPOption == true)) {

                    println("Testing the NivreStandard algorithm with pseudo-projective parsing (PP) ...");
                    Double nivrestandardLAS = runAlgorithm5FoldPPOption("NivreStandard.xml", "nivrestandard");
                    if (nivrestandardLAS > (Optimizer.bestResult)) {
                        Optimizer.bestAlgorithm = "nivrestandard";
                        difference = nivrestandardLAS - bestResult;
                        Optimizer.bestResult = nivrestandardLAS;
                        String sDifferenceLabel2 = String.format(pattern, difference);
                        println("New best algorithm: nivrestandard + pp option");
                        Optimizer.bestAlgorithm = "nivrestandard";
                        Optimizer.usePPOption = true;
                        String s = String.format(pattern, bestResult);
                        println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel2 + "% (" + s + "%)");
                    }
                }
                break;
        }

        String bestAlgoPrintOut = bestAlgNonProjective;
        if (bestAlgNonProjective.equals("nivreeager")) {
            bestAlgoPrintOut = "NivreEager";
        }

        if (bestAlgNonProjective.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            bestAlgoPrintOut = "NivreStandard";
        }

        if (bestAlgNonProjective.equals("covnonproj")) {
            bestAlgoPrintOut = "CovingtonNonProjective";
        }

        if (bestAlgNonProjective.equals("covproj")) {
            bestAlgoPrintOut = "CovingtonProjective";
        }

        if (bestAlgNonProjective.equals("stackproj")) {
            bestAlgoPrintOut = "StackProjective";
        }

        if (bestAlgNonProjective.equals("stackeager")) {
            bestAlgoPrintOut = "StackEager";
        }

        if (bestAlgNonProjective.equals("stacklazy")) {
            bestAlgoPrintOut = "StackLazy";
        }
        println("Best Non-Projective algorithm: " + bestAlgoPrintOut + "\n");
    }

    private void runStrictlyProjective() {

        //First: nivreeager against stackproj
        //if nivreeager better, test covproj
        //else test nivrestandard
        println("Testing projective algorithms ...");
        println("");
        println("                       NivreEager --vs-- StackProjective");
        println("                           /                  \\");
        println("                          /                    \\");
        println("                         /                      \\");
        println("                        /                        \\");
        println("                       /                          \\");
        println("                      /                            \\");
        println("                     /                              \\");
        println("            CovingtonProjective                 NivreStandard");
        println("");
        println("");



        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester(this.language, ch, this.trainingCorpus, writer);
        Double difference;
        //this.bestAlgorithm="nivreeager";

        println("Testing the NivreEager algorithm ...");
        Double nivreEagerLAS = at.executeNivreEager("NivreEager");
        if (nivreEagerLAS > (Optimizer.bestResult)) {
            difference = nivreEagerLAS - bestResult;
            Optimizer.bestResult = nivreEagerLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: nivreeager");
            Optimizer.bestAlgorithm = "nivreeager";
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }

        println("Testing the StackProjective algorithm ...");
        double stackprojLAS = at.executeStackProjective("StackProjective");
        if (stackprojLAS > (Optimizer.bestResult)) {
            Optimizer.bestAlgorithm = "stackproj";
            difference = stackprojLAS - bestResult;
            Optimizer.bestResult = stackprojLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: stackproj");
            Optimizer.bestAlgorithm = "stackproj";
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }

        if (Optimizer.bestAlgorithm.equals("stackproj")) {
            println("Testing the NivreStandard algorithm ...");
            double nivrestandardLAS = at.executeNivreStandard("NivreStandard");
            if (nivrestandardLAS > (Optimizer.bestResult)) {
                Optimizer.bestAlgorithm = "nivrestandard";
                difference = nivrestandardLAS - bestResult;
                Optimizer.bestResult = nivrestandardLAS;
                String sDifferenceLabel = String.format(pattern, difference);
                println("New best algorithm: nivrestandard");
                Optimizer.bestAlgorithm = "nivrestandard";
                String s = String.format(pattern, bestResult);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        } else {
            println("Testing the Covington--Projective algorithm ...");
            Double covprojLAS = at.executeCovingtonProjective("CovingtonProjective");
            if (covprojLAS > (Optimizer.bestResult)) {
                Optimizer.bestAlgorithm = "covproj";
                difference = covprojLAS - bestResult;
                Optimizer.bestResult = covprojLAS;
                String sDifferenceLabel = String.format(pattern, difference);
                println("New best algorithm: covproj");
                Optimizer.bestAlgorithm = "covproj";
                String s = String.format(pattern, bestResult);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        }

        String bestAlgoPrintOut = bestAlgorithm;
        if (bestAlgorithm.equals("nivreeager")) {
            bestAlgoPrintOut = "NivreEager";
        }

        if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            bestAlgoPrintOut = "NivreStandard";
        }

        if (bestAlgorithm.equals("covnonproj")) {
            bestAlgoPrintOut = "CovingtonNonProjective";
        }

        if (bestAlgorithm.equals("covproj")) {
            bestAlgoPrintOut = "CovingtonProjective";
        }

        if (bestAlgorithm.equals("stackproj")) {
            bestAlgoPrintOut = "StackProjective";
        }

        if (bestAlgorithm.equals("stackeager")) {
            bestAlgoPrintOut = "StackEager";
        }

        if (bestAlgorithm.equals("stacklazy")) {
            bestAlgoPrintOut = "StackLazy";
        }
        println("Best projective algorithm: " + bestAlgoPrintOut + "\n");
    }

    private void runStrictlyProjective5Fold() {

        //First: nivreeager against stackproj
        //if nivreeager better, test covproj
        //else test nivrestandard
        println("Testing projective algorithms ...");
        println("");
        println("                       NivreEager --vs-- StackProjective");
        println("                           /                  \\");
        println("                          /                    \\");
        println("                         /                      \\");
        println("                        /                        \\");
        println("                       /                          \\");
        println("                      /                            \\");
        println("                     /                              \\");
        println("            CovingtonProjective                 NivreStandard");
        println("");
        println("");



        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester(this.language, ch, this.trainingCorpus, writer);
        Double difference;
        //this.bestAlgorithm="nivreeager";

        println("Testing the NivreEager algorithm ...");
        Double nivreEagerLAS = runAlgorithm5Fold("NivreEager.xml", "nivreeager");
        if (nivreEagerLAS > (Optimizer.bestResult)) {
            difference = nivreEagerLAS - bestResult;
            Optimizer.bestResult = nivreEagerLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: nivreeager");
            Optimizer.bestAlgorithm = "nivreeager";
            String s = String.format(pattern, Optimizer.bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }

        println("Testing the StackProjective algorithm ...");
        Double stackprojLAS = runAlgorithm5Fold("StackProjective.xml", "stackproj");
        if (stackprojLAS > (Optimizer.bestResult)) {
            Optimizer.bestAlgorithm = "stackproj";
            difference = stackprojLAS - bestResult;
            Optimizer.bestResult = stackprojLAS;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best algorithm: stackproj");
            Optimizer.bestAlgorithm = "stackproj";
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        }

        if (Optimizer.bestAlgorithm.equals("stackproj")) {
            println("Testing the NivreStandard algorithm ...");
            Double nivrestandardLAS = runAlgorithm5Fold("NivreStandard.xml", "nivrestandard");
            if (nivrestandardLAS > (Optimizer.bestResult)) {
                Optimizer.bestAlgorithm = "nivrestandard";
                difference = nivrestandardLAS - bestResult;
                Optimizer.bestResult = nivrestandardLAS;
                String sDifferenceLabel = String.format(pattern, difference);
                println("New best algorithm: nivrestandard");
                Optimizer.bestAlgorithm = "nivrestandard";
                String s = String.format(pattern, Optimizer.bestResult);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        } else {
            println("Testing the Covington--Projective algorithm ...");
            Double covprojLAS = runAlgorithm5Fold("CovingtonProjective.xml", "covproj");
            if (covprojLAS > (Optimizer.bestResult)) {
                Optimizer.bestAlgorithm = "covproj";
                difference = covprojLAS - bestResult;
                Optimizer.bestResult = covprojLAS;
                String sDifferenceLabel = String.format(pattern, difference);
                println("New best algorithm: covproj");
                Optimizer.bestAlgorithm = "covproj";
                String s = String.format(pattern, bestResult);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        }

        String bestAlgoPrintOut = bestAlgorithm;
        if (bestAlgorithm.equals("nivreeager")) {
            bestAlgoPrintOut = "NivreEager";
        }

        if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            bestAlgoPrintOut = "NivreStandard";
        }

        if (bestAlgorithm.equals("covnonproj")) {
            bestAlgoPrintOut = "CovingtonNonProjective";
        }

        if (bestAlgorithm.equals("covproj")) {
            bestAlgoPrintOut = "CovingtonProjective";
        }

        if (bestAlgorithm.equals("stackproj")) {
            bestAlgoPrintOut = "StackProjective";
        }

        if (bestAlgorithm.equals("stackeager")) {
            bestAlgoPrintOut = "StackEager";
        }

        if (bestAlgorithm.equals("stacklazy")) {
            bestAlgoPrintOut = "StackLazy";
        }
        println("Best projective algorithm: " + bestAlgoPrintOut + "\n");
    }

    private void loadPhase2Results(String pathTrainingSet) {

        //phase1_optFile.txt
        //phase1_logFile.txt
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("phase2_logFile.txt"));
            try {
                int contador = 0;
                while (br.ready()) {
                    String line;
                    try {
                        line = br.readLine();
                        StringTokenizer st = new StringTokenizer(line, ":");
                        String tok = "";
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        }
                        contador++;
                        if (contador == 1) {
                            if (pathTrainingSet.equals(tok)) {
                                this.setTrainingCorpus(tok);
                            } else {
                                try {
                                    throw new PathNotFoundException(writer);
                                } catch (PathNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (contador == 2) {
                            Integer nt = Integer.parseInt(tok);
                            this.setNumbTokens(nt);
                        }
                        if (contador == 3) {
                            Integer nt = Integer.parseInt(tok);
                            this.setNumbSentences(nt);
                        }
                        if (contador == 4) {
                            Double nt = Double.parseDouble(tok);
                            this.setPercentage(nt);
                            if (nt == 0.0) {
                                this.setNoNonProjective(true);
                            } else {
                                if (nt > 15) {
                                    this.setSubstantialNonProjective(true);
                                } else {
                                    this.setSmallCaseBothThings(true);
                                }
                            }
                        }
                        if (contador == 5) {
                            Integer it = Integer.parseInt(tok);
                            if (it > 0) {
                                this.setDanglingPunctuation(true);
                            }
                            this.setNumbDanglingCases(it);
                        }
                        if (contador == 6) {
                            Double nt = Double.parseDouble(tok);
                            Optimizer.setBestResult(nt);
                        }
                        if (contador == 7) {
                            Double nt = Double.parseDouble(tok);
                            Optimizer.setDefaultBaseline(nt);
                        }
                        if (contador == 8) {
                            Integer nt = Integer.parseInt(tok);
                            Optimizer.numRootLabels = nt;
                        }
                        if (contador == 9) {
                            Optimizer.javaHeapValue = tok;
                        }

                        if (contador == 10) {
                            //this.javaHeapValue=tok;
                        }

                        if (contador == 11) {
                            if (tok.equals("true")) {
                                cposEqPos = true;
                            } else {
                                cposEqPos = false;
                            }
                        }
                        if (contador == 12) {
                            if (tok.equals("true")) {
                                lemmaBlank = true;
                            } else {
                                lemmaBlank = false;
                            }
                        }
                        if (contador == 13) {
                            if (tok.equals("true")) {
                                featsBlank = true;
                            } else {
                                featsBlank = false;
                            }
                            //println(featsBlank);
                        }

                        /*
                         * if (contador==14) { if (tok.equals("true"))
                         * allow_rootNiv=true; else allow_rootNiv=false;
                         * //println("allow_root:"+allow_rootNiv); }
                         *
                         * if (contador==15) { if (tok.equals("true"))
                         * allow_reduceNiv=true; else allow_reduceNiv=false;
                         * //println("allow_reduce:"+allow_reduceNiv);
							}
                         */
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        BufferedReader brOpt;
        try {
            brOpt = new BufferedReader(new FileReader("phase2_optFile.txt"));
            try {
                int contador = 0;
                boolean grl;
                boolean pcr;
                boolean algo;
                boolean pp;
                boolean cs;
                boolean cr;
                boolean menosr;
                boolean nr;
                boolean ne = false;
                while (brOpt.ready()) {
                    String line;
                    try {
                        line = brOpt.readLine();
                        StringTokenizer st = new StringTokenizer(line, ":");
                        grl = false;
                        pcr = false;
                        algo = false;
                        pp = false;
                        cr = false;
                        cs = false;
                        menosr = false;
                        nr = false;
                        if (line.contains("-grl")) {
                            grl = true;
                        }
                        if (line.contains("-pcr")) {
                            pcr = true;
                        }
                        if (line.contains("-a")) {
                            algo = true;
                        }
                        if (line.contains("-pp")) {
                            pp = true;
                        }
                        if (line.contains("-cs")) {
                            cs = true;
                        }
                        if (line.contains("-cr")) {
                            cr = true;
                        }
                        if (line.contains("-r")) {
                            menosr = true;
                        }
                        if (line.contains("-nr")) {
                            nr = true;
                        }
                        if (line.contains("-ne")) {
                            ne = true;
                        }
                        String tok = "";
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        }
                        contador++;


                        if (grl) {
                            Optimizer.setOptionGRL(tok);
                            grl = false;
                        }
                        if (pcr) {
                            Optimizer.setPcrOption(tok);
                            pcr = false;
                        }
                        if (algo) {
                            this.setBestAlgorithm(tok);
                            algo = false;
                        }
                        if (pp) {
                            Optimizer.usePPOption = true;
                            Optimizer.setPpOption(tok);
                            //println(this.getPpOption());
                            pp = false;
                        }
                        if (cs) {
                            allow_shift = true;
                            cs = false;
                        }
                        if (cr) {
                            allow_root = true;
                            cr = false;
                        }
                        if (menosr) {
                            optionMenosR = tok;
                            menosr = false;
                        }
                        if (nr) {
                            Optimizer.allow_rootNiv = Boolean.parseBoolean(tok);
                            menosr = false;
                        }
                        if (ne) {
                            //optionMenosR=tok;
                            Optimizer.allow_reduceNiv = Boolean.parseBoolean(tok);
                            menosr = false;
                        }
                        //println(allow_rootNiv);
                        //println(allow_reduceNiv);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private void runPhase3() {

        ShowIntroduction();

        println("PHASE 3: FEATURE SELECTION\n");

        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        for (int i = 1; i < 6; i++) {
            new File ("fold_train_" + i + ".conll").delete();
        }
        ch.generateDivision8020();

        ShowPhase3Introduction();
        
        // Create default model if not exist
        String defaultModel = featureModel;
        File feat = new File(defaultModel);
        if (!feat.exists()) {
            BufferedWriter bwGuides;
            try {
                bwGuides = new BufferedWriter(new FileWriter(defaultModel));
                bwGuides.write(GuidesGenerator.generateGuides(defaultModel));
                bwGuides.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        println("1. Tuning the window of POSTAG n-grams ... \n");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM features ... \n");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("3. Tuning dependency tree features ... \n");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("4. Adding string features ... \n");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        if (!cposEqPos || !lemmaBlank || !featsBlank) {
            println("5. Adding CPOSTAG, FEATS, and LEMMA features ... ");
            if (!cposEqPos) {
                //println("CPostag and Postag are distinct in your corpus.");
                //println("Adding CPOSTAG Features ... ");
                addNewFeaturesCpostagFeatsLemma("CPOSTAG");
            }

            if (!lemmaBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Lemma column is used in your training set.");
                //println("Adding LEMMA Features ... ");
                addNewFeaturesCpostagFeatsLemma("LEMMA");
            }
            if (!featsBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Feats column is used in your training set.");
                //println("Adding FEATS Features ... ");
                addSplitFeaturesFeats("FEATS");

            }

            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
        }


        println("6. Adding conjunctions of POSTAG and FORM features... \n");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has concluded feature selection and is going to tune the SVM cost parameter.\n");
        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);
        
        // delete unused file
        if (!defaultModel.equals(featureModel)) {
            feat = new File(defaultModel);
            if (feat.exists()) {
               feat.delete();
            }
        }




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String s = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 3: + " + sDifferenceLabel + "% (" + s + ")");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt.");
        //println("To proceed with Phase 4 (Library Parameters) run the following command:");
        //println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
        if (ExitInEnd != true)
            println("END");
    }

    private void runPhase35Fold() {

        ShowIntroduction();

        println("PHASE 3: FEATURE SELECTION - 5-FOLD CROSS-VALIDATION\n");

        CoNLLHandler ch = new CoNLLHandler(this.trainingCorpus, writer);
        new File (ch.training80).delete();
        new File (ch.testing20).delete();
        ch.generate5FoldCrossCorporaPseudo();
        
        ShowPhase3Introduction();

        ch.generate5FoldCrossCorpora();
        
        // Create default model if not exist
        String defaultModel = featureModel;
        File feat = new File(defaultModel);
        if (!feat.exists()) {
            BufferedWriter bwGuides;
            try {
                bwGuides = new BufferedWriter(new FileWriter(defaultModel));
                bwGuides.write(GuidesGenerator.generateGuides(defaultModel));
                bwGuides.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        println("1. Tuning the window of POSTAG n-grams ... \n");
        //postagTuning();
        postagTuning5Fold();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM features ... \n");
        formTuning5Fold();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("3. Tuning dependency tree features ... \n");
        deprelTuning5Fold();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("4. Adding string features ... \n");
        predeccessorSuccessor5Fold("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        if (!cposEqPos || !lemmaBlank || !featsBlank) {
            println("5. Adding CPOSTAG, FEATS, and LEMMA features ... ");
            if (!cposEqPos) {
                //println("CPostag and Postag are distinct in your corpus.");
                //println("Adding CPOSTAG Features ... ");
                addNewFeaturesCpostagFeatsLemma5Fold("CPOSTAG");
            }

            if (!lemmaBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Lemma column is used in your training set.");
                //println("Adding LEMMA Features ... ");
                addNewFeaturesCpostagFeatsLemma5Fold("LEMMA");
            }
            if (!featsBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Feats column is used in your training set.");
                //println("Adding FEATS Features ... ");
                addSplitFeaturesFeats5Fold("FEATS");

            }

            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
        }


        println("6. Adding conjunctions of POSTAG and FORM features... \n");
        addConjunctionFeatures5Fold("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has concluded feature selection and is going to tune the SVM cost parameter.\n");
        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String s = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 3: + " + sDifferenceLabel + "% (" + s + ")");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt.");
        println("");
        //println("To proceed with Phase 4 (Library Parameters) run the following command:");
        //println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3AlternativeOrder1() {

        ShowIntroduction();

        println("PHASE 3: FEATURE MODEL TUNING. ALTERNATIVE ORDER 1");

        ShowPhase3Introduction();

        println("6. Adding conjunctions of POSTAG and FORM features. ... ");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("5. Add CPOSTAG, FEATS,  and LEMMA features if available. ... ");
        if (!cposEqPos) {
            println("CPostag and Postag are distinct in your corpus.");
            println("Adding Cpostag Features ...");
            addNewFeaturesCpostagFeatsLemma("CPOSTAG");
        }

        if (!lemmaBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Lemma column is used in your training set.");
            println("Adding Lemma Features ...");
            addNewFeaturesCpostagFeatsLemma("LEMMA");
        }
        if (!featsBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Feats column is used in your training set.");
            println("Adding Feats Features ...");
            addSplitFeaturesFeats("FEATS");

        }

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("4. Adding predecessor and successor features using POSTAG and FORM features ... ");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("3. Tuning dependency tree features (DEPREL) ... ");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM ... ");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("1. Tuning the window of POSTAG ... ");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        println("At the end of Phase 3 MaltOptimizer achieves an increment of: + " + sDifferenceLabel + "% \ncompared with the default settings baseline.");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt. If you want to change any of these options");
        println("you should edit phase3_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 4 (Library Parameters) run the following command:");
        println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3AlternativeOrder2() {

        ShowIntroduction();

        println("PHASE 3: FEATURE MODEL TUNING. ALTERNATIVE ORDER 2");

        ShowPhase3Introduction();

        println("4. Adding predecessor and successor features using POSTAG and FORM features ... ");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("5. Add CPOSTAG, FEATS,  and LEMMA features if available. ... ");
        if (!cposEqPos) {
            println("CPostag and Postag are distinct in your corpus.");
            println("Adding Cpostag Features ...");
            addNewFeaturesCpostagFeatsLemma("CPOSTAG");
        }

        if (!lemmaBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Lemma column is used in your training set.");
            println("Adding Lemma Features ...");
            addNewFeaturesCpostagFeatsLemma("LEMMA");
        }
        if (!featsBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Feats column is used in your training set.");
            println("Adding Feats Features ...");
            addSplitFeaturesFeats("FEATS");
        }

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("6. Adding conjunctions of POSTAG and FORM features. ... ");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("1. Tuning the window of POSTAG ... ");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM ... ");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("3. Tuning dependency tree features (DEPREL) ... ");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        println("At the end of Phase 3 MaltOptimizer achieves an increment of: + " + sDifferenceLabel + "% \ncompared with the default settings baseline.");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt. If you want to change any of these options");
        println("you should edit phase3_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 4 (Library Parameters) run the following command:");
        println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3AlternativeOrder3() {

        ShowIntroduction();

        println("PHASE 3: FEATURE MODEL TUNING. ALTERNATIVE ORDER 3");

        ShowPhase3Introduction();

        println("1. Tuning the window of POSTAG ... ");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("3. Tuning dependency tree features (DEPREL) ... ");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM ... ");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("5. Add CPOSTAG, FEATS,  and LEMMA features if available. ... ");
        if (!cposEqPos) {
            println("CPostag and Postag are distinct in your corpus.");
            println("Adding Cpostag Features ...");
            addNewFeaturesCpostagFeatsLemma("CPOSTAG");
        }

        if (!lemmaBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Lemma column is used in your training set.");
            println("Adding Lemma Features ...");
            addNewFeaturesCpostagFeatsLemma("LEMMA");
        }
        if (!featsBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Feats column is used in your training set.");
            println("Adding Feats Features ...");
            addSplitFeaturesFeats("FEATS");

        }

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("4. Adding predecessor and successor features using POSTAG and FORM features ... ");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");




        println("6. Adding conjunctions of POSTAG and FORM features. ... ");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        println("At the end of Phase 3 MaltOptimizer achieves an increment of: + " + sDifferenceLabel + "% \ncompared with the default settings baseline.");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt. If you want to change any of these options");
        println("you should edit phase3_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 4 (Library Parameters) run the following command:");
        println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3AlternativeOrder4() {

        ShowIntroduction();

        println("PHASE 3: FEATURE MODEL TUNING. ALTERNATIVE ORDER 4");

        ShowPhase3Introduction();

        println("6. Adding conjunctions of POSTAG and FORM features. ... ");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("1. Tuning the window of POSTAG ... ");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("5. Add CPOSTAG, FEATS,  and LEMMA features if available. ... ");
        if (!cposEqPos) {
            println("CPostag and Postag are distinct in your corpus.");
            println("Adding Cpostag Features ...");
            addNewFeaturesCpostagFeatsLemma("CPOSTAG");
        }

        if (!lemmaBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Lemma column is used in your training set.");
            println("Adding Lemma Features ...");
            addNewFeaturesCpostagFeatsLemma("LEMMA");
        }
        if (!featsBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Feats column is used in your training set.");
            println("Adding Feats Features ...");
            addSplitFeaturesFeats("FEATS");

        }

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("4. Adding predecessor and successor features using POSTAG and FORM features ... ");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("3. Tuning dependency tree features (DEPREL) ... ");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM ... ");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");







        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        println("At the end of Phase 3 MaltOptimizer achieves an increment of: + " + sDifferenceLabel + "% \ncompared with the default settings baseline.");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt. If you want to change any of these options");
        println("you should edit phase3_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 4 (Library Parameters) run the following command:");
        println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3AlternativeOrder5() {

        ShowIntroduction();

        println("PHASE 3: FEATURE MODEL TUNING. ALTERNATIVE ORDER 5");

        ShowPhase3Introduction();

        println("5. Add CPOSTAG, FEATS,  and LEMMA features if available. ... ");
        if (!cposEqPos) {
            println("CPostag and Postag are distinct in your corpus.");
            println("Adding Cpostag Features ...");
            addNewFeaturesCpostagFeatsLemma("CPOSTAG");
        }

        if (!lemmaBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Lemma column is used in your training set.");
            println("Adding Lemma Features ...");
            addNewFeaturesCpostagFeatsLemma("LEMMA");
        }
        if (!featsBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Feats column is used in your training set.");
            println("Adding Feats Features ...");
            addSplitFeaturesFeats("FEATS");
        }

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("6. Adding conjunctions of POSTAG and FORM features. ... ");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("4. Adding predecessor and successor features using POSTAG and FORM features ... ");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("3. Tuning dependency tree features (DEPREL) ... ");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("1. Tuning the window of POSTAG ... ");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM ... ");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");



        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        println("At the end of Phase 3 MaltOptimizer achieves an increment of: + " + sDifferenceLabel + "% \ncompared with the default settings baseline.");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt. If you want to change any of these options");
        println("you should edit phase3_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 4 (Library Parameters) run the following command:");
        println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3AlternativeOrder6() {

        ShowIntroduction();

        println("PHASE 3: FEATURE MODEL TUNING. ALTERNATIVE ORDER 6");

        ShowPhase3Introduction();

        println("2. Tuning the window of FORM ... ");
        formTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("6. Adding conjunctions of POSTAG and FORM features. ... ");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");



        println("5. Add CPOSTAG, FEATS,  and LEMMA features if available. ... ");
        if (!cposEqPos) {
            println("CPostag and Postag are distinct in your corpus.");
            println("Adding Cpostag Features ...");
            addNewFeaturesCpostagFeatsLemma("CPOSTAG");
        }

        if (!lemmaBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Lemma column is used in your training set.");
            println("Adding Lemma Features ...");
            addNewFeaturesCpostagFeatsLemma("LEMMA");
        }
        if (!featsBlank) {
            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
            println("Feats column is used in your training set.");
            println("Adding Feats Features ...");
            addSplitFeaturesFeats("FEATS");

        }

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("4. Adding predecessor and successor features using POSTAG and FORM features ... ");
        predeccessorSuccessor("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("3. Tuning dependency tree features (DEPREL) ... ");
        deprelTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("1. Tuning the window of POSTAG ... ");
        postagTuning();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        println("At the end of Phase 3 MaltOptimizer achieves an increment of: + " + sDifferenceLabel + "% \ncompared with the default settings baseline.");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt. If you want to change any of these options");
        println("you should edit phase3_optFile.txt before you start the next optimization phase.");
        println("");
        println("To proceed with Phase 4 (Library Parameters) run the following command:");
        println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3BruteForce() {

        ShowIntroduction();

        println("PHASE 3: FEATURE SELECTION\n");

        ShowPhase3Introduction();

        FeatureGenerator fg = new FeatureGenerator(writer);
        fg.emptyFeatureModel(featureModel, featureModelBruteForce);
        //featureModelBruteForce=featureModel;

        println("1. Tuning the window of POSTAG n-grams ... \n");
        postagTuningBruteForce();
        println("\nBest feature model Bf: " + featureModelBruteForce);
        println("-----------------------------------------------------------------------------");
        featureModel = featureModelBruteForce;

        println("2 (3). Tuning dependency tree features ... \n");
        deprelTuningBruteForce();
        println("\nBest feature model: " + featureModelBruteForce);
        println("-----------------------------------------------------------------------------");
        featureModel = featureModelBruteForce;

        println("3 (2). Tuning the window of FORM features ... \n");
        formTuningBruteForce();
        println("\nBest feature model: " + featureModelBruteForce);
        println("-----------------------------------------------------------------------------");
        featureModel = featureModelBruteForce;

        println("R1 (2). Reviewing some features ... \n");
        backTrackingAfter3();
        println("\nBest feature model: " + featureModelBruteForce);
        println("-----------------------------------------------------------------------------");
        featureModel = featureModelBruteForce;

        println("R1 (1). Reviewing the window of POSTAG n-grams ... \n");
        this.postagTuning();
        println("-----------------------------------------------------------------------------");
        println("R2 (2). Reviewing the window of FORM features ... \n");
        this.formTuning();
        println("-----------------------------------------------------------------------------");
        println("R3 (3). Reviewing dependency tree features ... \n");
        this.deprelTuning();
        println("-----------------------------------------------------------------------------");

        featureModelBruteForce = featureModel;

        println("4. Adding string features ... \n");
        predeccessorSuccessorBruteForce("POSTAG");
        println("\nBest feature model: " + featureModelBruteForce);
        println("-----------------------------------------------------------------------------");

        if (!cposEqPos || !lemmaBlank || !featsBlank) {
            println("5. Adding CPOSTAG, FEATS, and LEMMA features ... ");
            if (!cposEqPos) {
                //println("CPostag and Postag are distinct in your corpus.");
                //println("Adding CPOSTAG Features ... ");
                addNewFeaturesCpostagFeatsLemma("CPOSTAG");
            }

            if (!lemmaBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Lemma column is used in your training set.");
                //println("Adding LEMMA Features ... ");
                addNewFeaturesCpostagFeatsLemma("LEMMA");
            }
            if (!featsBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Feats column is used in your training set.");
                //println("Adding FEATS Features ... ");
                addSplitFeaturesFeats("FEATS");
            }

            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
        }


        println("6. Adding conjunctions of POSTAG and FORM features... \n");
        addConjunctionFeatures("POSTAG", "FORM");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has concluded feature selection and is going to tune the SVM cost parameter.\n");
        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String s = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 3: + " + sDifferenceLabel + "% (" + s + ")");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt.");
        println("");
        //println("To proceed with Phase 4 (Library Parameters) run the following command:");
        //println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");

    }

    private void runPhase3RelaxedGreedy() {

        ShowIntroduction();

        println("PHASE 3: FEATURE SELECTION DEEP GREEDY\n");

        ShowPhase3Introduction();

        FeatureGenerator fg = new FeatureGenerator(writer);
        fg.emptyFeatureModel(featureModel, featureModelBruteForce);

        println("1. Tuning the window of POSTAG n-grams ... \n");
        postagTuningRelaxedGreedy();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM features ... \n");
        formTuningRelaxedGreedy();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("3. Tuning dependency tree features ... \n");
        deprelTuningRelaxedGreedy();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("4. Adding string features ... \n");
        predeccessorSuccessorRelaxedGreedy("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        if (!cposEqPos || !lemmaBlank || !featsBlank) {
            println("5. Adding CPOSTAG, FEATS, and LEMMA features ... ");
            if (!cposEqPos) {
                //println("CPostag and Postag are distinct in your corpus.");
                //println("Adding CPOSTAG Features ... ");
                addNewFeaturesCpostagFeatsLemmaRelaxedGreedy("CPOSTAG");
                predeccessorSuccessorRelaxedGreedy("CPOSTAG");
            }

            if (!lemmaBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Lemma column is used in your training set.");
                //println("Adding LEMMA Features ... ");
                addNewFeaturesCpostagFeatsLemmaRelaxedGreedy("LEMMA");
                predeccessorSuccessorRelaxedGreedy("LEMMA");
            }
            if (!featsBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Feats column is used in your training set.");
                //println("Adding FEATS Features ... ");
                addSplitFeaturesFeatsRelaxedGreedy("FEATS");

            }

            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
        }


        println("6. Adding conjunctions of POSTAG and FORM features... \n");
        addConjunctionFeatures("POSTAG", "FORM");
        addConjunctionFeatures("POSTAG", "LEMMA");
        addConjunctionFeatures("CPOSTAG", "LEMMA");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has concluded feature selection and is going to tune the SVM cost parameter.\n");
        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String s = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 3: + " + sDifferenceLabel + "% (" + s + ")");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt.");
        println("");
        //println("To proceed with Phase 4 (Library Parameters) run the following command:");
        //println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3OnlyBackward(String featureModelSoFar) {

        ShowIntroduction();

        println("PHASE 3: FEATURE SELECTION ONLY BACKWARD SELECTION\n");

        ShowPhase3Introduction();
        
        if (!featureModelSoFar.equals("featureModel")) {
            if (!featureModelSoFar.equals("")) {
                featureModel = featureModelSoFar;
            }
        }

        FeatureGenerator fg = new FeatureGenerator(writer);

        println("1. Tuning the window of POSTAG n-grams ... \n");
        postagTuningOnlyBackward();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM features ... \n");
        formTuningOnlyBackward();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("3. Tuning dependency tree features ... \n");
        deprelTuningOnlyBackward();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        /*
         * println("4. Adding string features ... \n");
         * predeccessorSuccessorOnlyBackward("POSTAG");
         *
         * println("\nBest feature model: "+featureModel);
         * println("-----------------------------------------------------------------------------");
         *
         * /*if (!cposEqPos || !lemmaBlank || !featsBlank) {
         * println("5. Adding CPOSTAG, FEATS, and LEMMA features ...
         * "); if (!cposEqPos) { //println("CPostag and Postag are
         * distinct in your corpus."); //println("Adding CPOSTAG
         * Features ... ");
         * addNewFeaturesCpostagFeatsLemmaOnlyBackward("CPOSTAG");
         * //predeccessorSuccessorRelaxedGreedy("CPOSTAG"); }
         *
         * if (!lemmaBlank) { //println("\nBest feature model:
         * "+featureModel);
         * //println("-----------------------------------------------------------------------------");
         * //println("Lemma column is used in your training set.");
         * //println("Adding LEMMA Features ... ");
         * addNewFeaturesCpostagFeatsLemmaOnlyBackward("LEMMA");
         * predeccessorSuccessorOnlyBackward("LEMMA"); } if (!featsBlank) {
         * //println("\nBest feature model: "+featureModel);
         * //println("-----------------------------------------------------------------------------");
         * //println("Feats column is used in your training set.");
         * //println("Adding FEATS Features ... ");
         * addSplitFeaturesFeatsOnlyBackward("FEATS");
         *
         * }
         *
         * println("\nBest feature model: "+featureModel);
         * println("-----------------------------------------------------------------------------");
         * }
         *
         *
         * /*println("6. Adding conjunctions of POSTAG and FORM
         * features... \n"); addConjunctionFeatures("POSTAG","FORM");
         * addConjunctionFeatures("POSTAG","LEMMA");
         * addConjunctionFeatures("CPOSTAG","LEMMA");
         *
         * println("\nBest feature model: "+featureModel);
		println("-----------------------------------------------------------------------------");
         */
        println("MaltOptimizer has concluded feature selection and is going to tune the SVM cost parameter.\n");
        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String s = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 3: + " + sDifferenceLabel + "% (" + s + ")");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt.");
        println("");
        //println("To proceed with Phase 4 (Library Parameters) run the following command:");
        //println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void runPhase3OnlyForward(String featureModelSoFar) {

        ShowIntroduction();

        println("PHASE 3: FEATURE SELECTION ONLY BACKWARD SELECTION\n");

        ShowPhase3Introduction();
        
        if (!featureModelSoFar.equals("featureModel")) {
            if (!featureModelSoFar.equals("")) {
                featureModel = featureModelSoFar;
            }
        }

        FeatureGenerator fg = new FeatureGenerator(writer);

        println("1. Tuning the window of POSTAG n-grams ... \n");
        postagTuningOnlyForward();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        println("2. Tuning the window of FORM features ... \n");
        formTuningOnlyForward();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("3. Tuning dependency tree features ... \n");
        deprelTuningOnlyForward();
        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");


        println("4. Adding string features ... \n");
        predeccessorSuccessorRelaxedGreedy("POSTAG");

        println("\nBest feature model: " + featureModel);
        println("-----------------------------------------------------------------------------");

        if (!cposEqPos || !lemmaBlank || !featsBlank) {
            println("5. Adding CPOSTAG, FEATS, and LEMMA features ... ");
            if (!cposEqPos) {
                //println("CPostag and Postag are distinct in your corpus.");
                //println("Adding CPOSTAG Features ... ");
                addNewFeaturesCpostagFeatsLemmaRelaxedGreedy("CPOSTAG");
                predeccessorSuccessorRelaxedGreedy("CPOSTAG");
            }

            if (!lemmaBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Lemma column is used in your training set.");
                //println("Adding LEMMA Features ... ");
                addNewFeaturesCpostagFeatsLemmaRelaxedGreedy("LEMMA");
                predeccessorSuccessorRelaxedGreedy("LEMMA");
            }
            if (!featsBlank) {
                //println("\nBest feature model: "+featureModel);
                //println("-----------------------------------------------------------------------------");
                //println("Feats column is used in your training set.");
                //println("Adding FEATS Features ... ");
                addSplitFeaturesFeatsRelaxedGreedy("FEATS");

            }

            println("\nBest feature model: " + featureModel);
            println("-----------------------------------------------------------------------------");
        }


        /*
         * println("6. Adding conjunctions of POSTAG and FORM
         * features... \n"); addConjunctionFeatures("POSTAG","FORM");
         * addConjunctionFeatures("POSTAG","LEMMA");
         * addConjunctionFeatures("CPOSTAG","LEMMA");
         *
         * println("\nBest feature model: "+featureModel);
		println("-----------------------------------------------------------------------------");
         */
        println("MaltOptimizer has concluded feature selection and is going to tune the SVM cost parameter.\n");
        this.runPhase4SimplifiedVersion();

        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        //(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        OptionsGenerator og = new OptionsGenerator(language, this.trainingCorpus);
        String optionsCat = og.generateIncOptionsTestingsPhases(language, Optimizer.bestAlgorithm, this.trainingCorpus, Optimizer.optionMenosR, Optimizer.libraryValue, Optimizer.optionGRL, Optimizer.pcrOption);
        String optionsNivreEager = "finalOptionsFile.xml";
        BufferedWriter bwOptionsNivreEager;
        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createLogFile(3);
        createOptionFile(3);




        Double diff = bestResult - defaultBaseline;
        String sDifferenceLabel = String.format(pattern, diff);
        String s = String.format(pattern, bestResult);
        println("Incremental improvement over the baseline at the end of Phase 3: + " + sDifferenceLabel + "% (" + s + ")");



        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the feature model testing phase using your training set,");
        println("it saved the results for future use in phase3_logFile.txt. Updated MaltParser ");
        println("options can be found in phase3_optFile.txt.");
        println("");
        //println("To proceed with Phase 4 (Library Parameters) run the following command:");
        //println("java -jar MaltOptimizer.jar -p 4 -m <malt_path> -c <trainingCorpus>");
    }

    private void postagTuning() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        newFeature = "backwardStackPostag.xml";
        // rm InputColumn(POSTAG, Left[1])                  [covingtion]
        fg.removeStackWindow(featureModel, newFeature, "POSTAG");
        result = runBestAlgorithm(newFeature);
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardStackPostag" + i + ".xml";
                    // add InputColumn(POSTAG, Left[i])     [covingtion]
                    fg.addStackWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////

        newFeature = "backwardInputPostag.xml";
        // rm InputColumn(POSTAG,Right[3])                  [covingtion]
        fg.removeInputWindow(featureModel, newFeature, "POSTAG", InputLookAhead);
        result = runBestAlgorithm(newFeature);
        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardInputPostag" + i + ".xml";
                    // add InputColumn(POSTAG, Right[i])    [covingtion]
                    fg.addInputWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }

        String anterior;
        boolean keepGoing;
        switch (bestAlgorithm) {
            case "stackeager":
            case "stacklazy":
                newFeature = "backwardINPUT.xml";
                fg.removeInputWindowSpecial(featureModel, newFeature, "POSTAG");
                result = runBestAlgorithm(newFeature);
                boolean input0Esta = true;

                res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
                if (res == true) { 
                    input0Esta = false;
                } else { //Expanding
                    anterior = featureModel;
                    keepGoing = true;
                    for (int i = 1; i < 3; i++) {
                        if (keepGoing) {
                            newFeature = "forwardINPUT" + i + ".xml";
                            fg.addInputWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                            anterior = newFeature;
                            result = runBestAlgorithm(newFeature);
                            keepGoing = setBestResult(result, newFeature);  //Shrinking
                        }
                    }
                }
                if (input0Esta) {
                    //add trigram stack[0], input[0], lookahead[0]
                    newFeature = "specialTrigramINPUT.xml";
                    fg.addMergeFeaturesMerge3SpecialCase(featureModel, newFeature, "POSTAG", 0, "InputColumn");
                    result = runBestAlgorithm(newFeature);
                    setBestResult(result, newFeature); //Shrinking
                }
                break;
            case "covnonproj":
                newFeature = "backwardLeftContext1.xml";
                fg.removeLeftContextWindowSpecial(featureModel, newFeature, "POSTAG");
                result = runBestAlgorithm(newFeature);
                res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
                if (res == false) { //Expanding
                    anterior = featureModel;
                    keepGoing = true;
                    for (int i = 1; i < 3; i++) {
                        if (keepGoing) {
                            newFeature = "forwardRightContext" + i + ".xml";
                            fg.addLeftContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                            anterior = newFeature;
                            result = runBestAlgorithm(newFeature);
                            keepGoing = setBestResult(result, newFeature); //Shrinking
                        }
                    }
                }

                newFeature = "backwardLeftContext2.xml";
                fg.removeRightContextWindowSpecial(featureModel, newFeature, "POSTAG");
                result = runBestAlgorithm(newFeature);
                res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
                if (res == false) { //Expanding
                    anterior = featureModel;
                    keepGoing = true;
                    for (int i = 1; i < 3; i++) {
                        if (keepGoing) {
                            newFeature = "forwardRightContext" + i + ".xml";
                            fg.addRightContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                            anterior = newFeature;
                            result = runBestAlgorithm(newFeature);
                            keepGoing = setBestResult(result, newFeature); //Shrinking
                        }
                    }
                }
                break;
        }
    }

    private void postagTuning5Fold() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        newFeature = "backwardStackPostag.xml";
        fg.removeStackWindow(featureModel, newFeature, "POSTAG");
        result = runBestAlgorithm5Fold(newFeature);
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardStackPostag" + i + ".xml";
                    fg.addStackWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm5Fold(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////

        newFeature = "backwardInputPostag.xml";
        fg.removeInputWindow(featureModel, newFeature, "POSTAG", InputLookAhead);
        result = runBestAlgorithm5Fold(newFeature);
        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardInputPostag" + i + ".xml";
                    fg.addInputWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm5Fold(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }

        String anterior;
        boolean keepGoing;
        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            //REMOVE INPUT[0] (no lookahead) (shrinking)
            //si no mejora al quitarlo add Input[1], Input[2].
            //si se mantuvo INPUT[0] add trigram Stack[0], Input[0], LookAhead[0]
            newFeature = "backwardINPUT.xml";
            fg.removeInputWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm5Fold(newFeature);
            boolean input0Esta = true;
            res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            if (res == true) { 
                input0Esta = false;
            } else { //Expanding
                anterior = featureModel;
                keepGoing = true;
                for (int i = 1; i < 3; i++) {
                    if (keepGoing) {
                        newFeature = "forwardINPUT" + i + ".xml";
                        fg.addInputWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                        anterior = newFeature;
                        result = runBestAlgorithm5Fold(newFeature);
                        keepGoing = setBestResult(result, newFeature); //Shrinking
                    }
                }
            }
            if (input0Esta) {
                //add trigram stack[0], input[0], lookahead[0]
                newFeature = "specialTrigramINPUT.xml";
                fg.addMergeFeaturesMerge3SpecialCase(featureModel, newFeature, "POSTAG", 0, "InputColumn");
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        if (bestAlgorithm.equals("covnonproj")) {

            newFeature = "backwardLeftContext3.xml";
            fg.removeLeftContextWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm5Fold(newFeature);

            res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            if (res == false) { //Expanding
                anterior = featureModel;
                keepGoing = true;
                for (int i = 1; i < 3; i++) {
                    if (keepGoing) {
                        newFeature = "forwardRightContext" + i + ".xml";
                        fg.addLeftContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                        anterior = newFeature;
                        result = runBestAlgorithm5Fold(newFeature);
                        keepGoing = setBestResult(result, newFeature); //Shrinking
                    }
                }
            }


            newFeature = "backwardLeftContext4.xml";
            fg.removeRightContextWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm5Fold(newFeature);
            res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            if (res == false) { //Expanding
                anterior = featureModel;
                keepGoing = true;
                for (int i = 1; i < 3; i++) {
                    if (keepGoing) {
                        newFeature = "forwardRightContext" + i + ".xml";
                        fg.addRightContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                        anterior = newFeature;
                        result = runBestAlgorithm5Fold(newFeature);
                        keepGoing = setBestResult(result, newFeature); //Shrinking
                    }
                }
            }
        }
    }

    private void postagTuningRelaxedGreedy() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        /*
         * String newFeature2="prueba.xml";
		fg.addStackWindow(featureModel,newFeature2,"POSTAG",InputLookAhead);
         */

        String oldFeatureModel = featureModel;
        String newFeature = "backwardStack.xml";
        fg.removeStackWindow(featureModel, newFeature, "POSTAG");
        double result = runBestAlgorithm(newFeature);
        
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardStackTwice.xml";
            fg.removeStackWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardStackTwice.xml";
            fg.removeStackWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }

        //Expanding
        String anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardStack" + i + ".xml";
            fg.addStackWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature);
        }

        /*
         * String newFeature2="prueba.xml";
			fg.addStackWindow(featureModel,newFeature2,"POSTAG",InputLookAhead);
         */
        oldFeatureModel = featureModel;
        newFeature = "backwardInput.xml";
        fg.removeInputWindow(featureModel, newFeature, "POSTAG");
        result = runBestAlgorithm(newFeature);

        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardInputTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardInputTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }

        //Expanding
        anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardInput" + i + ".xml";
            fg.addInputWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature);
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            //REMOVE INPUT[0] (no lookahead) (shrinking)
            //si no mejora al quitarlo add Input[1], Input[2].
            //si se mantuvo INPUT[0] add trigram Stack[0], Input[0], LookAhead[0]
            oldFeatureModel = featureModel;
            newFeature = "backwardINPUT.xml";
            fg.removeInputWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm(newFeature);
            boolean input0Esta = true;

            res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            if (res == true) { 
                input0Esta = false;
            }
            anterior = oldFeatureModel;
            for (int i = 1; i < 3; i++) {
                newFeature = "forwardINPUT" + i + ".xml";
                fg.addInputWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }

            if (input0Esta) {
                //add trigram stack[0], input[0], lookahead[0]
                newFeature = "specialTrigramINPUT.xml";
                fg.addMergeFeaturesMerge3SpecialCase(featureModel, newFeature, "POSTAG", 0, "InputColumn");
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        if (bestAlgorithm.equals("covnonproj")) {
            oldFeatureModel = featureModel;
            newFeature = "backwardLeftContext5.xml";
            fg.removeLeftContextWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            anterior = oldFeatureModel;
            for (int i = 1; i < 3; i++) {
                newFeature = "forwardRightContext" + i + ".xml";
                fg.addLeftContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }

            oldFeatureModel = featureModel;
            newFeature = "backwardLeftContext6.xml";
            fg.removeRightContextWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm(newFeature);

            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            anterior = oldFeatureModel;
            for (int i = 1; i < 3; i++) {
                newFeature = "forwardRightContext" + i + ".xml";
                fg.addRightContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void postagTuningOnlyBackward() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        /*
         * String newFeature2="prueba.xml";
		fg.addStackWindow(featureModel,newFeature2,"POSTAG",InputLookAhead);
         */

        String oldFeatureModel = featureModel;
        String newFeature = "backwardStack.xml";

        fg.removeStackWindow(featureModel, newFeature, "POSTAG");
        double result = runBestAlgorithm(newFeature);

        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardStackTwice.xml";
            fg.removeStackWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardStackTwice.xml";
            fg.removeStackWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }


        oldFeatureModel = featureModel;
        newFeature = "backwardInput.xml";
        fg.removeInputWindow(featureModel, newFeature, "POSTAG");
        result = runBestAlgorithm(newFeature);

        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardInputTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardInputTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "POSTAG");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }



        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            //REMOVE INPUT[0] (no lookahead) (shrinking)
            //si no mejora al quitarlo add Input[1], Input[2].
            //si se mantuvo INPUT[0] add trigram Stack[0], Input[0], LookAhead[0]
            oldFeatureModel = featureModel;
            newFeature = "backwardINPUT.xml";
            fg.removeInputWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }



        if (bestAlgorithm.equals("covnonproj")) {
            oldFeatureModel = featureModel;
            newFeature = "backwardLeftContext7.xml";
            fg.removeLeftContextWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            
            
            oldFeatureModel = featureModel;
            newFeature = "backwardLeftContext8.xml";
            fg.removeRightContextWindowSpecial(featureModel, newFeature, "POSTAG");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }
    }

    private void postagTuningOnlyForward() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        /*
         * String newFeature2="prueba.xml";
		fg.addStackWindow(featureModel,newFeature2,"POSTAG",InputLookAhead);
         */

        String oldFeatureModel = featureModel;
        String newFeature;
        double result;


        //Expanding
        String anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardStack" + i + ".xml";
            fg.addStackWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature);
        }



        //Expanding
        anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardInput" + i + ".xml";
            fg.addInputWindow(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature);
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            //REMOVE INPUT[0] (no lookahead) (shrinking)
            //si no mejora al quitarlo add Input[1], Input[2].
            //si se mantuvo INPUT[0] add trigram Stack[0], Input[0], LookAhead[0]
            boolean input0Esta = false;
            anterior = oldFeatureModel;
            for (int i = 1; i < 3; i++) {
                newFeature = "forwardINPUT" + i + ".xml";
                fg.addInputWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                boolean res = setBestResult(result, newFeature);
                if (res == true && i == 1) {
                    input0Esta = true;
                }
            }

            if (input0Esta) {
                //add trigram stack[0], input[0], lookahead[0]
                newFeature = "specialTrigramINPUT.xml";
                fg.addMergeFeaturesMerge3SpecialCase(featureModel, newFeature, "POSTAG", 0, "InputColumn");
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        if (bestAlgorithm.equals("covnonproj")) {
            oldFeatureModel = featureModel;
            anterior = oldFeatureModel;
            for (int i = 1; i < 3; i++) {
                newFeature = "forwardRightContext" + i + ".xml";
                fg.addLeftContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }

            oldFeatureModel = featureModel;
            anterior = oldFeatureModel;
            for (int i = 1; i < 3; i++) {
                newFeature = "forwardRightContext" + i + ".xml";
                fg.addRightContextWindowSpecialCase(anterior, newFeature, "POSTAG", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void postagTuningBruteForce() {

        FeatureGenerator fg = new FeatureGenerator(writer);

        /*
         * double result=runBestAlgorithm(featureModelBruteForce);
         * println(result); println("Best: "+bestResult);
         * //
         *
         */
        double result;
        String antFeature = featureModelBruteForce;
        double difference;
        /*
         * if (result>=(this.bestResultBruteForce)) { //Shrinking //NO THRESHOLD
         * because we are removing this.featureModelBruteForce=antFeature;
         * //dummy (just a matter of completity)
         * difference=result-bestResultBruteForce; bestResultBruteForce=result;
         * String sDifferenceLabel=""+difference; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * /*println("New best feature model:
         * "+featureModelBruteForce); String s=""+this.bestResult; if
         * (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         */

        //}
        //ADD FEATURES TO THE POOL. 
        //If we have 3...	
        //  {f1,f2,f3} => { {f1},{f2},{f3},{f1,f2},{f1,f3},{f2,f3},{f1,f2,f3} } 
        //
        ArrayList<String> pool = new ArrayList<>();
        String structure = "Stack";
        if (bestAlgorithm.contains("cov")) {
            structure = "Left";
        }

        String structureI = "Input";
        if (bestAlgorithm.contains("cov")) {
            structureI = "Right";
        }
        if (bestAlgorithm.contains("stack")) {
            structureI = "Lookahead";
        }
        //for(int i=0;i<6;i++){
        int i = 0;
        pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>");
        pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>");
        if (i < 5) {
            int j = i + 1;
            pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + j + "])</feature>");
            pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + j + "])</feature>");
            if (i < 4) {
                int k = j + 1;
                pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + k + "])</feature>");
                pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + k + "])</feature>");
                if (i < 3) {
                    int l = k + 1;
                    pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + k + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + l + "])</feature>");
                    pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + k + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + l + "])</feature>");
                    if (i < 2) {
                        int m = l + 1;
                        pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + k + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + l + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + m + "])</feature>");
                        pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + k + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + l + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + m + "])</feature>");
                        if (i < 1) {
                            int n = m + 1;
                            pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + k + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + l + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + m + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[" + n + "])</feature>");
                            pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + k + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + l + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + m + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + n + "])</feature>");
                        }
                    }
                }
            }
        }
        //}


        Iterator<String> it = pool.iterator();
        int a = 0;
        while (it.hasNext()) {
            String newFeature = "postag" + structure + structureI + a + ".xml";
            fg.addFeatureLine("bruteForce1.xml", newFeature, it.next());
            a++;
            antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);
            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */

            }
        }



        //ADD INPUT FEATURES
        //TEST WITH THE NEW FEATURES

        //ADD FEATURES TO THE POOL. 
        //If we have 3...	
        //  {f1,f2,f3} => { {f1},{f2},{f3},{f1,f2},{f1,f3},{f2,f3},{f1,f2,f3} } 
        //
		/*
         * pool = new ArrayList<String>(); structure="Input"; if
         * (bestAlgorithm.contains("cov")) structure="Right"; if
         * (bestAlgorithm.contains("stack")) structure="Lookahead"; //for(int
         * i=0;i<6;i++){ i=0; pool.add("\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+i+"])</feature>"); if (i<5) { int j=i+1;
         * pool.add("\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+i+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+j+"])</feature>"); if (i<4) { int k=j+1;
         * pool.add("\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+i+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+j+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+k+"])</feature>"); if (i<3) { int l=k+1;
         * pool.add("\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+i+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+j+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+k+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+l+"])</feature>"); if (i<2) { int m=l+1;
         * pool.add("\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+i+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+j+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+k+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+l+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+m+"])</feature>"); if (i<1) { int n=m+1;
         * pool.add("\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+i+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+j+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+k+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+l+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+m+"])</feature>\n\t\t<feature>InputColumn(POSTAG,
         * "+structure+"["+n+"])</feature>"); } } } } } //}
         *
         *
         * it=pool.iterator(); newFeature="postag"+structure; a=0; String
         * currentFeature=featureModelBruteForce; while(it.hasNext()){
         * newFeature="postag"+structure+a+".xml";
         * fg.addFeatureLine(currentFeature,newFeature,it.next()); a++;
         * antFeature=newFeature; println("Testing "+newFeature +"
         * ..."); result=runBestAlgorithm(newFeature); difference=0.0;
         * println(" "+result); println(" Default:
         * "+bestResult); if (result>=(this.bestResultBruteForce+threshold)) {
         * //Shrinking //NO THRESHOLD because we are removing
         * this.featureModelBruteForce=antFeature; //dummy (just a matter of
         * completity) difference=result-bestResultBruteForce;
         * bestResultBruteForce=result; String sDifferenceLabel=""+difference;
         * if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * /*println("New best feature model:
         * "+featureModelBruteForce); String s=""+this.bestResult; if
         * (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         */

        /*
         * }
         * }
         */
        String currentFeature = featureModelBruteForce;
        int maxS = fg.findMaxStack("POSTAG", currentFeature);
        int maxI = fg.findMaxInput("POSTAG", currentFeature, structureI);
        pool = new ArrayList<>();
        if ((maxS >= 0) && (maxI == -1)) {
            pool = new ArrayList<>();
            structure = "Input";
            if (bestAlgorithm.contains("cov")) {
                structure = "Right";
            }
            if (bestAlgorithm.contains("stack")) {
                structure = "Lookahead";
            }
            //for(int i=0;i<6;i++){
            i = 0;
            pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>");
            pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[1])</feature>");
            pool.add("\t\t<feature>InputColumn(POSTAG, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structureI + "[1])</feature>");
        } else {
            if ((maxS == -1) && (maxI >= 0)) {
                structure = "Stack";
                if (bestAlgorithm.contains("cov")) {
                    structure = "Left";
                }
                i = 0;
                pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>");
                pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[1])</feature>");
                pool.add("\t\t<feature>InputColumn(POSTAG, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(POSTAG, " + structure + "[1])</feature>");
            }
        }
        it = pool.iterator();
        a = 0;

        while (it.hasNext()) {
            String newFeature = "postagBackTracking" + a + ".xml";
            fg.addFeatureLine(currentFeature, newFeature, it.next());
            a++;
            antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);
            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */
            }
        }
        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {

            String concat = "";
            pool = new ArrayList<>();
            pool.add("\t\t<feature>InputColumn(POSTAG, Input[0])</feature>");
            concat += "\t\t<feature>InputColumn(POSTAG, Input[0])</feature>";
            pool.add("\t\t<feature>InputColumn(POSTAG, Input[1])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, Input[1])</feature>";
            pool.add(concat);
            pool.add("\t\t<feature>InputColumn(POSTAG, Input[2])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, Input[2])</feature>";
            pool.add(concat);
            pool.add("\t\t<feature>InputColumn(POSTAG, Input[3])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, Input[3])</feature>";
            pool.add(concat);

            it = pool.iterator();
            a = 0;

            boolean input0Esta = false;
            while (it.hasNext()) {
                String newFeature = "postagLazyInput" + a + ".xml";
                fg.addFeatureLine(currentFeature, newFeature, it.next());
                a++;
                antFeature = newFeature;
                println("Testing " + newFeature + " ...");
                result = runBestAlgorithm(newFeature);
                println("  " + result);
                println("  Default: " + bestResult);
                if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                    Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                    difference = result - bestResultBruteForce;
                    bestResultBruteForce = result;
                    String sDifferenceLabel = String.format(pattern, difference);
                    if (a == 0) {
                        input0Esta = true;
                    }
                    /*
                     * println("New best feature model:
                     * "+featureModelBruteForce); String s=""+this.bestResult;
                     * if (s.length()==4) s+="0";
                     *
                     * println("Incremental "+evaluationMeasure+"
                     * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                     */

                }
            }
            if (input0Esta) {
                //add trigram stack[0], input[0], lookahead[0]
                String newFeature = "specialTrigramINPUT.xml";
                //
                fg.addMergeFeaturesMerge3SpecialCase(featureModelBruteForce, newFeature, "POSTAG", 0, "InputColumn");
                result = runBestAlgorithm(newFeature);
                if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                    Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                    difference = result - bestResultBruteForce;
                    bestResultBruteForce = result;
                    String sDifferenceLabel = String.format(pattern, difference);
                    /*
                     * println("New best feature model:
                     * "+featureModelBruteForce); String s=""+this.bestResult;
                     * if (s.length()==4) s+="0";
                     *
                     * println("Incremental "+evaluationMeasure+"
                     * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                     */

                }
            }
        }




        maxS = fg.findMaxStack("POSTAG", featureModelBruteForce);
        maxI = fg.findMaxInput("POSTAG", featureModelBruteForce, structureI);
        //ADD MERGE FEATURES
        //TEST WITH THE NEW FEATURES
		/*
         * String newFeature2="prueba.xml";
		fg.addStackWindow(featureModel,newFeature2,"POSTAG",InputLookAhead);
         */
        //ArrayList<String> poolOfActions=new ArrayList<String>();

        String structureS = "Stack";
        structureI = InputLookAhead;
        if (bestAlgorithm.contains("cov")) {
            structureS = "Left";
        }
        //<feature>Merge(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]))</feature>
        if ((maxS >= 0) && (maxI >= 0)) {
            if (bestAlgorithm.contains("stack")) {
                //pool.add("\t\t<feature>Merge(InputColumn(POSTAG, "+structureS+"[1]), InputColumn(POSTAG, "+structureS+"[0]))</feature>");
				/*
                 * <feature>Merge3(InputColumn(POSTAG, Stack[2]),
                 * InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG,
                 * Stack[0]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG,
                 * Lookahead[0]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Stack[0]), InputColumn(POSTAG, Lookahead[0]),
                 * InputColumn(POSTAG, Lookahead[1]))</feature>
                 * <feature>Merge3(InputColumn(POSTAG, Lookahead[0]),
                 * InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]))</feature>
                 */
                pool = new ArrayList<>();
                String concat = "";
                int h = -1;
                for (int j = 1; j <= maxS; j++) {
                    if (h == -1) {
                        concat += "\t\t<feature>Merge(InputColumn(POSTAG, " + structureS + "[" + j + "]), InputColumn(POSTAG, " + structureS + "[0]))</feature>";
                    } else {
                        concat += "\n\t\t<feature>Merge(InputColumn(POSTAG, " + structureS + "[" + j + "]), InputColumn(POSTAG, " + structureI + "[" + h + "]))</feature>";
                    }
                    pool.add(concat);
                    h++;
                }
                it = pool.iterator();
                a = 0;
                currentFeature = featureModelBruteForce;
                while (it.hasNext()) {
                    String newFeature = "postagMerge" + a + ".xml";
                    fg.addFeatureLine(currentFeature, newFeature, it.next());
                    a++;
                    antFeature = newFeature;
                    println("Testing " + newFeature + " ...");
                    result = runBestAlgorithm(newFeature);
                    println("  " + result);
                    println("  Default: " + bestResult);
                    if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                        Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                        difference = result - bestResultBruteForce;
                        bestResultBruteForce = result;
                        String sDifferenceLabel = String.format(pattern, difference);
                        /*
                         * println("New best feature model:
                         * "+featureModelBruteForce); String
                         * s=""+this.bestResult; if (s.length()==4) s+="0";
                         *
                         * println("Incremental "+evaluationMeasure+"
                         * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                         */

                    }
                }
                pool = new ArrayList<>();
                concat = "";
                /*
                 * <feature>Merge3(InputColumn(POSTAG, Stack[2]),
                 * InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG,
                 * Stack[0]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG,
                 * Lookahead[0]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Stack[0]), InputColumn(POSTAG, Lookahead[0]),
                 * InputColumn(POSTAG, Lookahead[1]))</feature>
                 * <feature>Merge3(InputColumn(POSTAG, Lookahead[0]),
                 * InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG,
                 * Lookahead[2]))</feature>
                 */
                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[2]), InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]))</feature>");
                concat += "\t\t<feature>Merge3(InputColumn(POSTAG, Stack[2]), InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]))</feature>";

                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]))</feature>");
                concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]))</feature>";
                pool.add(concat);
                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]))</feature>");
                concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]))</feature>";
                pool.add(concat);

                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]))</feature>");
                concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]))</feature>";
                pool.add(concat);

                if (maxI >= 3) {
                    pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]), InputColumn(POSTAG, Lookahead[3]))</feature>");
                    concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]), InputColumn(POSTAG, Lookahead[3]))</feature>";
                    pool.add(concat);
                }

                if (maxS >= 3) {
                    pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[3]), InputColumn(POSTAG, Stack[2]), InputColumn(POSTAG, Stack[1]))</feature>");
                    concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, Stack[3]), InputColumn(POSTAG, Stack[2]), InputColumn(POSTAG, Stack[1]))</feature>";
                    pool.add(concat);
                }

                it = pool.iterator();
                a = 0;
                currentFeature = featureModelBruteForce;
                while (it.hasNext()) {
                    String newFeature = "postagMerge3" + a + ".xml";
                    fg.addFeatureLine(currentFeature, newFeature, it.next());
                    a++;
                    antFeature = newFeature;
                    println("Testing " + newFeature + " ...");
                    result = runBestAlgorithm(newFeature);
                    println("  " + result);
                    println("  Default: " + bestResult);
                    if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                        Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                        difference = result - bestResultBruteForce;
                        bestResultBruteForce = result;
                        String sDifferenceLabel = String.format(pattern, difference);
                        /*
                         * println("New best feature model:
                         * "+featureModelBruteForce); String
                         * s=""+this.bestResult; if (s.length()==4) s+="0";
                         *
                         * println("Incremental "+evaluationMeasure+"
                         * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                         */
                    }
                }
            } else {
                pool = new ArrayList<>();
                String concat = "";
                for (int j = 0; j <= maxS; j++) {
                    if (j <= maxI) {
                        if (j == 0) {
                            concat += "\t\t<feature>Merge(InputColumn(POSTAG, " + structureS + "[" + j + "]), InputColumn(POSTAG, " + structureI + "[" + j + "]))</feature>";
                        } else {
                            concat += "\n\t\t<feature>Merge(InputColumn(POSTAG, " + structureS + "[" + j + "]), InputColumn(POSTAG, " + structureI + "[" + j + "]))</feature>";
                        }
                        pool.add(concat);
                    }
                }
                it = pool.iterator();
                a = 0;
                currentFeature = featureModelBruteForce;
                while (it.hasNext()) {
                    String newFeature = "postagMerge" + a + ".xml";
                    fg.addFeatureLine(currentFeature, newFeature, it.next());
                    a++;
                    antFeature = newFeature;
                    println("Testing " + newFeature + " ...");
                    result = runBestAlgorithm(newFeature);
                    println("  " + result);
                    println("  Default: " + bestResult);
                    if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                        Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                        difference = result - bestResultBruteForce;
                        bestResultBruteForce = result;
                        String sDifferenceLabel = String.format(pattern, difference);
                        /*
                         * println("New best feature model:
                         * "+featureModelBruteForce); String
                         * s=""+this.bestResult; if (s.length()==4) s+="0";
                         *
                         * println("Incremental "+evaluationMeasure+"
                         * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                         */

                    }
                }

                /*
                 * <feature>Merge3(InputColumn(POSTAG, Stack[1]),
                 * InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG,
                 * Input[0]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Stack[0]), InputColumn(POSTAG, Input[0]), InputColumn(POSTAG,
                 * Input[1]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Input[0]), InputColumn(POSTAG, Input[1]), InputColumn(POSTAG,
                 * Input[2]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Input[1]), InputColumn(POSTAG, Input[2]), InputColumn(POSTAG, Input[3]))</feature>
                 */
                concat = "";
                pool = new ArrayList<>();
                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structureS + "[1]), InputColumn(POSTAG, " + structureS + "[0]), InputColumn(POSTAG, " + structureI + "[0]))</feature>");
                concat += "\t\t<feature>Merge3(InputColumn(POSTAG, " + structureS + "[1]), InputColumn(POSTAG, " + structureS + "[0]), InputColumn(POSTAG, " + structureI + "[0]))</feature>";
                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structureS + "[0]), InputColumn(POSTAG, " + structureI + "[0]), InputColumn(POSTAG, " + structureI + "[1]))</feature>");
                concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, " + structureS + "[0]), InputColumn(POSTAG, " + structureI + "[0]), InputColumn(POSTAG, " + structureI + "[1]))</feature>";
                pool.add(concat);
                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structureI + "[0]), InputColumn(POSTAG, " + structureI + "[1]), InputColumn(POSTAG, " + structureI + "[2]))</feature>");
                concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, " + structureI + "[0]), InputColumn(POSTAG, " + structureI + "[1]), InputColumn(POSTAG, " + structureI + "[2]))</feature>";
                pool.add(concat);

                pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structureI + "[1]), InputColumn(POSTAG, " + structureI + "[2]), InputColumn(POSTAG, " + structureI + "[3]))</feature>");
                concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, " + structureI + "[1]), InputColumn(POSTAG, " + structureI + "[2]), InputColumn(POSTAG, " + structureI + "[3]))</feature>";

                if (maxI > 3) {
                    pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structureI + "[2]), InputColumn(POSTAG, " + structureI + "[3]), InputColumn(POSTAG, " + structureI + "[4]))</feature>");
                    concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, " + structureI + "[2]), InputColumn(POSTAG, " + structureI + "[3]), InputColumn(POSTAG, " + structureI + "[4]))</feature>";
                }
                if (maxS > 1) {
                    pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structureS + "[2]), InputColumn(POSTAG, " + structureS + "[1]), InputColumn(POSTAG, " + structureS + "[0]))</feature>");
                    concat += "\n\t\t<feature>Merge3(InputColumn(POSTAG, " + structureS + "[2]), InputColumn(POSTAG, " + structureS + "[1]), InputColumn(POSTAG, " + structureS + "[0]))</feature>";
                }

                it = pool.iterator();
                a = 0;
                currentFeature = featureModelBruteForce;
                while (it.hasNext()) {
                    String newFeature = "postagMerge3" + a + ".xml";
                    fg.addFeatureLine(currentFeature, newFeature, it.next());
                    a++;
                    antFeature = newFeature;
                    println("Testing " + newFeature + " ...");
                    result = runBestAlgorithm(newFeature);
                    println("  " + result);
                    println("  Default: " + bestResult);
                    
                    //Shrinking //NO THRESHOLD because we are removing
                    if (result >= (Optimizer.bestResultBruteForce + threshold)) { 
                        //dummy (just a matter of completity)
                        Optimizer.featureModelBruteForce = antFeature; 
                        difference = result - bestResultBruteForce;
                        bestResultBruteForce = result;
                        String sDifferenceLabel = String.format(pattern, difference);
                        /*
                         * println("New best feature model:
                         * "+featureModelBruteForce); String
                         * s=""+this.bestResult; if (s.length()==4) s+="0";
                         *
                         * println("Incremental "+evaluationMeasure+"
                         * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                         */

                    }
                }

                /*
                 * <feature>Merge3(InputColumn(POSTAG, Stack[1]),
                 * InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG,
                 * Input[0]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Stack[0]), InputColumn(POSTAG, Input[0]), InputColumn(POSTAG,
                 * Input[1]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Input[0]), InputColumn(POSTAG, Input[1]), InputColumn(POSTAG,
                 * Input[2]))</feature> <feature>Merge3(InputColumn(POSTAG,
                 * Input[1]), InputColumn(POSTAG, Input[2]), InputColumn(POSTAG, Input[3]))</feature>
                 */
            }
        }

        if (bestAlgorithm.equals("covnonproj")) {

            //
            String concat = "";
            pool = new ArrayList<>();
            pool.add("\t\t<feature>InputColumn(POSTAG, LeftContext[0])</feature>");
            concat += "\t\t<feature>InputColumn(POSTAG, LeftContext[0])</feature>";
            pool.add("\t\t<feature>InputColumn(POSTAG, RightContext[0])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, RightContext[0])</feature>";
            pool.add(concat);
            pool.add("\t\t<feature>InputColumn(POSTAG, LeftContext[1])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, LeftContext[1])</feature>";
            pool.add("\t\t<feature>InputColumn(POSTAG, RightContext[1])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, RightContext[1])</feature>";
            pool.add(concat);
            pool.add("\t\t<feature>InputColumn(POSTAG, LeftContext[2])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, LeftContext[2])</feature>";
            pool.add("\t\t<feature>InputColumn(POSTAG, RightContext[2])</feature>");
            concat += "\n\t\t<feature>InputColumn(POSTAG, RightContext[2])</feature>";
            pool.add(concat);



            it = pool.iterator();
            a = 0;
            currentFeature = featureModelBruteForce;
            while (it.hasNext()) {
                String newFeature = "postagRightLeftContext" + a + ".xml";
                fg.addFeatureLine(currentFeature, newFeature, it.next());
                a++;
                antFeature = newFeature;
                println("Testing " + newFeature + " ...");
                result = runBestAlgorithm(newFeature);
                println("  " + result);
                println("  Default: " + bestResult);
                if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                    Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                    difference = result - bestResultBruteForce;
                    bestResultBruteForce = result;
                    String sDifferenceLabel = String.format(pattern, difference);
                    /*
                     * println("New best feature model:
                     * "+featureModelBruteForce); String s=""+this.bestResult;
                     * if (s.length()==4) s+="0";
                     *
                     * println("Incremental "+evaluationMeasure+"
                     * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                     */
                }
            }
        }




        /*
         *
         * if (bestAlgorithm.equals("covnonproj")) {
         *
         * newFeature="backwardLeftContext.xml"; //
         * fg.removeLeftContextWindowSpecial(featureModel,newFeature,"POSTAG");
         * result=runBestAlgorithm(newFeature); //println(result);
         * boolean leftContext0Esta=true; // difference=0.0; if
         * (result>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing leftContext0Esta=false; featureModel=newFeature;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * else { //Expanding // String newFeature2Abs="forwardRightContext";
         * String anterior=featureModel; boolean keepGoing=true; for(int
         * i=1;i<3;i++) { if (keepGoing) { String
         * newFeature2=newFeature2Abs+i+".xml";
         * fg.addLeftContextWindowSpecialCase(anterior,newFeature2,"POSTAG",InputLookAhead,"InputColumn");
         * anterior=newFeature2; //OJO!!! En este caso es Input porque para el
         * primer test es con NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS
         * double result2=runBestAlgorithm(newFeature2);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature2; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * else { keepGoing=false; } } } }
         *
         *
         * newFeature="backwardLeftContext.xml"; //
         * fg.removeRightContextWindowSpecial(featureModel,newFeature,"POSTAG");
         * result=runBestAlgorithm(newFeature); //println(result);
         * boolean rightContext0Esta=true; // difference=0.0; if
         * (result>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing leftContext0Esta=false; featureModel=newFeature;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * else { //Expanding // String newFeature2Abs="forwardRightContext";
         * String anterior=featureModel; boolean keepGoing=true; for(int
         * i=1;i<3;i++) { if (keepGoing) { String
         * newFeature2=newFeature2Abs+i+".xml";
         * fg.addRightContextWindowSpecialCase(anterior,newFeature2,"POSTAG",InputLookAhead,"InputColumn");
         * anterior=newFeature2; //OJO!!! En este caso es Input porque para el
         * primer test es con NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS
         * double result2=runBestAlgorithm(newFeature2);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature2; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * else { keepGoing=false; } } } }
			}
         */
    }

    private void formTuning() {
        FeatureGenerator fg = new FeatureGenerator(writer);

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        String newFeature = "backwardStackForm.xml";
        //
        fg.removeStackWindow(featureModel, newFeature, "FORM");
        double result = runBestAlgorithm(newFeature);
        
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardStackForm" + i + ".xml";
                    fg.addStackWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////

        newFeature = "backwardInputForm.xml";
        fg.removeInputWindow(featureModel, newFeature, "FORM", InputLookAhead);
        result = runBestAlgorithm(newFeature);
        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardInputForm" + i + ".xml";
                    fg.addInputWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm(newFeature);
                    keepGoing = setBestResult(result, newFeature);  //Shrinking
                }
            }
        }
        /////////////////////////////////////////////////////////////////
        //STEP 3
        /////////////////////////////////////////////////////////////////
        if (!bestAlgorithm.equals("nivrestandard")) {
            String newFeature5 = "backwardHeadIterative.xml";
            boolean generar = fg.removeIterativeWindow(featureModel, newFeature5, "FORM", "InputLookAhead", "Input");
            double result5 = 0.0;
            if (generar) {
                result5 = runBestAlgorithm(newFeature5);
            }
            res = setBestResultNoThreshold(result5, newFeature5); //Shrinking //NO THRESHOLD because we are removing
            if (res == false) { //Expanding
                String anterior = featureModel;
                boolean keepGoing = true;
                for (int i = 1; i < 4; i++) {
                    if (keepGoing) {
                        newFeature = "headIterativeForm" + i + ".xml";
                        fg.addHeadIterativeWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                        anterior = newFeature;
                        result = runBestAlgorithm(newFeature);
                        keepGoing = setBestResult(result, newFeature); //Shrinking
                    }
                }
            }
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            String anterior = featureModel;
            newFeature = "forwardINPUTsc0.xml";
            boolean generar = fg.addInputWindowSpecialCase(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            if (generar) {
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void formTuning5Fold() {
        FeatureGenerator fg = new FeatureGenerator(writer);

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        String newFeature = "backwardStackForm.xml";
        fg.removeStackWindow(featureModel, newFeature, "FORM");
        double result = runBestAlgorithm5Fold(newFeature);
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardStackForm" + i + ".xml";
                    fg.addStackWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm5Fold(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////

        newFeature = "backwardInputForm.xml";
        fg.removeInputWindow(featureModel, newFeature, "FORM", InputLookAhead);
        result = runBestAlgorithm5Fold(newFeature);
        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { //Expanding
            String anterior = featureModel;
            boolean keepGoing = true;
            for (int i = 1; i < 4; i++) {
                if (keepGoing) {
                    newFeature = "forwardInputForm" + i + ".xml";
                    fg.addInputWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                    anterior = newFeature;
                    result = runBestAlgorithm5Fold(newFeature);
                    keepGoing = setBestResult(result, newFeature); //Shrinking
                }
            }
        }
        /////////////////////////////////////////////////////////////////
        //STEP 3
        /////////////////////////////////////////////////////////////////
        if (!bestAlgorithm.equals("nivrestandard")) {
            String newFeature5 = "backwardHeadIterative.xml";
            boolean generar = fg.removeIterativeWindow(featureModel, newFeature5, "FORM", "InputLookAhead", "Input");
            double result5 = 0.0;
            if (generar) {
                result5 = runBestAlgorithm5Fold(newFeature5);
            }
            
            res = setBestResultNoThreshold(result5, newFeature5); //Shrinking //NO THRESHOLD because we are removing
            if (res == false) { //Expanding
                String anterior = featureModel;
                boolean keepGoing = true;
                for (int i = 1; i < 4; i++) {
                    if (keepGoing) {
                        newFeature = "headIterativeForm" + i + ".xml";
                        fg.addHeadIterativeWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                        anterior = newFeature;
                        result = runBestAlgorithm5Fold(newFeature);
                        keepGoing = setBestResult(result, newFeature); //Shrinking
                    }
                }
            }
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            String anterior = featureModel;

            newFeature = "forwardINPUTsc0.xml";
            boolean generar = fg.addInputWindowSpecialCase(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            if (generar) {
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void formTuningRelaxedGreedy() {

        FeatureGenerator fg = new FeatureGenerator(writer);

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        String oldFeatureModel = featureModel;
        String newFeature = "backwardStackForm.xml";
        fg.removeStackWindow(featureModel, newFeature, "FORM");
        double result = runBestAlgorithm(newFeature);
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == false) { 
            newFeature = "backwardStackFormTwice.xml";
            fg.removeStackWindow(newFeature, newFeature, "FORM");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }

        String anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardStackForm" + i + ".xml";
            fg.addStackWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////
        oldFeatureModel = featureModel;
        newFeature = "backwardInputForm.xml";
        fg.removeInputWindow(featureModel, newFeature, "FORM", InputLookAhead);
        result = runBestAlgorithm(newFeature);
        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardInputFormTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "FORM", InputLookAhead);
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardInputFormTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "FORM", InputLookAhead);
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }


        //Expanding
        anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardInputForm" + i + ".xml";
            fg.addInputWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3
        /////////////////////////////////////////////////////////////////
        if (!bestAlgorithm.equals("nivrestandard")) {
            oldFeatureModel = featureModel;
            newFeature = "backwardHeadIterative.xml";
            fg.removeIterativeWindow(featureModel, newFeature, "FORM", "InputLookAhead", "Input");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            //else { //Expanding
            anterior = oldFeatureModel;
            for (int i = 1; i < 4; i++) {
                newFeature = "headIterativeForm" + i + ".xml";
                fg.addHeadIterativeWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            anterior = featureModel;
            newFeature = "forwardINPUT0.xml";
            fg.addInputWindowSpecialCase(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }
    }

    private void formTuningOnlyBackward() {

        FeatureGenerator fg = new FeatureGenerator(writer);

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        String newFeature = "backwardStackForm.xml";
        fg.removeStackWindow(featureModel, newFeature, "FORM");
        double result = runBestAlgorithm(newFeature);
        boolean res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardStackFormTwice.xml";
            fg.removeStackWindow(newFeature, newFeature2, "FORM");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardStackFormTwice.xml";
            fg.removeStackWindow(newFeature, newFeature2, "FORM");
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////
        newFeature = "backwardInputForm.xml";
        fg.removeInputWindow(featureModel, newFeature, "FORM", InputLookAhead);
        result = runBestAlgorithm(newFeature);
        res = setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        if (res == true) { 
            String newFeature2 = "backwardInputFormTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "FORM", InputLookAhead);
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        } else {
            String newFeature2 = "backwardInputFormTwice.xml";
            fg.removeInputWindow(newFeature, newFeature2, "FORM", InputLookAhead);
            result = runBestAlgorithm(newFeature2);
            setBestResultNoThreshold(result, newFeature2); //Shrinking //NO THRESHOLD because we are removing
        }



        /////////////////////////////////////////////////////////////////
        //STEP 3
        /////////////////////////////////////////////////////////////////
        if (!bestAlgorithm.equals("nivrestandard")) {
            newFeature = "backwardHeadIterative.xml";
            fg.removeIterativeWindow(featureModel, newFeature, "FORM", "InputLookAhead", "Input");
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }
    }

    private void formTuningOnlyForward() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1
        /////////////////////////////////////////////////////////////////
        String oldFeatureModel = featureModel;
        String anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardStackForm" + i + ".xml";
            fg.addStackWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2
        /////////////////////////////////////////////////////////////////
        oldFeatureModel = featureModel;
        anterior = oldFeatureModel;
        for (int i = 1; i < 4; i++) {
            newFeature = "forwardInputForm" + i + ".xml";
            fg.addInputWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3
        /////////////////////////////////////////////////////////////////
        if (!bestAlgorithm.equals("nivrestandard")) {
            oldFeatureModel = featureModel;
            anterior = oldFeatureModel;
            for (int i = 1; i < 4; i++) {
                newFeature = "headIterativeForm" + i + ".xml";
                fg.addHeadIterativeWindow(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            anterior = featureModel;
            newFeature = "forwardINPUT0.xml";
            fg.addInputWindowSpecialCase(anterior, newFeature, "FORM", InputLookAhead, "InputColumn");
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }
    }

    private void formTuningBruteForce() {
        FeatureGenerator fg = new FeatureGenerator(writer);

        ArrayList<String> pool = new ArrayList<>();
        String structure = "Stack";
        if (bestAlgorithm.contains("cov")) {
            structure = "Left";
        }

        String structureI = "Input";
        if (bestAlgorithm.contains("cov")) {
            structureI = "Right";
        }
        if (bestAlgorithm.contains("stack")) {
            structureI = "Lookahead";
        }
        //for(int i=0;i<6;i++){
        int i = 0;
        pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>");
        pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>");
        if (i < 5) {
            int j = i + 1;
            pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + j + "])</feature>");
            pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + j + "])</feature>");
            if (i < 4) {
                int k = j + 1;
                pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + k + "])</feature>");
                pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + k + "])</feature>");
                if (i < 3) {
                    int l = k + 1;
                    pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + k + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + l + "])</feature>");
                    pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + k + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + l + "])</feature>");
                    if (i < 2) {
                        int m = l + 1;
                        pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + k + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + l + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + m + "])</feature>");
                        pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + k + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + l + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + m + "])</feature>");
                        if (i < 1) {
                            int n = m + 1;
                            pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + k + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + l + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + m + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[" + n + "])</feature>");
                            pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + j + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + k + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + l + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + m + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[" + n + "])</feature>");
                        }
                    }
                }
            }
        }
        //}


        Iterator<String> it = pool.iterator();
        String currentFeature = featureModelBruteForce;
        int a = 0;
        while (it.hasNext()) {
            String newFeature = "form" + structure + structureI + a + ".xml";
            fg.addFeatureLine(currentFeature, newFeature, it.next());
            a++;
            String antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            double result = runBestAlgorithm(newFeature);
            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                bestResultBruteForce = result;
            }
        }

        currentFeature = featureModelBruteForce;
        int maxS = fg.findMaxStack("FORM", currentFeature);
        int maxI = fg.findMaxInput("FORM", currentFeature, structureI);
        pool = new ArrayList<>();
        if ((maxS >= 0) && (maxI == -1)) {
            pool = new ArrayList<>();
            structure = "Input";
            if (bestAlgorithm.contains("cov")) {
                structure = "Right";
            }
            if (bestAlgorithm.contains("stack")) {
                structure = "Lookahead";
            }
            //for(int i=0;i<6;i++){
            i = 0;
            pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>");
            pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[1])</feature>");
            pool.add("\t\t<feature>InputColumn(FORM, " + structureI + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structureI + "[1])</feature>");
        } else {
            if ((maxS == -1) && (maxI >= 0)) {
                structure = "Stack";
                if (bestAlgorithm.contains("cov")) {
                    structure = "Left";
                }
                i = 0;
                pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>");
                pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[1])</feature>");
                pool.add("\t\t<feature>InputColumn(FORM, " + structure + "[" + i + "])</feature>\n\t\t<feature>InputColumn(FORM, " + structure + "[1])</feature>");
            }
        }
        it = pool.iterator();
        a = 0;

        while (it.hasNext()) {
            String newFeature = "formBackTracking" + a + ".xml";
            fg.addFeatureLine(currentFeature, newFeature, it.next());
            a++;
            String antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            double result = runBestAlgorithm(newFeature);

            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) { //Shrinking //NO THRESHOLD because we are removing
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                bestResultBruteForce = result;
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3
        /////////////////////////////////////////////////////////////////
        //
        String newFeature4Abs = "headIterativeForm";
        String anterior = featureModelBruteForce;
        for (int j = 1; j < 5; j++) {

            String newFeature4 = newFeature4Abs + j + ".xml";
            fg.addHeadIterativeWindow(anterior, newFeature4, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature4;
            println("Testing " + newFeature4 + " ...");
            double result2 = runBestAlgorithm(newFeature4);
            println("  " + result2);
            println("  Default: " + bestResult);
            double difference2 = 0.0;
            if (result2 > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
                featureModelBruteForce = newFeature4;
                Optimizer.bestResultBruteForce = result2;
            }
        }

        if (bestAlgorithm.equals("stackeager") || bestAlgorithm.equals("stacklazy")) {
            String newFeature2Abs = "forwardINPUT";
            anterior = featureModel;
            String newFeature2 = newFeature2Abs + "0.xml";
            fg.addInputWindowSpecialCase(anterior, newFeature2, "FORM", InputLookAhead, "InputColumn");
            anterior = newFeature2;
            println("Testing " + newFeature2 + " ...");
            double result2 = runBestAlgorithm(newFeature2);

            println("  " + result2);
            println("  Default: " + bestResult);

            double difference2 = 0.0;
            if (result2 > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
                featureModelBruteForce = newFeature2;
                Optimizer.bestResultBruteForce = result2;
            }
        }
    }

    private void deprelTuning() {
        
        FeatureGenerator fg = new FeatureGenerator(writer);
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1_ Subtracting single DEPREL one by one.
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 4; i++) {
            newFeature = "deprelSubtractingFeature" + i + ".xml";
            boolean lanzar = fg.removeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            if (lanzar) {
                result = runBestAlgorithm(newFeature);
                setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            }
        }
        ///////////////////////////////////////////
        //NIVRESTANDARD EXCEPTION
        //ADD RDEP(Stack[0]) if it works add(Postag, RDEP(Stack[0]))
        //////////////////////////////////////////
        if (bestAlgorithm.equals("nivrestandard")) {
            newFeature = "rdepAddFeature.xml";
            fg.addRdepWindow(featureModel, newFeature, "DEPREL");
            result = runBestAlgorithm(newFeature);
            boolean res = setBestResult(result, newFeature); //Shrinking
            if (res == true) {
                fg.addRdepWindow(featureModel, newFeature, "POSTAG");
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2_ Subtracting merges one by one.
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 3; i++) {
            newFeature = "deprelMergeSubtractingFeature" + i + ".xml";
            boolean generar = fg.removeMergeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            if (generar) {
                result = runBestAlgorithm(newFeature);
                setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3_ REPLICATING DEPREL--POSTAG
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 3; i++) {
            newFeature = "replicateDeprelPostagSubtractingFeature" + i + ".xml";
            boolean generar = fg.replicatePostagDeprel(featureModel, newFeature, "DEPREL", "", i);
            if (generar) {
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void deprelTuning5Fold() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1_ Subtracting single DEPREL one by one.
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 4; i++) {
            newFeature = "deprelSubtractingFeature" + i + ".xml";
            boolean lanzar = fg.removeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            if (lanzar) {
                result = runBestAlgorithm5Fold(newFeature);
                setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            }
        }
        ///////////////////////////////////////////
        //NIVRESTANDARD EXCEPTION
        //ADD RDEP(Stack[0]) if it works add(Postag, RDEP(Stack[0]))
        //////////////////////////////////////////
        if (bestAlgorithm.equals("nivrestandard")) {
            newFeature = "rdepAddFeature.xml";
            fg.addRdepWindow(featureModel, newFeature, "DEPREL");
            result = runBestAlgorithm5Fold(newFeature);
            boolean res = setBestResult(result, newFeature); //Shrinking
            if (res == true) {
                fg.addRdepWindow(featureModel, newFeature, "POSTAG");
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2_ Subtracting merges one by one.
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 3; i++) {
            newFeature = "deprelMergeSubtractingFeature" + i + ".xml";
            boolean generar = fg.removeMergeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            if (generar) {
                result = runBestAlgorithm5Fold(newFeature);
                setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3_ REPLICATING DEPREL--POSTAG
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 3; i++) {
            newFeature = "replicateDeprelPostagSubtractingFeature" + i + ".xml";
            boolean generar = fg.replicatePostagDeprel(featureModel, newFeature, "DEPREL", "", i);
            if (generar) {
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void deprelTuningRelaxedGreedy() {
        
        FeatureGenerator fg = new FeatureGenerator(writer);
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1_ Subtracting single DEPREL one by one.
        /////////////////////////////////////////////////////////////////

        String antFeature = featureModel;
        for (int i = 1; i <= 4; i++) {
            newFeature = "deprelSubtractingFeature" + i + ".xml";
            fg.removeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            antFeature = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }
        ///////////////////////////////////////////
        //NIVRESTANDARD EXCEPTION
        //ADD RDEP(Stack[0]) if it works add(Postag, RDEP(Stack[0]))
        //////////////////////////////////////////
        if (bestAlgorithm.equals("nivrestandard")) {
            newFeature = "rdepAddFeature.xml";
            fg.addRdepWindow(featureModel, newFeature, "DEPREL");
            antFeature = newFeature;
            result = runBestAlgorithm(newFeature);
            boolean res = setBestResult(result, newFeature); //Shrinking
            if (res == true) {
                fg.addRdepWindow(featureModel, newFeature, "POSTAG");
                antFeature = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 2_ Subtracting merges one by one.
        /////////////////////////////////////////////////////////////////

        antFeature = featureModel;
        for (int i = 1; i <= 3; i++) {
            newFeature = "deprelMergeSubtractingFeature" + i + ".xml";
            fg.removeMergeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            antFeature = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3_ REPLICATING DEPREL--POSTAG
        /////////////////////////////////////////////////////////////////

        antFeature = featureModel;
        for (int i = 1; i <= 3; i++) {
            newFeature = "replicateDeprelPostagSubtractingFeature";
            newFeature += i + ".xml";
            fg.replicatePostagDeprel(featureModel, newFeature, "DEPREL", "", i);
            antFeature = newFeature;
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        /////////////////////////////////////////////////////////////////
        //ADDING SINGLE FEATURES
        /////////////////////////////////////////////////////////////////
		/*
         * String newFeature="backwardDeprelForm.xml"; //
         * fg.removeStackWindow(featureModel,newFeature,"DEPREL"); double
         * result=runBestAlgorithm(newFeature); //println(result); //
         * double difference=0.0; if (result>=(this.bestResult)) { //Shrinking
         * //NO THRESHOLD because we are removing featureModel=newFeature;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)"); String
         * newFeature2="backwardStackFormTwice.xml"; //
         * fg.removeStackWindow(newFeature,newFeature2,"DEPREL");
         * result=runBestAlgorithm(newFeature2); //println(result);
         * // difference=0.0; if (result>=(this.bestResult)) { //Shrinking //NO
         * THRESHOLD because we are removing featureModel=newFeature2;
         * difference=result-bestResult; bestResult=result;
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * }
         * else { String newFeature2="backwardDeprelFormTwice.xml"; //
         * fg.removeStackWindow(newFeature,newFeature2,"DEPREL");
         * result=runBestAlgorithm(newFeature2); //println(result);
         * // difference=0.0; if (result>=(this.bestResult)) { //Shrinking //NO
         * THRESHOLD because we are removing featureModel=newFeature2;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * else {//Expanding // String newFeature2Abs="forwardDeprelStack";
         * String anterior=featureModel; boolean keepGoing=true; for(int
         * i=1;i<4;i++) { newFeature2=newFeature2Abs+i+".xml";
         * fg.addStackWindow(anterior,newFeature2,"DEPREL",InputLookAhead,"OutputColumn");
         * anterior=newFeature2; //OJO!!! En este caso es Input porque para el
         * primer test es con NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS
         * double result2=runBestAlgorithm(newFeature2);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature2; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         * }
         * }
         *
         * /////////////////////////////////////////////////////////////////
         * //STEP 2
         * /////////////////////////////////////////////////////////////////
         *
         * String newFeature3="backwardInputDeprel.xml"; //
         * fg.removeInputWindow(featureModel,newFeature3,"FORM",InputLookAhead);
         * double result3=runBestAlgorithm(newFeature3);
         * //println(result); // double difference3=0.0; if
         * (result3>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing featureModel=newFeature3;
         * difference3=result3-bestResult; bestResult=result3; String
         * sDifferenceLabel=""+difference3; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)"); String
         * newFeature4="backwardInputDeprelTwice.xml"; //
         * fg.removeInputWindow(newFeature3, newFeature4,
         * "DEPREL",InputLookAhead); result3=runBestAlgorithm(newFeature4);
         * //println(result); // difference3=0.0; if
         * (result3>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing featureModel=newFeature4;
         * difference3=result3-bestResult; bestResult=result3;
         * sDifferenceLabel=""+difference3; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * }
         * else { String newFeature4="backwardInputDeprelTwice.xml"; //
         * fg.removeInputWindow(newFeature3, newFeature4,
         * "DEPREL",InputLookAhead); result3=runBestAlgorithm(newFeature4);
         * //println(result); // difference3=0.0; if
         * (result3>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing featureModel=newFeature4;
         * difference3=result3-bestResult; bestResult=result3; String
         * sDifferenceLabel=""+difference3; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * else {
         *
         *
         * //Expanding // String newFeature4Abs="forwardInputDeprel"; String
         * anterior=featureModel; boolean keepGoing=true; for(int i=1;i<4;i++) {
         * newFeature4=newFeature4Abs+i+".xml";
         * fg.addInputWindow(anterior,newFeature4,"DEPREL",InputLookAhead,"OutputColumn");
         * anterior=newFeature4; //OJO!!! En este caso es Input porque para el
         * primer test es con NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS
         * double result2=runBestAlgorithm(newFeature4);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature4; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         * }
         * }
         */
    }

    private void deprelTuningOnlyBackward() {

        FeatureGenerator fg = new FeatureGenerator(writer);
        String newFeature;
        double result;

        /////////////////////////////////////////////////////////////////
        //STEP 1_ Subtracting single DEPREL one by one.
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 4; i++) {
            newFeature = "deprelSubtractingFeature" + i + ".xml";
            fg.removeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }


        /////////////////////////////////////////////////////////////////
        //STEP 2_ Subtracting merges one by one.
        /////////////////////////////////////////////////////////////////

        for (int i = 1; i <= 3; i++) {
            newFeature = "deprelMergeSubtractingFeature" + i + ".xml";
            fg.removeMergeDeprelWindow(featureModel, newFeature, "DEPREL", "", i);
            result = runBestAlgorithm(newFeature);
            setBestResultNoThreshold(result, newFeature); //Shrinking //NO THRESHOLD because we are removing
        }
    }

    private void deprelTuningOnlyForward() {

        FeatureGenerator fg = new FeatureGenerator(writer);

        String antFeature = featureModel;
        ///////////////////////////////////////////
        //NIVRESTANDARD EXCEPTION
        //ADD RDEP(Stack[0]) if it works add(Postag, RDEP(Stack[0]))
        //////////////////////////////////////////
        if (bestAlgorithm.equals("nivrestandard")) {

            String newFeature = "rdepAddFeature.xml";
            fg.addRdepWindow(featureModel, newFeature, "DEPREL");
            antFeature = newFeature;
            double result = runBestAlgorithm(newFeature);
            boolean res = setBestResult(result, newFeature); //Shrinking
            if (res == true) {
                fg.addRdepWindow(featureModel, newFeature, "POSTAG");
                antFeature = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }

        /////////////////////////////////////////////////////////////////
        //STEP 3_ REPLICATING DEPREL--POSTAG
        /////////////////////////////////////////////////////////////////

        antFeature = featureModel;
        for (int i = 1; i <= 3; i++) {
            String newFeature = "replicateDeprelPostagSubtractingFeature";
            newFeature += i + ".xml";
            fg.replicatePostagDeprel(featureModel, newFeature, "DEPREL", "", i);
            antFeature = newFeature;
            double result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        /////////////////////////////////////////////////////////////////
        //ADDING SINGLE FEATURES
        /////////////////////////////////////////////////////////////////
		/*
         * String newFeature="backwardDeprelForm.xml"; //
         * fg.removeStackWindow(featureModel,newFeature,"DEPREL"); double
         * result=runBestAlgorithm(newFeature); //println(result); //
         * double difference=0.0; if (result>=(this.bestResult)) { //Shrinking
         * //NO THRESHOLD because we are removing featureModel=newFeature;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)"); String
         * newFeature2="backwardStackFormTwice.xml"; //
         * fg.removeStackWindow(newFeature,newFeature2,"DEPREL");
         * result=runBestAlgorithm(newFeature2); //println(result);
         * // difference=0.0; if (result>=(this.bestResult)) { //Shrinking //NO
         * THRESHOLD because we are removing featureModel=newFeature2;
         * difference=result-bestResult; bestResult=result;
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * }
         * else { String newFeature2="backwardDeprelFormTwice.xml"; //
         * fg.removeStackWindow(newFeature,newFeature2,"DEPREL");
         * result=runBestAlgorithm(newFeature2); //println(result);
         * // difference=0.0; if (result>=(this.bestResult)) { //Shrinking //NO
         * THRESHOLD because we are removing featureModel=newFeature2;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * else {//Expanding // String newFeature2Abs="forwardDeprelStack";
         * String anterior=featureModel; boolean keepGoing=true; for(int
         * i=1;i<4;i++) { newFeature2=newFeature2Abs+i+".xml";
         * fg.addStackWindow(anterior,newFeature2,"DEPREL",InputLookAhead,"OutputColumn");
         * anterior=newFeature2; //OJO!!! En este caso es Input porque para el
         * primer test es con NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS
         * double result2=runBestAlgorithm(newFeature2);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature2; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         * }
         * }
         *
         * /////////////////////////////////////////////////////////////////
         * //STEP 2
         * /////////////////////////////////////////////////////////////////
         *
         * String newFeature3="backwardInputDeprel.xml"; //
         * fg.removeInputWindow(featureModel,newFeature3,"FORM",InputLookAhead);
         * double result3=runBestAlgorithm(newFeature3);
         * //println(result); // double difference3=0.0; if
         * (result3>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing featureModel=newFeature3;
         * difference3=result3-bestResult; bestResult=result3; String
         * sDifferenceLabel=""+difference3; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)"); String
         * newFeature4="backwardInputDeprelTwice.xml"; //
         * fg.removeInputWindow(newFeature3, newFeature4,
         * "DEPREL",InputLookAhead); result3=runBestAlgorithm(newFeature4);
         * //println(result); // difference3=0.0; if
         * (result3>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing featureModel=newFeature4;
         * difference3=result3-bestResult; bestResult=result3;
         * sDifferenceLabel=""+difference3; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         *
         * }
         * else { String newFeature4="backwardInputDeprelTwice.xml"; //
         * fg.removeInputWindow(newFeature3, newFeature4,
         * "DEPREL",InputLookAhead); result3=runBestAlgorithm(newFeature4);
         * //println(result); // difference3=0.0; if
         * (result3>=(this.bestResult)) { //Shrinking //NO THRESHOLD because we
         * are removing featureModel=newFeature4;
         * difference3=result3-bestResult; bestResult=result3; String
         * sDifferenceLabel=""+difference3; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * else {
         *
         *
         * //Expanding // String newFeature4Abs="forwardInputDeprel"; String
         * anterior=featureModel; boolean keepGoing=true; for(int i=1;i<4;i++) {
         * newFeature4=newFeature4Abs+i+".xml";
         * fg.addInputWindow(anterior,newFeature4,"DEPREL",InputLookAhead,"OutputColumn");
         * anterior=newFeature4; //OJO!!! En este caso es Input porque para el
         * primer test es con NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS
         * double result2=runBestAlgorithm(newFeature4);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature4; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         * }
         * }
         */
    }

    private void deprelTuningBruteForce() {

        FeatureGenerator fg = new FeatureGenerator(writer);

        double result;
        double difference;
        //ADD FEATURES TO THE POOL. 
        //If we have 3...	
        //  {f1,f2,f3} => { {f1},{f2},{f3},{f1,f2},{f1,f3},{f2,f3},{f1,f2,f3} } 
        //
        ArrayList<String> pool = new ArrayList<>();
        String structure = "Stack";
        if (bestAlgorithm.contains("cov")) {
            structure = "Left";
        }

        String structureI = "Input";
        if (bestAlgorithm.contains("cov")) {
            structureI = "Right";
        }
        if (bestAlgorithm.contains("stack")) {
            structureI = "Lookahead";
        }
        //for(int i=0;i<6;i++){
        int i = 0;
        pool.add("\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + i + "])</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + i + "])</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + i + "])</feature>");
        if (i < 5) {
            int j = i + 1;
            pool.add("\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + j + "])</feature>");
            pool.add("\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + j + "])</feature>");
            pool.add("\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + j + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + j + "])</feature>");
            if (i < 4) {
                int k = j + 1;
                pool.add("\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + j + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + k + "])</feature>");
                pool.add("\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + j + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + k + "])</feature>");
                pool.add("\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + j + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structure + "[" + k + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + i + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + j + "])</feature>\n\t\t<feature>OutputColumn(DEPREL, " + structureI + "[" + k + "])</feature>");
            }
        }
        //}


        Iterator<String> it = pool.iterator();
        int a = 0;
        String currentFeature = featureModelBruteForce;
        while (it.hasNext()) {
            String newFeature = "deprel" + structure + structureI + a + ".xml";
            fg.addFeatureLine(currentFeature, newFeature, it.next());
            a++;
            String antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);

            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */

            }
        }

        pool = new ArrayList<>();
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structure + "[0]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structure + "[0]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structureI + "[0]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structureI + "[0]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structure + "[1]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structure + "[1]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structureI + "[1]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structureI + "[1]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structure + "[2]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structure + "[2]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structureI + "[2]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structureI + "[2]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structure + "[3]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structure + "[3]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, rdep(" + structureI + "[3]))</feature>");
        pool.add("\t\t<feature>OutputColumn(DEPREL, ldep(" + structureI + "[3]))</feature>");

        it = pool.iterator();
        a = 0;
        while (it.hasNext()) {
            String newFeature = "deprelLdepRdep" + structure + structureI + a + ".xml";
            fg.addFeatureLine(featureModelBruteForce, newFeature, it.next());
            a++;
            String antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);

            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */
            }
        }



        /*
         * <feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL,
         * ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>
         * <feature>Merge3(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL,
         * ldep(Stack[1])), OutputColumn(DEPREL, rdep(Stack[1])))</feature>
         */
        /*
         * <feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL,
         * ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>
         * <feature>Merge(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL,
         * Stack[0]))</feature> <feature>Merge(InputColumn(POSTAG, Input[0]),
         * OutputColumn(DEPREL, ldep(Input[0])))</feature>
         */
        /*
         * <feature>Merge3(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL,
         * ldep(Left[0])), OutputColumn(DEPREL, rdep(Left[0])))</feature>
         * <feature>Merge(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL,
         * Left[0]))</feature> <feature>Merge(InputColumn(POSTAG, Right[0]),
         * OutputColumn(DEPREL, ldep(Right[0])))</feature>
         * <feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, Right[0]))</feature>
         */

        pool = new ArrayList<>();
        if (bestAlgorithm.contains("stack")) {
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structure + "[0]), OutputColumn(DEPREL, ldep(" + structure + "[0])), OutputColumn(DEPREL, rdep(" + structure + "[0])))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structure + "[1]), OutputColumn(DEPREL, ldep(" + structure + "[1])), OutputColumn(DEPREL, rdep(" + structure + "[1])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL, ldep(Stack[1])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL, ldep(Stack[0])))</feature>");
        }
        if (bestAlgorithm.contains("nivre")) {
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, Stack[0]))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Input[0]), OutputColumn(DEPREL, ldep(Input[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Input[0]), OutputColumn(DEPREL, rdep(Input[0])))</feature>");
        }
        if (bestAlgorithm.contains("cov")) {
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, ldep(Left[0])), OutputColumn(DEPREL, rdep(Left[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, Left[0]))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, ldep(Right[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, Right[0]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Left[1]), InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]), InputColumn(POSTAG, Right[3]))</feature>");
        }

        it = pool.iterator();
        a = 0;
        while (it.hasNext()) {
            String newFeature = "deprelMerge" + structure + structureI + a + ".xml";
            fg.addFeatureLine(featureModelBruteForce, newFeature, it.next());
            a++;
            String antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);

            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */
            }
        }
        /*
         * /////////////////////////////////////////////////////////////////
         * //STEP 1_ Subtracting single DEPREL one by one.
         * /////////////////////////////////////////////////////////////////
         *
         * String antFeature=featureModel; for (int i=1;i<=4;i++){ String
         * newFeature="deprelSubtractingFeature"; newFeature+=i+".xml";
         * fg.removeDeprelWindow(featureModel, newFeature, "DEPREL", "",i);
         * antFeature=newFeature; //fg.removeInputWindow(newFeature, newFeature,
         * window, newFeature)(featureModel,newFeature,"FORM"); double
         * result=runBestAlgorithm(newFeature); //println(result); //
         * double difference=0.0; if (result>=(this.bestResult)) { //Shrinking
         * //NO THRESHOLD because we are removing featureModel=newFeature;
         * difference=result-bestResult; bestResult=result; String
         * sDifferenceLabel=""+difference; if (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         */
        ///////////////////////////////////////////
        //NIVRESTANDARD EXCEPTION
        //ADD RDEP(Stack[0]) if it works add(Postag, RDEP(Stack[0]))
        //////////////////////////////////////////
        if (bestAlgorithm.equals("nivrestandard")) {

            String newFeature = "rdepAddFeature.xml";
            fg.addRdepWindow(featureModelBruteForce, newFeature, "DEPREL");
            String antFeature = newFeature;
            //fg.removeInputWindow(newFeature, newFeature, window, newFeature)(featureModel,newFeature,"FORM");
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);
            println("  " + result);
            println("  Default: " + bestResult);

            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */
                String newFeature2 = "rdepAddFeature2.xml";
                fg.addRdepWindow(featureModel, newFeature, "POSTAG");
                //fg.removeInputWindow(newFeature, newFeature, window, newFeature)(featureModel,newFeature,"FORM");
                println("Testing " + newFeature2 + " ...");
                double result2 = runBestAlgorithm(newFeature);
                println("  " + result2);
                println("  Default: " + bestResult);
                //println(result);
                double difference2 = 0.0;
                if (result2 > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
                    featureModelBruteForce = newFeature2;
                    bestResultBruteForce = result2;
                }
            }
        }


        /////////////////////////////////////////////////////////////////
        //STEP 3_ REPLICATING DEPREL--POSTAG
        /////////////////////////////////////////////////////////////////

        for (int j = 1; j <= 3; j++) {
            String newFeature = "replicateDeprelPostagSubtractingFeature" + j + ".xml";
            fg.replicatePostagDeprel(featureModelBruteForce, newFeature, "DEPREL", "", i);
            String antFeature = newFeature;
            //fg.removeInputWindow(newFeature, newFeature, window, newFeature)(featureModel,newFeature,"FORM");
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);
            println("  " + result);
            println("  Default: " + bestResult);

            if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
                featureModelBruteForce = newFeature;
                bestResultBruteForce = result;
            }
        }
    }

    private void backTrackingAfter3() {
        FeatureGenerator fg = new FeatureGenerator(writer);

        double result;
        double difference;
        //ADD FEATURES TO THE POOL. 
        //If we have 3...	
        //  {f1,f2,f3} => { {f1},{f2},{f3},{f1,f2},{f1,f3},{f2,f3},{f1,f2,f3} } 
        //
        String structure = "Stack";
        if (bestAlgorithm.contains("cov")) {
            structure = "Left";
        }

        String structureI = "Input";
        if (bestAlgorithm.contains("cov")) {
            structureI = "Right";
        }
        if (bestAlgorithm.contains("stack")) {
            structureI = "Lookahead";
        }
        ArrayList<String> pool = new ArrayList<>();
        if (bestAlgorithm.contains("stack")) {
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structure + "[0]), OutputColumn(DEPREL, ldep(" + structure + "[0])), OutputColumn(DEPREL, rdep(" + structure + "[0])))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, " + structure + "[1]), OutputColumn(DEPREL, ldep(" + structure + "[1])), OutputColumn(DEPREL, rdep(" + structure + "[1])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL, ldep(Stack[1])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL, ldep(Stack[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[2]), OutputColumn(DEPREL, ldep(Stack[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Lookahead[0]), OutputColumn(DEPREL, ldep(Stack[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Lookahead[0]), OutputColumn(DEPREL, rdep(Stack[0])))</feature>");
        }
        if (bestAlgorithm.contains("nivre")) {
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, Stack[0]))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Input[0]), OutputColumn(DEPREL, ldep(Input[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Input[0]), OutputColumn(DEPREL, rdep(Input[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Input[1]), OutputColumn(DEPREL, rdep(Input[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Input[2]), OutputColumn(DEPREL, rdep(Input[0])))</feature>");
        }
        if (bestAlgorithm.contains("cov")) {
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, ldep(Left[0])), OutputColumn(DEPREL, rdep(Left[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, Left[0]))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, ldep(Right[0])))</feature>");
            pool.add("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, Right[0]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Left[1]), InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]))</feature>");
            pool.add("\t\t<feature>Merge3(InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]), InputColumn(POSTAG, Right[3]))</feature>");

        }

        Iterator<String> it = pool.iterator();
        int a = 0;
        while (it.hasNext()) {
            String newFeature = "deprelMergeBT" + structure + structureI + a + ".xml";
            fg.addFeatureLineBefore(featureModelBruteForce, newFeature, it.next(), "Merge", "DEPREL");
            a++;
            String antFeature = newFeature;
            println("Testing " + newFeature + " ...");
            result = runBestAlgorithm(newFeature);

            println("  " + result);
            println("  Default: " + bestResult);
            if (result >= (Optimizer.bestResultBruteForce + threshold)) {
                Optimizer.featureModelBruteForce = antFeature; //dummy (just a matter of completity)
                difference = result - bestResultBruteForce;
                bestResultBruteForce = result;
                String sDifferenceLabel = String.format(pattern, difference);
                /*
                 * println("New best feature model:
                 * "+featureModelBruteForce); String s=""+this.bestResult; if
                 * (s.length()==4) s+="0";
                 *
                 * println("Incremental "+evaluationMeasure+"
                 * improvement: + "+sDifferenceLabel+"% ("+s+"%)");
                 */
            }
        }
    }

    private void predeccessorSuccessor(String window) {
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThemImprove;
        String newFeature;
        double result;
        boolean res;
        
        
        // 1. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String value = "Stack[0]";
        if (bestAlgorithm.contains("cov")) {
            value = "Left[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, value, "pred");
        result = runBestAlgorithm(newFeature);
        anyOfThemImprove = setBestResult(result, newFeature); //Shrinking

        // 2. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        fg.addPredSucc(featureModel, newFeature, window, value, "succ");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 3. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String val = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val = "Stack[1]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val = "Right[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, val, "pred");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 4. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String val2 = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val2 = "Stack[1]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val2 = "Right[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, val2, "succ");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        if (anyOfThemImprove && window.equals("POSTAG")) {
            predeccessorSuccessor("FORM");
        }
    }

    private void predeccessorSuccessor5Fold(String window) {
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThemImprove;
        double result;
        boolean res;

        // 1. predSuccFeaturepredStack.xml
        String newFeature = "predSuccFeaturepredStack.xml";
        String value = "Stack[0]";
        if (bestAlgorithm.contains("cov")) {
            value = "Left[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, value, "pred");
        result = runBestAlgorithm5Fold(newFeature);
        anyOfThemImprove = setBestResult(result, newFeature); //Shrinking

        // 2. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        fg.addPredSucc(featureModel, newFeature, window, value, "succ");
        result = runBestAlgorithm5Fold(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 3. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String val = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val = "Stack[1]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val = "Right[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, val, "pred");
        result = runBestAlgorithm5Fold(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 4. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String val2 = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val2 = "Stack[1]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val2 = "Right[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, val2, "succ");
        //fg.removeInputWindow(newFeature, newFeature, window, newFeature)(featureModel,newFeature,"FORM");
        result = runBestAlgorithm5Fold(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        if (anyOfThemImprove && window.equals("POSTAG")) {
            predeccessorSuccessor("FORM");
        }
    }

    private void predeccessorSuccessorRelaxedGreedy(String window) {
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThemImprove;
        double result;
        boolean res;

        // 1. predSuccFeaturepredStack.xml
        String newFeature = "predSuccFeaturepredStack.xml";
        String value = "Stack[0]";
        if (bestAlgorithm.contains("cov")) {
            value = "Left[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, value, "pred");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 2. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        fg.addPredSucc(featureModel, newFeature, window, value, "succ");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 3. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String val = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val = "Stack[1]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val = "Right[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, val, "pred");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        // 4. predSuccFeaturepredStack.xml
        newFeature = "predSuccFeaturepredStack.xml";
        String val2 = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val2 = "Stack[1]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val2 = "Right[0]";
        }
        fg.addPredSucc(featureModel, newFeature, window, val2, "succ");
        result = runBestAlgorithm(newFeature);
        res = setBestResult(result, newFeature); //Shrinking
        if (res == true) {
            anyOfThemImprove = true;
        }

        if (window.equals("POSTAG")) {
            predeccessorSuccessor("FORM");
        }
    }

    private void predeccessorSuccessorBruteForce(String window) {
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThemImprove = false;

        String antFeature = featureModelBruteForce;
        String newFeature = "predSuccFeaturepredStack.xml";
        String value = "Stack[0]";
        if (bestAlgorithm.contains("cov")) {
            value = "Left[0]";
        }
        fg.addPredSucc(featureModelBruteForce, newFeature, window, value, "pred");
        antFeature = newFeature;
        double result = runBestAlgorithm(newFeature);
        double difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        fg.addPredSucc(featureModelBruteForce, newFeature, window, value, "succ");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        value = "Stack[1]";
        if (bestAlgorithm.contains("cov")) {
            value = "Left[1]";
        }
        fg.addPredSucc(featureModelBruteForce, newFeature, window, value, "pred");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        fg.addPredSucc(featureModelBruteForce, newFeature, window, value, "succ");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        String val = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val = "Stack[2]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val = "Right[0]";
        }
        fg.addPredSucc(featureModelBruteForce, newFeature, window, val, "pred");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        String val2 = "Input[0]";
        if (bestAlgorithm.contains("stack")) {
            val2 = "Stack[2]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val2 = "Right[0]";
        }
        fg.addPredSucc(featureModelBruteForce, newFeature, window, val2, "succ");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        val = "Input[1]";
        if (bestAlgorithm.contains("stack")) {
            val = "Lookahead[0]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val = "Right[1]";
        }
        fg.addPredSucc(featureModelBruteForce, newFeature, window, val, "pred");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        antFeature = featureModelBruteForce;
        newFeature = "predSuccFeaturepredStack.xml";
        val2 = "Input[1]";
        if (bestAlgorithm.contains("stack")) {
            val2 = "Lookahead[0]"; //Input[0] corresponds to Stack[0] but Stack[0] is already tested above!
        }
        if (bestAlgorithm.contains("cov")) {
            val2 = "Right[1]";
        }
        fg.addPredSucc(featureModelBruteForce, newFeature, window, val2, "succ");
        antFeature = newFeature;
        result = runBestAlgorithm(newFeature);
        difference = 0.0;
        if (result > (Optimizer.bestResultBruteForce + threshold)) { //Shrinking
            anyOfThemImprove = true;
            featureModelBruteForce = newFeature;
            difference = result - bestResult;
            bestResultBruteForce = result;
        }

        if (window.equals("POSTAG")) {
            predeccessorSuccessorBruteForce("FORM");
        }
    }

    private void addNewFeaturesCpostagFeatsLemma(String window) {
        if (!window.equals("FEATS")) {
            println("\nAdding " + window + " features ...");
        }
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThem = false;
        ArrayList<Integer> stackValues = new ArrayList<>();
        ArrayList<Integer> inputLookValues = new ArrayList<>();

        String value;
        boolean keepGoing = true;
        for (int i = 0; i < 3; i++) {
            if (keepGoing) {
                String newFeature4 = "add" + InputLookAhead + window + i + ".xml";
                value = InputLookAhead + "[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    if (i == 0) {
                        value = "Stack[0]";
                    } else {
                        int j = i - 1;
                        value = InputLookAhead + "[" + j + "]";
                    }
                }
                fg.addFeature(featureModel, newFeature4, window, value);
                double result2 = runBestAlgorithm(newFeature4);
                keepGoing = setBestResult(result2, newFeature4); //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                    inputLookValues.add(i);
                }
            }
        }

        keepGoing = true;
        for (int i = 0; i < 3; i++) {
            if (keepGoing) {
                String newFeature4 = "addStack" + window + i + ".xml";
                value = "Stack[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    int j = i + 1;
                    value = "Stack[" + j + "]";
                }
                if (bestAlgorithm.contains("cov")) {
                    value = "Left[" + i + "]";
                }
                fg.addFeature(featureModel, newFeature4, window, value);
                double result2 = runBestAlgorithm(newFeature4);
                keepGoing = setBestResult(result2, newFeature4);  //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                    stackValues.add(i);
                }
            }
        }

        if (window.equals("FEATS") && anyOfThem) {
            //Merge Unsplit with POSTAG
            Iterator<Integer> it = inputLookValues.iterator();
            while (it.hasNext()) {
                Integer i = it.next();
                String newFeature4 = "addMergePostagFeats" + InputLookAhead + i + ".xml";
                fg.addMergeFeatures(featureModel, newFeature4, "POSTAG", "FEATS", InputLookAhead, "InputColumn", i);
                double result2 = runBestAlgorithm(newFeature4);
                setBestResult(result2, newFeature4); //Shrinking
            }

            Iterator<Integer> it2 = stackValues.iterator();
            while (it2.hasNext()) {
                Integer i = it2.next();
                String newFeature4 = "addMergePostagFeatsStack" + i + ".xml";
                fg.addMergeFeatures(featureModel, newFeature4, "POSTAG", "FEATS", "Stack", "InputColumn", i);
                double result2 = runBestAlgorithm(newFeature4);
                setBestResult(result2, newFeature4); //Shrinking
            }
        }
    }

    private void addNewFeaturesCpostagFeatsLemma5Fold(String window) {
        if (!window.equals("FEATS")) {
            println("\nAdding " + window + " features ...");
        }
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThem = false;
        ArrayList<Integer> stackValues = new ArrayList<>();
        ArrayList<Integer> inputLookValues = new ArrayList<>();

        String newFeature;
        double result;
        String value;
        boolean keepGoing = true;
        for (int i = 0; i < 3; i++) {
            if (keepGoing) {
                newFeature = "add" + InputLookAhead + window + i + ".xml";
                value = InputLookAhead + "[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    if (i == 0) {
                        value = "Stack[0]";
                    } else {
                        int j = i - 1;
                        value = InputLookAhead + "[" + j + "]";
                    }
                }
                fg.addFeature(featureModel, newFeature, window, value);
                result = runBestAlgorithm5Fold(newFeature);
                keepGoing = setBestResult(result, newFeature);  //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                    inputLookValues.add(i);
                }
            }
        }

        keepGoing = true;
        for (int i = 0; i < 3; i++) {
            if (keepGoing) {
                newFeature = "addStack" + window + i + ".xml";
                value = "Stack[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    int j = i + 1;
                    value = "Stack[" + j + "]";
                }
                if (bestAlgorithm.contains("cov")) {
                    value = "Left[" + i + "]";
                }
                fg.addFeature(featureModel, newFeature, window, value);
                result = runBestAlgorithm5Fold(newFeature);
                keepGoing = setBestResult(result, newFeature); //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                    stackValues.add(i);
                }
            }
        }

        if (window.equals("FEATS") && anyOfThem) {
            //Merge Unsplit with POSTAG
            Iterator<Integer> it = inputLookValues.iterator();
            while (it.hasNext()) {
                Integer i = it.next();
                newFeature = "addMergePostagFeats" + InputLookAhead + i + ".xml";
                fg.addMergeFeatures(featureModel, newFeature, "POSTAG", "FEATS", InputLookAhead, "InputColumn", i);
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }

            Iterator<Integer> it2 = stackValues.iterator();
            while (it2.hasNext()) {
                Integer i = it2.next();
                newFeature = "addMergePostagFeatsStack" + i + ".xml";
                fg.addMergeFeatures(featureModel, newFeature, "POSTAG", "FEATS", "Stack", "InputColumn", i);
                result = runBestAlgorithm5Fold(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void addNewFeaturesCpostagFeatsLemmaRelaxedGreedy(String window) {
        if (!window.equals("FEATS")) {
            println("\nAdding " + window + " features ... ");
        }

        FeatureGenerator fg = new FeatureGenerator(writer);
        
        String newFeature;
        double result;

        boolean anyOfThem = false;
        ArrayList<Integer> stackValues = new ArrayList<>();
        ArrayList<Integer> inputLookValues = new ArrayList<>();

        String value;
        String anterior = featureModel;
        boolean keepGoing;
        for (int i = 0; i < 4; i++) {
            newFeature = "add" + InputLookAhead + window + i + ".xml";
            value = InputLookAhead + "[" + i + "]";
            if (bestAlgorithm.contains("stack")) {
                if (i == 0) {
                    value = "Stack[0]";
                } else {
                    int j = i - 1;
                    value = InputLookAhead + "[" + j + "]";
                }
            }
            //anterior=featureModel;
            //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
            fg.addFeature(anterior, newFeature, window, value);
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            keepGoing =setBestResult(result, newFeature); //Shrinking
            if (keepGoing == true) {
                anyOfThem = true;
                inputLookValues.add(i);
            }
        }

        anterior = featureModel;
        for (int i = 0; i < 4; i++) {
            newFeature = "addStack" + window + i + ".xml";
            value = "Stack[" + i + "]";
            if (bestAlgorithm.contains("stack")) {
                int j = i + 1;
                value = "Stack[" + j + "]";
            }
            if (bestAlgorithm.contains("cov")) {
                value = "Left[" + i + "]";
            }
            //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
            //anterior=featureModel;
            fg.addFeature(anterior, newFeature, window, value);
            anterior = newFeature;
            result = runBestAlgorithm(newFeature);
            keepGoing = setBestResult(result, newFeature); //Shrinking
            if (keepGoing == true) {
                anyOfThem = true;
                stackValues.add(i);
            }
        }

        if (window.equals("FEATS")) {
            //Merge Unsplit with POSTAG
            Iterator<Integer> it = inputLookValues.iterator();
            anterior = featureModel;
            while (it.hasNext()) {
                Integer i = it.next();
                newFeature = "addMergePostagFeats" + InputLookAhead + i + ".xml";
                fg.addMergeFeatures(anterior, newFeature, "POSTAG", "FEATS", InputLookAhead, "InputColumn", i);
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
            anterior = featureModel;
            Iterator<Integer> it2 = stackValues.iterator();
            while (it2.hasNext()) {
                Integer i = it2.next();
                newFeature = "addMergePostagFeatsStack" + i + ".xml";
                fg.addMergeFeatures(anterior, newFeature, "POSTAG", "FEATS", "Stack", "InputColumn", i);
                anterior = newFeature;
                result = runBestAlgorithm(newFeature);
                setBestResult(result, newFeature); //Shrinking
            }
        }
    }

    private void addConjunctionFeatures(String window1, String window2) {
        FeatureGenerator fg = new FeatureGenerator(writer);
        
        String newFeature;
        double result;

        ArrayList<Integer> inputLookValues = fg.getListOfValuesFeatures(featureModel, window2, InputLookAhead);
        ArrayList<Integer> stackValues = fg.getListOfValuesFeatures(featureModel, window2, "Stack");
        /*
         * println(inputLookValues);
		println(stackValues);
         */

        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH OWN POSTAG
        /////////////////////////////////////////////////////////////////////////////////
        Iterator<Integer> it = inputLookValues.iterator();

        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeatures(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        Iterator<Integer> it2 = stackValues.iterator();

        /*
         * while(it2.hasNext()){ Integer i=it2.next(); String
         * newFeature4="addMerg"+window1+window2+"Stack"+i+".xml"; //(String
         * featureModel, String newFeature, String window, String inputStack,
         * String predSucc) fg.addMergeFeatures(featureModel, newFeature4,
         * "POSTAG", "FORM", "Stack", "InputColumn", i); 
         * //OJO!!! En este caso es Input porque para el primer test es con
         * NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS double
         * result2=runBestAlgorithm(newFeature4); //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature4; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         */


        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH POSTAG STACK[0]
        /////////////////////////////////////////////////////////////////////////////////

        it = inputLookValues.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + "S0" + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeaturesS0(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }
        //featureModel="addMergePostagS0FeatsInput0.xml";
        it2 = stackValues.iterator();
        while (it2.hasNext()) {
            Integer i = it2.next();
            newFeature = "addMerg" + window1 + "S0" + window2 + "Stack" + i + ".xml";
            fg.addMergeFeaturesS0(featureModel, newFeature, "POSTAG", "FORM", "Stack", "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH POSTAG INPUT[0]
        /////////////////////////////////////////////////////////////////////////////////

        it = inputLookValues.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + "I0" + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeaturesI0(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        it2 = stackValues.iterator();
        while (it2.hasNext()) {
            Integer i = it2.next();
            newFeature = "addMerg" + window1 + "I0" + window2 + "Stack" + i + ".xml";
            fg.addMergeFeaturesI0(featureModel, newFeature, "POSTAG", "FORM", "Stack", "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }



        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH MERGE(P(S[0],P(I[0]))
        /////////////////////////////////////////////////////////////////////////////////

        it = inputLookValues.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + "S0I0" + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeaturesMerge3(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        it2 = stackValues.iterator();
        while (it2.hasNext()) {
            Integer i = it2.next();
            newFeature = "addMerg" + window1 + "S0I0" + window2 + "Stack" + i + ".xml";
            fg.addMergeFeaturesMerge3(featureModel, newFeature, "POSTAG", "FORM", "Stack", "InputColumn", i);
            result = runBestAlgorithm(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }
    }

    private void addConjunctionFeatures5Fold(String window1, String window2) {
        FeatureGenerator fg = new FeatureGenerator(writer);
        
        String newFeature;
        double result;

        ArrayList<Integer> inputLookValues = fg.getListOfValuesFeatures(featureModel, window2, InputLookAhead);
        ArrayList<Integer> stackValues = fg.getListOfValuesFeatures(featureModel, window2, "Stack");
        /*
         * println(inputLookValues);
		println(stackValues);
         */

        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH OWN POSTAG
        /////////////////////////////////////////////////////////////////////////////////
        Iterator<Integer> it = inputLookValues.iterator();

        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeatures(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        Iterator<Integer> it2 = stackValues.iterator();

        /*
         * while(it2.hasNext()){ Integer i=it2.next(); String
         * newFeature4="addMerg"+window1+window2+"Stack"+i+".xml"; //(String
         * featureModel, String newFeature, String window, String inputStack,
         * String predSucc) fg.addMergeFeatures(featureModel, newFeature4,
         * "POSTAG", "FORM", "Stack", "InputColumn", i); 
         * //OJO!!! En este caso es Input porque para el primer test es con
         * NIVREEAGER. Habrá que usar LOOKAHEAD EN OTROS double
         * result2=runBestAlgorithm5Fold(newFeature4);
         * //println(result2);
         * //println("best:"+bestResult); // double difference2=0.0;
         * if (result2>(this.bestResult+threshold)) { //Shrinking
         * featureModel=newFeature4; difference2=result2-bestResult;
         * bestResult=result2; String sDifferenceLabel=""+difference2; if
         * (sDifferenceLabel.length()>5)
         * sDifferenceLabel=sDifferenceLabel.substring(0, 5);
         * println("New best feature model: "+featureModel); String
         * s=""+this.bestResult; if (s.length()==4) s+="0";
         *
         * println("Incremental "+evaluationMeasure+" improvement: +
         * "+sDifferenceLabel+"% ("+s+"%)");
         *
         * }
         * }
         */


        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH POSTAG STACK[0]
        /////////////////////////////////////////////////////////////////////////////////

        it = inputLookValues.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + "S0" + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeaturesS0(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }
        //featureModel="addMergePostagS0FeatsInput0.xml";
        it2 = stackValues.iterator();
        while (it2.hasNext()) {
            Integer i = it2.next();
            newFeature = "addMerg" + window1 + "S0" + window2 + "Stack" + i + ".xml";
            fg.addMergeFeaturesS0(featureModel, newFeature, "POSTAG", "FORM", "Stack", "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH POSTAG INPUT[0]
        /////////////////////////////////////////////////////////////////////////////////

        it = inputLookValues.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + "I0" + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeaturesI0(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        it2 = stackValues.iterator();
        while (it2.hasNext()) {
            Integer i = it2.next();
            newFeature = "addMerg" + window1 + "I0" + window2 + "Stack" + i + ".xml";
            fg.addMergeFeaturesI0(featureModel, newFeature, "POSTAG", "FORM", "Stack", "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }



        ////////////////////////////////////////////////////////////////////////////////////
        //MERGE WITH MERGE(P(S[0],P(I[0]))
        /////////////////////////////////////////////////////////////////////////////////

        it = inputLookValues.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            newFeature = "addMerg" + window1 + "S0I0" + window2 + String.valueOf(InputLookAhead) + i + ".xml";
            fg.addMergeFeaturesMerge3(featureModel, newFeature, "POSTAG", "FORM", InputLookAhead, "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }

        it2 = stackValues.iterator();
        while (it2.hasNext()) {
            Integer i = it2.next();
            newFeature = "addMerg" + window1 + "S0I0" + window2 + "Stack" + i + ".xml";
            fg.addMergeFeaturesMerge3(featureModel, newFeature, "POSTAG", "FORM", "Stack", "InputColumn", i);
            result = runBestAlgorithm5Fold(newFeature);
            setBestResult(result, newFeature); //Shrinking
        }
    }

    private void addSplitFeaturesFeatsRelaxedGreedy(String window) {

        println("\nAdding " + window + " features ... ");
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThem = false;

        String value;
        String anterior = featureModel;
        boolean keepGoing;
        for (int i = 0; i < 5; i++) {
            String newFeature4 = "addSplit" + InputLookAhead + window + i + ".xml";
            value = InputLookAhead + "[" + i + "]";
            if (bestAlgorithm.contains("stack")) {
                if (i == 0) {
                    value = "Stack[0]";
                } else {
                    int j = i - 1;
                    value = InputLookAhead + "[" + j + "]";
                }
            }
            //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
            fg.addSplitFeature(anterior, newFeature4, window, value);
            anterior = newFeature4;
            double result2 = runBestAlgorithm(newFeature4);
            keepGoing = setBestResult(result2, newFeature4); //Shrinking
            if (keepGoing == true) {
                anyOfThem = true;
            }
        }

        anterior = featureModel;
        for (int i = 0; i < 5; i++) {
            String newFeature4 = "addSplitStack" + window + i + ".xml";
            value = "Stack[" + i + "]";
            if (bestAlgorithm.contains("stack")) {
                int j = i + 1;
                value = "Stack[" + j + "]";
            }
            if (bestAlgorithm.contains("cov")) {
                value = "Left[" + i + "]";
            }
            //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
            fg.addSplitFeature(anterior, newFeature4, window, value);
            anterior = newFeature4;
            double result2 = runBestAlgorithm(newFeature4);
            keepGoing = setBestResult(result2, newFeature4); //Shrinking
            if (keepGoing == true) {
                anyOfThem = true;
            }
        }

        //if (anyOfThem) {
        addNewFeaturesCpostagFeatsLemmaRelaxedGreedy("FEATS");
        //MERGE
        //}
    }

    private void addSplitFeaturesFeats(String window) {
        
        println("\nAdding " + window + " features ... ");
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThem = false;

        String newFeature;
        double result;
        String value;
        boolean keepGoing = true;
        for (int i = 0; i < 4; i++) {
            if (keepGoing) {
                newFeature = "addSplit" + InputLookAhead + window + i + ".xml";
                value = InputLookAhead + "[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    if (i == 0) {
                        value = "Stack[0]";
                    } else {
                        int j = i - 1;
                        value = InputLookAhead + "[" + j + "]";
                    }

                }
                //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
                fg.addSplitFeature(featureModel, newFeature, window, value);
                result = runBestAlgorithm(newFeature);
                keepGoing = setBestResult(result, newFeature); //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                }
            }
        }

        keepGoing = true;
        for (int i = 0; i < 4; i++) {
            if (keepGoing) {
                newFeature = "addSplitStack" + window + i + ".xml";
                value = "Stack[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    int j = i + 1;
                    value = "Stack[" + j + "]";
                }
                if (bestAlgorithm.contains("cov")) {
                    value = "Left[" + i + "]";
                }
                //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
                fg.addSplitFeature(featureModel, newFeature, window, value);
                result = runBestAlgorithm(newFeature);
                keepGoing = setBestResult(result, newFeature); //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                }
            }
        }

        if (anyOfThem) {
            this.addNewFeaturesCpostagFeatsLemma("FEATS");
            //MERGE
        }
    }

    private void addSplitFeaturesFeats5Fold(String window) {
        
        println("\nAdding " + window + " features ... ");
        FeatureGenerator fg = new FeatureGenerator(writer);

        boolean anyOfThem = false;

        String newFeature;
        double result;
        String value;
        boolean keepGoing = true;
        for (int i = 0; i < 4; i++) {
            if (keepGoing) {
                newFeature = "addSplit" + InputLookAhead + window + i + ".xml";
                value = InputLookAhead + "[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    if (i == 0) {
                        value = "Stack[0]";
                    } else {
                        int j = i - 1;
                        value = InputLookAhead + "[" + j + "]";
                    }
                }
                //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
                fg.addSplitFeature(featureModel, newFeature, window, value);
                result = runBestAlgorithm5Fold(newFeature);
                keepGoing = setBestResult(result, newFeature); //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                }
            }
        }

        keepGoing = true;
        for (int i = 0; i < 4; i++) {
            if (keepGoing) {
                newFeature = "addSplitStack" + window + i + ".xml";
                value = "Stack[" + i + "]";
                if (bestAlgorithm.contains("stack")) {
                    int j = i + 1;
                    value = "Stack[" + j + "]";
                }
                if (bestAlgorithm.contains("cov")) {
                    value = "Left[" + i + "]";
                }
                //(String featureModel, String newFeature, String window, String inputStack, String predSucc)
                fg.addSplitFeature(featureModel, newFeature, window, value);
                result = runBestAlgorithm5Fold(newFeature);
                keepGoing = setBestResult(result, newFeature); //Shrinking
                if (keepGoing == true) {
                    anyOfThem = true;
                }
            }
        }

        if (anyOfThem) {
            this.addNewFeaturesCpostagFeatsLemma("FEATS");
            //MERGE
        }
    }

    private double runBestAlgorithm(String feature) {

        //String language, double np, CoNLLHandler ch, String trainingCorpus, String rootHandling
        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }

        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
        double result = 0.0;
        try {
            if (bestAlgorithm.equals("nivreeager")) {
                result = at.executeNivreEager(feature);
            }

            if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
                //result=at.executeNivreEager(feature);
                result = at.executeNivreStandard(feature);
            }

            if (bestAlgorithm.equals("covnonproj")) {
                //result=at.executeNivreEager(feature);
                result = at.executeCovingtonNonProjective(feature);
            }

            if (bestAlgorithm.equals("covproj")) {
                //result=at.executeNivreEager(feature);
                result = at.executeCovingtonProjective(feature);
            }

            if (bestAlgorithm.equals("stackproj")) {
                //result=at.executeNivreEager(feature);
                result = at.executeStackProjective(feature);
            }

            if (bestAlgorithm.equals("stackeager")) {
                //result=at.feature);
                result = at.executestackEager(feature);
            }

            if (bestAlgorithm.equals("stacklazy")) {
                //result=at.executeNivreEager(feature);
                result = at.executeStackLazy(feature);
            }
        } catch (Exception e) {
            println("Feature not valid.");
        }
        return result;
    }

    private double runBestAlgorithm5Fold(String feature) {
        //String language, double np, CoNLLHandler ch, String trainingCorpus, String rootHandling

        int contExitos = 0;

        File f = new File(feature);
        if (f.exists()) {
            int ind = feature.lastIndexOf(".xml");
            if (ind != -1) {
                feature = feature.substring(0, ind);
            }
            
            CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
            AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
            double result = 0.0;
            double max = bestResult;
            double sum = 0.0;
            for (int i = 1; i < 6; i++) {
                try {
                    if (bestAlgorithm.equals("nivreeager")) {
                        result = at.executeNivreEagerTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }

                    if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
                        //result=at.executeNivreEager(feature);
                        result = at.executeNivreStandardTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }

                    if (bestAlgorithm.equals("covnonproj")) {
                        //result=at.executeNivreEager(feature);
                        result = at.executeCovingtonNonProjectiveTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }

                    if (bestAlgorithm.equals("covproj")) {
                        //result=at.executeNivreEager(feature);
                        result = at.executeCovingtonProjectiveTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }

                    if (bestAlgorithm.equals("stackproj")) {
                        //result=at.executeNivreEager(feature);
                        result = at.executeStackProjectiveTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }

                    if (bestAlgorithm.equals("stackeager")) {
                        //result=at.feature);
                        result = at.executestackEagerTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }

                    if (bestAlgorithm.equals("stacklazy")) {
                        //result=at.executeNivreEager(feature);
                        result = at.executeStackLazyTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                    }
                } catch (Exception e) {
                    println("Feature not valid.");
                }
                String s = String.format(pattern, result);
                println("    Fold " + i + ": " + s + "%");
                sum += result;
                if (result >= bestResult) {
                    contExitos++;
                    if (max < result) {
                        max = result;
                    }
                }
            }
            sum = sum / 5;
            String cad = String.valueOf(sum);
            if (cad.length() > 4) {
                int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
                int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

                if ((cad.length() > 5) && (a == 9)) {
                    cad = cad.substring(0, 5);
                } else if ((cad.length() > 5)) {
                    if (cad.charAt(5) > 5) {
                        char c = cad.charAt(4);
                        Integer in = Integer.parseInt(String.valueOf(c));
                        in++;
                        cad = cad.substring(0, 4);

                        cad = cad + String.valueOf(in);
                    } else {
                        cad = cad.substring(0, 5);
                    }
                }
            }
            if (cad.length() == 4) {
                cad += "0";
            }
            //if (cad.length()>=5) cad=cad.substring(0,5);
            sum = Double.parseDouble(cad);
            println("    Average: " + cad + "%");
            if (Optimizer.chooseAverage) {
                if (sum >= Optimizer.bestResult) {
                    return sum;
                }
            } else if (Optimizer.chooseMajority) {
                if (contExitos >= 3) {
                    return sum;
                }
            } else if (Optimizer.chooseAllOfThem) {
                if (contExitos >= 5) {
                    return sum;
                }
            }
            return 0.0;
        }
        return 0.0;
    }

    private double runAlgorithm5Fold(String feature, String algorithm) {

        //String language, double np, CoNLLHandler ch, String trainingCorpus, String rootHandling
        int contExitos = 0;

        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }
            
        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
        double result = 0.0;
        double max = bestResult;
        double sum = 0.0;
        for (int i = 1; i < 6; i++) {
            try {
                if (algorithm.equals("nivreeager")) {
                    result = at.executeNivreEagerTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }

                if (algorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
                    result = at.executeNivreStandardTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }

                if (algorithm.equals("covnonproj")) {
                    result = at.executeCovingtonNonProjectiveTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }

                if (algorithm.equals("covproj")) {
                    result = at.executeCovingtonProjectiveTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }

                if (algorithm.equals("stackproj")) {
                    result = at.executeStackProjectiveTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }

                if (algorithm.equals("stackeager")) {
                    result = at.executestackEagerTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }

                if (algorithm.equals("stacklazy")) {
                    result = at.executeStackLazyTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll");
                }
            } catch (Exception e) {
                println("Feature not valid.");
            }
            String s = String.format(pattern, result);
            println("    Fold " + i + ": " + s + "%");
            sum += result;
            if (result >= bestResult) {
                contExitos++;
                if (max < result) {
                    max = result;
                }
            }
        }
        sum = sum / 5;
        String cad = String.valueOf(sum);
        if (cad.length() > 4) {
            int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
            int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

            if ((cad.length() > 5) && (a == 9)) {
                cad = cad.substring(0, 5);
            } else if ((cad.length() > 5)) {
                if (cad.charAt(5) > 5) {
                    char c = cad.charAt(4);
                    Integer in = Integer.parseInt(String.valueOf(c));
                    in++;
                    cad = cad.substring(0, 4);

                    cad = cad + String.valueOf(in);
                } else {
                    cad = cad.substring(0, 5);
                }
            }
        }
        if (cad.length() == 4) {
            cad += "0";
        }
        //if (cad.length()>=5) cad=cad.substring(0,5);
        sum = Double.parseDouble(cad);
        println("    Average: " + cad + "%");
        if (Optimizer.chooseAverage) {

            if (sum >= Optimizer.bestResult) {
                return sum;
            }
        } else if (Optimizer.chooseMajority) {
            if (contExitos >= 3) {
                return sum;
            }
        } else if (Optimizer.chooseAllOfThem) {
            if (contExitos >= 5) {
                return sum;
            }
        }
        return 0.0;
    }

    private double runCovingtonNonProjectivePPAllowShiftAllowRoot5Fold(String feature, String head, boolean allow_shift, boolean allow_root) {
        int contExitos = 0;

        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }            

        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
        double result = 0.0;
        double max = bestResult;
        double sum = 0.0;
        for (int i = 1; i < 6; i++) {
            try {
                result = at.executeCovingtonNonProjectivePPAllowShiftAllowRootTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", head, allow_shift, allow_root);
            } catch (Exception e) {
                println("Feature not valid.");
            }
            String s = String.format(pattern, result);
            println("    Fold " + i + ": " + s + "%");
            sum += result;
            if (result >= bestResult) {
                contExitos++;
                if (max < result) {
                    max = result;
                }
            }
        }
        sum = sum / 5;
        String cad = String.valueOf(sum);
        if (cad.length() > 4) {
            int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
            int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

            if ((cad.length() > 5) && (a == 9)) {
                cad = cad.substring(0, 5);
            } else if ((cad.length() > 5)) {
                if (cad.charAt(5) > 5) {
                    char c = cad.charAt(4);
                    Integer in = Integer.parseInt(String.valueOf(c));
                    in++;
                    cad = cad.substring(0, 4);

                    cad = cad + String.valueOf(in);
                } else {
                    cad = cad.substring(0, 5);
                }
            }
        }
        if (cad.length() == 4) {
            cad += "0";
        }
        //if (cad.length()>=5) cad=cad.substring(0,5);
        sum = Double.parseDouble(cad);
        println("    Average: " + cad + "%");
        if (Optimizer.chooseAverage) {

            if (sum >= Optimizer.bestResult) {
                return sum;
            }
        } else if (Optimizer.chooseMajority) {
            if (contExitos >= 3) {
                return sum;
            }
        } else if (Optimizer.chooseAllOfThem) {
            if (contExitos >= 5) {
                return sum;
            }
        }
        return 0.0;
    }

    private double runCovingtonProjectivePPAllowShiftAllowRoot5Fold(String feature, String head, boolean allow_shift, boolean allow_root) {
        int contExitos = 0;

        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }
            
        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
        double result = 0.0;
        double max = bestResult;
        double sum = 0.0;
        for (int i = 1; i < 6; i++) {
            try {
                result = at.executeCovingtonProjectivePPAllowShiftAllowRootTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", head, allow_shift, allow_root);
            } catch (Exception e) {
                println("Feature not valid.");
            }
            String s = String.format(pattern, result);
            println("    Fold " + i + ": " + s + "%");
            sum += result;
            if (result >= bestResult) {
                contExitos++;
                if (max < result) {
                    max = result;
                }
            }
        }
        sum = sum / 5;
        String cad = String.valueOf(sum);
        if (cad.length() > 4) {
            int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
            int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

            if ((cad.length() > 5) && (a == 9)) {
                cad = cad.substring(0, 5);
            } else if ((cad.length() > 5)) {
                if (cad.charAt(5) > 5) {
                    char c = cad.charAt(4);
                    Integer in = Integer.parseInt(String.valueOf(c));
                    in++;
                    cad = cad.substring(0, 4);

                    cad = cad + String.valueOf(in);
                } else {
                    cad = cad.substring(0, 5);
                }
            }
        }
        if (cad.length() == 4) {
            cad += "0";
        }
        //if (cad.length()>=5) cad=cad.substring(0,5);
        sum = Double.parseDouble(cad);
        println("    Average: " + cad + "%");
        if (Optimizer.chooseAverage) {

            if (sum >= Optimizer.bestResult) {

                return sum;
            }
        } else if (Optimizer.chooseMajority) {
            if (contExitos >= 3) {
                return sum;
            }
        } else if (Optimizer.chooseAllOfThem) {
            if (contExitos >= 5) {
                return sum;
            }
        }
        return 0.0;
    }

    private double runCovingtonNonProjectiveAllowShiftAllowRoot5Fold(String feature, boolean allow_shift, boolean allow_root) {
        int contExitos = 0;

        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }
            
        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
        double result = 0.0;
        double max = bestResult;
        double sum = 0.0;
        for (int i = 1; i < 6; i++) {
            try {
                //result=at.executeCovingtonNonProjectivePPAllowShiftAllowRootTestTrain(featureModel,head,allow_shift,allow_root,"fold_train_"+i+".conll","fold_test_"+i+".conll");
                result = at.executeCovingtonNonProjectiveAllowShiftAllowRootTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", allow_shift, allow_root);
            } catch (Exception e) {
                println("Feature not valid.");
            }
            String s = String.format(pattern, result);
            println("    Fold " + i + ": " + s + "%");
            sum += result;
            if (result >= bestResult) {
                contExitos++;
                if (max < result) {
                    max = result;
                }
            }
        }
        sum = sum / 5;
        String cad = String.valueOf(sum);
        if (cad.length() > 4) {
            int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
            int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

            if ((cad.length() > 5) && (a == 9)) {
                cad = cad.substring(0, 5);
            } else if ((cad.length() > 5)) {
                if (cad.charAt(5) > 5) {
                    char c = cad.charAt(4);
                    Integer in = Integer.parseInt(String.valueOf(c));
                    in++;
                    cad = cad.substring(0, 4);

                    cad = cad + String.valueOf(in);
                } else {
                    cad = cad.substring(0, 5);
                }
            }
        }
        if (cad.length() == 4) {
            cad += "0";
        }
        //if (cad.length()>=5) cad=cad.substring(0,5);
        sum = Double.parseDouble(cad);
        println("    Average: " + cad + "%");
        if (Optimizer.chooseAverage) {

            if (sum >= Optimizer.bestResult) {
                //println("    (Av:"+sum+")");
                return sum;
            }
        } else if (Optimizer.chooseMajority) {
            if (contExitos >= 3) {
                return sum;
            }
        } else if (Optimizer.chooseAllOfThem) {
            if (contExitos >= 5) {
                return sum;
            }
        }
        return 0.0;
    }

    private double runCovingtonProjectiveAllowShiftAllowRoot5Fold(String feature, boolean allow_shift, boolean allow_root) {
        int contExitos = 0;

        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }
            
        CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
        AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
        double result = 0.0;
        double max = bestResult;
        double sum = 0.0;
        for (int i = 1; i < 6; i++) {
            try {
                //result=at.executeCovingtonNonProjectivePPAllowShiftAllowRootTestTrain(featureModel,head,allow_shift,allow_root,"fold_train_"+i+".conll","fold_test_"+i+".conll");
                result = at.executeCovingtonProjectiveAllowShiftAllowRootTestTrain(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", allow_shift, allow_root);
            } catch (Exception e) {
                println("Feature not valid.");
            }
            String s = String.format(pattern, result);
            println("    Fold " + i + ": " + s + "%");
            sum += result;
            if (result >= bestResult) {
                contExitos++;
                if (max < result) {
                    max = result;
                }
            }
        }
        sum = sum / 5;
        String cad = String.valueOf(sum);
        if (cad.length() > 4) {
            int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
            int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

            if ((cad.length() > 5) && (a == 9)) {
                cad = cad.substring(0, 5);
            } else if ((cad.length() > 5)) {
                if (cad.charAt(5) > 5) {
                    char c = cad.charAt(4);
                    Integer in = Integer.parseInt(String.valueOf(c));
                    in++;
                    cad = cad.substring(0, 4);

                    cad = cad + String.valueOf(in);
                } else {
                    cad = cad.substring(0, 5);
                }
            }
        }
        if (cad.length() == 4) {
            cad += "0";
        }
        //if (cad.length()>=5) cad=cad.substring(0,5);
        sum = Double.parseDouble(cad);
        println("    Average: " + cad + "%");
        if (Optimizer.chooseAverage) {

            if (sum >= Optimizer.bestResult) {
                return sum;
            }
        } else if (Optimizer.chooseMajority) {
            if (contExitos >= 3) {
                return sum;
            }
        } else if (Optimizer.chooseAllOfThem) {
            if (contExitos >= 5) {
                return sum;
            }
        }
        return 0.0;
    }

    /*
     * private double runAlgorithm5Fold(String feature, String algorithm) { //
     * //String language, double np,
     * CoNLLHandler ch, String trainingCorpus, String rootHandling
     *
     * int contExitos=0;
     *
     * File f=new File(feature); if (f.exists()) { CoNLLHandler ch=new
     * CoNLLHandler(trainingCorpus); AlgorithmTester at=new
     * AlgorithmTester("lang",this.percentage,ch,trainingCorpus,optionMenosR);
     * double result=0.0; double max=bestResult; double sum=0.0; for (int
     * i=1;i<6;i++) { try { if (algorithm.equals("nivreeager")) {
     * //result=at.executeNivreEagerTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * result=at.executeNivreEagerTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * }
     *
     * if (algorithm.equals("nivrestandard")) { //A PARTIR DE LA PROXIMA VERSION
     * PONER EL FEATURE CORRESPONDIENTE!!!
     * //result=at.executeNivreEager(feature);
     * result=at.executeNivreStandardTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * }
     *
     * if (algorithm.equals("covnonproj")) {
     * //result=at.executeNivreEager(feature);
     * result=at.executeCovingtonNonProjectiveTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * }
     *
     * if (algorithm.equals("covproj")) {
     * //result=at.executeNivreEager(feature);
     * result=at.executeCovingtonProjectiveTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * }
     *
     * if (algorithm.equals("stackproj")) {
     * //result=at.executeNivreEager(feature);
     * result=at.executeStackProjectiveTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * }
     *
     * if (algorithm.equals("stackeager")) { //result=at.feature);
     * result=at.executestackEagerTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * }
     *
     * if (algorithm.equals("stacklazy")) {
     * //result=at.executeNivreEager(feature);
     * result=at.executeStackLazyTestTrain(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
     * } }catch(Exception e) { println("Feature not valid."); }
     * println(" Fold "+i+":"+result); sum+=result; if
     * (result>=bestResult) { contExitos++; if (max<result) max=result; } }
     * sum=sum/5; if (Optimizer.chooseAverage) {
     *
     * if (sum>=Optimizer.bestResult) { println(" (Av:"+sum+")");
     * return sum; } } else if (Optimizer.chooseMajority) { if (contExitos>=3) {
     * return sum; } } else if (Optimizer.chooseAllOfThem) { if (contExitos>=5)
     * { return sum; } } return 0.0; } return 0.0;
	}
     */
    private double runAlgorithm5FoldPPOption(String feature, String algorithm) {

        //String language, double np, CoNLLHandler ch, String trainingCorpus, String rootHandling

        int contExitos = 0;

        File f = new File(feature);
        if (f.exists()) {
            int ind = feature.lastIndexOf(".xml");
            if (ind != -1) {
                feature = feature.substring(0, ind);
            }
            
            CoNLLHandler ch = new CoNLLHandler(trainingCorpus, writer);
            AlgorithmTester at = new AlgorithmTester("lang", this.percentage, ch, trainingCorpus, optionMenosR);
            double result = 0.0;
            double max = bestResult;
            double sum = 0.0;
            for (int i = 1; i < 6; i++) {
                try {
                    if (algorithm.equals("nivreeager")) {
                        result = at.executeNivreEagerTestTrainPPOption(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", Optimizer.ppOption);
                    }

                    if (algorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
                        //result=at.executeNivreEager(feature);
                        result = at.executeNivreStandardTestTrainPPOption(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", Optimizer.ppOption);
                    }

                    /*
                     * if (bestAlgorithm.equals("covnonproj")) {
                     * //result=at.executeNivreEager(feature);
                     * result=at.executeCovingtonNonProjectiveTestTrainPPOption(feature,"fold_train_"+i+".conll","fold_test_"+i+".conll");
				}
                     */

                    if (algorithm.equals("covproj")) {
                        result = at.executeCovingtonProjectiveTestTrainPPOption(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", Optimizer.ppOption);
                    }

                    if (algorithm.equals("stackproj")) {
                        result = at.executeStackProjectiveTestTrainPPOption(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", Optimizer.ppOption);
                    }

                    if (algorithm.equals("stackeager")) {
                        result = at.executestackEagerTestTrainPPOption(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", Optimizer.ppOption);
                    }

                    if (algorithm.equals("stacklazy")) {
                        result = at.executeStackLazyTestTrainPPOption(feature, "fold_train_" + i + ".conll", "fold_test_" + i + ".conll", Optimizer.ppOption);
                    }
                } catch (Exception e) {
                    println("Feature not valid.");
                }
                String s = String.format(pattern, result);
                println("    Fold " + i + ": " + s + "%");
                sum += result;
                if (result >= bestResult) {
                    contExitos++;
                    if (max < result) {
                        max = result;
                    }
                }
            }
            sum = sum / 5;
            String cad = String.valueOf(sum);
            if (cad.length() > 4) {
                int a = Integer.parseInt(String.valueOf(cad.charAt(4)));
                int b = Integer.parseInt(String.valueOf(cad.charAt(3)));

                if ((cad.length() > 5) && (a == 9)) {
                    cad = cad.substring(0, 5);
                } else if ((cad.length() > 5)) {
                    if (cad.charAt(5) > 5) {
                        char c = cad.charAt(4);
                        Integer in = Integer.parseInt(String.valueOf(c));
                        in++;
                        cad = cad.substring(0, 4);

                        cad = cad + String.valueOf(in);
                    } else {
                        cad = cad.substring(0, 5);
                    }
                }
            }
            if (cad.length() == 4) {
                cad += "0";
            }
            //if (cad.length()>=5) cad=cad.substring(0,5);
            sum = Double.parseDouble(cad);
            println("    Average: " + cad + "%");
            if (Optimizer.chooseAverage) {

                if (sum >= Optimizer.bestResult) {
                    return sum;
                }
            } else if (Optimizer.chooseMajority) {
                if (contExitos >= 3) {
                    return sum;
                }
            } else if (Optimizer.chooseAllOfThem) {
                if (contExitos >= 5) {
                    return sum;
                }
            }
            return 0.0;
        }
        return 0.0;
    }

    private void loadPhase3Results(String pathTrainingSet) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("phase3_logFile.txt"));
            try {
                int contador = 0;
                while (br.ready()) {
                    String line;
                    try {
                        line = br.readLine();
                        StringTokenizer st = new StringTokenizer(line, ":");
                        String tok = "";
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        }
                        contador++;
                        if (contador == 1) {
                            if (pathTrainingSet.equals(tok)) {
                                this.setTrainingCorpus(tok);
                            } else {
                                try {
                                    throw new PathNotFoundException(writer);
                                } catch (PathNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (contador == 2) {
                            Integer nt = Integer.parseInt(tok);
                            this.setNumbTokens(nt);
                        }
                        if (contador == 3) {
                            Integer nt = Integer.parseInt(tok);
                            this.setNumbSentences(nt);
                        }
                        if (contador == 4) {
                            Double nt = Double.parseDouble(tok);
                            this.setPercentage(nt);
                            if (nt == 0.0) {
                                this.setNoNonProjective(true);
                            } else {
                                if (nt > 15) {
                                    this.setSubstantialNonProjective(true);
                                } else {
                                    this.setSmallCaseBothThings(true);
                                }
                            }
                        }
                        if (contador == 5) {
                            Integer it = Integer.parseInt(tok);
                            if (it > 0) {
                                this.setDanglingPunctuation(true);
                            }
                            this.setNumbDanglingCases(it);
                        }
                        if (contador == 6) {
                            Double nt = Double.parseDouble(tok);
                            Optimizer.setBestResult(nt);
                        }
                        if (contador == 7) {
                            Double nt = Double.parseDouble(tok);
                            Optimizer.setDefaultBaseline(nt);
                        }
                        if (contador == 8) {
                            Integer nt = Integer.parseInt(tok);
                            Optimizer.numRootLabels = nt;
                        }
                        if (contador == 9) {
                            Optimizer.javaHeapValue = tok;
                        }

                        if (contador == 10) {
                            if (tok.equals("true")) {
                                cposEqPos = true;
                            } else {
                                cposEqPos = false;
                            }
                        }
                        if (contador == 11) {
                            if (tok.equals("true")) {
                                lemmaBlank = true;
                            } else {
                                lemmaBlank = false;
                            }
                        }
                        if (contador == 12) {
                            if (tok.equals("true")) {
                                featsBlank = true;
                            } else {
                                featsBlank = false;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        BufferedReader brOpt;
        try {
            brOpt = new BufferedReader(new FileReader("phase3_optFile.txt"));
            try {
                int contador = 0;
                boolean grl = false;
                boolean pcr = false;
                boolean algo = false;
                boolean pp = false;
                boolean cs = false;
                boolean cr = false;
                boolean menosr = false;
                boolean feature = false;
                while (brOpt.ready()) {
                    String line;
                    try {
                        line = brOpt.readLine();
                        StringTokenizer st = new StringTokenizer(line, ":");
                        grl = false;
                        pcr = false;
                        algo = false;
                        pp = false;
                        cr = false;
                        cs = false;
                        menosr = false;
                        feature = false;
                        if (line.contains("-grl")) {
                            grl = true;
                        }
                        if (line.contains("-pcr")) {
                            pcr = true;
                        }
                        if (line.contains("-a")) {
                            algo = true;
                        }
                        if (line.contains("-pp")) {
                            pp = true;
                        }
                        if (line.contains("-cs")) {
                            cs = true;
                        }
                        if (line.contains("-cr")) {
                            cr = true;
                        }
                        if (line.contains("-r")) {
                            menosr = true;
                        }
                        if (line.contains("-F")) {
                            feature = true;
                        }
                        String tok = "";
                        while (st.hasMoreTokens()) {
                            tok = st.nextToken();
                        }
                        contador++;


                        if (grl) {
                            Optimizer.setOptionGRL(tok);
                            grl = false;
                        }
                        if (pcr) {
                            Optimizer.setPcrOption(tok);
                            pcr = false;
                        }
                        if (algo) {
                            this.setBestAlgorithm(tok);
                            algo = false;
                        }
                        if (pp) {
                            Optimizer.setPpOption(tok);
                            pp = false;
                        }
                        if (cs) {
                            allow_shift = true;
                            cs = false;
                        }
                        if (cr) {
                            allow_root = true;
                            cr = false;
                        }
                        if (menosr) {
                            optionMenosR = tok;
                            menosr = false;
                        }
                        if (feature) {
                            featureModel = tok;
                            feature = false;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private void runPhase4() {

        ShowIntroduction();

        println("PHASE 4: LIBRARY CONFIGURATION PARAMETERS TESTING");

        println("MaltOptimizer is going to test the best training library configuration:\n");

        int cParameter = 1;
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        lo.setLibraryOptions("-s_4_-c_0.01");
        while (cParameter < 10) {
            cParameter++;

            println("Testing the: " + lo.getLibraryOptions());
            //Double d=cParameter;

            double result = runBestAlgorithm(featureModel);
            println(String.valueOf(result));
            println("best:" + bestResult);
            //
            double difference;
            if (result > (Optimizer.bestResult)) {
                Optimizer.libraryValue = lo.getLibraryOptions();
                difference = result - bestResult;
                bestResult = result;
                String sDifferenceLabel = String.format(pattern, difference);
                println("New best Library Configuration Parameter: " + libraryValue);
                String s = String.format(pattern, bestResult);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
            if (lo.getLibraryOptions().equals("-s_4_-c_0.01")) {
                lo.setLibraryOptions("-s_4_-c_0.1");
            } else {
                lo.incrementC(0.1);
            }
        }

        createLogFile(4);
        createOptionFile(4);

        println("\nThe best Library configuration is: " + libraryValue);
        println("-----------------------------------------------------------------------------");
        println("MaltOptimizer has completed the library parameter configuration phase using your training set,");
        println("it saved the results for future use in /phase4_logFile.txt. Updated MaltParser ");
        println("options can be found in /phase4_optFile.txt. ");
        println("");
    }

    private void runPhase4SimplifiedVersion() {

        /*
         * ShowIntroduction();
         */

        //println("PHASE 4: LIBRARY CONFIGURATION PARAMETERS TESTING");
        //println(this.featureModel);
        //println("MaltOptimizer is going to test the best training library configuration:\n");
        /**
         *
         */
        int cParameter = 1;
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        lo.setLibraryOptions("-s_4_-c_0.01");
        while (cParameter < 6) {
            cParameter++;
            String anterior = lo.getLibraryOptions();

            println("Testing: C=" + lo.getC());
            //Double d=cParameter;
            //println(cParameter);
            double result = runBestAlgorithm(featureModel);
            String res = String.format(pattern, result);
            String bestRes = String.format(pattern, bestResult);
            println(res + " (Best:" + bestRes + ")");
            //
            double difference;
            if (result > (Optimizer.bestResult)) {
                Optimizer.libraryValue = lo.getLibraryOptions();
                difference = result - bestResult;
                bestResult = result;
                String sDifferenceLabel = String.format(pattern, difference);
                println("New best Library Configuration Parameter: " + libraryValue);
                String s = String.format(pattern, bestResult);
                println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
            if (lo.getLibraryOptions().equals("-s_4_-c_0.01")) {
                lo.setLibraryOptions("-s_4_-c_0.2");
            } else {
                lo.incrementC(0.2);
            }
        }
        lo.setLibraryOptions(Optimizer.libraryValue);
        println("\nBest C value: " + lo.getC());
        /*
         * println("-----------------------------------------------------------------------------");
         * println("MaltOptimizer has completed the library parameter
         * configuration phase using your training set,");
         * println("it saved the results for future use in
         * /phase4_logFile.txt. Updated MaltParser ");
         * println("options can be found in /phase4_optFile.txt. ");
		println("");
         */
    }

    public ArrayList<String> getThreeFrequent(HashMap<String, Double> roots) {
        Set<String> set = roots.keySet();
        String max = "";
        Double maxD = 0.0;
        String sec = "";
        Double secD = 0.0;
        String third = "";
        Double thirdD = 0.0;

        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String r = it.next();
            Double d = roots.get(r) * 100;
            if (d > maxD) {
                if (maxD > secD) {
                    third = sec;
                    thirdD = secD;
                    sec = max;
                    secD = maxD;
                }
                max = r;
                maxD = d;
            } else {
                if (d > secD) {
                    if (secD > thirdD) {
                        third = sec;
                        thirdD = secD;
                    }
                    sec = r;
                    secD = d;
                } else {
                    if (d > thirdD) {
                        third = r;
                        thirdD = d;
                    }
                }
            }
        }
        ArrayList<String> output = new ArrayList<>();
        output.add(max);
        output.add(sec);
        if (third != null && !third.equals("")) {
            output.add(third);
        }
        return output;
    }

    private void showDefault() {

        ShowIntroduction();
        
        StringBuilder text = new StringBuilder();
        
        text.append("Usage:\n");
        text.append("1. Run Optimization:\n");
        text.append("java -jar MaltOptimizer.jar -p <phase number> -m <path to MaltParser> -c <path to training corpus> [-v <validation method> -o <path to log file>]\n");
        text.append("2. for more help and options:\n");
        text.append("java -jar malt.jar -h");
        
        println(text.toString());
    }

    private void showHelp() {
        showDefault();
        
        StringBuilder help = new StringBuilder();
        

        help.append("-----------------------------------------------------------------------------\n");
        help.append("PHASE 1: DATA CHARACTERISTICS\n");

        help.append("In the data analysis, MaltOptimizer gather information about the following properties of the training set:\n");
        help.append("Number of words/sentences\n");
        help.append("Percentage of non-projective arcs/trees\n");
        help.append("Existence of ''covered roots'' (arcs spanning tokens with HEAD = 0)\n");
        help.append("Frequency of labels used for tokens with HEAD = 0\n");
        help.append("Existence of non-empty feature values in the LEMMA and FEATS columns\n");
        help.append("Identity (or not) of feature values in the CPOSTAG and POSTAG columns\n\n");

        help.append("Usage:\n");
        help.append("java -jar MaltOptimizer.jar -p 1 -m <-MaltParser jar path-> -c <path to training corpus> [-v <validation method> -o <path to log file>]\n");

        help.append("-----------------------------------------------------------------------------\n");
        help.append("PHASE 2: PARSING ALGORITHM SELECTION\n");
        help.append("MaltOptimizer selects the best algorithm implemented in MaltParser for the input training set.\n\n");

        help.append("Usage:\n");
        help.append("java -jar MaltOptimizer.jar -p 2 -m <-MaltParser jar path-> -c <path to training corpus> [-v <validation method> -o <path to log file>]\n");

        help.append("------------------------------------------------------------------------------\n");

        help.append("PHASE 3: FEATURE SELECTION\n");

        help.append("MaltOptimizer tests the following feature selection experiments:\n");
        help.append("1. Tune the window of POSTAG n-grams over the parser state\n");
        help.append("2. Tune the window of FORM features over the parser state\n");
        help.append("3. Tune DEPREL and POSTAG features over the partially built dependency tree\n");
        help.append("4. Add POSTAG and FORM features over the input string\n");
        help.append("5. Add CPOSTAG, FEATS, and LEMMA features if available\n");
        help.append("6. Add conjunctions of POSTAG and FORM features\n\n");

        help.append("Usage:\n");
        help.append("java -jar MaltOptimizer.jar -p 3 -m <-MaltParser jar path-> -c <path to training corpus> [-v <validation method> -o <path to log file>]\n");

        help.append("------------------------------------------------------------------------------\n");

        help.append("EXTRA OPTIONS \n");

        help.append("evaluation_measure	 -e	las	Labeled Attachment Score (DEFAULT)\n");
        help.append("\t\t\t\tuas	Unlabeled Attachment Score\n");
        help.append("\t\t\t\tlcm	Labeled Complete Match\n");
        help.append("\t\t\t\tucm	Unlabeled Complete Match\n\n");

        help.append("Usage:\n");
        help.append("java -jar MaltOptimizer.jar -p <-phase number-> -m <-MaltParser jar path-> -c <path to training corpus> [-v <validation method> -o <path to log file>] -e uas\n");

        help.append("punctuation_symbols	 -s	true	Include punctuaton symbols (DEFAULT)\n");
        help.append("\t\t\t\tfalse	Exclude punctuation symbols\n");

        help.append("Usage:\n");
        help.append("java -jar MaltOptimizer.jar -p <-phase number-> -m <-MaltParser jar path-> -c <path to training corpus> [-v <validation method> -o <path to log file>] -s false\n");
        
        println(help.toString());
    }

    private void setMalt(String malt) {
        maltPath = malt;
    }

    public static String getMaltPath() {
        return maltPath;
    }

    public static void setMaltPath(String maltPath) {
        Optimizer.maltPath = maltPath;
    }

    public String getTrainingCorpus() {
        return trainingCorpus;
    }

    public void setTrainingCorpus(String trainingCorpus) {
        this.trainingCorpus = trainingCorpus;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isProjective() {
        return projective;
    }

    public void setProjective(boolean projective) {
        this.projective = projective;
    }

    public boolean isStrictRoot() {
        return strictRoot;
    }

    public void setStrictRoot(boolean strictRoot) {
        this.strictRoot = strictRoot;
    }

    public boolean isCoveredRoots() {
        return coveredRoots;
    }

    public void setCoveredRoots(boolean coveredRoots) {
        this.coveredRoots = coveredRoots;
    }

    public boolean isCoveredRootsWithoutChildren() {
        return coveredRootsWithoutChildren;
    }

    public void setCoveredRootsWithoutChildren(boolean coveredRootsWithoutChildren) {
        this.coveredRootsWithoutChildren = coveredRootsWithoutChildren;
    }

    public String getBestAlgorithm() {
        return bestAlgorithm;
    }

    public void setBestAlgorithm(String bestAlgorithm) {
        Optimizer.bestAlgorithm = bestAlgorithm;
    }

    public boolean isRootGRL() {
        return rootGRL;
    }

    public void setRootGRL(boolean rootGRL) {
        this.rootGRL = rootGRL;
    }

    public boolean isDanglingPunctuation() {
        return danglingPunctuation;
    }

    public void setDanglingPunctuation(boolean danglingPunctuation) {
        this.danglingPunctuation = danglingPunctuation;
    }

    public static String getPcrOption() {
        return pcrOption;
    }

    public static void setPcrOption(String pcrOption) {
        Optimizer.pcrOption = pcrOption;
    }

    public boolean isPcr() {
        return pcr;
    }

    public void setPcr(boolean pcr) {
        this.pcr = pcr;
    }

    public static String getOptionGRL() {
        return optionGRL;
    }

    public static void setOptionGRL(String optionGRL) {
        Optimizer.optionGRL = optionGRL;
    }

    public static String getOptionMenosR() {
        return optionMenosR;
    }

    public static void setOptionMenosR(String optionMenosR) {
        Optimizer.optionMenosR = optionMenosR;
    }

    public static Double getBestResult() {
        return bestResult;
    }

    public static void setBestResult(Double bestResult) {
        Optimizer.bestResult = bestResult;
    }

    public static void main(String[] args) {

        Optimizer d = new Optimizer();
        int phase;
        String maltPathArg;
        String pathTrainingSet;
        if (args.length == 0) {
            d.showDefault();
        } else {
            if (args[0].equals("-h")) {
                d.showHelp();
            } else {
                //java -jar MaltOptimizer.jar -p <int> -m <path to MaltParser> -c <path to training corpus> [-v <validation method>]
                if (args[0].equals("-p") && (args.length > 5)) {
                    try {
                        phase = Integer.parseInt(args[1]);
                    }
                    catch(Exception ex) {
                        d.showDefault();
                        return;
                    }
                    if (args[2].equals("-m")) {
                        maltPathArg = args[3];
                        if (args[4].equals("-c")) {
                            pathTrainingSet = args[5];
                            
                            // Read Optional Parameters
                            for (int i = 6; i < args.length; i+=2) {
                                switch (args[i]) {
                                    case "-debug":
                                        Optimizer.ShowMaltLog = args[i+1].equals("1");
                                        break;
                                    case "-s":
                                        if (args[i+1].toLowerCase().equals("true")) {
                                            Optimizer.includePunctuation = true;
                                        } else {
                                            Optimizer.includePunctuation = false;
                                        }
                                        break;
                                    case "-e":
                                        String val = args[i+1].toLowerCase();
                                        switch (val) {
                                            case "las":
                                                Optimizer.evaluationMeasure = "LAS";
                                                break;
                                            case "uas":
                                                Optimizer.evaluationMeasure = "UAS";
                                                break;
                                            case "lcm":
                                                Optimizer.evaluationMeasure = "LCM";
                                                break;
                                            case "ucm":
                                                Optimizer.evaluationMeasure = "UCM";
                                                break;
                                            default:
                                                d.showDefault();
                                                break;
                                        }
                                        break;
                                    case "-t":
                                        Optimizer.testCorpus = args[i+1];
                                        break;
                                    case "-v":
                                        if (args[i+1].toLowerCase().equals("cv")) {
                                            Optimizer.chooseAverage = true;
                                            Optimizer.crossValidation = true;
                                        }
                                        break;
                                    case "-a":
                                        //FEATURE ALGORITHM
                                        String s = args[i+1];
                                        if (s.equals("Greedy")) {
                                            Optimizer.featureAlgorithm = s;
                                        } else {
                                            if (s.equals("RelaxedGreedy")) {
                                                Optimizer.featureAlgorithm = s;
                                            } else {
                                                if (s.equals("BruteForce")) {
                                                    Optimizer.featureAlgorithm = s;
                                                } else if (s.equals("OnlyBackward")) {
                                                    Optimizer.featureAlgorithm = s;
                                                } else if (s.equals("OnlyForward")) {
                                                    Optimizer.featureAlgorithm = s;
                                                } else if (s.contains("5Fold")) {
                                                    Optimizer.featureAlgorithm = s;
                                                } else {
                                                    Optimizer.featureAlgorithm = "Greedy";
                                                }
                                            }
                                        }
                                        break;
                                    case "-o":
                                        // Log file
                                        try {
                                            d.writer = new BufferedWriter(new OutputStreamWriter(
                                                new FileOutputStream(args[i+1], true), "UTF-8"));
                                        }
                                        catch(FileNotFoundException | UnsupportedEncodingException ex) {
                                            d.writer = null;
                                        }
                                        break;
                                }
                            }
                            
                            d.setMalt(maltPathArg);
                            d.setCorpus(pathTrainingSet);
                            
                            switch(phase) {
                                case 1:
                                    d.runPhase1();
                                    break;
                                case 2:
                                    d.loadPhase1Results(pathTrainingSet);

                                    if (Optimizer.crossValidation) {
                                        d.runPhase25Fold();
                                    } else {
                                        d.runPhase2();
                                    }
                                    break;
                                case 3:
                                    d.loadPhase2Results(pathTrainingSet);
                                    switch (Optimizer.featureAlgorithm) {
                                        case "Greedy":
                                            if (Optimizer.crossValidation == true) {
                                                Optimizer.pseudoRandomizeSelection = true;
                                                d.runPhase35Fold();
                                            } else {
                                                d.runPhase3(); //defatult is Greedy
                                            }
                                            break;
                                        case "BruteForce":
                                            d.runPhase3BruteForce();
                                            break;
                                        case "RelaxedGreedy":
                                            d.runPhase3RelaxedGreedy();
                                            break;
                                        case "OnlyBackward":
                                            d.runPhase3OnlyBackward("");
                                            break;
                                        case "OnlyForward":
                                            d.runPhase3OnlyForward("");
                                            break;
                                        case "5FoldMajorityPS":
                                            Optimizer.pseudoRandomizeSelection = true;
                                            Optimizer.chooseMajority = true;
                                            d.runPhase35Fold();
                                            break;
                                        case "5FoldAveragePS":
                                            Optimizer.pseudoRandomizeSelection = true;
                                            Optimizer.chooseAverage = true;
                                            d.runPhase35Fold();
                                            break;
                                        case "5FoldAllPS":
                                            Optimizer.pseudoRandomizeSelection = true;
                                            Optimizer.chooseAllOfThem = true;
                                            d.runPhase35Fold();
                                            break;
                                        case "5FoldMajorityNoPS":
                                            Optimizer.pseudoRandomizeSelection = false;
                                            Optimizer.chooseMajority = true;
                                            d.runPhase35Fold();
                                            break;
                                        case "5FoldAverageNoPS":
                                            Optimizer.pseudoRandomizeSelection = false;
                                            Optimizer.chooseAverage = true;
                                            d.runPhase35Fold();
                                            break;
                                        case "5FoldAllNoPS":
                                            Optimizer.pseudoRandomizeSelection = false;
                                            Optimizer.chooseAllOfThem = true;
                                            d.runPhase35Fold();
                                            break;
                                        default:
                                            d.runPhase3();
                                            break;
                                    }
                                    break;
                            }
                            /*
                             * else if (phase.equals("4")) {
                             *
                             * d.setMalt(maltPath);
                             * d.loadPhase3Results(pathTrainingSet);
                             * d.setCorpus(pathTrainingSet); d.runPhase4();
                             * //////////////////////
							}
                             */                            
                            
                            // Delete Files
                            for (int i = 1; i < 6; i++) {
                                new File ("fold_train_" + i + ".conll").delete();
                            }
                        } else {
                            d.showDefault();
                        }
                    } else {
                        //println(args[2]);
                        d.showDefault();
                    }
                } else {
                    d.showDefault();
                }
            }
        }
    }
    
    private void ShowIntroduction() {
        StringBuilder intro = new StringBuilder();
        
        intro.append("-----------------------------------------------------------------------------\n");
        intro.append("                   MaltOptimizer 1.0\n");
        intro.append("-----------------------------------------------------------------------------\n");
        intro.append("         Miguel Ballesteros* and Joakim Nivre**\n");
        intro.append("          *Complutense University of Madrid (Spain)  \n");
        intro.append("                **Uppsala University (Sweden)   \n");
        intro.append("-----------------------------------------------------------------------------");
        
        println(intro.toString());
    }

    private void ShowPhase3Introduction() {
        println("MaltOptimizer is going to perform the following feature selection experiments:");
        println("1. Tune the window of POSTAG (n-gram) over the parser state.");
        println("2. Tune the window of (lexical) FORM features over the parser state.");
        println("3. Tune DEPREL and POSTAG features over the partially built dependency tree.");
        println("4. Add POSTAG and FORM features over the input string.");
        println("5. Add CPOSTAG, FEATS, and LEMMA features if available.");
        println("6. Add conjunctions of POSTAG and FORM features.");
        println("-----------------------------------------------------------------------------");

        if (bestAlgorithm.equals("nivreeager")) {
            featureModel = "NivreEager.xml";
            InputLookAhead = "Input";
        }

        if (bestAlgorithm.equals("nivrestandard")) {  //A PARTIR DE LA PROXIMA VERSION PONER EL FEATURE CORRESPONDIENTE!!!
            featureModel = "NivreStandard.xml";
            InputLookAhead = "Input";
        }

        if (bestAlgorithm.equals("covnonproj")) {
            featureModel = "CovingtonNonProjective.xml";
            InputLookAhead = "Right";
        }

        if (bestAlgorithm.equals("covproj")) {
            featureModel = "CovingtonProjective.xml";
            InputLookAhead = "Right";
        }

        if (bestAlgorithm.equals("stackproj")) {
            featureModel = "StackProjective.xml";
            InputLookAhead = "Lookahead";
        }

        if (bestAlgorithm.equals("stackeager")) {
            featureModel = "StackSwap.xml";
            InputLookAhead = "Lookahead";
        }

        if (bestAlgorithm.equals("stacklazy")) {
            featureModel = "StackSwap.xml";
            InputLookAhead = "Lookahead";
        }
    }
    
    private boolean setBestResult(double result, String newFeature) {
        boolean keepGoing = true;
        if (result > (Optimizer.bestResult + threshold)) { //Shrinking
            if (featureModel.equals(newFeature))
                new File(featureModel).delete();
            featureModel = newFeature;
            double difference = result - bestResult;
            bestResult = result;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best feature model: " + featureModel);
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        } else {
            keepGoing = false;
            if (!featureModel.equals(newFeature))
                new File(newFeature).delete();
        }
        return keepGoing;
    }
    
    private boolean setBestResultNoThreshold(double result, String newFeature) {
        boolean keepGoing = true;
        if (result >= (Optimizer.bestResult)) { //Shrinking //NO THRESHOLD because we are removing
            if (featureModel.equals(newFeature))
                new File(featureModel).delete();
            featureModel = newFeature;
            double difference = result - bestResult;
            bestResult = result;
            String sDifferenceLabel = String.format(pattern, difference);
            println("New best feature model: " + featureModel);
            String s = String.format(pattern, bestResult);
            println("Incremental " + evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
        } else { //Expanding
            keepGoing = false;
            if (featureModel.equals(newFeature))
                new File(newFeature).delete();
        }
        return keepGoing;
    }
    
    private void createLogFile(int phase) {
        BufferedWriter bwPhaseHiddenLogFile;

        try {
            bwPhaseHiddenLogFile = new BufferedWriter(new FileWriter("phase" + phase + "_logFile.txt"));
            bwPhaseHiddenLogFile.write("Training set path:" + trainingCorpus + "\n");
            bwPhaseHiddenLogFile.write("Size (tokens):" + getNumbTokens() + "\n");
            bwPhaseHiddenLogFile.write("Size (sentences):" + getNumbSentences() + "\n");
            bwPhaseHiddenLogFile.write("Non projective:" + String.format(pattern, percentage) + "\n");
            bwPhaseHiddenLogFile.write("Dangling Punctuation:" + numbDanglingCases + "\n");
            bwPhaseHiddenLogFile.write("LAS:" + String.format(pattern, bestResult) + "\n");
            bwPhaseHiddenLogFile.write("Default:" + String.format(pattern, defaultBaseline) + "\n");
            bwPhaseHiddenLogFile.write("NumRootLabels:" + numRootLabels + "\n");
            bwPhaseHiddenLogFile.write("JavaHeap:" + javaHeapValue + "\n");
            bwPhaseHiddenLogFile.write("MaxTokens:" + nMaxTokens + "\n");
            bwPhaseHiddenLogFile.write("CposEqPos:" + cposEqPos + "\n");
            bwPhaseHiddenLogFile.write("LemmaBlank:" + lemmaBlank + "\n");
            bwPhaseHiddenLogFile.write("FeatsBlank:" + featsBlank + "\n");
            if (phase != 1 && bestAlgorithm.contains("nivre")) {
                bwPhaseHiddenLogFile.write("allow_root (-nr):" + Optimizer.allow_rootNiv + "\n");
                bwPhaseHiddenLogFile.write("allow_reduce (-ne):" + Optimizer.allow_reduceNiv + "\n");
            }
            bwPhaseHiddenLogFile.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    private void createOptionFile(int phase) {
        BufferedWriter bwPhaseLogFile;
        
        int line = 1;

        try {
            bwPhaseLogFile = new BufferedWriter(new FileWriter("phase" + phase + "_optFile.txt"));
            if ( (phase == 1 && rootGRL == true) || (phase != 1)) {
               bwPhaseLogFile.write((line++) + ". root_label (-grl):" + optionGRL + "\n");
            }
            bwPhaseLogFile.write((line++) + ". covered_root (-pcr):" + pcrOption + "\n");
            if (phase > 1) {
                bwPhaseLogFile.write((line++) + ". parsing_algorithm (-a):" + bestAlgorithm + "\n");
                if (usePPOption) {
                    bwPhaseLogFile.write((line++) + ". marking_strategy (-pp):" + ppOption + "\n");
                }
                if (bestAlgorithm.contains("cov")) {
                    bwPhaseLogFile.write((line++) + ". allow_shift (-cs):" + allow_shift + "\n");
                    bwPhaseLogFile.write((line++) + ". allow_root (-cr):" + allow_root + "\n");
                }
                if (bestAlgorithm.contains("nivre")) {
                    bwPhaseLogFile.write((line++) + ". allow_root (-nr):" + Optimizer.allow_rootNiv + "\n");
                    bwPhaseLogFile.write((line++) + ". allow_reduce (-ne):" + Optimizer.allow_reduceNiv + "\n");
                }
                if (phase == 3) {
                    bwPhaseLogFile.write((line++) + ". feature_model (-F):" + Optimizer.featureModel + "\n");
                }
                if (phase == 4) {
                    bwPhaseLogFile.write((line++) + ". library (-lo):" + Optimizer.libraryValue + "\n");
                }
            }
            bwPhaseLogFile.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    public void print(String text) {
        out.print(text);
        if (writer != null) {
            try {
                writer.write(text);
            }
            catch (Exception ex) {}
        }        
    }
    
    public void println(String text) {
        out.println(text);
        if (writer != null) {
            try {
                writer.write(text + "\n");
            }
            catch (Exception ex) {}
        }
    }
    
    private void Exit() {
        println("END");

        if (writer != null) {
            try {
                writer.close();
            }
            catch(Exception ex) {}
        }
        
        if (ExitInEnd == true)
            System.exit(0);
    }
}