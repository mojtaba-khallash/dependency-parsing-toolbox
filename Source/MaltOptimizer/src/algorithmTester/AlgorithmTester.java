package algorithmTester;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import optimizer.*;

/**
 * @author Miguel Ballesteros
 *
 */
public class AlgorithmTester {

    String algorithm;
    double nonprojectivities;
    boolean nonprojectivity;
    private CoNLLHandler ch;
    private String language;
    String trainingCorpus;
    String bestAlgorithm;
    double bestResult;
    public static String training80;
    String testing80;
    private String rootHandling;
    private Double bestLabelLASResult;
    
    private Writer writer = null;


    /**
     * Constructor of AlgorithmTesters
     *
     * @param language Language used in the corpus.
     * @param np percentage of non-projectivity
     * @param ch CoNLLHandler instance
     * @param trainingCorpus Training Corpus
     */
    public AlgorithmTester(String language, double np, CoNLLHandler ch, String trainingCorpus, String rootHandling) {
        this.trainingCorpus = trainingCorpus;
        this.language = language;
        bestResult = 0.0;
        this.rootHandling = rootHandling;

        if (trainingCorpus.contains("/")) {
            StringTokenizer st = new StringTokenizer(trainingCorpus, "/");
            String relPath = "";
            while (st.hasMoreTokens()) {
                relPath = st.nextToken("/");
            }
            //Echo(relPath);
            training80 = relPath.replaceAll(".conll", "");
            testing80 = relPath.replaceAll(".conll", "");
            training80 += "_train80.conll";
            testing80 += "_test20.conll";
        } else {
            training80 = trainingCorpus.replaceAll(".conll", "");
            testing80 = trainingCorpus.replaceAll(".conll", "");
            training80 += "_train80.conll";
            testing80 += "_test20.conll";
        }
        this.nonprojectivities = np;
        if (np > 25) {
            nonprojectivity = true;
        } else {
            nonprojectivity = false;
        }
        this.ch = ch;
    }

    public AlgorithmTester(String language, CoNLLHandler ch, String trainingCorpus) {
        this(language, ch, trainingCorpus, null);
    }
    
    public AlgorithmTester(String language, CoNLLHandler ch, String trainingCorpus, Writer writer) {
        this.trainingCorpus = trainingCorpus;
        this.language = language;
        bestResult = 0.0;
        
        this.writer = writer;

        if (trainingCorpus.contains("/")) {
            StringTokenizer st = new StringTokenizer(trainingCorpus, "/");
            String relPath = "";
            while (st.hasMoreTokens()) {
                relPath = st.nextToken("/");
            }
            //Echo(relPath);
            training80 = relPath.replaceAll(".conll", "");
            testing80 = relPath.replaceAll(".conll", "");
            training80 += "_train80.conll";
            testing80 += "_test20.conll";
        } else {
            training80 = trainingCorpus.replaceAll(".conll", "");
            testing80 = trainingCorpus.replaceAll(".conll", "");
            training80 += "_train80.conll";
            testing80 += "_test20.conll";
        }

        this.ch = ch;
    }
    
