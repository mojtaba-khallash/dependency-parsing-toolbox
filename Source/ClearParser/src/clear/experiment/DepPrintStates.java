package clear.experiment;

import clear.dep.DepTree;
import clear.parse.AbstractDepParser;
import clear.parse.ShiftPopParser;
import clear.reader.DepReader;

public class DepPrintStates {

    public DepPrintStates(String inputFile, String outputFile) {
        DepReader reader = new DepReader(inputFile, true);
        DepTree tree;
        ShiftPopParser parser = new ShiftPopParser(AbstractDepParser.FLAG_PRINT_TRANSITION, outputFile);

        while ((tree = reader.nextTree()) != null) {
            parser.parse(tree);
        }
    }

    static public void main(String[] args) {
        new DepPrintStates(args[0], args[1]);
    }
}