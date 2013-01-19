package examples;

import is2.data.SentenceData09;
import is2.parser.Options;
import is2.parser.Parser;

public class ParseOnly {

    public static void main(String[] args) {

        if (args.length == 0) {
            plain();
        }
    }

    /**
     * This example shows how to parse a sentence.
     */
    public static void plain() {

        // initialize the options 
        String[] opts = {"-model", "models/prs-eng-x.model"};
        Options options = new Options(opts);

        // create a parser
        Parser parser = new Parser(options);

        // Create a data container for a sentence
        SentenceData09 i = new SentenceData09();

        // Provide the sentence 
        i.init(new String[]{"<root>", "This", "is", "a", "test", "."});
        i.setPPos(new String[]{"<root-POS>", "DT", "VBZ", "DT", "NN", "."});

        // parse the sentence 
        SentenceData09 out = parser.apply(i);

        // output the sentence and dependency tree
        Parser.out.println(out.toString());

        // Get the parsing results
        out.getLabels();
        out.getParents();
    }
}