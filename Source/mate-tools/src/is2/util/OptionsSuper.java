package is2.util;

import is2.parser.Parser;
import java.io.File;

public class OptionsSuper {

    public String trainfile = null;
    public String testfile = null;
    public File trainforest = null;
    public String nbframes = null;
    public String pbframes = null;
    public boolean nopred = false;
    public boolean upper = true;
    public boolean train = false;
    public boolean eval = false;
    public boolean test = false;
    public boolean keep = false;
    public boolean flt = false;
    public boolean loadTaggerModels = false;
    public String modelName = "prs.model";
    public String useMapping = null;
    public String device = "C:";
    public String tmp = null;
    public boolean createForest = true;
    public boolean decodeProjective = false;
    public double decodeTH = 0.3d;
    public String format = "CONLL";
    // CoNLL Format (4|6|8|9)
    public int formatTask = 9;
    public int numIters = 10;
    public int best = 1000;
    public String outfile = "dp.conll";
    public String charset = "UTF-8";
    public String phraseTrain = null;
    public String phraseTest = null;
    public String goldfile = null;
    public String gout = "sec23.gld";
    public String features = null;
    public String lexicon = null;
    public int hsize = 0x07ffffff;
    public int maxLen = 2000;
    public int maxForms = Integer.MAX_VALUE;
    public int beam = 4;
    public float prune = -100000000;
    public String third = "";
    public String second = "";
    public String first = "";
    public int cross = 10;
    //public boolean secondOrder = true;
    public boolean useRelationalFeatures = false;
    public int count = 10000000;
    public int cores = Integer.MAX_VALUE;
    public int start = 0;
    public int minOccureForms = 0;
    public int tt = 30; // tagger averaging
    public boolean allFeatures = false;
    public boolean normalize = false;
    public boolean no2nd = false;
    public boolean noLemmas = false;
    public boolean few2nd = false, noLinear = false, noMorph = false;
    public String clusterFile;
    // output confidence values
    public boolean conf = false;
    public String phraseFormat = "penn"; // tiger | penn
    public boolean average = true;
    public boolean label = false;
    public boolean stack = false;
    public String significant1 = null, significant2 = null;
    // horizontal stacking 
    public int minLength = 0, maxLength = Integer.MAX_VALUE;
    public boolean overwritegold = false;
    public static final int MULTIPLICATIVE = 1, SHIFT = 2;
    public int featureCreation = MULTIPLICATIVE;

    public OptionsSuper(String[] args, String dummy) {

        for (int i = 0; i < args.length; i++) {
            i = addOption(args, i);
        }
    }

    public OptionsSuper() {
    }

    public int addOption(String args[], int i) {
        switch (args[i]) {
            case "-train":
                train = true;
                trainfile = args[i + 1];
                break;
            case "-eval":
                eval = true;
                goldfile = args[i + 1];
                i++;
                break;
            case "-gout":
                gout = args[i + 1];
                i++;
                break;
            case "-test":
                test = true;
                testfile = args[i + 1];
                i++;
                break;
            case "-sig1":
                significant1 = args[i + 1];
                i++;
                break;
            case "-sig2":
                significant2 = args[i + 1];
                i++;
                break;
            case "-i":
                numIters = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-out":
                outfile = args[i + 1];
                i++;
                break;
            case "-cluster":
                clusterFile = args[i + 1];
                i++;
                break;
            case "-count":
                count = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-model":
                modelName = args[i + 1];
                i++;
                break;
            case "-nonormalize":
                normalize = false;
                break;
            case "-float":
                flt = true;
                break;
            case "-hsize":
                hsize = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-charset":
                charset = args[++i];
                break;
            case "-pstrain":
                this.phraseTrain = args[i + 1];
                i++;
                break;
            case "-pstest":
                this.phraseTest = args[i + 1];
                i++;
                break;
            case "-len":
                maxLen = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-cores":
                cores = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-start":
                start = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-max":
                maxLength = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-min":
                minLength = Integer.parseInt(args[i + 1]);
                i++;
                break;
            case "-noLemmas":
                noLemmas = true;
                break;
            case "-noavg":
                this.average = false;
                break;
            case "-label":
                label = true;
                break;
            case "-stack":
                stack = true;
                break;
            case "-overwritegold":
                overwritegold = true;
                break;
            case "-format":
                formatTask = Integer.parseInt(args[++i]);
                break;
            case "-tt":
                tt = Integer.parseInt(args[++i]);
                break;
            case "-min-occure-forms":
                minOccureForms = Integer.parseInt(args[++i]);
                break;
            case "-loadTaggerModels":
                this.loadTaggerModels = true;
                break;
            case "-feature_creation":
                this.featureCreation = args[++i].equals("shift") ? SHIFT : MULTIPLICATIVE;
                break;
        }

        return i;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FLAGS [");
        sb.append("train-file: ").append(trainfile);
        sb.append(" | ");
        sb.append("test-file: ").append(testfile);
        sb.append(" | ");
        sb.append("gold-file: ").append(goldfile);
        sb.append(" | ");
        sb.append("output-file: ").append(outfile);
        sb.append(" | ");
        sb.append("model-name: ").append(modelName);
        sb.append(" | ");
        sb.append("train: ").append(train);
        sb.append(" | ");
        sb.append("test: ").append(test);
        sb.append(" | ");
        sb.append("eval: ").append(eval);
        sb.append(" | ");
        sb.append("training-iterations: ").append(numIters);
        sb.append(" | ");
        sb.append("decode-type: ").append(decodeProjective);
        sb.append(" | ");
        sb.append("create-forest: ").append(createForest);
        sb.append(" | ");
        sb.append("format: ").append(format);

        sb.append("]\n");
        return sb.toString();
    }

    protected void explain(String clssPath1, String classPath2, boolean showFormat) {
        Parser.out.println("Usage: ");
        Parser.out.println("java " + clssPath1 + " [Options]");
        Parser.out.println();
        Parser.out.println("Example: ");
        Parser.out.println(" java " + classPath2 + " -model eps3.model -train corpora/conll08st/train/train.closed -test corpora/conll08st/devel/devel.closed  -out b3.test -eval corpora/conll08st/devel/devel.closed  -count 2000 -i 6");
        Parser.out.println("");
        Parser.out.println("Options:");
        Parser.out.println("");
        Parser.out.println(" -train  <file>    the corpus a model is trained on; default " + this.trainfile);
        Parser.out.println(" -test   <file>    the input corpus for testing; default " + this.testfile);
        Parser.out.println(" -out    <file>    the output corpus (result) of a test run; default " + this.outfile);
        Parser.out.println(" -model  <file>    the parsing model for traing the model is stored in the files");
        Parser.out.println("                   and for parsing the model is load from this file; default " + this.modelName);
        Parser.out.println(" -i      <number>  the number of training iterations; good numbers are 10 for smaller corpora and 6 for bigger; default " + this.numIters);
        Parser.out.println(" -count  <number>  the n first sentences of the corpus are take for the training default " + this.count);
        if (showFormat == true) {
            Parser.out.println(" -format <number>  conll format of the year 6 or 8 or 9; default " + this.formatTask);
        }

        System.exit(0);
    }
}