    public double executeMalt(String feature, String trainCorpus, String testCorpus, 
            String label, String optionsCat, String pcr, String ppOption, String rOption,
            Boolean allow_shift, Boolean allow_root) {
        String outFile = System.getProperty("user.dir") + File.separator + "out" + feature + ".conll";
        
        String options = "options" + feature + (label != null ? "GRL" : "") + (pcr != null ? "PCR" : "") + ".xml";

        BufferedWriter bwOptionsMalt;
        boolean isExist = true;
        try {
            bwOptionsMalt = new BufferedWriter(new FileWriter(options));
            bwOptionsMalt.write(optionsCat);
            bwOptionsMalt.close();
            String s;

            File feat = new File(feature + ".xml");
            isExist = feat.exists();
            if (!isExist) {
                BufferedWriter bwGuides;
                try {
                    bwGuides = new BufferedWriter(new FileWriter(feature + ".xml"));
                    bwGuides.write(GuidesGenerator.generateGuides(feature));
                    bwGuides.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            String maltPath = Optimizer.maltPath;
            String command = "java " + Optimizer.javaHeapValue + 
                " -jar " + maltPath + " -f " + options + " -F " + feature + ".xml";
            if (label != null) {
                command += " -grl " + label;
            }
            if (pcr != null) {
                command += " -pcr " + pcr;
            }
            if (ppOption != null) {
                command += " -pp " + ppOption;
            }
            if (rOption != null) {
                command += " " + rOption;
            }
            if (allow_shift != null) {
                command += " -cs " + allow_shift;
            }
            if (allow_root != null) {
                command += " -cr " + allow_root;
            }
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            while ((s = stdError.readLine()) != null) {
                if (Optimizer.ShowMaltLog == true) {
                    println(s);
                }
            }
            while ((s = stdInput.readLine()) != null) {
                if (Optimizer.ShowMaltLog == true) {
                    println(s);
                }
            }
            p.destroy();

            String outName = new File(outFile).getName();
            command = "java -jar " + maltPath + " -c " + language + 
                "Model -i " + testCorpus + " -o " + outName + " -m parse";
            Process p2 = Runtime.getRuntime().exec(command);
            BufferedReader stdInput2 = new BufferedReader(new InputStreamReader(
                    p2.getInputStream()));

            BufferedReader stdError2 = new BufferedReader(new InputStreamReader(
                    p2.getErrorStream()));
            while ((s = stdInput2.readLine()) != null) {
                if (Optimizer.ShowMaltLog == true) {
                    println(s);
                }
            }
            while ((s = stdError2.readLine()) != null) {
                if (Optimizer.ShowMaltLog == true) {
                    println(s);
                }
            }
            p.destroy();
        } catch (IOException e) {
            println(e.getMessage());
            e.printStackTrace();
        }
        finally {
            File feat = new File(feature + ".xml");
            if (!isExist && feat.exists()) {
               feat.delete();
            }
            new File(options).delete();
        }


        double maltResult = ch.evaluator(testCorpus, outFile);
        try {
            new File(outFile).delete();
            new File(language + "Model.mco").delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maltResult;
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm
     */
    public double executeNivreEagerDefault(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        String optionsCat = og.generateOptionsNivreEager();
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm
     */
    public boolean executeCovNonProjEagerDefaultJavaHeapTesting(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        //Firstly, Execute Nivre Eager
        String optionsCat = og.generateIncOptionsTestingsPhases("lang", "covnonproj", training80, "normal", lo.getLibraryOptions(), "ROOT", "none");
        String optionsNivreEager = "optionsCovTest.xml";
        BufferedWriter bwOptionsNivreEager;
        /*
         * String testCorpus=trainingCorpus.replaceAll(".conll","");
         * testCorpus+="_test20.conll"; String
         * trainCorpus=trainingCorpus.replaceAll(".conll","");
		trainCorpus+="_train80.conll";
         */

        try {
            bwOptionsNivreEager = new BufferedWriter(new FileWriter(optionsNivreEager));
            bwOptionsNivreEager.write(optionsCat);
            bwOptionsNivreEager.close();
            //Echo("java -jar "+maltPath+" -f "+optionsNivreEager+" -F NivreEager.xml");
            String s;
            String maltPath = Optimizer.maltPath;
            Process p = Runtime.getRuntime().exec("java " + Optimizer.javaHeapValue + " -jar " + maltPath + " -f " + optionsNivreEager + " -F " + feature);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));

            // Leemos la salida del comando
            //Echo("Ésta es la salida standard del comando:\n");
            while ((s = stdInput.readLine()) != null) {
                if (s.contains("Out of memory.")) {
                    return false;
                }
                if (s.contains("Could not reserve enough space for object heap")) {
                    return false;
                }
                //Echo(s);
            }
            while ((s = stdError.readLine()) != null) {
                if (s.contains("exceeds")) {
                    return false;
                }
                if (s.contains("Out of memory.")) {
                    return false;
                }
                if (s.contains("Could not reserve enough space for object heap")) {
                    return false;
                }
                if (s.contains("Unable to access")) {
                    return false;
                }
                //Echo(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing the best root label
     */
    public double executeNivreEagerRootLabelTest(String feature, 
            String trainCorpus, String testCorpus, String label) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsPrevioGRL(language, "nivreeager", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions());
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing the best PCR test
     */
    private double executeNivreEagerPCRTest(String feature, 
            String trainCorpus, String testCorpus, String pcr) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsPrevioPCR(language, "nivreeager", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL);
        String label = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing using the current
     * configuration
     */
    public double executeNivreEager(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivreeager", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing using the current
     * configuration
     */
    public double executeNivreEagerTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivreeager", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing using the current
     * configuration
     */
    public double executeNivreEagerTestTrainPPOption(String feature, 
            String trainCorpus, String testCorpus, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivreeager", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for stackproj algorithm testing using the current
     * configuration
     */
    public double executeStackProjective(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for stackproj algorithm testing using the current
     * configuration
     */
    public double executeStackProjectiveTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for stackproj algorithm testing using the current
     * configuration
     */
    public double executeStackProjectiveTestTrainPPOption(String feature, 
            String trainCorpus, String testCorpus, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for covproj algorithm testing using the current
     * configuration
     */
    public double executeCovingtonProjective(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for covproj algorithm testing using the current
     * configuration
     */
    public double executeCovingtonProjectiveTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for covproj algorithm testing using the current
     * configuration
     */
    public double executeCovingtonProjectiveTestTrainPPOption(String feature, 
            String trainCorpus, String testCorpus, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for nivrestandard algorithm testing using the
     * current configuration
     */
    public double executeNivreStandard(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivrestandard", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for nivrestandard algorithm testing using the
     * current configuration
     */
    public double executeNivreStandardTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivrestandard", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for nivrestandard algorithm testing using the
     * current configuration
     */
    public double executeNivreStandardTestTrainPPOption(String feature, 
            String trainCorpus, String testCorpus, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivrestandard", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for covnonproj algorithm testing using the current
     * configuration
     */
    public double executeCovingtonNonProjective(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covnonproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for covnonproj algorithm testing using the current
     * configuration
     */
    public double executeCovingtonNonProjectiveTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covnonproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     * @return LAS accuracy for stackLazy algorithm testing using the current
     * configuration
     */
    public double executeStackLazy(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stacklazy", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     * @return LAS accuracy for stackLazy algorithm testing using the current
     * configuration
     */
    public double executeStackLazyTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stacklazy", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     * @return LAS accuracy for stackLazy algorithm testing using the current
     * configuration
     */
    public double executeStackLazyTestTrainPPOption(String feature, 
            String trainCorpus, String testCorpus, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stacklazy", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     * @return LAS accuracy for stackEager algorithm testing using the current
     * configuration
     */
    public double executestackEager(String feature) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackeager", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     * @return LAS accuracy for stackEager algorithm testing using the current
     * configuration
     */
    public double executestackEagerTestTrain(String feature, 
            String trainCorpus, String testCorpus) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackeager", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     * @return LAS accuracy for stackEager algorithm testing using the current
     * configuration
     */
    public double executestackEagerTestTrainPPOption(String feature, 
            String trainCorpus, String testCorpus, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackeager", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing using the current
     * configuration
     */
    public double executeNivreEagerPPOption(String feature, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivreeager", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing using the current
     * configuration
     */
    public double executeNivreEagerROption(String feature, String rOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhasesb(language, "nivreeager", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption, Optimizer.ppOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for NivreEager algorithm testing using the current
     * configuration
     */
    public double executeNivreEagerROption17(String feature, String rOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhasesb(language, "nivreeager", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption, Optimizer.ppOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for covproj algorithm testing using the current
     * configuration
     */
    public double executecovprojPPOption(String feature, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for stackproj algorithm testing using the current
     * configuration
     */
    public double executestackprojPPOption(String feature, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "stackproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for nivrestandard algorithm testing using the
     * current configuration
     */
    public double executenivrestandardPPOption(String feature, String ppOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "nivrestandard", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for nivrestandard algorithm testing using the
     * current configuration
     */
    public double executenivrestandardROption(String feature, String rOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhasesb(language, "nivrestandard", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption, Optimizer.ppOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     *
     * @return LAS accuracy for nivrestandard algorithm testing using the
     * current configuration
     */
    public double executenivrestandardROption17(String feature, String rOption) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhasesb(language, "nivrestandard", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption, Optimizer.ppOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        Boolean allow_shift = null;
        Boolean allow_root = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    /**
     * This method infers the best projectivity algorithm
     *
     * @return the best projectivity algorithm
     */
    public String executeProjectivity() {
        /*
         * HashMap<String, Double> results=new HashMap<String, Double>();
         * results.put("NivreEager", executeNivreEager());
         * results.put("StackLazy", executeStackLazy());
         */
        //ch.generateDivision8020();
        println("--------------------------------------");
        println("The system is going to check with the best projectivity algorithms");

        double nivreEager = executeNivreEagerDefault("NivreEager", training80, testing80);
        String ne = String.format(Optimizer.pattern, nivreEager);
        println("NivreEager LAS= " + ne);

        double stackLazy = executeStackLazy("StackSwap");
        String sl = String.format(Optimizer.pattern, stackLazy);
        println("StackLazy LAS= " + sl);

        try {
            Runtime.getRuntime().exec("rm *.mco");
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (nivreEager >= stackLazy) {
            bestResult = nivreEager;
            return "nivreeager";
        } else {
            bestResult = stackLazy;
            return "stacklazy";
        }
    }

    public double getBestResult() {
        return bestResult;
    }

    /**
     * This method infers the best non-oprojectivity algorithm
     *
     * @return the best non-projectivity algorithm
     */
    public String executeNonProjectivity() {

        return null;
    }

    /**
     * This method tries to find the best ROOT label configuration
     *
     * @param threeLabels
     * @return best label
     */
    public String executeLabelTest(ArrayList<String> threeLabels) {
        Iterator<String> it = threeLabels.iterator();
        double maxD = 0.0;
        String max = "";
        while (it.hasNext()) {
            String lab = it.next();
            println(lab);
            double d  = executeNivreEagerRootLabelTest("NivreEager", training80, testing80, lab);
            if (d > maxD) {
                maxD = d;
                max = lab;
            }
        }
        bestLabelLASResult = maxD;
        return max;
    }

    public Double getBestLabelLASResult() {
        return bestLabelLASResult;
    }

    /**
     * This method tries to find the best PCR configuration
     *
     * @return best config value
     */
    public String executePCRTest() {
        ArrayList<String> options = new ArrayList<>();
        //-p none, -p left, -p right, -p head
        options.add("none");
        options.add("left");
        options.add("right");
        options.add("head");
        Iterator<String> it = options.iterator();
        Double maxD = 0.0;
        String max = "";
        while (it.hasNext()) {
            String lab = it.next();
            Double d = executeNivreEagerPCRTest("NivreEager", training80, testing80, lab);
            if (d > maxD) {
                maxD = d;
                max = lab;
            }
        }
        bestLabelLASResult = maxD;
        return max;
    }

    /**
     * This method tries to find the best PP configuration
     *
     * @return best config value
     */
    public String executePPTest(String algorithm) {
        ArrayList<String> options = new ArrayList<>();
        //-p none, -p left, -p right, -p head
        options.add("baseline");
        options.add("head");
        options.add("path");
        options.add("head+path");
        Iterator<String> it = options.iterator();
        Double maxD = Optimizer.bestResult;
        String max = "head";
        while (it.hasNext()) {
            String lab = it.next();
            double d = 0.0;
            if (algorithm.equals("nivreeager")) {
                d = this.executeNivreEagerPPOption("NivreEager", lab);
            }
            if (algorithm.equals("nivrestandard")) {
                d = this.executenivrestandardPPOption("NivreStandard", lab);
            }
            if (algorithm.equals("covproj")) {
                d = this.executecovprojPPOption("CovingtonProjective", lab);
            }
            if (algorithm.equals("stackproj")) {
                d = this.executestackprojPPOption("StackProjective", lab);
            }
            if (d > maxD) {
                maxD = d;
                max = lab;
                println("New best pp option:" + max);
                //
                Double difference = maxD - Optimizer.bestResult;
                Optimizer.bestResult = maxD;
                String sDifferenceLabel = String.format(Optimizer.pattern, difference);

                String s = "" + Optimizer.bestResult;
                if (s.length() == 4) {
                    s += "0";
                }

                println("Incremental " + Optimizer.evaluationMeasure + " improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        }
        bestLabelLASResult = maxD;
        return max;
    }

    /**
     * This method tries to find the best RH configuration
     *
     * @return best config value
     */
    public String executeRootHandlingTest(String algorithm) {
        ArrayList<String> options = new ArrayList<>();
        //-p none, -p left, -p right, -p head

        options.add("normal");
        options.add("strict");
        options.add("relaxed");

        Iterator<String> it = options.iterator();
        Double maxD = Optimizer.bestResult;
        String max = "normal";
        while (it.hasNext()) {
            String lab = it.next();
            Double d = 0.0;
            if (algorithm.equals("nivreeager")) {
                d = this.executeNivreEagerROption("NivreEager", lab);
            }
            if (algorithm.equals("nivrestandard")) {
                d = this.executenivrestandardROption("NivreStandard", lab);
            }
            if (d > maxD) {
                maxD = d;
                max = lab;
                println("New best root handling option:" + max);
                //
                Double difference = maxD - Optimizer.bestResult;
                Optimizer.bestResult = maxD;
                String sDifferenceLabel = String.format(Optimizer.pattern, difference);

                String s = "" + Optimizer.bestResult;
                if (s.length() == 4);
                println("Incremental LAS improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        }
        bestLabelLASResult = maxD;
        println("(" + max + ")");
        return max;
    }

    /**
     * This method tries to find the best RH configuration
     *
     * @return best config value
     */
    public String executeRootHandlingTestNivre17(String algorithm) {
        ArrayList<String> options = new ArrayList<>();
        //-p none, -p left, -p right, -p head

        options.add("-nr false -ne false");
        options.add("-nr true -ne true");
        options.add("-nr false -ne true");

        Iterator<String> it = options.iterator();
        Double maxD = Optimizer.bestResult;
        String max = "normal";

        int optionCounter = 0;

        while (it.hasNext()) {
            String lab = it.next();
            optionCounter++;
            double d = 0.0;
            if (algorithm.equals("nivreeager")) {
                d = this.executeNivreEagerROption17("NivreEager", lab);
            }
            //d=this.executeNivreEagerROption("NivreEager.xml", lab);
            if (algorithm.equals("nivrestandard")) {
                d = this.executenivrestandardROption17("NivreStandard", lab);
            }
            //d=this.executenivrestandardROption("NivreStandard.xml", lab);
            println(String.format(Optimizer.pattern, d));
            if (d > maxD) {
                maxD = d;
                max = lab;

                if (optionCounter == 1) {
                    Optimizer.allow_rootNiv = false;
                    Optimizer.allow_reduceNiv = false;
                }
                if (optionCounter == 2) {
                    Optimizer.allow_rootNiv = true;
                    Optimizer.allow_reduceNiv = true;
                }
                if (optionCounter == 3) {
                    Optimizer.allow_rootNiv = false;
                    Optimizer.allow_reduceNiv = true;
                }
                //
                Double difference = maxD - Optimizer.bestResult;
                Optimizer.bestResult = maxD;
                String sDifferenceLabel = String.format(Optimizer.pattern, difference);

                String s = String.format(Optimizer.pattern, Optimizer.bestResult);
                println("Incremental LAS improvement: + " + sDifferenceLabel + "% (" + s + "%)");
            }
        }
        bestLabelLASResult = maxD;
        println("(" + max + ")");
        return max;
    }

    public Double executeDefault() {
        double d = executeNivreEagerDefault("NivreEager", training80, testing80);
        return new Double(d);
    }

    public double executeCovingtonProjectivePPAllowShiftAllowRoot(String feature, 
            String ppOption, boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonProjectivePPAllowShiftAllowRootTestTrain(String feature, 
            String trainCorpus, String testCorpus, String ppOption, 
            boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonProjectiveAllowShiftAllowRoot(String feature, 
            boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonProjectiveAllowShiftAllowRootTestTrain(String feature, 
            String trainCorpus, String testCorpus,
            boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonNonProjectiveAllowShiftAllowRootTestTrain(String feature, 
            String trainCorpus, String testCorpus, boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covnonproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonNonProjectivePPAllowShiftAllowRoot(String feature, 
            String ppOption, boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covnonproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonNonProjectivePPAllowShiftAllowRootTestTrain(String feature, 
            String trainCorpus, String testCorpus, String ppOption, 
            boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, trainCorpus);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covnonproj", trainCorpus, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String rOption = null;
        return executeMalt(feature, trainCorpus, testCorpus, label, optionsCat, 
                pcr, ppOption, rOption, allow_shift, allow_root);
    }

    public double executeCovingtonNonProjectiveAllowShiftAllowRoot(String feature, 
            boolean allow_shift, boolean allow_root) {

        OptionsGenerator og = new OptionsGenerator(language, training80);
        LibraryOptionsSetter los = LibraryOptionsSetter.getSingleton();
        String optionsCat = og.generateIncOptionsTestingsPhases(language, "covnonproj", AlgorithmTester.training80, Optimizer.optionMenosR, los.getLibraryOptions(), Optimizer.optionGRL, Optimizer.pcrOption);
        String label = null;
        String pcr = null;
        String pseudoProj = null;
        String rOption = null;
        return executeMalt(feature, training80, testing80, label, optionsCat, 
                pcr, pseudoProj, rOption, allow_shift, allow_root);
    }
    
    public void print(String text) {
        Optimizer.out.print(text);
        if (writer != null) {
            try {
                writer.write(text);
            }
            catch (Exception ex) {}
        }
    }
    
    public void println(String text) {
        Optimizer.out.println(text);
        if (writer != null) {
            try {
                writer.write(text + "\n");
            }
            catch (Exception ex) {}
        }
    }
}