package is2.lemmatizer;

import is2.parser.Parser;
import is2.util.OptionsSuper;
import java.io.File;
import java.io.IOException;

public final class Options extends OptionsSuper {

    public Options(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("--help")) {
                explain();
            }
            switch (args[i]) {
                case "-normalize":
                    normalize = Boolean.parseBoolean(args[++i]);
                    break;
                case "-features":
                    features = args[i + 1];
                    i++;
                    break;
                case "-hsize":
                    hsize = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-len":
                    maxLen = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-tmp":
                    tmp = args[i + 1];
                    i++;
                    break;
                case "-uc":
                    upper = true;
                    Parser.out.println("set uppercase " + upper);
                    break;
                default:
                    super.addOption(args, i);
                    break;
            }
        }

        if (trainfile != null) {

            if (tmp != null) {
                trainforest = File.createTempFile("train", ".tmp", new File(tmp));
            } else {
                trainforest = File.createTempFile("train", ".tmp"); //,new File("F:\\")
            }
            trainforest.deleteOnExit();
        }
    }

    private void explain() {
        Parser.out.println("Usage: ");
        Parser.out.println("java -class mate.jar is2.lemmatizer.Lemmatizer [Options]");
        Parser.out.println();
        Parser.out.println("Options:");
        Parser.out.println("");
        Parser.out.println(" -train  <file>    the corpus a model is trained on; default " + this.trainfile);
        Parser.out.println(" -test   <file>    the input corpus for testing; default " + this.testfile);
        Parser.out.println(" -out    <file>    the output corpus (result) of a test run; default " + this.outfile);
        Parser.out.println(" -model  <file>    the parsing model for traing the model is stored in the files");
        Parser.out.println("                   and for parsing the model is load from this file; default " + this.modelName);
        Parser.out.println(" -i      <number>  the number of training iterations; good numbers are 10 for smaller corpora and 6 for bigger; default " + this.numIters);
        Parser.out.println(" -count  <number>  the n first sentences of the corpus are take for the training default " + this.count);

        System.exit(0);
    }
}