package optimizer;

/**
 *
 * @author Miguel Ballesteros
 *
 * This class generates the options file
 */
public class OptionsGenerator {

    String libValue;
    String modelName;//nivreeager
    String modelNameStackLazy;
    String algorithm;
    String trainingCorpus;
    String libraryOptions;
    boolean danglingPunctuation;
    boolean rootGRL;
    String realRoot;

    public OptionsGenerator(String language, boolean projective, String trainingCorpus, boolean danglingPunct, boolean rootGRL, String realRoot) {

        this.trainingCorpus = trainingCorpus;
        this.danglingPunctuation = danglingPunct;
        this.rootGRL = rootGRL;
        this.realRoot = realRoot;

        //libraryOptions="-s_0_-t_1_-d_2_-g_0.2_-c_1.0_-r_0.4_-e_0.1";

        /*
         * modelName=language+"Model"; if (language.equals("en")){
         * libValue="-s_0_-t_1_-d_2_-g_0.18_-c_0.4_-r_0.4_-e_1.0"; } if
         * (language.equals("es")){
         * libValue="-s_0_-t_1_-d_2_-g_0.2_-c_0.5_-r_0_-e_0.01"; } if
         * (language.equals("sw")){
         *
         * }
         */
        //etc....

        if (projective) {
            algorithm = "nivreeager";
        } else {
            algorithm = "covnonproj";
        }
    }

