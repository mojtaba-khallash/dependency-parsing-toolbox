package clear.engine;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLEval;
import clear.reader.AbstractReader;
import clear.reader.SRLReader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class SRLEvaluate {

    @Option(name = "-g", usage = "gold-standard file", required = true, metaVar = "REQUIRED")
    private String s_goldFile;
    @Option(name = "-s", usage = "system file", required = true, metaVar = "REQUIRED")
    private String s_sysFile;
    private SRLEval d_eval;

    public SRLEvaluate(String args[]) {
        CmdLineParser cmd = new CmdLineParser(this);

        try {
            cmd.parseArgument(args);

            AbstractReader<DepNode, DepTree> gReader = new SRLReader(s_goldFile, true);
            AbstractReader<DepNode, DepTree> sReader = new SRLReader(s_sysFile, true);
            DepTree gTree, sTree;
            d_eval = new SRLEval();

            while ((gTree = gReader.nextTree()) != null) {
                sTree = sReader.nextTree();

                if (sTree == null) {
                    System.err.println("More tree needed in '" + s_sysFile + "'");
                    System.exit(1);
                }

                d_eval.evaluate(gTree, sTree);
            }

            d_eval.print();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            cmd.printUsage(System.err);
        }
    }

    public double getF1() {
        return d_eval.getF1();
    }

    static public void main(String[] args) {
        new SRLEvaluate(args);
    }
}