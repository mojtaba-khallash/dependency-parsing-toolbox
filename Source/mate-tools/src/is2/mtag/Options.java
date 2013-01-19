package is2.mtag;

import is2.util.OptionsSuper;

public final class Options extends OptionsSuper {

    public Options(String[] args) {

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("--help")) {
                explain("-cp anna.jar is2.mtag.Tagger", "-cp mate.jar is2.mtag.Tagger", false);
            }
            switch (args[i]) {
                case "-nonormalize":
                    normalize = false;
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
                default:
                    super.addOption(args, i);
                    break;
            }
        }
    }
}