    public String generateIncOptionsPrevioGRL(String lang, String algorithm, String training80, String rootHandling, String libOptions) {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";
        algorithm = "nivreeager";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + lang + "Model'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + training80 + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");
        return options;
    }

    public String generateIncOptionsPrevioPCR(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel) {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";
        algorithm = "nivreeager";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + lang + "Model'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + training80 + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='graph'>";
        options += "\n\t\t\t" + "<option name='root_label' value='" + rootLabel + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");
        return options;
    }

    public String generateIncOptionsPhase1(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";
        algorithm = "nivreeager";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + lang + "Model'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + training80 + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        if (algorithm.contains("nivre")) {
            options += "\n\t\t" + "<optiongroup groupname='nivre'>";
            options += "\n\t\t\t" + "<option name='allow_root' value='" + Optimizer.allow_rootNiv + "'/>";
            options += "\n\t\t\t" + "<option name='allow_reduce' value='" + Optimizer.allow_reduceNiv + "'/>";
            options += "\n\t\t" + "</optiongroup>";
        }
        options += "\n\t\t" + "<optiongroup groupname='graph'>";
        options += "\n\t\t\t" + "<option name='root_label' value='" + rootLabel + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='pproj'>";
        options += "\n\t\t\t" + "<option name='covered_root' value='" + pcr + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");
        return options;
    }

    public String generateIncOptionsTestingsPhases(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr) {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + lang + "Model'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + training80 + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        if (algorithm.contains("nivre")) {
            options += "\n\t\t" + "<optiongroup groupname='nivre'>";
            options += "\n\t\t\t" + "<option name='allow_root' value='" + Optimizer.allow_rootNiv + "'/>";
            options += "\n\t\t\t" + "<option name='allow_reduce' value='" + Optimizer.allow_reduceNiv + "'/>";
            options += "\n\t\t" + "</optiongroup>";
        }

        options += "\n\t\t" + "<optiongroup groupname='graph'>";
        options += "\n\t\t\t" + "<option name='root_label' value='" + rootLabel + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='pproj'>";
        if (Optimizer.usePPOption) {
            options += "\n\t\t\t" + "<option name='marking_strategy' value='" + Optimizer.ppOption + "'/>";
        }
        options += "\n\t\t\t" + "<option name='covered_root' value='" + pcr + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");
        return options;
    }

    public String generateIncOptionsTestingsPhasesb(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr, String pp) {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + lang + "Model'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + training80 + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        if (algorithm.contains("nivre")) {
            options += "\n\t\t" + "<optiongroup groupname='nivre'>";
            options += "\n\t\t\t" + "<option name='allow_root' value='" + Optimizer.allow_rootNiv + "'/>";
            options += "\n\t\t\t" + "<option name='allow_reduce' value='" + Optimizer.allow_reduceNiv + "'/>";
            options += "\n\t\t" + "</optiongroup>";
        }
        options += "\n\t\t" + "<optiongroup groupname='graph'>";
        options += "\n\t\t\t" + "<option name='root_label' value='" + rootLabel + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='pproj'>";
        options += "\n\t\t\t" + "<option name='marking_strategy' value='" + pp + "'/>";
        options += "\n\t\t\t" + "<option name='covered_root' value='" + pcr + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");
        return options;
    }

    public String generateIncOptionsTestingsEndPhase2(String lang, String algorithm, String training80, String rootHandling, String libOptions, String rootLabel, String pcr, String pp, boolean allowShift, boolean allowROOT) {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + lang + "Model'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + training80 + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        if (algorithm.contains("nivre")) {
            options += "\n\t\t" + "<optiongroup groupname='nivre'>";
            options += "\n\t\t\t" + "<option name='allow_root' value='" + Optimizer.allow_rootNiv + "'/>";
            options += "\n\t\t\t" + "<option name='allow_reduce' value='" + Optimizer.allow_reduceNiv + "'/>";
            options += "\n\t\t" + "</optiongroup>";
        }
        if (algorithm.contains("cov")) {
            options += "\n\t\t" + "<optiongroup groupname='covington'>";
            options += "\n\t\t\t" + "<option name='allow_root' value='" + allowROOT + "'/>";
            options += "\n\t\t\t" + "<option name='allow_shift' value='" + allowShift + "'/>";
            options += "\n\t\t" + "</optiongroup>";
        }
        options += "\n\t\t" + "<optiongroup groupname='graph'>";
        options += "\n\t\t\t" + "<option name='root_label' value='" + rootLabel + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='pproj'>";
        options += "\n\t\t\t" + "<option name='marking_strategy' value='" + pp + "'/>";
        options += "\n\t\t\t" + "<option name='covered_root' value='" + pcr + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");
        return options;
    }

    public String getLibraryOptions() {
        return libraryOptions;
    }

    public void setLibraryOptions(String libraryOptions) {
        this.libraryOptions = libraryOptions;
    }

    public OptionsGenerator(String language, String trainingCorpus) {
        modelName = language + "Model";
        modelNameStackLazy = language + "ModelStack";
        this.trainingCorpus = trainingCorpus;
        //System.out.println(trainingCorpus);
    }

    public OptionsGenerator(String bestAlgorithm, String language,
            String trainingCorpus, String liboptions) {

        this.algorithm = bestAlgorithm;
        this.libValue = liboptions;
        this.trainingCorpus = trainingCorpus;
        modelName = language + "Model";
    }

    public OptionsGenerator() {
    }

    public String generateOptionsNivreEager() {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";
        algorithm = "nivreeager";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + modelName + "'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + trainingCorpus + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libraryOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        //<option name="learner" value="liblinear"/>
        options += "\n\t\t\t" + "<option name='learner' value='liblinear'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");

        return options;
    }

    public String generateOptionsStackLazy() {
        LibraryOptionsSetter lo = LibraryOptionsSetter.getSingleton();
        libraryOptions = lo.getLibraryOptions();
        String options = "";
        algorithm = "stacklazy";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n<experiment>";
        options += "\n\t<optioncontainer>";
        options += "\n\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t" + "<option name='name' value='" + modelNameStackLazy + "'/>";
        options += "\n\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t" + "<option name='infile' value='" + trainingCorpus + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        if (algorithm.contains("nivre")) {
            options += "\n\t\t" + "<optiongroup groupname='nivre'>";
            options += "\n\t\t\t" + "<option name='allow_root' value='" + Optimizer.allow_rootNiv + "'/>";
            options += "\n\t\t\t" + "<option name='allow_reduce' value='" + Optimizer.allow_reduceNiv + "'/>";
            options += "\n\t\t" + "</optiongroup>";
        }
        options += "\n\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t" + "<option name='options' value='" + libraryOptions + "'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t" + "<option name='data_split_column' value='POSTAG'/>";
        options += "\n\t\t\t" + "<option name='data_split_structure' value='Input[0]'/>";
        options += "\n\t\t\t" + "<option name='data_split_threshold' value='1000'/>";
        options += "\n\t\t" + "</optiongroup>";
        options += "\n\t</optioncontainer>";
        options += "\n</experiment>";

        options = options.replaceAll("'", "\"");

        return options;
    }

    public String generateOptionsFile() {
        String options = "";

        options += "<?xml version='1.0' encoding='UTF-8'?>";
        options += "\n\t<experiment>";
        options += "\n\t\t<optioncontainer>";
        options += "\n\t\t\t" + "<optiongroup groupname='config'>";
        options += "\n\t\t\t\t" + "<option name='name' value='" + modelName + "'/>";
        options += "\n\t\t\t\t" + "<option name='flowchart' value='learn'/>";
        options += "\n\t\t\t" + "</optiongroup>";
        options += "\n\t\t\t" + "<optiongroup groupname='singlemalt'>";
        options += "\n\t\t\t\t" + "<option name='parsing_algorithm' value='" + algorithm + "'/>";
        options += "\n\t\t\t" + "</optiongroup>";
        options += "\n\t\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t\t" + "<option name='name' value='" + trainingCorpus + "'/>";
        options += "\n\t\t\t" + "</optiongroup>";
        options += "\n\t\t\t" + "<optiongroup groupname='input'>";
        options += "\n\t\t\t\t" + "<option name='root_handling' value='strict'/>";
        options += "\n\t\t\t" + "</optiongroup>";
        options += "\n\t\t\t" + "<optiongroup groupname='lib'>";
        options += "\n\t\t\t\t" + "<option name='name' value='" + libValue + "'/>";
        options += "\n\t\t\t" + "</optiongroup>";
        options += "\n\t\t\t" + "<optiongroup groupname='guide'>";
        options += "\n\t\t\t\t" + "<option name='data_split_column' value='POSTAG'/>";
        options += "\n\t\t\t\t" + "<option name='data_split_structure' value='Input[0]'/>";
        options += "\n\t\t\t\t" + "<option name='data_split_threshold' value='1000'/>";
        options += "\n\t\t\t" + "</optiongroup>";
        options += "\n\t\t</optioncontainer>";
        options += "\n\t</experiment>";

        options = options.replaceAll("'", "\"");

        return options;
    }

    public String generateTrainingCommand() {
        return "java -Dfile.encoding=UTF8 -jar malt.jar -f <options_file>.xml -F <feature_model>.xml";
    }

    public String generateTestingCommand() {
        if (rootGRL && !danglingPunctuation) {
            return "java -Dfile.encoding=UTF8 -jar malt.jar -c " + modelName + " -i <test_corpus>.conll -o output.conll -m parse";
        }

        if (!rootGRL && !danglingPunctuation) {
            return "java -Dfile.encoding=UTF8 -jar malt.jar -c " + modelName + " -i <test_corpus>.conll -o output.conll -m parse -grl " + this.realRoot;
        }

        if (rootGRL && danglingPunctuation) {
            return "java -Dfile.encoding=UTF8 -jar malt.jar -c " + modelName + " -i <test_corpus>.conll -o output.conll -m parse -pcr head";
        }

        //if (rootGRL && danglingPunctuation)
        return "java -Dfile.encoding=UTF8 -jar malt.jar -c " + modelName + " -i <test_corpus>.conll -o output.conll -m parse -grl " + this.realRoot + " -pcr head";
    }
}