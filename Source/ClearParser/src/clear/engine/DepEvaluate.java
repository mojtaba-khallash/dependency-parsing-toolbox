package clear.engine;

import clear.dep.DepEval;
import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.reader.AbstractReader;
import clear.reader.DepReader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class DepEvaluate {

    @Option(name = "-g", usage = "gold-standard file", required = true, metaVar = "REQUIRED")
    private String s_goldFile;
    @Option(name = "-s", usage = "system file", required = true, metaVar = "REQUIRED")
    private String s_sysFile;
    @Option(name = "-b", usage = "1: skip unclassified dependencies (default = 0)", metaVar = "OPTIONAL")
    private byte b_skip = 0;
    private DepEval d_eval;

    public DepEvaluate(String args[]) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);

            AbstractReader<DepNode, DepTree> gReader = new DepReader(s_goldFile, true);
            AbstractReader<DepNode, DepTree> sReader = new DepReader(s_sysFile, true);
            DepTree gTree, sTree;
            d_eval = new DepEval(b_skip);

            while ((gTree = gReader.nextTree()) != null) {
                sTree = sReader.nextTree();
                if (sTree == null) {
                    System.err.println("More tree needed in '" + s_sysFile + "'");
                    System.exit(1);
                }

                d_eval.evaluate(gTree, sTree);
            }

            d_eval.printTotal();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        }
    }

    public double getLas() {
        return d_eval.getLas();
    }

    public double getUas() {
        return d_eval.getUas();
    }

    public double getLs() {
        return d_eval.getLs();
    }

    static public void main(String[] args) {
        DepEvaluate depEvaluate = new DepEvaluate(args);
    }
}