package is2.parser;

import is2.util.OptionsSuper;

public final class Options extends OptionsSuper {

    public Options(String[] args) {

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("--help")) {
                explain("-class mate.jar is2.parser.Parser", "-class mate.jar is2.parser.Parser", true);
            }
            switch (args[i]) {
                case "-decode":
                    decodeProjective = args[i + 1].equals("proj");
                    i++;
                    break;
                case "-decodeTH":
                    decodeTH = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
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
                case "-cores":
                    cores = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-no2nd":
                    no2nd = true;
                    break;
                case "-few2nd":
                    few2nd = true;
                    break;
                default:
                    super.addOption(args, i);
                    break;
            }
        }
    }
}