package is2.tag;

import is2.parser.Parser;
import is2.util.OptionsSuper;
import java.io.File;

public final class Options extends OptionsSuper {

    public Options(String[] args) {

        for (int i = 0; i < args.length; i++) {
            String[] pair = args[i].split(":");
            
            switch (pair[0]) {
                case "--help":
                    explain("-class mate.jar is2.parser.Parser", "-class mate.jar is2.parser.Parser", true);
                    break;
                case "-train":
                    train = true;
                    trainfile = args[i + 1];
                    break;
                case "-eval":
                    eval = true;
                    goldfile = args[i + 1];
                    i++;
                    break;
                case "-test":
                    test = true;
                    testfile = args[i + 1];
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
                case "-decode":
                    decodeProjective = args[i + 1].equals("proj");
                    i++;
                    break;
                case "-confidence":
                    conf = true;
                    break;
                case "-count":
                    count = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-model":
                    modelName = args[i + 1];
                    i++;
                    break;
                case "-tmp":
                    tmp = args[i + 1];
                    i++;
                    break;
                case "-format":
                    //format = args[i+1];
                    formatTask = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-allfeatures":
                    allFeatures = true;
                    break;
                case "-nonormalize":
                    normalize = false;
                    break;
                case "-nframes":
                    //format = args[i+1];
                    nbframes = args[i + 1];
                    i++;
                    break;
                case "-pframes":
                    //format = args[i+1];
                    pbframes = args[i + 1];
                    i++;
                    break;
                case "-nopred":
                    nopred = true;
                    break;
                case "-divide":
                    keep = true;
                    break;
                case "-lexicon":
                    lexicon = args[i + 1];
                    i++;
                    break;
                default:
                    super.addOption(args, i);
                    break;
            }
        }

        try {

            if (trainfile != null) {

                if (keep && tmp != null) {
                    trainforest = new File(tmp);
                    if (!trainforest.exists()) {
                        keep = false;
                    }

                } else if (tmp != null) {
                    trainforest = File.createTempFile("train", ".tmp", new File(tmp));
                    trainforest.deleteOnExit();
                } else {
                    trainforest = File.createTempFile("train", ".tmp"); //,new File("F:\\")
                    trainforest.deleteOnExit();
                }
            }
        } catch (java.io.IOException e) {
            Parser.out.println("Unable to create tmp files for feature forests!");
            Parser.out.println(e);
            System.exit(0);
        }
    }
}