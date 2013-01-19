package clear.experiment;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.morph.MorphEnAnalyzer;
import clear.reader.SRLReader;
import clear.util.IOUtil;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

public class ExtractClusterFeature {

    final String SLOT_TRUE = " 1";
    final String SLOT_FALSE = " 0";
    final String[] ARR_DEPREL = {"DTV", "OBJ", "OPRD", "PRD", "ADV", "BNF", "DIR", "EXT", "LOC", "MNR", "PRP", "TMP"};
    final String[] ARR_IN = {"of", "to", "in", "for", "on", "at", "by", "from", "with", "as"};
    final String[] ARR_PRT = {"up", "out", "off", "down", "in", "back", "on", "over", "away", "around"};

    public ExtractClusterFeature(String dicFile, String inputFile, String outputFile) {
        MorphEnAnalyzer morph = new MorphEnAnalyzer(dicFile);
        SRLReader reader = new SRLReader(inputFile, true);
        PrintStream fout = IOUtil.createPrintFileStream(outputFile);
        DepTree tree;
        DepNode node;

        while ((tree = reader.nextTree()) != null) {
            for (int i = 1; i < tree.size(); i++) {
                node = tree.get(i);

                if (node.isPredicate()) {
                    fout.println(getFeatures(morph, tree, node));
                }
            }
        }

        fout.close();
    }

    String getFeatures(MorphEnAnalyzer morph, DepTree tree, DepNode pred) {
        StringBuilder build = new StringBuilder();

        build.append(morph.getLemma(pred.form, "VB"));
        build.append(" ");
        build.append(pred.getFeat("vn"));	// VerbNet class

        ArrayList<DepNode> aDeps = tree.getDependents(pred.id);
        HashSet<String> sDeprels = tree.getDeprelDepSet(pred.id);
        int nSlot = 0;

        nSlot += getOBJ2Feature(aDeps, build);
        nSlot += getDeprelFeatures(sDeprels, build);
        nSlot += getPrepFeatures(aDeps, build);
        nSlot += getParticleFeature(tree, pred, build);

        build.append(" ");
        build.append(nSlot);

        return build.toString();
    }

    int getOBJ2Feature(ArrayList<DepNode> aDeps, StringBuilder build) {
        int count = 0;

        for (DepNode node : aDeps) {
            if (node.isDeprel("OBJ") && ++count > 1) {
                build.append(SLOT_TRUE);
                return 1;
            }
        }

        build.append(SLOT_FALSE);
        return 0;
    }

    int getDeprelFeatures(HashSet<String> sDeprels, StringBuilder build) {
        int count = 0;

        for (String deprel : ARR_DEPREL) {
            if (sDeprels.contains(deprel)) {
                build.append(SLOT_TRUE);
                count++;
            } else {
                build.append(SLOT_FALSE);
            }
        }

        return count;
    }

    int getPrepFeatures(ArrayList<DepNode> aDeps, StringBuilder build) {
        HashSet<String> sIN = new HashSet<>();

        for (DepNode node : aDeps) {
            if (node.isPos("IN") && !node.isDeprel("PRT")) {
                sIN.add(node.lemma);
            }
        }

        int count = 0;

        for (String in : ARR_IN) {
            if (sIN.contains(in)) {
                build.append(SLOT_TRUE);
                count++;
            } else {
                build.append(SLOT_FALSE);
            }
        }

        return count;
    }

    int getParticleFeature(DepTree tree, DepNode pred, StringBuilder build) {
        String nPrt = tree.getPRT(pred.id);
        int count = 0;

        for (String prt : ARR_PRT) {
            if (prt.equals(nPrt)) {
                build.append(SLOT_TRUE);
                count++;
            } else {
                build.append(SLOT_FALSE);
            }
        }

        return count;
    }

    static public void main(String[] args) {
        String dicFile = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        new ExtractClusterFeature(dicFile, inputFile, outputFile);
    }
}