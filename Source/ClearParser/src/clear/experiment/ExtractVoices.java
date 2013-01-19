package clear.experiment;

import clear.parse.VoiceDetector;
import clear.treebank.TBNode;
import clear.treebank.TBReader;
import clear.treebank.TBTree;
import java.io.File;

public class ExtractVoices {

    public ExtractVoices(String directory) {
        File dir = new File(directory);

        for (String filename : dir.list()) {
            if (!filename.endsWith(".mrg")) {
                continue;
            }

            TBReader reader = new TBReader(directory + File.separator + filename);
            TBTree tree;

            for (int treeId = 0; (tree = reader.nextTree()) != null; treeId++) {
                for (TBNode node : tree.getTerminalNodes()) {
                    int id = VoiceDetector.getPassive(node);
                    if (id == 0) {
                        continue;
                    }
                    System.out.println(filename + "\t" + treeId + "\t" + node.terminalId + "\tvo=" + id);
                    //	if (id == 1 || id == 2)	System.out.println(filename+"\t"+treeId+"\t"+node.terminalId+"\tvo=1");
                    //	else					System.out.println(filename+"\t"+treeId+"\t"+node.terminalId+"\tvo=2");
                }
            }
        }
    }

    public static void main(String[] args) {
        new ExtractVoices(args[0]);
    }
}